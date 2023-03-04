/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Arrays;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 * Test ModuleOperations.
 *
 * @author Martin Krauskopf
 */
public class ModuleOperationsTest extends TestBase {
    
    static {
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
    }
    
    public ModuleOperationsTest(String name) {
        super(name);
    }
    
    private ContextGlobalProviderImpl cgpi = new ContextGlobalProviderImpl();

    protected @Override void setUp() throws Exception {
        super.setUp();
        System.setProperty("sync.project.execution", "true");
        MockLookup.setLayersAndInstances(cgpi);
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        ((DialogDisplayerImpl) Lookup.getDefault().lookup(DialogDisplayer.class)).reset();
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public void testDelete() throws Exception {
        NbModuleProject project = generateStandaloneModule("module");
        project.open();
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled", ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        
        FileObject prjDir = project.getProjectDirectory();
        
        FileObject buildXML = prjDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        // build project
        ActionUtils.runTarget(buildXML, new String[] { "compile" }, null).waitFinished();
        assertNotNull("project was build", prjDir.getFileObject("build"));
        
        FileObject[] expectedMetadataFiles = {
            buildXML,
            prjDir.getFileObject("manifest.mf"),
            prjDir.getFileObject("nbproject"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles), ProjectOperations.getMetadataFiles(project));
        
        FileObject[] expectedDataFiles = {
            prjDir.getFileObject("src"),
            prjDir.getFileObject("test"),
        };
        assertEquals("correct data files", Arrays.asList(expectedDataFiles), ProjectOperations.getDataFiles(project));
        
        // It is hard to simulate exact scenario invoked by user. Let's test at least something.
        ProjectOperations.notifyDeleting(project);
        prjDir.getFileSystem().refresh(true);
        assertNull(prjDir.getFileObject("build"));
    }
    
    public void testOperationActions() throws Exception { // #72397
        final NbModuleProject project = generateStandaloneModule("module");
        cgpi.setProject(project);
        DialogDisplayerImpl dd = (DialogDisplayerImpl) Lookup.getDefault().lookup(DialogDisplayer.class);
        FileObject lock = FileUtil.createData(project.getProjectDirectory(), "build/testuserdir/lock");
        RandomAccessFile raf = new RandomAccessFile(FileUtil.toFile(lock), "rw");
        FileLock lck = raf.getChannel().lock();
        EventQueue.invokeAndWait(new Runnable() {
            @Override public void run() {
                ((ContextAwareAction) CommonProjectActions.deleteProjectAction()).createContextAwareInstance(Lookups.singleton(project)).actionPerformed(null);
            }
        });
        assertNotNull("warning message emitted", dd.getLastNotifyDescriptor());
        assertEquals("warning message emitted", dd.getLastNotifyDescriptor().getMessage(),
                Bundle.ERR_ModuleIsBeingRun());
        dd.reset();
        lck.release();
        raf.close();
        lock.delete();
        EventQueue.invokeAndWait(new Runnable() {
            @Override public void run() {
                CommonProjectActions.deleteProjectAction().actionPerformed(null);
            }
        });
        assertNull("no warning message", dd.getLastNotifyDescriptor());
    }
    
    static final class ContextGlobalProviderImpl implements ContextGlobalProvider {
        
        private Lookup contextLookup;
        
        void setProject(final Project project) {
            contextLookup = Lookups.singleton(project);
        }
        
        @Override public Lookup createGlobalContext() {
            return contextLookup;
        }
        
    }
    
}
