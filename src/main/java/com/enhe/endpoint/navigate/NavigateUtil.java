package com.enhe.endpoint.navigate;

import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.PsiNavigateUtil;

import java.awt.event.MouseEvent;
import java.util.List;

import static com.intellij.codeInsight.navigation.NavigationUtil.getPsiElementPopup;

/**
 * @author ding.shichen
 */
public class NavigateUtil {

    /**
     * kotlin Java 兼容调用，打开弹窗或者直接导航过去
     */
    public static void openTargetsMethod(List<PsiElement> elements, String title, MouseEvent e) {
        if (elements.size() == 1) {
            PsiNavigateUtil.navigate(elements.get(0));
        } else {
            getPsiElementPopup(elements.toArray(PsiElement[]::new), title).show(new RelativePoint(e));
        }
    }
}
