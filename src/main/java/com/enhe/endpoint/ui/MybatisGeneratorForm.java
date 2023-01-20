package com.enhe.endpoint.ui;

import com.enhe.endpoint.database.EFColumn;
import com.enhe.endpoint.database.EFTable;
import com.enhe.endpoint.psi.ModuleItem;
import com.google.common.base.CaseFormat;

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
    private JLabel controlModuleLabel;
    private JComboBox<ModuleItem> controlModuleComboBox;
    private JLabel controlPackageLabel;
    private JTextField controlPackage;
    private JComboBox<ModuleItem>clientModuleComboBox;
    private JTextField clientPackage;
    private JComboBox<ModuleItem> serviceImplModuleComboBox;
    private JLabel serviceModuleLabel;
    private JLabel clientPackageLabel;
    private JLabel clientModuleLabel;
    private JLabel serviceImplPackageLabel;
    private JTextField serviceImplPackage;
    private JCheckBox enableControlServiceCheckBox;
    private JTextField tableName;
    private JLabel entityNameLabel;
    private JTextField entityName;
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
        tableName.setText(table.getName());
        table.getPrimaryKeys().forEach(pk -> pkComboBox.addItem(pk));
        entityName.setText(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.getName()) + "Entity");

        // 控制器开关联动
        enableControlServiceCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                controlModuleComboBox.setEnabled(true);
                controlPackage.setEnabled(true);
                clientModuleComboBox.setEnabled(true);
                clientPackage.setEnabled(true);
                serviceImplModuleComboBox.setEnabled(true);
                serviceImplPackage.setEnabled(true);
            } else {
                controlModuleComboBox.setEnabled(false);
                controlPackage.setEnabled(false);
                clientModuleComboBox.setEnabled(false);
                clientPackage.setEnabled(false);
                serviceImplModuleComboBox.setEnabled(false);
                serviceImplPackage.setEnabled(false);
            }
        });

        // 下拉联动
        moduleComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ModuleItem selected = (ModuleItem) e.getItem();
                modules.stream()
                        .filter(m -> m.toString().equals(selected + ".service"))
                        .findFirst()
                        .ifPresent(m -> {
                            persistentModuleComboBox.setSelectedItem(m);
                            serviceImplModuleComboBox.setSelectedItem(m);
                            // 设置包目录
                            entityPackage.setText(String.format("com.enhe.dagp.%s.entity", selected));
                            mapperPackage.setText(String.format("com.enhe.dagp.%s.mapper", selected));
                            serviceImplPackage.setText(String.format("com.enhe.dagp.%s.service", selected));
                        });
                modules.stream()
                        .filter(m -> m.toString().equals(selected + ".app"))
                        .findFirst()
                        .ifPresent(m -> {
                            controlModuleComboBox.setSelectedItem(m);
                            controlPackage.setText(String.format("com.enhe.dagp.%s.controller", selected));
                        });
                modules.stream()
                        .filter(m -> m.toString().equals(selected + ".api"))
                        .findFirst()
                        .ifPresent(m -> {
                            clientModuleComboBox.setSelectedItem(m);
                            clientPackage.setText(String.format("com.enhe.dagp.%s.api.service", selected));
                        });
            }
        });

        // 下拉框
        // 主要模块
        modules.stream()
                .filter(m -> !m.toString().contains(".") && modules.stream().anyMatch(m2 -> m2.toString().equals(m + ".service")))
                .forEach(m -> moduleComboBox.addItem(m));
        // 持久层模块
        modules.stream()
                .filter(m -> m.toString().contains(".service"))
                .forEach(m -> persistentModuleComboBox.addItem(m));
        // 控制层模块
        modules.stream()
                .filter(m -> m.toString().contains(".app"))
                .forEach(m -> controlModuleComboBox.addItem(m));
        // Client 模块
        modules.stream()
                .filter(m -> m.toString().contains(".api"))
                .forEach(m -> clientModuleComboBox.addItem(m));
        // ServiceImpl 模块
        modules.stream()
                .filter(m -> m.toString().contains(".service"))
                .forEach(m -> serviceImplModuleComboBox.addItem(m));

        // TODO 根据表名默认匹配出模块

    }

    public boolean isEnableControlService() {
        return enableControlServiceCheckBox.isSelected();
    }

    public EFColumn getSelectedTableId() {
        Object selected = pkComboBox.getSelectedItem();
        return selected == null ? null : (EFColumn) selected;
    }

    public ModuleItem getSelectedModuleItem() {
        return (ModuleItem) moduleComboBox.getSelectedItem();
    }

    public ModuleItem getSelectedPersistentModuleItem() {
        return (ModuleItem) persistentModuleComboBox.getSelectedItem();
    }

    public ModuleItem getSelectedControlModuleItem() {
        return (ModuleItem) controlModuleComboBox.getSelectedItem();
    }

    public ModuleItem getSelectedClientModuleItem() {
        return (ModuleItem) clientModuleComboBox.getSelectedItem();
    }

    public ModuleItem getSelectedServiceImplModuleItem() {
        return (ModuleItem) serviceImplModuleComboBox.getSelectedItem();
    }

    public String getEntityName() {
        return entityName.getText();
    }

    public String getEntityPackageName() {
        return entityPackage.getText();
    }

    public String getMapperPackageName() {
        return mapperPackage.getText();
    }

    public String getControlPackageName() {
        return controlPackage.getText();
    }

    public String getClientPackageName() {
        return clientPackage.getText();
    }

    public String getServiceImplPackageName() {
        return serviceImplPackage.getText();
    }
}
