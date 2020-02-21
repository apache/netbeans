/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.toolchain.compilerset;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
//import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.spi.toolchain.CSMNotifier;
//import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.toolchain.Installer;
import org.netbeans.modules.cnd.toolchain.compilers.SPICompilerAccesor;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class ToolchainValidator {
    private static final boolean DISABLED = Boolean.getBoolean("cnd.toolchain.validator.disabled") || CompilerSetManagerImpl.DISABLED; // NOI18N
    private static final Logger LOG = Logger.getLogger(ToolchainValidator.class.getName());

    public static final ToolchainValidator INSTANCE = new ToolchainValidator();
    private static final RequestProcessor RP = new RequestProcessor("Tool collection validator", 1); // NOI18N

    private ToolchainValidator() {
    }

    public void validate(final ExecutionEnvironment env, final CompilerSetManagerImpl csm) {
        if (DISABLED || Installer.isClosed()) {
            return;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                validateImpl(env, csm);
            }
        };
        boolean postpone = env.isRemote() && ! ServerList.get(env).isOnline();
        if (postpone) {
            ServerList.get(env).addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ServerRecord.PROP_STATE_CHANGED.equals(evt.getPropertyName())) {
                        if (ServerList.get(env).isOnline()) {
                            RP.post(runnable);
                            ServerList.get(env).removePropertyChangeListener(this);
                        }
                    }
                }
            });
        } else {
            RP.post(runnable);
        }
    }

    //made this method public as it is placed in closed package so I do not see the need to keep 
    //this method package visible and create accessor for it
    public void applyChanges(Map<Tool, List<List<String>>> needReset, final CompilerSetManager csm) {
        for(Map.Entry<Tool, List<List<String>>> entry : needReset.entrySet()) {
            Tool tool = entry.getKey();
            List<List<String>> compilerDefinitions = entry.getValue();
            new SPICompilerAccesor(tool).applyCompilerDefinitions(compilerDefinitions);
        }
        final CompilerSetManagerImpl csmImpl = (CompilerSetManagerImpl)csm;
        CompilerSetPreferences.saveToDisk(csmImpl);
        ToolchainUtilities.fireCodeAssistanceChange(csmImpl);
    }

    private void validateImpl(final ExecutionEnvironment env, final CompilerSetManagerImpl csm) {
        //this method is not called when we are in standalone or unit test mode but we still should be 
        //confident we do not have UI here
        if (Installer.isClosed()) {
            return;
        }
        ProgressHandle createHandle = ProgressHandle.createHandle(NbBundle.getMessage(ToolchainValidator.class, "ToolCollectionValidation", env.getDisplayName())); // NOI18N
        createHandle.start();
        try {
            Map<Tool, List<List<String>>> needReset = new HashMap<Tool, List<List<String>>>();
            for (CompilerSet cs : csm.getCompilerSets()) {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                if (hostInfo == null) {
                    LOG.log(Level.INFO, "Cannot get hostinfo for {0}", env.getDisplayName()); //NOI18N
                    break;
                }
                for (Tool tool : cs.getTools()) {
                    if (tool instanceof AbstractCompiler) {
                        if (tool.getKind() == PredefinedToolKind.CCompiler || tool.getKind() == PredefinedToolKind.CCCompiler) {
                            List<List<String>> compilerDefinitions = new SPICompilerAccesor(tool).getCompilerDefinitions();
                            if (!isEqualsCompilerDefinitions(compilerDefinitions, (AbstractCompiler) tool)) {
                                needReset.put(tool, compilerDefinitions);
                            } else {

                            }
                        }
                    }
                }
            }
            if (needReset.size() > 0) {
                CSMNotifier.getInstance().showNotification(needReset, csm);
            }
        } catch (Throwable ex) {
            LOG.log(Level.INFO, ex.getMessage());
        } finally {
            createHandle.finish();
        }
    }

    private boolean isEqualsCompilerDefinitions(List<List<String>> compilerDefinitions, AbstractCompiler tool) {
        if (compilerDefinitions == null) {
            return true;
        }
        List<String> systemIncludeDirectories = tool.getSystemIncludeDirectories();
        if (!comparePathsLists(compilerDefinitions.get(0), systemIncludeDirectories, tool)) {
            return false;
        }
        List<String> systemPreprocessorSymbols = tool.getSystemPreprocessorSymbols();
        if (!compareMacrosLists(compilerDefinitions.get(1), systemPreprocessorSymbols, tool)) {
            return false;
        }
        List<String> systemIncludeHeaders = tool.getSystemIncludeHeaders();
        if (!comparePathsLists(compilerDefinitions.get(2), systemIncludeHeaders, tool)) {
            return false;
        }
        return true;
    }

    private boolean comparePathsLists(List<String> newList, List<String> oldList, AbstractCompiler tool) {
        Set<String> oldSet = new HashSet<String>(oldList);
        boolean res = true;
        for(String s : newList) {
            if (!oldSet.contains(s)) {
                LOG.log(Level.FINE, "Tool {0} was changed. Added system include path {1}", new Object[]{tool.getDisplayName(), s}); // NOI18N
                res = false;
            }
        }
        return res;
    }

    private boolean compareMacrosLists(List<String> newList, List<String> oldList, AbstractCompiler tool) {
        Map<String,String> oldMap = new HashMap<String,String>();
        for(String s : oldList) {
            int i = s.indexOf('='); // NOI18N
            if (i > 0) {
                oldMap.put(s.substring(0,i), s.substring(i+1));
            } else {
                oldMap.put(s, null);
            }
        }
        boolean res = true;
        for(String s : newList) {
            if (s.startsWith("__TIME__") || // NOI18N
                s.startsWith("__DATE__") || // NOI18N
                s.startsWith("__FILE__") || // NOI18N
                s.startsWith("__LINE__")) { // NOI18N
                continue;
            }
            String key;
            String value;
            int i = s.indexOf('='); // NOI18N
            if (i > 0) {
                key = s.substring(0,i);
                value = s.substring(i+1);
            } else {
                key = s;
                value = null;
            }
            if (!oldMap.containsKey(key)) {
                LOG.log(Level.FINE, "Tool {0} was changed. Added macro {1}", new Object[]{tool.getDisplayName(), s}); // NOI18N
                res = false;
            }
            String oldValue = oldMap.get(key);
            if (value == null && oldValue == null) {
                // equals
                continue;
            }
            if (value == null && "1".equals(oldValue) || // NOI18N
                "1".equals(value) && oldValue == null) { // NOI18N
                // equals
                continue;
            }
            if (value != null && oldValue != null && value.equals(oldValue)) {
                // equals
                continue;
            }
            LOG.log(Level.FINE, "Tool {0} was changed. Changed macro {1} from [{2}] to [{3}]", new Object[]{tool.getDisplayName(), key, oldValue, value}); // NOI18N
            res = false;
        }
        return res;
    }
}
