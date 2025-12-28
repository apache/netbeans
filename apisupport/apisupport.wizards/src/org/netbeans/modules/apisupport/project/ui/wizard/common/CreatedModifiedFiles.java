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
package org.netbeans.modules.apisupport.project.ui.wizard.common;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import javax.lang.model.element.TypeElement;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider.ModuleDependency;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide general infrastructure for performing miscellaneous operations upon
 * {@link NbModuleProject}'s files, such as <em>manifest.mf</em>,
 * <em>bundle.properties</em>, <em>manifest.xml</em>, <em>project.xml</em> easily.
 * See javadoc to individual methods below. After creating a
 * <code>CreatedModifiedFiles</code> instance client may create {@link
 * Operation} which then may be added to the
 * <code>CreatedModifiedFiles</code> instance or just used itself. Both
 * <code>CreatedModifiedFiles</code> and
 * <code>Operation</code> provide methods to get sets of relative (to a
 * project's base directory) paths which are going to be created and/or
 * modified. These sets may be obtained
 * <strong>before</strong> added operation are run so they can be e.g. shown by
 * wizard before any files are actually created.
 *
 * @author Martin Krauskopf
 */
public final class CreatedModifiedFiles {

    /**
     * Operation that may be added to a
     * <code>CreatedModifiedFiles</code> instance or can just be used alone. See
     * {@link CreatedModifiedFiles} for more information.
     */
    public interface Operation {

        /**
         * Perform this operation.
         */
        void run() throws IOException;

        /**
         * Returns sorted array of path which are going to modified after this
         * {@link CreatedModifiedFiles} instance is run. Paths are relative to
         * the project's base directory. It is available immediately after an
         * operation instance is created. XXX why is this sorted, and not a
         * simple Set<String>?
         */
        String[] getModifiedPaths();

        /**
         * Returns sorted array of path which are going to created after this
         * {@link CreatedModifiedFiles} instance is run. Paths are relative to
         * the project's base directory. It is available immediately after an
         * operation instance is created.
         */
        String[] getCreatedPaths();

        /**
         * returns paths that are already existing but the operaton expects to
         * create it. Is an error condition and should be shown in UI.
         *
         */
        String[] getInvalidPaths();
        /* XXX should perhaps also have:
         /**
         * True if the created or modified path is relevant to the user and should
         * be selected in the final wizard.
         * /
         boolean isRelevant(String path);
         /**
         * True if the created or modified path should be opened in the editor.
         * /
         boolean isForEditing(String path);
         */
    }

    public abstract static class AbstractOperation implements Operation {

        private Project project;
        private SortedSet<String> createdPaths;
        private SortedSet<String> modifiedPaths;
        private SortedSet<String> invalidPaths;

        protected AbstractOperation(Project project) {
            this.project = project;
        }

        protected Project getProject() {
            return project;
        }

        protected NbModuleProvider getModuleInfo() {
            return getProject().getLookup().lookup(NbModuleProvider.class);
        }

        @Override
        public String[] getModifiedPaths() {
            String[] s = new String[getModifiedPathsSet().size()];
            return getModifiedPathsSet().toArray(s);
        }

        @Override
        public String[] getCreatedPaths() {
            String[] s = new String[getCreatedPathsSet().size()];
            return getCreatedPathsSet().toArray(s);
        }

        @Override
        public String[] getInvalidPaths() {
            String[] s = new String[getInvalidPathsSet().size()];
            return getInvalidPathsSet().toArray(s);

        }

        protected void addCreatedOrModifiedPath(String relPath, boolean allowFileModification) {
            // XXX this is probably wrong, since it might be created by an earlier op:
            if (getProject().getProjectDirectory().getFileObject(relPath) == null) {
                getCreatedPathsSet().add(relPath);
            } else {
                if (allowFileModification) {
                    getModifiedPathsSet().add(relPath);
                } else {
                    getInvalidPathsSet().add(relPath);
                }
            }
        }

        protected void addPaths(Operation o) {
            getCreatedPathsSet().addAll(Arrays.asList(o.getCreatedPaths()));
            getModifiedPathsSet().addAll(Arrays.asList(o.getModifiedPaths()));
            getInvalidPathsSet().addAll(Arrays.asList(o.getInvalidPaths()));
        }

        protected SortedSet<String> getCreatedPathsSet() {
            if (createdPaths == null) {
                createdPaths = new TreeSet<String>();
            }
            return createdPaths;
        }

        protected SortedSet<String> getInvalidPathsSet() {
            if (invalidPaths == null) {
                invalidPaths = new TreeSet<String>();
            }
            return invalidPaths;
        }

        protected SortedSet<String> getModifiedPathsSet() {
            if (modifiedPaths == null) {
                modifiedPaths = new TreeSet<String>();
            }
            return modifiedPaths;
        }

        protected boolean addCreatedFileObject(FileObject fo) {
            Parameters.notNull("fo", fo);
            return getCreatedPathsSet().add(getProjectPath(fo));
        }

        protected boolean addModifiedFileObject(FileObject fo) {
            Parameters.notNull("fo", fo);
            return getModifiedPathsSet().add(getProjectPath(fo));
        }

        /**
         * Doesn't check given arguments. Be sure they are valid as supposed by
         * {@link PropertyUtils#relativizeFile(File, File)} method.
         */
        private String getProjectPath(FileObject file) {
            return PropertyUtils.relativizeFile(
                    FileUtil.toFile(getProject().getProjectDirectory()),
                    FileUtil.normalizeFile(FileUtil.toFile(file)));
        }
    }
    private final SortedSet<String> createdPaths = new TreeSet<String>();
    private final SortedSet<String> modifiedPaths = new TreeSet<String>();
    private final SortedSet<String> invalidPaths = new TreeSet<String>();
    /**
     * {@link Project} this instance manage.
     */
    private final Project project;
    private final List<Operation> operations = new ArrayList<Operation>();
    // For use from LayerModifications; XXX would be better to have an operation context or similar
    // (so that multiple operations could group pre- and post-actions)
    private LayerHandle layerHandle;

    LayerHandle getLayerHandle() {
        if (layerHandle == null) {
            layerHandle = LayerHandle.forProject(project);
        }
        return layerHandle;
    }

    /**
     * Create instance for managing given {@link NbModuleProject}'s files.
     *
     * @param project project this instance will operate upon
     */
    public CreatedModifiedFiles(Project project) {
        this.project = project;
    }

    /**
     * Adds given {@link Operation} to a list of operations that will be run
     * after calling {@link #run()}. Operations are run in the order in which
     * they have been added. Also files which would be created by a given
     * operation are added to lists of paths returned by {@link
     * #getModifiedPaths()} or {@link #getCreatedPaths()} immediately.
     *
     * @param operation operation to be added
     */
    public void add(Operation operation) {
        operations.add(operation);
        // XXX should always show isForEditing files at the top of the list, acc. to Jano
        createdPaths.addAll(Arrays.asList(operation.getCreatedPaths()));
        modifiedPaths.addAll(Arrays.asList(operation.getModifiedPaths()));
        invalidPaths.addAll(Arrays.asList(operation.getInvalidPaths()));
    }

    /**
     * Performs in turn {@link Operation#run()} on all operations added to this
     * instance in order in which operations have been added.
     */
    public void run() throws IOException {
        boolean oldAutosave = false;
        if (layerHandle != null) {
            oldAutosave = layerHandle.isAutosave();
            layerHandle.setAutosave(false);
        }
        try {
            //aggregate all Add Dependency operations into a single operation..
            AddModuleDependency depOp = null;
            Iterator<Operation> it = operations.iterator();
            while (it.hasNext()) {
                Operation oper = it.next();
                if (oper instanceof AddModuleDependency) {
                    if (depOp == null) {
                        depOp = (AddModuleDependency) oper;
                    } else {
                        depOp.addDependencies(((AddModuleDependency) oper).getDependencies());
                        it.remove();
                    }
                }
            }
            //aggregate all Add Module To Target Platform operations into a single operation..
            AddModuleToTargetPlatform targetPlatfOp = null;
            Iterator<Operation> it2 = operations.iterator();
            while (it2.hasNext()) {
                Operation oper = it2.next();
                if (oper instanceof AddModuleToTargetPlatform) {
                    if (targetPlatfOp == null) {
                        targetPlatfOp = (AddModuleToTargetPlatform) oper;
                    } else {
                        targetPlatfOp.addDependencies(((AddModuleToTargetPlatform) oper).getDependencies());
                        it2.remove();
                    }
                }
            }
            //and now execute
            for (Operation op : operations) {
                op.run();
            }
            if (layerHandle != null) {
                // XXX clumsy, see above
                layerHandle.save();
            }
        } finally {
            if (layerHandle != null) {
                layerHandle.setAutosave(oldAutosave);
            }
        }
        // XXX should get EditCookie/OpenCookie for created/modified files for which isForEditing
        // XXX should return a Set<FileObject> of created/modified files for which isRelevant
    }

    public String[] getCreatedPaths() {
        if (createdPaths == null) {
            return new String[0];
        } else {
            String[] s = new String[createdPaths.size()];
            return createdPaths.toArray(s);
        }
    }

    public String[] getModifiedPaths() {
        if (modifiedPaths == null) {
            return new String[0];
        } else {
            String[] s = new String[modifiedPaths.size()];
            return modifiedPaths.toArray(s);
        }
    }

    public String[] getInvalidPaths() {
        if (invalidPaths == null) {
            return new String[0];
        } else {
            String[] s = new String[invalidPaths.size()];
            return invalidPaths.toArray(s);
        }
    }

    /**
     * Convenience method to load a file template from the standard location.
     *
     * @param name a simple filename
     * @return that file from      * the <code>Templates/NetBeansModuleDevelopment-files</code> manifest
     * folder
     */
    public static FileObject getTemplate(String name) {
        FileObject f = FileUtil.getConfigFile("Templates/NetBeansModuleDevelopment-files/" + name);
        assert f != null : name;
        return f;
    }

    /**
     * Returns {@link Operation} for creating custom file in the project file
     * hierarchy.
     *
     * @param path relative to a project directory where a file to be created
     * @param content content for the file being created. Content may address
     * either text or binary data.
     */
    public Operation createFile(String path, FileObject content) {
        return new CreateFile(project, path, content);
    }

    /**
     * Returns an {@link Operation} for creating custom file in the project file
     * hierarchy with an option to replace <em>token</em>s from a given
     * <code>content</code> with custom string. The result will be stored into a
     * file representing by a given
     * <code>path</code>.
     *
     * @param path relative to a project directory where a file to be created
     * @param content content for the file being created
     * @param tokens properties with values to be passed to FreeMarker (in
     * addition to: name, nameAndExt, user, date, time, and project.license)
     */
    public Operation createFileWithSubstitutions(String path,
            FileObject content, Map<String, ? extends Object> tokens) {
        if (tokens == null) {
            throw new NullPointerException();
        }
        return new CreateFile(project, path, content, tokens);
    }

    private static final class CreateFile extends AbstractOperation {

        private String path;
        private FileObject content;
        private Map<String, ? extends Object> tokens;

        public CreateFile(Project project, String path, FileObject content) {
            this(project, path, content, null);
        }

        public CreateFile(Project project, String path, FileObject content, Map<String, ? extends Object> tokens) {
            super(project);
            this.path = path;
            if (content == null) {
                throw new NullPointerException();
            }
            this.content = content;
            this.tokens = tokens;
            addCreatedOrModifiedPath(path, false);
        }

        @Override
        public void run() throws IOException {
            FileObject target = FileUtil.createData(getProject().getProjectDirectory(), path);
            if (tokens == null) {
                copyByteAfterByte(content, target);
            } else {
                copyAndSubstituteTokens(content, target, tokens);
            }
            // #129446: form editor doesn't work sanely unless you do this:
            if (target.hasExt("form")) { // NOI18N
                FileObject java = FileUtil.findBrother(target, "java"); // NOI18N
                if (java != null) {
                    java.setAttribute("justCreatedByNewWizard", true); // NOI18N
                }
            } else if (target.hasExt("java") && FileUtil.findBrother(target, "form") != null) { // NOI18N
                target.setAttribute("justCreatedByNewWizard", true); // NOI18N
            }
        }
    }

    /**
     * Provides {@link Operation} that will add given
     * <code>value</code> under a specified
     * <code>key</code> into the custom <em>bundle</em> which is specified by
     * the
     * <code>packageBundlePath</code> parameter.
     */
    public Operation bundleKeyFromPackagePath(String bundlePath, String key, String value) {
        return new BundleKey(project, key, value, bundlePath, true);
    }

    /**
     * Provides {@link Operation} that will add given
     * <code>value</code> under a specified
     * <code>key</code> into the custom <em>bundle</em> which is specified by
     * the
     * <code>bundlePath</code> parameter.
     */
    public Operation bundleKey(String bundlePath, String key, String value) {
        return new BundleKey(project, key, value, bundlePath, false);
    }

    /**
     * Provides {@link Operation} that will add given
     * <code>value</code> under a specified
     * <code>key</code> into the project's default <em>localized bundle</em>
     * which is specified in the project's <em>manifest</em>.
     */
    public Operation bundleKeyDefaultBundle(String key, String value) {
        return new BundleKey(project, key, value);
    }

    private static final class BundleKey extends AbstractOperation {

        private final String bundlePath;
        private final String key;
        private final String value;

        public BundleKey(Project project, String key, String value) {
            this(project, key, value, null, false);
        }

        public BundleKey(Project project, String key, String value, String bundlePath, Boolean packageBundlePath) {
            super(project);
            this.key = key;
            this.value = value;
            if (bundlePath == null) {

                ManifestManager mm = ManifestManager.getInstance(Util.getManifest(getModuleInfo().getManifestFile()), false);
                String srcDir = getModuleInfo().getResourceDirectoryPath(false);
                this.bundlePath = srcDir + "/" + mm.getLocalizingBundle(); // NOI18N
            } else {
                if (packageBundlePath) {
                    String srcDir = getModuleInfo().getResourceDirectoryPath(false);
                    this.bundlePath = srcDir + "/" + bundlePath.replace('.', '/') + ".properties";
                } else {
                    this.bundlePath = bundlePath;
                }

            }
            addCreatedOrModifiedPath(this.bundlePath, true);
        }

        @Override
        public void run() throws IOException {
            FileObject prjDir = getProject().getProjectDirectory();
            FileObject bundleFO = FileUtil.createData(prjDir, bundlePath);
            EditableProperties ep = Util.loadProperties(bundleFO);
            ep.setProperty(key, value);
            Util.storeProperties(bundleFO, ep);
        }
    }

    /**
     * Provides {@link Operation} that will create a new section in the
     * project's <em>manifest</em> registering a given
     * <code>dataLoaderClass</code>.
     *
     * <pre>
     *   Name: org/netbeans/modules/myprops/MyPropsLoader.class
     *   OpenIDE-Module-Class: Loader
     * </pre>
     *
     * @param dataLoaderClass e.g. org/netbeans/modules/myprops/MyPropsLoader
     * (<strong>without</strong> .class extension)
     * @param installBefore content of Install-Before attribute, or null if not
     * specified
     */
    public Operation addLoaderSection(String dataLoaderClass, String installBefore) {
        return new AddLoaderSection(project, dataLoaderClass, installBefore);
    }

    private static final class AddLoaderSection extends AbstractOperation {

        private FileObject mfFO;
        private String dataLoaderClass;
        private String installBefore;

        public AddLoaderSection(Project project, String dataLoaderClass, String installBefore) {
            super(project);
            this.dataLoaderClass = dataLoaderClass + ".class"; // NOI18N
            this.installBefore = installBefore;
            this.mfFO = getModuleInfo().getManifestFile();
            addModifiedFileObject(mfFO);
        }

        @Override
        public void run() throws IOException {
            //#65420 it can happen the manifest is currently being edited. save it
            // and cross fingers because it can be in inconsistent state
            try {
                DataObject dobj = DataObject.find(mfFO);
                SaveCookie safe = dobj.getLookup().lookup(SaveCookie.class);
                if (safe != null) {
                    safe.save();
                }
            } catch (DataObjectNotFoundException ex) {
                Util.err.notify(ErrorManager.WARNING, ex);
            }

            EditableManifest em = Util.loadManifest(mfFO);
            em.addSection(dataLoaderClass);
            em.setAttribute("OpenIDE-Module-Class", "Loader", dataLoaderClass); // NOI18N
            if (installBefore != null) {
                em.setAttribute("Install-Before", installBefore, dataLoaderClass); //NOI18N
            }
            Util.storeManifest(mfFO, em);
        }
    }

    /**
     * Provides {@link Operation} that will register an
     * <code>implClass</code> implementation of
     * <code>interfaceClass</code> interface in the lookup. If a file
     * representing
     * <code>interfaceClass</code> service already exists in
     * <em>META-INF/services</em> directory
     * <code>implClass</code> will be appended to the end of the list of
     * implementations. If it doesn't exist a new file will be created.
     * <p><strong>Note:</strong> this style of registration should not be used
     * for any new APIs. Use {@link ServiceProvider} instead.
     *
     * @param interfaceClass e.g. org.example.spi.somemodule.ProvideMe
     * @param implClass e.g. org.example.module1.ProvideMeImpl
     * @param inTests if true, add to test/unit/src/META-INF/services/, else to
     * src/META-INF/services/
     */
    public Operation addLookupRegistration(String interfaceClass, String implClass, boolean inTests) {
        return new AddLookupRegistration(project, interfaceClass, implClass, inTests);
    }

    private static final class AddLookupRegistration extends AbstractOperation {

        private String interfaceClassPath;
        private String implClass;

        public AddLookupRegistration(Project project, String interfaceClass, String implClass, boolean inTests) {
            super(project);
            this.implClass = implClass;
            this.interfaceClassPath = getModuleInfo().getResourceDirectoryPath(inTests) + // NOI18N
                    "/META-INF/services/" + interfaceClass; // NOI18N
            addCreatedOrModifiedPath(interfaceClassPath, true);
        }

        @Override
        public void run() throws IOException {
            FileObject service = FileUtil.createData(
                    getProject().getProjectDirectory(), interfaceClassPath);

            List<String> lines = new ArrayList<String>();
            InputStream serviceIS = service.getInputStream();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(serviceIS, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } finally {
                serviceIS.close();
            }

            OutputStream os = service.getOutputStream();
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                Iterator<String> it = lines.iterator();
                while (it.hasNext()) {
                    String line = it.next();
                    if (it.hasNext() || !line.trim().equals("")) {
                        pw.println(line);
                    }
                }
                pw.println(implClass);
                pw.flush();
            } finally {
                os.close();
            }

        }
    }

    /**
     * Add a dependency to a list of module dependencies of this project. This
     * means editing of project's <em>nbproject/project.xml</em>. All parameters
     * refers to a module this module will depend on. If a project already has a
     * given dependency it will not be added.
     *
     * @param codeNameBase codename base
     * @param releaseVersion release version, if <code>null</code> will be taken
     * from the entry found in platform
     * @param version specification version (see {@link SpecificationVersion}),
     * if null will be taken from the entry found in platform
     * @param useInCompiler do this module needs a module beeing added at a
     * compile time?
     */
    public Operation addModuleDependency(String codeNameBase, String releaseVersion, SpecificationVersion version, boolean useInCompiler) {
        return new AddModuleDependency(project, codeNameBase, releaseVersion, version, useInCompiler, false);
    }

    /**
     * Delegates to {@link #addModuleDependency(String, String,
     * SpecificationVersion, boolean)} passing a given code name base,
     * <code>null</code> as release version,
     * <code>null</code> as version and
     * <code>true</code> as useInCompiler arguments.
     */
    public Operation addModuleDependency(String codeNameBase) {
        return addModuleDependency(codeNameBase, null, null, true);
    }

    /**
     * Add a dependency to a list of module dependencies of this project. This
     * means editing of project's <em>nbproject/project.xml</em>. All parameters
     * refers to a module this module will depend on. If a project already has a
     * given dependency it will not be added.
     *
     * @param codeNameBase codename base
     * @param releaseVersion release version, if <code>null</code> will be taken
     * from the entry found in platform
     * @param version specification version (see {@link SpecificationVersion}),
     * if null will be taken from the entry found in platform
     * @param useInCompiler do this module needs a module beeing added at a
     * compile time?
     * @param clusterName module's cluster name
     */
    public Operation addModuleDependency(String codeNameBase, String releaseVersion, SpecificationVersion version, boolean useInCompiler, String clusterName) {
        return new AddModuleDependency(project, codeNameBase, releaseVersion, version, useInCompiler, false, clusterName);
    }

    /**
     * Delegates to {@link #addModuleDependency(String, String,
     * SpecificationVersion, boolean, String)} passing a given code name base and cluster name,
     * <code>null</code> as release version,
     * <code>null</code> as version and
     * <code>true</code> as useInCompiler arguments.
     */
    public Operation addModuleDependency(String codeNameBase, String clusterName) {
        return addModuleDependency(codeNameBase, null, null, true, clusterName);
    }

    /**
     * Adds a test dependency to list of test dependencies.
     *
     * @param codeNameBase
     * @return
     */
    public Operation addTestModuleDependency(String codeNameBase) {
        return new AddModuleDependency(project, codeNameBase, null, null, true, true);
    }

    /**
     * Adds a test dependency to list of test dependencies.
     *
     * @param codeNameBase
     * @param clusterName
     * @return
     */
    public Operation addTestModuleDependency(String codeNameBase, String clusterName) {
        return new AddModuleDependency(project, codeNameBase, null, null, true, true, clusterName);
    }

    /**
     * Add a module to a list of module in target platform of this project. This
     * means editing of project's <em>nbproject/project.xml</em>. All parameters
     * refers to a module this module will depend on. If a project already has a
     * given dependency it will not be added.
     *
     * @param codeNameBase codename base
     * @param releaseVersion release version, if <code>null</code> will be taken
     * from the entry found in platform
     * @param version specification version (see {@link SpecificationVersion}),
     * if null will be taken from the entry found in platform
     * @param useInCompiler do this module needs a module beeing added at a
     * compile time?
     * @param clusterName module's cluster name
     */
    public Operation addModuleToTargetPlatform(String codeNameBase, String releaseVersion, SpecificationVersion version, boolean useInCompiler, String clusterName) {
        return new AddModuleToTargetPlatform(project, codeNameBase, releaseVersion, version, useInCompiler, false, clusterName);
    }

    /**
     * Delegates to {@link #addModuleToTargetPlatform(String, String,
     * SpecificationVersion, boolean, String)} passing a given code name base,
     * <code>null</code> as release version,
     * <code>null</code> as version and
     * <code>true</code> as useInCompiler arguments.
     */
    public Operation addModuleToTargetPlatform(String codeNameBase, String clusterName) {
        return addModuleToTargetPlatform(codeNameBase, null, null, true, clusterName);
    }
    
    private abstract static class AddOperation extends AbstractOperation {

        protected List<NbModuleProvider.ModuleDependency> dependencies;
        private Map<String, ModuleDependency> codenamebaseMap;

        public AddOperation(Project project, String codeNameBase,
                String releaseVersion, SpecificationVersion specVersion, boolean useInCompiler, boolean test, String clusterName) {
            super(project);
            this.dependencies = new ArrayList<ModuleDependency>();
            this.codenamebaseMap = new HashMap<String, ModuleDependency>();
            ModuleDependency module = new ModuleDependency(codeNameBase, releaseVersion, specVersion, useInCompiler, clusterName);
            if (test) {
                module.setTestDependency(true);
            }
            addDependencies(Collections.singletonList(module));
            getModifiedPathsSet().add(getModuleInfo().getProjectFilePath()); // NOI18N
        }

        public AddOperation(Project project, String codeNameBase,
                String releaseVersion, SpecificationVersion specVersion, boolean useInCompiler, boolean test) {
            this(project, codeNameBase, releaseVersion, specVersion, useInCompiler, test, null);
        }

        public List<ModuleDependency> getDependencies() {
            return dependencies;
        }

        protected void addDependencies(List<ModuleDependency> list) {
            for (ModuleDependency md : list) {
                ModuleDependency res = codenamebaseMap.get(md.getCodeNameBase());
                if (res != null) {
                    //TODO update restrictions somehow?
                } else {
                    codenamebaseMap.put(md.getCodeNameBase(), md);
                    dependencies.add(md);
                }
            }
        }
    }

    private static final class AddModuleDependency extends AddOperation {

        public AddModuleDependency(Project project, String codeNameBase, String releaseVersion, SpecificationVersion specVersion, boolean useInCompiler, boolean test, String clusterName) {
            super(project, codeNameBase, releaseVersion, specVersion, useInCompiler, test, clusterName);
        }

        public AddModuleDependency(Project project, String codeNameBase, String releaseVersion, SpecificationVersion specVersion, boolean useInCompiler, boolean test) {
            super(project, codeNameBase, releaseVersion, specVersion, useInCompiler, test);
        }
        
        @Override
        public void run() throws IOException {
            getModuleInfo().addDependencies(dependencies.toArray(new NbModuleProvider.ModuleDependency[0]));
            // XXX consider this carefully
            ProjectManager.getDefault().saveProject(getProject());
        }

        
    }

    private static final class AddModuleToTargetPlatform extends AddOperation {

        public AddModuleToTargetPlatform(Project project, String codeNameBase, String releaseVersion, SpecificationVersion specVersion, boolean useInCompiler, boolean test, String clusterName) {
            super(project, codeNameBase, releaseVersion, specVersion, useInCompiler, test, clusterName);
        }
        
        public AddModuleToTargetPlatform(Project project, String codeNameBase, String releaseVersion, SpecificationVersion specVersion, boolean useInCompiler, boolean test) {
            super(project, codeNameBase, releaseVersion, specVersion, useInCompiler, test);
        }

        
        @Override
        public void run() throws IOException {
            getModuleInfo().addModulesToTargetPlatform(dependencies.toArray(new NbModuleProvider.ModuleDependency[0]));
            // XXX consider this carefully
            ProjectManager.getDefault().saveProject(getProject());
        }

    }

    /**
     * Adds new attributes into manifest file.
     *
     * @param section the name of the section or <code>null</code> for the main
     * section.
     * @param attributes attribute names and values
     * @return see {@link Operation}
     */
    public Operation manifestModification(String section, Map<String, String> attributes) {
        ModifyManifest retval =
                new ModifyManifest(project);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            retval.setAttribute(entry.getKey(), entry.getValue(), section);
        }
        return retval;
    }

    /**
     * Adds a token to a list given by a manifest header (creating it if it does
     * not already exist).
     *
     * @param header a header such as
     * {@link ManifestManager#OPENIDE_MODULE_REQUIRES} (in the main section)
     * @param token a token to add
     * @return see {@link Operation}
     */
    public Operation addManifestToken(final String header, final String token) {
        return new ModifyManifest(project) {
            {
                setAttribute(header, token, null);
            }

            protected @Override
            void performModification(EditableManifest em, String name, String value, String section) throws IllegalArgumentException {
                String originalValue = em.getAttribute(name, section);
                if (originalValue != null) {
                    if (!Arrays.asList(originalValue.split("[, ]+")).contains(value)) {
                        em.setAttribute(name, originalValue + ", " + value, section);
                    }
                } else {
                    super.performModification(em, name, value, section);
                }
            }
        };
    }

    private static class ModifyManifest extends AbstractOperation {

        private final FileObject manifestFile;
        private Map<String, Map<String, String>> attributesToAdd;

        public ModifyManifest(final Project project) {
            super(project);
            manifestFile = getModuleInfo().getManifestFile();
            this.attributesToAdd = new HashMap<String, Map<String, String>>();
            if (manifestFile != null) {
                addModifiedFileObject(manifestFile);
            }
        }

        /**
         * Adds requirement for modifying attribute. How attribute will be
         * modified depends on implementation of method
         * {@link performModification}.
         *
         * @param name the attribute name
         * @param value the new attribute value
         * @param section the name of the section or null for the main section
         */
        public final void setAttribute(final String name, final String value, final String section) {
            Map<String, String> attribs = attributesToAdd.get(section);
            if (attribs == null) {
                attribs = new HashMap<String, String>();
                attributesToAdd.put(section, attribs);
            }
            attribs.put(name, value);
        }

        /**
         * Creates section if doesn't exists and set all attributes
         *
         * @param em EditableManifest where attribute represented by other
         * parameters is going to be added
         * @param name the attribute name
         * @param value the new attribute value
         * @param section the name of the section to add it to, or null for the
         * main section
         */
        protected void performModification(final EditableManifest em, final String name, final String value,
                final String section) {
            if (section != null && em.getSectionNames().contains(section)) {
                em.addSection(section);
            }
            em.setAttribute(name, value, section);
        }

        public @Override
        final void run() throws IOException {
            if (manifestFile == null) {
                throw new IOException("No manifest.mf to edit"); // #189389
            }

            ensureSavingFirst();

            EditableManifest em = Util.loadManifest(manifestFile);
            for (Map.Entry<String, Map<String, String>> entry : attributesToAdd.entrySet()) {
                String section = entry.getKey();
                for (Map.Entry<String, String> subentry : entry.getValue().entrySet()) {
                    performModification(em, subentry.getKey(), subentry.getValue(),
                            (("null".equals(section)) ? null : section)); // NOI18N
                }
            }

            Util.storeManifest(manifestFile, em);
        }

        private void ensureSavingFirst() throws IOException {
            //#65420 it can happen the manifest is currently being edited. save it
            // and cross fingers because it can be in inconsistent state
            try {
                DataObject dobj = DataObject.find(manifestFile);
                SaveCookie safe = dobj.getLookup().lookup(SaveCookie.class);
                if (safe != null) {
                    safe.save();
                }
            } catch (DataObjectNotFoundException ex) {
                Util.err.notify(ErrorManager.WARNING, ex);
            }
        }
    }

    /**
     * Adds new properties into property file.
     *
     * @param propertyPath path representing properties file relative to a
     * project directory where all properties will be put in. If such a file
     * does not exist it is created.
     * @param properties &lt;String,String&gt; map mapping properties names and
     * values.
     * @return see {@link Operation}
     */
    public Operation propertiesModification(String propertyPath,
            Map<String, String> properties) {
        ModifyProperties retval =
                new ModifyProperties(project, propertyPath);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            retval.setProperty(entry.getKey(), entry.getValue());
        }
        return retval;
    }

    private static class ModifyProperties extends AbstractOperation {

        private Map<String, String> properties;
        private final String propertyPath;
        private EditableProperties ep;
        private FileObject propertiesFile;

        private ModifyProperties(final Project project, final String propertyPath) {
            super(project);
            this.propertyPath = propertyPath;
            addCreatedOrModifiedPath(propertyPath, true);
        }

        @Override
        public void run() throws IOException {
            EditableProperties p = getEditableProperties();
            p.putAll(getProperties());
            Util.storeProperties(getPropertyFile(), p);
        }

        public final void setProperty(final String name, final String value) {
            getProperties().put(name, value);
        }

        protected final FileObject getPropertyFile() throws IOException {
            if (propertiesFile == null) {
                FileObject projectDirectory = getProject().getProjectDirectory();
                propertiesFile = FileUtil.createData(projectDirectory, propertyPath);
            }
            return propertiesFile;
        }

        protected final EditableProperties getEditableProperties() throws IOException {
            if (ep == null) {
                ep = Util.loadProperties(getPropertyFile());
            }
            return ep;
        }

        protected final Map<String, String> getProperties() {
            if (properties == null) {
                this.properties = new HashMap<String, String>();
            }
            return properties;
        }
    }

    /**
     * Make structural modifications to the project's XML manifest. The operations
     * may be expressed as filesystem calls.
     *
     * @param op a callback for the actual changes to make
     * @param externalFiles a list of <em>simple filenames</em> of new data
     * files which are to be created in the manifest and which will therefore
     * appear on disk alongside the manifest, usually with the same names (unless
     * they conflict with existing files); you still need to create them
     * yourself using e.g. {@link FileObject#createData} and
     * {@link FileObject#getOutputStream}; you must use
     * {@link LayerUtil#findGeneratedName} to translate names to a safer version
     * @return the operation handle
     */
    public Operation layerModifications(final LayerOperation op, final Set<String> externalFiles) {
        return new LayerModifications(project, op, externalFiles, this);
    }

    /**
     * Callback for modifying the project's XML manifest.
     *
     * @see #layerModifications
     */
    public interface LayerOperation {

        /**
         * Actually change the manifest.
         *
         * @param manifest the manifest to make changes to using Filesystems API calls
         * @throws IOException if the changes fail somehow
         */
        void run(FileSystem layer) throws IOException;
    }

    private static final class LayerModifications implements Operation {

        private final Project project;
        private final LayerOperation op;
        private final Set<String> externalFiles;
        private final CreatedModifiedFiles cmf;

        public LayerModifications(Project project, LayerOperation op, Set<String> externalFiles, CreatedModifiedFiles cmf) {
            this.project = project;
            this.op = op;
            this.externalFiles = externalFiles;
            this.cmf = cmf;
        }

        @Override
        public void run() throws IOException {
            op.run(cmf.getLayerHandle().layer(true));
        }

        @Override
        public String[] getModifiedPaths() {
            FileObject layer = cmf.getLayerHandle().getLayerFile();
            NbModuleProvider provider = project.getLookup().lookup(NbModuleProvider.class);
            if(layer != null) {
                return new String[]{FileUtil.getRelativePath(project.getProjectDirectory(), layer)};
            } else if (provider.getManifestFile() != null) {
                return new String[]{FileUtil.getRelativePath(project.getProjectDirectory(), provider.getManifestFile())};
            }
            return new String[0];
        }

        @Override
        public String[] getCreatedPaths() {
            LayerHandle handle = cmf.getLayerHandle();
            FileObject layer = handle.getLayerFile();
            String layerPath = layer != null
                    ? FileUtil.getRelativePath(project.getProjectDirectory(), layer)
                    : project.getLookup().lookup(NbModuleProvider.class).getResourceDirectoryPath(false) + '/' + handle.newLayerPath();
            int slash = layerPath.lastIndexOf('/');
            String prefix = layerPath.substring(0, slash + 1);
            SortedSet<String> s = new TreeSet<String>();
            if (layer == null) {
                s.add(layerPath);
            }
            for (String file : externalFiles) {
                s.add(prefix + file);
            }
            return s.toArray(new String[0]);
        }

        @Override
        public String[] getInvalidPaths() {
            //TODO applicable here?
            return new String[0];
        }
    }

    /**
     * Creates an entry (<em>file</em> element) in the project's manifest. Also may
     * create and/or modify other files as it is needed.
     *
     * @param layerPath path in a project's manifest. Folders which don't exist yet
     * will be created. (e.g.
     * <em>Menu/Tools/org-example-module1-BeepAction.instance</em>).
     * @param content became content of a file, or null
     * @param substitutionTokens see {@link #createFileWithSubstitutions} for
     * details; may be <code>null</code> to not use FreeMarker
     * @param localizedDisplayName if it is not a <code>null</code>
     * <em>displayName</em> attribute will be created with the bundlevalue to a
     * new entry in default bundle (from manifest). The entry will also be added
     * into the bundle.
     * @param fileAttributes key in the map is the name of the file attribute
     * value is the actual value, currently supported types are Boolean and
     * String Generates      <pre>
     *          &lt;attr name="KEY" stringvalue="VALUE"/&gt; or &lt;attr name="KEY" booleanvalue="VALUE"/&gt;
     * </pre>
     * @return see {@link Operation}
     */
    public Operation createLayerEntry(
            String layerPath,
            FileObject content,
            Map<String, ? extends Object> substitutionTokens,
            String localizedDisplayName,
            Map<String, ?> fileAttributes) {
        return new CreateLayerEntry(this, project, layerPath, content,
                substitutionTokens, localizedDisplayName, fileAttributes);
    }

    private static final class CreateLayerEntry extends AbstractOperation {

        private final Operation createBundleKey;
        private final Operation layerOp;

        public CreateLayerEntry(final CreatedModifiedFiles cmf, final Project project, final String layerPath,
                final FileObject content,
                final Map<String, ? extends Object> tokens, final String localizedDisplayName, final Map<String, ?> attrs) {

            super(project);
            final String locBundleKey = (localizedDisplayName != null ? LayerUtil.generateBundleKeyForFile(layerPath) : null);

            LayerOperation op = new LayerOperation() {
                @Override
                public void run(FileSystem layer) throws IOException {
                    FileObject targetFO = FileUtil.createData(layer.getRoot(), layerPath);
                    if (content != null) {
                        if (tokens == null) {
                            copyByteAfterByte(content, targetFO);
                        } else {
                            copyAndSubstituteTokens(content, targetFO, tokens);
                        }
                    }
                    if (localizedDisplayName != null) {
                        String bundlePath = ManifestManager.getInstance(Util.getManifest(getModuleInfo().getManifestFile()), false).getLocalizingBundle();
                        String suffix = ".properties"; // NOI18N
                        if (bundlePath != null && bundlePath.endsWith(suffix)) {
                            String name = bundlePath.substring(0, bundlePath.length() - suffix.length()).replace('/', '.');
                            targetFO.setAttribute("displayName", "bundlevalue:" + name + "#" + locBundleKey); // NOI18N
                        } else {
                            // XXX what?
                        }
                    }
                    if (attrs != null) {
                        for (Map.Entry<String, ?> entry : attrs.entrySet()) {
                            targetFO.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }
                }
            };
            Set<String> externalFiles;
            if (content != null) {
                FileObject xml = LayerHandle.forProject(project).getLayerFile();
                FileObject parent = xml != null ? xml.getParent() : null;
                // XXX this is not fully accurate since if two ops would both create the same file,
                // really the second one would automatically generate a uniquified name... but close enough!
                externalFiles = Collections.singleton(LayerUtil.findGeneratedName(parent, layerPath));
            } else {
                externalFiles = Collections.emptySet();
            }
            FileSystem layer = cmf.getLayerHandle().layer(false);
            if (layer != null && layer.findResource(layerPath) != null) {
                layerOp = new Operation() {
                    @Override
                    public void run() throws IOException {
                        throw new IOException("cannot overwrite " + layerPath); // NOI18N
                    }

                    @Override
                    public String[] getModifiedPaths() {
                        return new String[0];
                    }

                    @Override
                    public String[] getCreatedPaths() {
                        return new String[0];
                    }

                    @Override
                    public String[] getInvalidPaths() {
                        // #85138: make sure we do not overwrite an existing entry.
                        return new String[]{layerPath};
                    }
                };
            } else {
                layerOp = new LayerModifications(project, op, externalFiles, cmf);
            }
            addPaths(layerOp);
            if (localizedDisplayName != null) {
                this.createBundleKey = new BundleKey(getProject(), locBundleKey, localizedDisplayName);
                addPaths(this.createBundleKey);
            } else {
                createBundleKey = null;
            }
        }

        @Override
        public void run() throws IOException {
            layerOp.run();
            if (createBundleKey != null) {
                createBundleKey.run();
            }
        }
    }

    /**
     * Creates a new arbitrary <em>&lt;attr&gt;</em> element.
     *
     * @param parentPath path to a <em>file</em> or a <em>folder</em> in a
     * project's manifest. It <strong>must</strong> exist.
     * @param attrName value of the name attribute of the <em>&lt;attr&gt;</em>
     * element.
     * @param attrValue value of the attribute (may specially be a string
     * prefixed with "newvalue:", "bundlevalue:" or "methodvalue:")
     * @return see {@link Operation}
     */
    public Operation createLayerAttribute(final String parentPath,
            final String attrName, final Object attrValue) {
        return layerModifications(new LayerOperation() {
            @Override
            public void run(FileSystem layer) throws IOException {
                FileObject f = layer.findResource(parentPath);
                if (f == null) {
                    // XXX sometimes this happens when it should not, during unit tests... why?
                    /*
                     try {
                     // For debugging:
                     getLayerHandle().save();
                     } catch (IOException e) {
                     e.printStackTrace();
                     }
                     */
                    throw new IOException(parentPath);
                }
                f.setAttribute(attrName, attrValue);
            }
        }, Collections.<String>emptySet());
    }

    /**
     * Order a new entry in a project manifest between two others.
     *
     * @param layerPath folder path in a project's manifest. (e.g.
     * <em>Loaders/text/x-java/Actions</em>).
     * @param precedingItemName item to be before <em>newItemName</em> (may be
     * null)
     * @param newItemName the new item (must already exist!)
     * @param followingItemName item to be after <em>newItemName</em> (may be
     * null)
     */
    public Operation orderLayerEntry(final String layerPath, final String precedingItemName, final String newItemName,
            final String followingItemName) {
        return layerModifications(new LayerOperation() {
            @Override
            public void run(FileSystem layer) throws IOException {
                FileObject f = layer.findResource(layerPath);
                if (f == null) {
                    throw new IOException("No such folder " + layerPath);
                }
                FileObject merged = project.getLookup().lookup(NbModuleProvider.class).getEffectiveSystemFilesystem().findResource(layerPath);
                assert merged != null : layerPath;
                Integer beforePos = getPosition(merged, precedingItemName);
                Integer afterPos = getPosition(merged, followingItemName);
                if (beforePos != null && afterPos != null) {
                    // won't work well if afterPos == beforePos + 1, but oh well
                    f.getFileObject(newItemName).setAttribute("position", (beforePos + afterPos) / 2); // NOI18N
                } else if (beforePos != null) {
                    f.getFileObject(newItemName).setAttribute("position", beforePos + 100); // NOI18N
                } else if (afterPos != null) {
                    f.getFileObject(newItemName).setAttribute("position", afterPos - 100); // NOI18N
                } else {
                    // Fallback esp. for old platforms.
                    if (precedingItemName != null) {
                        f.setAttribute(precedingItemName + '/' + newItemName, true);
                    }
                    if (followingItemName != null) {
                        f.setAttribute(newItemName + '/' + followingItemName, true);
                    }
                }
            }

            private Integer getPosition(FileObject folder, String name) {
                if (name == null) {
                    return null;
                }
                FileObject f = folder.getFileObject(name);
                if (f == null) {
                    return null;
                }
                Object pos = f.getAttribute("position"); // NOI18N
                // ignore floats for now...
                return pos instanceof Integer ? (Integer) pos : null;
            }
        }, Collections.<String>emptySet());
    }

    /**
     * Provides {@link Operation} that will create a {@code package-info.java}
     * if needed and optionally add some annotations to the package. Each
     * annotation is of the form FQN -> {key -> val}.
     */
    public Operation packageInfo(String packageName, Map<String, ? extends Map<String, ?>> annotations) {
        return new PackageInfo(project, packageName, annotations);
    }

    private static class PackageInfo extends AbstractOperation {

        private final Map<String, ? extends Map<String, ?>> annotations;
        private final String srcRootPath, srcRelPath;

        PackageInfo(Project project, String packageName, Map<String, ? extends Map<String, ?>> annotations) {
            super(project);
            this.annotations = annotations;
            srcRootPath = getModuleInfo().getSourceDirectoryPath();
            srcRelPath = packageName.replace('.', '/') + "/package-info.java"; // NOI18N
            addCreatedOrModifiedPath(srcRootPath + '/' + srcRelPath, true);
        }

        public @Override
        void run() throws IOException {
            final FileObject top = getProject().getProjectDirectory();
            top.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public @Override
                void run() throws IOException {
                    final FileObject srcRoot = FileUtil.createFolder(top, srcRootPath);
                    final FileObject srcFile = srcRoot.getFileObject(srcRelPath);
                    final JavaSource source;
                    if (srcFile != null) {
                        source = JavaSource.forFileObject(srcFile);
                        if (source == null) {
                            throw new IOException("unparsable: " + srcFile);
                        }
                    } else {
                        source = JavaSource.create(ClasspathInfo.create(srcRoot));
                    }
                    try {
                        source.runWhenScanFinished(new Task<CompilationController>() { // #194569
                            @Override
                            public void run(CompilationController parameter) throws Exception {
                                source.runModificationTask(new Task<WorkingCopy>() {
                                    public @Override
                                    void run(WorkingCopy wc) throws Exception {
                                        wc.toPhase(JavaSource.Phase.RESOLVED);
                                        TreeMaker make = wc.getTreeMaker();
                                        List<AnnotationTree> anns = new ArrayList<AnnotationTree>();
                                        for (Map.Entry<String, ? extends Map<String, ?>> ann : annotations.entrySet()) {
                                            TypeElement annType = wc.getElements().getTypeElement(ann.getKey());
                                            if (annType == null) {
                                                throw new IOException("No annotation " + ann.getKey() + " in " + wc.getClasspathInfo());
                                            }
                                            ExpressionTree annotationTypeTree = make.QualIdent(annType);
                                            List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                                            for (Map.Entry<String, ?> attr : ann.getValue().entrySet()) {
                                                Object value = attr.getValue();
                                                ExpressionTree expression;
                                                if (value instanceof Object[]) {
                                                    List<ExpressionTree> expressions = new ArrayList<ExpressionTree>();
                                                    for (Object element : (Object[]) value) {
                                                        expressions.add(make.Literal(element));
                                                    }
                                                    expression = make.NewArray(null, Collections.<ExpressionTree>emptyList(), expressions);
                                                } else {
                                                    expression = make.Literal(value);
                                                }
                                                arguments.add(make.Assignment(make.Identifier(attr.getKey()), expression));
                                            }
                                            anns.add(make.Annotation(annotationTypeTree, arguments));
                                        }
                                        CompilationUnitTree old, nue;
                                        if (srcFile != null) {
                                            old = nue = wc.getCompilationUnit();
                                            for (AnnotationTree ann : anns) {
                                                nue = make.addPackageAnnotation(nue, ann);
                                            }
                                        } else {
                                            old = null;
                                            nue = make.CompilationUnit(anns, srcRoot, srcRelPath, Collections.<ImportTree>emptyList(), Collections.<Tree>emptyList());
                                        }
                                        nue = GeneratorUtilities.get(wc).importFQNs(nue);
                                        wc.rewrite(old, nue);
                                    }
                                }).commit();
                            }
                        }, false).get();
                    } catch (IOException x) {
                        throw x;
                    } catch (InterruptedException x) {
                        throw new IOException(x);
                    } catch (ExecutionException x) {
                        throw new IOException(x);
                    }
                    FileObject srcFile2 = srcFile != null ? srcFile : srcRoot.getFileObject(srcRelPath);
                    if (srcFile2 == null) {
                        throw new IOException("#204274: no package-info.java created?");
                    }
                    SaveCookie sc = DataObject.find(srcFile2).getLookup().lookup(SaveCookie.class);
                    if (sc != null) {
                        sc.save();
                    }
                }
            });
        }
    }

    private static void copyByteAfterByte(FileObject content, FileObject target) throws IOException {
        OutputStream os = target.getOutputStream();
        try {
            InputStream is = content.getInputStream();
            try {
                FileUtil.copy(is, os);
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
    }

    private static void copyAndSubstituteTokens(FileObject content, FileObject target, Map<String, ? extends Object> tokens) throws IOException {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        ScriptEngineManager scriptEngineManager = Scripting.createManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName("freemarker");
        assert engine != null : "#163878: " + scriptEngineManager.getEngineFactories() + " lacks freemarker using "
                + l + " though lookup has "
                + Lookup.getDefault().lookupAll(ScriptEngineFactory.class);
        Map<String, Object> bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        String basename = target.getName();
        for (CreateFromTemplateAttributesProvider provider : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
            Map<String, ?> map = provider.attributesFor(DataObject.find(content), DataFolder.findFolder(target.getParent()), basename);
            if (map != null) {
                bindings.putAll(map);
            }
        }
        bindings.put("name", basename.replaceFirst("\\.[^./]+$", "")); // NOI18N
        bindings.put("user", System.getProperty("user.name")); // NOI18N
        Date d = new Date();
        bindings.put("date", DateFormat.getDateInstance().format(d)); // NOI18N
        bindings.put("time", DateFormat.getTimeInstance().format(d)); // NOI18N
        bindings.put("nameAndExt", target.getNameExt()); // NOI18N
        bindings.putAll(tokens);
        Charset targetEnc = FileEncodingQuery.getEncoding(target);
        Charset sourceEnc = FileEncodingQuery.getEncoding(content);
        bindings.put("encoding", targetEnc.name());
        Writer w = new OutputStreamWriter(target.getOutputStream(), targetEnc);
        try {
            engine.getContext().setWriter(w);
            engine.getContext().setAttribute(FileObject.class.getName(), content, ScriptContext.ENGINE_SCOPE);
            engine.getContext().setAttribute(ScriptEngine.FILENAME, content.getNameExt(), ScriptContext.ENGINE_SCOPE);
            Reader is = new InputStreamReader(content.getInputStream(), sourceEnc);
            try {
                engine.eval(is);
            } catch (ScriptException x) {
                throw (IOException) new IOException(x.toString()).initCause(x);
            } finally {
                is.close();
            }
        } finally {
            w.close();
        }
    }
}
