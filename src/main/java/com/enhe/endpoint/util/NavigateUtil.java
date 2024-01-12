package com.enhe.endpoint.util;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.PsiNavigateUtil;

import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author ding.shichen
 */
public class NavigateUtil {

    public static void openTargetsMethod(List<PsiElement> elements, String title, MouseEvent e) {
        if (elements.size() == 1) {
            PsiNavigateUtil.navigate(elements.get(0));
        } else {
            NavigationUtil.getPsiElementPopup(elements.toArray(PsiElement[]::new), title).show(new RelativePoint(e));
        }
    }
}
