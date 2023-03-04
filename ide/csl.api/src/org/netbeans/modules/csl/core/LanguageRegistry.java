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
package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.editor.codetemplates.CslCorePackageAccessor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Registry which locates and provides information about languages supported
 * by various plugins.
 *
 * @author Tor Norbye
 */
public final class LanguageRegistry implements Iterable<Language> {

    public static synchronized LanguageRegistry getInstance() {
        if (instance == null) {
            instance = new LanguageRegistry();
        }

        return instance;
    }

    /**
     * Return a language implementation that corresponds to the given mimeType,
     * or null if no such language is supported
     */
    public Language getLanguageByMimeType(@NonNull String mimeType) {
        final Map<String, Language> map = getLanguages();
        MimePath mp = MimePath.get(mimeType);
        Language lng = map.get(mimeType);
        if (lng != null) {
            return lng;
        }
        String s = mp.getInheritedType();
        if (s != null && !s.isEmpty()) {
            return map.get(s);
        } else {
            return null;
        }
    }

// XXX: parsingapi
//    private Map<String,Map<String,Boolean>> relevantMimes = new HashMap<String,Map<String,Boolean>>();
//
//    /** Return true iff the given file object is relevant for the given mimeType.
//     * This is true when the file is of a mime type that we're looking for, or if there
//     * is an embedding model mapping available for the given file's mime type targeting
//     * the requested mime type.
//     */
//    public boolean isRelevantFor(FileObject fo, String targetMimeType) {
//        final String fileMimeType = fo.getMIMEType();
//        if (targetMimeType.equals(fileMimeType)) {
//            return true;
//        }
//
//        Map<String,Boolean> mimeMap = relevantMimes.get(targetMimeType);
//        if (mimeMap == null) {
//            mimeMap = new  HashMap<String,Boolean>();
//            relevantMimes.put(targetMimeType, mimeMap);
//        }
//
//        Boolean result = mimeMap.get(fileMimeType);
//        if (result == null) {
//            // Check to see if the file is relevant
//            result = Boolean.FALSE;
//
//            Collection<? extends EmbeddingModel> models = getEmbeddingModels();
//            for (EmbeddingModel model : models) {
//                if (model.getTargetMimeType().equals(targetMimeType) &&
//                    model.getSourceMimeTypes().contains(fileMimeType)) {
//                    result = Boolean.TRUE;
//                    break;
//                }
//            }
//
//            mimeMap.put(fileMimeType, result);
//        }
//
//        return result.booleanValue();
//    }

    @NonNull
    public List<Language> getApplicableLanguages(String mimeType) {
        List<Language> result = new ArrayList<Language>(5);

        final Language origLanguage = getLanguageByMimeType(mimeType);
        if (origLanguage != null) {
            result.add(origLanguage);
        }

// XXX: parsingapi
//        // TODO - cache the answer since this is called a lot (for example during
//        // task list scanning)
//        Collection<? extends EmbeddingModel> models = getEmbeddingModels();
//
//        for (EmbeddingModel model : models) {
//            if (model.getSourceMimeTypes().contains(mimeType)) {
//                Language language = getLanguageByMimeType(model.getTargetMimeType());
//                if (language != null && !result.contains(language)) {
//                    result.add(language);
//                }
//            }
//        }

        return result;
    }

    public List<Language> getEmbeddedLanguages(Document doc, int offset) {
        List<Language> result = new ArrayList<Language>();

        doc.render(
            () -> {
                // Read-lock due to Token hierarchy use
                /*
         at org.netbeans.lib.lexer.inc.IncTokenList.reinit(IncTokenList.java:113)
            at org.netbeans.lib.lexer.TokenHierarchyOperation.setActiveImpl(TokenHierarchyOperation.java:257)
            at org.netbeans.lib.lexer.TokenHierarchyOperation.isActiveImpl(TokenHierarchyOperation.java:308)
            at org.netbeans.lib.lexer.TokenHierarchyOperation.tokenSequence(TokenHierarchyOperation.java:344)
            at org.netbeans.lib.lexer.TokenHierarchyOperation.tokenSequence(TokenHierarchyOperation.java:338)
            at org.netbeans.api.lexer.TokenHierarchy.tokenSequence(TokenHierarchy.java:183)
            at org.netbeans.modules.csl.LanguageRegistry.getEmbeddedLanguages(LanguageRegistry.java:336)
            at org.netbeans.modules.gsfret.hints.infrastructure.SuggestionsTask.getHintsProviderLanguage(SuggestionsTask.java:70)
            at org.netbeans.modules.gsfret.hints.infrastructure.SuggestionsTask.run(SuggestionsTask.java:94)
            at org.netbeans.modules.gsfret.hints.infrastructure.SuggestionsTask.run(SuggestionsTask.java:63)
    [catch] at org.netbeans.napi.gsfret.source.Source$CompilationJob.run(Source.java:1272)             * 
                 */
                TokenSequence ts = TokenHierarchy.get(doc).tokenSequence();
                if (ts != null) {
                    addLanguages(result, ts, offset);
                }
            }
        );

        String mimeType = (String) doc.getProperty("mimeType"); // NOI18N
        if (mimeType != null) {
            Language language = getLanguageByMimeType(mimeType);
            if (language != null && (result.size() == 0 || result.get(result.size()-1) != language))  {
                result.add(language);
            }
        }
        
        return result;
    }

    /**
     * Return true iff the given mimeType is supported by a registered language plugin
     * @return True iff the given mimeType is supported
     */
    public boolean isSupported(@NonNull String mimeType) {
        Parameters.notNull("mimeType", mimeType); //NOI18N
        return getLanguageByMimeType(mimeType) != null;
    }
    
    public Iterator<Language> iterator() {
        Map<String, Language> map = getLanguages();
        return map.values().iterator();
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(LanguageRegistry.class.getName());

    private static LanguageRegistry instance;

    // Keep in sync with csl.api/anttask/**/GsfJar.java!
    private static final String STRUCTURE = "structure.instance"; // NOI18N
    private static final String LANGUAGE = "language.instance"; // NOI18N

    private static final String ICON_BASE = "iconBase"; // NOI18N
    private static final String PARSER = "parser.instance"; // NOI18N
    private static final String COMPLETION = "completion.instance"; // NOI18N
    private static final String RENAMER = "renamer.instance"; // NOI18N
    private static final String FORMATTER = "formatter.instance"; // NOI18N
    private static final String BRACKET_COMPLETION = "bracket.instance"; // NOI18N
    private static final String DECLARATION_FINDER = "declarationfinder.instance"; // NOI18N
    private static final String INDEXER = "indexer.instance"; // NOI18N
    private static final String HINTS = "hints.instance"; // NOI18N
    private static final String SEMANTIC = "semantic.instance"; // NOI18N
    private static final String OCCURRENCES = "occurrences.instance"; // NOI18N
    private static final String OVERRIDING_METHODS = "overridingmethods.instance"; // NOI18N
    private static final String INDEX_SEARCHER = "index_searcher.instance"; // NOI18N

    /** Location in the system file system where languages are registered */
    private static final String FOLDER = "CslPlugins"; // NOI18N
    private boolean cacheDirty = true;
    private Map<String, Language> languagesCache;
    private FileChangeListener sfsTracker;

    /**
     * Creates a new instance of LanguageRegistry
     */
    private LanguageRegistry() {
        // no-op
    }

    /** For testing only! */
    private synchronized void addLanguages(Collection<? extends Language> newLanguages) {
        if (languagesCache != null) {
            throw new RuntimeException("This is for testing purposes only!!!"); //NOI18N
        }

        cacheDirty = false;
        languagesCache = new HashMap<String, Language>(2 * newLanguages.size());
        for (Language language : newLanguages) {
            String mimeType = language.getMimeType();
            languagesCache.put(mimeType, language);
        }
    }

    private void addLanguages(List<Language> result, TokenSequence ts, int offset) {
        ts.move(offset);
        if (ts.moveNext() || ts.movePrevious()) {
            TokenSequence ets = ts.embedded();
            if (ets != null) {
                addLanguages(result, ets, offset); // Recurse
            }
            String mimeType = ts.language().mimeType();
            Language language = getLanguageByMimeType(mimeType);

            if (language != null) {
                result.add(language);
            }
        }
    }

    private Map<String, Language> getLanguages() {
        boolean [] refreshLoader = new boolean [] { false };

        synchronized (this) {
            if (cacheDirty) {
                cacheDirty = false;
                FileSystem sfs;
                try {
                    sfs = FileUtil.getConfigRoot().getFileSystem();
                } catch (FileStateInvalidException fse) {
                    throw new IllegalStateException(fse);
                }
                languagesCache = readSfs(sfs, languagesCache, refreshLoader);
                if (sfsTracker == null) {
                    // First time we run do the cleanup
                    userdirCleanup();

                    // start listening on SystemFileSystem
                    sfsTracker = new FsTracker(sfs);
                }
            }
        }

        if (refreshLoader[0]) {
            GsfDataLoader.getLoader(GsfDataLoader.class).initExtensions();
        }

        return languagesCache;
    }

    private static boolean isValidType(FileObject typeFile) {
        if (!typeFile.isFolder()) {
            return false;
        }

        String typeName = typeFile.getNameExt();
        return MimePath.validate(typeName, null);
    }

    private static boolean isValidSubtype(FileObject subtypeFile) {
        if (!subtypeFile.isFolder()) {
            return false;
        }

        String typeName = subtypeFile.getNameExt();
        return MimePath.validate(null, typeName) && !typeName.equals("base"); //NOI18N
    }

    private static Map<String, Language> readSfs(FileSystem sfs, Map<String, Language> existingMap, boolean changesDetected[]) {
        FileObject registryFolder = sfs.findResource(FOLDER);

        if (registryFolder == null) {
            LOG.fine("No " + FOLDER + " folder"); //NOI18N
            return Collections.<String, Language>emptyMap();
        }

        // Read languages
        LOG.fine("Reading " + FOLDER + " registry..."); //NOI18N
        Map<String, Language> newMap = new HashMap<String, Language>();
        changesDetected[0] = false;

        // Go through mimetype types
        FileObject[] types = registryFolder.getChildren();
        for (int i = 0; i < types.length; i++) {
            if (!isValidType(types[i])) {
                continue;
            }

            // Go through mimetype subtypes
            FileObject[] subtypes = types[i].getChildren();
            for (int j = 0; j < subtypes.length; j++) {
                if (!isValidSubtype(subtypes[j])) {
                    continue;
                }

                String mimeType = types[i].getNameExt() + "/" + subtypes[j].getNameExt(); // NOI18N
                Language existingLanguage = existingMap != null ? existingMap.get(mimeType) : null;
                if (existingLanguage != null) {
                    LOG.fine("Reusing existing Language for '" + mimeType + "': " + existingLanguage); //NOI18N
                    newMap.put(mimeType, existingLanguage);
                    continue;
                }

                Integer attr = (Integer) subtypes[j].getAttribute("genver"); //NOI18N
                if (attr == null) {
                    LOG.log(Level.SEVERE, "Language " + mimeType + " has not been preprocessed during jar module creation"); //NOI18N
                } else if (attr.intValue() == 1) {
                    LOG.log(Level.WARNING, "Language " + mimeType + " has been preprocessed using the deprecated CslJar task. " + //NOI18N
                        "Please use @LanguageRegistration annotation, see https://netbeans.org/bugzilla/show_bug.cgi?id=169991 for details."); //NOI18N
                }

                Language language = new Language(mimeType);
                newMap.put(mimeType, language);
                LOG.fine("Adding new Language for '" + mimeType + "': " + language); //NOI18N
                changesDetected[0] = true;

                Boolean useCustomEditorKit = (Boolean)subtypes[j].getAttribute("useCustomEditorKit"); // NOI18N
                if (useCustomEditorKit != null && useCustomEditorKit.booleanValue()) {
                    language.setUseCustomEditorKit(true);
                    LOG.fine("Language for '" + mimeType + "' is using custom editor kit."); //NOI18N
                }
                
                Boolean useMultiview = (Boolean)subtypes[j].getAttribute("useMultiview"); // NOI18N
                if (useMultiview != null && useMultiview.booleanValue()) {
                    language.setUseMultiview(true);
                    LOG.fine("Language for '" + mimeType + "' is using multview."); //NOI18N
                }
                
                // Try to obtain icon from (new) IDE location for icons per mime type:
                FileObject loaderMimeFile = sfs.findResource("Loaders/" + mimeType); // NOI18N
                if (loaderMimeFile != null) {
                    String iconBase = (String)loaderMimeFile.getAttribute(ICON_BASE);

                    if ((iconBase != null) && (iconBase.length() > 0)) {
                        language.setIconBase(iconBase);
                    }
                }
                
                boolean foundConfig = false;
                for (FileObject fo : subtypes[j].getChildren()) {
                    String name = fo.getNameExt();
                    LOG.fine("Language for '" + mimeType + "' registers: " + name); //NOI18N
                    
                    if (LANGUAGE.equals(name)) {
                        foundConfig = true;
                        language.setGsfLanguageFile(fo);
                    } else if (HINTS.equals(name)) {
                        language.setHintsProviderFile(fo);
                    } else if (STRUCTURE.equals(name)) {
                        language.setStructureFile(fo);
                    } else if (PARSER.equals(name)) {
                        language.setParserFile(fo);
                    } else if (COMPLETION.equals(name)) {
                        language.setCompletionProviderFile(fo);
                    } else if (RENAMER.equals(name)) {
                        language.setInstantRenamerFile(fo);
                    } else if (FORMATTER.equals(name)) {
                        language.setFormatterFile(fo);
                    } else if (DECLARATION_FINDER.equals(name)) {
                        language.setDeclarationFinderFile(fo);
                    } else if (BRACKET_COMPLETION.equals(name)) {
                        language.setBracketCompletionFile(fo);
                    } else if (INDEXER.equals(name)) {
                        language.setIndexerFile(fo);
                    //} else if (PALETTE.equals(name)) {
                    //    language.setPaletteFile(fo);
                    } else if (SEMANTIC.equals(name)) {
                        language.setSemanticAnalyzer(fo);
                    } else if (OCCURRENCES.equals(name)) {
                        language.setOccurrencesFinderFile(fo);
                    } else if (OVERRIDING_METHODS.equals(name)) {
                        language.setOverridingMethodsFile(fo);
                    } else if (INDEX_SEARCHER.equals(name)) {
                        language.setIndexSearcher(fo);
                    }
                }

                if (!foundConfig) {
                    LOG.warning("No GsfLanguage instance registered in " + subtypes[j].getPath()); //NOI18N
                }
            }
        }

        if (existingMap != null && newMap.size() != existingMap.size()) {
            changesDetected[0] = true;
        }

        LOG.fine("-- Finished reading " + FOLDER + " registry!"); //NOI18N
        return newMap;
    }

    private static void userdirCleanup() {
        // Don't do this check in release builds, it's for dev builds only.
        // We don't migrate userdir settings related to these services from
        // dev build to dev build.
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (!assertionsEnabled) {
            return;
        }

        FileObject navFo = FileUtil.getConfigFile("Navigator/Panels/text/javascript/org-netbeans-modules-gsfret-navigation-ClassMemberPanel.instance"); // NOI18N
        if (navFo == null || !navFo.canRevert()) {
            // We've already done the cleanup.
            // (text/javascript is in all configurations of the IDE, so if
            // the text/javascript folder is gone, so are all the others
            return;
        }

        try {
            FileObject navigator = FileUtil.getConfigFile("Navigator"); // NOI18N
            if (navigator != null) {
                FileObject panels = navigator.getFileObject("Panels"); // NOI18N
                if (panels != null) {
                    for (FileObject outerMime : panels.getChildren()) {
                        for (FileObject innerMime : outerMime.getChildren()) {
                            FileObject panel = innerMime.getFileObject("org-netbeans-modules-gsfret-navigation-ClassMemberPanel.instance"); // NOI18N
                            if (panel != null) {
                                panel.revert();
                                if (innerMime.getChildren().length == 0) {
                                    innerMime.revert();
                                }
                            }
                            if (outerMime.getChildren().length == 0) {
                                outerMime.revert();
                            }
                        }
                    }
                    if (panels.getChildren().length == 0) {
                        panels.revert();
                        if (navigator.getChildren().length == 0) {
                            navigator.revert();
                        }
                    }
                }
            }


            // Delete editors stuff
            FileObject editors = FileUtil.getConfigFile("Editors"); // NOI18N
            if (editors != null) {
                for (FileObject outerMime : editors.getChildren()) {
                    for (FileObject innerMime : outerMime.getChildren()) {
                        String mimeType = outerMime.getName() + "/" + innerMime.getName();

                        FileObject root = innerMime;

                        // Clean up the settings files
                        FileObject settings = root.getFileObject("Settings.settings"); // NOI18N
                        if (settings != null) {
                            settings.revert();
                        }

                        // init code folding bar
                        FileObject fo = root.getFileObject("SideBar/org-netbeans-modules-editor-gsfret-GsfCodeFoldingSideBarFactory.instance");
                        if (fo != null) {
                            fo.revert();
                        }
                        fo = root.getFileObject("SideBar");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.revert();
                        }
                        fo = root.getFileObject("FoldManager/org-netbeans-modules-gsfret-editor-fold-GsfFoldManagerFactory.instance");
                        if (fo != null) {
                            fo.revert();
                        }
                        fo = root.getFileObject("FoldManager");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.revert();
                        }

                        // YAML cleanup: Was a Schliemann editor in 6.0/6.1/6.5dev so may have to delete its persistent system files
                        if (mimeType.equals("text/x-yaml") || mimeType.equals("text/x-json")) { // NOI18N
                            FileObject f = root.getFileObject("Popup/generate-fold-popup"); // NOI18N
                            if (f != null) {
                                f.revert();
                                f = root.getFileObject("ToolTips/org-netbeans-modules-languages-features-ToolTipAnnotation.instance"); // NOI18N
                                if (f != null) {
                                    f.revert();
                                }
                                f = root.getFileObject("Popup/org-netbeans-modules-languages-features-GoToDeclarationAction.instance"); // NOI18N
                                if (f != null) {
                                    f.revert();
                                }
                                f = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance"); // NOI18N
                                if (f != null) {
                                    f.revert();
                                }
                                f = root.getFileObject("run_script.instance"); // NOI18N
                                if (f != null) {
                                    f.revert();
                                }
                            }
                        }

                        // Delete old names present up to and including beta2
                        FileObject oldSidebar = root.getFileObject("SideBar/org-netbeans-modules-editor-retouche-GsfCodeFoldingSideBarFactory.instance");

                        if (oldSidebar != null) {
                            oldSidebar.revert();
                            oldSidebar = root.getFileObject("FoldManager/org-netbeans-modules-retouche-editor-fold-GsfFoldManagerFactory.instance");
                            if (oldSidebar != null) {
                                oldSidebar.revert();
                            }
                        }

                        // init hyperlink provider
                        FileObject hyperlinkProvider = root.getFileObject("HyperlinkProviders/GsfHyperlinkProvider.instance");
                        if (hyperlinkProvider != null) {
                            hyperlinkProvider.revert();
                        }
                        fo = root.getFileObject("HyperlinkProviders");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.revert();
                        }

                        // Context menu
                        FileObject popup = root.getFileObject("Popup");

                        if (popup != null) {
                            // I can't just do popup!=null to see if I need to dynamically add csl
                            // menu items because modules may have registered additional Popup
                            // items, so the layer will contain Popup already
                            FileObject ref = popup.getFileObject("in-place-refactoring");
                            if (ref != null) {
                                ref.revert();
                            }

                            FileObject gotoF = popup.getFileObject("goto");
                            if (gotoF != null) {
                                fo = gotoF.getFileObject("goto-declaration");
                                if (fo != null) {
                                    fo.revert();
                                }
                                fo = gotoF.getFileObject("goto");
                                if (fo != null) {
                                    fo.revert();
                                }
                                if (gotoF.getChildren().length == 0) {
                                    gotoF.revert();
                                }
                            }
                            fo = popup.getFileObject("SeparatorBeforeCut.instance");
                            if (fo != null) {
                                fo.revert();
                            }
                            fo = popup.getFileObject("format");
                            if (fo != null) {
                                fo.revert();
                            }
                            fo = popup.getFileObject("SeparatorAfterFormat.instance");
                            if (fo != null) {
                                fo.revert();
                            }
                            fo = popup.getFileObject("pretty-print");
                            if (fo != null) {
                                fo.revert();
                            }
                            fo = popup.getFileObject("generate-goto-popup");
                            if (fo != null) {
                                fo.revert();
                            }
                            if (popup.getChildren().length == 0) {
                                popup.revert();
                            }
                        }

                        // Service to show if file is compileable or not
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-gsfret-hints-GsfUpToDateStateProviderFactory.instance");
                        if (fo != null) {
                            fo.revert();
                        }
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-retouche-hints-GsfUpToDateStateProviderFactory.instance");
                        if (fo != null) {
                            fo.revert();
                        }

                        // I'm not sure what this is used for - perhaps to turn orange when there are unused imports etc.
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-gsfret-editor-semantic-OccurrencesMarkProviderCreator.instance");
                        if (fo != null) {
                            fo.revert();
                        }
                        fo = root.getFileObject("UpToDateStatusProvider/org-netbeans-modules-retouche-editor-semantic-OccurrencesMarkProviderCreator.instance");
                        if (fo != null) {
                            fo.revert();
                        }
                        fo = root.getFileObject("UpToDateStatusProvider");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.revert();
                        }

                        // Highlighting layers
                        fo = root.getFileObject("org-netbeans-modules-gsfret-editor-semantic-HighlightsLayerFactoryImpl.instance");
                        if (fo != null) {
                            fo.revert();
                        }

                        // Code completion
                        String completionProviders = "CompletionProviders";
                        FileObject completion = root.getFileObject(completionProviders);

                        if (completion != null) {
                            String templates = "org-netbeans-lib-editor-codetemplates-CodeTemplateCompletionProvider.instance";
                            FileObject templeteProvider = root.getFileObject(completionProviders + "/" + templates);
                            if (templeteProvider != null) {
                                templeteProvider.revert();
                            }
                            String provider = "org-netbeans-modules-gsfret-editor-completion-GsfCompletionProvider.instance";
                            FileObject completionProvider = root.getFileObject(completionProviders + "/" + provider);
                            if (completionProvider != null) {
                                completionProvider.revert();
                            }

                            FileObject old = completion.getFileObject("org-netbeans-modules-retouche-editor-completion-GsfCompletionProvider.instance");
                            if (old != null) {
                                old.revert();
                            }
                            if (completion.getChildren().length == 0) {
                                completion.revert();
                            }
                        }

                        // Editor toolbar: commenting and uncommenting actions
                        fo = root.getFileObject("Toolbars/Default/comment");
                        if (fo != null) {
                            fo.revert();
                        }
                        fo = root.getFileObject("Toolbars/Default/uncomment");
                        if (fo != null) {
                            fo.revert();
                        }
                        FileObject sep = root.getFileObject("Toolbars/Default/Separator-before-comment.instance");
                        if (sep != null) {
                            sep.revert();
                        }
                        fo = root.getFileObject("Toolbars/Default");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.revert();
                            fo = root.getFileObject("Toolbars");
                            if (fo != null && fo.getChildren().length == 0) {
                                fo.revert();
                            }
                        }

                        // init code templates
                        fo = root.getFileObject("CodeTemplateProcessorFactories/org-netbeans-modules-gsfret-editor-codetemplates-GsfCodeTemplateProcessor$Factory.instance");
                        if (fo != null) {
                            fo.revert();
                        }
                        FileObject old = root.getFileObject("CodeTemplateProcessorFactories/org-netbeans-modules-retouche-editor-codetemplates-GsfCodeTemplateProcessor$Factory.instance");
                        if (old != null) {
                            old.revert();
                        }
                        fo = root.getFileObject("CodeTemplateProcessorFactories");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.revert();
                        }

                        // init code templates filters
                        fo = root.getFileObject("CodeTemplateFilterFactories/org-netbeans-modules-gsfret-editor-codetemplates-GsfCodeTemplateFilter$Factory.instance");
                        if (fo != null) {
                            fo.revert();
                        }
                        old = root.getFileObject("CodeTemplateFilterFactories/org-netbeans-modules-retouche-editor-codetemplates-GsfCodeTemplateFilter$Factory.instance");
                        if (old != null) {
                            old.revert();
                        }
                        fo = root.getFileObject("CodeTemplateFilterFactories");
                        if (fo != null && fo.getChildren().length == 0) {
                            fo.revert();
                        }

                        if (innerMime.getChildren().length == 0) {
                            innerMime.revert();
                        }
                    }

                    if (outerMime.getChildren().length == 0) {
                        outerMime.revert();
                    }
                }
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
    }

    private final class FsTracker implements FileChangeListener, Runnable {

        private final FileSystem fs;
        private RequestProcessor.Task slidingTask = RequestProcessor.getDefault().create(this);

        public FsTracker(FileSystem fs) {
            this.fs = fs;
            this.fs.addFileChangeListener(FileUtil.weakFileChangeListener(this, this.fs));
        }

        public void fileFolderCreated(FileEvent fe) {
            process(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            process(fe);
        }

        public void fileChanged(FileEvent fe) {
            process(fe);
        }

        public void fileDeleted(FileEvent fe) {
            process(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            process(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            process(fe);
        }

        private void process(FileEvent fe) {
            if (fe.getFile().getPath().startsWith(FOLDER)) {
                synchronized (LanguageRegistry.this) {
                    cacheDirty = true;
                    slidingTask.schedule(100);
                }
            }
        }

        public void run() {
            boolean [] refreshLoader = new boolean [] { false };

            synchronized (LanguageRegistry.this) {
                if (cacheDirty) {
                    cacheDirty = false;
                    languagesCache = readSfs(fs, languagesCache, refreshLoader);
                }
            }

            if (refreshLoader[0]) {
                GsfDataLoader.getLoader(GsfDataLoader.class).initExtensions();
            }
        }
    } // End of SfsTracker class

    // trampoline for the o.n.m.csl.core package
    static {
        CslCorePackageAccessor.register(new CslCorePackageAccessorImpl());
    }

    private static final class CslCorePackageAccessorImpl extends CslCorePackageAccessor {

        public CslCorePackageAccessorImpl() {
            // no-op
        }

        @Override
        public void languageRegistryAddLanguages(Collection<? extends Language> languages) {
            LanguageRegistry.getInstance().addLanguages(languages);
        }

    } // End of CslCorePackageAccessorImpl class
}
