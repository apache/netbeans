/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.refactoring;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.spi.NbRefactoringContext;
import org.netbeans.modules.apisupport.project.spi.NbRefactoringProvider;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint
 */
public class NbMoveRefactoringPlugin extends AbstractRefactoringPlugin {
    protected static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.refactoring");   // NOI18N
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    
    private Collection manifestRefactorings;
    private boolean firstManifestRefactoring = true;
    
    private HashMap oldManifests; /** <NBModuleProject, EditableManifest> */
    private EditableManifest targetManifest;
    
    private Map packagePostfix = new HashMap();
    ArrayList<FileObject> filesToMove = new ArrayList();    
    HashMap<FileObject,ElementHandle> classes;
    
    /**
     * Creates a new instance of NbRenameRefactoringPlugin
     */
    public NbMoveRefactoringPlugin(MoveRefactoring move) {
        super(move);
        
        manifestRefactorings = new ArrayList();
        oldManifests = new HashMap();
        setup(move.getRefactoringSource().lookupAll(FileObject.class), "", true);
    }
    
    
    public NbMoveRefactoringPlugin(RenameRefactoring rename) {
        super(rename);
        FileObject fo = rename.getRefactoringSource().lookup(FileObject.class);
        if (fo!=null) {
            setup(Collections.singletonList(fo), "", true);
        } else {
            setup(Collections.singletonList(((NonRecursiveFolder)rename.getRefactoringSource().lookup(NonRecursiveFolder.class)).getFolder()), "", false);
        }
    }  
    
    
    
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
        return null;
    }
    
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    /** Collects refactoring manifestRefactoringElements for a given refactoring.
     * @param refactoringElements Collection of refactoring manifestRefactoringElements - the implementation of this method
     * should add refactoring manifestRefactoringElements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() != null) {
            return null;
        }
        semafor.set(new Object());
        Problem problem = null;
        try {
            initClasses();
            Project cachedProject = null;
            Manifest cachedManifest = null;
            
            Lookup lkp = refactoring.getRefactoringSource();
            TreePathHandle handle = lkp.lookup(TreePathHandle.class);
            
            if (handle != null) {
                InfoHolder infoholder = examineLookup(lkp);
                Project project = FileOwnerQuery.getOwner(handle.getFileObject());
                if (project == null || project.getLookup().lookup(NbModuleProvider.class) == null) {
                    // take just netbeans module development into account..
                    return null;
                }
                
                if (infoholder.isClass) {
                    checkManifest(project, infoholder.fullName, refactoringElements);
                    checkLayer(project, infoholder.fullName, refactoringElements);
                }
                if (infoholder.isMethod) {
                    checkMethodLayer(infoholder, handle.getFileObject(), refactoringElements);
                }
            }
            
            NonRecursiveFolder nrf = lkp.lookup(NonRecursiveFolder.class);
            if(nrf!=null) {
                refactorProjectPropertyFiles(nrf.getFolder(), refactoringElements);
            } else {
                FileObject folder = lkp.lookup(FileObject.class);
                refactorProjectPropertyFiles(folder, refactoringElements);
            }
            
            
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            
            //TODO
            
            
//            Iterator it = col.iterator();
//            while (it.hasNext()) {
//                Resource res = (Resource)it.next();
//                FileObject fo = JavaModel.getFileObject(res);
//                Project project = FileOwnerQuery.getOwner(fo);
//                if (project != null && project instanceof NbModuleProject) {
//                    if (cachedProject == null || cachedProject != project) {
//                        cachedProject = (NbModuleProject)project;
//                        cachedManifest = cachedProject.getManifest();
//                    }
//                    String name = res.getName();
//                    String clazzName = name.replaceAll("\\.java$", ".class"); //NOI18N
//                    // check main attributes..
//                    Attributes attrs = cachedManifest.getMainAttributes();
//                    Iterator itx = attrs.entrySet().iterator();
//                    while (itx.hasNext()) {
//                        Map.Entry entry = (Map.Entry)itx.next();
//                        String val = (String)entry.getValue();
//                        if (val.indexOf(clazzName) != -1 || val.indexOf(clazzName) != -1) {
//                            RefactoringElementImplementation elem =
//                                    createManifestRefactoring(clazz, cachedProject.getManifestFile(),
//                                    ((Attributes.Name)entry.getKey()).toString(), val, null, cachedProject);
//                            refactoringElements.add(refactoring, elem);
//                            manifestRefactorings.add(elem);
//                        }
//                    }
//                    // check section attributes
//                    Map entries = cachedManifest.getEntries();
//                    if (entries != null) {
//                        Iterator itf = entries.entrySet().iterator();
//                        while (itf.hasNext()) {
//                            Map.Entry secEnt = (Map.Entry)itf.next();
//                            attrs = (Attributes)secEnt.getValue();
//                            String val = (String)secEnt.getKey();
//                            if (val.indexOf(clazzName) != -1) {
//                                String section = attrs.getValue("OpenIDE-Module-Class"); //NOI18N
//                                RefactoringElementImplementation elem =
//                                        createManifestRefactoring(clazz, cachedProject.getManifestFile(), null, val, section, cachedProject);
//                                refactoringElements.add(refactoring, elem);
//                                manifestRefactorings.add(elem);
//                            }
//                        }
//                    }
//                }
//            }
//            // now check layer.xml and bundle file in manifest
//            
//            Iterator itd = refactoring.getOtherDataObjects().iterator();
//            while (itd.hasNext()) {
//                DataObject dobj = (DataObject)itd.next();
//                Project project = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
//                if (project != null && project instanceof NbModuleProject) {
//                    if (cachedProject == null || cachedProject != project) {
//                        cachedProject = (NbModuleProject)project;
//                        cachedManifest = cachedProject.getManifest();
//                    }
//                }
//                String packageName = findPackageName(cachedProject, dobj.getPrimaryFile());
//                if (packageName != null) {
//                    Iterator itf = cachedManifest.getMainAttributes().entrySet().iterator();
//                    while (itf.hasNext()) {
//                        Map.Entry ent = (Map.Entry)itf.next();
//                        String val = (String)ent.getValue();
//                        if (packageName.equals(val)) {
//                            RefactoringElementImplementation elem = new ManifestMoveRefactoringElement(cachedProject.getManifestFile(), val,
//                                    ((Attributes.Name)ent.getKey()).toString(), cachedProject, dobj.getPrimaryFile());
//                            refactoringElements.add(refactoring, elem);
//                            manifestRefactorings.add(elem);
//                        }
//                    }
//                }
//            }
            
        } finally {
            semafor.set(null);
        }
        return problem;
    }
    
    private void refactorProjectPropertyFiles(FileObject folder, RefactoringElementsBag refactoringElements) {
        Project project = FileOwnerQuery.getOwner(folder);
        NbModuleProvider moduleProvider = project.getLookup().lookup(NbModuleProvider.class);
        if (moduleProvider == null) {
            // take just netbeans module development into account..
            return;
        }
        
        Sources srcs = org.netbeans.api.project.ProjectUtils.getSources(project);
        SourceGroup[] srcGrps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] rscGrps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        StringBuffer relPath = new StringBuffer();
        String newName = ((RenameRefactoring)refactoring).getNewName().replace('.', '/');
        if(isProjectPropertiesFilePath(srcGrps, folder, relPath) 
            || isProjectPropertiesFilePath(rscGrps, folder, relPath)) {
            refactoringElements.add(refactoring, new ManifestMoveRefactoringElement(moduleProvider.getManifestFile(), 
            relPath.toString().replace('.', '/'), newName));
        }
        
        NbRefactoringProvider refactoringProvider = project.getLookup().lookup(NbRefactoringProvider.class);
        if(refactoringProvider == null) {
            return;
        }
        
        List<NbRefactoringProvider.ProjectFileRefactoring> projectFilesRefactoring = refactoringProvider.getProjectFilesRefactoring(
            new NbRefactoringContext(folder, newName, relPath.toString().replace('.', '/')));
        if(projectFilesRefactoring!=null) {
            for(NbRefactoringProvider.ProjectFileRefactoring projectFileRefIter : projectFilesRefactoring) {
                refactoringElements.add(refactoring, new ProjectFileMoveRefactoringElement(projectFileRefIter));
            }
        }
    }
    
    private boolean isProjectPropertiesFilePath(SourceGroup [] srcGrps, FileObject folder, StringBuffer relPath) {
        for (SourceGroup gr : srcGrps) {
            if (FileUtil.isParentOf(gr.getRootFolder(), folder)) {
                relPath.append(FileUtil.getRelativePath(gr.getRootFolder(), folder));
                for(FileObject childIter:folder.getChildren()) {
                    if(childIter.getNameExt().equals("Bundle.properties")
                        || childIter.getNameExt().equals("layer.xml")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    protected RefactoringElementImplementation createManifestRefactoring(
            String fqname,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section) {
       return null;
//TODO        return new ManifestMoveRefactoringElement(fqname, manifestFile, attributeValue,
//                attributeKey, section);
    }
    
//    private JavaClass findClazz(Resource res, String name) {
//        Iterator itx = res.getClassifiers().iterator();
//        while (itx.hasNext()) {
//            JavaClass clzz = (JavaClass)itx.next();
//            if (clzz.getName().equals(name)) {
//                return clzz;
//            }
//        }
//        //what to do now.. we should match always something, better to return wrong, than nothing?
//        return (JavaClass)res.getClassifiers().iterator().next();
//    }
    
    private static String findPackageName(Project project, FileObject fo) {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            if (FileUtil.isParentOf(grps[i].getRootFolder(), fo) && grps[i].contains(fo)) {
                return FileUtil.getRelativePath(grps[i].getRootFolder(), fo);
            }
        }
        return null;
    }
    
    /*public final class ProjectPropertiesRefactoringElement extends AbstractRefactoringElement {

        private Project project;
        
        private String newCodeNameBase;
        
        private String oldCodeNameBase;
        
        public ProjectPropertiesRefactoringElement(FileObject parentFile, Project project, String newCodeNameBase, String oldCodeNameBase) {
            super(parentFile);
            this.project = project;
            this.newCodeNameBase = newCodeNameBase;
            this.oldCodeNameBase = oldCodeNameBase;
        }

        @Override
        public String getDisplayText() {
            return "Refactoring project properties";
        }

        @Override
        public void performChange() {
            NbRefactoringProvider refactoringProvider = this.project.getLookup().lookup(NbRefactoringProvider.class);
            refactoringProvider.doRefactoring(new NbRefactoringContext(this.newCodeNameBase, this.oldCodeNameBase));
        }
        
        
        
    }*/
    
    public final class ManifestMoveRefactoringElement extends AbstractRefactoringElement {
        
        
        private String oldName;
        private String oldContent;
        private String newName;
        private String clazz;
        private String attrName;
        private String sectionName = null;
        private FileObject movedFile = null;

        public ManifestMoveRefactoringElement(FileObject parentFile, String oldName, String newName) {
            super(parentFile);
            this.oldName = oldName;
            this.newName = newName;
        }
        
        public ManifestMoveRefactoringElement(String clazz, FileObject parentFile,
                String attributeValue, String attributeName) {
            super(parentFile);
            this.name = attributeValue;
            this.clazz = clazz;
            attrName = attributeName;
        }
        public ManifestMoveRefactoringElement(String clazz, FileObject parentFile,
                String attributeValue, String attributeName, String secName) {
            this(clazz, parentFile, attributeValue, attributeName);
            sectionName = secName;
        }
        
        //for data objects that are not classes
        public ManifestMoveRefactoringElement(FileObject parentFile,
                String attributeValue, String attributeName, FileObject movedFile) {
            super(parentFile);
            this.name = attributeValue;
            this.attrName = attributeName;
            this.movedFile = movedFile;
        }
        
        
        
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (oldName != null && newName != null) {
                return NbBundle.getMessage(NbMoveRefactoringPlugin.class, "TXT_ManifestPathRename", this.newName, this.oldName);
            }
            if (sectionName != null) {
                return NbBundle.getMessage(NbMoveRefactoringPlugin.class, "TXT_ManifestSectionRename", this.attrName, this.sectionName);
            }
            return NbBundle.getMessage(NbMoveRefactoringPlugin.class, "TXT_ManifestRename", this.name, this.attrName);
        }
        
        @Override
        public void performChange() {
            /*EditableManifest manifest = readManifest(parentFile);
            String value = manifest.getAttribute(attrName, sectionName);
            if(value != null && !value.equals(name)) {
                manifest.setAttribute(attrName, name, sectionName);
                writeManifest(parentFile, manifest);
            }*/
            String content = Utility.readFileIntoString(parentFile);
            oldContent = content;
            if (content != null) {
                content = content.replaceAll(oldName, newName);
                Utility.writeFileFromString(parentFile, content);
            }
//            NbModuleProject targetProject = (NbModuleProject)FileOwnerQuery.getOwner(refactoring.getTargetClassPathRoot());
//            if (firstManifestRefactoring) {
//                // if this is the first manifest refactoring, check the list for non-enable ones and remove them
//                Iterator it = manifestRefactorings.iterator();
//                while (it.hasNext()) {
//                    ManifestMoveRefactoringElement el = (ManifestMoveRefactoringElement)it.next();
//                    if (!el.isEnabled()) {
//                        it.remove();
//                    }
//                }
//                FileObject fo = targetProject.getManifestFile();
//                targetManifest = readManifest(fo);
//                firstManifestRefactoring = false;
//            }
//            
//            NbModuleProject sourceProject = project;
//            EditableManifest sourceManifest = null;
//            if (sourceProject == targetProject) {
//                sourceManifest = targetManifest;
//            } else {
//                sourceManifest = (EditableManifest)oldManifests.get(sourceProject);
//                if (sourceManifest == null) {
//                    sourceManifest = readManifest(sourceProject.getManifestFile());
//                    oldManifests.put(sourceProject, sourceManifest);
//                }
//            }
//            // update section info
//            if (sectionName != null) {
//                String newSectionName = clazz.getName().replace('.', '/') + ".class"; //NOI18N
//                targetManifest.addSection(newSectionName);
//                Iterator it = sourceManifest.getAttributeNames(name).iterator();
//                while (it.hasNext()) {
//                    String secattrname = (String)it.next();
//                    targetManifest.setAttribute(secattrname, sourceManifest.getAttribute(secattrname, name), newSectionName);
//                }
//                sourceManifest.removeSection(name);
//            } else {
//                // update regular attributes
//                if (sourceManifest != targetManifest) {
//                    sourceManifest.removeAttribute(attrName, null);
//                }
//                if (clazz != null) {
//                    String newClassname = clazz.getName().replace('.','/') + ".class"; //NOI18N
//                    targetManifest.setAttribute(attrName, newClassname, null);
//                } else {
//                    // mkleint - afaik this will get called only on folder rename.
//                    String newPath = refactoring.getTargetPackageName(movedFile).replace('.','/') + "/" + movedFile.getNameExt(); //NOI18N
//                    targetManifest.setAttribute(attrName, newPath, null);
//                }
//            }
//            manifestRefactorings.remove(this);
//            if (manifestRefactorings.isEmpty()) {
//                // now write all the manifests that were edited.
//                writeManifest(targetProject.getManifestFile(), targetManifest);
//                Iterator it = oldManifests.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry entry = (Map.Entry)it.next();
//                    EditableManifest man = (EditableManifest)entry.getValue();
//                    NbModuleProject proj = (NbModuleProject)entry.getKey();
//                    if (man == targetManifest) {
//                        continue;
//                    }
//                    writeManifest(proj.getManifestFile(), man);
//                }
//            }
        }
    }
    
    public final class ProjectFileMoveRefactoringElement extends AbstractRefactoringElement {
        
        private NbRefactoringProvider.ProjectFileRefactoring projectFileRefactoring;

        public ProjectFileMoveRefactoringElement(NbRefactoringProvider.ProjectFileRefactoring projectFileRefactoring) {
            super(projectFileRefactoring.getParentFile());
            this.projectFileRefactoring = projectFileRefactoring;
        }

        @Override
        public void performChange() {
            this.projectFileRefactoring.performChange();
        }
        
        @Override
        public String getDisplayText() {
            return this.projectFileRefactoring.getDisplayText();
        }
        
    }
    
    /*private static EditableManifest readManifest(FileObject fo) {
        InputStream str = null;
        try {
            str = fo.getInputStream();
            return  new EditableManifest(str);
        } catch (IOException exc) {
            err.notify(exc);
        } finally {
            if (str != null) {
                try {
                    str.close();
                } catch (IOException exc) {
                    err.notify(exc);
                }
            }
        }
        return new EditableManifest();
    }

    private static void writeManifest(FileObject fo, EditableManifest manifest) {
        OutputStream str = null;
        FileLock lock = null;
        try {
            lock = fo.lock();
            str = fo.getOutputStream(lock);
            manifest.write(str);

        } catch (IOException exc) {
            err.notify(exc);
        } finally {
            if (str != null) {
                try {
                    str.close();
                } catch (IOException exc) {
                    err.notify(exc);
                }
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }*/
    
    
    
    ///------- copied from MoveRefactoringPlugin
    
    private void setup(Collection fileObjects, String postfix, boolean recursively) {
        for (Iterator i = fileObjects.iterator(); i.hasNext(); ) {
            FileObject fo = (FileObject) i.next();
            if (RetoucheUtils.isJavaFile(fo)) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
                filesToMove.add(fo);
            } else if (!(fo.isFolder())) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
            } else if (VisibilityQuery.getDefault().isVisible(fo)) {
                //o instanceof DataFolder
                //CVS folders are ignored
                boolean addDot = !"".equals(postfix);
                Collection col = new ArrayList();
                for (FileObject fo2: fo.getChildren()) {
                    col.add(fo2);
                }
                if (recursively)
                    setup(col, postfix +(addDot?".":"") +fo.getName(), true); // NOI18N
            }
        }
    }   
    
   String getNewPackageName() {
        if (refactoring instanceof MoveRefactoring) {
            return RetoucheUtils.getPackageName(((MoveRefactoring) refactoring).getTarget().lookup(URL.class));        
        } else {
            return ((RenameRefactoring) refactoring).getNewName();
        }
    }
    
    String getTargetPackageName(FileObject fo) {
        if (refactoring instanceof RenameRefactoring) {
            if (refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class) !=null)
                //package rename
                return getNewPackageName();
            else {
                //folder rename
                FileObject folder = refactoring.getRefactoringSource().lookup(FileObject.class);
                ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
                FileObject root = cp.findOwnerRoot(folder);
                String prefix = FileUtil.getRelativePath(root, folder.getParent()).replace('/','.');
                String postfix = FileUtil.getRelativePath(folder, fo.getParent()).replace('/', '.');
                String t = concat(prefix, getNewPackageName(), postfix);
                return t;
            }
        } else if (packagePostfix != null) {
            String postfix = (String) packagePostfix.get(fo);
            String packageName = concat(null, getNewPackageName(), postfix);
            return packageName;
        } else
            return getNewPackageName();
    }   
    
   private String concat(String s1, String s2, String s3) {
        String result = "";
        if (s1 != null && !"".equals(s1)) {
            result += s1 + "."; // NOI18N
        }
        result +=s2;
        if (s3 != null && !"".equals(s3)) {
            result += ("".equals(result)? "" : ".") + s3; // NOI18N
        }
        return result;
    }  
   
   private void initClasses() {
        classes = new HashMap();
        for (int i=0;i<filesToMove.size();i++) {
            final int j = i;
            try {
                JavaSource source = JavaSource.forFileObject(filesToMove.get(i));
                
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    
                    public void cancel() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    
                    public void run(final CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        List<? extends Tree> trees= parameter.getCompilationUnit().getTypeDecls();
                        for (Tree t: trees) {
                            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                                if (((ClassTree) t).getSimpleName().toString().equals(filesToMove.get(j).getName())) {
                                    classes.put(filesToMove.get(j), ElementHandle.create(parameter.getTrees().getElement(TreePath.getPath(parameter.getCompilationUnit(), t))));
                                    return ;
                                }
                            }
                        }
                              
                    }
                }, true);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
            };
            
        }
    }  
   
}
