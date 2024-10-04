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
package org.netbeans.modules.php.blade.editor.format;

import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.php.blade.editor.BladeTestBase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * 
 */
public abstract class BladeFormatterTestBase extends BladeTestBase {

    public BladeFormatterTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new TestLanguageProvider());
        
       // AbstractIndenter.inUnitTestRun = true;

        // init TestLanguageProvider
        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;
        
        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            assertNotNull(ec);
            return (BaseDocument) ec.openDocument();
        } catch (Exception ex) {
            fail(ex.toString());
            return null;
        }
    }

    @Override
    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
        // override it because It's already done in setUp()
    }

    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        return new BladeFormatter();
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    protected void format(String fileName) throws Exception {
        format(fileName, new IndentPrefs(4, 4));
    }

    protected void format(String fileName, IndentPrefs indentPreferences) throws Exception {
        assert fileName != null;
        reformatFileContents("testfiles/format/" + fileName + ".blade.php", indentPreferences); //NOI18N
    }

}
