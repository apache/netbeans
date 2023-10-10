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

package org.netbeans.modules.j2ee.ddloaders.common.xmlutils;

import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.CharacterData;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;

/** Static methods useful for XMLJ2eeDataObject.
 *
 * @author  mkuchtiak
 */

public class XMLJ2eeUtils {

    /** This method updates document in editor with newDoc, but leaves the text before prefixMark.
     * @param doc original document
     * @param newDoc new value of whole document
     * @param prefixMark - beginning part of the document before this mark should be preserved
     */
    public static void updateDocument(javax.swing.text.Document doc, String newDoc, String prefixMark) throws javax.swing.text.BadLocationException {
        int origLen = doc.getLength();        
        String origDoc = doc.getText(0, origLen);
        int prefixInd=0;
        if (prefixMark!=null) {
            prefixInd = origDoc.indexOf(prefixMark);
            if (prefixInd>0) {
                origLen-=prefixInd;
                origDoc=doc.getText(prefixInd,origLen);
            }
            else {
                prefixInd=0;
            }
            int prefixIndNewDoc=newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc>0)
            newDoc=newDoc.substring(prefixIndNewDoc);
        }
        //newDoc=filterEndLines(newDoc);
        int newLen = newDoc.length();
        
        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }

        final int granularity = 20;
        
        int prefix = -1;
        int postfix = -1;
        String toInsert = newDoc;
        
        if ((origLen > granularity) && (newLen > granularity)) {
            int pos1 = 0;
            int len = Math.min(origLen, newLen);
            // find the prefix which both Strings begin with
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos1, granularity)) {
                    pos1 += granularity;
                    if (pos1 + granularity >= len) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
            if (pos1 > 0)
                prefix = pos1;
            
            pos1 = origLen - granularity;
            int pos2 = newLen - granularity;
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos2, granularity)) {
                    pos1 -= granularity;
                    pos2 -= granularity;
                    if (pos1 < 0) {
                        pos1 += granularity;
                        break;
                    }
                    if (pos2 < 0) {
                        pos2 += granularity;
                        break;
                    }
                }
                else {
                    pos1 += granularity;
                    pos2 += granularity;
                    break;
                }
            }
            if (pos1 < origLen - granularity) {
                postfix = pos1;
            }
        }

        if ((prefix != -1) && (postfix != -1)) {
            if (postfix < prefix) {
                postfix = prefix;
            }
            
            int delta = (prefix + (origLen - postfix) - newLen);
            if (delta > 0) {
                postfix += delta;
            }
        }
        
        int removeBeginIndex = (prefix == -1) ? 0 : prefix;
        int removeEndIndex = (postfix == -1) ? origLen - 1 : postfix;
        
        doc.remove(prefixInd+removeBeginIndex, removeEndIndex - removeBeginIndex);

        if (toInsert.length() > 0) {
            int p1 = (prefix == -1) ? 0 : prefix;
            int p2 = toInsert.length();
            if (postfix != -1)
                p2 = p2 - (origLen - postfix);

            if (p2 > p1) {
                toInsert = toInsert.substring(p1, p2);
                doc.insertString(prefixInd+removeBeginIndex, toInsert, null);
            }
        }        
    }

    /** This method update document in editor after change in beans hierarchy.
     * It takes old document and new document in String.
     * To avoid regeneration of whole document in text editor following steps are done:
     *  1) compare the begin of both documents (old one and new one)
     *     - find the first position where both documents differ
     *  2) do the same from the ends of documents
     *  3) remove old middle part of text (modified part) and insert new one
     * 
     * @param doc original document
     * @param newDoc new value of whole document
     * @param prefixMark - beginning part of the document before this mark should be preserved
     */
    public static void replaceDocument(javax.swing.text.Document doc, String newDoc, String prefixMark) throws javax.swing.text.BadLocationException {
        int origLen = doc.getLength();        
        String origDoc = doc.getText(0, origLen);
        int prefixInd=0;
        if (prefixMark!=null) {
            prefixInd = origDoc.indexOf(prefixMark);
            if (prefixInd>0) {
                origLen-=prefixInd;
                origDoc=doc.getText(prefixInd,origLen);
            }
            else {
                prefixInd=0;
            }
            int prefixIndNewDoc=newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc>0)
            newDoc=newDoc.substring(prefixIndNewDoc);
        }
        newDoc=filterEndLines(newDoc);
        int newLen = newDoc.length();
        
        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }

        final int granularity = 20;
        
        int prefix = -1;
        int postfix = -1;
        String toInsert = newDoc;
        
        if ((origLen > granularity) && (newLen > granularity)) {
            int pos1 = 0;
            int len = Math.min(origLen, newLen);
            // find the prefix which both Strings begin with
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos1, granularity)) {
                    pos1 += granularity;
                    if (pos1 + granularity >= len) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
            if (pos1 > 0)
                prefix = pos1;
            
            pos1 = origLen - granularity;
            int pos2 = newLen - granularity;
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos2, granularity)) {
                    pos1 -= granularity;
                    pos2 -= granularity;
                    if (pos1 < 0) {
                        pos1 += granularity;
                        break;
                    }
                    if (pos2 < 0) {
                        pos2 += granularity;
                        break;
                    }
                }
                else {
                    pos1 += granularity;
                    pos2 += granularity;
                    break;
                }
            }
            if (pos1 < origLen - granularity) {
                postfix = pos1;
            }
        }

        if ((prefix != -1) && (postfix != -1)) {
            if (postfix < prefix) {
                postfix = prefix;
            }
            
            int delta = (prefix + (origLen - postfix) - newLen);
            if (delta > 0) {
                postfix += delta;
            }
        }
        
        int removeBeginIndex = (prefix == -1) ? 0 : prefix;
        int removeEndIndex;
        if (postfix == -1){
            if(doc.getText(0, doc.getLength()).charAt(doc.getLength()-1) == '>'){
                removeEndIndex = origLen;
            }
            else
                removeEndIndex = origLen-1;
        }
        else 
            removeEndIndex = postfix;
        
        doc.remove(prefixInd+removeBeginIndex, removeEndIndex - removeBeginIndex);
        
        if (toInsert.length() > 0) {
            int p1 = (prefix == -1) ? 0 : prefix;
            int p2 = toInsert.length();
            if (postfix != -1)
                p2 = p2 - (origLen - postfix);

            if (p2 > p1) {
                toInsert = toInsert.substring(p1, p2);
                doc.insertString(prefixInd+removeBeginIndex, toInsert, null);
            }
        }
    }
    
    public static void replaceDocument(javax.swing.text.Document doc, String newDoc) throws javax.swing.text.BadLocationException {
        replaceDocument(doc,newDoc,null);
    }
    /** Filter characters #13 (CR) from the specified String
     * @param str original string
     * @return the string without #13 characters
     */
    public static String filterEndLines(String str) {
        char[] text = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < text.length; i++) {
            char c = text[i];
            if (c != 13) {
                if (pos != i)
                    text[pos] = c;
                pos++;
            }
        }
        return new String(text, 0, pos - 1);
    }

    /** This method updates the attribute specified by the elementPath in DOM tree
     * @param root Root element of the DOM graph 
     * @param elementPath List of ElementAttrInfo that defines the element path to the target element
     * @param attrName Attribute name of the target attribute 
     * @param attrValue New value for the attribute defined by attrName
     * @return true if method was successful, false otherwise
     */    
    public static boolean changeAttribute (Element root, List<ElementAttrInfo> elementPath, String attrName, String attrValue)
    throws org.w3c.dom.DOMException {
       
        //if (elementPath.size()==0) return false;
        if (elementPath==null) return false;
        Iterator<ElementAttrInfo> it = elementPath.iterator();
        Element element = root;
        String keyAttributeName=null;
        NodeList lastNodeList=null;
        int elementIndex=-1; 
        while (it.hasNext()){
            elementIndex=-1;
            ElementAttrInfo info = it.next();
            String attributeName = info.getAttributeName();
            String attributeValue = info.getAttributeValue();
            NodeList nodeList = element.getElementsByTagName(info.getElementName());
            if (nodeList.getLength()==0) return false;
            lastNodeList = nodeList;
            Element newElement=null;
            if (attributeName==null) { // there is only one element
                newElement = (Element)nodeList.item(0);
            } else { // element need to be found by element value
                for (int i=0;i<nodeList.getLength();i++){
                    Element el = (Element)nodeList.item(i);
                    String value = el.getAttribute(attributeName);
                    if (value!=null && value.equals(attributeValue)){
                        newElement = el;
                        keyAttributeName=attributeName;
                        elementIndex=i;
                        break;
                    }
                }
            }
            if (newElement==null) return false;
            else element=newElement;
        }
        if (attrValue==null)
            element.removeAttribute(attrName);
        else {
            // test if there is no such a keyAttribute - avoid a duplicity
            if (attrName!=null && elementIndex>=0 && attrName.equals(keyAttributeName)) {
                for (int i=0;i<lastNodeList.getLength();i++) {
                    if (elementIndex!=i) {
                        Element el = (Element)lastNodeList.item(i);
                        if (el.getAttribute(attrName).equals(attrValue)){
                            showDialog(element.getNodeName(),attrName,attrValue);
                            return false;
                        }
                    }
                }
            }
            element.setAttribute(attrName,attrValue);
        }
        return true;
    }
    public static boolean changeAttribute (Element root, List<ElementAttrInfo> elementPath, String elementName, int index, String attrName, String attrValue)
    throws org.w3c.dom.DOMException {
        if (elementPath.isEmpty()) return false;
        Iterator<ElementAttrInfo> it = elementPath.iterator();
        Element element = root;
        //String keyAttributeName=null;
        NodeList lastNodeList=null;
        int elementIndex=-1;
        while (it.hasNext()){
            elementIndex=-1;
            ElementAttrInfo info = it.next();
            String attributeName = info.getAttributeName();
            String attributeValue = info.getAttributeValue();
            NodeList nodeList = element.getElementsByTagName(info.getElementName());
            if (nodeList.getLength()==0) return false;
            lastNodeList = nodeList;
            Element newElement=null;
            if (attributeName==null) { // there is only one element
                newElement = (Element)nodeList.item(0);
            } else { // element need to be found by element value
                for (int i=0;i<nodeList.getLength();i++){
                    Element el = (Element)nodeList.item(i);
                    String value = el.getAttribute(attributeName);
                    if (value!=null && value.equals(attributeValue)){
                        newElement = el;
                        //keyAttributeName=attributeName;
                        elementIndex=i;
                        break;
                    }
                }
            }
            if (newElement==null) return false;
            else element=newElement;
        }
        NodeList nodeList = element.getElementsByTagName(elementName);
        element = (Element)nodeList.item(index);
        if (attrValue==null)
            element.removeAttribute(attrName);
        else {
            element.setAttribute(attrName,attrValue);
        }
        return true;
    }

    /** This method deletes the element specified by elementPath from the DOM tree
     * @param root Root element of the DOM graph 
     * @param elementPath List of ElementAttrInfo that defines the element path to the target element
     * @return true if method was successful, false otherwise
     */    
    public static boolean deleteElement (Element root, List<ElementAttrInfo> elementPath)
    throws org.w3c.dom.DOMException {
        if (elementPath.isEmpty()) return false;
        Iterator<ElementAttrInfo> it = elementPath.iterator();
        Element parent = null;
        Element element = root;
        while (it.hasNext()){
            ElementAttrInfo info = it.next();
            String attributeName = info.getAttributeName();
            String attributeValue = info.getAttributeValue();
            NodeList nodeList = element.getElementsByTagName(info.getElementName());
            if (nodeList.getLength()==0) return false;
            Element newElement=null;
            if (attributeName==null) { // there is only one element
                newElement = (Element)nodeList.item(0);
            } else { // element need to be found by element value
                for (int i=0;i<nodeList.getLength();i++){
                    Element el = (Element)nodeList.item(i);
                    String name = el.getAttribute(attributeName);
                    if (name!=null && name.equals(attributeValue)){
                        newElement = el;
                        break;
                    }
                }
            }
            if (newElement==null) return false;
            else {
                parent=element;
                element=newElement;
            }
        }
        // removing the previous text element and test if it is the first element in parent
        Node previous = element.getPreviousSibling();
        boolean firstElement=false;
        if (previous==null) firstElement=true;
        else if (previous instanceof CharacterData) {
            if (previous.getPreviousSibling()==null) firstElement=true;
            parent.removeChild(previous);
        }
        // removing the next text element (only if this element was the last non text element in parent)
        if (firstElement) {
            Node next = element.getNextSibling();
            if (next instanceof CharacterData && next.getNextSibling() == null)
                     parent.removeChild(next);        
        }
     
        parent.removeChild(element);
        
        return true;
    }

    /** This method adds the element specified by elementName and element attributes into the element
     * specified by the elementPath
     * @param root Root element of the DOM graph 
     * @param elementPath List of ElementAttrInfo that defines the element path to the target element
     * @param elementName name of the new Element
     * @param keyAttribute key Attribute of the new Element
     * @param attrNames names of the attributes that will be initialized
     * @param attrValues initial values for attrNames
     * @return true if method was successful, false otherwise
     */ 
    public static boolean addElement (Element root, List<ElementAttrInfo> elementPath, String elementName, String keyAttribute, String[] attrNames, String[] attrValues){
        Iterator<ElementAttrInfo> it = elementPath.iterator();
        Element element = root;
        while (it.hasNext()){
            ElementAttrInfo info = it.next();
            String attributeName = info.getAttributeName();
            String attributeValue = info.getAttributeValue();
            NodeList nodeList = element.getElementsByTagName(info.getElementName());
            if (nodeList.getLength()==0) return false;
            Element newElement=null;
            if (attributeName==null) { // there is only one element
                newElement = (Element)nodeList.item(0);
            } else { // element need to be found by element value
                for (int i=0;i<nodeList.getLength();i++){
                    Element el = (Element)nodeList.item(i);
                    String name = el.getAttribute(attributeName);
                    if (name!=null && name.equals(attributeValue)){
                        newElement = el;
                        break;
                    }
                }
            }
            if (newElement==null) return false;            
            else element=newElement;
        }
        
        // creating new newElement
        org.w3c.dom.Document doc = root.getOwnerDocument();
        Element newElement = doc.createElement(elementName);
        // adding list of the attributes
        for (int i=0;i<attrNames.length;i++)
            newElement.setAttribute(attrNames[i],attrValues[i]);
        
        // test if there is no such an element already
        if (keyAttribute!=null) {
            String newKeyValue = newElement.getAttribute(keyAttribute);
            if (newKeyValue!=null) {
                NodeList nodeList = element.getElementsByTagName(elementName);
                if (nodeList!=null) {
                    for (int i=0;i<nodeList.getLength();i++){
                        if (newKeyValue.equals(((Element)nodeList.item(i)).getAttribute(keyAttribute))){
                            showDialog(elementName,keyAttribute,newKeyValue);
                            return false;
                        }
                    }
                }
            }
        }
        
        // appending newElement into DOM graph
        
        if (!element.hasChildNodes()) { //element contains no elements until now
            element.appendChild(doc.createTextNode(getIndentBefore(elementPath.size())));
            element.appendChild(newElement);
            element.appendChild(doc.createTextNode(getIndentAfter(elementPath.size())));
        } else {
            NodeList list = element.getElementsByTagName(elementName);
            if (list.getLength()==0) { // that will be the first Element
                Node lastChild = element.getLastChild();
                if (lastChild instanceof CharacterData) {
                    element.appendChild(doc.createTextNode("  "));  // NOI18N
                    element.appendChild(newElement);                    
                    //Node textEl = (CharacterData)lastChild.cloneNode(false);
                    element.appendChild(lastChild.cloneNode(false));
                } else {
                    element.appendChild(newElement);
                }
            } else { // new Element will be added to other Elements
                Node lastInList = list.item(list.getLength()-1);
                Node previous = lastInList.getPreviousSibling();
                Node next = lastInList.getNextSibling();
                if (next != null) {
                    if (previous instanceof CharacterData)
                        element.insertBefore(previous.cloneNode(false),next);
                    element.insertBefore(newElement,next);
                } else {
                    if (previous instanceof CharacterData)
                        element.appendChild(previous.cloneNode(false));
                    element.appendChild(newElement);
                }
            }
        }
        return true;
    }
    
    /** This method adds the element specified by elementName and elementValue into the element
     * specified by the elementPath
     * @param root Root element of the DOM graph 
     * @param elementPath List of ElementAttrInfo that defines the element path to the target element
     * @param elementName name of the new Element
     * @param elementValue value of the new Element
     * @return true if method was successful, false otherwise
     */ 
    public static boolean addStringElement (Element root, List<ElementAttrInfo> elementPath, String elementName, String elementValue){
        Iterator<ElementAttrInfo> it = elementPath.iterator();
        Element element = root;
        
        while (it.hasNext()){
            ElementAttrInfo info = it.next();
            String attributeName = info.getAttributeName();
            String attributeValue = info.getAttributeValue();
            NodeList nodeList = element.getElementsByTagName(info.getElementName());
            if (nodeList.getLength()==0) return false;
            Element newElement=null;
            if (attributeName==null) { // there is only one element
                newElement = (Element)nodeList.item(0);
            } else { // element need to be found by element value
                for (int i=0;i<nodeList.getLength();i++){
                    Element el = (Element)nodeList.item(i);
                    String name = el.getAttribute(attributeName);
                    if (name!=null && name.equals(attributeValue)){
                        newElement = el;
                        break;
                    }
                }
            }
            if (newElement==null) return false;            
            else element=newElement;
        }
        
        // creating new newElement
        org.w3c.dom.Document doc = root.getOwnerDocument();
        Element newElement = doc.createElement(elementName);
        newElement.appendChild(doc.createTextNode(elementValue));

        // appending newElement into DOM graph
        
        if (!element.hasChildNodes()) { //element contains no elements until now
            element.appendChild(doc.createTextNode(getIndentBefore(elementPath.size())));
            element.appendChild(newElement);
            element.appendChild(doc.createTextNode(getIndentAfter(elementPath.size())));
        } else {
            NodeList list = element.getElementsByTagName(elementName);
            if (list.getLength()==0) { // that will be the first Element
                Node lastChild = element.getLastChild();
                if (lastChild instanceof CharacterData) {
                    element.appendChild(doc.createTextNode("  "));  // NOI18N
                    element.appendChild(newElement);                    
                    //Node textEl = (CharacterData)lastChild.cloneNode(false);
                    element.appendChild(lastChild.cloneNode(false));
                } else {
                    element.appendChild(newElement);
                }
            } else { // new Element will be added to other Elements
                Node lastInList = list.item(list.getLength()-1);
                Node previous = lastInList.getPreviousSibling();
                Node next = lastInList.getNextSibling();
                if (next != null) {
                    if (previous instanceof CharacterData)
                        element.insertBefore(previous.cloneNode(false),next);
                    element.insertBefore(newElement,next);
                } else {
                    if (previous instanceof CharacterData)
                        element.appendChild(previous.cloneNode(false));
                    element.appendChild(newElement);
                }
            }
        }
        return true;
    }
    
    /** This method deletes the all elements specified by elementName from the DOM tree
     * @param root Root element of the DOM graph 
     * @param elementPath List of ElementAttrInfo that defines the element path to the parent element
     * @param elementName String specifying the elements for deleting 
     * @return true if method was successful, false otherwise
     */    
    public static boolean deleteAllElements (Element root, List<ElementAttrInfo> elementPath, String elementName)
    throws org.w3c.dom.DOMException {
        if (elementPath.isEmpty()) return false;
        Iterator<ElementAttrInfo> it = elementPath.iterator();
        Element parent = null;
        Element element = root;
        while (it.hasNext()){
            ElementAttrInfo info = it.next();
            String attributeName = info.getAttributeName();
            String attributeValue = info.getAttributeValue();
            NodeList nodeList = element.getElementsByTagName(info.getElementName());
            if (nodeList.getLength()==0) return false;
            Element newElement=null;
            if (attributeName==null) { // there is only one element
                newElement = (Element)nodeList.item(0);
            } else { // element need to be found by element value
                for (int i=0;i<nodeList.getLength();i++){
                    Element el = (Element)nodeList.item(i);
                    String name = el.getAttribute(attributeName);
                    if (name!=null && name.equals(attributeValue)){
                        newElement = el;
                        break;
                    }
                }
            }
            if (newElement==null) return false;
            else {
                parent=element;
                element=newElement;
            }
        }
        
        NodeList list = element.getElementsByTagName(elementName);
        if ( list.getLength()==0) return false;
        
        // removing the previous text element and test if it is the first element in parent
        Node beforeFirst = list.item(0).getPreviousSibling();
        boolean firstElement=false;
        if (beforeFirst==null) firstElement=true;
        else if (beforeFirst instanceof CharacterData) {
            if (beforeFirst.getPreviousSibling()==null) firstElement=true;
        }
        // removing the next text element (only if this element was the last non text element in parent)
        if (firstElement) {
            Node next = list.item(list.getLength()-1).getNextSibling();
            if (next instanceof CharacterData && next.getNextSibling() == null)
                     element.removeChild(next);        
        }
        for(int i=list.getLength()-1;i>=0;i--){
            Node item = list.item(i);
            Node previous = item.getPreviousSibling();
            if (previous instanceof CharacterData)
                element.removeChild(previous);
            element.removeChild(item);
        }
        
        return true;
    }
    
    private static void showDialog(String elementName, String attrName, String attrValue){
     String mes = NbBundle.getMessage(XMLJ2eeUtils.class, "TXT_elementExists",
        new Object [] { elementName, attrName, attrValue});
     NotifyDescriptor.Message message = new NotifyDescriptor.Message(mes);
     DialogDisplayer.getDefault().notify(message);
    }
    
    private static String getIndentBefore(int level) {
        StringBuffer sb = new StringBuffer("\n"); //NOI18N
        for (int i=0;i<=level;i++) sb.append("  "); //NOI18N
        return sb.toString();
    }
    private static String getIndentAfter(int level) {
        StringBuffer sb = new StringBuffer("\n"); //NOI18N
        for (int i=0;i<level;i++) sb.append("  "); //NOI18N
        return sb.toString();
    }    
     
}
