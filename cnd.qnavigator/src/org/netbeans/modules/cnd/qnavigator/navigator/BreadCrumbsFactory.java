/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
