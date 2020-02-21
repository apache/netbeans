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
