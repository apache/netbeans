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

package org.netbeans.modules.spellchecker.spi.language.support;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.util.ChangeSupport;

/**
 *
 * @author lahvac
 */
public class MultiTokenList {
    
    public static TokenList create(List<TokenList> l) {
        return new MultiTokenListImpl(l);
    }

    private static final class MultiTokenListImpl implements TokenList, ChangeListener {

        private List<TokenList> delegateTo;
        private List<Boolean> moved;
        
        private int currentOffset;
        private CharSequence currentWord;
        
        private ChangeSupport cs = new ChangeSupport(this);

        public MultiTokenListImpl(List<TokenList> delegateTo) {
            this.delegateTo = delegateTo;
            this.moved = new ArrayList<Boolean>(delegateTo.size());
            
            for (TokenList l : delegateTo) {
                l.addChangeListener(this);
            }
        }
    
        public void setStartOffset(int offset) {
            moved.clear();
            
            for (TokenList l : delegateTo) {
                l.setStartOffset(offset);
                moved.add(l.nextWord());
            }
        }

        public boolean nextWord() {
            TokenList first = null;
            int firstOffset = Integer.MAX_VALUE;
            int firstIndex = 0;
            int index = 0;
            
            for (TokenList l : delegateTo) {
                if (moved.get(index)) {
                    if (l.getCurrentWordStartOffset() < firstOffset) {
                        firstOffset = l.getCurrentWordStartOffset();
                        first = l;
                        firstIndex = index;
                    }
                }
                
                index++;
            }
            
            if (first != null) {
                currentOffset = firstOffset;
                currentWord = first.getCurrentWordText();
                
                moved.set(firstIndex, first.nextWord());
                return true;
            } else {
                return false;
            }
        }

        public int getCurrentWordStartOffset() {
            return currentOffset;
        }

        public CharSequence getCurrentWordText() {
            return currentWord;
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }
        
    }
}
