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
package org.netbeans.modules.css.refactoring;

import java.util.Collection;
import java.util.regex.Matcher;
import javax.swing.text.Document;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class CssElementContext {

    public abstract boolean isRefactoringAllowed();

    public abstract String getElementName();

    public abstract FileObject getFileObject();

    public abstract static class AbstractFileContext extends CssElementContext {

        private FileObject fo;

        public AbstractFileContext(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public FileObject getFileObject() {
            return fo;
        }

        @Override
        public String getElementName() {
            return getFileObject().getName();
        }
    }

    public static class Folder extends AbstractFileContext {

        public Folder(FileObject folder) {
            super(folder);
        }

        @Override
        public boolean isRefactoringAllowed() {
            return true;
        }
    }

    public static class File extends AbstractFileContext {

        private Collection<CssParserResult> results;

        public File(FileObject fileObject, Collection<CssParserResult> result) {
            super(fileObject);
            this.results = result;
        }

        public Collection<CssParserResult> getParserResults() {
            return results;
        }

        @Override
        public boolean isRefactoringAllowed() {
            return true;
        }
    }

    public static class Editor extends CssElementContext {

        private int caretOffset;
        private int selectionFrom, selectionTo;
        private Node element;
        private CssParserResult result;
        private Snapshot topLevelSnapshot;

        public Editor(CssParserResult result, Snapshot topLevelSnapshot, int caretOffset, int selectionFrom, int selectionTo) {
            this.result = result;
            this.caretOffset = caretOffset;
            this.selectionFrom = selectionFrom;
            this.selectionTo = selectionTo;
            this.element = findCurrentElement();
            this.topLevelSnapshot = topLevelSnapshot;

            assert element != null; //at least the root node should always be found
        }

        //XXX make it only caret position sensitive for now
        private Node findCurrentElement() {
            Node root = getParserResult().getParseTree();
            int astOffset = getParserResult().getSnapshot().getEmbeddedOffset(caretOffset);
            Node leaf = NodeUtil.findNodeAtOffset(root, astOffset);
            if (leaf != null) {
                //we found token node, use its encolosing node - parent
                leaf = leaf.parent();
            }

            //if leaf == null the astOffset is out of the root's node range, return the root node
            return leaf == null ? root : leaf;
        }

        public Document getDocument() {
            return result.getSnapshot().getSource().getDocument(false);
        }

        public Snapshot getTopLevelSnapshot() {
            return topLevelSnapshot;
        }

        public CssParserResult getParserResult() {
            return result;
        }

        @Override
        public FileObject getFileObject() {
            return getParserResult().getSnapshot().getSource().getFileObject();
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

//        public Node getSimpleSelectorElement() {
//            return  NodeUtil.getAncestorByType(getElement(), NodeType.simpleSelectorSequence);
//        }
        @Override
        public String getElementName() {
            switch (getElement().type()) {
                case resourceIdentifier:
                    Node string = NodeUtil.getChildTokenNode(getElement(), CssTokenId.STRING);
                    if (string != null) {
                        String ret = WebUtils.unquotedValue(string.image());
                        //w/o extension!
                        int dotIndex = ret.lastIndexOf('.');
                        if (dotIndex != -1) {
                            ret = ret.substring(0, dotIndex);
                        }
                        return ret;
                    }
                case term:
                    Node uri = NodeUtil.getChildTokenNode(getElement(), CssTokenId.URI);
                    if (uri != null) {
                        Matcher m = Css3Utils.URI_PATTERN.matcher(uri.image());
                        if (m.matches()) {
                            int groupIndex = 1;
                            String content = m.group(groupIndex);
                            String ret = WebUtils.unquotedValue(content);
                            //w/o extension!
                            int dotIndex = ret.lastIndexOf('.');
                            if (dotIndex != -1) {
                                ret = ret.substring(0, dotIndex);
                            }
                            return ret;
                        }
                    }
                    return null; //bad!
                default:
                    return getElement().image().toString().trim();
            }
        }

        @Override
        public boolean isRefactoringAllowed() {
            switch (getElement().type()) {
                case hexColor:
                case resourceIdentifier:
                    return true;
                case term:
                    //only for uris
                    return null != NodeUtil.getChildTokenNode(getElement(), CssTokenId.URI);
                default:
                    //selectors
                    return NodeUtil.isSelectorNode(getElement());
            }

        }
    }
}
