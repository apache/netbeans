/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.MessageDrivenOverviewForm;
import org.netbeans.modules.xml.multiview.ItemComboBoxHelper;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ItemOptionHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class MessageDrivenOverviewPanel extends MessageDrivenOverviewForm {

    private ActivationConfig config;
    private static final String PROPERTY_MESSAGE_SELECTOR = "messageSelector";  //NOI18N
    private static final String PROPERTY_ACKNOWLEDGE_NAME = "acknowledgeMode";  //NOI18N
    private static final String PROPERTY_SUBSCRIPTION_DURABILITY = "subscriptionDurability";  //NOI18N
    private static final String DESTINATION_TYPE_TOPIC = "javax.jms.Topic";  //NOI18N
    private static final String DESTINATION_TYPE_QUEUE = "javax.jms.Queue";  //NOI18N
    private static final String SUBSCRIPTION_DURABILITY_NONDURABLE = "NonDurable";  //NOI18N
    private static final String SUBSCRIPTION_DURABILITY_DURABLE = "Durable";  //NOI18N
    private static final String DESTINATION_TYPE = "DestinationType";  //NOI18N

    /**
     * @param sectionNodeView enclosing SectionNodeView object
     */
    public MessageDrivenOverviewPanel(SectionNodeView sectionNodeView, final MessageDriven messageDriven) {
        super(sectionNodeView);

        final EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();

        XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
        addRefreshable(new ItemEditorHelper(getNameTextField(), new TextItemEditorModel(synchronizer,
                false) {

            protected String getValue() {
                return messageDriven.getEjbName();
            }

            protected void setValue(String value) {
                messageDriven.setEjbName(value);
            }
        }));
        getNameTextField().setEditable(false);

        addRefreshable(new ItemOptionHelper(synchronizer, getTransactionTypeButtonGroup()) {
            public String getItemValue() {
                return messageDriven.getTransactionType();
            }

            public void setItemValue(String value) {
                messageDriven.setTransactionType(value);
            }
        });

        config = getActivationConfig(messageDriven);

        final JTextField messageSelectorTextField = getMessageSelectorTextField();

        final JComboBox destinationTypeComboBox = getDestinationTypeComboBox();
        destinationTypeComboBox.addItem(DESTINATION_TYPE_TOPIC);
        destinationTypeComboBox.addItem(DESTINATION_TYPE_QUEUE);

        final JComboBox durabilityComboBox = getDurabilityComboBox();
        durabilityComboBox.addItem(SUBSCRIPTION_DURABILITY_NONDURABLE);
        durabilityComboBox.addItem(SUBSCRIPTION_DURABILITY_DURABLE);

        if (config == null) {
            durabilityComboBox.setEnabled(false);
            messageSelectorTextField.setEnabled(false);
        } else {
            addRefreshable(new ItemEditorHelper(messageSelectorTextField,
                            new TextItemEditorModel(synchronizer, true, true) {
                protected String getValue() {
                    return getConfigProperty(PROPERTY_MESSAGE_SELECTOR);
                }

                protected void setValue(String value) {
                    setConfigProperty(PROPERTY_MESSAGE_SELECTOR, value);
                }
            }));

            addRefreshable(new ItemOptionHelper(synchronizer, getAcknowledgeModeButtonGroup()) {
                public String getItemValue() {
                    return getConfigProperty(PROPERTY_ACKNOWLEDGE_NAME, "Auto-acknowledge");//NOI18N
                }

                public void setItemValue(String value) {
                    setConfigProperty(PROPERTY_ACKNOWLEDGE_NAME, value);
                }
            });

            final DurabilityComboBoxHelper durabilityComboBoxHelper = new DurabilityComboBoxHelper(synchronizer, durabilityComboBox);

            new ItemComboBoxHelper(synchronizer, destinationTypeComboBox) {
                {
                    setDurabilityEnabled();
                }

                public String getItemValue() {
                    return getConfigProperty(DESTINATION_TYPE);
                }

                public void setItemValue(String value) {
                    setConfigProperty(DESTINATION_TYPE, value);
                    setDurabilityEnabled();
                }

                private void setDurabilityEnabled() {
                    durabilityComboBoxHelper.setComboBoxEnabled(DESTINATION_TYPE_TOPIC.equals(getItemValue()));
                }
            };

        }

        // the second ItemComboboxHelper for destinationTypeComboBox handles message-destination-type element
        new ItemComboBoxHelper(synchronizer, destinationTypeComboBox) {

            public String getItemValue() {
                try {
                    return messageDriven.getMessageDestinationType();
                } catch (VersionNotSupportedException e) {
                    return null;
                }
            }

            public void setItemValue(String value) {
                try {
                    messageDriven.setMessageDestinationType(value);
                } catch (VersionNotSupportedException e) {
                    // ignore
                }
            }

        };

    }

    private ActivationConfig getActivationConfig(final MessageDriven messageDriven) {
        ActivationConfig ac;

        try {
            ac = messageDriven.getActivationConfig();
        } catch (VersionNotSupportedException e1) {
            ac = null;
        }
        return ac;
    }

    private String getConfigProperty(String propertyName) {
        return getConfigProperty(propertyName, null);
    }

    private String getConfigProperty(String propertyName, String defaultValue) {
        ActivationConfigProperty[] properties = config.getActivationConfigProperty();
        String value = null;
        for (int i = 0; i < properties.length; i++) {
            ActivationConfigProperty property = properties[i];
            if (propertyName.equalsIgnoreCase(property.getActivationConfigPropertyName())) {
                value = property.getActivationConfigPropertyValue();
                break;
            }
        }
        return value == null ? defaultValue : value;
    }

    private void setConfigProperty(String propertyName, String propertyValue) {
        ActivationConfigProperty[] properties = config.getActivationConfigProperty();
        for (int i = 0; i < properties.length; i++) {
            ActivationConfigProperty property = properties[i];
            if (propertyName.equalsIgnoreCase(property.getActivationConfigPropertyName())) {
                if (propertyValue != null) {
                    property.setActivationConfigPropertyValue(propertyValue);
                } else {
                    config.removeActivationConfigProperty(property);
                }
                signalUIChange();
                return;
            }
        }
        if (propertyValue != null) {
            ActivationConfigProperty property = config.newActivationConfigProperty();
            property.setActivationConfigPropertyName(propertyName);
            property.setActivationConfigPropertyValue(propertyValue);
            config.addActivationConfigProperty(property);
        }
    }

    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        scheduleRefreshView();
    }

    private class DurabilityComboBoxHelper extends ItemComboBoxHelper {

        public DurabilityComboBoxHelper(XmlMultiViewDataSynchronizer synchronizer, JComboBox durabilityComboBox) {
            super(synchronizer, durabilityComboBox);
        }

        public String getItemValue() {
            return getConfigProperty(PROPERTY_SUBSCRIPTION_DURABILITY, "NonDurable");//NOI18N
        }

        public void setItemValue(String value) {
            setConfigProperty(PROPERTY_SUBSCRIPTION_DURABILITY, value);
        }

        public void setComboBoxEnabled(boolean enabled) {
            getComboBox().setEnabled(enabled);
            setValue(enabled ? getItemValue() : null);
        }
    }
}
