/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
