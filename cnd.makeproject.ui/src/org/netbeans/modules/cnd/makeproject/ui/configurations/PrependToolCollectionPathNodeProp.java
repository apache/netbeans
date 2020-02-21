/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
