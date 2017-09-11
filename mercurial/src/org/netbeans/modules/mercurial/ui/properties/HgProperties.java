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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.properties;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Padraig O'Briain
 */
public class HgProperties implements ListSelectionListener {
    
    public static final String HGPROPNAME_USERNAME = "username"; // NOI18N
    public static final String HGPROPNAME_DEFAULT_PULL = "default-pull"; // NOI18N
    public static final String HGPROPNAME_DEFAULT_PUSH = "default-push"; // NOI18N

    private PropertiesPanel panel;
    private File root;
    private PropertiesTable propTable;
    private HgProgressSupport support;
    private File loadedValueFile;
    private Font fontTextArea;
    private HgPropertiesNode[] initHgProps;
    
    /** Creates a new instance of HgProperties */
    public HgProperties(PropertiesPanel panel, PropertiesTable propTable, File root) {
        this.panel = panel;
        this.propTable = propTable;
        this.root = root;
        propTable.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propTable.getTable().getSelectionModel().addListSelectionListener(this);
        
        refreshProperties();
    }
    
    public PropertiesPanel getPropertiesPanel() {
        return panel;
    }
    
    public void setPropertiesPanel(PropertiesPanel panel) {
        this.panel = panel;
    }
    
    public File getRoot() {
        return root;
    }
    
    public void setRoot(File root) {
        this.root = root;
    }
    
    protected String getPropertyValue() {
        return panel.txtAreaValue.getText();
    }
    
    protected void refreshProperties() {        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    Properties props = HgModuleConfig.getDefault().getProperties(root);
                    if (props == null) {
                        return;
                    }
                    HgPropertiesNode[] hgProps = new HgPropertiesNode[props.size()];
                    initHgProps = new HgPropertiesNode[props.size()];
                    int i = 0;

                    for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
                        String name = (String) e.nextElement();
                        String tmp = props.getProperty(name);
                        String value = tmp != null ? tmp : ""; // NOI18N
                        hgProps[i] = new HgPropertiesNode(name, value);
                        initHgProps[i] = new HgPropertiesNode(name, value);
                        i++;
                     }
                     propTable.setNodes(hgProps);
                }
            };
            support.start(rp, root, org.openide.util.NbBundle.getMessage(HgProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }
    
    public void setProperties() {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
                    try {
                    for (int i = 0; i < hgPropertiesNodes.length; i++) {
                            String hgPropertyName = hgPropertiesNodes[i].getName();
                            String hgPropertyValue = hgPropertiesNodes[i].getValue();
                            boolean bPropChanged = !(initHgProps[i].getValue()).equals(hgPropertyValue);
                            if (bPropChanged && hgPropertyValue.trim().length() >= 0) {
                                if (hgPropertyName.equals(HGPROPNAME_USERNAME) &&
                                        !HgModuleConfig.getDefault().isUserNameValid(hgPropertyValue)) {
                                    JOptionPane.showMessageDialog(null,
                                            NbBundle.getMessage(HgProperties.class, "MSG_WARN_USER_NAME_TEXT"), // NOI18N
                                            NbBundle.getMessage(HgProperties.class, "MSG_WARN_FIELD_TITLE"), // NOI18N
                                            JOptionPane.WARNING_MESSAGE);
                                } else {
                                    HgModuleConfig.getDefault().setProperty(root, hgPropertyName, hgPropertyValue);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        HgModuleConfig.notifyParsingError();
                    }
                    HgRepositoryContextCache.getInstance().reset();
                }
            };
            support.start(rp, root, org.openide.util.NbBundle.getMessage(HgProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }

    private int lastIndex = -1;
    
    
    public void updateLastSelection () {
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        if (lastIndex >= 0) {
            hgPropertiesNodes[lastIndex].setValue(getPropertyValue());
        }
    }

    public void valueChanged (ListSelectionEvent e) {
        int index = propTable.getTable().getSelectedRow();
        if (index < 0) {
            lastIndex = -1;
            return;
        }
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        if (lastIndex >= 0) {
            hgPropertiesNodes[lastIndex].setValue(getPropertyValue());
        }
        panel.txtAreaValue.setText(hgPropertiesNodes[index].getValue());
        lastIndex = index;
    }
}
