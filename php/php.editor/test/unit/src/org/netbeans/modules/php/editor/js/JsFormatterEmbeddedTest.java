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

package org.netbeans.modules.php.editor.js;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import junit.framework.Test;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

public class JsFormatterEmbeddedTest extends PHPTestBase {

    public JsFormatterEmbeddedTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(JsFormatterEmbeddedTest.class).gui(false).suite();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;

        try {
            TestLanguageProvider.register(JsTokenId.javascriptLanguage());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }

        MockLookup.setInstances(new FileEncodingQueryImplementation() {
            @Override
            public Charset getEncoding(FileObject file) {
                return StandardCharsets.UTF_8;
            }
        });
    }

    public void testEmbeddedSimple1() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple1.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple2() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple2.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple3() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple3.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple4() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple4.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple5() throws Exception {
        reformatFileContents("testfiles/js/embeddedSimple5.php", new IndentPrefs(4,4));
    }

    public void testEmbeddedMultipleSections1() throws Exception {
        reformatFileContents("testfiles/js/embeddedMultipleSections1.php", new IndentPrefs(4,4));
    }
}
