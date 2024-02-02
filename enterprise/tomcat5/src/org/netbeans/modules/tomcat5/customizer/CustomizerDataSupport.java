/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tomcat5.customizer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
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
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.j2ee.TomcatPlatformImpl;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
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
    private ButtonModel             secManagerModel;        
    private Document                catalinaHomeModel;
    private Document                catalinaBaseModel;    
    private Document                usernameModel;
    private Document                passwordModel;    
    private Document                scriptPathModel;
    private ButtonModel             customScriptModel;
    private ButtonModel             forceStopModel;    
    private ButtonModel             sharedMemModel;
    private ButtonModel             socketModel;    
    private ButtonModel             monitorModel;
    private ButtonModel             proxyModel;
    private Document                sharedMemNameModel;    
    private CustomizerSupport.PathModel sourceModel;
    private CustomizerSupport.PathModel classModel;
    private CustomizerSupport.PathModel javadocModel;
    private SpinnerNumberModel      serverPortModel;
    private SpinnerNumberModel      shutdownPortModel;
    private SpinnerNumberModel      debugPortModel;
    private SpinnerNumberModel      deploymentTimeoutModel;
    private SpinnerNumberModel      startupTimeoutModel;
    private SpinnerNumberModel      shutdownTimeoutModel;
    private ButtonModel             driverDeploymentModel;
    
    // model dirty flags    
    private boolean jvmModelFlag;
    private boolean javaOptsModelFlag;
    private boolean secManagerModelFlag;
    private boolean usernameModelFlag;
    private boolean passwordModelFlag;
    private boolean scriptPathModelFlag;
    private boolean customScriptModelFlag;
    private boolean forceStopModelFlag;
    private boolean sharedMemModelFlag;
    private boolean socketModelFlag;
    private boolean monitorModelFlag;
    private boolean proxyModelFlag;
    private boolean sharedMemNameModelFlag;
    private boolean sourceModelFlag;
    private boolean javadocModelFlag;
    private boolean serverPortModelFlag;
    private boolean shutdownPortModelFlag;
    private boolean debugPortModelFlag;
    private boolean deploymentTimeoutModelFlag;
    private boolean driverDeploymentModelFlag;
    private boolean startupTimeoutModelFlag;
    private boolean shutdownTimeoutModelFlag;
    
    private TomcatProperties tp;
    private TomcatManager tm;
    
    /**
     * Creates a new instance of CustomizerDataSupport 
     */
    public CustomizerDataSupport(TomcatManager tm) {
        this.tm = tm;
        tp = tm.getTomcatProperties();
        init();
    }
    
    /** Initialize the customizer models. */
    private void init() {
        
        // jvmModel
        jvmModel = new DefaultComboBoxModel();
        loadJvmModel();
        jvmModel.addListDataListener(new ListDataListener() {
            @Override
            public void contentsChanged(ListDataEvent e) {
                jvmModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
            
            @Override
            public void intervalAdded(ListDataEvent e) {
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
            }
        });
        
        // javaOptions
        javaOptsModel = createDocument(tp.getJavaOpts());
        javaOptsModel.addDocumentListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                javaOptsModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // catalinaHomeModel
        catalinaHomeModel = createDocument(tp.getCatalinaHome().toString());
        
        // catalinaBaseModel
        catalinaBaseModel = createDocument(tp.getCatalinaDir().toString());
        
        // usernameModel
        usernameModel = createDocument(tp.getUsername());
        usernameModel.addDocumentListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                usernameModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // passwordModel
        passwordModel = createDocument(tp.getPassword());
        passwordModel.addDocumentListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                passwordModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // sharedMemNameModel
        sharedMemNameModel = createDocument(tp.getSharedMem());
        sharedMemNameModel.addDocumentListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                sharedMemNameModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });

        // scriptPathModel
        scriptPathModel = createDocument(tp.getScriptPath());
        scriptPathModel.addDocumentListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                scriptPathModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // secManagerModel
        secManagerModel = createToggleButtonModel(tp.getSecManager());
        secManagerModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                secManagerModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // customScriptModel
        customScriptModel = createToggleButtonModel(tp.getCustomScript());
        customScriptModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                customScriptModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // forceStopModel
        forceStopModel = createToggleButtonModel(tp.getForceStop());
        forceStopModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                forceStopModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // monitorModel
        monitorModel = createToggleButtonModel(tp.getMonitor());
        monitorModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                monitorModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // proxyModel
        proxyModel = createToggleButtonModel(tp.getProxyEnabled());
        proxyModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                proxyModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });

        // classModel
        classModel = new CustomizerSupport.PathModel(tp.getClasses());
        
        // sourceModel
        sourceModel = new CustomizerSupport.PathModel(tp.getSources());
        sourceModel.addListDataListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                sourceModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // javadocModel
        javadocModel = new CustomizerSupport.PathModel(tp.getJavadocs());
        javadocModel.addListDataListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                javadocModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // serverPortModel
        serverPortModel = new SpinnerNumberModel(tm.getServerPort(), 0, 65535, 1);
        serverPortModel.addChangeListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                serverPortModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // shutdownPortModel
        shutdownPortModel = new SpinnerNumberModel(tm.getShutdownPort(), 0, 65535, 1);
        shutdownPortModel.addChangeListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                shutdownPortModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // debugPortModel
        debugPortModel = new SpinnerNumberModel(tp.getDebugPort(), 0, 65535, 1);
        debugPortModel.addChangeListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                debugPortModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        ButtonGroup debugButtonGroup = new ButtonGroup();
        
        // socketModel
        socketModel = new JToggleButton.ToggleButtonModel();
        socketModel.setGroup(debugButtonGroup);
        socketModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                socketModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // sharedMemModel
        sharedMemModel = new JToggleButton.ToggleButtonModel();
        sharedMemModel.setGroup(debugButtonGroup);
        sharedMemModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                sharedMemModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        boolean socketEnabled = TomcatProperties.DEBUG_TYPE_SOCKET.equalsIgnoreCase(tp.getDebugType());
        debugButtonGroup.setSelected(socketEnabled ? socketModel : sharedMemModel, true);
        
        // deploymentTimeoutModel
        deploymentTimeoutModel = new SpinnerNumberModel(tp.getDeploymentTimeout(), 1, Integer.MAX_VALUE, 1);
        deploymentTimeoutModel.addChangeListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                deploymentTimeoutModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // driverDeploymentModel
        driverDeploymentModel = createToggleButtonModel(tp.getDriverDeployment());
        driverDeploymentModel.addItemListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                driverDeploymentModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // startupTimeoutModel
        startupTimeoutModel = new SpinnerNumberModel(tp.getStartupTimeout(), 1, Integer.MAX_VALUE, 1);
        startupTimeoutModel.addChangeListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                startupTimeoutModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // shutdownTimeoutModel
        shutdownTimeoutModel = new SpinnerNumberModel(tp.getShutdownTimeout(), 1, Integer.MAX_VALUE, 1);
        shutdownTimeoutModel.addChangeListener(new ModelChangeAdapter() {
            @Override
            public void modelChanged() {
                shutdownTimeoutModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
    }
    

    public boolean useManagerScript() {
        return tm.isAboveTomcat70();
    }

    /** Update the jvm model */
    public void loadJvmModel() {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatformAdapter curJvm = (JavaPlatformAdapter)jvmModel.getSelectedItem();
        final String curPlatformName;
        if (curJvm != null) {
            curPlatformName = curJvm.getName();
        } else {
            curPlatformName = (String)tp.getJavaPlatform().getProperties().get(TomcatProperties.PLAT_PROP_ANT_NAME);
        }
        jvmModel.removeAllElements();
        
        // Supported jvm platforms for this version of Tomcat or TomEE
        TomcatPlatformImpl tomcatPlatformImpl = new TomcatPlatformImpl(tm);
        Set<String> tomcatPlatforms = tomcatPlatformImpl.getSupportedJavaPlatformVersions();
        
        // jvm platforms registered in NetBeans
        JavaPlatform[] j2sePlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        
        Set<JavaPlatformAdapter> platformAdapters = new TreeSet<>();
        
        // Only add the jvm platforms that are supported from the registered set
        for (JavaPlatform jp : j2sePlatforms) {
            if (tomcatPlatforms.contains(jp.getSpecification().getVersion().toString())) {
                platformAdapters.add(new JavaPlatformAdapter(jp));
            }
        }
        
        if (platformAdapters.isEmpty()) {
            jvmModel.setSelectedItem(null);
            return;
        } else {
            for (JavaPlatformAdapter platformAdapter : platformAdapters) {
                jvmModel.addElement(platformAdapter);
            }
        }
        
        // try to set selected item
        for (JavaPlatformAdapter j2sePlatform : platformAdapters) {
            if (curPlatformName != null) {
                if (curPlatformName.equals(j2sePlatform.getName())) {
                    jvmModel.setSelectedItem(j2sePlatform);
                    // if we do not change the flag the jvm will not change
                    jvmModelFlag = true;
                    break;
                } else {
                    jvmModel.setSelectedItem(j2sePlatform);
                    jvmModelFlag = true;
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
    
    public Document getCatalinaHomeModel() {
        return catalinaHomeModel;
    }
    
    public Document getCatalinaBaseModel() {
        return catalinaBaseModel;
    }
    
    public Document getUsernameModel() {
        return usernameModel;
    }    
    
    public Document getPasswordModel() {
        return passwordModel;
    }
    
    public ButtonModel getCustomScriptModel() {
        return customScriptModel;
    }
    
    public ButtonModel getForceStopModel() {
        return forceStopModel;
    }
    
    public Document getScriptPathModel() {
        return scriptPathModel;
    }
    
    public ButtonModel getSharedMemModel() {
        return sharedMemModel;
    }
    
    public ButtonModel getSocketModel() {
        return socketModel;
    }
    
    public ButtonModel getMonitorModel() {
        return monitorModel;
    }
    
    public ButtonModel getProxyModel() {
        return proxyModel;
    }
    
    public ButtonModel getSecManagerModel() {
        return secManagerModel;
    }
    
    public Document getSharedMemNameModel() {
        return sharedMemNameModel;
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
    
    public SpinnerNumberModel getServerPortModel() {
        return serverPortModel;
    }
    
    public SpinnerNumberModel getShutdownPortModel() {
        return shutdownPortModel;
    }
    
    public SpinnerNumberModel getDebugPortModel() {
        return debugPortModel;
    }
    
    public SpinnerNumberModel getDeploymentTimeoutModel() {
        return deploymentTimeoutModel;
    }
    
    public SpinnerNumberModel getStartupTimeoutModel() {
        return startupTimeoutModel;
    }
    
    public SpinnerNumberModel getShutdownTimeoutModel() {
        return shutdownTimeoutModel;
    }
    
    public ButtonModel getDriverDeploymentModel() {
        return driverDeploymentModel;
    }
    
    // private helper methods -------------------------------------------------
    
    /** Save all changes */
    private void store() {
        
        if (jvmModelFlag) {
            JavaPlatformAdapter platformAdapter = (JavaPlatformAdapter)jvmModel.getSelectedItem();
            if (platformAdapter != null) {
                tp.setJavaPlatform(platformAdapter.getJavaPlatform());
            } else {
                tp.setJavaPlatform(null);
            }
            jvmModelFlag = false;
        }
        
        if (javaOptsModelFlag) {
            tp.setJavaOpts(getText(javaOptsModel));
            javaOptsModelFlag = false;
        }
        
        if (secManagerModelFlag) {
            tp.setSecManager(secManagerModel.isSelected());
            secManagerModelFlag = false;
        }
        
        if (usernameModelFlag) {
            tp.setUsername(getText(usernameModel));
            usernameModelFlag = false;
        }
        
        if (passwordModelFlag) {
            tp.setPassword(getText(passwordModel));
            passwordModelFlag = false;
        }
        
        if (scriptPathModelFlag) {
            tp.setScriptPath(getText(scriptPathModel));
            scriptPathModelFlag = false;
        }
        
        if (customScriptModelFlag) {
            tp.setCustomScript(customScriptModel.isSelected());
            customScriptModelFlag = false;
        }
        
        if (forceStopModelFlag) {
            tp.setForceStop(forceStopModel.isSelected());
            forceStopModelFlag = false;
        }
        
        if (sharedMemModelFlag || socketModelFlag) {
            tp.setDebugType(sharedMemModel.isSelected() ? TomcatProperties.DEBUG_TYPE_SHARED 
                                                        : TomcatProperties.DEBUG_TYPE_SOCKET);
            sharedMemModelFlag = false;
            socketModelFlag = false;
        }
        
        if (monitorModelFlag) {
            tp.setMonitor(monitorModel.isSelected());
            monitorModelFlag = false;
        }
        
        if (proxyModelFlag) {
            tp.setProxyEnabled(proxyModel.isSelected());
            proxyModelFlag = false;
        }
        
        if (sharedMemNameModelFlag) {
            tp.setSharedMem(getText(sharedMemNameModel));
            sharedMemNameModelFlag = false;
        }
        
        if (sourceModelFlag) {
            tp.setSources(sourceModel.getData());
            sourceModelFlag = false;
        }
        
        if (javadocModelFlag) {
            tp.setJavadocs(javadocModel.getData());
            javadocModelFlag = false;
        }
        
        if (serverPortModelFlag) {
            tm.setServerPort(((Integer)serverPortModel.getValue()));
            serverPortModelFlag = false;
        }
        
        if (shutdownPortModelFlag) {
            tm.setShutdownPort(((Integer)shutdownPortModel.getValue()));
            shutdownPortModelFlag = false;
        }
        
        if (debugPortModelFlag) {
            tp.setDebugPort(((Integer)debugPortModel.getValue()));
            debugPortModelFlag = false;
        }
        
        if (deploymentTimeoutModelFlag) {
            tp.setDeploymentTimeout(((Integer)deploymentTimeoutModel.getValue()));
            deploymentTimeoutModelFlag = false;
        }
        
        if (driverDeploymentModelFlag) {
            tp.setDriverDeployment(driverDeploymentModel.isSelected());
            driverDeploymentModelFlag = false;
        }
        
        if (startupTimeoutModelFlag) {
            tp.setStartupTimeout((Integer)startupTimeoutModel.getValue());
            startupTimeoutModelFlag = false;
        }
        
        if (shutdownTimeoutModelFlag) {
            tp.setShutdownTimeout((Integer)shutdownTimeoutModel.getValue());
            shutdownTimeoutModelFlag = false;
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
    
    /** Create a ToggleButtonModel inilialized by the specified selected parameter. */
    private JToggleButton.ToggleButtonModel createToggleButtonModel(boolean selected) {
        JToggleButton.ToggleButtonModel model = new JToggleButton.ToggleButtonModel();
        model.setSelected(selected);
        return model;
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
        
    // private helper class ---------------------------------------------------
    
    /** 
     * Adapter that implements several listeners, which is useful for dirty model
     * monitoring.
     */
    private abstract class ModelChangeAdapter implements ListDataListener, 
            DocumentListener, ItemListener, ChangeListener {
        
        public abstract void modelChanged();
        
        @Override
        public void contentsChanged(ListDataEvent e) {
            modelChanged();
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            modelChanged();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            modelChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            modelChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            modelChanged();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            modelChanged();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            modelChanged();
        }

        @Override
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
            return (String)platform.getProperties().get(TomcatProperties.PLAT_PROP_ANT_NAME);
        }
        
        @Override
        public String toString() {
            return platform.getDisplayName();
        }
        
        @Override
        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
    }
}
