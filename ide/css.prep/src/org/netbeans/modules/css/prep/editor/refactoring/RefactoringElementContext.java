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
package org.netbeans.modules.css.prep.editor.refactoring;

import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.prep.editor.model.CPModel;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class RefactoringElementContext {

    private int caretOffset;
    private int selectionFrom, selectionTo;
    private Node element;
    private Snapshot snapshot;
    private CPModel cpModel;

    public RefactoringElementContext(CssParserResult result, int caretOffset) {
        this(result, caretOffset, -1, -1);
    }
    
    public RefactoringElementContext(CssParserResult result, int caretOffset, int selectionFrom, int selectionTo) {
        this.caretOffset = caretOffset;
        this.selectionFrom = selectionFrom;
        this.selectionTo = selectionTo;
        this.snapshot = result.getSnapshot();
        this.element = findCurrentElement(result);
        assert element != null; //at least the root node should always be found
        this.cpModel = CPModel.getModel(result);
    }

    //XXX make it only caret position sensitive for now
    private Node findCurrentElement(CssParserResult result) {
        Node root = result.getParseTree();
        int astOffset = result.getSnapshot().getEmbeddedOffset(caretOffset);
        Node leaf = NodeUtil.findNodeAtOffset(root, astOffset);
        if (leaf != null) {
            //we found token node, use its encolosing node - parent
            leaf = leaf.parent();
        }

        //if leaf == null the astOffset is out of the root's node range, return the root node
        return leaf == null ? root : leaf;
    }

    public Document getDocument() {
        return snapshot.getSource().getDocument(false);
    }

    public FileObject getFileObject() {
        return snapshot.getSource().getFileObject();
    }
    
    public CPModel getCPModel() {
        return cpModel;
    }

    public int getCaret() {
        return caretOffset;
    }

    public int getSelectionFrom() {
        return selectionFrom;
    }

    public int getSelectionTo() {
        return selectionTo;
    }

    public Node getElement() {
        return element;
    }

    public String getElementName() {
        return getElement().image().toString().trim();
    }

    public boolean isRefactoringAllowed() {
        switch (getElement().type()) {
            case cp_variable:
            case cp_mixin_name:
                return true;
            default:
                return false;
        }
    }
    
}
