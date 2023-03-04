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
package org.netbeans.modules.php.smarty;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.modules.php.smarty.editor.gsf.TplLanguage;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class TplTestBase extends CslTestBase {

    public TplTestBase(String testName) {
        super(testName);
        MockLookup.setLookup(Lookups.singleton(new TestLanguageProvider()));
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});
        MockLookup.setInstances(
                new SimpleFileOwnerQueryImplementation(),
                new TestLanguageProvider());

        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
            TestLanguageProvider.register(PHPTokenId.language());
            TestLanguageProvider.register(JsTokenId.javascriptLanguage());
            TestLanguageProvider.register(TplTopTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        resetSmartyOptions();
        super.setUp();
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new TplLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return TplDataLoader.MIME_TYPE;
    }

    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    protected void setupSmartyOptions(String openDelimiter, String closeDelimiter, SmartyFramework.Version version) {
        SmartyOptions.getInstance().setDefaultOpenDelimiter(openDelimiter);
        SmartyOptions.getInstance().setDefaultCloseDelimiter(closeDelimiter);
        SmartyOptions.getInstance().setSmartyVersion(version);
    }

    protected void resetSmartyOptions() {
        SmartyOptions.getInstance().setDefaultOpenDelimiter("{");
        SmartyOptions.getInstance().setDefaultCloseDelimiter("}");
        SmartyOptions.getInstance().setSmartyVersion(SmartyFramework.Version.SMARTY3);
    }

}
