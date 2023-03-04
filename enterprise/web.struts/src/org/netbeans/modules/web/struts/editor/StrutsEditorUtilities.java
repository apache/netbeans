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

package org.netbeans.modules.web.struts.editor;


import java.io.IOException;
import java.io.StringWriter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.web.struts.config.model.FormProperty;
import org.netbeans.modules.web.struts.config.model.Forward;
import org.netbeans.modules.web.struts.config.model.StrutsException;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */

public class StrutsEditorUtilities {
    
    /** The constant from XML editor
     */
    protected static int XML_ATTRIBUTE = 5;
    protected static int XML_ELEMENT = 4;
    protected static int XML_ATTRIBUTE_VALUE = 7;
    
    public static String END_LINE = System.getProperty("line.separator");  //NOI18N
    
    
    /** Returns the value of the path attribute, when there is an action
     * definition on the offset possition. Otherwise returns null.
     */
    public static String getActionPath(BaseDocument doc, int offset){
        try {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            // find the element, which is on the offset
            while (token != null
                    && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ELEMENT
                        && !( token.getImage().equals("/>") 
                            || token.getImage().equals(">")
                            || token.getImage().equals("<forward")
                            || token.getImage().equals("<exception")
                            || token.getImage().equals("</action")
                            || token.getImage().equals("<description")
                            || token.getImage().equals("<display-name")
                            || token.getImage().equals("<set-property")
                            || token.getImage().equals("<icon"))))
                token = token.getPrevious();
            if (token != null && token.getImage().equals("<action")){   //NOI18N
                token = token.getNext();
                while (token!= null 
                        && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                        && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE
                        && token.getImage().equals("path")))   // NOI18N
                    token = token.getNext();
                if (token != null && token.getImage().equals("path")){ // NOI18N
                    token = token.getNext();
                    while (token != null 
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE_VALUE    
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE)
                        token = token.getNext();
                    if (token != null && token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE_VALUE){
                        String value = token.getImage().trim();
                        value = value.substring(1);
                        value = value.substring(0, value.length()-1);
                        return value;
                    }
                }
            }   
            return null;
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
    /** Returns the value of the name attribute, when there is an ActionForm Bean
     * definition on the offset possition. Otherwise returns null.
     */
    public static String getActionFormBeanName(BaseDocument doc, int offset){
        try {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            // find the start of element, which is on the offset
            while (token != null
                    && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ELEMENT
                        && !( token.getImage().equals("/>") 
                            || token.getImage().equals(">")
                            || token.getImage().equals("<form-property")
                            || token.getImage().equals("</form-bean")
                            || token.getImage().equals("<description")
                            || token.getImage().equals("<display-name")
                            || token.getImage().equals("<set-property")
                            || token.getImage().equals("<icon"))))
                token = token.getPrevious();
            if (token != null && token.getImage().equals("<form-bean")){   //NOI18N
                token = token.getNext();
                while (token!= null 
                        && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                        && !(token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE
                        && token.getImage().equals("name")))   // NOI18N
                    token = token.getNext();
                if (token != null && token.getImage().equals("name")){ // NOI18N
                    token = token.getNext();
                    while (token != null 
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE_VALUE    
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ELEMENT
                            && token.getTokenID().getNumericID() != StrutsEditorUtilities.XML_ATTRIBUTE)
                        token = token.getNext();
                    if (token != null && token.getTokenID().getNumericID() == StrutsEditorUtilities.XML_ATTRIBUTE_VALUE){
                        String value = token.getImage().trim();
                        value = value.substring(1);
                        value = value.substring(0, value.length()-1);
                        return value;
                    }
                }
            }   
            return null;
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
    public static int writeForwardIntoAction(BaseDocument doc, Forward forward, String actionName)
                    throws IOException{
        return writeElementIntoFather(doc, forward, "action", actionName, "forward");           //NOI18N
    }
    
    public static int writeExceptionIntoAction(BaseDocument doc, StrutsException ex, String actionName)
                    throws IOException{
        return writeElementIntoFather(doc, ex, "action", actionName, "exception");              //NOI18N
    }
    
    public static int writePropertyIntoBean(BaseDocument doc, FormProperty prop, String beanName)
                    throws IOException{
        return writeElementIntoFather(doc, prop, "form-bean", beanName, "form-property");       //NOI18N
    }
    
    private static int writeElementIntoFather(BaseDocument doc, BaseBean bean, 
            String father, String fatherName, String element) throws IOException{
        int possition = -1;
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token =  null;
        try {
            int offset = 0;
            // find the name as an attribute value
            do{
                offset = doc.getText(0, doc.getLength()).indexOf("\""+fatherName+"\"", offset+1);       //NOI18N
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == XML_ATTRIBUTE_VALUE)
                    while ( token != null 
                            && token.getTokenID().getNumericID() != XML_ELEMENT)
                        token = token.getPrevious();
            }
            while (offset > 0 && token != null 
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals("<"+father)));                       //NOI18N
            if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT
                    && token.getImage().equals("<"+father)){                            //NOI18N
                token = token.getNext();
                // find the /> or >
                while (token != null && token.getTokenID().getNumericID() != XML_ELEMENT )
                    token = token.getNext();
                if (token != null && token.getImage().equals("/>")){                    //NOI18N
                    StringBuffer text = new StringBuffer();
                    offset = token.getOffset();
                    text.append(">");                                                   //NOI18N
                    text.append(END_LINE);
                    text.append(addNewLines(bean));
                    text.append(END_LINE);
                    text.append("</"); text.append(father); text.append(">");           //NOI18N
                    Reformat fmt = Reformat.get(doc);
                    fmt.lock();
                    try {
                        doc.atomicLock();
                        try{
                            doc.remove(offset, 2);
                            doc.insertString(offset, text.toString(), null);
                            Position endPos = doc.createPosition(offset + text.length() - 1);
                            fmt.reformat(offset, endPos.getOffset());
                            offset += Math.max(0, endPos.getOffset() - offset);
                            possition = offset;
                        }
                        finally{
                            doc.atomicUnlock();
                        }
                    } finally {
                        fmt.unlock();
                    }
                }
                if (token != null && token.getImage().equals(">")){                     //NOI18N
                    offset = -1;
                    while (token != null 
                            && !(token.getTokenID().getNumericID() == XML_ELEMENT
                            && token.getImage().equals("</"+father))){                  //NOI18N
                        while(token != null
                                && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                && (token.getImage().equals("<"+element)                //NOI18N
                                || token.getImage().equals("</"+father))))              //NOI18N
                            token = token.getNext();
                        if (token != null && token.getImage().equals("<"+element)){     //NOI18N
                            while (token!= null
                                    && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                    && (token.getImage().equals("/>")                   //NOI18N
                                    || token.getImage().equals("</"+element))))         //NOI18N
                                token = token.getNext();
                            if (token != null && token.getImage().equals("</"+element)) //NOI18N
                                while (token!= null
                                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                        && token.getImage().equals(">")))               //NOI18N
                                    token = token.getNext();
                            if (token != null )
                                offset = token.getOffset() + token.getImage().length()-1;
                        }    
                        if (token != null && token.getImage().equals("</"+father) && offset == -1){     //NOI18N
                            while (token!= null
                                    && !(token.getTokenID().getNumericID() == XML_ELEMENT
                                    && (token.getImage().equals("/>")                   //NOI18N
                                    || token.getImage().equals(">"))))                  //NOI18N
                                token = token.getPrevious();
                            offset = token.getOffset()+token.getImage().length()-1;
                        }   
                    }
                    if (offset > 0)
                        possition = writeString(doc, addNewLines(bean), offset);
                }
            }
                
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return possition;
    }
    
    public static int writeBean(BaseDocument doc, BaseBean bean, String element, String section) throws IOException{
        int possition = -1;
        boolean addroot = false;
        boolean addsection = false;
        String sBean = addNewLines(bean);
        StringBuffer appendText = new StringBuffer();
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        int offset;
        try {
            String docText = doc.getText(0, doc.getLength());
            //Check whether there is root element
            String findString = "</" + element;                             //NOI18N
            
            //find index of last definition 
            offset = docText.lastIndexOf(findString);
            if (offset == -1){
                if ((offset = findEndOfElement(doc, "struts-config")) == -1){                 //NOI18N
                    offset = doc.getLength();
                    appendText = new StringBuffer();
                    appendText.append("<struts-config>");                   //NOI18N
                    appendText.append(END_LINE);
                    if (section != null && section.length()>0 ){
                        appendText.append("<"+section+">");                 //NOI18N
                        appendText.append(END_LINE);
                        appendText.append(sBean);
                        appendText.append(END_LINE);
                        appendText.append("</"+section+">");                //NOI18N
                    }
                    else{
                        appendText.append(sBean);
                    }
                    appendText.append(END_LINE);
                    appendText.append("</struts-config>");                  //NOI18N
                    appendText.append(END_LINE);
                    possition = writeString(doc, appendText.toString(), offset);
                }
                else{
                    if (section != null && section.length()>0){
                        int offsetSection;
                        if ((offsetSection = findEndOfElement(doc, section)) == -1){
                            appendText.append("<"+section+">");             //NOI18N
                            appendText.append(END_LINE);
                            appendText.append(sBean);
                            appendText.append(END_LINE);
                            appendText.append("</"+section+">");            //NOI18N
                        }
                        else {
                            appendText.append(sBean);
                            offset = offsetSection;
                        }
                    }
                    else 
                        appendText.append(sBean);
                    token = sup.getTokenChain(offset, offset+1);
                    if (token != null) token = token.getPrevious();
                    while (token != null
                            && !(token.getTokenID().getNumericID() == XML_ELEMENT
                            && token.getImage().equals(">")))               //NOI18N
                        token = token.getPrevious();
                    if (token != null)
                        offset = token.getOffset();
                    possition=writeString(doc, appendText.toString(), offset);
                }
            }
            else{
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT){
                    while (token != null
                            && !(token.getTokenID().getNumericID() == XML_ELEMENT
                            && token.getImage().equals(">")))               //NOI18N
                        token = token.getPrevious();
                    if (token != null)
                        possition = writeString(doc, sBean, token.getOffset());    
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }        
        return possition;
    }
    
    private static String addNewLines(final BaseBean bean) throws IOException {
        StringWriter sWriter = new StringWriter();
        bean.writeNode(sWriter);
        return sWriter.toString().replace("><", ">"+END_LINE+"<");        //NOI18N
    }
    
    private static int writeString(BaseDocument doc, String text, int offset) throws BadLocationException {
        int formatLength = 0;
        Indent indent = Indent.get(doc);
        Reformat fmt = Reformat.get(doc);
        indent.lock();
        try {
            fmt.lock();
            try {
                doc.atomicLock();
                try{
                    offset = indent.indentNewLine(offset + 1);
                    doc.insertString(Math.min(offset, doc.getLength()), text, null );
                    Position endPos = doc.createPosition(offset + text.length() - 1);
                    fmt.reformat(offset, endPos.getOffset());
                    formatLength = Math.max(0, endPos.getOffset() - offset);
                }
                finally{
                    doc.atomicUnlock();
                }
            } finally {
                fmt.unlock();
            }
        } finally {
            indent.unlock();
        }
        return Math.min(offset + formatLength + 1, doc.getLength());
    }
    
    private static int findStartOfElement(BaseDocument doc, String element) throws BadLocationException{
        String docText = doc.getText(0, doc.getLength());
        int offset = doc.getText(0, doc.getLength()).indexOf("<" + element);            //NOI18N
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        while (offset > 0){
           token = sup.getTokenChain(offset, offset+1); 
           if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT)
               return token.getOffset();
           offset = doc.getText(0, doc.getLength()).indexOf("<" + element);             //NOI18N
        }
        return -1;
    }
    
    private static int findEndOfElement(BaseDocument doc, String element) throws BadLocationException{
        String docText = doc.getText(0, doc.getLength());
        int offset = doc.getText(0, doc.getLength()).indexOf("</" + element);           //NOI18N
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        while (offset > 0){
           token = sup.getTokenChain(offset, offset+1); 
           if (token != null && token.getTokenID().getNumericID() == XML_ELEMENT){
                offset = token.getOffset();
                token = token.getNext();
                while (token != null
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals(">")))               //NOI18N
                    token = token.getNext();
                if (token != null)
                    offset = token.getOffset();
               return offset;
           }
           offset = doc.getText(0, doc.getLength()).indexOf("</" + element);            //NOI18N
        }
        return -1;
    }
    
}
