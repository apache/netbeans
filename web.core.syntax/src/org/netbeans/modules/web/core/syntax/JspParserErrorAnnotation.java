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
