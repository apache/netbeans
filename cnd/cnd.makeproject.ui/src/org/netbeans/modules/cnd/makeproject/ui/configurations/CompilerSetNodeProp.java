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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.uiapi.NodePesentation;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class CompilerSetNodeProp extends Node.Property<String> implements NodePesentation {

    private final CompilerSet2Configuration configuration;
    private final DevelopmentHostConfiguration hostConfiguration;
    private volatile boolean supportDefault = true;
    private final boolean canWrite;
    private final String txt1;
    private final String txt2;
    private final String txt3;
    private final String oldname;

    public CompilerSetNodeProp(CompilerSet2Configuration configuration, DevelopmentHostConfiguration hostConf, boolean canWrite, String txt1, String txt2, String txt3) {
        super(String.class);
        this.configuration = configuration;
        this.hostConfiguration = hostConf;
        this.canWrite = canWrite;
        this.txt1 = txt1;
        this.txt2 = txt2;
        this.txt3 = txt3;
        oldname = configuration.getOption();
        configuration.setCompilerSetNodeProp(CompilerSetNodeProp.this);
    }

    public String getOldname() {
        return oldname;
    }

    @Override
    public String getName() {
        return txt1;
    }

    @Override
    public String getDisplayName() {
        return txt2;
    }

    @Override
    public String getShortDescription() {
        return txt3;
    }

    @Override
    public String getHtmlDisplayName() {
        if (configuration.getCompilerSetName().getModified()) {
            return configuration.isDevHostSetUp() ? "<b>" + getDisplayName() : getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public String getValue() {
        return configuration.getCompilerSetName().getValue();
    }

    @Override
    public void setValue(String v) {
        configuration.setValue(v);
    }
    
    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canAutoComplete")) { //NOI18N
            return Boolean.FALSE;
        }
        return super.getValue(attributeName);
    }    

    @Override
    public void restoreDefaultValue() {
        configuration.getCompilerSetName().reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return supportDefault;
    }

    @Override
    public boolean isDefaultValue() {
        return !configuration.getCompilerSetName().getModified();
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public void update() {
        ((CompilerSetEditor) getPropertyEditor()).repaint();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new CompilerSetEditor();
    }

    private class CompilerSetEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv env;
        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            String displayName = configuration.getDisplayName(true);
            return displayName;
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public String[] getTags() {
            supportDefault = true;
            List<String> list = new ArrayList<>();
            // TODO: this works unpredictable on switching development hosts
            // TODO: should be resolved later on
//            if (configuration.getCompilerSetManager().getCompilerSet(getOldname()) == null) {
//                list.add(getOldname());
//            }
            if (configuration.isDevHostSetUp()) {
                CompilerSetManager compilerSetManager = configuration.getCompilerSetManager();
                CompilerSet defaultCompilerSet = compilerSetManager.getDefaultCompilerSet();
                compilerSetManager.getCompilerSets().forEach((cs) -> {
                    list.add(cs.getName());
                });
                if (defaultCompilerSet != null) {
                    list.add(0, CompilerSet2Configuration.DEFAULT_CS_NAME+" ("+defaultCompilerSet.getName()+")"); //NOI18N
                }
            }
            return list.toArray(new String[list.size()]);
        }

        public void repaint() {
            firePropertyChange();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            supportDefault = false;
            return new CompilerSetEditorCustomizer(env);
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }

    private final class CompilerSetEditorCustomizer extends JPanel implements VetoableChangeListener {
        private final VetoableChangeListener delegate;
        private final JComponent tpc;
        public CompilerSetEditorCustomizer(PropertyEnv propertyEnv) {
            this.setLayout(new BorderLayout());
            this.setBorder(new EmptyBorder(6,6,0,6));
            tpc = ToolsPanelSupport.getToolsPanelComponent(hostConfiguration.getExecutionEnvironment(), getValue());
            delegate = (VetoableChangeListener) tpc.getClientProperty(ToolsPanelSupport.OK_LISTENER_KEY);
            add(tpc, BorderLayout.CENTER);
            this.putClientProperty("title", NbBundle.getMessage(CompilerSetNodeProp.class, "CompilerSetEditorCustomizerTitile", hostConfiguration.getExecutionEnvironment().getDisplayName()));
            propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propertyEnv.addVetoableChangeListener(CompilerSetEditorCustomizer.this);
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (delegate != null) {
                delegate.vetoableChange(evt);
            }
            String toolchain = (String) tpc.getClientProperty(ToolsPanelSupport.SELECTED_TOOLCHAIN_KEY);
            if (toolchain != null) {
                setValue(toolchain);
            }
        }
    }
}
