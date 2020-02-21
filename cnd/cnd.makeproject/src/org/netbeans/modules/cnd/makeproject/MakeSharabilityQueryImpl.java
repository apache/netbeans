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
package org.netbeans.modules.cnd.makeproject;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.api.MakeSharabilityQuery;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

/**
 * SharabilityQueryImplementation for make project with multiple sources
 */
public class MakeSharabilityQueryImpl implements MakeSharabilityQuery {

    private final FileObject baseDirFile;
    private final String baseDir;
    private final int baseDirLength;
    private boolean privateShared;
    private final ConfigurationDescriptorProvider projectDescriptorProvider;
    private static final boolean IGNORE_BINARIES = CndUtils.getBoolean("cnd.vcs.ignore.binaries", true);
    private boolean inited = false;
    private Set<String> skippedFiles = new HashSet<>();

    MakeSharabilityQueryImpl(ConfigurationDescriptorProvider projectDescriptorProvider, FileObject baseDirFile) {
        this.projectDescriptorProvider = projectDescriptorProvider;
        this.baseDirFile = baseDirFile;
        this.baseDir = baseDirFile.getPath();
        this.baseDirLength = this.baseDir.length();
        privateShared = false;
    }

    /**
     * Check whether a file or directory should be shared.
     * If it is, it ought to be committed to a VCS if the user is using one.
     * If it is not, it is either a disposable build product, or a per-user
     * private file which is important but should not be shared.
     * @param uri a normalized URI to check for sharability (may or may not yet exist).
     * @return one of the {@link org.netbeans.api.queries.SharabilityQuery.Sharability}'s constant
     */
    @Override
    public Sharability getSharability(final URI uri) {
        init();
        //if (projectDescriptorProvider.gotDescriptor()) {
        //    ConfigurationDescriptor configurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
        //    if (configurationDescriptor != null && configurationDescriptor.getModified()) {
        //        // Make sure all sharable files are saved on disk
        //        // See IZ http://www.netbeans.org/issues/show_bug.cgi?id=153504
        //        configurationDescriptor.save();
        //    }
        //}
        Sharability ret = ProjectManager.mutex().readAccess((Mutex.Action<Sharability>) () -> {
            synchronized (MakeSharabilityQueryImpl.this) {
                if (IGNORE_BINARIES && CndFileVisibilityQuery.getDefault().isIgnored(uri.getPath()))  {
                    return Sharability.NOT_SHARABLE;
                }
                if (skippedFiles.contains(uri.getPath())) {
                    return Sharability.NOT_SHARABLE;
                }
                boolean sub = uri.getPath().startsWith(baseDir);
                if (!sub) {
                    return Sharability.UNKNOWN;
                }
                if (uri.getPath().equals(baseDir)) {
                    return Sharability.MIXED;
                }
                if (uri.getPath().length() <= baseDirLength + 1) {
                    return Sharability.UNKNOWN;
                }
                String subString = uri.getPath().substring(baseDirLength + 1).replace('\\', '/');
                if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER)) {
                    return Sharability.MIXED;
                } else if (subString.equals("Makefile")) { // NOI18N
                    return Sharability.SHARABLE;
                } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + '/' + MakeConfiguration.CONFIGURATIONS_XML)) {
                    return Sharability.SHARABLE;
                } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + '/' + "private")) { // NOI18N
                    return privateShared ? Sharability.SHARABLE : Sharability.NOT_SHARABLE; // see IZ 121796, IZ 109580 and IZ 109573
                } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + '/' + "project.properties")) { // NOI18N
                    return Sharability.SHARABLE;
                } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + '/' + MakeConfiguration.PROJECT_XML)) { // NOI18N
                    return Sharability.SHARABLE;
                } else if (subString.startsWith(MakeConfiguration.NBPROJECT_FOLDER + '/' + "Makefile-")) { // NOI18N
                    return Sharability.SHARABLE;
                } else if (subString.startsWith(MakeConfiguration.NBPROJECT_FOLDER + '/' + "Package-")) { // NOI18N
                    return Sharability.SHARABLE;
                } else if (subString.startsWith(MakeConfiguration.NBPROJECT_FOLDER + '/' + "qt-")) { // NOI18N
                    return subString.endsWith(".pro")? Sharability.SHARABLE : Sharability.NOT_SHARABLE; // NOI18N
                } else if (subString.startsWith(MakeConfiguration.BUILD_FOLDER + '/')) { // NOI18N
                    return Sharability.NOT_SHARABLE;
                } else if (subString.startsWith(MakeConfiguration.DIST_FOLDER + '/')) { // NOI18N
                    return Sharability.NOT_SHARABLE;
                }
                return Sharability.UNKNOWN;
            }
        });
        return ret;
    }

    public void setPrivateShared(boolean privateShared) {
        this.privateShared = privateShared;
    }

    public boolean getPrivateShared() {
        return privateShared;
    }

    @Override
    public void update() {
        inited = false;
        init();
    }
    private synchronized void init() {
        if (!inited && this.projectDescriptorProvider.gotDescriptor()) {
            MakeConfigurationDescriptor cd = this.projectDescriptorProvider.getConfigurationDescriptor();
            if (cd != null) {
                Configurations confs = cd.getConfs();
                Set<String> newSet = new HashSet<>();
                for (Configuration conf : confs.getConfigurations()) {
                    if (conf instanceof MakeConfiguration) {
                        String outputValue = ((MakeConfiguration) conf).getOutputValue();
                        if (!outputValue.isEmpty()) {
                            newSet.add(CndFileUtils.normalizeAbsolutePath(((MakeConfiguration) conf).getAbsoluteOutputValue()));
                        }
                    }
                }
                skippedFiles = newSet;
                inited = true;
            }
        }
    }
}
