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
package org.netbeans.modules.php.twig.editor;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TwigTestBase extends CslTestBase {

    public TwigTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        suppressUselessLogging();
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
        return new TwigLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return TwigLanguage.TWIG_MIME_TYPE;
    }

}
