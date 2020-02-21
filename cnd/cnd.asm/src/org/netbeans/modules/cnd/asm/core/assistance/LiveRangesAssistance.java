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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.model.AsmState;
import org.netbeans.modules.cnd.asm.model.lang.AsmOffsetable;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.lang.Register;


public class LiveRangesAssistance implements AsmModelAccessor.ParseListener,
                                             CaretListener,
                                             RegisterChooserListener    {
      
    private final CodeAnnotationSidebar sideBar;    
    private final LiveRangesAction action;
    private LiveRangesAccessor accessor;
    private RegisterChooser lastChooser;
    
    private AsmOffsetable lastRangeStart;
    private AsmOffsetable lastRangeEnd;
    
    private final BaseDocument doc;
    private final JEditorPane pane;
    private final EditorCookie cookie;
        
    public LiveRangesAssistance(BaseDocument doc) {                                
        this.doc = doc;                                       
        
        DataObject obj = NbEditorUtilities.getDataObject(doc);          
        cookie = obj.getCookie(EditorCookie.class);                
        
        pane = cookie.getOpenedPanes()[0];        
        pane.addCaretListener(this);                            
        action = new LiveRangesAction();
        
        sideBar = (CodeAnnotationSidebar) 
                pane.getClientProperty(LiveRangeSidebarFactory.LIVE_RANGE_SIDEBAR);
           
        AsmModelAccessor acc = AsmObjectUtilities.getAccessor(doc);
        if (acc == null) {
            return;
        }
        
        acc.addParseListener(this); 
    }    

    
    public void notifyParsed() {
        AsmState state = AsmObjectUtilities.getAccessor(doc).getState();
        accessor = action.calculateRanges(state); 
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                calcLiveRanges(getCaretPosition());                
            }
        });
    }
    
    public void update(RegisterChooser ch) {
        lastChooser = ch;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                calcLiveRanges(getCaretPosition());                
            }
        });
    }
    
    private int getCaretPosition() {
        return pane.getCaretPosition();
    }        
    
    private void addAnnotation(AsmOffsetable start, AsmOffsetable end,
                               List<CodeAnnotationSidebar.AnnotationEntry> res) {
        try {
            int lnStart = Utilities.getLineOffset(doc, start.getStartOffset() + 1);
            int lnEnd = Utilities.getLineOffset(doc, end.getStartOffset() + 1);

            res.add(new CodeAnnotationSidebar.AnnotationEntry(java.awt.Color.GREEN, 
                                                              lnStart + 1, lnEnd + 1));
        } catch (BadLocationException ex) {
            // nothing
        }
    }
    
    private void calcLiveRanges(int curPos){                                               
        
        if (lastChooser == null)
             return;
        
        List<CodeAnnotationSidebar.AnnotationEntry> res = 
               new LinkedList<CodeAnnotationSidebar.AnnotationEntry>();
              
        AsmState state = AsmObjectUtilities.getAccessor(doc).getState();
        
        if (accessor == null) {            
            accessor = action.calculateRanges(state); 
        }                   
                       
        for (Register reg: lastChooser.getRegisters()) {
            lastRangeStart = lastRangeEnd = null;
            
            List<Integer> ranges = accessor.getRangesForRegister(reg);
            for (Iterator<Integer> iter = ranges.iterator(); iter.hasNext();) {
                AsmOffsetable offStart = state.getElements().
                                            getCompounds().get(iter.next());
                AsmOffsetable offEnd = state.getElements().
                                            getCompounds().get(iter.next());

                addAnnotation(offStart, offEnd, res);                
            }
        }   
         
        sideBar.setAnnotations(res);
    }               

    private boolean isInRange(AsmOffsetable offStart, AsmOffsetable offEnd, 
                               int pos){
        return isInRange(offStart.getStartOffset(), offEnd.getStartOffset(),
                         pos);
    }
   
    private boolean isInRange(int start, int end, int pos) {
        return start <= pos && pos < end; 
    }
           
    public void caretUpdate(CaretEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int curPos = getCaretPosition();
                if (lastRangeStart != null && lastRangeEnd != null &&
                     isInRange(lastRangeStart, lastRangeEnd, curPos)) {
                        return;
                 }
                
                calcLiveRanges(curPos);                
            }
        });
    }
}
