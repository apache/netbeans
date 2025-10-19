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
package org.netbeans.modules.spellchecker.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spellchecker.ComponentPeer;
import org.netbeans.modules.spellchecker.spi.dictionary.Dictionary;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;


/**
 *
 * @author Jan Lahoda
 */
public class WordCompletion implements CompletionProvider {
    
    /** Creates a new instance of WordCompletion */
    public WordCompletion() {
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE) {
            return new AsyncCompletionTask(new Query(), component);
        }
        
        return null;
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    private static class Query extends AsyncCompletionQuery {
        
        protected void query(CompletionResultSet resultSet, Document document, final int caretOffset) {
            Dictionary dictionary = ComponentPeer.getDictionary(document);
            final TokenList  l = ComponentPeer.ACCESSOR.lookupTokenList(document);
            
            if (dictionary != null && l != null && document instanceof BaseDocument) {
                final BaseDocument bdoc = (BaseDocument) document;
                final String[] text = new String[2];
                
                document.render(new Runnable() {
                    public void run() {

                        try {
                            int lineStart = LineDocumentUtils.getLineStartOffset(bdoc, caretOffset);
                            
                            l.setStartOffset(lineStart);
                            
                            while (l.nextWord()) {
                                int start = l.getCurrentWordStartOffset();
                                int end   = l.getCurrentWordStartOffset() + l.getCurrentWordText().length();
                                
                                if (start < caretOffset && end >= caretOffset) {
                                    text[0] = l.getCurrentWordText().subSequence(0, caretOffset - start).toString();
                                    text[1] = l.getCurrentWordText().toString();
                                    return ;
                                }
                            }
                        } catch (BadLocationException e) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                });
                
                if (text[0] != null) {
                    int i = 0;
                    for (String proposal : dictionary.findValidWordsForPrefix(text[0])) {
                        resultSet.addItem (new WordCompletionItem (
                            caretOffset - text[0].length (),
                            proposal
                        ));
                        if (i == 8) break;
                        i++;
                    }
                    if (dictionary.validateWord (text [1]) != ValidityType.VALID) {
                        resultSet.addItem (new AddToDictionaryCompletionItem (
                            text [1],
                            true
                        ));
                        resultSet.addItem (new AddToDictionaryCompletionItem (
                            text [1],
                            false
                        ));
                    }
                }
            }
            
            resultSet.finish();
        }
    }
    
}
