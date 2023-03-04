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
package org.netbeans.modules.php.latte;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteTestBase extends CslTestBase {

    public LatteTestBase(String testName) {
        super(testName);
        MockLookup.setLookup(Lookups.singleton(new TestLanguageProvider()));
    }

    @Override
    protected void setUp() throws Exception {
        suppressUselessLogging();
        MockLookup.init();
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});
        MockLookup.setInstances(
                new SimpleFileOwnerQueryImplementation(),
                new TestLanguageProvider());

        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
            TestLanguageProvider.register(LatteTopTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        super.setUp();
    }

    private static void suppressUselessLogging() {
        for (Handler handler : Logger.getLogger("").getHandlers()) {
            handler.setFilter(new Filter() {

                @Override
                public boolean isLoggable(LogRecord record) {
                    boolean result = true;
                    if (record.getSourceClassName().startsWith("org.netbeans.modules.parsing.impl.indexing.LogContext")
                            || record.getSourceClassName().startsWith("org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater")
                            || record.getSourceClassName().startsWith("org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage")) { //NOI18N
                        result = false;
                    }
                    return result;
                }
            });
        }
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new LatteLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return LatteLanguage.LATTE_MIME_TYPE;
    }

}
