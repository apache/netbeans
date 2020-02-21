/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.qnavigator.navigator.BreadcrumbsElementImpl.BreadcrumbsRoot;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 *
 */
public final class BreadCrumbsFactory {
    private static final RequestProcessor RP = new RequestProcessor("C/C++ BreadCrumbsFactory", 1, false, false); //NOI18N
    
    private BreadCrumbsFactory(){
    }
    
    public static void createBreadCrumbs(long caretLineNo, Node selectedNode, JEditorPane jEditorPane, DataObject cdo, final AtomicBoolean canceled, CharSequence text) {
        if (!(selectedNode instanceof CppDeclarationNode)) {
            return;
        }
        final Document doc = jEditorPane.getDocument();
        if (!BreadcrumbsController.areBreadCrumsEnabled(doc)) {
            return;
        }
        Node node = selectedNode;
        final LinkedList<Node> list = new LinkedList<Node>();
        while(true) {
            list.addFirst(node);
            node = node.getParentNode();
            if (node == null) {
                break;
            }
        }
        BreadcrumbsElementImpl breadcrumbsElement = new BreadcrumbsElementImpl(null, list.removeFirst(), cdo, canceled, text);
        breadcrumbsElement.setParent(new BreadcrumbsRoot(cdo, breadcrumbsElement));
        while(true) {
            if (list.isEmpty()) {
                break;
            }
            boolean found = false;
            node = list.removeFirst();
            for(BreadcrumbsElement b : breadcrumbsElement.getChildren()) {
                if (b instanceof BreadcrumbsElementImpl) {
                    if (((BreadcrumbsElementImpl)b).getNode() == node) {
                        breadcrumbsElement = (BreadcrumbsElementImpl) b;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                break;
            }
        }
        if (canceled != null && canceled.get()) {
            return;
        }
        boolean deep = false;
        if (breadcrumbsElement.getNode() == selectedNode) {
            CsmObject csmObject = ((CppDeclarationNode)selectedNode).getCsmObject();
            int startOffset = -1;
            int endOffset = -1;
            if (CsmKindUtilities.isOffsetable(csmObject)) {
                startOffset = ((CsmOffsetable)csmObject).getStartOffset();
                endOffset = ((CsmOffsetable)csmObject).getEndOffset();
            }
            if (startOffset < caretLineNo && caretLineNo < endOffset) {
                if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                    deep = true;
                }
            }
        }
        RP.post(new Body(breadcrumbsElement, doc, caretLineNo, deep, canceled));
    }
    
    private static final class Body implements Runnable {
        private BreadcrumbsElement selected;
        private final long caretLineNo;
        private final Document doc;
        private final boolean deep;
        private final AtomicBoolean canceled;
        
        private Body(BreadcrumbsElement selected, Document doc, long caretLineNo, boolean deep, AtomicBoolean canceled) {
            this.selected = selected;
            this.caretLineNo = caretLineNo;
            this.doc = doc;
            this.deep = deep;
            this.canceled = canceled;
        }

        @Override
        public void run() {
            if (deep) {
                while (true) {
                    if (canceled != null && canceled.get()) {
                        return;
                    }
                    boolean advance = false;
                    for (BreadcrumbsElement child : selected.getChildren()) {
                        if (canceled != null && canceled.get()) {
                            return;
                        }
                        if (child instanceof StatementNode) {
                            int start = ((StatementNode) child).getStartOffset();
                            int end = ((StatementNode) child).getEndOffset();
                            if (start <= caretLineNo && caretLineNo < end) {
                                selected = child;
                                advance = true;
                                //break;
                            }
                        }
                    }
                    if (!advance) {
                        break;
                    }
                }
            }
            BreadcrumbsController.setBreadcrumbs(doc, selected);
        }
    }
}
