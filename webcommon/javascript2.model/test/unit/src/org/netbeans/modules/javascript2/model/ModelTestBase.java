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
package org.netbeans.modules.javascript2.model;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ModelTestBase extends CslTestBase {

    public ModelTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        super.setUp();
    }

    public void checkModel(String file) throws Exception {
        checkModel(file, false);
    }

    public void checkModel(String file, boolean resolve) throws Exception {
        FileObject fo = getTestFile(file);
        Model model = getModel(file);

        final StringWriter sw = new StringWriter();
        Model.Printer p = (String str) -> {
            sw.append(str).append("\n");
        };
        model.writeModel(p, resolve);
        assertDescriptionMatches(fo, sw.toString(), false, ".model", true);
    }

    public Model getModel(String file) throws Exception {
        final Model[] globals = new Model[1];
        Source source = getTestSource(getTestFile(file));

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                Model model = Model.getModel(parameter, false);
                globals[0] = model;
            }
        });
        return globals[0];
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected String getPreferredMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new DefaultLanguageConfig() {
            @Override
            public Language getLexerLanguage() {
                return JsTokenId.javascriptLanguage();
            }

            @Override
            public String getDisplayName() {
                return "Model";
            }

            @Override
            public Set<String> getSourcePathIds() {
                return Collections.emptySet();
            }
        };
    }
}
