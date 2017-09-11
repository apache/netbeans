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
