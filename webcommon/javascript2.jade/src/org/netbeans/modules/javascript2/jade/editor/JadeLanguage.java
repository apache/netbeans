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

import org.netbeans.modules.javascript2.jade.editor.indent.JadeFormatter;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Pisl
 */
@LanguageRegistration(mimeType="text/jade", useMultiview = true) //NOI18N
public class JadeLanguage extends DefaultLanguageConfig {

    private static String LINE_COMMENT_PREFIX = "//";
    
    @MIMEResolver.ExtensionRegistration(
        extension={ "jade"},
        displayName="#JadeResolver",
        mimeType=JadeTokenId.JADE_MIME_TYPE,
        position=191
    )
    @NbBundle.Messages("JadeResolver=Jade Files")
    @MultiViewElement.Registration(displayName = "#LBL_JadeEditorTab",
        iconBase = "org/netbeans/modules/javascript2/jade/resources/jade16.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "jade.source",
        mimeType = JadeTokenId.JADE_MIME_TYPE,
        position = 2)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
    
    public JadeLanguage() {
        super();
    }
    
    @Override
    public Language getLexerLanguage() {
        return JadeTokenId.jadeLanguage();
    }

    @Override
    public String getDisplayName() {
        return "Jade"; //NOI18N
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new JadeStructureScanner();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new JadeCodeCompletion();
    }
    
    @Override
    public Parser getParser() {
        return new JadeParser();
    }

    @Override
    public Formatter getFormatter() {
        return new JadeFormatter();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public String getLineCommentPrefix() {
        return LINE_COMMENT_PREFIX;
    }

    
    // This is a fake parser to get work some features like folding.
    private static class JadeParser extends Parser {

        private Snapshot lastSnapshot = null;
        
        public JadeParser() {
        }

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            lastSnapshot = snapshot;
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            return new JadeParserResult(lastSnapshot);
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
            
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
            
        }
    }

    private static class JadeParserResult extends ParserResult {

        public JadeParserResult(final Snapshot snapshot) {
            super(snapshot);
        }

        @Override
        protected void invalidate() {
            
        }

        @Override
        public List<? extends Error> getDiagnostics() {
            return Collections.emptyList();
        }
    }

}
