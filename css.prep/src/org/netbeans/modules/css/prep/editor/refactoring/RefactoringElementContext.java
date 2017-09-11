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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
