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
package org.netbeans.modules.cnd.makeproject.ui.utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.makeproject.api.TempEnv;
import org.netbeans.modules.cnd.utils.ui.EditableComboBox;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.openide.util.Exceptions;

/**
 *
 */
public final class ExpandableEditableComboBox extends EditableComboBox {
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private MacroConverter converter;

    public ExpandableEditableComboBox() {
        super();
    }

    public void setEnv(ExecutionEnvironment env) {
        converter = new MacroConverter(env);
    }

    @Override
    public String getText() {
        String res = super.getText();
        if (converter != null) {
            res = converter.expand(res);
        }
        return res;
    }

    private static final class MacroConverter {

        private final MacroExpanderFactory.MacroExpander expander;
        private final Map<String, String> envVariables;
        private String homeDir;

        public MacroConverter(ExecutionEnvironment env) {
            envVariables = new HashMap<>();
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    envVariables.putAll(hostInfo.getEnvironment());
                    homeDir = hostInfo.getUserDir();
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    // should never == null occur if isHostInfoAvailable(env) => report
                    Exceptions.printStackTrace(ex);
                }
            } else {
                LOGGER.log(Level.INFO, "Host info should be available here!", new Exception());
            }
            TempEnv.getInstance(env).addTemporaryEnv(envVariables);
            this.expander = (envVariables == null) ? null : MacroExpanderFactory.getExpander(env, false);
        }

        public String expand(String in) {
            try {
                if (in.startsWith("~") && homeDir != null) { //NOI18N
                    in = homeDir+in.substring(1);
                }
                return expander != null ? expander.expandMacros(in, envVariables) : in;
            } catch (ParseException ex) {
                //nothing to do
            }
            return in;
        }
    }
    
}
