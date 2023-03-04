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

package org.netbeans.modules.web.core.syntax;

/**
 *
 * @author Petr Pisl, Marek Fukala
 */


import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.text.Line;
import org.openide.text.Line.Set;
import org.openide.text.NbDocument;

public class JspParserErrorAnnotation extends ErrorAnnotationImpl.LineSetAnnotation {
    
    /** Document line where the bug is reported
     */
    private Line docline;
    /** Line and column, where the bug is reported
     *
     */
    private final int line,column;
    /** The description of the error.
     */
    private final String error;
    /** The document, where the error is.
     */
    private NbEditorDocument document;
    /** Creates a new instance of JspParserErrorAnnotation */
    public JspParserErrorAnnotation(int line, int column, String error, NbEditorDocument document) {
        this.line = line;
        this.column = column;
        this.error = error;
        this.document = document;
    }
    
    public String getShortDescription() {
        // Localize this with NbBundle:
        return error;
    }
    
    public int getLine(){
        return line;
    }
    
    public int getColumn(){
        return column;
    }
    
    public String getError(){
        return error;
    }
    
    public String getAnnotationType() {
        return "org-netbeans-modules-web-core-syntax-JspParserErrorAnnotation"; //NOI18N
    }
    
    public void attachToLineSet(Set lines) {
        char string[];
        int start,end;
        Line.Part part;
        
        try {
            docline=lines.getCurrent(line-1);
        } catch (IndexOutOfBoundsException ex) {
            // the document has been changed and the line is deleted
            return;
        }
        
        String annTxt = docline.getText(); // text on the line
        if (annTxt == null) return; // document is already closed
        
        int offset = NbDocument.findLineOffset(document, docline.getLineNumber()) + column+1;  // offset, where the bug is reported
        start = 0;  // column, where the underlining starts on the line, where the bug should be attached. default first column
        string = annTxt.toCharArray();
        end = string.length - 1; // length of the underlining
        
        // when the error is reported outside the page, underline the first line
        if (offset < 1){
            textOnLine(docline);
            return;
        }
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(document);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(offset - 1);
        if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
            //no token
            textOnLine(docline);
            return ;
        }
        start = NbDocument.findLineColumn(document, tokenSequence.token().offset(tokenHierarchy));
        offset = tokenSequence.token().offset(tokenHierarchy);
        
        // Find the start and the end of the appropriate tag or EL
        if (tokenSequence.token().id() != JspTokenId.EL){
            // the error is in the tag or directive
            // find the start of the tag, directive
            while (!(tokenSequence.token().id() == JspTokenId.SYMBOL
                    && tokenSequence.token().text().toString().charAt(0) == '<' //directive
                    || tokenSequence.token().id() == JspTokenId.TAG)  //or jsp tag
                    && tokenSequence.movePrevious()) {
                start = NbDocument.findLineColumn(document, tokenSequence.token().offset(tokenHierarchy));
                offset = tokenSequence.token().offset(tokenHierarchy);
            }
            
            // find the end of the tag or directive
            while ((tokenSequence.token().id() != JspTokenId.SYMBOL
                    || tokenSequence.token().text().toString().trim().length() > 0 
                    && tokenSequence.token().text().toString().charAt(tokenSequence.token().text().toString().trim().length()-1) != '>')
                    && tokenSequence.moveNext());
        } else {
            // The error is in EL - start and offset are set properly - we have one big EL token now in JspLexer
        }
        
        end = tokenSequence.token().offset(tokenHierarchy) + tokenSequence.token().length() - offset;
        
//            if (token != null)
//                end = token.getOffset() + token.getImage().trim().length() - offset;
//            else {
//                while (end >= 0 && end > start && string[end] != ' ') {
//                    end--;
//                }
//            }
        
        part=docline.createPart(start, end);//token.getImage().length());
        attach(part);
    }
    
    private void textOnLine(Line docline){
        int start = 0;  // column, where the underlining starts on the line, where the bug should be attached. default first column
        char string[] = docline.getText().toCharArray();
        int end = string.length - 1; // length of the underlining
        Line.Part part;
        
        while (start<=end && string[start]<=' ') {
            start++;
        }
        while (start<=end && string[end]<=' ') {
            end--;
        }
        if (start<=end)
            part=docline.createPart(start,end-start+1);
        else
            part=docline.createPart(0,string.length);
        attach(part);
        return;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof JspParserErrorAnnotation) {
            JspParserErrorAnnotation ann=(JspParserErrorAnnotation)obj;
            
            if (this==obj)
                return true;
            if (line!=ann.getLine())
                return false;
            if (column!=ann.getColumn())
                return false;
            if (!error.equals(ann.getError()))
                return false;
            /*if (getState()==STATE_DETACHED || ann.getState()==STATE_DETACHED)
                return false;*/
            return true;
        }
        return false;
    }
}
