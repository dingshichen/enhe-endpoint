package com.enhe.endpoint.ui;

import com.enhe.endpoint.database.EFColumn;
import com.enhe.endpoint.database.EFTable;
import com.enhe.endpoint.psi.ModuleItem;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * @author ding.shichen
 */
public class MybatisGeneratorForm {
    private JPanel root;
    private JLabel tableLabel;
    private JLabel pkLabel;
    private JComboBox<EFColumn> pkComboBox;
    private JLabel moduleLabel;
    private JComboBox<ModuleItem> moduleComboBox;
    private JComboBox<ModuleItem> persistentModuleComboBox;
    private JTextField entityPackage;
    private JTextField mapperPackage;
    private JLabel persistentLabel;
    private JLabel entityPackageLabel;
    private JLabel mapperPackageLabel;
    private EFTable table;
    private List<ModuleItem> modules;

    public MybatisGeneratorForm(EFTable table, List<ModuleItem> modules) {
        this.table = table;
        this.modules = modules;
        init();
    }

    public JPanel getRoot() {
        return root;
    }

    private void init() {
        tableLabel.setText(tableLabel.getText() + table.getName());
        table.getPrimaryKeys().forEach(pk -> pkComboBox.addItem(pk));
        // TODO 增加 Entity 输入框，根据表名生成，支持自定义

        // 下拉联动
        moduleComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ModuleItem selected = (ModuleItem) e.getItem();
                modules.stream()
                        .filter(m -> m.toString().equals(selected.toString() + ".service"))
                        .findFirst()
                        .ifPresent(m -> {
                            persistentModuleComboBox.setSelectedItem(m);
                            // 设置包目录
                            entityPackage.setText(String.format("com.enhe.dagp.%s.entity", selected));
                            mapperPackage.setText(String.format("com.enhe.dagp.%s.mapper", selected));
                        });
            }
        });

        // 下拉框
        // 主要模块
        modules.stream()
                .filter(m -> !m.toString().contains(".") && modules.stream().anyMatch(m2 -> m2.toString().equals(m + ".service")))
                .forEach(m -> moduleComboBox.addItem(m));
        // 主要模块的子模块
        modules.stream()
                .filter(m -> m.toString().contains(".service"))
                .forEach(m -> persistentModuleComboBox.addItem(m));
        // TODO 根据表名默认匹配出模块

    }

    public EFColumn getSelectedTableId() {
        return (EFColumn) pkComboBox.getSelectedItem();
    }

    public ModuleItem getSelectedModuleItem() {
        return (ModuleItem) moduleComboBox.getSelectedItem();
    }

    public ModuleItem getSelectedPersistentModuleItem() {
        return (ModuleItem) persistentModuleComboBox.getSelectedItem();
    }

    public String getEntityPackageName() {
        return entityPackage.getText();
    }

    public String getMapperPackageName() {
        return mapperPackage.getText();
    }
}
