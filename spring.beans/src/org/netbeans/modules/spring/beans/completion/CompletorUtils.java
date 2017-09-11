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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade
 */
public final class CompletorUtils {

    private CompletorUtils() {
    }
    
    public interface Acceptor {
        boolean accept(String str);
    }
    
    public static final Acceptor JAVA_IDENTIFIER_ACCEPTOR = new Acceptor() {

        public boolean accept(String str) {
            for(char c : str.toCharArray()) {
                if(!Character.isJavaIdentifierPart(c)) {
                    return false;
                }
            }
            
            return true;
        }
    };
    
    public static final Acceptor CHARACTER_STRING_ACCEPTOR = new Acceptor() {

        public boolean accept(String str) {
            for(char c : str.toCharArray()) {
                if(!Character.isLetter(c)) {
                    return false;
                }
            }
            
            return true;
        }
    };
    
    public static final Acceptor BEAN_NAME_ACCEPTOR = new Acceptor() {

        public boolean accept(String str) {
            for(char c : str.toCharArray()) {
                if(!Character.isJavaIdentifierPart(c)) { // XXX : need to clarify the exact acceptable stuff
                    return false;
                }
            }
            
            return true;
        }
    };
    
    public static final Acceptor RESOURCE_PATH_ELEMENT_ACCEPTOR = new Acceptor() {

        public boolean accept(String str) {
            if(str.contains("/")) { // NOI18N
                return false;
            }
            
            return true;
        }
    };
    
    public static final Acceptor P_ATTRIBUTE_ACCEPTOR = new Acceptor() {

        public boolean accept(String str) {
            for(char c : str.toCharArray()) {
                if(Character.isWhitespace(c)) {
                    return false;
                }
                
                if(!Character.isLetter(c) && !Character.isDigit(c) && c != '-' && c != '_' && c != ':') {
                    return false;
                }
            }
            
            return true;
        }
        
    };
    
    public static boolean canFilter(Document doc, int invocationOffset, int caretOffset, int anchorOffset, Acceptor acceptor) {
        if(anchorOffset == -1 || caretOffset < invocationOffset) {
            return false;
        }
        
        try {
            String prefix = doc.getText(anchorOffset, caretOffset - anchorOffset);
            if(acceptor.accept(prefix)) {
                return true;
            } else {
                return false;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    public static List<SpringXMLConfigCompletionItem> filter(List<SpringXMLConfigCompletionItem> cachedItems, 
            Document doc, int invocationOffset, int caretOffset, int anchorOffset) {
        if(anchorOffset == -1) {
            return Collections.emptyList();
        }
        
        List<SpringXMLConfigCompletionItem> filteredItems = new ArrayList<SpringXMLConfigCompletionItem>();
        
        try {
            String prefix = doc.getText(anchorOffset, caretOffset - anchorOffset);
            for (SpringXMLConfigCompletionItem item : cachedItems) {
                if (item.getInsertPrefix().toString().regionMatches(!OptionCodeCompletionSettings.isCaseSensitive(), 0, prefix, 0, caretOffset - anchorOffset)) {
                    filteredItems.add(item);
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            filteredItems = Collections.<SpringXMLConfigCompletionItem>emptyList();
        }
        
        return filteredItems;
    }
}
