
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints.css;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.css.model.api.ElementHandle;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.html.editor.HtmlSourceUtils;
import org.netbeans.modules.html.editor.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "description.create.rule.in.stylesheet=Create rule {0} in stylesheet {1}",
    "description.create.rule.and.import.stylesheet=Create rule {0} and import stylesheet {1}",
    "description.create.rule.and.import.new.stylesheet=Create rule {0} in new stylesheet {1}"
})
public class CreateRuleInStylesheet implements HintFix {

    private static final String NEW_STYLESHEET_NAME = "style.css"; //NOI18N
    private FileObject externalStylesheet;
    private final FileObject sourceFile;
    private final String ruleName;
    private final String path;
    private final boolean importStyleSheet;
    private final boolean createStyleSheet;

    public CreateRuleInStylesheet(FileObject sourceFile, final FileObject externalStylesheet, String ruleName, boolean importStylesheet, boolean createStyleSheet) {
        this.sourceFile = sourceFile;
        this.externalStylesheet = externalStylesheet;
        this.ruleName = ruleName;
        this.importStyleSheet = importStylesheet;
        this.createStyleSheet = createStyleSheet;

        this.path = externalStylesheet != null
                ? WebUtils.getRelativePath(sourceFile, externalStylesheet)
                : null;
    }

    private String getSelectorText() {
        return new StringBuilder().append(ruleName).toString();
    }

    @Override
    public String getDescription() {
        if (createStyleSheet) {
            return Bundle.description_create_rule_and_import_new_stylesheet(getSelectorText(), NEW_STYLESHEET_NAME);
        } else if (importStyleSheet) {
            return Bundle.description_create_rule_and_import_stylesheet(getSelectorText(), path);
        } else {
            return Bundle.description_create_rule_in_stylesheet(getSelectorText(), path);
        }
    }

    @Override
    public void implement() throws Exception {
        if (createStyleSheet) {
            FileObject folder = sourceFile.getParent();
            externalStylesheet = FileUtil.createData(folder, NEW_STYLESHEET_NAME);
        }

        if (importStyleSheet) {
            Source source = Source.create(sourceFile);
            final Document doc = source.getDocument(false);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    //html must be top level
                    Result result = resultIterator.getParserResult();
                    if (!(result instanceof HtmlParserResult)) {
                        return;
                    }
                    ModificationResult modification = new ModificationResult();
                    if (HtmlSourceUtils.importStyleSheet(modification, (HtmlParserResult) result, externalStylesheet)) {
                        modification.commit();
//                        if (doc != null) {
//                            HtmlSourceUtils.forceReindex(sourceFile);
//                            //refresh hints once the indexing stops 
//                            ParserManager.parseWhenScanFinished("text/css", new UserTask() {
//
//                                @Override
//                                public void run(ResultIterator resultIterator) throws Exception {
//                                    HtmlSourceUtils.rebuildTokenHierarchy(doc);
//                                }
//                            });
//                        }
                    }
                }
            });
        }

        //create the new rule in the stylesheet
        //XXX is it ok to call a parsing task inside a parsing task? I reckon it is safe
        final Model model = Utils.createCssSourceModel(Source.create(externalStylesheet));
        final AtomicReference<ElementHandle> handleRef = new AtomicReference<>();
        model.runWriteTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                try {
                    ModelUtils utils = new ModelUtils(model);
                    Rule rule = utils.createRule(Collections.singleton(getSelectorText()), Collections.<String>emptyList());
                    utils.getBody().addRule(rule);
                    handleRef.set(rule.getElementHandle());
                    model.applyChanges();
                } catch (IOException | BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        final Model newmodel = Utils.createCssSourceModel(Source.create(externalStylesheet));
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                Rule rule = (Rule) handleRef.get().resolve(newmodel);
                if (rule != null) {
                    openLocation(externalStylesheet, rule.getStartOffset());
                }
            }
        });
    }

    private void openLocation(FileObject file, final int offset) {
        try {
            DataObject dob = DataObject.find(file);

            //open and set caret to the location
            final EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        JEditorPane[] openedPanes = ec.getOpenedPanes();
                        if (openedPanes != null && openedPanes.length > 0) {
                            //already opened
                            ec.open(); //give it a focus 
                            JEditorPane pane = openedPanes[0];
                            pane.setCaretPosition(offset);
                        } else {
                            //not opened, open it
                            try {
                                ec.openDocument();
                                ec.open();
                                openedPanes = ec.getOpenedPanes();
                                if (openedPanes != null && openedPanes.length > 0) {
                                    //now opened
                                    JEditorPane pane = openedPanes[0];
                                    pane.setCaretPosition(offset);
                                }
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                });
            }


        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
}
