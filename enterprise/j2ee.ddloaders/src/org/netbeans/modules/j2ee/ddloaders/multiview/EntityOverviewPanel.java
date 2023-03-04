/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EntityOverviewForm;
import org.netbeans.modules.xml.multiview.ItemComboBoxHelper;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ItemCheckBoxHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * @author pfiala
 */
public class EntityOverviewPanel extends EntityOverviewForm {

    private XmlMultiViewDataSynchronizer synchronizer;
    private Entity entity;
    private static final String PK_COMPOUND = Utils.getBundleMessage("LBL_Compound_PK");
    private EntityHelper entityHelper;

    /**
     * Creates new form EntityOverviewForm
     */
    public EntityOverviewPanel(SectionNodeView sectionNodeView, final Entity entity, final EntityHelper entityHelper) {
        super(sectionNodeView);
        this.entityHelper = entityHelper;
        synchronizer = ((EjbJarMultiViewDataObject) sectionNodeView.getDataObject()).getModelSynchronizer();

        JTextField ejbNameTextField = getEjbNameTextField();
        JTextField persistenceTypeTextField = getPersistenceTypeTextField();
        JTextField abstractSchemaNameTextField = getAbstractSchemaNameTextField();
        JLabel primaryKeyFieldLabel = getPrimaryKeyFieldLabel();
        final JComboBox primaryKeyFieldComboBox = getPrimaryKeyFieldComboBox();
        final JComboBox primaryKeyClassComboBox = getPrimaryKeyClassComboBox();
        final JTextField primaryKeyClassTextField = getPrimaryKeyClassTextField();

        addRefreshable(new ItemEditorHelper(ejbNameTextField, new TextItemEditorModel(synchronizer, false) {
            protected String getValue() {
                return entity.getEjbName();
            }

            protected void setValue(String value) {
                entity.setEjbName(value);
            }
        }));
        ejbNameTextField.setEditable(false);

        persistenceTypeTextField.setEditable(false);
        this.entity = entity;
        String persistenceType = this.entity.getPersistenceType();
        boolean isCmp = Entity.PERSISTENCE_TYPE_CONTAINER.equals(persistenceType);
        persistenceTypeTextField.setText(persistenceType + ((isCmp ? " (CMP)" : " (BMP)")));    //NOI18N

        addRefreshable(new ItemEditorHelper(abstractSchemaNameTextField, new TextItemEditorModel(synchronizer, true) {
            protected String getValue() {
                return entity.getAbstractSchemaName();
            }

            protected void setValue(String value) {
                entity.setAbstractSchemaName(value);
            }
        }));
        abstractSchemaNameTextField.setEditable(false);

        if (isCmp) {
            primaryKeyFieldLabel.setVisible(true);
            primaryKeyFieldComboBox.setVisible(true);
            primaryKeyClassComboBox.setVisible(true);
            primaryKeyClassTextField.setVisible(false);

            initPrimaryKeyFieldComboBox();
            final ItemComboBoxHelper primaryKeyComboBoxHelper = new ItemComboBoxHelper(synchronizer,
                    primaryKeyFieldComboBox) {
                public String getItemValue() {
                    String value = entity.getPrimkeyField();
                    return value == null ? PK_COMPOUND : value;
                }

                public void setItemValue(String value) {
                    try {
                        entityHelper.setPrimkeyField(value == PK_COMPOUND ? null : value);
                    } catch (ClassNotFoundException e) {
                        Utils.notifyError(e);
                    }
                }
            };
            addRefreshable(primaryKeyComboBoxHelper);
            primaryKeyFieldComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int selectedIndex = primaryKeyFieldComboBox.getSelectedIndex();
                    if (selectedIndex == 0) {
                        primaryKeyClassComboBox.setEnabled(true);
                        primaryKeyClassComboBox.setSelectedItem(entity.getPrimKeyClass());
                    } else {
                        primaryKeyClassComboBox.setEnabled(false);
                    }
                    primaryKeyClassComboBox.setSelectedItem(entity.getPrimKeyClass());
                }
            });
            primaryKeyClassComboBox.setEnabled(primaryKeyFieldComboBox.getSelectedIndex() == 0);

            primaryKeyClassComboBox.addItem("boolean");             //NOI18N
            primaryKeyClassComboBox.addItem("byte");                //NOI18N
            primaryKeyClassComboBox.addItem("char");                //NOI18N
            primaryKeyClassComboBox.addItem("double");              //NOI18N
            primaryKeyClassComboBox.addItem("float");               //NOI18N
            primaryKeyClassComboBox.addItem("int");                 //NOI18N
            primaryKeyClassComboBox.addItem("long");                //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Boolean");   //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Byte");      //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Character"); //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Double");    //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Float");     //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Integer");   //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Long");      //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.Object");    //NOI18N
            primaryKeyClassComboBox.addItem("java.lang.String");    //NOI18N
            primaryKeyClassComboBox.addItem("java.math.BigDecimal");//NOI18N

            addRefreshable(new ItemComboBoxHelper(synchronizer, primaryKeyClassComboBox) {
                public String getItemValue() {
                    return entity.getPrimKeyClass();
                }

                public void setItemValue(String value) {
                    if (!Utils.isValidPackageName(value)) {
                        primaryKeyComboBoxHelper.refresh();
                    }
                }
            });

        } else {
            primaryKeyFieldLabel.setVisible(false);
            primaryKeyFieldComboBox.setVisible(false);
            primaryKeyClassComboBox.setVisible(false);
            primaryKeyClassTextField.setVisible(true);

            addRefreshable(new ItemEditorHelper(primaryKeyClassTextField, new TextItemEditorModel(synchronizer, false) {
                protected String getValue() {
                    return entity.getPrimKeyClass();
                }

                protected void setValue(String value) {
                    entity.setPrimKeyClass(value);
                }
            }));
        }
        addRefreshable(new ItemCheckBoxHelper(synchronizer, getReentrantCheckBox()) {
            public boolean getItemValue() {
                return entity.isReentrant();
            }

            public void setItemValue(boolean value) {
                entity.setReentrant(value);
            }
        });
    }

    private void initPrimaryKeyFieldComboBox() {
        final JComboBox primaryKeyFieldComboBox = getPrimaryKeyFieldComboBox();
        CmpField[] cmpFields = entityHelper.cmpFields.getCmpFields();
        String[] items = new String[cmpFields.length + 1];
        items[0] = PK_COMPOUND;
        for (int i = 0; i < cmpFields.length; i++) {
            items[i+1] = cmpFields[i].getFieldName();
        }
        primaryKeyFieldComboBox.setModel(new DefaultComboBoxModel(items));
    }

    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }

    @Override
    public void refreshView() {
        initPrimaryKeyFieldComboBox();
        super.refreshView();
        getReentrantCheckBox().setSelected(entity.isReentrant());
    }
}
