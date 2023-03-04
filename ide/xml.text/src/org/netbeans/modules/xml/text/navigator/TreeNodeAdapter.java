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

package org.netbeans.modules.xml.text.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentElementEvent;
import org.netbeans.modules.editor.structure.api.DocumentElementListener;
import static org.netbeans.modules.xml.text.structure.XMLConstants.*;
import org.openide.ErrorManager;

/**
 * TreeNodeAdapter is an implementation of j.s.t.TreeNode encapsulating a DocumentElement
 * instance and listening on its changes.
 *
 * @author Marek Fukala
 */
public class TreeNodeAdapter implements TreeNode, DocumentElementListener {
    
    private DocumentElement de;
    private DefaultTreeModel tm;
    private TreeNode parent;
    private JTree tree;
    
    private ArrayList children = null; //a list of non-content nodes
    private ArrayList textElements = new ArrayList(); //stores wrappers of DocumentElement-s which are children of the DocumentElement held by this TreeNode
    private String textContent = EMPTY_STRING;
    
    //if the node itself contains an error
    private boolean containsError = false;
    //if one of its descendants contains an error
    private int childrenErrorCount = 0;
    
    private static final String EMPTY_STRING = new String("");
    
    /**
     * True, if the Node is currently firing a 'nodechanged' event.
     */
    private boolean firingChange;
    
    public TreeNodeAdapter(DocumentElement de, DefaultTreeModel tm, JTree tree, TreeNode parent) {
        this.de = de;
        this.tm = tm;
        this.tree = tree;
        this.parent = parent;
    }
    
    public int getStart() {
        return de.getStartOffset();
    }
    
    public int getEnd() {
        return de.getEndOffset();
    }
    
    /**Returns a text content of this node. The content is fetched from all text  document elements which
     * are children of the element held by this tree node.
     *
     * @return the text content of this node.
     */
    private String getDocumentContent() {
        checkChildrenAdapters();
        checkDocumentContent();
        return textContent;
    }
    
    public java.util.Enumeration children() {
        checkChildrenAdapters();
        return Collections.enumeration(children);
    }
    
    public boolean getAllowsChildren() {
        return true;
    }
    
    public TreeNode getChildAt(int param) {
        checkChildrenAdapters();
        return (TreeNode)children.get(param);
    }
    
    public int getChildCount() {
        checkChildrenAdapters();
        return children.size();
    }
    
    public int getIndex(TreeNode treeNode) {
        checkChildrenAdapters();
        return children.indexOf(treeNode);
    }
    
    public TreeNode getParent() {
        return parent;
    }
    
    public boolean isLeaf() {
        return getChildCount() == 0;
    }
    
    public DocumentElement getDocumentElement() {
        return de;
    }
    
    private TreeNodeAdapter getChildTreeNode(DocumentElement de) {
        checkChildrenAdapters();
        Iterator i = children.iterator();
        while(i.hasNext()) {
            TreeNodeAdapter tn = (TreeNodeAdapter)i.next();
            if(tn.getDocumentElement().equals(de)) return tn;
        }
        return null;
    }
    
    public boolean containsError() {
        checkChildrenAdapters();
        return containsError;
    }
    
    //returns a number of ancestors with error
    public int getChildrenErrorCount() {
        checkChildrenAdapters();
        return childrenErrorCount;
    }
    
    public String toString() {
        return getText(false);
    }
    
    public String getText(boolean html) {
        if(de.getType().equals(XML_TAG)
                || de.getType().equals(XML_EMPTY_TAG)) {
            //XML TAG text
            String attribsVisibleText = "";
            AttributeSet attribs = getDocumentElement().getAttributes();
            
            if(attribs.getAttributeCount() > 0) {
                String attribsText = getAttribsText();
                if(NavigatorContent.showAttributes) {
                    attribsVisibleText = attribsText.length() > ATTRIBS_MAX_LEN ? attribsText.substring(0,ATTRIBS_MAX_LEN) + "..." : attribsText.toString();
                }
            }
            
            String contentText = "";
            String documentText = getDocumentContent();
            if(NavigatorContent.showContent) {
                contentText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            }
            
            StringBuffer text = new StringBuffer();
            text.append(html ? "<html>" : "");
            text.append(html && containsError ? "<font color=FF0000><b>": ""); //red
            text.append(getDocumentElement().getName());
            text.append(html && containsError ? "</b></font>": "");
            text.append(html ? "<font color=888888>" : "");//gray
            if(attribsVisibleText.trim().length() > 0) {
                text.append(" ");
                text.append(attribsVisibleText);
            }
            text.append(html ? "</font>" : "");
            if(contentText.trim().length() > 0) {
                text.append(" (");
                text.append(HTMLTextEncoder.encodeHTMLText(contentText));
                text.append(")");
            }
            text.append(html ? "</html>" : "");
            
            return text.toString();
            
        } else if(de.getType().equals(XML_PI)) {
            //PI text
            String documentText = getPIText();
            documentText = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return documentText;
        } else if(de.getType().equals(XML_DOCTYPE)) {
            //limit the text length
            String documentText = getDoctypeText();
            String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return visibleText;
        } else if(de.getType().equals(XML_CDATA)) {
            //limit the text length
            String documentText = getCDATAText();
            String visibleText  = documentText.length() > TEXT_MAX_LEN ? documentText.substring(0,TEXT_MAX_LEN) + "..." : documentText;
            return visibleText;
        }
        
        return de.getName() + " [unknown content]";
    }
    
    public String getToolTipText() {
        if(de.getType().equals(XML_TAG)
                || de.getType().equals(XML_EMPTY_TAG)) {
            return getAttribsText() + " " + getDocumentContent();
        } else if(de.getType().equals(XML_PI)) {
            return getPIText();
        } else if(de.getType().equals(XML_DOCTYPE)) {
            return getDoctypeText();
        } else if(de.getType().equals(XML_CDATA)) {
            return getCDATAText();
        }
        return "";
    }
    
    private String getPIText() {
        String documentText = null;
        try {
            documentText = de.getDocumentModel().getDocument().getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            int index = "<?".length() + de.getName().length();
            if(index > (documentText.length() - 1)) index = documentText.length() - 1;
            if(documentText.length() > 0) documentText = documentText.substring(index, documentText.length() - 1).trim();
        }catch(BadLocationException e) {
            return "???";
        }
        return documentText;
    }
    
    private String getDoctypeText() {
        String documentText = "???";
        try {
            documentText = de.getDocumentModel().getDocument().getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading PI name and the <?
            if(documentText.length() > 0) documentText = documentText.substring("<!DOCTYPE ".length() + de.getName().length(), documentText.length() - 1).trim();
        }catch(BadLocationException e) {
            return "???";
        }
        return documentText;
    }
    
    private String getCDATAText() {
        String documentText = "???";
        try {
            documentText = de.getDocumentModel().getDocument().getText(de.getStartOffset(), de.getEndOffset() - de.getStartOffset());
            //cut the leading "<![CDATA[" and trailing "]]"
            String cdataStart = "<![CDATA[";    //NOI18N
            String cdataEnd = "]]";             //NOI18N
            if(documentText.length() > 0 && documentText.contains(cdataStart))
                documentText = documentText.substring(cdataStart.length()).trim();
            if(documentText.length() > 0 && documentText.contains(cdataEnd))
                documentText = documentText.substring(0, documentText.indexOf(cdataEnd)).trim();
        } catch(Exception e) {
            return "???";
        }
        return documentText;
    }
    
    public void childrenReordered(DocumentElementEvent ce) {
        //notify treemodel - do that in event dispath thread
        tm.nodeStructureChanged(TreeNodeAdapter.this);
    }
    
    public String getAttribsText() {
        StringBuffer attribsText = new StringBuffer();
        Enumeration attrNames = getDocumentElement().getAttributes().getAttributeNames();
        if(attrNames.hasMoreElements()) {
            while(attrNames.hasMoreElements()) {
                String aname = (String)attrNames.nextElement();
                String value = (String)getDocumentElement().getAttributes().getAttribute(aname);
                attribsText.append(aname);
                attribsText.append("=\"");
                attribsText.append(value);
                attribsText.append("\"");
                if(attrNames.hasMoreElements()) attribsText.append(", ");
            }
        }
        return attribsText.toString();
    }
    
    public void elementAdded(DocumentElementEvent e) {
        DocumentElement ade = e.getChangedChild();
        
        if(debug) System.out.println(">>> +EVENT called on " + hashCode() + " - " + de + ": element " + ade + " is going to be added");
        if(ade.getType().equals(XML_CONTENT)) {
            //create a text node listener
            textElements.add(new TextElementWrapper(ade));
            //update text text content of the node
            childTextElementChanged();
        } else if(ade.getType().equals(XML_ERROR)) {
            //handle error element
            markNodeAsError(this);
        } else if(ade.getType().equals(XML_COMMENT)) {
            //do nothing for comments
        } else {
            TreeNode tn = new TreeNodeAdapter(ade, tm, tree, this);
            int insertIndex = getVisibleChildIndex(ade);
            //add the element only when there isn't such one
            if(getChildTreeNode(ade) == null) {
                //check whether the insert index doesn't go beyond the actual children length (which states an error)
                if(children.size() < insertIndex /*||
                        children.size() + 1 /* it doesn't contain the currently added element != getDocumentElement().getElementCount()*/) {
                    //error => try to recover by refreshing the current node
                    //debugError(e);
                    //notify treemodel
                    tm.nodeStructureChanged(TreeNodeAdapter.this);
                } else {
                    children.add(insertIndex, tn);
                    final int tnIndex = getIndex(tn);
                    tm.nodesWereInserted(TreeNodeAdapter.this, new int[]{tnIndex});
                }
            }
            if(debug)System.out.println("<<<EVENT finished (node " + tn + " added)");
        }
        
        //fix: if a new nodes are added into the root element (set as invisible), the navigator
        //window is empty. So we need to always expand the root element when adding something into
        if(de.equals(de.getDocumentModel().getRootElement())) {
            //expand path
            tree.expandPath(new TreePath(this));
        }
        
    }
    
    private void debugError(DocumentElementEvent e) {
        StringBuffer sb = new StringBuffer();
        sb.append("An inconsistency between XML navigator and XML DocumentModel occured when adding a new element in the XML DocumentModel! Please report the problem and add following debug messages to the issue along with the XML document you are editing.");
        sb.append("Debug for Node " + this + ":\n"); //NOI18N
        sb.append("Children of current node:\n"); //NOI18N
        Iterator itr = children.iterator();
        while(itr.hasNext()) {
            TreeNodeAdapter tna = (TreeNodeAdapter)itr.next();
            sb.append(tna.toString());
            sb.append("\n"); //NOI18N
        }
        sb.append("\nChildren of DocumentElement (" + getDocumentElement() + ") wrapped by the current node:\n"); //NOI18N
        Iterator currChildrenItr = getDocumentElement().getChildren().iterator();
        while(itr.hasNext()) {
            DocumentElement de = (DocumentElement)itr.next();
            sb.append(de.toString());
            sb.append("\n"); //NOI18N
        }
        sb.append("------------"); //NOI18N
        
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, sb.toString());
    }
    
    private void fireNodeChange() {
        if (!firingChange) {
            try {
                firingChange = true;
                tm.nodeChanged(this);
            } finally {
                firingChange = false;
            }
        }
    }
    
    private void markNodeAsError(final TreeNodeAdapter tna) {
        tna.containsError = true;
        //mark all its ancestors as "childrenContainsError"
        TreeNodeAdapter parent = tna;
        tna.fireNodeChange();
        while((parent = (TreeNodeAdapter)parent.getParent()) != null) {
            if(parent.getParent() != null) parent.childrenErrorCount++; //do not fire for root element
            parent.fireNodeChange();
        }
    }
    
    private int getVisibleChildIndex(DocumentElement de) {
        int index = 0;
        Iterator children = getDocumentElement().getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            if(child.equals(de)) return index;
            
            //skip text and error tokens
            if(!child.getType().equals(XML_CONTENT)
                    && !child.getType().equals(XML_ERROR)
                    && !child.getType().equals(XML_COMMENT)) index++;
        }
        return -1;
    }
    
    public void elementRemoved(DocumentElementEvent e) {
        DocumentElement rde = e.getChangedChild();
        
        if(debug) System.out.println(">>> -EVENT on " + hashCode() + " - " + de + ": element " + rde + " is going to be removed ");
        
        if(rde.getType().equals(XML_CONTENT)) {
            if(debug) System.out.println(">>> removing CONTENT element");
            //remove the text eleemnt listener
            Iterator i = textElements.iterator();
            ArrayList toRemove = new ArrayList();
            while(i.hasNext()) {
                TextElementWrapper tew = (TextElementWrapper)i.next();
                if(rde.equals(tew.getDocumentElement())) toRemove.add(tew);
            }
            textElements.removeAll(toRemove);
            //update text text content of the node
            childTextElementChanged();
        } else if(rde.getType().equals(XML_ERROR)) {
            unmarkNodeAsError(this);
        } else if(rde.getType().equals(XML_COMMENT)) {
            //do nothing for comments
        } else {
            if(debug) System.out.println(">>> removing tag element");
            final TreeNode tn = getChildTreeNode(rde);
            final int tnIndex = getIndex(tn);
            
            if(tn != null) {
                children.remove(tn);
                //notify treemodel - do that in event dispath thread
                tm.nodesWereRemoved(TreeNodeAdapter.this, new int[]{tnIndex}, new Object[]{tn});
            } else if(debug) System.out.println("Warning: TreeNode for removed element doesn't exist!!!");
            
        }
        if(debug) System.out.println("<<<EVENT finished (node removed)");
    }
    
    private void unmarkNodeAsError(final TreeNodeAdapter tna) {
        //handle error element
        tna.containsError = false;
        //unmark all its ancestors as "childrenContainsError"
        TreeNodeAdapter parent = tna;
        tna.fireNodeChange();
        while((parent = (TreeNodeAdapter)parent.getParent()) != null) {
            if(parent.getParent() != null) parent.childrenErrorCount--; //do not fire for root element
            parent.fireNodeChange();
        }
    }
    
    public void attributesChanged(DocumentElementEvent e) {
        if(debug)System.out.println("Attributes of treenode " + this + " has changed.");
        fireNodeChange();
    }
    
    public void contentChanged(DocumentElementEvent e) {
        if(debug) System.out.println("treenode " + this + " changed.");
        fireNodeChange();
    }
    
    //---- private -----
    
    private synchronized void checkChildrenAdapters() {
        if(children == null) {
            //attach myself to the document element as a listener
            de.addDocumentElementListener(this);
            
            //lazyloading children for node
            children = new ArrayList();
            Iterator i = de.getChildren().iterator();
            boolean textElementAdded = false;
            while(i.hasNext()) {
                DocumentElement chde = (DocumentElement)i.next();
                if(chde.getType().equals(XML_CONTENT)) {
                    //create a text node listener
                    textElements.add(new TextElementWrapper(chde));
                    textElementAdded = true;
                } else if(chde.getType().equals(XML_ERROR)) {
                    markNodeAsError(this);
                } else if(chde.getType().equals(XML_COMMENT)) {
                    //do nothing for comments
                } else {
                    //add the adapter only when there isn't any
                    if(getChildTreeNode(chde) == null) {
                        TreeNodeAdapter tna = new TreeNodeAdapter(chde, tm, tree, this);
                        children.add(tna);
                    }
                }
            }
            //update text text content of the node
            if(textElementAdded) childTextElementChanged();
        }
    }
    
    private void childTextElementChanged() {
        textContent = null;
        fireNodeChange();
    }
    
    //generate this node text if one of its nodes has changed
    private void checkDocumentContent() {
        if(textContent == null) {
            //get all text elements children of the document element held by this node
            Iterator children = getDocumentElement().getChildren().iterator();
            StringBuffer buf = new StringBuffer();
            //System.out.println("childTextElementChanged(): " + getDocumentElement() + " has these children:");
            while(children.hasNext()) {
                DocumentElement del = (DocumentElement)children.next();
                if(del.getType().equals(XML_CONTENT)) {
                    try {
                        //the endoffset if increased by +1 due to still not yet resolved issue with element boundaries
                        //should be removed once it is properly fixed. On the other hand the issue has no user impact now.
                        int endOfs = del.getEndOffset() - del.getStartOffset();
                        //check document boundary - the condition should never be true
                        endOfs = endOfs > del.getDocument().getLength() ? del.getDocument().getLength() : endOfs;
                        buf.append((del.getDocument().getText(del.getStartOffset(), endOfs)).trim());
                    }catch(BadLocationException e) {
                        buf.append("???");
                    }
                }
            }
            if(buf.length() == 0) {
                textContent = EMPTY_STRING;
            } else {
                textContent = buf.toString();
            }
            //fire a change event for this node
            fireNodeChange();
        }
    }
    
    private final class TextElementWrapper implements DocumentElementListener {
        
        private DocumentElement de;
        
        public TextElementWrapper(DocumentElement de) {
            this.de = de;
            de.addDocumentElementListener(TextElementWrapper.this);
        }
        
        public DocumentElement getDocumentElement() {
            return de;
        }
        
        public void contentChanged(DocumentElementEvent e) {
            TreeNodeAdapter.this.childTextElementChanged();
        }
        
        //no need to implement these methods
        public void elementAdded(DocumentElementEvent e) {
            //just a test
            System.err.println("????? a child node added into a text element!!!!");
        }
        public void elementRemoved(DocumentElementEvent e) {}
        public void childrenReordered(DocumentElementEvent e) {}
        public void attributesChanged(DocumentElementEvent e) {}
        
    }
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.debug");
    
    private static final int ATTRIBS_MAX_LEN = 100;
    private static final int TEXT_MAX_LEN = 50;
    
}
