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

package org.netbeans.modules.javaee.wildfly.customizer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.javaee.wildfly.util.WildFlyProperties;
import org.openide.util.Exceptions;


/**
 * Customizer data support keeps models for all the customizer components, 
 * initializes them, tracks model changes and performs save.
 *
 * @author sherold
 */
public class CustomizerDataSupport {
    
    // models    
    private DefaultComboBoxModel    jvmModel;
    private Document                javaOptsModel;
    private ButtonModel             proxyModel;
    private CustomizerSupport.PathModel sourceModel;
    private CustomizerSupport.PathModel classModel;
    private CustomizerSupport.PathModel javadocModel;
    
    // model dirty flags    
    private boolean jvmModelFlag;
    private boolean javaOptsModelFlag;
    private boolean proxyModelFlag;
    private boolean sourceModelFlag;
    private boolean javadocModelFlag;
    
    private WildFlyProperties properties;
    
    /**
     * Creates a new instance of CustomizerDataSupport 
     */
    public CustomizerDataSupport(WildFlyProperties properties) {
        this.properties = properties;
        init();
    }
    
    /** Initialize the customizer models. */
    private void init() {
        
        // jvmModel
        jvmModel = new DefaultComboBoxModel();
        loadJvmModel();
        jvmModel.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) {
                jvmModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
            
            public void intervalAdded(ListDataEvent e) {
            }

            public void intervalRemoved(ListDataEvent e) {
            }
        });
        
        // javaOptions
        javaOptsModel = createDocument(properties.getJavaOpts());
        javaOptsModel.addDocumentListener(new ModelChangeAdapter() {
            public void modelChanged() {
                javaOptsModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // proxyModel
        proxyModel = createToggleButtonModel(properties.getProxyEnabled());
        proxyModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                proxyModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // classModel
        classModel = new CustomizerSupport.PathModel(properties.getClasses());
        
        // sourceModel
        sourceModel = new CustomizerSupport.PathModel(properties.getSources());
        sourceModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                sourceModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // javadocModel
        javadocModel = new CustomizerSupport.PathModel(properties.getJavadocs());
        javadocModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                javadocModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
    }
    
    /** Update the jvm model */
    public void loadJvmModel() {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatformAdapter curJvm = (JavaPlatformAdapter)jvmModel.getSelectedItem();
        String curPlatformName = null;
        if (curJvm != null) {
            curPlatformName = curJvm.getName();
        } else {
            curPlatformName = (String)properties.getJavaPlatform().getProperties().get(WildFlyProperties.PLAT_PROP_ANT_NAME);
        }

        jvmModel.removeAllElements();
        
        // feed the combo with sorted platform list
        JavaPlatform[] j2sePlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        JavaPlatformAdapter[] platformAdapters = new JavaPlatformAdapter[j2sePlatforms.length];
        for (int i = 0; i < platformAdapters.length; i++) {
            platformAdapters[i] = new JavaPlatformAdapter(j2sePlatforms[i]);
        }
        Arrays.sort(platformAdapters);
        for (int i = 0; i < platformAdapters.length; i++) {
            JavaPlatformAdapter platformAdapter = platformAdapters[i];
            jvmModel.addElement(platformAdapter);
            // try to set selected item
            if (curPlatformName != null) {
                if (curPlatformName.equals(platformAdapter.getName())) {
                    jvmModel.setSelectedItem(platformAdapter);
                }
            }   
        }
    }
    
    // model getters ----------------------------------------------------------
        
    public DefaultComboBoxModel getJvmModel() {
        return jvmModel;
    }
    
    public Document getJavaOptsModel() {
        return javaOptsModel;
    }
    
    public ButtonModel getProxyModel() {
        return proxyModel;
    }
    
    public CustomizerSupport.PathModel getClassModel() {
        return classModel;
    }
    
    public CustomizerSupport.PathModel getSourceModel() {
        return sourceModel;
    }
    
    public CustomizerSupport.PathModel getJavadocsModel() {
        return javadocModel;
    }
    
    // private helper methods -------------------------------------------------
    
    /** Save all changes */
    private void store() {
        
        if (jvmModelFlag) {
            JavaPlatformAdapter platformAdapter = (JavaPlatformAdapter)jvmModel.getSelectedItem();
            properties.setJavaPlatform(platformAdapter.getJavaPlatform());
            jvmModelFlag = false;
        }
        
        if (javaOptsModelFlag) {
            properties.setJavaOpts(getText(javaOptsModel));
            javaOptsModelFlag = false;
        }
        
        if (proxyModelFlag) {
            properties.setProxyEnabled(proxyModel.isSelected());
            proxyModelFlag = false;
        }
        
        if (sourceModelFlag) {
            properties.setSources(sourceModel.getData());
            sourceModelFlag = false;
        }
        
        if (javadocModelFlag) {
            properties.setJavadocs(javadocModel.getData());
            javadocModelFlag = false;
        }
    }
    
    /** Create a Document initialized by the specified text parameter, which may be null */
    private Document createDocument(String text) {
        PlainDocument doc = new PlainDocument();
        if (text != null) {
            try {
                doc.insertString(0, text, null);
            } catch(BadLocationException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return doc;
    }
    
    /** Get the text value from the document */
    private String getText(Document doc) {
        try {
            return doc.getText(0, doc.getLength());
        } catch(BadLocationException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
    
    /** Create a ToggleButtonModel inilialized by the specified selected parameter. */
    private JToggleButton.ToggleButtonModel createToggleButtonModel(boolean selected) {
        JToggleButton.ToggleButtonModel model = new JToggleButton.ToggleButtonModel();
        model.setSelected(selected);
        return model;
    }
        
    // private helper class ---------------------------------------------------
    
    /** 
     * Adapter that implements several listeners, which is useful for dirty model
     * monitoring.
     */
    private abstract class ModelChangeAdapter implements ListDataListener, 
            DocumentListener, ItemListener, ChangeListener {
        
        public abstract void modelChanged();
        
        public void contentsChanged(ListDataEvent e) {
            modelChanged();
        }

        public void intervalAdded(ListDataEvent e) {
            modelChanged();
        }

        public void intervalRemoved(ListDataEvent e) {
            modelChanged();
        }

        public void changedUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void itemStateChanged(ItemEvent e) {
            modelChanged();
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            modelChanged();
        }
    }
    
    /** Java platform combo box model helper */
    private static class JavaPlatformAdapter implements Comparable {
        private JavaPlatform platform;
        
        public JavaPlatformAdapter(JavaPlatform platform) {
            this.platform = platform;
        }
        
        public JavaPlatform getJavaPlatform() {
            return platform;
        }
        
        public String getName() {
            return (String)platform.getProperties().get(WildFlyProperties.PLAT_PROP_ANT_NAME);
        }
        
        public String toString() {
            return platform.getDisplayName();
        }
        
        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
    }
}
