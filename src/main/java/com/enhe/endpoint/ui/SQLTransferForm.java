package com.enhe.endpoint.ui;

import com.enhe.sql.DBProduct;
import com.enhe.sql.Feature;
import com.enhe.sql.MySQLTransfer;
import com.enhe.sql.SQLTransfer;
import com.enhe.sql.model.IScript;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author ding.shichen
 */
public class SQLTransferForm {
    private JPanel root;
    private JPanel descPanel;
    private JLabel descLabel;
    private JPanel contentPanel;
    private JSplitPane splitPane;
    private JScrollPane sourcePane;
    private JButton dmButton;
    private JButton gsButton;
    private JButton copyButton;
    private JTextArea sourceArea;
    private JPanel splitRightPanel;
    private JPanel buttonPanel;
    private JScrollPane targetPane;
    private JTextArea targetArea;
    private SQLTransfer sqlTransfer;

    public SQLTransferForm() {
        initUI();
        initListener();
    }

    private void initUI() {
        GuiUtils.replaceJSplitPaneWithIDEASplitter(root, true);
        sourceArea.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
        targetArea.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
        descLabel.setText("支持 MySQL 以下语句：" + Feature.getComment());
    }

    private void initListener() {
        sqlTransfer = new MySQLTransfer();
        dmButton.addActionListener(e -> {
            IScript script = sqlTransfer.transform(sourceArea.getText(), DBProduct.DM8);
            targetArea.setText(String.join("\n\n", script.getTexts()));
        });
        gsButton.addActionListener(e -> {
            IScript script = sqlTransfer.transform(sourceArea.getText(), DBProduct.GaussDB);
            targetArea.setText(String.join("\n\n", script.getTexts()));
        });
        copyButton.addActionListener(e -> CopyPasteManager.getInstance().setContents(new StringSelection(targetArea.getText())));
    }

    public JPanel getRoot() {
        return root;
    }
}
