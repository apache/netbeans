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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbBundle;

/**
 */
public class DevelopmentHostConfiguration implements Cloneable {

    public static final String PROP_DEV_HOST = "devHost"; // NOI18N
    private final int def;

    // TODO: rewrite! list/value concept is error prone!

    private int value;
    private List<ExecutionEnvironment> servers;

//    private int buildPlatform; // Actual build platform
    private BuildPlatformConfiguration buildPlatformConfiguration;

    private boolean modified;
    private boolean dirty = false;
    private final PropertyChangeSupport pcs;

    public DevelopmentHostConfiguration(ExecutionEnvironment execEnv) {
        servers = ServerList.getEnvironments();
        value = 0;
        for (int i = 0; i < servers.size(); i++) {
            if (execEnv.equals(servers.get(i))) {
                value = i;
                break;
            }
        }
        def = value;
        pcs = new PropertyChangeSupport(this);

        int buildPlatform = CompilerSetManager.get(execEnv).getPlatform();
        if (buildPlatform == -1) {
            // TODO: CompilerSet is not reliable about platform; it must be.
            buildPlatform = PlatformTypes.PLATFORM_NONE;
        }
        buildPlatformConfiguration = new BuildPlatformConfiguration(buildPlatform, Platforms.getPlatformDisplayNames());
    }

    /** TODO: deprecate and remove, see #158983 */
    public String getHostKey() {
        return ExecutionEnvironmentFactory.toUniqueID(servers.get(value));
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return servers.get(value);
    }

    public String getDisplayName(boolean displayIfNotFound) {
        String out = ServerList.get(getExecutionEnvironment()).getDisplayName();
        if (displayIfNotFound && !isConfigured()) {
            out = NbBundle.getMessage(DevelopmentHostConfiguration.class,  "NOT_CONFIGURED", out); // NOI18N
        }
        else {
            int platformID = getBuildPlatformConfiguration().getValue();
//            platformID = CompilerSetManager.getDefault(getExecutionEnvironment()).getPlatform();
            Platform platform = Platforms.getPlatform(platformID);
            if (platform != null) {
                out += " [" + platform.getDisplayName() + "]"; // NOI18N
            }
        }
        return out;
    }

    public String getHostDisplayName(boolean displayIfNotFound) {
        String out = ServerList.get(getExecutionEnvironment()).getServerDisplayName();
        if (displayIfNotFound && !isConfigured()) {
            out = NbBundle.getMessage(DevelopmentHostConfiguration.class,  "NOT_CONFIGURED", out); // NOI18N
        }
        return out;
    }

    public boolean isConfigured() {
        // localhost is always STATE_COMPLETE so isLocalhost() is assumed
        // keeping track of online status takes more efforts and can miss sometimes
        return !CompilerSetManager.get(getExecutionEnvironment()).isUninitialized();
    }

    public int getValue() {
        return value;
    }

    public void setValue(String v) {
        setValue(v, false);
    }

    public void setValue(final String v, boolean firePC) {
        if (setValueImpl(v, firePC)) {
            return;
        }
        // The project's configuration wants a dev host not currently defined.
        // We don't want to ask user at this moment, so we create offline host and preserve compilerset name
        // User will be asked about connection after choosing action like build for this particular project
        // or after click on brand-new "..." button!
        addDevelopmentHost(v);
        setValueImpl(v, firePC);
    }

    public boolean setHost(ExecutionEnvironment execEnv) {
        return setHost(execEnv, false);
    }
    
    public boolean setHost(ExecutionEnvironment execEnv, boolean firePC) {
        CndUtils.assertTrue(execEnv != null);
        boolean result = setHostImpl(execEnv);
        if (!result) {
            addDevelopmentHost(execEnv);
            result = setHostImpl(execEnv);
        }
        if (firePC) {
            fireHostChanged();
        }
        return result;
    }

    private boolean setHostImpl(ExecutionEnvironment execEnv) {
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).equals(execEnv)) {
                value = i;
                // do not reset platform by compiler set, you can damage foreign configuration
                //CompilerSetManager compilerSetmanager = CompilerSetManager.get(execEnv);
                //int platform = compilerSetmanager.getPlatform();
                //setBuildPlatform(platform);
                //if (getBuildPlatform() == -1) {
                //    // TODO: CompilerSet is not reliable about platform; it must be.
                //    setBuildPlatform(PlatformTypes.PLATFORM_NONE);
                //}
                return true;
            }
        }
        return false;
    }

    private void fireHostChanged() {
        ExecutionEnvironment env = (0 <= value && value < servers.size()) ? servers.get(value) : null;
        if (env != null) {
            pcs.firePropertyChange(PROP_DEV_HOST, ExecutionEnvironmentFactory.toUniqueID(env), DevelopmentHostConfiguration.this);
        }
    }

    /**
     * @return true in the case host is found (even it is not yet set up);
     * in other words, returning false means that we should add this host
     * to ServerList
     */
    private boolean setValueImpl(final String v, final boolean firePC) {
        for (int i = 0; i < servers.size(); i++) {
            final ExecutionEnvironment currEnv = servers.get(i);
            final ServerRecord currRecord = ServerList.get(currEnv);
            //TODO: could we use something straightforward here?
            if (currRecord.getDisplayName().equals(v)) {
                final int newValue = i;
                final Runnable setter = () -> {
                    value = newValue;
                    setBuildPlatform(CompilerSetManager.get(currEnv).getPlatform());
                    if (getBuildPlatform() == -1) {
                        // TODO: CompilerSet is not reliable about platform; it must be.
                        setBuildPlatform(PlatformTypes.PLATFORM_NONE);
                    }
                    if (firePC) {
                        fireHostChanged();
                    }
                };
                if (currRecord.isSetUp()) {
                    setter.run();
                } else {
                    SwingUtilities.invokeLater(() -> {
                        if (currRecord.setUp()) {
                            setter.run();
                        }
                    });
                }
                return true;
            }
        }
        return false;
    }

    private boolean addDevelopmentHost(String host) {
        return addDevelopmentHost(ExecutionEnvironmentFactory.fromUniqueID(host));
    }

    private boolean addDevelopmentHost(ExecutionEnvironment execEnv) {
        final ServerRecord record = ServerList.addServer(execEnv, null, null, false, false);
        servers = ServerList.getEnvironments();
        return record != null;
    }

    public void reset() {
        servers = ServerList.getEnvironments();
        value = def;
    }

    public boolean getModified() {
        return modified;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    void assign(DevelopmentHostConfiguration conf) {
        boolean dirty2 = false;
        ExecutionEnvironment oldEnv = getExecutionEnvironment();
        ExecutionEnvironment newEnv = conf.getExecutionEnvironment();

        if (servers.size() != conf.servers.size()) {
            servers = ServerList.getEnvironments();
            dirty2 = true;
        }
        if (!newEnv.equals(oldEnv)) {
            dirty2 = true;
        }
        setDirty(dirty2);
        setHost(newEnv);
        getBuildPlatformConfiguration().assign(conf.getBuildPlatformConfiguration());
    }

    @Override
    public DevelopmentHostConfiguration clone() {
        DevelopmentHostConfiguration clone = new DevelopmentHostConfiguration(getExecutionEnvironment());
        // FIXUP: left setValue call to leave old logic
        clone.setHost(getExecutionEnvironment());
        clone.setBuildPlatformConfiguration(getBuildPlatformConfiguration().clone());
        return clone;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public boolean isLocalhost() {
        return getExecutionEnvironment().isLocal();
    }

    /**
     * @return the buildPlatform
     */
    public int getBuildPlatform() {
        return getBuildPlatformConfiguration().getValue();
    }

    /**
     * @param buildPlatform the buildPlatform to set
     */
    public void setBuildPlatform(int buildPlatform) {
        getBuildPlatformConfiguration().setValue(buildPlatform);
    }

    public String getBuildPlatformDisplayName() {
        if (isConfigured()) {
            return Platforms.getPlatform(getBuildPlatform()).getDisplayName();
        }
        else {
            return "";
        }
    }

    public String getBuildPlatformName() {
        if (isConfigured()) {
            return Platforms.getPlatform(getBuildPlatform()).getName();
        }
        else {
            return "";
        }
    }

    /**
     * @return the buildPlatformConfiguration
     */
    public BuildPlatformConfiguration getBuildPlatformConfiguration() {
        return buildPlatformConfiguration;
    }

    /**
     * @param buildPlatformConfiguration the buildPlatformConfiguration to set
     */
    public void setBuildPlatformConfiguration(BuildPlatformConfiguration buildPlatformConfiguration) {
        this.buildPlatformConfiguration = buildPlatformConfiguration;
    }

}
