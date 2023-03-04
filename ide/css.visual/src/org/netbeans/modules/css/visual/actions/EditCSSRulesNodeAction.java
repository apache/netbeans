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
import java.util.Collections;
import org.netbeans.modules.css.visual.HtmlSourceElementHandle;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.SourceElementHandle;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * @author mfukala@netbeans.org
 */
@NbBundle.Messages({
    "EditCSSRules=Edit CSS Rules"
})
@ActionRegistration(displayName = "#EditCSSRules", lazy = false, asynchronous = false)
@ActionID(category = "DOM", id = "css.visual.actions.EditCSSRulesNodeAction")
@ActionReference(path = "Navigation/DOM/Actions", position = 70, separatorBefore = 50)
public class EditCSSRulesNodeAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }
        Node node = activatedNodes[0];
        final SourceElementHandle sourceElementHandle = node.getLookup().lookup(SourceElementHandle.class);
        
        final FileObject file = sourceElementHandle.getFileObject();
        Source source = Source.create(file);
        if(source == null) {
            return ; //invalid file
        }
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/html");
                        if (ri != null) {
                            Parser.Result parserResult = ri.getParserResult();
                            HtmlParsingResult result = (HtmlParsingResult)parserResult;
                            
                            final EditCSSRulesAction action = new EditCSSRulesAction();
                            action.setContext(file);
                            
                            org.netbeans.modules.html.editor.lib.api.elements.Node resolved = sourceElementHandle.resolve(parserResult);
                            if(resolved != null) {
                                action.setHtmlSourceElementHandle((OpenTag)resolved, ri.getParserResult().getSnapshot(), file);

                                RequestProcessor.getDefault().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        action.actionPerformed(null);
                                    }
                                });
                            }
                            
                            
                        }
                    }
                });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        Node node = activatedNodes[0];
        SourceElementHandle sourceElementHandle = node.getLookup().lookup(SourceElementHandle.class);
        return sourceElementHandle != null && sourceElementHandle.getFileObject() != null;
    }

    @Override
    public String getName() {
        return Bundle.EditCSSRules();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("css.visual.actions.EditRulesNodeAction"); //NOI18N
    }
//    private static class PopupPresenter extends JMenuItem implements DynamicMenuContent {
//        
//        private final JCheckBoxMenuItem[] items;
//
//        private PopupPresenter(Node[] activatedNodes, boolean enabled) {
//            items = new JCheckBoxMenuItem[] {
//                new JCheckBoxMenuItem(Bundle.CTL_BreakOnSubtreeModif()),
//                new JCheckBoxMenuItem(Bundle.CTL_BreakOnAttributesModif()),
//                new JCheckBoxMenuItem(Bundle.CTL_BreakOnNodeRemove()),
//            };
//            if (!enabled) {
//                for (JComponent c : items) {
//                    c.setEnabled(false);
//                }
//            } else {
//                DOMBreakpoint[] domBreakpoints = findDOMBreakpoints();
//                for (int i = 0; i < activatedNodes.length; i++) {
//                    Node node = activatedNodes[i];
//                    bind(items[0], node, DOMBreakpoint.Type.SUBTREE_MODIFIED, domBreakpoints);
//                    bind(items[1], node, DOMBreakpoint.Type.ATTRIBUTE_MODIFIED, domBreakpoints);
//                    bind(items[2], node, DOMBreakpoint.Type.NODE_REMOVED, domBreakpoints);
//                }
//            }
//        }
//
//        @Override
//        public JComponent[] getMenuPresenters() {
//            return items;
//        }
//
//        @Override
//        public JComponent[] synchMenuPresenters(JComponent[] items) {
//            return this.items;
//        }
//        
//    }
}
