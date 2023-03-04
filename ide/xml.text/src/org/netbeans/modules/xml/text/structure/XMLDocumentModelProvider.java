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

package org.netbeans.modules.xml.text.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentChange;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelTransactionCancelledException;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelUtils;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.ErrorManager;

import static org.netbeans.modules.xml.text.structure.XMLConstants.*;
import org.w3c.dom.Attr;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 *
 * @author mf100882
 */
public class XMLDocumentModelProvider implements DocumentModelProvider {
    
    
    public void updateModel(DocumentModel.DocumentModelModificationTransaction dtm,
            DocumentModel model, DocumentChange[] changes)
            throws DocumentModelException, DocumentModelTransactionCancelledException {
        
        long a = System.currentTimeMillis();
        
        if(debug) System.out.println("\n\n\n\n\n");
        if(debug) DocumentModelUtils.dumpElementStructure(model.getRootElement());
        
        XMLSyntaxSupport sup = XMLSyntaxSupport.getSyntaxSupport(model.getDocument());
        if (sup == null) {
            return;
        }
        ArrayList regenerate = new ArrayList(); //used to store elements to be regenerated
        
        for(int i = 0; i < changes.length; i++) {
            DocumentChange dch = changes[i];
            int changeOffset = dch.getChangeStart().getOffset();
            Exception[] thrown = new Exception[1];
            
            try {
                sup.runWithSequence(changeOffset,
                        (TokenSequence s) -> {
                            try {
                                updateModelLocked(sup, regenerate, dtm, model, dch, s);
                            } catch (DocumentModelException | DocumentModelTransactionCancelledException ex) {
                                thrown[0] = ex;
                            }
                            return null;
                        }
                );
                if (thrown[0] != null) {
                    if (thrown[0] instanceof DocumentModelException) {
                        throw (DocumentModelException)thrown[0];
                    }
                    if (thrown[0] instanceof DocumentModelTransactionCancelledException) {
                        throw (DocumentModelTransactionCancelledException)thrown[0];
                    }
                }
            } catch (BadLocationException ex) {
            }
        }
        //update the model
        Iterator elementsToUpdate = regenerate.iterator();
        while(elementsToUpdate.hasNext()) {
            DocumentElement de = (DocumentElement)elementsToUpdate.next();
            generateDocumentElements(dtm, model, de);
        }
        
        if(measure) System.out.println("[xmlmodel] generated in " + (System.currentTimeMillis() - a));
    }
    
    private void updateModelLocked(XMLSyntaxSupport sup, ArrayList regenerate, DocumentModel.DocumentModelModificationTransaction dtm,
            DocumentModel model, DocumentChange dch, TokenSequence seq)
            throws DocumentModelException, DocumentModelTransactionCancelledException, BadLocationException {
        int changeOffset = dch.getChangeStart().getOffset();
        int changeLength = dch.getChangeLength();

        //find an element in which the change happened
        DocumentElement leaf = model.getLeafElementForOffset(changeOffset);
        DocumentElement toRegenerate = leaf;

        if(debug) System.out.println("");
        if(debug) System.out.println(dch);
        try {
            if(debug) System.out.println("inserted text:'" + model.getDocument().getText(changeOffset, changeLength) + "'");
        }catch(BadLocationException e) {
            ;
        }
        if(debug) System.out.println("leaf = " + leaf);

        //parse the document context
        boolean textOnly = false;
        boolean attribsOnly = false;
        //scan the inserted text - if it contains only text set textOnly flag
        seq.move(changeOffset);
        W: while (seq.moveNext()) {
            Token<XMLTokenId>   ti = seq.token();
            XMLTokenId id = ti.id();
            switch (id) {
                case TEXT:
                case DECLARATION:
                case BLOCK_COMMENT:
                case PI_CONTENT:
                case CDATA_SECTION:
                    textOnly = true;
                    break W;

                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                    attribsOnly = true;
                    break W;
            }
        }
        if(textOnly &&
                ( leaf.getType().equals(XML_CONTENT)
                || leaf.getType().equals(XML_DOCTYPE)
                || leaf.getType().equals(XML_PI)
                || leaf.getType().equals(XML_COMMENT)
                || leaf.getType().equals(XML_CDATA))){
            //just a text written into a text element simply fire document element change event and do not regenerate anything
            //add the element update request into transaction
            if(debug) System.out.println("ONLY CONTENT UPDATE!!!");
            dtm.updateDocumentElementText(leaf);

            //do not scan the context tag if the change is only insert or remove of one character into a text (typing text perf. optimalization)
            if(dch.getChangeLength() == 1) {
                return;
            }
        }

        if((attribsOnly || dch.getChangeType() == DocumentChange.REMOVE)
                && (leaf.getType().equals(XML_TAG)
                || leaf.getType().equals(XML_EMPTY_TAG))) {
            if(debug) System.out.println("POSSIBLE ATTRIBS UPDATE!!!");
            //we need to parse the tag element attributes and set them according to the new values
            try {
                SyntaxElement sel = sup.getElementChain(leaf.getStartOffset() + 1);
                if (sup.isStartTag(sel) || sup.isEmptyTag(sel)) {
                    //test whether the attributes changed
                    Map newAttrs = createAttributesMap(sel.getNode());
                    AttributeSet existing = leaf.getAttributes();
                    boolean update = false;
                    if(existing.getAttributeCount() == newAttrs.size()) {
                        Iterator itr = newAttrs.keySet().iterator();
                        while (itr.hasNext()) {
                            String attrName = (String) itr.next();
                            String attrValue = (String)newAttrs.get(attrName);
                            if(attrName != null && attrValue != null
                                    && !existing.containsAttribute(attrName, attrValue)) {
                                update = true;
                                break;
                            }

                        }
                    } else update = true;

                    if(update) {
                        dtm.updateDocumentElementAttribs(leaf, newAttrs);
                    }
                }
            }catch(BadLocationException ble) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ble);
            }
        }

        //if one or more elements are deleted get correct paret to regenerate
        if((leaf.getStartOffset() == leaf.getEndOffset())
                || (changeOffset == leaf.getStartOffset())
                || (changeOffset == leaf.getEndOffset()))
            toRegenerate = leaf.getParentElement();
        else {
            //user written a tag or something what is not a text
            //we need to get the element's parent. Simple leaf.getParent() is not enought
            //since when an element is deleted then a wrong parent can be choosen
            if(leaf.getType().equals(XML_CONTENT)) {
                do {
                    toRegenerate = toRegenerate.getParentElement();
                } while(toRegenerate != null && toRegenerate.getType().equals(XML_CONTENT));

                if(toRegenerate == null) {
                    //no suitable parent found - the element is either a root or doesn't have any xml_tag ancestor => use root
                    toRegenerate = model.getRootElement();
                }
            }
        }

        if(toRegenerate == null) toRegenerate = model.getRootElement(); //root element is empty

        //now regenerate all sub-elements inside parent of the affected element

        //check if the element is not a descendant a one of the elements
        //which are going to be regenerated
        Iterator itr = regenerate.iterator();
        boolean hasAncestor = false;
        while(itr.hasNext()) {
            DocumentElement de = (DocumentElement)itr.next();
            if(de.equals(toRegenerate) || model.isDescendantOf(de, toRegenerate)) {
                hasAncestor = true;
                break;
            }
        }

        if(!hasAncestor) {
            //check whether the element is not an ancestor of one or more element
            //which are going to be regenerated
            ArrayList toRemove = new ArrayList();
            Iterator i2 = regenerate.iterator();
            while(i2.hasNext()) {
                DocumentElement de = (DocumentElement)i2.next();
                if(model.isDescendantOf(toRegenerate, de)) toRemove.add(de);
            }

            //now really remove the elements
            regenerate.removeAll(toRemove);

            //add the element - it will be likely regenerated in next model update
            regenerate.add(toRegenerate);

            //debug>>>
            if(debug) System.out.println("===================================================================");
            if(debug) System.out.println("change happened in " + leaf);
            if(debug) System.out.println("we will regenerate its parent " + toRegenerate);
            //<<<debug
        }
    }
    
    /** generates document elements within an area defined by startoffset and
     *endoffset. */
    private void generateDocumentElements(DocumentModel.DocumentModelModificationTransaction dtm,
            DocumentModel model, DocumentElement de) throws DocumentModelException, DocumentModelTransactionCancelledException {
        
        int startOffset = de.getStartOffset();
        int endOffset = de.getEndOffset();
        
        BaseDocument doc = (BaseDocument)model.getDocument();
        XMLSyntaxSupport sup = XMLSyntaxSupport.createSyntaxSupport(doc);
        
        if(debug) System.out.println("[XMLDocumentModelProvider] regenerating " + de);
        
        Set addedElements = new TreeSet(DocumentModel.ELEMENTS_COMPARATOR);
        ArrayList skipped = new ArrayList();
        try {
            Stack<SyntaxElement> elementsStack = new Stack<>(); //we need this to determine tags nesting
            
            //the syntax element is created for token on offset - 1
            //so I need to add 1 to the startOffset
            SyntaxElement sel = sup.getElementChain(Math.min(doc.getLength(), startOffset+1));
            
            //scan the document for syntax elements - from startOffset to endOffset
            while(sel != null && getSyntaxElementEndOffset(sel) <= endOffset) {
                if(sel.getType() == SyntaxElement.NODE_ERROR) {
                    //add error element into the structure
                    if(debug) System.out.println("Error found! => adding error element.");
                    String errorText = doc.getText(sel.getElementOffset(), sel.getElementLength());

                    //check for empty elements - the error syntax elements is often
                    //just one character long and due to the great hack with
                    //substracting the elements lenght (see getSyntaxElementEndOffset()
                    //method comment) we need to ensure we do not try to add
                    //empty elements (from==to).
                    int from = sel.getElementOffset();
                    int to = getSyntaxElementEndOffset(sel);
                    if(from == to) {
                        to++;
                    }
                    addedElements.add(dtm.addDocumentElement(errorText, XML_ERROR, Collections.emptyMap(),
                            from, to));
                }
                
                if(sup.isStartTag(sel)) {
                    //test if there is already an existing documet element in the model
                    String nn = sel.getNode().getNodeName();
                    DocumentElement tagDE = DocumentModelUtils.findElement(model, sel.getElementOffset(), nn, XML_TAG);
                    
                    //do not skip the 'de' element which is to be regenerated
                    if(tagDE != null && !tagDE.equals(de)) {
                        //test if the element has also correct end tag
                        SyntaxElement endTagCheck = sup.getElementChain(Math.min(doc.getLength(), tagDE.getEndOffset() + 1));
                        if(sup.isEndTag(endTagCheck) && endTagCheck.getNode().getNodeName().equals(nn)) {
                            //there is an element - skip it - analyze an element after the end of the
                            //existing element
                            if(debug) System.out.println("found existing element " + tagDE + " => skipping");
                            //sel = sup.getElementChain(Math.min(doc.getLength(), tagDE.getEndOffset() + 2));
                            sel = endTagCheck.getNext();
                            skipped.add(tagDE);
                            continue;
                        }
                    }
                    
                    //add the tag syntax element into stack
                    elementsStack.push(sel);
                    
                } else if (sup.isEndTag(sel)) {
                    if(!elementsStack.isEmpty()) {
                        SyntaxElement latest = elementsStack.peek();
                        String nn = latest.getNode().getNodeName();
                        if((sel.getNode().getNodeName().equals(nn))) {
                            //we have encountered a pair end tag to open tag on the peek of the stack
                            Map attribs = createAttributesMap(latest.getNode());
                            addedElements.add(dtm.addDocumentElement(nn, XML_TAG, attribs,
                                    latest.getElementOffset(), getSyntaxElementEndOffset(sel)));
                            //remove the open tag syntax element from the stack
                            elementsStack.pop();
                        } else {
                            //found an end tag which doesn't have a start tag
                            //=> take elements from the stack until I found a matching tag
                            
                            //I need to save the pop-ed elements for the case that there isn't
                            //any matching start tag found
                            ArrayList<SyntaxElement> savedElements = new ArrayList<>();
                            //this semaphore is used behind the loop to detect whether a
                            //matching start has been found
                            boolean foundStartTag = false;
                            
                            while(!elementsStack.isEmpty()) {
                                SyntaxElement s = (SyntaxElement)elementsStack.pop();
                                savedElements.add(s);
                                
                                int soff = s.getElementOffset();
                                String sn = s.getNode().getNodeName();
                                String en = sel.getNode().getNodeName();
                                
                                if(sup.isStartTag(s) && sn.equals(en)) {
                                    //found a matching start tag
                                    //XXX I am not sure whether this algorith is correct
                                    Map attribs = createAttributesMap(s.getNode());
                                    addedElements.add(dtm.addDocumentElement(sn, XML_TAG, attribs,
                                            soff, getSyntaxElementEndOffset(sel)));
                                    
                                    foundStartTag = true;
                                    break; //break the while loop
                                }
                            }
                            
                            if(!foundStartTag) {
                                //we didn't find any matching start tag =>
                                //return all elements back to the stack
                                for(int i = savedElements.size() - 1; i >= 0; i--) {
                                    elementsStack.push(savedElements.get(i));
                                }
                            }
                        }
                    }
                } else if(sup.isEmptyTag(sel)) {
                    Map attribs = createAttributesMap(sel.getNode());
                    addedElements.add(dtm.addDocumentElement(sel.getNode().getNodeName(), XML_EMPTY_TAG, attribs,
                            sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                } else {
                    switch (sel.getType()) {
                        case Node.CDATA_SECTION_NODE:
                            addedElements.add(dtm.addDocumentElement("cdata", XML_CDATA, Collections.emptyMap(),
                                    sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                            break;
                        case Node.PROCESSING_INSTRUCTION_NODE: {
                            //PI section
                            String nodeName = ((ProcessingInstruction)sel.getNode()).getNodeName();
                            //if the nodename is not parsed, then the element is somehow broken => do not show it.
                            if(nodeName != null) {
                                addedElements.add(dtm.addDocumentElement(nodeName, XML_PI, Collections.emptyMap(),
                                        sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                            }
                            break;
                        }
                        case Node.DOCUMENT_TYPE_NODE: {
                            //document type <!DOCTYPE xxx [...]>
                            String nodeName = ((DocumentType)sel.getNode()).getName();
                            //if the nodename is not parsed, then the element is somehow broken => do not show it.
                            if(nodeName != null) {
                                addedElements.add(dtm.addDocumentElement(nodeName, XML_DOCTYPE, Collections.emptyMap(),
                                        sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                            }
                            break;
                        }
                        case Node.COMMENT_NODE:
                            //comment element <!-- xxx -->
                            //DO NOT CREATE ELEMENT FOR COMMENTS
                            addedElements.add(dtm.addDocumentElement("comment", XML_COMMENT, Collections.emptyMap(),
                                    sel.getElementOffset(), getSyntaxElementEndOffset(sel)));
                            break;
                        default:
                            //everything else is content
                            int from = sel.getElementOffset();
                            // because of changeset #1d6a31161c70, all elements report end at the last char, but that is not
                            // what is expected from the DocumentElement.
                            int to = getSyntaxElementEndOffset(sel) + 1;
                            if(from < to) {
                                //Do not allow empty elements to be added:
                                //Otherwise it causes problems in DocumentModel.ELEMENTS_COMPARATOR
                                //(idential elements are considered as unequal (the reason why this
                                //required is distinguishing elements after text deletion) and subsequently
                                //the empty elements are added and removed all over again after each model
                                //update which causes performance problems.
                                addedElements.add(dtm.addDocumentElement("...", XML_CONTENT, Collections.emptyMap(), from, to));
                            }
                    }
                }
                //find next syntax element
                //                sel = sel.getNext();     //this cannot be used since it chains the results and they are hard to GC then.
                try {
                    //prevent cycles
                    SyntaxElement prev = null;
                    int add = 0;
                    do {
                        add++;
                        prev = sup.getElementChain(sel.getElementOffset() + sel.getElementLength() + add);
                    } while(prev != null && sel.getElementOffset() >= prev.getElementOffset());
                    sel = prev;
                }catch(BadLocationException ble) {
                    sel = null;
                }
            }
            
            //*** elements removal ***
            // we need to remove those elements which existed before and not exist now
            //we need to get all descendants from non-skipped elements
            List existingElements = getDescendantsOfNotSkippedElements(de, skipped);
            existingElements.add(de);
            
            Iterator existingItr = existingElements.iterator();
            //iterate all existing elements and check if they are still valid
            while(existingItr.hasNext()) {
                DocumentElement d = (DocumentElement)existingItr.next();
                if(!addedElements.contains(d)) {
                    //remove the element - it doesn't longer exist
                    dtm.removeDocumentElement(d, false);
                    if(debug) System.out.println("[xml model] removed element " + d);
                }
            }
            
        } catch( BadLocationException e ) {
            throw new DocumentModelException("Error occurred during generation of Document elements", e);
        }
    }
    
    
    private List<DocumentElement> getDescendantsOfNotSkippedElements(DocumentElement de, List<DocumentElement> skippedElements) {
        ArrayList desc = new ArrayList();
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            if(!skippedElements.contains(child)) {
                desc.add(child);
                desc.addAll(getDescendantsOfNotSkippedElements(child, skippedElements));
            }
        }
        return desc;
    }
    
    private int getSyntaxElementEndOffset(SyntaxElement sel) {
        //I have reduced the length of all SyntaxElements by one character which
        //solves a problem of typing at the SE end position. In such case the text
        //is added to the next SyntaxElement after the position.
        //An example:
        // <a>xxx</a>X
        //Originally the X char was added to the 'a' element which is wrong
        //TODO - the problem of the reduced length needs to be solved somehow
        //resonably. Now if anyone needs to get the SE's text, one character needs
        //to be added to the SE lenght.
        //
        //Please note that this is a big hack which is supposed to be solved
        //in completely different way. However such change would be quite extensive
        //so keeping the current state until it is absolute must to fix that :-|
        return sel.getElementOffset() + sel.getElementLength() -1;
    }
    
    private Map createAttributesMap(Node tag) {
        if(tag.getAttributes().getLength() == 0) {
            return Collections.emptyMap();
        } else {
            HashMap map = new LinkedHashMap(tag.getAttributes().getLength());
            for(int i = 0; i < tag.getAttributes().getLength(); i++) {
                Attr attr = (Attr)tag.getAttributes().item(i);
                map.put(attr.getName(), attr.getValue());
            }
            return map;
        }
    }
    
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.debug");
    private static final boolean measure = Boolean.getBoolean("org.netbeans.modules.xml.text.structure.measure");
    
}
