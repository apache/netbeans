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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
