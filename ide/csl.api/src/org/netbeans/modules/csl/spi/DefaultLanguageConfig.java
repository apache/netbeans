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

package org.netbeans.modules.csl.spi;

import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.GsfLanguage;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;

/**
 * Default implementation of the LanguageConfig class. Descendants of this
 * class also get some extra support; instead of registering services in the
 * layer you can just override the service creation methods below.
 * 
 * @author Tor Norbye
 */
public abstract class DefaultLanguageConfig implements GsfLanguage {
    public DefaultLanguageConfig() {
    }

    //the method is not added to the GsfLanguage interface so far
    //so compatibility is preserved
    public CommentHandler getCommentHandler() {
        return null;
    }

    @Override
    public String getLineCommentPrefix() {
        return null;
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c);
    }

    @Override
    public abstract Language getLexerLanguage();

    @Override
    public abstract String getDisplayName();

    @Override
    public String getPreferredExtension() {
        return null;
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        return null;
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return null;
    }

    @Override
    public Set<String> getSourcePathIds() {
        return null;
    }

    /** 
     * Get a Parser to use for this language, or null if none is available
     * @return a parser instance
     */
    public Parser getParser() {
        return null;
    }
    
    /** 
     * HACK: Some language supports may want to use their own editor kit
     * implementation (such as Schliemann) for some services. By returning
     * true here GSF will not register its own editing services for this mime type.
     * <p>
     * If you set this flag, you may need to register additional services on your
     * own. For example, if you still want GSF "Go To Declaration" functionality,
     * you need to register the GsfHyperlinkProvider.
     * The ruby.rhtml/ module provides an example of this.
     * <p>
     * NOTE: Code folding doesn't work until you enable code folding for your
     * editor kit; see GsfEditorKitFactory's reference to CODE_FOLDING_ENABLE for
     * an example.
     * @deprecated This function is not called anymore. You
     *  MUST register the custom editor kit attribute via the layer! That's because
     *  currently, finding out whether a module has supplies a custom editor
     *  kit must be done early during startup (in the file type recognition code,
     *  to decide if a given module mime type should be owned by GSF)
     *  and we don't want to force loading all language configurations
     *  (including classes they reference) at startup. Hopefully a
     *  better solution will be provided soon.
     */
    @Deprecated
    public boolean isUsingCustomEditorKit() {
        return false;
    }
    
    /**
     * Get a CodeCompletionHandler for this language, or null if none is available
     * @return a CodeCompletionHandler
     */
    @CheckForNull
    public CodeCompletionHandler getCompletionHandler() {
        return null;
    }

    /**
     * Get an InstantRenamer for this language, or null if none is available
     * @return a renamer
     */
    @CheckForNull
    public InstantRenamer getInstantRenamer() {
        return null;
    }

    /**
     * A DeclarationFinder for this language, or null if none is available
     * @return a declaration finder
     */
    @CheckForNull
    public DeclarationFinder getDeclarationFinder() {
        return null;
    }

    /**
     * Return true iff the {@link #getFormatter} method
     * will return a formatter
     * 
     * @return true iff this language configuration provides a
     *  formatter.
     */
    public boolean hasFormatter() {
        return false;
    }

    /**
     * A Formatter for this language, or null if none is available
     * @return the formatter
     */
    @CheckForNull
    public Formatter getFormatter() {
        return null;
    }

    /**
     * A KeystrokeHandler for this language, or null if none is available
     * @return the keystroke handler
     */
    @CheckForNull
    public KeystrokeHandler getKeystrokeHandler() {
        return null;
    }

    /**
     * A Indexer for this language, or null if none is available
     * @return the indexer
     */
    @CheckForNull
    public EmbeddingIndexerFactory getIndexerFactory() {
        return null;
    }
    
    /**
     * Return true iff the {@link #getStructureScanner} method
     * will return a structure scanner.
     * 
     * @return true iff this language configuration provides a
     *  structure scanner.
     * @deprecated This function is not called anymore. You
     *  MUST register structure scanners via the layer! That's because
     *  currently, finding out whether a module has a structure scanner
     *  has to be done very early (before any language types are opened)
     *  and we don't want to force loading all language configurations
     *  (including classes they reference) at startup. Hopefully a
     *  better solution will be provided soon.
     */
    @Deprecated
    public boolean hasStructureScanner() {
        return false;
    }

    /**
     * A StructureScanner for this language, or null if none is available
     * @return the structure scanner
     */
    @CheckForNull
    public StructureScanner getStructureScanner() {
        return null;
    }

    /**
     * Return true iff the {@link #getHintsProvider} method
     * will return a hints provider.
     * 
     * @return true iff this language configuration provides a
     *  hints provider.
     */
    public boolean hasHintsProvider() {
        return false;
    }
    
    /**
     * A HintsProvider for this language, or null if none is available
     * @return the hints provider
     */
    @CheckForNull
    public HintsProvider getHintsProvider() {
        return null;
    }

    /**
     * Return true iff the {@link #getOccurrencesFinder} method
     * will return an occurrences finder.
     * 
     * @return true iff this language configuration provides an
     *  occurrences finder
     */
    public boolean hasOccurrencesFinder() {
        return false;
    }

    /**
     * A OccurrencesFinder for this language, or null if none is available
     * @return the occurrences finder
     */
    @CheckForNull
    public OccurrencesFinder getOccurrencesFinder() {
        return null;
    }

    /**
     * A SemanticAnalyzer for this language, or null if none is available
     * @return the semantic analyzer
     */
    @CheckForNull
    public SemanticAnalyzer getSemanticAnalyzer() {
        return null;
    }

    /**
     * An IndexSearcher which can help with the Open Type, Open Symbol etc features.
     *
     * @return the index searcher
     */
    public IndexSearcher getIndexSearcher() {
        return null;
    }

    public OverridingMethods getOverridingMethods() {
        return null;
    }
    
}
