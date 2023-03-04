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
package org.netbeans.modules.html.editor.xhtml;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.gsf.HtmlKeystrokeHandler;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

@LanguageRegistration(mimeType="text/xhtml", useCustomEditorKit=true)
public class XhtmlElLanguage extends DefaultLanguageConfig {

    public XhtmlElLanguage() {
    }

    @Override
    public Language getLexerLanguage() {
        return XhtmlElTokenId.language();
    }

    @Override
    public Parser getParser() {
        return new XhtmlELParser();
    }

    @Override
    public String getDisplayName() {
        return "XHTML";
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new EmptyHintsProvider();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }
    
    @Override
    public StructureScanner getStructureScanner() {
        return new Scanner();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public String getPreferredExtension() {
        return "xhtml"; // NOI18N
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new HtmlKeystrokeHandler();
    }
    
    private static class EmptyHintsProvider implements HintsProvider {

        @Override
        public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        }

        @Override
        public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
        }

        @Override
        public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
        }

        @Override
        public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        }

        @Override
        public void cancel() {
        }

        @Override
        public List<Rule> getBuiltinRules() {
            return Collections.emptyList();
        }

        @Override
        public RuleContext createRuleContext() {
            return new RuleContext();
        }
        
    }

    private static class XhtmlELParser extends Parser {

        private HtmlParserResult lastResult;
        private ParserResult instance;

        public
        @Override
        void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            instance = new FakeResult(snapshot);
        }

        public
        @Override
        Result getResult(Task task) throws ParseException {
            return instance;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        private static final class FakeResult extends ParserResult {

            public FakeResult(Snapshot snapshot) {
                super(snapshot);
            }

            @Override
            public List<? extends Error> getDiagnostics() {
                return Collections.emptyList();
            }

            @Override
            protected void invalidate() {
            }
        }
    }

    public static class Scanner implements StructureScanner {

        @Override
        public Map<String, List<OffsetRange>> folds(ParserResult info) {
            return Collections.emptyMap();
        }

        @Override
        public List<? extends StructureItem> scan(ParserResult info) {
            return Collections.emptyList();
        }

        @Override
        public Configuration getConfiguration() {
            return new Configuration(false, false, 0);
        }
    }
}
