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
package org.netbeans.modules.javascript2.vue.editor;

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.javascript2.vue.editor.lexer.VueLexer;
import org.netbeans.modules.javascript2.vue.editor.lexer.VueTokenId;
import org.netbeans.modules.javascript2.vue.editor.lexer.VueTokenId.VueLanguageHierarchy;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author bogdan.haidu
 */
@LanguageRegistration(mimeType = "text/x-vue", useMultiview = true)
public class VueLanguage extends DefaultLanguageConfig {

    public static final String MIME_TYPE = "text/x-vue"; //NOI18N

    @MIMEResolver.ExtensionRegistration(
            extension = {"vue", "Vue"},
            displayName = "Vue",
            mimeType = MIME_TYPE,
            position = 192
    )
    @NbBundle.Messages("Source=&Source Vue")
    @MultiViewElement.Registration(displayName = "#Source",
            iconBase = "org/netbeans/modules/javascript2/vue/resources/vue16.png",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "vue.source",
            mimeType = MIME_TYPE,
            position = 2)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    public VueLanguage() {
        super();
    }

    @Override
    public Language<VueTokenId> getLexerLanguage() {
        return language;
    }

    @Override
    public String getDisplayName() {
        return "Vue"; //NOI18N
    }

    @Override
    public String getPreferredExtension() {
        return "vue"; // NOI18N
    }

    @Override
    public Parser getParser() {
        return new VueParser();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return super.isIdentifierChar(c) || c == '-'; //NOI18N
    }

    private static final Language<VueTokenId> language
            = new VueLanguageHierarchy() {

                @Override
                protected String mimeType() {
                    return VueLanguage.MIME_TYPE;
                }

                @Override
                protected Lexer<VueTokenId> createLexer(LexerRestartInfo<VueTokenId> info) {
                    return new VueLexer(info);
                }

            }.language();

    // This is a fake parser to get work some features like folding.
    private static class VueParser extends Parser {

        private Snapshot lastSnapshot = null;

        public VueParser() {
        }

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            lastSnapshot = snapshot;
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            return new VueParserResult(lastSnapshot);
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {

        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {

        }
    }

    private static class VueParserResult extends ParserResult {

        public VueParserResult(final Snapshot snapshot) {
            super(snapshot);
        }

        @Override
        protected void invalidate() {

        }

        @Override
        public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
            return Collections.emptyList();
        }
    }
}
