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

package org.netbeans.modules.cnd.discovery.project.cases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.discovery.project.MakeProjectTestBase;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class SimpleScriptTestCase extends MakeProjectTestBase {

    public SimpleScriptTestCase() {
        super("SimpleTestCase");
    }

    @Test
    public void testSimple() throws Exception {
        File dataDir = getDataDir();
        String zip = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/discovery/project/DiscoveryTestApplication.tar.gz";
        assert new File(zip).exists() : "Not  found file "+zip;
        performTestProject(zip, null, false, "");
    }

    @Override
    protected void setupWizard(WizardDescriptor wizard) {
        final String path = WizardConstants.PROPERTY_NATIVE_PROJ_DIR.get(wizard);
        final ExecutionEnvironment fs = WizardConstants.PROPERTY_SOURCE_HOST_ENV.get(wizard);
        final FSPath fsPath = new FSPath(FileSystemProvider.getFileSystem(fs), RemoteFileUtil.normalizeAbsolutePath(path, fs));
        WizardConstants.PROPERTY_RUN_CONFIGURE.put(wizard, false);
        WizardConstants.PROPERTY_SIMPLE_MODE.put(wizard, Boolean.FALSE);
        WizardConstants.PROPERTY_WORKING_DIR.put(wizard, path);
        WizardConstants.PROPERTY_BUILD_COMMAND.put(wizard, "./build.bash");
        WizardConstants.PROPERTY_CLEAN_COMMAND.put(wizard, "./clean.bash");
        WizardConstants.PROPERTY_RUN_REBUILD.put(wizard, true);
        WizardConstants.PROPERTY_SOURCE_FOLDERS_FILTER.put(wizard, MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT);
        WizardConstants.PROPERTY_NAME.put(wizard, CndPathUtilities.getBaseName(path));

        final
        List<SourceFolderInfo> list = new ArrayList<>();
        list.add(new SourceFolderInfo() {

            @Override
            public FileObject getFileObject() {
                return fsPath.getFileObject();
            }

            @Override
            public String getFolderName() {
                return CndPathUtilities.getBaseName(path);
            }

            @Override
            public boolean isAddSubfoldersSelected() {
                return true;
            }
        });
        WizardConstants.PROPERTY_SOURCE_FOLDERS.put(wizard, list.iterator());
    }  
}
