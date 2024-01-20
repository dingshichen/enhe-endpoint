package com.enhe.endpoint.ui;

import com.enhe.endpoint.consts.DagpModule;
import com.enhe.endpoint.consts.DagpModuleKt;
import com.enhe.endpoint.database.model.*;
import com.enhe.endpoint.extend.ModuleItem;
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
    private JComboBox<EColumn> pkComboBox;
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
    private JCheckBox enableTempImplCheckBox;
    private JCheckBox pageCheckBox;
    private JCheckBox listAllCheckBox;
    private JCheckBox selectCheckBox;
    private JCheckBox fillCheckBox;
    private JCheckBox loadCheckBox;
    private JCheckBox insertCheckBox;
    private JCheckBox updateCheckBox;
    private JCheckBox deleteCheckBox;
    private JCheckBox impCheckBox;
    private JCheckBox expCheckBox;
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
        pkComboBox.addItem(new EColumnOption());
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
                enableTempImplCheckBox.setEnabled(true);
            } else {
                controlModuleComboBox.setEnabled(false);
                controlPackage.setEnabled(false);
                clientModuleComboBox.setEnabled(false);
                clientPackage.setEnabled(false);
                serviceImplModuleComboBox.setEnabled(false);
                serviceImplPackage.setEnabled(false);
                enableTempImplCheckBox.setSelected(false);
                enableTempImplCheckBox.setEnabled(false);
            }
        });

        // 实现模版接口开关联动
        enableTempImplCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pageCheckBox.setEnabled(true);
                listAllCheckBox.setEnabled(true);
                selectCheckBox.setEnabled(true);
                fillCheckBox.setEnabled(true);
                loadCheckBox.setEnabled(true);
                insertCheckBox.setEnabled(true);
                updateCheckBox.setEnabled(true);
                deleteCheckBox.setEnabled(true);
                impCheckBox.setEnabled(true);
                expCheckBox.setEnabled(true);
            } else {
                pageCheckBox.setEnabled(false);
                listAllCheckBox.setEnabled(false);
                selectCheckBox.setEnabled(false);
                fillCheckBox.setEnabled(false);
                loadCheckBox.setEnabled(false);
                insertCheckBox.setEnabled(false);
                updateCheckBox.setEnabled(false);
                deleteCheckBox.setEnabled(false);
                impCheckBox.setEnabled(false);
                expCheckBox.setEnabled(false);
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
        modules.stream()
                .filter(m -> {
                    DagpModule dagpModule = DagpModuleKt.ofDagpModule(m.toString());
                    if (dagpModule == null) {
                        return false;
                    }
                    switch (dagpModule) {
                        case ASSESSMENT:
                            return table.getName().startsWith(DagpModule.ASSESSMENT.tableNamePrefix());
                        case ASSET:
                            return table.getName().startsWith(DagpModule.ASSET.tableNamePrefix());
                        case MASTER:
                            return table.getName().startsWith(DagpModule.MASTER.tableNamePrefix());
                        case METADATA:
                            return table.getName().startsWith(DagpModule.METADATA.tableNamePrefix());
                        case MODEL2:
                            return table.getName().startsWith(DagpModule.MODEL2.tableNamePrefix()) || table.getName().startsWith("ar_") || table.getName().startsWith("ar2_");
                        case QUALITY:
                            return table.getName().startsWith(DagpModule.QUALITY.tableNamePrefix());
                        case REQUIREMENT:
                            return table.getName().startsWith(DagpModule.REQUIREMENT.tableNamePrefix());
                        case STANDARD:
                            return table.getName().startsWith(DagpModule.STANDARD.tableNamePrefix());
                        case PROFILE:
                            return table.getName().startsWith(DagpModule.PROFILE.tableNamePrefix());
                        case SYSTEM:
                            return table.getName().startsWith(DagpModule.SYSTEM.tableNamePrefix());
                        default:
                            return false;
                    }
                })
                .findFirst()
                .ifPresent(m -> moduleComboBox.setSelectedItem(m));

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

    public JTextField getEntityName() {
        return entityName;
    }

    public JTextField getEntityPackage() {
        return entityPackage;
    }

    public JTextField getMapperPackage() {
        return mapperPackage;
    }

    public JTextField getControlPackage() {
        return controlPackage;
    }

    public JTextField getClientPackage() {
        return clientPackage;
    }

    public JTextField getServiceImplPackage() {
        return serviceImplPackage;
    }

    public JCheckBox getEnableTempImplCheckBox() {
        return enableTempImplCheckBox;
    }

    public JCheckBox getPageCheckBox() {
        return pageCheckBox;
    }

    public JCheckBox getListAllCheckBox() {
        return listAllCheckBox;
    }

    public JCheckBox getSelectCheckBox() {
        return selectCheckBox;
    }

    public JCheckBox getFillCheckBox() {
        return fillCheckBox;
    }

    public JCheckBox getLoadCheckBox() {
        return loadCheckBox;
    }

    public JCheckBox getInsertCheckBox() {
        return insertCheckBox;
    }

    public JCheckBox getUpdateCheckBox() {
        return updateCheckBox;
    }

    public JCheckBox getDeleteCheckBox() {
        return deleteCheckBox;
    }

    public JCheckBox getImpCheckBox() {
        return impCheckBox;
    }

    public JCheckBox getExpCheckBox() {
        return expCheckBox;
    }

    public boolean isEnableControlService() {
        return enableControlServiceCheckBox.isSelected();
    }

    public PersistentState getPersistentState() {
        return new PersistentState(getSelectedTableId(), getSelectedPersistentModuleItem(), getEntityNameText(),
                getEntityPackageName(), getMapperPackageName());
    }

    public ControlServiceState getControlServiceState() {
        return new ControlServiceState(getPersistentState(), getSelectedControlModuleItem(), getSelectedClientModuleItem(), getSelectedServiceImplModuleItem(),
                getControlPackageName(), getClientPackageName(), getServiceImplPackageName());
    }

    public ImplTempState getImplTempState() {
        return new ImplTempState(getPersistentState(), getControlServiceState(), enableTempImplCheckBox.isSelected(),
                pageCheckBox.isSelected(), listAllCheckBox.isSelected(), selectCheckBox.isSelected(),
                fillCheckBox.isSelected(), loadCheckBox.isSelected(), insertCheckBox.isSelected(),
                updateCheckBox.isSelected(), deleteCheckBox.isSelected(), impCheckBox.isSelected(),
                expCheckBox.isSelected());
    }

    private EFColumn getSelectedTableId() {
        Object selected = pkComboBox.getSelectedItem();
        if (selected instanceof EFColumn) {
            return (EFColumn) selected;
        }
        return null;
    }

    private ModuleItem getSelectedPersistentModuleItem() {
        return (ModuleItem) persistentModuleComboBox.getSelectedItem();
    }

    private ModuleItem getSelectedControlModuleItem() {
        return (ModuleItem) controlModuleComboBox.getSelectedItem();
    }

    private ModuleItem getSelectedClientModuleItem() {
        return (ModuleItem) clientModuleComboBox.getSelectedItem();
    }

    private ModuleItem getSelectedServiceImplModuleItem() {
        return (ModuleItem) serviceImplModuleComboBox.getSelectedItem();
    }

    private String getEntityNameText() {
        return entityName.getText();
    }

    private String getEntityPackageName() {
        return entityPackage.getText();
    }

    private String getMapperPackageName() {
        return mapperPackage.getText();
    }

    private String getControlPackageName() {
        return controlPackage.getText();
    }

    private String getClientPackageName() {
        return clientPackage.getText();
    }

    private String getServiceImplPackageName() {
        return serviceImplPackage.getText();
    }
}
