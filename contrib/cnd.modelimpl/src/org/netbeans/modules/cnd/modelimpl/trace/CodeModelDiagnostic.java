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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.netbeans.modules.cnd.modelimpl.content.file.ReferencesIndex;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputWriter;
import static org.netbeans.modules.cnd.modelimpl.trace.Bundle.*;

/**
 *
 */

public final class CodeModelDiagnostic {
    public static void dumpProjectContainers(PrintStream ps, CsmProject prj, boolean dumpFiles) {
        ProjectBase.dumpProjectContainers(ps, prj, dumpFiles);
    }

    public static void dumpFileContainer(CsmProject project, OutputWriter out) {
        ProjectBase.dumpFileContainer(project, out);
    }

    public static void dumpProjectGrapthContainer(CsmProject project, OutputWriter out) {
        ProjectBase.dumpProjectGrapthContainer(project, out);
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1000)
    public final static class StandAloneProviderTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"StandAloneProviderTrace.displayName=Standalone Files Information"})
        @Override
        public String getDisplayName() {
            return StandAloneProviderTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====CsmStandaloneFileProviders info:%n");// NOI18N
            for (CsmStandaloneFileProvider sap : Lookup.getDefault().lookupAll(CsmStandaloneFileProvider.class)) {
                if (sap instanceof CsmStandaloneFileProviderImpl) {
                    ((CsmStandaloneFileProviderImpl) sap).dumpInfo(printOut);
                } else {
                    printOut.printf("UNKNOWN FOR ME [%s] %s%n", sap.getClass().getName(), sap.toString());// NOI18N
                }
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1100)
    public final static class FileTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"FileTrace.displayName=General File Information"})
        @Override
        public String getDisplayName() {
            return FileTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====Files info:%nGlobal ParseCount=%d%n", FileImpl.getLongParseCount());// NOI18N
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    ((FileImpl) csmFile).dumpInfo(printOut);
                } else if (csmFile instanceof FileSnapshot) {
                    ((FileSnapshot) csmFile).dumpInfo(printOut);
                } else {
                    printOut.printf("UNKNOWN FOR ME [%s] %s%n", csmFile.getClass().getName(), csmFile.toString());// NOI18N
                }
            }
            Collection<? extends Document> docs = context.lookupAll(Document.class); 
            for (Document doc : docs) {
                Language language = (Language) doc.getProperty(Language.class);
                printOut.printf("DocLanguage: %s%n", language); // NOI18N
                InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
                if (lexerAttrs != null && language != null) {
                    Filter<?> filter = (Filter<?>) lexerAttrs.getValue(LanguagePath.get(language), CndLexerUtilities.LEXER_FILTER);
                    printOut.printf("\tDocLanguageFilter: %s%n", filter); // NOI18N
                }
            }
            Collection<? extends DataObject> dobs = context.lookupAll(DataObject.class);
            if (!dobs.isEmpty()) {
                boolean foundItemSet = false;
                for(DataObject dob : dobs) {
                    NativeFileItemSet nfis = dob.getLookup().lookup(NativeFileItemSet.class);
                    if (nfis != null) {
                        foundItemSet = true;
                        printOut.printf("NativeFileItemSet has %d elements%n", nfis.getItems().size());// NOI18N
                        int ind = 0;
                        for (NativeFileItem item : nfis.getItems()) {
                            printOut.printf("[%d] NativeFileItem %s of class %s%n", ++ind, item.getAbsolutePath(), item.getClass().getName());// NOI18N
                            NativeProject nativeProject = item.getNativeProject();
                            printOut.printf(" from project %s [%s]%n", nativeProject.getProjectDisplayName(), nativeProject.getProjectRoot());// NOI18N
                            printOut.printf("\tLang=%s Flavor=%s excluded=%s%n", item.getLanguage(), item.getLanguageFlavor(), item.isExcluded());// NOI18N
                            printOut.print("\tUser Include Paths:\n");// NOI18N
                            for (IncludePath path : item.getUserIncludePaths()) {
                                String msg = CndFileUtils.isLocalFileSystem(path.getFileSystem()) ? path.getFSPath().getPath() : path.getFSPath().getURL().toString();
                                FileObject valid = path.getFSPath().getFileObject();
                                if (valid != null && !valid.isValid()) {
                                    valid = null;
                                }
                                printOut.printf("\t\t%s%s%n", msg, valid == null ? "[invalid]" : "");// NOI18N
                            }
                            if (!item.getSystemIncludeHeaders().isEmpty()) {
                                printOut.print("\tSystem pre-included headers:\n");// NOI18N
                                for (FSPath path : item.getSystemIncludeHeaders()) {
                                    String msg = CndFileUtils.isLocalFileSystem(path.getFileSystem()) ? path.getPath() : path.getURL().toString();
                                    FileObject valid = path.getFileObject();
                                    if (valid != null && !valid.isValid()) {
                                        valid = null;
                                    }
                                    printOut.printf("\t\t%s%s%n", msg, valid == null ? "[invalid]" : "");// NOI18N
                                }
                            }
                            if (!item.getIncludeFiles().isEmpty()) {
                                printOut.print("\tUser Include Files:\n");// NOI18N
                                for (FSPath path : item.getIncludeFiles()) {
                                    String msg = CndFileUtils.isLocalFileSystem(path.getFileSystem()) ? path.getPath() : path.getURL().toString();
                                    FileObject valid = path.getFileObject();
                                    if (valid != null && !valid.isValid()) {
                                        valid = null;
                                    }
                                    printOut.printf("\t\t%s%s%n", msg, valid == null ? "[invalid]" : "");// NOI18N
                                }
                            }
                            printOut.print("\tUser Macros:\n");// NOI18N
                            for (String macro : item.getUserMacroDefinitions()) {
                                printOut.printf("\t\t%s%n", macro);// NOI18N
                            }
                            printOut.print("\tSystem Include Paths:\n");// NOI18N
                            for (IncludePath path : item.getSystemIncludePaths()) {
                                String msg = CndFileUtils.isLocalFileSystem(path.getFileSystem()) ? path.getFSPath().getPath() : path.getFSPath().getURL().toString();
                                FileObject valid = path.getFSPath().getFileObject();
                                if (valid != null && !valid.isValid()) {
                                    valid = null;
                                }
                                printOut.printf("\t\t%s%s%n", msg, valid == null ? "[invalid]" : "");// NOI18N
                            }
                            printOut.print("\tSystem Macros:\n");// NOI18N
                            for (String macro : item.getSystemMacroDefinitions()) {
                                printOut.printf("\t\t%s%n", macro);// NOI18N
                            }
                        }
                    }
                }
                if(!foundItemSet) {
                    printOut.printf("no NativeFileItemSet in %s%n", context);// NOI18N
                }
            } else {
                printOut.printf("no file object in lookup%n");// NOI18N
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1200)
    public final static class PPStatesTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"PPStatesTrace.displayName=Preprocessor States"})
        @Override
        public String getDisplayName() {
            return PPStatesTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====Files info:%nGlobal ParseCount=%d%n", FileImpl.getLongParseCount());// NOI18N
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    ((FileImpl) csmFile).dumpPPStates(printOut);
                } else {
                    printOut.printf("UNKNOWN FOR ME [%s] %s%n", csmFile.getClass().getName(), csmFile.toString());// NOI18N
                }
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1250)
    public final static class IncludePPStatesTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"IncludePPStatesTrace.displayName=Included Preprocessor States"})
        @Override
        public String getDisplayName() {
            return IncludePPStatesTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====Files info:%nGlobal ParseCount=%d%n", FileImpl.getLongParseCount());// NOI18N
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    ((FileImpl) csmFile).dumpIncludePPStates(printOut);
                } else {
                    printOut.printf("UNKOWN FOR ME [%s] %s%n", csmFile.getClass().getName(), csmFile.toString());// NOI18N
                }
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1300)
    public final static class ModelProjectsTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"ModelProjectsTrace.displayName=Model Projects"})
        @Override
        public String getDisplayName() {
            return ModelProjectsTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====ModelImpl:%n");// NOI18N
            ModelImpl.instance().dumpInfo(printOut, false);
            printOut.printf("====Libraries:%n"); //NOI18N
            LibraryManager.dumpInfo(printOut, false);
            printOut.printf("====Files count size summary:%n"); //NOI18N
            dumpProjectFilesInfo(printOut);
        }

        private void dumpProjectFilesInfo(PrintWriter printOut) {
            Collection<CsmProject> projects = CsmModelAccessor.getModel().projects();
            for (CsmProject project : projects) {
                dumpProjectFilesInfo(project, printOut, false);
                for (CsmProject lib : project.getLibraries()) {
                    dumpProjectFilesInfo(lib, printOut, false);
                }
            }
        }

        private void dumpProjectFilesInfo(CsmProject project, PrintWriter printOut, boolean printList) {
            Collection<CsmFile> sourceFiles = project.getSourceFiles();
            Collection<CsmFile> headerFiles = project.getHeaderFiles();
            printOut.printf("%s%n", project.getDisplayName());// NOI18N
            printOut.printf("   %,d source files; %,d header files; %,d total files%n", // NOI18N
                    sourceFiles.size(), headerFiles.size(), sourceFiles.size() + headerFiles.size());
            long totalSize = 0;
            long maxSize = 0;
            for (CsmFile file : sourceFiles) {
                if (printList) {
                    printOut.printf("\t%s%n", file.getAbsolutePath()); // NOI18N
                }
                FileObject fo = file.getFileObject();
                if (fo != null && fo.isValid()) {
                    totalSize += fo.getSize();
                    maxSize = Math.max(maxSize, fo.getSize());
                }
            }
            for (CsmFile file : headerFiles) {
                if (printList) {
                    printOut.printf("\t%s%n", file.getAbsolutePath()); // NOI18N
                }
                FileObject fo = file.getFileObject();
                if (fo != null && fo.isValid()) {
                    totalSize += fo.getSize();
                    maxSize = Math.max(maxSize, fo.getSize());
                }
            }
            printOut.printf("   total files size: %,d KBytes;  max file size: %,d KBytes%n", kilobytes(totalSize), kilobytes(maxSize)); // NOI18N
        }
        private static long kilobytes(long num) {
            return ((num % 1024) < 512) ? num/1024 : num/1024+1;
        }

    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1350)
    public final static class ModelProjectsContainers implements CndDiagnosticProvider {

        @NbBundle.Messages({"ModelProjectsContainers.displayName=Model Projects File Containers"})
        @Override
        public String getDisplayName() {
            return ModelProjectsContainers_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====ModelImpl:%n");// NOI18N
            ModelImpl.instance().dumpInfo(printOut, true);
            printOut.printf("====Libraries:%n"); //NOI18N
            LibraryManager.dumpInfo(printOut, true);
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1325)
    public final static class ModelProjectsIndex implements CndDiagnosticProvider {

        @NbBundle.Messages({"ModelProjectsIndex.displayName=Model Projects Index"})
        @Override
        public String getDisplayName() {
            return ModelProjectsIndex_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====Model Projects Index:%n");// NOI18N
            ReferencesIndex.dumpInfo(printOut);
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1375)
    public final static class ModelFileIndex implements CndDiagnosticProvider {

        @NbBundle.Messages({"ModelFileIndex.displayName=File References Index"})
        @Override
        public String getDisplayName() {
            return ModelFileIndex_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====File Indices:%n");// NOI18N
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    ((FileImpl) csmFile).dumpIndex(printOut);
                } else if (csmFile instanceof FileSnapshot) {
                    ((FileSnapshot) csmFile).dumpIndex(printOut);
                } else {
                    printOut.printf("UNKNOWN FOR ME [%s] %s%n", csmFile.getClass().getName(), csmFile.toString());// NOI18N
                }
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1400)
    public final static class FileImplModelTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"FileImplModelTrace.displayName=File Code Model"})
        @Override
        public String getDisplayName() {
            return FileImplModelTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) throws IOException {
            CsmCacheManager.enter();
            try {
                Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
                for (CsmFile csmFile : allFiles) {
                    new CsmTracer(printOut).dumpModel(csmFile);
                }
            } finally {
                CsmCacheManager.leave();
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1400)
    public final static class FileImplASTTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"FileImplASTTrace.displayName=File AST"})
        @Override
        public String getDisplayName() {
            return FileImplASTTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if(csmFile instanceof FileImpl) {
                    ASTFrameEx frame = new ASTFrameEx(csmFile.getName().toString(), ((FileImpl) csmFile).debugParse());
                    frame.setVisible(true);
                }
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1500)
    public final static class ProjectDeclarationsTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"ProjectDeclarationsTrace.displayName=Project Declaration Containers (Huge size)"})
        @Override
        public String getDisplayName() {
            return ProjectDeclarationsTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) throws IOException{
            Collection<CsmProject> projects = new ArrayList<CsmProject>(context.lookupAll(CsmProject.class));
            if (projects.isEmpty()) {
                CsmFile file = context.lookup(CsmFile.class);
                if (file != null) {
                    CsmProject project = file.getProject();
                    if (project instanceof ProjectBase) {
                        projects.add(project);
                    }
                }
            }
            PrintStream ps = CsmTracer.toPrintStream(printOut);
            for (CsmProject prj : projects) {
                if (prj instanceof ProjectBase) {
                    dumpProjectContainers(ps, prj, false);
                }
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1600)
    public final static class ModelTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"ModelTrace.displayName=Project Code Model (Huge size)"})
        @Override
        public String getDisplayName() {
            return ModelTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) throws IOException {
            Collection<CsmProject> projects = new ArrayList<CsmProject>(context.lookupAll(CsmProject.class));
            if (projects.isEmpty()) {
                CsmFile file = context.lookup(CsmFile.class);
                if (file != null) {
                    CsmProject project = file.getProject();
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
            CsmCacheManager.enter();
            try {
                for (CsmProject prj : projects) {
                    new CsmTracer(printOut).dumpModel(prj);
                }
            } finally {
                CsmCacheManager.leave();
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1600)
    public final static class ProjectReferencesTrace implements CndDiagnosticProvider {

        @NbBundle.Messages({"ProjectReferencesTrace.displayName=Project References"})
        @Override
        public String getDisplayName() {
            return ProjectReferencesTrace_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<CsmProject> projects = new ArrayList<CsmProject>(context.lookupAll(CsmProject.class));
            if (projects.isEmpty()) {
                CsmFile file = context.lookup(CsmFile.class);
                if (file != null) {
                    CsmProject project = file.getProject();
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
            printOut.println("References:"); // NOI18N
            for (CsmProject prj : projects) {
                printOut.print(prj.getName() + " : "); // NOI18N
                int refsNumber = 0;
                for (CsmFile file : prj.getAllFiles()) {
                    refsNumber += ((FileImpl)file).getReferences().size();
                }
                printOut.println(refsNumber);
            }
        }
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 1400)
    public final static class OffsetToPositionProvider implements CndDiagnosticProvider {

        @NbBundle.Messages({"OffsetToPositionProvider.displayName=Offset to Position"})
        @Override
        public String getDisplayName() {
            return OffsetToPositionProvider_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<? extends CsmFile> allFiles = context.lookupAll(CsmFile.class);
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    OffsetToPositionFrame frame = new OffsetToPositionFrame((FileImpl) csmFile);
                    frame.setVisible(true);
                }
            }
        }
    }
}
