package com.enhe.endpoint.ui;

import com.enhe.endpoint.doc.DocService;
import com.enhe.endpoint.doc.model.Api;
import com.intellij.find.editorHeaderActions.Utils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.WindowMoveListener;
import com.intellij.ui.components.JBScrollBar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.settings.MarkdownSettings;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.enhe.endpoint.notifier.EnheNotifier.info;

/**
 * @author ding.shichen
 */
public class ApiDocPreviewForm {
    private JPanel rootPanel;
    private JPanel headPanel;
    private JPanel viewPanel;
    private JLabel docNameLabel;
    private JPanel previewParent;
    private JPanel previewToolbarPanel;
    private JPanel previewPanel;
    private JBScrollPane markdownSourceScrollPanel;
    private EditorEx markdownEditor;
    private MarkdownHtmlPanel markdownHtmlPanel;
    private JBPopup popup;
    public static final String DOC_VIEW_POPUP = "com.intellij.enhe.endpoint.popup";
    private final Document markdownDocument = EditorFactory.getInstance().createDocument("");
    public static final AtomicBoolean myIsPinned = new AtomicBoolean(false);
    private static final AtomicBoolean previewIsHtml = new AtomicBoolean(true);
    private final Project project;
    private final PsiFile psiFile;
    private final Api api;

    public ApiDocPreviewForm(Project project, PsiFile psiFile, Api api) {
        this.project = project;
        this.psiFile = psiFile;
        this.api = api;
        // UI调整
        initUI();
        initHeadToolbar();
        initMarkdownSourceScrollPanel();
        initMarkdownHtmlPanel();
        initPreviewPanel();
        initPreviewLeftToolbar();
        initPreviewRightToolbar();
        // 生成文档
        buildDoc();
        addMouseListeners();
    }

    private void addMouseListeners() {
        WindowMoveListener windowMoveListener = new WindowMoveListener(rootPanel);
        rootPanel.addMouseListener(windowMoveListener);
        rootPanel.addMouseMotionListener(windowMoveListener);
        previewToolbarPanel.addMouseListener(windowMoveListener);
        previewToolbarPanel.addMouseMotionListener(windowMoveListener);
    }

    public static ApiDocPreviewForm getInstance(Project project, PsiFile psiFile, Api api) {
        return new ApiDocPreviewForm(project, psiFile, api);
    }

    public void popup() {
        // dialog 改成 popup, 第一个为根面板，第二个为焦点面板
        popup = JBPopupFactory.getInstance().createComponentPopupBuilder(rootPanel, previewToolbarPanel)
                .setProject(project)
                .setResizable(true)
                .setMovable(true)

                .setModalContext(false)
                .setRequestFocus(true)
                .setBelongsToGlobalPopupStack(true)
                .setDimensionServiceKey(null, DOC_VIEW_POPUP, true)
                .setLocateWithinScreenBounds(false)
                // 鼠标点击外部时是否取消弹窗 外部单击, 未处于 pin 状态则可关闭
                .setCancelOnMouseOutCallback(event -> event.getID() == MouseEvent.MOUSE_PRESSED && !myIsPinned.get())

                // 单击外部时取消弹窗
                .setCancelOnClickOutside(true)
                // 在其他窗口打开时取消
                .setCancelOnOtherWindowOpen(true)
                .setCancelOnWindowDeactivation(true)
                .createPopup();
        popup.showCenteredInCurrentWindow(project);
    }


    private void initUI() {
        GuiUtils.replaceJSplitPaneWithIDEASplitter(rootPanel, true);
        // 边框
        rootPanel.setBorder(JBUI.Borders.empty());
        previewToolbarPanel.setBorder(JBUI.Borders.empty());
        previewPanel.setBorder(JBUI.Borders.empty());
        viewPanel.setBorder(JBUI.Borders.empty());
        docNameLabel.setBorder(JBUI.Borders.emptyLeft(5));
        // 设置滚动条, 总是隐藏
        JBScrollBar jbScrollBar = new JBScrollBar();
        jbScrollBar.setBackground(UIUtil.getTextFieldBackground());
        jbScrollBar.setAutoscrolls(true);
    }

    private void initHeadToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new ToggleAction("Pin", "Pin window", AllIcons.General.Pin_tab) {

            @Override
            public boolean isDumbAware() {
                return true;
            }

            @Override
            public boolean isSelected(AnActionEvent e) {
                return myIsPinned.get();
            }

            @Override
            public void setSelected(AnActionEvent e, boolean state) {
                myIsPinned.set(state);
            }

            @Override
            public ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        ActionToolbarImpl toolbar = (ActionToolbarImpl) ActionManager.getInstance()
                .createActionToolbar("DocViewRootToolbar", group, true);
        toolbar.setTargetComponent(headPanel);

        toolbar.setForceMinimumSize(true);
        toolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
        Utils.setSmallerFontForChildren(toolbar);

        headPanel.add(toolbar.getComponent(), BorderLayout.EAST);
        docNameLabel.setText("恩核星码");
    }


    private void initMarkdownSourceScrollPanel() {
        // 会使用 velocity 渲染模版
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("md");

        final EditorHighlighter editorHighlighter =
                HighlighterFactory.createHighlighter(fileType, EditorColorsManager.getInstance().getGlobalScheme(), project);

        markdownEditor = (EditorEx) EditorFactory.getInstance().createEditor(markdownDocument, project, fileType, true);

        EditorSettings editorSettings = markdownEditor.getSettings();
        editorSettings.setAdditionalLinesCount(0);
        editorSettings.setAdditionalColumnsCount(0);
        editorSettings.setLineMarkerAreaShown(false);
        editorSettings.setLineNumbersShown(false);
        editorSettings.setVirtualSpace(false);
        editorSettings.setFoldingOutlineShown(false);

        editorSettings.setLanguageSupplier(() -> Language.findLanguageByID("Markdown"));

        markdownEditor.setHighlighter(editorHighlighter);
        markdownEditor.setBorder(JBUI.Borders.emptyLeft(5));
        markdownSourceScrollPanel = new JBScrollPane(markdownEditor.getComponent());
    }

    private void initMarkdownHtmlPanel() {
        MarkdownSettings settings = MarkdownSettings.getInstance(project);
        MarkdownHtmlPanelProvider.ProviderInfo providerInfo = settings.getPreviewPanelProviderInfo();
        MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo(providerInfo);
        // xx
        if (!JBCefApp.isSupported()) {
            // Fallback to an alternative browser-less solution
            // https://plugins.jetbrains.com/docs/intellij/jcef.html#jbcefapp
            return;
        }
        markdownHtmlPanel = provider.createHtmlPanel();
    }


    private void initPreviewPanel() {

        if (previewIsHtml.get() && JBCefApp.isSupported()) {
            previewPanel.add(markdownHtmlPanel.getComponent(), BorderLayout.CENTER);
        } else {
            // 展示源码
            previewPanel.add(markdownSourceScrollPanel, BorderLayout.CENTER);
        }

    }


    private void initPreviewLeftToolbar() {

        DefaultActionGroup leftGroup = new DefaultActionGroup();

        leftGroup.add(new ToggleAction("预览", "Preview markdown", AllIcons.Actions.Preview) {

            @Override
            public boolean isDumbAware() {
                return true;
            }

            @Override
            public boolean isSelected(AnActionEvent e) {
                return previewIsHtml.get();
            }

            @Override
            public void setSelected(AnActionEvent e, boolean state) {

                if (!JBCefApp.isSupported()) {
                    // 不支持 JCEF 不允许预览
                    previewIsHtml.set(false);
                    info(project, "不支持 JCEF 无法预览");
                } else {
                    previewIsHtml.set(state);
                    if (state) {
                        previewPanel.removeAll();
                        previewPanel.repaint();
                        previewPanel.add(markdownHtmlPanel.getComponent(), BorderLayout.CENTER);
                        previewPanel.revalidate();
                    } else {
                        // 展示源码
                        previewPanel.removeAll();
                        previewPanel.repaint();
                        previewPanel.add(markdownSourceScrollPanel, BorderLayout.CENTER);
                        previewPanel.revalidate();
                    }
                }
            }
        });

        ActionToolbarImpl toolbar = (ActionToolbarImpl) ActionManager.getInstance()
                .createActionToolbar("DocViewEditorLeftToolbar", leftGroup, true);
        toolbar.setTargetComponent(previewToolbarPanel);
        toolbar.getComponent().setBackground(markdownEditor.getBackgroundColor());

        toolbar.setForceMinimumSize(true);
        toolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
        Utils.setSmallerFontForChildren(toolbar);

        previewToolbarPanel.setBackground(markdownEditor.getBackgroundColor());
        previewToolbarPanel.add(toolbar.getComponent(), BorderLayout.WEST);
    }

    private void initPreviewRightToolbar() {
        DefaultActionGroup rightGroup = new DefaultActionGroup();
        rightGroup.add(new AnAction("导出", "Export markdown", AllIcons.ToolbarDecorator.Export) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                popup.cancel();
                DocService service = DocService.instance(project);
                service.export(project, api.getFileName(), api.getMarkdownText());
            }
        });

        rightGroup.add(new AnAction("复制", "Copy to clipboard", AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(AnActionEvent e) {

                StringSelection selection = new StringSelection(api.getMarkdownText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                info(project, "复制成功");
            }
        });

        // init toolbar
        ActionToolbarImpl toolbar = (ActionToolbarImpl) ActionManager.getInstance()
                .createActionToolbar("DocViewEditorRightToolbar", rightGroup, true);
        toolbar.setTargetComponent(previewToolbarPanel);
        toolbar.getComponent().setBackground(markdownEditor.getBackgroundColor());

        toolbar.setForceMinimumSize(true);
        toolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
        Utils.setSmallerFontForChildren(toolbar);

        previewToolbarPanel.setBackground(markdownEditor.getBackgroundColor());
        previewToolbarPanel.add(toolbar.getComponent(), BorderLayout.EAST);

    }

    private void buildDoc() {
        if (JBCefApp.isSupported()) {
            markdownHtmlPanel.setHtml(MarkdownUtil.INSTANCE.generateMarkdownHtml(psiFile.getVirtualFile(), api.getMarkdownText(), project), 0);
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            // 光标放在顶部
            markdownDocument.setText(api.getMarkdownText());
        });
    }
}
