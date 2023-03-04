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
package org.netbeans.modules.javascript2.jade.editor;

import java.util.Collections;
import java.util.Set;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;

/**
 *
 * @author Petr Pisl
 */
public class JadeTestBase extends CslTestBase {

    public static String JS_SOURCE_ID = "classpath/js-source"; // NOI18N
    
    public JadeTestBase(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }
    
    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new TestJadeLanguage();
    }
 
    @Override
    protected String getPreferredMimeType() {
        return JadeTokenId.JADE_MIME_TYPE;
    }
    
    public static class TestJadeLanguage extends JadeLanguage {

        public TestJadeLanguage() {
            super();
        }

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(JS_SOURCE_ID);
        }
        
        
        
    }

    @Override
    protected void setUp() throws Exception {        
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        super.setUp();
    }
}
