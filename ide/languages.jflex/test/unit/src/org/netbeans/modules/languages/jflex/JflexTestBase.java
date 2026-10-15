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
package org.netbeans.modules.languages.jflex;

import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author bogdan.haidu
 */
public abstract class JflexTestBase extends CslTestBase {

    public JflexTestBase(String name) {
        super(name);
        MockLookup.setLookup(Lookups.singleton(new TestLanguageProvider()));
    }

    @Override
    public void setUp() throws Exception {
        MockLookup.init();
        MockLookup.setInstances(
                new TestLanguageProvider());
        super.setUp();
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JflexLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return JflexLanguage.MIME_TYPE;
    }
}
