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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.uiapi.NodePesentation;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class CompilerSet2Configuration implements PropertyChangeListener, Cloneable {
    public static final String DEFAULT_CS = "default"; // NOI18N
    public static final String DEFAULT_CS_NAME = NbBundle.getMessage(CompilerSet2Configuration.class, "default_cs"); //NOI18N
    private DevelopmentHostConfiguration dhconf;
    private StringConfiguration compilerSetName;
    private NodePesentation compilerSetNodeProp;
    private String flavor;
    private boolean dirty = false;
    
    private static final RequestProcessor RP = new RequestProcessor("CompilerSet2Configuration", 1); // NOI18N

    private CompilerSet2Configuration(CompilerSet2Configuration other) {
        this.dhconf = other.dhconf.clone();
        this.compilerSetName = other.compilerSetName.clone();
        this.flavor = other.flavor;
        this.compilerSetNodeProp = null;        
    }
    
    // Constructor for default tool collection
    public CompilerSet2Configuration(DevelopmentHostConfiguration dhconf) {
        this.dhconf = dhconf;
        compilerSetName = new StringConfiguration(null, DEFAULT_CS);
        flavor = null;
        compilerSetNodeProp = null;
    }

    // Constructor for tool collection
    public CompilerSet2Configuration(DevelopmentHostConfiguration dhconf, CompilerSet cs) {
        this.dhconf = dhconf;
        String csName = (cs == null) ? null : cs.getName();
        if (csName == null || csName.length() == 0) {
            if (getCompilerSetManager().getCompilerSets().size() > 0) {
                csName = getCompilerSetManager().getCompilerSets().get(0).getName();
            } else {
                if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                    csName = "Sun"; // NOI18N
                } else {
                    csName = "GNU"; // NOI18N
                }
            }
        }
        compilerSetName = new StringConfiguration(null, csName);
        flavor = null;
        compilerSetNodeProp = null;
    }

    // we can't store CSM because it's dependent on devHostConfig name which is not persistent
    public final CompilerSetManager getCompilerSetManager() {
        return CompilerSetManager.get(dhconf.getExecutionEnvironment());
    }

    // compilerSetName
    public StringConfiguration getCompilerSetName() {
        return compilerSetName;
    }

    public void setCompilerSetName(StringConfiguration compilerSetName) {
        this.compilerSetName = compilerSetName;
    }

    public void setCompilerSetNodeProp(NodePesentation compilerSetNodeProp) {
        this.compilerSetNodeProp = compilerSetNodeProp;
    }

    // ----------------------------------------------------------------------------------------------------

    public void setValue(String name) {
        if (!getOption().equals(name)) {
            setValue(name, null);
        }
    }

    public void restore(String name, int version) {
        String nm;
        String fl;
        int index = name.indexOf('|'); // NOI18N
        if (index > 0) {
            nm = name.substring(0, index);
            fl = name.substring(index+1);
        } else {
            nm = name;
            if (DEFAULT_CS.equals(nm)) {
                fl = null;
            } else {
                fl = name;
            }
        }
        setValue(CompilerSet2Configuration.mapOldToNew(nm, version), CompilerSet2Configuration.mapOldToNew(fl, version));
    }

    private void setValue(String name, String flavor) {
        if (name == null || name.startsWith(DEFAULT_CS_NAME+" (")) { // NOI18N
            name = DEFAULT_CS;
        }
        getCompilerSetName().setValue(name);
        setFlavor(flavor);
    }

    /*
     * TODO: spread it out (Sergey)
     * Should this return csm.getCurrentCompilerSet()? (GRP)
     */
    public CompilerSet getCompilerSet() {
        String value = getCompilerSetName().getValue();
        if (DEFAULT_CS.equals(value)) {
            return getCompilerSetManager().getDefaultCompilerSet();
        }
        return getCompilerSetManager().getCompilerSet(value);
    }

    public boolean isDefaultCompilerSet() {
        return DEFAULT_CS.equals(getCompilerSetName().getValue());
    }

    public String getName() {
        return getDisplayName(false);
    }

    public String getDisplayName(boolean createIfNotFound) {
        CompilerSet compilerSet;
        String value = getCompilerSetName().getValue();
        boolean isDefault;
        if (DEFAULT_CS.equals(value)) {
            isDefault = true;
            compilerSet = getCompilerSetManager().getDefaultCompilerSet();
        } else {
            isDefault = false;
            compilerSet = getCompilerSetManager().getCompilerSet(value);
        }
        String displayName = null;

        if (compilerSet != null) {
            if (isDefault) {
                displayName = DEFAULT_CS_NAME+" ("+compilerSet.getName()+")"; //NOI18N
            } else {
                displayName = compilerSet.getName();
            }
        }
        if (displayName != null && dhconf.isConfigured()) {
            return displayName;
        } else {
            if (createIfNotFound) {
                return createNotFoundName(value);
            } else {
                return ""; // NOI18N
            }
        }
    }

    private String createNotFoundName(String name) {
        if (!dhconf.isConfigured()) {
            return "";
        } else {
            return name.equals(CompilerSet.None) ? name : NbBundle.getMessage(CompilerSet2Configuration.class,  "NOT_FOUND", name); // NOI18N
        }
    }

    public boolean isDevHostSetUp() {
        return dhconf.isConfigured();
    }

    // Clone and assign
    public void assign(CompilerSet2Configuration conf) {
        String oldName = getCompilerSetName().getValue();
        String newName = conf.getCompilerSetName().getValue();
        setDirty(newName != null && !newName.equals(oldName));
//        setMakeConfiguration(conf.getMakeConfiguration());
        setValue(conf.getCompilerSetName().getValue());
    }

    @Override
    public CompilerSet2Configuration clone() {
        CompilerSet2Configuration clone = new CompilerSet2Configuration(this);
        return clone;
    }

    public void setDevelopmentHostConfiguration(DevelopmentHostConfiguration dhconf) {
        this.dhconf = dhconf;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    /*
     * Backward compatibility with old CompilerSetConfiguration (for now)
     */
    public boolean isValid() {
        return getCompilerSet() != null;
    }

    public String getOption() {
        return getCompilerSetName().getValue();
    }

    public String getNameAndFlavor() {
        StringBuilder ret = new StringBuilder();
        ret.append(getOption());
        if (!DEFAULT_CS.equals(getOption())) {
            if (getFlavor() != null) {
                ret.append("|"); // NOI18N
                ret.append(getFlavor());
            }
        }
        return ret.toString();
    }

    public String getFlavor() {
        if (flavor == null) {
            CompilerSet cs = getCompilerSet();
            if (cs != null) {
                this.flavor = cs.getCompilerFlavor().getToolchainDescriptor().getName();
            }
        }
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        CompilerSet ocs;
        String hkey = ((DevelopmentHostConfiguration) evt.getNewValue()).getHostKey();
        final ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(hkey);
        final String oldName = getName();
        if (oldName != null) {
            ocs = CompilerSetManager.get(env).getCompilerSet(oldName);
        } else {
            ocs = CompilerSetManager.get(env).getDefaultCompilerSet();
        }
        if (ocs == null && !CompilerSetManager.get(env).getCompilerSets().isEmpty()) {
            ocs = CompilerSetManager.get(env).getCompilerSets().get(0);
        }
        if (ocs == null) {
            return;
        }

        if (env.isLocal()) {
            setValue(ocs.getName());
        } else {
            setValue(ocs.getName());
            final CompilerSet focs = ocs;
            RP.post(() -> {
                ServerRecord record = ServerList.get(env);
                if (record != null) {
                    // Not sure why we do this in an RP, but don't want to remove it this late in the release
                    setValue(focs.getName());
                    if (compilerSetNodeProp != null) {
                        compilerSetNodeProp.update();
                    }
                }
            });
        }
    }

    private static String mapOldToNew(String flavor, int version) {
        if (version <= 43) {
            if ("Sun".equals(flavor)) { // NOI18N
                flavor = "SunStudio"; // NOI18N
            } else if ("SunExpress".equals(flavor)) { // NOI18N
                flavor = "SunStudioExpress"; // NOI18N
            } else if ("Sun12".equals(flavor)) { // NOI18N
                flavor = "SunStudio_12"; // NOI18N
            } else if ("Sun11".equals(flavor)) { // NOI18N
                flavor = "SunStudio_11"; // NOI18N
            } else if ("Sun10".equals(flavor)) { // NOI18N
                flavor = "SunStudio_10"; // NOI18N
            } else if ("Sun9".equals(flavor)) { // NOI18N
                flavor = "SunStudio_9"; // NOI18N
            } else if ("Sun8".equals(flavor)) { // NOI18N
                flavor = "SunStudio_8"; // NOI18N
            } else if ("DJGPP".equals(flavor)) { // NOI18N
                flavor = "GNU"; // NOI18N
            } else if ("Interix".equals(flavor)) { // NOI18N
                flavor = "GNU"; // NOI18N
            } else if (CompilerSet.UNKNOWN.equals(flavor)) {
                flavor = "GNU"; // NOI18N
            }
        }
        if ("Sun".equals(flavor) || // NOI18N
            "SunStudio".equals(flavor) || // NOI18N
            "OracleSolarisStudio".equals(flavor)) { // NOI18N
            flavor = "OracleDeveloperStudio"; // NOI18N
        }
        return flavor;
    }
}
