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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import org.netbeans.modules.cnd.api.toolchain.ui.PathEnvVariables;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.BooleanNodeProp;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

public class PrependToolCollectionPathNodeProp extends BooleanNodeProp {

    private final CompilerSet2Configuration configuration;
    private final DevelopmentHostConfiguration hostConfiguration;

    public PrependToolCollectionPathNodeProp(BooleanConfiguration booleanConfiguration, CompilerSet2Configuration configuration, DevelopmentHostConfiguration hostConf, boolean canWrite, String name1, String name2, String name3) {
        super(booleanConfiguration, canWrite, name1, name2, name3);
        this.configuration = configuration;
        this.hostConfiguration = hostConf;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new PE();
    }

    private class PE extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            CustomEditorPanel component = new CustomEditorPanel(env);
            component.putClientProperty("title", getDisplayName()); //NOI18N
            return component;
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }

    private class CustomEditorPanel extends PathEnvVariables {

        public CustomEditorPanel(PropertyEnv propertyEnv) {
            super(configuration.getCompilerSet(), hostConfiguration.getExecutionEnvironment());
            propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propertyEnv.addVetoableChangeListener(new VetoableChangeListener() {
                @Override
                public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                    ToolchainUtilities.setModifyBuildPath(configuration.getCompilerSet(), getModifyBuildPath());
                    ToolchainUtilities.setModifyRunPath(configuration.getCompilerSet(), getModifyRunPath());
                }
            });
        }
    }
}
