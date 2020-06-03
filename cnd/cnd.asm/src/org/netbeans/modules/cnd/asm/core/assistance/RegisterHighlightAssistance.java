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

package org.netbeans.modules.cnd.asm.core.assistance;

import java.awt.Color;
import java.util.Iterator;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.lang.OperandElement;
import org.netbeans.modules.cnd.asm.model.util.IntervalSet;
import org.netbeans.modules.cnd.asm.core.assistance.RegisterHighlightAction.HighlightEntry;
import org.netbeans.modules.cnd.asm.core.ui.top.RegisterUsagesPanel;


public class RegisterHighlightAssistance extends AbstractHighlightsContainer  
                        implements CaretListener, AsmModelAccessor.ParseListener {

    public static final String LAYER_NAME = "reg-highlight-layer"; // NOI18N
    
    public static final Color READ_COLORING = RegisterUsagesPanel.READ_COLOR;
    public static final Color WRITE_COLORING = RegisterUsagesPanel.WRITE_COLOR;

    private final JTextComponent pane;
  
    private IntervalSet<HighlightEntry> highlight = new IntervalSet<HighlightEntry>();
    
    private final RegisterHighlightAction action = new RegisterHighlightAction();

    public RegisterHighlightAssistance(JTextComponent pane, Document doc) {                     
        this.pane = pane;
        
        pane.addCaretListener(this);                                               
        
        AsmModelAccessor acc = AsmObjectUtilities.getAccessor(doc);
        if (acc == null) {
            return;
        }        
        acc.addParseListener(this); 
    }

    private void update() {
        AsmModelAccessor acc = AsmObjectUtilities.getAccessor(pane);
        if (acc == null || pane.getCaret() == null)  {
            return;
        }  
        
        IntervalSet<HighlightEntry> newHighlight = action.getHighlight(acc.getState(), pane.getCaretPosition());
        
        if (newHighlight.isEmpty() && highlight.isEmpty()) {
            return;
        }
        
        // Determining update bounds (max of new and old)
        int start = 0, end = 0;
        if (newHighlight.isEmpty()) {
            start = highlight.getLowerBound();
            end = highlight.getUpperBound();
        } else if (highlight.isEmpty()) {
            start = newHighlight.getLowerBound();
            end = newHighlight.getUpperBound();
        } else {
            start = Math.min(highlight.getLowerBound(), 
                             newHighlight.getLowerBound());
            end = Math.max(highlight.getUpperBound(), 
                             newHighlight.getUpperBound());
        }
          
        highlight = newHighlight;
        
        super.fireHighlightsChange(start, end);        
    }
      
    public void caretUpdate(CaretEvent e) {
        update();
    }

    public void notifyParsed() {
        update();
    }
    
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {                
        return new RegisterHighlightsSequence(
                    highlight.getFromBounds(startOffset, endOffset));
    }
       
    private static class RegisterHighlightsSequence implements HighlightsSequence {
        private HighlightEntry cur;
        private final Iterator<HighlightEntry> it;
                
        public RegisterHighlightsSequence(IntervalSet<HighlightEntry> acc) {
            it = acc.iterator();           
        }
        
        public boolean moveNext() {            
            if (!it.hasNext()) {
                return false;
            }
            
            cur = it.next();
            
            return true;
        }

        public int getStartOffset() {
            return cur.getStartOffset();
        }

        public int getEndOffset() {
            return cur.getEndOffset();
        }

        public AttributeSet getAttributes() {
            Color res = null;
            if (cur.getUsage().contains(OperandElement.Usage.OP_USE_WRITE)) {
                res = WRITE_COLORING;
            } else if (cur.getUsage().contains(OperandElement.Usage.OP_USE_READ)) {
                res = READ_COLORING;
            }
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            if (res != null) {
                attrs.addAttribute(StyleConstants.Background, res);
            }
            
            return attrs;
        }        
    }      
}
