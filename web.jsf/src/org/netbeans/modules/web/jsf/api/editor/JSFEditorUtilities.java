/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.jsf.api.editor;


import java.io.IOException;
import java.io.StringWriter;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Task;

/**
 *
 * @author Petr Pisl, Po-Ting Wu
 */

public class JSFEditorUtilities {
    
    /** The constant from XML editor
     */
    // The constant are taken from class org.netbeans.modules.xml.text.syntax.XMLTokenIDs
    public  final static int XML_ELEMENT = 4;
    public  final static int XML_TEXT = 1;
    public final static String END_LINE = System.getProperty("line.separator");  //NOI18N
    
    /** Returns the value of from-view-id element of navigation rule definition on the offset possition.
     *  If there is not the navigation rule definition on the offset, then returns null. 
     */
    public static String getNavigationRule(BaseDocument doc, int offset){
        try {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token = sup.getTokenChain(offset, offset+1);
            // find the srart of the navigation rule definition
            while (token != null
                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                    && (token.getImage().equals("<navigation-rule")
                    || token.getImage().equals("<managed-bean"))))
                token = token.getPrevious();
            if (token != null && token.getImage().equals("<navigation-rule")){
                // find the from-view-ide element
                while (token != null
                        && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                        && (token.getImage().equals("</navigation-rule")
                        || token.getImage().equals("<from-view-id"))))
                    token = token.getNext();
                if (token!= null && token.getImage().equals("<from-view-id")){
                    token = token.getNext();
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && token.getImage().equals(">")))
                        token = token.getNext();
                    while (token != null
                            && token.getTokenID().getNumericID() != JSFEditorUtilities.XML_TEXT
                            && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT)
                        token = token.getNext();
                    if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT)
                        return token.getImage().trim();
                }
            }
        } 
        catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
    public static int[] getNavigationRuleDefinition(BaseDocument doc, String ruleName){
        try{
            String text = doc.getText(0, doc.getLength());
            //find first possition of text that is the ruleName
            int offset = text.indexOf(ruleName);
            int start = 0;
            int end = 0;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token;
            
            while (offset != -1){
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT){
                    // find first xml element before the ruleName
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && !token.getImage().equals(">")))
                        token = token.getPrevious();
                    // is it the rule definition?
                    if (token != null && token.getImage().equals("<from-view-id")){
                        // find start of the rule definition
                        while (token != null
                                && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                && token.getImage().equals("<navigation-rule")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<navigation-rule")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            // find the end of the rule definition
                            while (token != null
                                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                    && token.getImage().equals("</navigation-rule")))
                                token = token.getNext();
                            if (token!=null && token.getImage().equals("</navigation-rule")){
                                while (token != null
                                        && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                        && token.getImage().equals(">")))
                                    token = token.getNext();
                                if (token!=null && token.getImage().equals(">")){
                                    end = token.getOffset()+1;
                                    return new int[]{start, end};
                                }
                            }
                            return new int[]{start, text.length()};
                        }
                    }
                }
                offset = text.indexOf(ruleName, offset+ruleName.length());
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } 
        return new int []{-1,-1};
    }
    
    /* Returns offset, where starts the definition of the manage bean
     **/
    public static int[] getManagedBeanDefinition(BaseDocument doc, String byElement, String content){
        try{
            String text = doc.getText(0, doc.getLength());
            int offset = text.indexOf(content);
            int start = 0;
            int end = 0;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token;
            
            while (offset != -1){
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT){
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && !token.getImage().equals(">")))
                        token = token.getPrevious();
                    if (token != null && token.getImage().equals("<" + byElement)){    //NOI18N
                        while (token != null
                                && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                && token.getImage().equals("<managed-bean")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<managed-bean")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            while (token != null
                                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                    && token.getImage().equals("</managed-bean")))
                                token = token.getNext();
                            if (token!=null && token.getImage().equals("</managed-bean")){
                                while (token != null
                                        && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                        && token.getImage().equals(">")))
                                    token = token.getNext();
                                if (token!=null && token.getImage().equals(">")){
                                    end = token.getOffset()+1;
                                    return new int[]{start, end};
                                }
                            }
                            return new int[]{start, text.length()};
                        }
                    }
                }
                offset = text.indexOf(content, offset+content.length());
            }
        }
        catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } 
        return new int []{-1,-1};
    }
    
    public static int[] getConverterDefinition(BaseDocument doc, String byElement, String content){
        try{
            String text = doc.getText(0, doc.getLength());
            //find first possition of text that is the ruleName
            int offset = text.indexOf(content);
            int start = 0;
            int end = 0;
            ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
            TokenItem token;
            
            while (offset != -1){
                token = sup.getTokenChain(offset, offset+1);
                if (token != null && token.getTokenID().getNumericID() == JSFEditorUtilities.XML_TEXT){
                    // find first xml element before the ruleName
                    while (token!=null 
                            && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                            && !token.getImage().equals(">")))
                        token = token.getPrevious();
                    // is it the rule definition?
                    if (token != null && token.getImage().equals("<" + byElement)){
                        // find start of the rule definition
                        while (token != null
                                && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                && token.getImage().equals("<converter")))
                            token = token.getPrevious();
                        if(token != null && token.getImage().equals("<converter")){
                            start = token.getOffset();
                            token = sup.getTokenChain(offset, offset+1);
                            // find the end of the rule definition
                            while (token != null
                                    && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                    && token.getImage().equals("</converter")))
                                token = token.getNext();
                            if (token!=null && token.getImage().equals("</converter")){
                                while (token != null
                                        && !(token.getTokenID().getNumericID() == JSFEditorUtilities.XML_ELEMENT
                                        && token.getImage().equals(">")))
                                    token = token.getNext();
                                if (token!=null && token.getImage().equals(">")){
                                    end = token.getOffset()+1;
                                    return new int[]{start, end};
                                }
                            }
                            return new int[]{start, text.length()};
                        }
                    }
                }
                offset = text.indexOf(content, offset+content.length());
            }
            
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } 
        return new int []{-1,-1};
    }
    

    /** Writes new bean to the document directly under <faces-config> element
     */
    public static int writeBean(BaseDocument doc, BaseBean bean, String element) throws IOException{
        String sBean = addNewLines(bean);
        int possition = -1;
        ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token;
        try {
            String docText = doc.getText(0, doc.getLength());
            //Check whether there is root element
            if (docText.indexOf("<faces-config") == -1){                //NOI18N
                doc.insertString(doc.getLength(), "<faces-config>"      //NOI18N
                        + END_LINE + "</faces-config>", null);          //NOI18N       
                docText = doc.getText(0, doc.getLength());
            }
            String findString = "</" + element;
            
            //find index of last definition 
            int offset = docText.lastIndexOf(findString);
            if (offset == -1)
                offset = docText.length() - 2;
            token = sup.getTokenChain(offset, offset+1);
            if (offset < (docText.length() - 2)
                    && token != null && token.getTokenID().getNumericID() == XML_ELEMENT){
                while (token != null
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals(">")))               //NOI18N
                    token = token.getNext();
                if (token != null)
                    possition = writeString(doc, sBean, token.getOffset());    
            }
            else {
                // write to end
                if (token != null && token.getImage().equals(">"))      //NOI18N
                    token = token.getPrevious();
                while (token != null
                        && !(token.getTokenID().getNumericID() == XML_ELEMENT
                        && token.getImage().equals(">")))               //NOI18N
                    token = token.getPrevious();
                if (token != null)
                    possition = writeString(doc, sBean, token.getOffset());    
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return possition;
    }
    
    private static String addNewLines(final BaseBean bean) throws IOException {
        StringWriter sWriter = new StringWriter();
        bean.writeNode(sWriter);
        String sBean = sWriter.toString();
        sBean = sBean.replaceAll("><", ">"+END_LINE+"<");               //NOI18N
        return sBean;
    }
    
    private static int writeString(BaseDocument doc, String text, int offset){
        int formatLength = 0;
        Indent indenter = Indent.get(doc);
        Reformat formatter = Reformat.get(doc);
        indenter.lock();
        formatter.lock();
        try {
            doc.atomicLock();
            try{
                offset = indenter.indentNewLine(offset + 1);
                doc.insertString(offset, text, null );
                Position endPos = doc.createPosition(offset + text.length() - 1);
                formatter.reformat(offset, endPos.getOffset());
                formatLength = Math.max(0, endPos.getOffset() - offset);
            }
            catch(BadLocationException ex){
                Exceptions.printStackTrace(ex);
            }
            finally {
                doc.atomicUnlock();
            }
        } finally {
            formatter.unlock();
            indenter.unlock();
        }
        return offset + formatLength + 1;
    }
    
    /**
     * Method that allows to find its
     * CloneableEditorSupport from given DataObject
     * @return the support or null if the CloneableEditorSupport 
     * was not found
     * This method is hot fix for issue #53309
     * this methd was copy/pasted from OpenSupport.Env class
     * @param dob an instance of DataObject
     */
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Node.Cookie obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }
    
    private static class CreateXMLPane implements Runnable{
        JEditorPane ep;

        public void run (){
            ep = new JEditorPane("text/xml", "");
        }

        public JEditorPane getPane (){
            return ep;
        }
    }
    
    /** This method returns a BaseDocument for the configuration file. If the configuration
     *  file is not opened, then the document is not created yet and this method push to load 
     *  the document to the memory. 
     */
    public static BaseDocument getBaseDocument(DataObject dataObject){
        BaseDocument document = null;
        
        if (dataObject != null){
            synchronized (dataObject){
                EditorCookie editor = dataObject.getLookup().lookup(EditorCookie.class);
                if (editor != null){
                    document = (BaseDocument)editor.getDocument();
                    if (document == null){
                        Task preparing = editor.prepareDocument();
                        preparing.waitFinished();
                        document = (BaseDocument)editor.getDocument();
                    }
                }
            }
        }
        return document;
    }
}
