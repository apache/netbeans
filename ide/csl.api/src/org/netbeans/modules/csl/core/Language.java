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
package org.netbeans.modules.csl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.Action;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.GsfLanguage;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.editor.semantic.ColoringManager;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.openide.filesystems.FileObject;


/**
 * @todo Should languages get to declared "priorities"? In case there are
 *    overlaps in extensions that is.
 * @todo Can I devise a way where one language can "extend" another?
 *    For example, the Jackpot Rule language should simply be the Java language
 *    with a couple of simple changes.
 * @todo Add LanguageVersion list property. For example, for Java, they could be
 *    JDK 1.4, 5.0, 6.0. This would be exposed as a property somewhere (perhaps
 *    on a project basis) and would be used by plugins to drive parser specific
 *    info.  Similarly for JavaScript I have multiple language versions - 1.0 through 1.6
 *    in the case of Rhino (corresponding to different JavaScript/EcmaScript versions).
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public final class Language {
    private ColoringManager coloringManager;
    private String iconBase;
    private String mime;
    private Boolean useCustomEditorKit;
    private Boolean useMultiview;
    private List<Action> actions;
    private GsfLanguage language;
    private DefaultLanguageConfig languageConfig;
    private CodeCompletionHandler completionProvider;
    private InstantRenamer renamer;
    private DeclarationFinder declarationFinder;
    private Formatter formatter;
    private KeystrokeHandler keystrokeHandler;
    private EmbeddingIndexerFactory indexerFactory;
    private StructureScanner structure;
    private OverridingMethods overridingMethods;
    private HintsProvider hintsProvider;
    private GsfHintsManager hintsManager;
    private IndexSearcher indexSearcher;
    //private PaletteController palette;
    private OccurrencesFinder occurrences;
    private SemanticAnalyzer semantic;
    private FileObject parserFile;
    private FileObject languageFile;
    private FileObject completionProviderFile;
    private FileObject renamerFile;
    private FileObject declarationFinderFile;
    private FileObject formatterFile;
    private FileObject keystrokeHandlerFile;
    private FileObject indexerFile;
    private FileObject structureFile;
    private FileObject overridingMethodsFile;
    private FileObject hintsProviderFile;
    //private FileObject paletteFile;
    private FileObject semanticFile;
    private FileObject occurrencesFile;
    private FileObject indexSearcherFile;

    private Set<String> sourcePathIds;
    private Set<String> libraryPathIds;
    private Set<String> binaryLibraryPathIds;

    /** Creates a new instance of DefaultLanguage */
    public Language(String mime) {
        this.mime = mime;
    }

    /** For testing purposes only!*/
    public Language(String iconBase, String mime, List<Action> actions,
            GsfLanguage gsfLanguage, CodeCompletionHandler completionProvider, InstantRenamer renamer,
            DeclarationFinder declarationFinder, Formatter formatter, KeystrokeHandler bracketCompletion, EmbeddingIndexerFactory indexerFactory ,
            StructureScanner structure, /*PaletteController*/Object palette, boolean useCustomEditorKit) {
        this.iconBase = iconBase;
        this.mime = mime;
        this.actions = actions;
        this.language = gsfLanguage;
        this.completionProvider = completionProvider;
        this.renamer = renamer;
        this.declarationFinder = declarationFinder;
        this.formatter = formatter;
        this.keystrokeHandler = bracketCompletion;
        this.indexerFactory = indexerFactory;
        this.structure = structure;
//        this.palette = palette;
        this.useCustomEditorKit = useCustomEditorKit;
    }

    public boolean useCustomEditorKit() {
// This is done during initialization so we don't want to configure all the language configurations
// at this point and cause a lot of class loading.        
//        if (useCustomEditorKit == null) { // Lazy init
//            getGsfLanguage(); // Also initializes languageConfig
//            if (languageConfig != null) {
//                useCustomEditorKit = languageConfig.isUsingCustomEditorKit();
//            } else {
//                useCustomEditorKit = Boolean.FALSE;
//            }
//        }
        if (useCustomEditorKit == null) {
            useCustomEditorKit = Boolean.FALSE;
        }

        return useCustomEditorKit;
    }
    
    void setUseCustomEditorKit(boolean useCustomEditorKit) {
        this.useCustomEditorKit = useCustomEditorKit;
    }
    
    public boolean useMultiview() {
        if (useMultiview == null) {
            useMultiview = Boolean.FALSE;
        }
        return useMultiview;
    }
    
    void setUseMultiview(boolean use) {
        this.useMultiview = use;
    }
    
    /** Return the display-name (user visible, and localized) name of this language.
     * It should be brief (one or two words). For example "Java", "C++", "Groovy",
     * "Visual Basic", etc.
     */
    @NonNull
    public String getDisplayName() {
        GsfLanguage l = getGsfLanguage();
        return l == null ? mime : l.getDisplayName();
    }

    /** Return an icon to be used for files of this language type.
     *  @see org.openide.util.Utilities#loadImage
     */

    //public Image getIcon();

    /** Hmmmm this is a bit rough. The path would have to be relative to some resource...
     *  I guess it would be relative to the specific plugin language class?
     * Example:  "com/foo/bar/javascript.gif"
     * @todo More documentation here, or revise API entirely
     */
    public String getIconBase() {
        return iconBase;
    }

    void setIconBase(String iconBase) {
        this.iconBase = iconBase;
    }

    /** Return the mime-type of this language. For example text/x-java.
     */
    @NonNull
    public String getMimeType() {
        return mime;
    }

    void setMimeType(String mime) {
        this.mime = mime;
    }

    /** Return Actions that will be provided in the editor context menu for this language.
     */
    public Action[] getEditorActions() {
        if (actions != null) {
            return actions.toArray(new Action[0]);
        } else {
            return new Action[0];
        }
    }

    /** Return a language configuration object for this language.
     */
    @NonNull
    public GsfLanguage getGsfLanguage() {
        if (language == null && languageFile != null) {
            // Lazily construct Language
            language = (GsfLanguage)createInstance(languageFile);
            if (language == null) {
                // Don't keep trying
                languageFile = null;
            } else if (language instanceof DefaultLanguageConfig) {
                languageConfig = (DefaultLanguageConfig)language;
            }
        }
        return language;
    }

    //void setGsfLanguage(GsfLanguage scanner) {
    //    this.language = language;
    //}

    void setGsfLanguageFile(FileObject languageFile) {
        this.languageFile = languageFile;
    }
    
    /** Return a parser for use with this language. A parser is optional (in which
     * case getParser() may return null) but in that case a lot of functionality
     * will be disabled for this language.
     * @todo Clarify whether clients should cache instances of this or if it will
     *  be called only once and management done by the IDE
     */
    @CheckForNull
    public Parser getParser(Collection<Snapshot> snapshots) {
        Parser parser = null;

        if (parserFile != null) {
            // Lazily construct Parser
            ParserFactory factory = (ParserFactory)createInstance(parserFile);
            if (factory == null) {
                // Don't keep trying
                parserFile = null;
            } else {
                parser = factory.createParser(snapshots);
            }
        } else {
            getGsfLanguage(); // Also initializes languageConfig
            if (languageConfig != null) {
                parser = languageConfig.getParser();
            }
        }

        return parser;
    }

    void setParserFile(FileObject parserFile) {
        this.parserFile = parserFile;
    }
    
    public void addAction(Action action) {
        if (actions == null) {
            actions = new ArrayList<Action>();
        }
        actions.add(action);
    }
    
    // XXX This is crying out for generics!
    private Object createInstance(FileObject file) {
        return DataLoadersBridge.getDefault().createInstance(file);
    }
    
    @Override
    public String toString() {
        return super.toString() + "[" + mime; //NOI18N
    }

    /**
     * Get a code completion handler, if any
     */
    @CheckForNull
    public CodeCompletionHandler getCompletionProvider() {
        if (completionProvider == null) {
            if (completionProviderFile != null) {
                // Lazily construct completion provider
                completionProvider = (CodeCompletionHandler)createInstance(completionProviderFile);
                if (completionProvider == null) {
                    // Don't keep trying
                    completionProviderFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    completionProvider = languageConfig.getCompletionHandler();
                }
            }
        }
        return completionProvider;
    }

    void setCompletionProvider(CodeCompletionHandler completionProvider) {
        this.completionProvider = completionProvider;
    }
    
    void setCompletionProviderFile(FileObject completionProviderFile) {
        this.completionProviderFile = completionProviderFile;
    }

    /**
     * Get a rename helper, if any, for instant renaming
     */
    @CheckForNull
    public InstantRenamer getInstantRenamer() {
        if (renamer == null) {
            if (renamerFile != null) {
                renamer = (InstantRenamer)createInstance(renamerFile);
                if (renamer == null) {
                    // Don't keep trying
                    renamerFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    renamer = languageConfig.getInstantRenamer();
                }
            }
        }
        return renamer;
    }

    void setInstantRenamerFile(FileObject renamerFile) {
        this.renamerFile = renamerFile;
    }

    /**
     * Get a Declaration finder, if any, for resolving declarations for a given identifier
     */
    @CheckForNull
    public DeclarationFinder getDeclarationFinder() {
        if (declarationFinder == null) {
            if (declarationFinderFile != null) {
                declarationFinder = (DeclarationFinder)createInstance(declarationFinderFile);
                if (declarationFinder == null) {
                    // Don't keep trying
                    declarationFinderFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    declarationFinder = languageConfig.getDeclarationFinder();
                }
            }
        }
        return declarationFinder;
    }

    void setDeclarationFinderFile(FileObject declarationFinderFile) {
        this.declarationFinderFile = declarationFinderFile;
    }

    /**
     * Get an Formatter, if any, for helping indent and reformat code
     */
    @CheckForNull
    public Formatter getFormatter() {
        if (formatter == null) {
            if (formatterFile != null) {
                formatter = (Formatter)createInstance(formatterFile);
                if (formatter == null) {
                    // Don't keep trying
                    formatterFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    formatter = languageConfig.getFormatter();
                    if (formatter != null) {
                        // You MUST return true from this method if you provide a formatter!
                        assert languageConfig.hasFormatter();
                    }
                }
            }
        }
        return formatter;
    }

    void setFormatterFile(FileObject formatterFile) {
        this.formatterFile = formatterFile;
    }
    
    /**
     * Get a KeystrokeHandler helper, if any, for helping with bracket completion
     */
    @CheckForNull
    public KeystrokeHandler getBracketCompletion() {
        if (keystrokeHandler == null) {
            if (keystrokeHandlerFile != null) {
                keystrokeHandler = (KeystrokeHandler)createInstance(keystrokeHandlerFile);
                if (keystrokeHandler == null) {
                    // Don't keep trying
                    keystrokeHandlerFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    keystrokeHandler = languageConfig.getKeystrokeHandler();
                }
            }
        }
        return keystrokeHandler;
    }

    void setBracketCompletionFile(FileObject bracketCompletionFile) {
        this.keystrokeHandlerFile = bracketCompletionFile;
    }

    /**
     * Get an associated palette controller, if any
     */
/*
    @CheckForNull
    PaletteController getPalette();
*/

    /**
     * Get an associated indexer, if any
     */
    @CheckForNull
    public EmbeddingIndexerFactory getIndexerFactory() {
        if (indexerFactory == null) {
            if (indexerFile != null) {
                indexerFactory = (EmbeddingIndexerFactory)createInstance(indexerFile);
                if (indexerFactory == null) {
                    // Don't keep trying
                    indexerFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    indexerFactory = languageConfig.getIndexerFactory();
                }
            }
        }
        return indexerFactory;
    }

    void setIndexerFile(FileObject indexerFile) {
        this.indexerFile = indexerFile;
    }

    /**
     * Get a structure scanner which produces navigation/outline contents
     */
    @CheckForNull
    public StructureScanner getStructure() {
        if (structure == null) {
            if (structureFile != null) {
                structure = (StructureScanner)createInstance(structureFile);
                if (structure == null) {
                    // Don't keep trying
                    structureFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    structure = languageConfig.getStructureScanner();
                    if (structure != null) {
                        // You MUST return true from this method if you provide a structure
                        // scanner!
                        assert languageConfig.hasStructureScanner();
                    }
                }
            }
        }
        return structure;
    }

    void setStructureFile(FileObject structureFile) {
        this.structureFile = structureFile;
    }

    /**
     * Get an associated hints provider, if any
     */
    @CheckForNull
    public HintsProvider getHintsProvider() {
        if (hintsProvider == null) {
            if (hintsProviderFile != null) {
                hintsProvider = (HintsProvider)createInstance(hintsProviderFile);
                if (hintsProvider == null) {
                    // Don't keep trying
                    hintsProviderFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    hintsProvider = languageConfig.getHintsProvider();
                    if (hintsProvider != null) {
                        // You MUST return true from this method if you provide a hints
                        // provider
                        assert languageConfig.hasHintsProvider();
                    }
                }
            }
            if (hintsProvider != null) {
                hintsManager = new GsfHintsManager(getMimeType(), hintsProvider, this);
            }
        }
        return hintsProvider;
    }
    
    @NonNull
    public GsfHintsManager getHintsManager() {
        if (hintsManager == null) {
            if (hintsProvider == null) {
                hintsProvider = getHintsProvider();
            }
            if (hintsProvider != null) {
                hintsManager = new GsfHintsManager(getMimeType(), hintsProvider, this);
            }
        }
        return hintsManager;
    }

    void setHintsProviderFile(FileObject hintsProviderFile) {
        this.hintsProviderFile = hintsProviderFile;
    }
    
//    public PaletteController getPalette() {
//        if (palette == null && paletteFile != null) {
//            palette = (PaletteController)createInstance(paletteFile);
//            if (palette == null) {
//                // Don't keep trying
//                paletteFile = null;
//            }
//        }
//        return palette;
//    }
//
//    void setPaletteFile(FileObject paletteFile) {
//        this.paletteFile = paletteFile;
//    }

    /**
     * Return the coloring manager for this language
     */
    @NonNull
    public ColoringManager getColoringManager() {
        if (coloringManager == null) {
            coloringManager = new ColoringManager(mime);
        }

        return coloringManager;
    }
    
    public boolean hasStructureScanner() {
        if (structureFile != null) {
            return true;
        } else {
            // For performance reasons, we don't want to initialize this yet;
            // navigators have to be installed early during startup for all languages,
            // but we don't want to actually create the configuration objects
            // (which can load a lot of state)
//            getGsfLanguage();
//            if (languageConfig != null) {
//                return languageConfig.hasStructureScanner();
//            }
            return false;
        }
    }
    
    public boolean hasFormatter() {
        if (formatterFile != null) {
            return true;
        } else {
            getGsfLanguage();
            if (languageConfig != null) {
                return languageConfig.hasFormatter();
            }
            return false;
        }
    }

    public boolean hasHints() {
        if (hintsProviderFile != null) {
            return true;
        } else {
            getGsfLanguage();
            if (languageConfig != null) {
                return languageConfig.hasHintsProvider();
            }
            return false;
        }
    }
    
    /**
     * Return the occurrences finder for this language
     */
    @NonNull
    public OccurrencesFinder getOccurrencesFinder() {
        if (occurrences == null) {
            if (occurrencesFile != null) {
                occurrences = (OccurrencesFinder)createInstance(occurrencesFile);
                if (occurrences == null) {
                    // Don't keep trying
                    occurrencesFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    occurrences = languageConfig.getOccurrencesFinder();
                    if (occurrences != null) {
                        // You MUST return true from this method if you provide a structure
                        // scanner!
                        assert languageConfig.hasOccurrencesFinder();
                    }
                }
            }
        }
        return occurrences;
    }

    void setOccurrencesFinderFile(FileObject occurrencesFile) {
        this.occurrencesFile = occurrencesFile;
    }
    
    public boolean hasOccurrencesFinder() {
        if (occurrencesFile != null) {
            return true;
        } else {
            getGsfLanguage();
            if (languageConfig != null) {
                return languageConfig.hasOccurrencesFinder();
            }
            return false;
        }
    }
    
    /**
     * Return the semantic analyzer for this language
     */
    @NonNull
    public SemanticAnalyzer getSemanticAnalyzer() {
        if (semantic == null) {
            if (semanticFile != null) {
                semantic = (SemanticAnalyzer)createInstance(semanticFile);
                if (semantic == null) {
                    // Don't keep trying
                    semanticFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    semantic = languageConfig.getSemanticAnalyzer();
                }
            }
        }
        return semantic;
    }

    void setSemanticAnalyzer(FileObject semanticFile) {
        this.semanticFile = semanticFile;
    }

    /**
     * Return the semantic analyzer for this language
     */
    @NonNull
    public IndexSearcher getIndexSearcher() {
        if (indexSearcher == null) {
            if (indexSearcherFile != null) {
                indexSearcher = (IndexSearcher)createInstance(indexSearcherFile);
                if (indexSearcher == null) {
                    // Don't keep trying
                    indexSearcherFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    indexSearcher = languageConfig.getIndexSearcher();
                }
            }
        }
        return indexSearcher;
    }

    void setIndexSearcher(FileObject indexSearcherFile) {
        this.indexSearcherFile = indexSearcherFile;
    }

    public Set<String> getSourcePathIds() {
        if (sourcePathIds == null) {
            getGsfLanguage();
            if (languageConfig != null) {
                sourcePathIds = languageConfig.getSourcePathIds();
            }
        }
        return sourcePathIds;
    }

    public Set<String> getLibraryPathIds() {
        if (libraryPathIds == null) {
            getGsfLanguage();
            if (languageConfig != null) {
                libraryPathIds = languageConfig.getLibraryPathIds();
            }
        }
        return libraryPathIds;
    }

    public Set<String> getBinaryLibraryPathIds() {
        if (binaryLibraryPathIds == null) {
            getGsfLanguage();
            if (languageConfig != null) {
                binaryLibraryPathIds = languageConfig.getBinaryLibraryPathIds();
            }
        }
        return binaryLibraryPathIds;
    }

    /**
     * Return the overriding methods computer for this language
     */
    @NonNull
    public OverridingMethods getOverridingMethods() {
        if (overridingMethods == null) {
            if (overridingMethodsFile != null) {
                overridingMethods = (OverridingMethods)createInstance(overridingMethodsFile);
                if (overridingMethods == null) {
                    // Don't keep trying
                    overridingMethodsFile = null;
                }
            } else {
                getGsfLanguage(); // Also initializes languageConfig
                if (languageConfig != null) {
                    overridingMethods = languageConfig.getOverridingMethods();
                }
            }
        }
        return overridingMethods;
    }

    void setOverridingMethodsFile(FileObject fo) {
        this.overridingMethodsFile = fo;
    }

}
