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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteBrandingModel;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;

/**
 * @author Martin Krauskopf
 */
public final class SuiteOperations implements DeleteOperationImplementation,
        MoveOrRenameOperationImplementation {
    
    private static final Map<String,Set<NbModuleProject>> TEMPORARY_CACHE = new HashMap<String,Set<NbModuleProject>>();
    
    private final SuiteProject suite;
    private final FileObject projectDir;
    
    public SuiteOperations(final SuiteProject suite) {
        this.suite = suite;
        this.projectDir = suite.getProjectDirectory();
    }
    
    @Override
    public void notifyDeleting() throws IOException {
        this.notifyDeleting(false);
    }
    
    private void notifyDeleting (boolean temporary) throws IOException {
        FileObject buildXML = projectDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        ActionUtils.runTarget(buildXML, new String[] { ActionProvider.COMMAND_CLEAN }, null).waitFinished();
        
        // remove all suite components from the suite - i.e. make them standalone
        final SubprojectProvider spp = suite.getLookup().lookup(SubprojectProvider.class);
        if (temporary) {
            SuiteUtils.moving(new Callable<Void>() {

                @Override
                public Void call () throws Exception {
                    for (Project suiteComponent : spp.getSubprojects()) {
                        SuiteUtils.removeModuleFromSuite((NbModuleProject) suiteComponent);
                    }
                    return null;
                }

            });
        } else {
            for (Project suiteComponent : spp.getSubprojects()) {
                SuiteUtils.removeModuleFromSuite((NbModuleProject) suiteComponent);
            }
        }
    }
    
    public void notifyDeleted() throws IOException {
        suite.getHelper().notifyDeleted();
    }
    
    public void notifyMoving() throws IOException {
        Set<NbModuleProject> subprojects = SuiteUtils.getSubProjects(suite);
        if (!subprojects.isEmpty()) {
            // XXX using suite's name is probably weak. Consider another solution. E.g.
            // store some "private" property and than read it.
            TEMPORARY_CACHE.put(ProjectUtils.getInformation(suite).getName(), subprojects);
        }
        // this will temporarily remove all suite components - this is needed
        // to prevent infrastructure confusion about lost suite. They will be
        // readded in the notifyMoved.
        notifyDeleting(true);
    }
    
    @Override
    public void notifyMoved (final Project original, File originalPath, String nueName) throws IOException {
        if (original == null) { // called on the original project
            suite.getHelper().notifyDeleted();
        } else { // called on the new project
            String name = ProjectUtils.getInformation(suite).getName();
            final Set<NbModuleProject> subprojects = TEMPORARY_CACHE.remove(name);
            if (subprojects != null) {
                final Set<Project> toOpen = new HashSet<Project>();
                SuiteUtils.moving(new Callable<Void>() {

                    @Override
                    public Void call () throws Exception {
                        for (Project _originalComp : subprojects) {
                            NbModuleProject originalComp = (NbModuleProject) _originalComp;

                            boolean directoryChanged = !original.getProjectDirectory().
                                    equals(suite.getProjectDirectory());
                            if (directoryChanged && FileUtil.isParentOf( // wasRelative
                                    original.getProjectDirectory(), originalComp.getProjectDirectory())) {
                                boolean isOpened = SuiteOperations.isOpened(originalComp);
                                Project nueComp;
                                if (originalComp.getProjectDirectory().isValid()) {
                                    nueComp = SuiteOperations.moveModule(originalComp, suite.getProjectDirectory());
                                } else {
                                    FileObject componentFolder = suite.getProjectDirectory().getFileObject(
                                            FileUtil.getRelativePath(original.getProjectDirectory(),
                                                    originalComp.getProjectDirectory()));
                                    assert componentFolder != null;
                                    nueComp = ProjectManager.getDefault().findProject(componentFolder);
                                    assert nueComp != null;
                                }
                                SuiteUtils.addModule(suite, (NbModuleProject) nueComp);
                                if (isOpened) {
                                    toOpen.add(nueComp);
                                }
                            } else {
                                SuiteUtils.addModule(suite, originalComp);
                            }
                        }
                        return null;
                    }

                });
                OpenProjects.getDefault().open(toOpen.toArray(new Project[0]), false);
            }
            boolean isRename = original.getProjectDirectory().getParent().equals(
                    suite.getProjectDirectory().getParent());
            if (isRename) {
                setDisplayName(nueName);
            }
            FileObject origSuiteFO = FileUtil.toFileObject(originalPath);
            if (origSuiteFO != null && origSuiteFO.getChildren().length == 0) {
                origSuiteFO.delete();
            }
        }
    }
    
    public @Override void notifyRenaming() throws IOException {
    }

    public @Override void notifyRenamed(String nueName) throws IOException {
        setDisplayName(nueName);
    }

    public List<FileObject> getMetadataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(GeneratedFilesHelper.BUILD_XML_PATH, files);
        addFile("nbproject", files); // NOI18N
        addFile("master.jnlp", files);    // NOI18N
        addFile("branding.jnlp", files);    // NOI18N
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(suite.getEvaluator().getProperty(SuiteBrandingModel.BRANDING_DIR_PROPERTY), files);
        return files;
    }
    
    private void addFile(String fileName, List<FileObject> result) {
        FileObject file = projectDir.getFileObject(fileName);
        if (file != null) {
            result.add(file);
        }
    }
    
    private void setDisplayName(final String nueName) throws IOException {
        final SuiteProperties sp = new SuiteProperties(suite, suite.getHelper(),
                suite.getEvaluator(), SuiteUtils.getSubProjects(suite));
        final BrandingModel branding = sp.getBrandingModel();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Object>() {
                public Object run() throws IOException {
                    if (branding.isBrandingEnabled()) { // application
                        branding.setTitle(nueName);
                        sp.storeProperties();
                    } else { // ordinary suite of modules
                        EditableProperties props = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.setProperty(SuiteBrandingModel.TITLE_PROPERTY, nueName);
                        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /** Package private for unit tests <strong>only</strong>. */
    static Project moveModule(final NbModuleProject original, final FileObject targetParent) throws IOException, IllegalArgumentException {
        ProjectOperations.notifyMoving(original);
        SuiteOperations.close(original);
        FileObject origDir = original.getProjectDirectory();
        FileObject copy = doCopy(original, origDir, targetParent);
        ProjectManager.getDefault().clearNonProjectCache();
        Project nueComp = ProjectManager.getDefault().findProject(copy);
        assert nueComp != null;
        File originalPath = FileUtil.toFile(origDir);
        doDelete(original, origDir);
        ProjectOperations.notifyMoved(original, nueComp, originalPath, originalPath.getName());
        return nueComp;
    }
    
    private static boolean isOpened(final Project original) {
        boolean opened = false;
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < openProjects.length; i++) {
            if (openProjects[i] == original) {
                opened = true;
                break;
            }
        }
        return opened;
    }
    
// XXX following is copy-pasted from the Project APIs
//<editor-fold defaultstate="collapsed" desc="copy-pasted from Project API">
    private static FileObject doCopy(final Project original,
            final FileObject from, final FileObject toParent) throws IOException {
        if (!VisibilityQuery.getDefault().isVisible(from)) {
            //Do not copy invisible files/folders.
            return null;
        }
        
        Project owner = FileOwnerQuery.getOwner(from);
        if (owner == null || !original.getProjectDirectory().equals(owner.getProjectDirectory())) {
            return null;
        }
        
        FileObject copy;
        if (from.isFolder()) {
            copy = toParent.createFolder(from.getNameExt());
            FileObject[] kids = from.getChildren();
            for (int i = 0; i < kids.length; i++) {
                doCopy(original, kids[i], copy);
            }
        } else {
            assert from.isData();
            copy = FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
        }
        return copy;
    }
    
    private static boolean doDelete(final Project original,
            final FileObject toDelete) throws IOException {
        Project owner = FileOwnerQuery.getOwner(toDelete);
        if (owner == null || !original.getProjectDirectory().equals(owner.getProjectDirectory())) {
            return false;
        }
        
        if (toDelete.isFolder()) {
            FileObject[] kids = toDelete.getChildren();
            boolean delete = true;
            
            for (int i = 0; i < kids.length; i++) {
                delete &= doDelete(original, kids[i]);
            }
            
            if (delete) {
                toDelete.delete();
            }
            
            return delete;
        } else {
            assert toDelete.isData();
            toDelete.delete();
            return true;
        }
    }
    
    private static void close(final Project prj) {
        Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
            public Object run() {
                LifecycleManager.getDefault().saveAll();
                
                Action closeAction = CommonProjectActions.closeProjectAction();
                closeAction = closeAction instanceof ContextAwareAction ? ((ContextAwareAction) closeAction).createContextAwareInstance(Lookups.fixed(new Object[] {prj})) : null;
                
                if (closeAction != null && closeAction.isEnabled()) {
                    closeAction.actionPerformed(new ActionEvent(prj, -1, "")); // NOI18N
                } else {
                    //fallback:
                    OpenProjects.getDefault().close(new Project[] {prj});
                }
                
                return null;
            }
        });
    }
//</editor-fold>
    
}
