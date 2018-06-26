/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.actions;

import org.netbeans.modules.groovy.editor.imports.ImportHelper;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.ErrorCollector;
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
                List errors = errorCollector.getErrors();
                if (errors == null) {
                    return;
                }

                collectMissingImports(errors);
            }
        }

        private void collectMissingImports(List errors) {
            for (Object error : errors) {
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
