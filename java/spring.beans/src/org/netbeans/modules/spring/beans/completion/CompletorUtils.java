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
