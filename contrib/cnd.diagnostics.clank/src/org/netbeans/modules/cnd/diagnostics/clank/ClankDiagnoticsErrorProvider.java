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
package org.netbeans.modules.cnd.diagnostics.clank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.clang.frontend.InputKind;
import org.clang.frontend.LangStandard;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.clang.tools.services.ClankDiagnosticResponse;
import org.clang.tools.services.ClankDiagnosticServices;
import org.clang.tools.services.ClankRunDiagnosticsSettings;
//import org.clang.tools.services.checkers.api.ClankCLOptionsProvider;
import org.clang.tools.services.checkers.api.ClankChecker;
import org.clang.tools.services.checkers.api.ClankCheckersProvider;
import org.clang.tools.services.spi.ClankFileSystemProvider;
import org.clang.tools.services.spi.ClankMemoryBufferProvider;
import org.clang.tools.services.support.DataBaseEntryBuilder;
import org.llvm.support.MemoryBuffer;
import org.netbeans.modules.cnd.analysis.api.AbstractCustomizerProvider;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 */
@ServiceProviders({
    //@ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1400),
    @ServiceProvider(service = CsmErrorProvider.class, position = 3000)
    ,   
    @ServiceProvider(service = CodeAuditProvider.class, position = 3000)
})
public class ClankDiagnoticsErrorProvider extends CsmErrorProvider implements CodeAuditProvider, AbstractCustomizerProvider {
    //, AbstractCustomizerProvider { 

    private static final Logger LOG = Logger.getLogger("cnd.diagnostics.clank.support"); //NOI18N
    private Collection<CodeAudit> audits;
    public static final String NAME = "Clank_Diagnostics"; //NOI18N
    private final AuditPreferences myPreferences;

    public static CsmErrorProvider getInstance() {
        for (CsmErrorProvider provider : Lookup.getDefault().lookupAll(CsmErrorProvider.class)) {
            if (NAME.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }

    public ClankDiagnoticsErrorProvider() {
        myPreferences = new AuditPreferences(AuditPreferences.AUDIT_PREFERENCES_ROOT.node(NAME));
    }

    /*package*/ ClankDiagnoticsErrorProvider(Preferences preferences) {
        try {
            if (preferences.nodeExists(NAME)) {
                preferences = preferences.node(NAME);
            }
        } catch (BackingStoreException ex) {
        }
        if (preferences.absolutePath().endsWith("/" + NAME)) { //NOI18N
            myPreferences = new AuditPreferences(preferences);
        } else {
            myPreferences = new AuditPreferences(preferences.node(NAME));
        }
    }

    @Override
    public JComponent createComponent(Preferences context) {
        return new ClankCLArsPanel(context);
    }

    @Override
    protected boolean validate(Request request) {
        CsmFile file = request.getFile();
        if (file == null) {
            return false;
        }
        //if (file.isHeaderFile()) {
        //    return false;
        //}
        for (CodeAudit audit : getAudits()) {
            if (audit.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasHintControlPanel() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_NAME"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ClankDiagnoticsErrorProvider.class, "Clank_DESCRIPTION"); //NOI18N
    }

    @Override
    public String getMimeType() {
        return MIMENames.SOURCES_MIME_TYPE;
    }

    @Override
    public boolean isSupportedEvent(EditorEvent kind) {
        return kind == EditorEvent.FileBased;
    }

    @Override
    protected void doGetErrors(Request request, final Response response) {
        ClankRunDiagnosticsSettings settings = new ClankRunDiagnosticsSettings();
        final CsmFile file = request.getFile();
        ClankCompilationDataBase.Entry entry = createEntry(file, true, settings);
        if (request.isCancelled()) {
            return;
        };
        settings.response = new ClankDiagnosticResponse() {
            @Override
            public void addError(final ClankDiagnosticInfo errorInfo) {
                response.addError(new ClankCsmErrorInfo(file, errorInfo));
            }

            @Override
            public void done() {

            }
        };
        for (String arg : ClankCLOptionsDeafaultImpl.getArgs()) {
            if (myPreferences.getPreferences().getBoolean(arg, true)) {
                settings.clArgs.add(arg);
            }
        }
        settings.showAllWarning = false;
        settings.checkers.clear();
        //and fill in
        for (CodeAudit audit :  audits) {
            if (audit.isEnabled()) {
                settings.checkers.add(((ClankDiagnosticAudit)audit).clankChecker);
            }
        }
        try {
            ClankMemoryBufferProvider provider = Lookup.getDefault().lookup(ClankMemoryBufferProvider.class);
            Map<String, MemoryBuffer> remappedBuffers = provider.getRemappedBuffers();            
            ClankDiagnosticServices.verify(entry, settings, remappedBuffers);
        } catch (Throwable ex) {
            //catch anything

        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
    private static ClankCompilationDataBase.Entry createEntry(CsmFile file, boolean useURL, ClankRunDiagnosticsSettings settings) {
        NativeFileItem nfi = CsmFileInfoQuery.getDefault().getNativeFileItem(file);
        CharSequence mainFile = nfi != null ? (useURL ? 
                CndFileSystemProvider.toUrl(FSPath.toFSPath(nfi.getFileObject())) : 
                nfi.getAbsolutePath()) : 
                (useURL ? 
                CndFileSystemProvider.toUrl(FSPath.toFSPath(file.getFileObject())) : 
                file.getAbsolutePath());        
        DataBaseEntryBuilder builder = new DataBaseEntryBuilder(mainFile, null);
        boolean isLangSet = false;
        if (nfi != null) {
            Lookup.Provider project = nfi.getNativeProject().getProject();
            boolean addNoException = true;
            if (project != null) {
                //we need to check for example if we have -fno-exceptions - and if not - add 
                //-fcxx-exceptions and -fexceptions
                //as g++ and clang defaults are different
                //shouldn't we create some kine of map
                //and just read from the file (XML for example) instead of hard-coding in the source code
                ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                if (provider != null && provider.gotDescriptor()) {
                    MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor();
                    Item item = configurationDescriptor.findItemByFileObject(file.getFileObject());
                    if (item != null) {
                        final MakeConfiguration conf = configurationDescriptor.getActiveConfiguration();
                        ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);
                        PredefinedToolKind tool = itemConfiguration.getTool();
                        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                                AbstractCompiler compiler = itemConfiguration.isCompilerToolConfiguration() ? 
                                (AbstractCompiler) compilerSet.getTool(tool) : 
                                (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
                        CCCompilerConfiguration configuration = null; 
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            configuration = itemConfiguration.getCCCompilerConfiguration();
                        } else if (item.hasHeaderOrSourceExtension(false, false)) {
                            configuration = conf.getCCCompilerConfiguration();
                        }
                        if (configuration != null && configuration.getCppStandard().getDefault() == 
                            configuration.getCppStandard().getValue()) { //isDefault, check Code eassiatcne
                            int externalCppStandard = itemConfiguration.isCompilerToolConfiguration() ? 
                                    (nfi.getLanguage() == NativeFileItem.Language.CPP ? 
                                    NativeProjectSupport.getDefaultCppStandard().toExternal()  :
                                    NativeProjectSupport.getDefaultCStandard().toExternal()): 
                                    (item.hasHeaderOrSourceExtension(false, false) ? 
                                        NativeProjectSupport.getDefaultHeaderStandard().toExternal() : 
                                        NativeFileItem.LanguageFlavor.DEFAULT.toExternal()
                                    );
                            int cppStandard = 0;
                            if (externalCppStandard == NativeFileItem.LanguageFlavor.DEFAULT.toExternal()) {
                                cppStandard = CCCompilerConfiguration.STANDARD_DEFAULT;
                            } else if (externalCppStandard == NativeFileItem.LanguageFlavor.CPP98.toExternal()) {
                                cppStandard = CCCompilerConfiguration.STANDARD_CPP98;
                            } else if (externalCppStandard == NativeFileItem.LanguageFlavor.CPP11.toExternal()) {
                                cppStandard = CCCompilerConfiguration.STANDARD_CPP11;
                            } else if (externalCppStandard == NativeFileItem.LanguageFlavor.CPP14.toExternal()) {
                                cppStandard = CCCompilerConfiguration.STANDARD_CPP14;
                            } else if (externalCppStandard == NativeFileItem.LanguageFlavor.CPP17.toExternal()) {
                                cppStandard = CCCompilerConfiguration.STANDARD_CPP17;
                            } else if (externalCppStandard == NativeFileItem.LanguageFlavor.UNKNOWN.toExternal()) {
                                cppStandard = CCCompilerConfiguration.STANDARD_INHERITED;
                            }
                            String cppStandardOptions = compiler.getCppStandardOptions(cppStandard); //value from compiler configration
                            settings.clArgs.add(cppStandardOptions);
                            if (cppStandardOptions.contains("c++")) { //NOI18N
                                settings.clArgs.add("-stdlib=libstdc++");//NOI18N
                            }
                        } else if (configuration != null){
                            String cppStandardOptions =  compiler.getCppStandardOptions(configuration.getCppStandard().getValue());
                            settings.clArgs.add(cppStandardOptions);
                            if (cppStandardOptions.contains("c++")) {//NOI18N
                                settings.clArgs.add("-stdlib=libstdc++");//NOI18N
                            }
                        }
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            String options = itemConfiguration.getCompilerConfiguration().getAllOptions(compiler);
                            if (compilerSet.getCompilerFlavor().isGnuCompiler()) {
                                if (options.contains("-fno-exceptions")) {//NOI18N
                                    addNoException = false;
                                }
                            } else if (compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                                if (options.contains("-noex") || options.contains("-fno-exceptions")) {//NOI18N
                                    addNoException = false;
                                }
                            }
                        }
                    }
                }
            }
            if (addNoException) {
                settings.clArgs.add("-fcxx-exceptions");//NOI18N
                settings.clArgs.add("-fexceptions");//NOI18N
            }
            if (!isLangSet) {
                builder.setLang(getLang(nfi)).setLangStd(getLangStd(nfi));
            }

            // -I or -F
            for (org.netbeans.modules.cnd.api.project.IncludePath incPath : nfi.getUserIncludePaths()) {
                FileObject fileObject = incPath.getFSPath().getFileObject();
                if (fileObject != null && fileObject.isFolder()) {
                    CharSequence path = useURL ? incPath.getFSPath().getURL() : incPath.getFSPath().getPath();
                    builder.addUserIncludePath(path, incPath.isFramework(), incPath.ignoreSysRoot());
                }
            }
            // -isystem
            for (org.netbeans.modules.cnd.api.project.IncludePath incPath : nfi.getSystemIncludePaths()) {
                FileObject fileObject = incPath.getFSPath().getFileObject();
                if (fileObject != null && fileObject.isFolder()) {
                    CharSequence path = useURL ? incPath.getFSPath().getURL() : incPath.getFSPath().getPath();
                    builder.addPredefinedSystemIncludePath(path, incPath.isFramework(), incPath.ignoreSysRoot());
                }
            }
            // system pre-included headers
            for (FSPath fSPath : nfi.getSystemIncludeHeaders()) {
                FileObject fileObject = fSPath.getFileObject();
                if (fileObject != null && fileObject.isData()) {
                    String path = useURL ? fSPath.getURL().toString() : fSPath.getPath();
                    builder.addIncFile(path);
                }
            }

            // handle -include
            for (FSPath fSPath : nfi.getIncludeFiles()) {
                FileObject fileObject = fSPath.getFileObject();
                if (fileObject != null && fileObject.isData()) {
                    String path = useURL ? fSPath.getURL().toString() : fSPath.getPath();
                    builder.addIncFile(path);
                }
            }

            // -D
            for (String macro : nfi.getSystemMacroDefinitions()) {
                builder.addPredefinedSystemMacroDef(macro);
            }
            for (String macro : nfi.getUserMacroDefinitions()) {
                builder.addUserMacroDef(macro);
            }            
        } else {
            //setLang see bz#270971
            switch (file.getFileType()) {
                case SOURCE_CPP_FILE:
                    builder.setLang(InputKind.IK_CXX);
                    break;
                case SOURCE_C_FILE:
                    builder.setLang(InputKind.IK_C);
                    break;
                default:
                    ///set C as default???
                    builder.setLang(InputKind.IK_C);
            }
            ExecutionEnvironment execEnv = FileSystemProvider.getExecutionEnvironment(file.getFileObject());
            //get from default toolcain
            CompilerSetManager csm = CompilerSetManager.get(execEnv);
            CompilerSet defaultCompilerSet = csm.getDefaultCompilerSet();
            List<Tool> tools = defaultCompilerSet.getTools();
            for (Tool tool : tools) {
                if (tool instanceof AbstractCompiler) {
                    if (tool.getKind() == PredefinedToolKind.CCompiler || tool.getKind() == PredefinedToolKind.CCCompiler) {
                        AbstractCompiler abstractCompiler = (AbstractCompiler) tool;
                        List<String> systemIncludeDirectories = abstractCompiler.getSystemIncludeDirectories();
                        for (String systemIncludeDirectory : systemIncludeDirectories) {
                            builder.addPredefinedSystemIncludePath(systemIncludeDirectory, false, false);
                        }
                        List<String> systemPreprocessorSymbols = abstractCompiler.getSystemPreprocessorSymbols();
                        for (String systemPreprocessorSymbol : systemPreprocessorSymbols) {
                            builder.addPredefinedSystemMacroDef(systemPreprocessorSymbol);
                        }
                    }
                }
            }            
//            tools.get(0).
        }

        builder.setFileSystem(ClankFileSystemProvider.getDefault().getFileSystem());
        try {
            if (CndFileSystemProvider.isRemote(file.getFileObject().getFileSystem())) {
                CharSequence prefix = CndFileSystemProvider.toUrl(file.getFileObject().getFileSystem(), "/"); //NOI18N
                builder.setAbsPathLookupPrefix(prefix);
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        return builder.createDataBaseEntry();
    }

    private static LangStandard.Kind getLangStd(NativeFileItem startEntry) throws AssertionError {
        LangStandard.Kind lang_std = LangStandard.Kind.lang_unspecified;
        switch (startEntry.getLanguageFlavor()) {
            case DEFAULT:
            case UNKNOWN:
                break;
            case C:
                break;
            case C89:
                lang_std = LangStandard.Kind.lang_gnu89;
                break;
            case C99:
                lang_std = LangStandard.Kind.lang_gnu99;
                break;
            case CPP98:
                // we don't have flavor for C++98 in APT, but C++03 is used in fact
                lang_std = LangStandard.Kind.lang_cxx03;
                break;
            case CPP11:
                lang_std = LangStandard.Kind.lang_gnucxx11;
                break;
            case C11:
                lang_std = LangStandard.Kind.lang_gnu11;
                break;
            case CPP14:
                // FIXME
                lang_std = LangStandard.Kind.lang_gnucxx14;
                break;
            case CPP17:
                // FIXME
                lang_std = LangStandard.Kind.lang_gnucxx1z;
                break;
            case F77:
            case F90:
            case F95:
            default:
                throw new AssertionError(startEntry.getLanguageFlavor().name());
        }
        return lang_std;
    }

    private static InputKind getLang(NativeFileItem startEntry) throws AssertionError {
        InputKind lang = InputKind.IK_None;
        switch (startEntry.getLanguage()) {
            case C:
            case C_HEADER:
                //header file should be considered th esame way code assitance does
                lang = NativeProjectSupport.getDefaultHeaderStandard() == NativeFileItem.LanguageFlavor.C ? 
                        InputKind.IK_C : InputKind.IK_CXX;
                break;
            case CPP:
                lang = InputKind.IK_CXX;
                break;
            case FORTRAN:
            case OTHER:
            default:
                throw new AssertionError(startEntry.getLanguage().name());
        }
        return lang;
    }

    @Override
    public synchronized Collection<CodeAudit> getAudits() {
        if (audits == null || audits.isEmpty()) {
            audits = new ArrayList<>();
            for (ClankChecker checker : ClankCheckersProvider.getAllCheckers()) {
                audits.add(new ClankDiagnosticAudit(checker));
            }
        }
        return audits;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//    private final ClankDiagnosticAudit clankShowAllWarning
//            = new ClankDiagnosticAudit("clank.diagnostic.show.all.warnings", "Show All Warning", "Show All Warning");//NOI18N
//    private final ClankDiagnosticAudit clankStaticAnalyzer
//            = new ClankDiagnosticAudit("clank.diagnostic.static.analyzer", "Static Analyzer", "Static Analyzer");//NOI18N    

    @Override
    public AuditPreferences getPreferences() {
        return myPreferences;
    }

//    @Override
//    public JComponent createComponent(Preferences context) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    private class ClankDiagnosticAudit implements CodeAudit {
        private final ClankChecker clankChecker;

        //private final AuditPreferences myPreferences;
        //private static final String CLANK_SHOW_ALL_WARNINGS = "clank.diagnostic.show.all.warnings";

        private ClankDiagnosticAudit(ClankChecker checker) {
            //myPreferences = new AuditPreferences()
            this.clankChecker = checker;
        }

        @Override
        public String getID() {
            return clankChecker.getName();
        }

        @Override
        public String getName() {
            return clankChecker.getName();
        }

        @Override
        public String getDescription() {
            return clankChecker.getDescription();
        }

        @Override
        public boolean isEnabled() {
            String defValue = getDefaultEnabled() ? "true" : "false"; //NOI18N
            return !"false".equals(getPreferences().get(getID(), "enabled", defValue)); //NOI18N
        }

        @Override
        public boolean getDefaultEnabled() {
            return false;
        }

        @Override
        public String minimalSeverity() {
            return "hint";//NOI18N
        }

        @Override
        public String getDefaultSeverity() {
            return "hint";//NOI18N
        }

        @Override
        public String getKind() {
            return "inspection";//NOI18N
        }

        @Override
        public AuditPreferences getPreferences() {
            return myPreferences;
        }

    }

// @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1600)
//    public static final class ClankDiagnosticFixProvider extends CsmErrorInfoHintProvider {
//
//        @Override
//        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
//            alreadyFound.addAll(createFixes(info));
//            return alreadyFound;
//        }
//    }
//
//    private static List<? extends Fix> createFixes(CsmErrorInfo info) {
//        if (info instanceof ClankDiagnosticInfo) {
//            ClankDiagnosticInfo clankInfo = (ClankDiagnosticInfo) info;
//            //List<Replacement> replacements = mei.getDiagnostics().getReplacements();
////            if (!replacements.isEmpty()) {
////                return Collections.singletonList(new ModernizeFix(replacements, mei.getId()));
////            }
//            return clankInfo.getFix() == null ? Collections.EMPTY_LIST : Collections.singletonList(clankInfo.getFix());
//        }
//        return Collections.EMPTY_LIST;
//    }  
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 9200)
    public final static class ClankHintProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(final CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof ClankCsmErrorInfo) {
                final ClankDiagnosticInfo errorInfo = ((ClankCsmErrorInfo) info).getDelegate();
                if (!errorInfo.fixes().isEmpty()) {
                    try {
                        ClankEnhancedFix fixImpl = new ClankEnhancedFix(((ClankCsmErrorInfo) info).getCsmFile(), errorInfo.fixes());
                        alreadyFound.add(fixImpl);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
                if (errorInfo.hasNotes()) {
                    //add action "Show Details"
                    Fix fixImpl = new Fix() {
                        @Override
                        public String getText() {
                            return "Error Path for " + errorInfo.getMessage();//NOI18N
                        }

                        @Override
                        public ChangeInfo implement() throws Exception {
                            ClankErrorPathDetailsProvider provider = Lookup.getDefault().lookup(ClankErrorPathDetailsProvider.class);
                            provider.implement((ClankCsmErrorInfo)info);
                            return null;
                        }
                    };
                    alreadyFound.add(fixImpl);
                }
            }
            return alreadyFound;
        }
    }

}
