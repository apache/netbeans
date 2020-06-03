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
package org.netbeans.modules.cnd.makeproject.api.launchers;

import org.netbeans.modules.cnd.makeproject.api.LaunchersRegistryAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class LaunchersRegistry {

    private final List<Launcher> launchers;
    private static final Object lock = "LaunchersRegistryLock"; //NOI18N
    public static final String LAUNCHER_TAG = "launcher";  // NOI18N
    public static final String COMMON_TAG = "common";  // NOI18N
    public static final String COMMAND_TAG = "runCommand"; // NOI18N
    public static final String BUILD_COMMAND_TAG = "buildCommand"; // NOI18N
    public static final String NAME_TAG = "displayName";   // NOI18N
    public static final String DIRECTORY_TAG = "runDir";   // NOI18N
    public static final String SYMFILES_TAG = "symbolFiles";// NOI18N
    public static final String ENV_TAG = "env";// NOI18N
    public static final String HIDE_TAG = "hide";// NOI18N
    public static final String RUN_IN_OWN_TAB_TAG = "runInOwnTab";// NOI18N
    public static final int COMMON_LAUNCHER_INDEX = -1;
    
    private static  Pattern pattern;

    private Object privateLaucnhersListener = null;  //for debugging purposes only
    
    static {  //for debugging purposes only
        LaunchersRegistryAccessor.setDefault(new LaunchersRegistryAccessorImpl());
    }

    /*package*/ void setPrivateLaucnhersListener(Object privateLaucnhersListener) {  //for debugging purposes only
        this.privateLaucnhersListener = privateLaucnhersListener;
    }

    private Object getPrivateLaucnhersListener() {
        return privateLaucnhersListener;
    }
    
    LaunchersRegistry() {
        launchers = new ArrayList<>();
        String regex = LAUNCHER_TAG + "(\\d*)[.]" + COMMAND_TAG; //NOI18N
        pattern = Pattern.compile(regex);
    }

    public void add(Launcher launcher) {
        synchronized (lock) {
            if (launchers.contains(launcher)) {
                return;
            }
            launchers.add(launcher);
        }
    }

    public void remove(Launcher launcher) {
        synchronized (lock) {
            launchers.remove(launcher);
        }
    }

    public boolean hasLaunchers() {
        return !launchers.isEmpty();
    }

    /**
     * Returns unmodified collection
     *
     * @return
     */
    public Collection<Launcher> getLaunchers() {
        return Collections.unmodifiableCollection(launchers);
    }

    boolean load(Properties properties) {
        List<Launcher> newLaunchers = new ArrayList<>();
        Launcher common = create(COMMON_LAUNCHER_INDEX, COMMON_TAG, properties, null);
        for (String key : properties.stringPropertyNames()) {
            Matcher matcher = pattern.matcher(key);
            if (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1));
                Launcher l = create(index, key.substring(0, key.indexOf("." + COMMAND_TAG)), properties, common);//NOI18N
                if (l != null && !l.isHide()) {
                    newLaunchers.add(l);
                }
            }
        }
        Collections.sort(newLaunchers, (Launcher o1, Launcher o2) -> o1.getIndex() - o2.getIndex());
        boolean modified = false;
        synchronized (lock) {
            if (!isEqualsLauncers(newLaunchers)) {
                launchers.clear();
                launchers.addAll(newLaunchers);
                modified = true;
            }
        }
        return modified;
    }

    private boolean isEqualsLauncers(List<Launcher> newLaunchers) {
        if (launchers.size() != newLaunchers.size()) {
            return false;
        }
        for(int i = 0; i < launchers.size(); i++) {
            Launcher l1 = launchers.get(i);
            Launcher l2 = newLaunchers.get(i);
            if (!l1.isLauncherEquals(l2)) {
                return false;
            }
        }
        return true;
    }
    
    private Launcher create(int index, String name, Properties properties, Launcher common) {
        boolean commonLauncher = name.equals(COMMON_TAG);
        assert !commonLauncher || common == null : "common launcher can not have other common";//NOI18N
        final String command = properties.getProperty(name + "." + COMMAND_TAG);//NOI18N
        assert commonLauncher || command != null : "usual launcher without command " + name;//NOI18N
        Launcher launcher = new Launcher(index, command, common);
        final String displayName = properties.getProperty(name + "." + NAME_TAG);//NOI18N
        if (displayName != null) {
            launcher.setName(displayName);
        } else {
            launcher.setName(command);
        }
        //build command can be null and this is OK
        String buildCommand = properties.getProperty(name + "." + BUILD_COMMAND_TAG);
        launcher.setBuildCommand(buildCommand);
        String directory = properties.getProperty(name + "." + DIRECTORY_TAG);//NOI18N
        //directory can be null and this is OK
        launcher.setRunDir(directory);
        final String symFiles = properties.getProperty(name + "." + SYMFILES_TAG);//NOI18N
        //symbol files can be null and this is OK
        launcher.setSymbolFiles(symFiles);
        for (String key : properties.stringPropertyNames()) {
            if (key.matches(name + "[.]" + ENV_TAG + "[.]\\w+")) {    //NOI18N
                launcher.putEnv(key.substring(key.lastIndexOf(".") + 1), properties.getProperty(key));
            }
        }
        String property = properties.getProperty(name + "." + HIDE_TAG);//NOI18N
        launcher.setHide("true".equals(property));//NOI18N
        property = properties.getProperty(name + "." + RUN_IN_OWN_TAB_TAG);//NOI18N
        launcher.setRunInOwnTab(!"false".equals(property));//NOI18N
        return launcher;
    }
    

    private static final class LaunchersRegistryAccessorImpl extends LaunchersRegistryAccessor {  //for debugging purposes only

        @Override
        public void assertPrivateListenerNotNull(FileObject dir) {
            if (LaunchersRegistryFactory.getInstance(dir).getPrivateLaucnhersListener() == null) {
                LaunchersProjectMetadataFactory factoryInstance = Lookups.forPath("Projects/org-netbeans-modules-cnd-makeproject/"//NOI18N
                        + ProjectMetadataFactory.LAYER_PATH)
                        .lookup(LaunchersProjectMetadataFactory.class);
                factoryInstance.read(dir);
                
                CndUtils.assertUnconditional("Private launchers listener is null for " + dir);//NOI18N
            }
        }
        
    }
}
