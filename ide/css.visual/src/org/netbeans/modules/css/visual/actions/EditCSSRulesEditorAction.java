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
