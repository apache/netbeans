/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.actions;

import org.netbeans.modules.css.visual.api.EditCSSRulesAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.css.visual.HtmlSourceElementHandle;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "Source",
id = "org.netbeans.modules.css.visual.actions.EditCSSRulesEditorAction")
@ActionRegistration(
    displayName = "#CTL_EditElementRulesEditorAction")
@ActionReferences({
    @ActionReference(path = "Editors/text/html/Popup", position = 1300),
    @ActionReference(path = "Editors/text/xhtml/Popup", position = 1300)
})
@Messages("CTL_EditElementRulesEditorAction=Edit CSS Rules")
public final class EditCSSRulesEditorAction implements ActionListener {

    private final EditorCookie context;

    public EditCSSRulesEditorAction(EditorCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            Document doc = context.openDocument();
            final FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
            if (file == null) {
                return;
            }

            JEditorPane[] panes = context.getOpenedPanes();
            if (panes == null || panes.length == 0) {
                return;
            }

            JEditorPane pane = panes[0];
            final int caret = pane.getCaretPosition();

            Source source = Source.create(file);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/html");
                    if (ri == null) {
                        return;
                    }

                    HtmlParserResult result = (HtmlParserResult) ri.getParserResult();
                    Node element = result.findBySemanticRange(caret, true);
                    
                    if(element instanceof OpenTag) {
                        //not the root element
                        OpenTag tag = (OpenTag)element;

                        EditCSSRulesAction action = new EditCSSRulesAction();
                        action.setContext(file);
                        action.setHtmlSourceElementHandle(tag, result.getSnapshot(), file);

                        action.actionPerformed(null);
                        
                    }
                }
            });


        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }


    }
    
    
    
}
