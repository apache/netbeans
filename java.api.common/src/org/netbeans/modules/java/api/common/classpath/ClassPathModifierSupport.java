/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.impl.ClassPathPackageAccessor;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;

/**
 * Helper methods for adding/removing items from project classpath.
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public class ClassPathModifierSupport {

    public static final int ADD = 1;
    public static final int ADD_NO_HEURISTICS = 3;
    public static final int REMOVE = 2;
    
    private static final Logger LOG = Logger.getLogger(ClassPathModifierSupport.class.getName());
    
    static {
        ClassPathPackageAccessor.setInstance(new ClassPathPackageAccessorImpl());
    }
    
    /**
     * Adds or removes jar files or folders to (from) given classpath.
     * @deprecated use {@link ClassPathModifierSupport#handleRoots(org.netbeans.api.project.Project, org.netbeans.modules.java.api.common.ant.UpdateHelper, org.netbeans.modules.java.api.common.classpath.ClassPathSupport, org.netbeans.spi.project.support.ant.PropertyEvaluator, org.netbeans.spi.project.support.ant.ReferenceHelper, org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport.Callback, java.net.URI[], java.lang.String, java.lang.String, int) }
     */
    @Deprecated
    public static boolean handleRoots (
            @NonNull final Project project,
            @NonNull final AntProjectHelper helper,
            @NonNull final ClassPathSupport cs,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final ClassPathUiSupport.Callback cpUiSupportCallback,
            @NonNull final URI[] classPathRoots,
            @NonNull final String classPathProperty,
            @NullAllowed final String projectXMLElementName,
            final int operation) throws IOException {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("cs", cs);           //NOI18N
        Parameters.notNull("eval", eval);       //NOI18N
        Parameters.notNull("classPathProperty", classPathRoots); //NOI18N
        Parameters.notNull("classPathProperty", classPathProperty); //NOI18N
        return handleRootsImpl(
                project,
                helper,
                cs,
                eval,
                null,
                null,
                cpUiSupportCallback,
                classPathRoots,
                classPathProperty,
                projectXMLElementName,
                operation);
    }
    
    
    /**
     * Adds or removes jar files or folders from given classpath.
     * @param project the project to (from) which classpath the artifacts should be added (removed).
     * @param helper the {@link UpdateHelper}
     * @param cs the {@link ClassPathSupport}
     * @param eval the {@link PropertyEvaluator}
     * @param refHelper the {@link ReferenceHelper}
     * @param cpUiSupportCallback the optional {@link ClassPathUiSupport.Callback} doing the {@link ClassPathSupport.Item} initialization.
     * @param classPathRoots the jar files or folders to be added (removed)
     * @param classPathProperty the classpath property into (from which) the artifacts should be added (removed).
     * @param projectXMLElementName optional project XML element name
     * @param operation to be performed {@link ClassPathModifierSupport#ADD}, {@link ClassPathModifierSupport#ADD_NO_HEURISTICS}, {@link ClassPathModifierSupport#REMOVE} 
     * @return true if classpath was changed
     * @throws IOException in case of failure
     * @since 1.38
     */
    public static boolean handleRoots (
            @NonNull final Project project,
            @NonNull final UpdateHelper helper,
            @NonNull final ClassPathSupport cs,
            @NonNull final PropertyEvaluator eval,
            @NonNull final ReferenceHelper refHelper,
            @NullAllowed final ClassPathUiSupport.Callback cpUiSupportCallback,
            @NonNull final URI[] classPathRoots,
            @NonNull final String classPathProperty,
            @NullAllowed final String projectXMLElementName,
            final int operation) throws IOException {        
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("cs", cs);           //NOI18N
        Parameters.notNull("eval", eval);       //NOI18N
        Parameters.notNull("refHelper", refHelper); //NOI18N
        Parameters.notNull("classPathProperty", classPathRoots); //NOI18N
        Parameters.notNull("classPathProperty", classPathProperty); //NOI18N
        return handleRootsImpl(
                project,
                helper.getAntProjectHelper(),
                cs,
                eval,
                helper,
                refHelper,
                cpUiSupportCallback,
                classPathRoots,
                classPathProperty,
                projectXMLElementName,
                operation);
    }
    
    private static boolean handleRootsImpl (
            @NonNull final Project project,
            @NonNull final AntProjectHelper helper,
            @NonNull final ClassPathSupport cs,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final UpdateHelper updateHelper,
            @NullAllowed final ReferenceHelper rh,
            @NullAllowed final ClassPathUiSupport.Callback cpUiSupportCallback,
            @NonNull final URI[] classPathRoots,
            @NonNull final String classPathProperty,
            @NullAllowed final String projectXMLElementName,
            final int operation) throws IOException {
        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathProperty);                            
                            List<ClassPathSupport.Item> resources = cs.itemsList(raw, projectXMLElementName);
                            boolean changed = false;
                            File projectFolderFile = FileUtil.toFile(helper.getProjectDirectory());
                            for (int i=0; i< classPathRoots.length; i++) {
                                String filePath;
                                if (ADD_NO_HEURISTICS == operation || REMOVE == operation || !classPathRoots[i].isAbsolute()) {
                                    URI toAdd = LibrariesSupport.getArchiveFile(classPathRoots[i]);
                                    if (toAdd == null) {
                                        toAdd = classPathRoots[i];
                                    }
                                    filePath =  LibrariesSupport.convertURIToFilePath(toAdd);
                                } else {
                                    filePath = Accessor.performHeuristics(classPathRoots[i], helper);
                                }
                                // LibrariesNode calls this method with variable based classpath items:
                                String filePath2 = filePath;
                                if (filePath2.startsWith("${var.")) { // NOI18N
                                    filePath2 = eval.evaluate(filePath);
                                }
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( filePath2, projectFolderFile, null, filePath.startsWith("${var.") ? filePath : null); // NOI18N
                                if (cpUiSupportCallback != null) {
                                    cpUiSupportCallback.initItem(item);
                                }
                                if ((operation == ADD || operation == ADD_NO_HEURISTICS) && !resources.contains(item)) {
                                    resources.add (item);
                                    changed = true;
                                } else if (operation == REMOVE) {
                                    for (Iterator<ClassPathSupport.Item> it = resources.iterator(); it.hasNext();) {
                                        ClassPathSupport.Item resource = it.next();
                                        if (resource.equals(item) ||
                                            (resource.isBroken() && resource.getType() == ClassPathSupport.Item.TYPE_JAR && 
                                            (filePath.equals(resource.getFilePath()) || filePath.equals(resource.getVariableBasedProperty())))) {
                                            it.remove();
                                            if (rh != null) {
                                                assert updateHelper != null;
                                                removeUnusedReference(
                                                    resource,
                                                    classPathProperty,
                                                    updateHelper,
                                                    rh);
                                            }
                                            changed = true;
                                        }
                                    }
                                }
                            }
                            if (changed) {
                                String itemRefs[] = cs.encodeToStrings( resources, projectXMLElementName);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);  //PathParser may change the EditableProperties
                                props.setProperty(classPathProperty, itemRefs);
                                if (updateHelper != null) {
                                    //Prefer UpdateHelper, it notifies user about an update
                                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                } else {
                                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                }
                                ProjectManager.getDefault().saveProject(project);
                                return true;
                            }
                            return false;
                        }
                    }
            
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                IOException t = new IOException();
                t.initCause(e);
                throw t;
            }
        }
    }

    /**
     * Adds or removes project's artifact to (from) given classpath.
     * @deprecated Use {@link ClassPathModifierSupport#handleAntArtifacts(org.netbeans.api.project.Project, org.netbeans.modules.java.api.common.ant.UpdateHelper, org.netbeans.modules.java.api.common.classpath.ClassPathSupport, org.netbeans.spi.project.support.ant.PropertyEvaluator, org.netbeans.spi.project.support.ant.ReferenceHelper, org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport.Callback, org.netbeans.api.project.ant.AntArtifact[], java.net.URI[], java.lang.String, java.lang.String, int)}
     */
    @Deprecated
    public static boolean handleAntArtifacts (
            @NonNull final Project project,
            @NonNull final AntProjectHelper helper,
            @NonNull final ClassPathSupport cs,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final ClassPathUiSupport.Callback cpUiSupportCallback,
            @NonNull final AntArtifact[] artifacts,
            @NonNull final URI[] artifactElements,
            @NonNull final String classPathProperty,
            @NullAllowed final String projectXMLElementName,
            final int operation) throws IOException, UnsupportedOperationException {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("cs", cs);           //NOI18N
        Parameters.notNull("eval", eval);       //NOI18N
        Parameters.notNull("artifacts", artifacts); //NOI18N
        Parameters.notNull("artifactElements", artifactElements);   //NOI18N
        Parameters.notNull("classPathProperty", classPathProperty); //NOI18N
        if (artifacts.length != artifactElements.length) {
            throw new IllegalArgumentException("Each artifact has to have corresponding artifactElement");  //NOI18N
        }
        return handleAntArtifactsImpl(
                project,
                helper,
                cs,
                eval,
                null,
                null,
                cpUiSupportCallback,
                artifacts,
                artifactElements,
                classPathProperty,
                projectXMLElementName,
                operation);
    }
    
    /**
     * Adds or removes project's artifact from given classpath.
     * @param project the project to (from) which classpath the artifacts should be added (removed).
     * @param helper the {@link UpdateHelper}
     * @param cs the {@link ClassPathSupport}
     * @param eval the {@link PropertyEvaluator}
     * @param refHelper the {@link ReferenceHelper}
     * @param cpUiSupportCallback the optional {@link ClassPathUiSupport.Callback} doing the {@link ClassPathSupport.Item} initialization.
     * @param artifacts the artifacts to be added
     * @param artifactElements the corresponding artifacts elements
     * @param classPathProperty the classpath property into (from which) the artifacts should be added (removed).
     * @param projectXMLElementName optional project XML element name
     * @param operation to be performed {@link ClassPathModifierSupport#ADD}, {@link ClassPathModifierSupport#ADD_NO_HEURISTICS}, {@link ClassPathModifierSupport#REMOVE} 
     * @return true if classpath was changed
     * @throws IOException in case of failure
     * @since 1.38
     */
    public static boolean handleAntArtifacts (
            @NonNull final Project project,
            @NonNull final UpdateHelper helper,
            @NonNull final ClassPathSupport cs,
            @NonNull final PropertyEvaluator eval,
            @NonNull final ReferenceHelper refHelper,
            @NullAllowed final ClassPathUiSupport.Callback cpUiSupportCallback,
            @NonNull final AntArtifact[] artifacts,
            @NonNull final URI[] artifactElements,
            @NonNull final String classPathProperty,
            @NullAllowed final String projectXMLElementName,
            final int operation) throws IOException, UnsupportedOperationException {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("cs", cs);           //NOI18N
        Parameters.notNull("eval", eval);       //NOI18N
        Parameters.notNull("refHelper", refHelper); //NOI18N
        Parameters.notNull("artifacts", artifacts); //NOI18N
        Parameters.notNull("artifactElements", artifactElements);   //NOI18N
        Parameters.notNull("classPathProperty", classPathProperty); //NOI18N
        if (artifacts.length != artifactElements.length) {
            throw new IllegalArgumentException("Each artifact has to have corresponding artifactElement");  //NOI18N
        }
        return handleAntArtifactsImpl(
                project,
                helper.getAntProjectHelper(),
                cs,
                eval,
                helper,
                refHelper,
                cpUiSupportCallback,
                artifacts,
                artifactElements,
                classPathProperty,
                projectXMLElementName,
                operation);
    }
    
    private static boolean handleAntArtifactsImpl (
            @NonNull final Project project,
            @NonNull final AntProjectHelper helper,
            @NonNull final ClassPathSupport cs,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final UpdateHelper updateHelper,
            @NullAllowed final ReferenceHelper rh,
            @NullAllowed final ClassPathUiSupport.Callback cpUiSupportCallback,
            @NonNull final AntArtifact[] artifacts,
            @NonNull final URI[] artifactElements,
            @NonNull final String classPathProperty,
            @NullAllowed final String projectXMLElementName,
            final int operation) throws IOException, UnsupportedOperationException {
        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws Exception {
                            EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty (classPathProperty);
                            List<ClassPathSupport.Item> resources = cs.itemsList(raw, projectXMLElementName);
                            boolean changed = false;
                            for (int i=0; i<artifacts.length; i++) {
                                assert artifacts[i] != null;
                                assert artifactElements[i] != null;
                                ClassPathSupport.Item item = ClassPathSupport.Item.create( artifacts[i], artifactElements[i], null);
                                if (cpUiSupportCallback != null) {
                                    cpUiSupportCallback.initItem(item);
                                }
                                if (operation == ADD && !resources.contains(item)) {
                                    resources.add (item);
                                    changed = true;
                                } else if (operation == REMOVE) {
                                    int index;
                                    if ((index=resources.indexOf(item)) >=0) {
                                        ClassPathSupport.Item origin = resources.remove(index);
                                        if (rh != null) {
                                            assert updateHelper != null;
                                            removeUnusedReference(
                                                    origin,
                                                    classPathProperty,
                                                    updateHelper,
                                                    rh);
                                        }
                                        changed = true;
                                    }
                                }
                            }                            
                            if (changed) {
                                String itemRefs[] = cs.encodeToStrings( resources, projectXMLElementName);
                                props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                                props.setProperty (classPathProperty, itemRefs);
                                if (updateHelper != null) {
                                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                } else {
                                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                }
                                ProjectManager.getDefault().saveProject(project);
                                return true;
                            }
                            return false;
                        }
                    }
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            else {
                throw new IOException(e);
            }
        }
    }
    
    public static boolean handleLibraries (final Project project, final AntProjectHelper helper, final ClassPathSupport cs, final PropertyEvaluator eval,
            final ClassPathUiSupport.Callback cpUiSupportCallback, final ReferenceHelper refHelper,
            final Library[] libraries, final String classPathProperty, final String projectXMLElementName, final int operation) throws IOException, UnsupportedOperationException {
        List<ClassPathSupport.Item> items = new ArrayList<ClassPathSupport.Item>(libraries.length);
        for (Library in : libraries) {
            if (in == null) {
                throw new IllegalArgumentException("Libraries array contains a null reference: " + Arrays.toString(libraries)); //NOI18N
            }
            Library lib = checkLibrarySharability(project, helper, refHelper, in);
            ClassPathSupport.Item item = ClassPathSupport.Item.create(lib, null);
            if (cpUiSupportCallback != null) {
                cpUiSupportCallback.initItem(item);
            }
            items.add(item);
        }
        return handleLibraryClassPathItems(project, helper, cs, items, classPathProperty, projectXMLElementName, operation, true);
    }
    
    private static Library checkLibrarySharability(Project project, AntProjectHelper antHelper, ReferenceHelper refHelper, Library lib) throws IOException {
        if (antHelper.isSharableProject()) {
            if (lib.getManager().getLocation() == null) {
                LOG.log(Level.FINE, "Client is adding global library ["+lib+
                        "] to sharable project.", new Exception());
                // For backward compatibility just copy the library to shared one.
                Library l = refHelper.getProjectLibraryManager().getLibrary(lib.getName());
                if (l != null) {
                    lib = l;
                } else {
                    lib = refHelper.copyLibrary(lib);
                }
            } else if (!lib.getManager().getLocation().equals(refHelper.getProjectLibraryManager().getLocation())) {
                throw new UnsupportedOperationException("Adding library '"+lib.getName()+ // NOI18N
                    "' from '"+lib.getManager().getLocation()+"' to project '"+project.getProjectDirectory()+ // NOI18N
                    "' is not supported because project libraries are defined in '"+refHelper.getProjectLibraryManager().getLocation()+"'"); // NOI18N
            }
        }
        return lib;
    }
    
    static boolean removeUnusedReference(
        @NonNull final ClassPathSupport.Item item,
        @NonNull final String classPathProperty,
        @NonNull final UpdateHelper updateHelper,
        @NonNull final ReferenceHelper rh) {
        if (isLastReference(item, updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH), classPathProperty)) {
            destroyReference(rh, updateHelper, item);
            return true;
        }
        return false;
    }
    
    private static boolean isLastReference(
            @NonNull final ClassPathSupport.Item item,
            @NonNull final EditableProperties props,
            @NonNull final String ignoreProperty) {
            final String property = CommonProjectUtils.getAntPropertyName(item.getReference());
            for (Map.Entry<String,String> entry : props.entrySet()) {
                if (ignoreProperty.equals(entry.getKey())) {
                    continue;
                }
                if (entry.getValue().contains(property)) {
                    return false;
                }
            }
            return true;
        }

    private static void destroyReference(
            @NonNull final ReferenceHelper rh,
            @NonNull final UpdateHelper uh,
            @NonNull final ClassPathSupport.Item item) {
        if ( item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT ||
                item.getType() == ClassPathSupport.Item.TYPE_JAR ) {
            rh.destroyReference(item.getReference());
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                item.removeSourceAndJavadoc(uh);
            }
        }
    }
    
    public static boolean handleLibraryClassPathItems (final Project project, final AntProjectHelper helper, final ClassPathSupport cs, 
            final List<ClassPathSupport.Item> items, final String classPathProperty, final String projectXMLElementName, final int operation, final boolean saveProject) throws IOException {
        assert items != null : "Libraries cannot be null";  //NOI18N
        assert classPathProperty != null;
        // if the caller doesn't wish to save the project, it is expected to do it later,
        // in which case it must have PM.mutex() write access to avoid race conditions
        assert saveProject || ProjectManager.mutex().isWriteAccess();
        try {
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(classPathProperty);
                        List<ClassPathSupport.Item> resources = cs.itemsList(raw, projectXMLElementName);
                        List<ClassPathSupport.Item> changed = new ArrayList<ClassPathSupport.Item>(items.size());
                        for (ClassPathSupport.Item item : items) {
                            assert item != null;
                            assert item.getType() == ClassPathSupport.Item.TYPE_LIBRARY;
                            if (operation == ADD && !resources.contains(item)) {
                                resources.add (item);                                
                                changed.add(item);
                            }
                            else if (operation == REMOVE && resources.contains(item)) {
                                resources.remove(item);
                                changed.add(item);
                            }
                        }
                        if (!changed.isEmpty()) {
                            String itemRefs[] = cs.encodeToStrings( resources, projectXMLElementName);
                            props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //PathParser may change the EditableProperties                                
                            props.setProperty(classPathProperty, itemRefs);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            if (saveProject) {
                                ProjectManager.getDefault().saveProject(project);
                            }
                            return true;
                        }
                        return false;
                    }
                }
            );
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    // accessor to performSharabilityHeuristics protected method
    private static class Accessor extends ProjectClassPathModifierImplementation  {

        public static String performHeuristics(URI classpathRoot, AntProjectHelper helper) throws URISyntaxException, IOException {
            Accessor i = new Accessor();
            return i.performSharabilityHeuristics(classpathRoot, helper);
        }
        
        @Override
        protected SourceGroup[] getExtensibleSourceGroups() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean addLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean removeLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean addRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean removeRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean addAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean removeAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    
    private static class ClassPathPackageAccessorImpl extends ClassPathPackageAccessor {

        @Override
        public boolean removeUnusedReference(Item item, String classPathProperty, UpdateHelper updateHelper, ReferenceHelper rh) {
            return ClassPathModifierSupport.removeUnusedReference(item, classPathProperty, updateHelper, rh);
        }
        
    }
}
