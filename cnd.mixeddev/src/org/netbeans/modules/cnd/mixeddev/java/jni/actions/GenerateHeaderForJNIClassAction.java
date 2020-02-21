/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.mixeddev.java.jni.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.Triple;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.cnd.mixeddev.java.jni.ui.JProjectFileChooser;
import org.netbeans.modules.cnd.mixeddev.java.model.jni.JNIClass;
import static org.netbeans.modules.cnd.mixeddev.wizard.Generator.createStub;
import static org.netbeans.modules.cnd.mixeddev.wizard.Generator.getRootHeader;
import static org.netbeans.modules.cnd.mixeddev.wizard.Generator.getRootSource;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MutableObject;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.java.api.common.project.ProjectPlatformProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 */
public class GenerateHeaderForJNIClassAction extends AbstractJNIAction {
    
    public GenerateHeaderForJNIClassAction(Lookup context) {
        super(context);
        putValue(NAME, NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.generate_header_for_jni_class")); // NOI18N
    }

    @Override
    protected boolean isEnabledAtPosition(Document doc, int caret) {
        return JNISupport.isJNIClass(doc, caret);
    }

    @Override
    protected void actionPerformedImpl(Node[] activatedNodes) {
        final Triple<DataObject, Document, Integer> context = extractContext(activatedNodes);
        if (context != null) {
            final FileObject javaFile = context.first.getPrimaryFile();
            final Document doc = context.second;
            final int caret = context.third;
            JNIClass cls = JNISupport.getJNIClass(doc, caret);
            assert cls != null;
            final String headerFileName = CharSequenceUtils.concatenate(cls.getClassInfo().getName(), ".h").toString(); // NOI18N
            final List<NativeProject> nativeProjects = MixedDevUtils.toList(MixedDevUtils.findNativeProjects());
            FileObject toJump = null;
            for (NativeProject nativeProject : nativeProjects) {
                NativeFileSearch fileSearch = NativeProjectSupport.getNativeFileSearch(nativeProject);
                Collection<FSPath> searchResult = fileSearch.searchFile(
                    nativeProject, 
                    headerFileName
                );
                if (searchResult != null && !searchResult.isEmpty()) {
                    if (searchResult.size() == 1) {
                        FSPath foundHeaderPath =  searchResult.iterator().next();
                        toJump = generateHeader(javaFile, foundHeaderPath.getPath());
                    } else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.header_files_ambiguity", nativeProject.getProjectDisplayName(), headerFileName), // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE
                        ));
                    }

                }
            }
            if (toJump == null) {
                Project chosenProject = null;
                FileObject chosenFolder = null;
                try {
                    final MutableObject<Project> chosenProjectHolder = new MutableObject<>();
                    final MutableObject<FileObject> chosenFolderHolder = new MutableObject<>();
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            JProjectFileChooser chooser = new JProjectFileChooser(
                                WindowManager.getDefault().getMainWindow(), 
                                true, 
                                nativeProjects
                            );
                            chooser.setVisible(true);
                            chosenProjectHolder.value = chooser.getChosenProject();
                            chosenFolderHolder.value = chooser.getChosenFile();
                        }
                        
                    });
                    chosenProject = chosenProjectHolder.value;
                    chosenFolder = chosenFolderHolder.value;
                } catch (InterruptedException | InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (chosenProject != null && chosenFolder != null) {
                    String headerPath = chosenFolder.getPath() + File.separator + headerFileName;
                    FileObject header = generateHeader(javaFile, headerPath);
                    if (header != null) {
                        String sourcePath = chosenFolder.getPath() + File.separator + header.getName() + ".cpp"; // NOI18N
                        StringBuilder sourceContent = printSourceHeader(javaFile, header, new StringBuilder());
                        FileObject source = generateSource(javaFile, sourcePath, sourceContent.toString());
                        registerFilesInProject(chosenProject, Pair.of(header, true), Pair.of(source, false));
                        waitParse(chosenProject);
                        try {
                            createStub(header, source, sourceContent);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        waitParse(chosenProject);
                        toJump = source;
                    }
                }
            }
            if (toJump != null) {
                CsmUtilities.openSource(toJump, 0);
            }
        }
    }
    
    private void registerFilesInProject(Project proj, Pair<FileObject, Boolean> ... files) {
        if (files != null && files.length > 0) {
            ConfigurationDescriptorProvider pdp = proj.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.getConfigurationDescriptor();
            if (pdp.gotDescriptor()) {
                final MakeConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
                for (Pair<FileObject, Boolean> pair : files) {
                    FileObject fileObject = pair.first();
                    boolean header = pair.second();
                    if (fileObject != null && fileObject.isValid()) {
                        Folder logicFolder = header ? getRootHeader(configurationDescriptor) : getRootSource(configurationDescriptor);
                        Item item = ItemFactory.getDefault().createInFileSystem(configurationDescriptor.getBaseDirFileSystem(), fileObject.getPath());
                        logicFolder.addItemAction(item);
                    }
                }
                configurationDescriptor.save();
            }
        }
    }
    
    private void waitParse(Project proj) {
        final CsmModel model = CsmModelAccessor.getModel();
        final CsmProject project = model.getProject(proj);
        project.waitParse();
    }
    
    private FileObject generateHeader(FileObject javaFile, String headerPath) {
        ClassPath sourceCP = ClassPath.getClassPath(javaFile, ClassPath.SOURCE);
        ClassPath compileCP = ClassPath.getClassPath(javaFile, ClassPath.COMPILE);
        FileObject sr = sourceCP != null ? sourceCP.findOwnerRoot(javaFile) : null;
        Project javaProject = FileOwnerQuery.getOwner(sr);
        ProjectPlatformProvider pp = javaProject.getLookup().lookup(ProjectPlatformProvider.class);
        JavaPlatform jp = pp.getProjectPlatform();
        final FileObject binFO = jp.findTool("javah"); // NOI18N
        return JNISupport.generateJNIHeader(binFO, sr, javaFile, headerPath, sourceCP, compileCP);
    }
    
    private FileObject generateSource(FileObject javaFile, String sourcePath, String content) {
        final File file = new File(sourcePath);
        try (Writer w = new FileWriter(file)) {
            w.write(content);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return FileUtil.toFileObject(file);
    }
    
    private StringBuilder printSourceHeader(FileObject javaFile, FileObject header, StringBuilder sb) {
        sb.append("// Native methods implementation of\n// ").append(javaFile.getPath()).append("\n\n"); //NOI18N
        sb.append("#include \"").append(header.getNameExt()).append("\"\n"); //NOI18N
        return sb;
    }
    
    private Pair<CsmScope, CsmScope> getCppFunctionScopes(CsmOffsetable symbol) {
        CsmFunction declaration = null;
        CsmFunctionDefinition definition = null;
        if (CsmKindUtilities.isFunctionDeclaration(symbol)) {
            declaration = (CsmFunction) symbol;
            definition = declaration.getDefinition();
        } else if (CsmKindUtilities.isFunctionDefinition(symbol)) {
            definition = (CsmFunctionDefinition) symbol;
            declaration = definition.getDeclaration();
        }
        CsmScope declScope = null;
        if (declaration != null) {
            declScope = declaration.getScope();
            if (!CsmKindUtilities.isNamespaceDefinition(declScope)) {
                declScope = declaration.getContainingFile();
            }
        }
        CsmScope defScope = null;
        if (definition != null) {
            defScope = definition.getScope();
            if (!CsmKindUtilities.isNamespaceDefinition(defScope)) {
                defScope = definition.getContainingFile();
            }
        }
        if (declScope != null || defScope != null) {
            return Pair.of(declScope, defScope);
        }
        return null;
    }
}
