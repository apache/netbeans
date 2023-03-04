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
package org.netbeans.modules.groovy.editor.actions;

import org.netbeans.modules.groovy.editor.imports.ImportHelper;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author schmidtm, Martin Janicek
 */
@EditorActionRegistration(
    name             = FixImportsAction.ACTION_NAME,
    mimeType         = "text/x-groovy",
    shortDescription = "#fix-groovy-imports-description",
    popupText        = "#fix-groovy-imports"
)
public class FixImportsAction extends BaseAction {

    protected static final String ACTION_NAME = "fix-groovy-imports"; //NOI18N


    public FixImportsAction() {
        super(MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
    }

    @Override
    public boolean isEnabled() {
        // here should go all the logic whether there are in fact missing
        // imports we're able to fix.
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        final FileObject fo = NbEditorUtilities.getDataObject(target.getDocument()).getPrimaryFile();
        final Source source = Source.create(fo);

        CollectMissingImportsTask task = new CollectMissingImportsTask();

        try {
            ParserManager.parse(Collections.singleton(source), task);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        ImportHelper.resolveImports(fo, task.getPackageName(), task.getMissingNames());
    }

    private static final class CollectMissingImportsTask extends UserTask {

        private final List<String> missingNames;
        private String packageName;

        public CollectMissingImportsTask() {
            this.missingNames = new ArrayList<>();
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
            if (result != null) {
                ModuleNode rootModule = ASTUtils.getRoot(result);
                if (rootModule != null) {
                    packageName = rootModule.getPackageName();
                }

                ErrorCollector errorCollector = result.getErrorCollector();
                if (errorCollector == null) {
                    return;
                }
                List<? extends Message> errors = errorCollector.getErrors();
                if (errors == null) {
                    return;
                }

                collectMissingImports(errors);
            }
        }

        private void collectMissingImports(List<? extends Message> errors) {
            for (Message error : errors) {
                if (error instanceof SyntaxErrorMessage) {
                    SyntaxException se = ((SyntaxErrorMessage) error).getCause();

                    if (se != null) {
                        String missingClassName = ImportHelper.getMissingClassName(se.getMessage());

                        if (missingClassName != null) {
                            if (!missingNames.contains(missingClassName)) {
                                missingNames.add(missingClassName);
                            }
                        }
                    }
                }
            }
        }

        public List<String> getMissingNames() {
            return missingNames;
        }

        public String getPackageName() {
            return packageName;
        }
    }
}
