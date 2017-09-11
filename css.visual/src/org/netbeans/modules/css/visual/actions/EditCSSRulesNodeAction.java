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
import java.util.Collections;
import org.netbeans.modules.css.visual.HtmlSourceElementHandle;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.SourceElementHandle;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
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
                            HtmlParserResult result = (HtmlParserResult)ri.getParserResult();
                            
                            final EditCSSRulesAction action = new EditCSSRulesAction();
                            action.setContext(file);
                            
                            org.netbeans.modules.html.editor.lib.api.elements.Node resolved = sourceElementHandle.resolve(result);
                            if(resolved != null) {
                                action.setHtmlSourceElementHandle((OpenTag)resolved, result.getSnapshot(), file);

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
