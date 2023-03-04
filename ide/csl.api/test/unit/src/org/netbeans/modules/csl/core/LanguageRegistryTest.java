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
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LanguageRegistryTest extends NbTestCase {
    private FileObject plugins;
    private GsfDataLoader loader;
    public LanguageRegistryTest(String n) {
        super(n);
    }
/*
    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            gui(false).
            addTest(LanguageRegistryTest.class)
        );
    }
*/
    @Override
    public void setUp() throws IOException {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().getRoot();
        plugins = FileUtil.createFolder(fo, "CslPlugins");
        loader = GsfDataLoader.getLoader(GsfDataLoader.class);
    }

    @Override
    public void tearDown() {
    }

    public void testAddLanguages() throws Exception {
        LanguageRegistry r = LanguageRegistry.getInstance();

        assertNull("No languages thus far", r.getLanguageByMimeType("text/x-php5"));
        
        Collection<String> previous = Collections.list(loader.getExtensions().mimeTypes());

        FileObject inst = FileUtil.createData(plugins, "text/x-php5/language.instance");
        inst.setAttribute("instanceCreate", new MyLang());

        assertNotNull("Language found", r.getLanguageByMimeType("text/x-php5"));
        
        Collection<String> current = new ArrayList<String>(Collections.list(loader.getExtensions().mimeTypes()));
        current.removeAll(previous);
        
        Enumeration<String> en = Collections.enumeration(current);
        assertTrue("One extension", en.hasMoreElements());
        assertEquals("One extension", "text/x-php5", en.nextElement());
        assertFalse("No extensions", en.hasMoreElements());
    }

    public static final class MyLang extends DefaultLanguageConfig {

        public static final String PHP_MIME_TYPE = "text/x-php5"; // NOI18N

        @Override
        public String getLineCommentPrefix() {
            return "//";    //NOI18N
        }

        @Override
        public boolean isIdentifierChar(char c) {
            return Character.isJavaIdentifierPart(c) || (c == '$') ;
        }

        @Override
        public org.netbeans.api.lexer.Language getLexerLanguage() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "PHP";
        }

        @Override
        public String getPreferredExtension() {
            return "php"; // NOI18N
        }

        // Service Registrations

        @Override
        public Parser getParser() {
            return null;
        }

        @Override
        public CodeCompletionHandler getCompletionHandler() {
            return null;
        }

        @Override
        public SemanticAnalyzer getSemanticAnalyzer() {
            return null;
        }

        @Override
        public boolean hasStructureScanner() {
            return false;
        }

        @Override
        public StructureScanner getStructureScanner() {
            return null;
        }

        @Override
        public DeclarationFinder getDeclarationFinder() {
            return null;
        }

        @Override
        public boolean hasOccurrencesFinder() {
            return false;
        }

        @Override
        public OccurrencesFinder getOccurrencesFinder() {
            return null;
        }

        @Override
        public boolean hasFormatter() {
            return true;
        }

        @Override
        public Formatter getFormatter() {
            return null;
        }

        @Override
        public KeystrokeHandler getKeystrokeHandler() {
            return null;
        }

        @Override
        public InstantRenamer getInstantRenamer() {
            return null;
        }

        @Override
        public boolean hasHintsProvider() {
            return true;
        }

        @Override
        public HintsProvider getHintsProvider() {
            return null;
        }

        @Override
        public IndexSearcher getIndexSearcher() {
            return null;
        }
    }


}
