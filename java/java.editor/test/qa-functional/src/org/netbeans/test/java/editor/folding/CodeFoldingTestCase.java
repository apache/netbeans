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

package org.netbeans.test.java.editor.folding;

import java.awt.event.KeyEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.test.java.editor.lib.EditorTestCase;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * Basic Code Folding Test class. 
 * It contains basic folding functionality methods.
 * 
 *
 * @author Martin Roskanin
 */
  public class CodeFoldingTestCase extends EditorTestCase {
      
    private final int COLLAPSE_EXPAND_DELAY = 200;  
      
    /** Creates a new instance of Main */
    public CodeFoldingTestCase(String testMethodName) {
        super(testMethodName);
    }

    protected static void appendSpaces(StringBuffer sb, int spaces) {
        while (--spaces >= 0) {
            sb.append(' ');
        }
    }
    
    protected static String foldToStringChildren(Fold fold, int indent) {
        indent += 4;
        StringBuffer sb = new StringBuffer();
        String foldTxt = fold.toString();

        //removing hash from the string
        int startOfHash = foldTxt.indexOf("hash=0x");
        int endOfHash = foldTxt.indexOf(",", startOfHash);
        foldTxt = foldTxt.replace("E0", "E1");
        foldTxt = foldTxt.replace("C0", "C1");
        String foldTxtCorrected = foldTxt.substring(0,startOfHash)+foldTxt.substring(endOfHash);
        sb.append(foldTxtCorrected);
        sb.append('\n');
        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            appendSpaces(sb, indent);
            sb.append('[');
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(foldToStringChildren(fold.getFold(i), indent));
        }
        
        return sb.toString();
    }

    
    public static String foldHierarchyToString(JTextComponent target){
        String ret = "";
        AbstractDocument adoc = (AbstractDocument)target.getDocument();

        // Dump fold hierarchy
        FoldHierarchy hierarchy = FoldHierarchy.get(target);
        adoc.readLock();
        try {
            hierarchy.lock();
            try {
                Fold root = hierarchy.getRootFold();
                ret = (root == null) ? "root is null" : foldToStringChildren(root, 0); //NOI18N
            } finally {
                hierarchy.unlock();
            }
        } finally {
            adoc.readUnlock();
        }
        return ret;
    }
    
    protected void waitForFolding(JTextComponent target, int maxMiliSeconds){
        //wait for parser and folding hierarchy creation
        int time = (int) maxMiliSeconds / 100;
        
        AbstractDocument adoc = (AbstractDocument)target.getDocument();
        
        // Dump fold hierarchy
        FoldHierarchy hierarchy = FoldHierarchy.get(target);
        int foldCount = 0;
        while (foldCount==0 && time > 0) {
            
            adoc.readLock();
            try {
                hierarchy.lock();
                try {
                    foldCount = hierarchy.getRootFold().getFoldCount();
                } finally {
                    hierarchy.unlock();
                }
            } finally {
                adoc.readUnlock();
            }
            
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
            
        }
            
    }
    
    private ValueResolver getResolver(final JTextComponent target){
        
        ValueResolver foldValueResolver = new ValueResolver(){
            public Object getValue(){
                FoldHierarchy hierarchy = FoldHierarchy.get(target);
                int dot = target.getCaret().getDot();
                hierarchy.lock();
                try{
                    try{
                        int rowStart = javax.swing.text.Utilities.getRowStart(target, dot);
                        int rowEnd = javax.swing.text.Utilities.getRowEnd(target, dot);
                        Fold fold = getLineFold(hierarchy, dot, rowStart, rowEnd);
                        if (fold!=null){
                            return Boolean.valueOf(fold.isCollapsed());
                        }else{
                            return null;
                        }
                    }catch(BadLocationException ble){
                        ble.printStackTrace();
                    }
                }finally {
                    hierarchy.unlock();
                }
                return null;
            }
        };
        
        return foldValueResolver;
    }
    
    protected void collapseFoldAtCaretPosition(EditorOperator editor, int line, int column){
        System.out.println("Collapsing");
        // 1. move to adequate place 
        editor.setCaretPosition(line, column);

        // 2. hit CTRL -
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK);

        JTextComponentOperator text = new JTextComponentOperator(editor);
        JTextComponent target = (JTextComponent)text.getSource();

        // give max 500 milis to fold to collapse
        waitMaxMilisForValue(500, getResolver(target), Boolean.TRUE);
       
    }

    protected void expandFoldAtCaretPosition(EditorOperator editor, int line, int column){
        // 1. move to adequate place 
        editor.setCaretPosition(line, column);

        // 2. hit CTRL +
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK);
        
        JTextComponentOperator text = new JTextComponentOperator(editor);
        JTextComponent target = (JTextComponent)text.getSource();
        
        // give max 500 milis to fold to expand
        waitMaxMilisForValue(500, getResolver(target), Boolean.FALSE);
    }
    
    protected void collapseAllFolds(EditorOperator editor){
        // hit CTRL Shift -
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        
        // wait a while
        try {
            Thread.currentThread().sleep(COLLAPSE_EXPAND_DELAY);
        } catch (InterruptedException ex) {
        }
    }

    protected void expandAllFolds(EditorOperator editor){
        // hit CTRL Shift +
        JEditorPaneOperator txtOper = editor.txtEditorPane();
        txtOper.pushKey(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        
        // wait a while
        try {
            Thread.currentThread().sleep(COLLAPSE_EXPAND_DELAY);
        } catch (InterruptedException ex) {
        }
    }

    /** Returns the fold that should be collapsed/expanded in the caret row
     *  @param hierarchy hierarchy under which all folds should be collapsed/expanded.
     *  @param dot caret position offset
     *  @param lineStart offset of the start of line
     *  @param lineEnd offset of the end of line
     *  @return the fold that meet common criteria in accordance with the caret position
     */
    private static Fold getLineFold(FoldHierarchy hierarchy, int dot, int lineStart, int lineEnd){
        Fold caretOffsetFold = FoldUtilities.findOffsetFold(hierarchy, dot);

        // beginning searching from the lineStart
        Fold fold = FoldUtilities.findNearestFold(hierarchy, lineStart);  
        
        while (fold!=null && 
                  (fold.getEndOffset()<=dot || // find next available fold if the 'fold' is one-line
                      // or it has children and the caret is in the fold body
                      // i.e. class A{ |public void method foo(){}}
                      (!fold.isCollapsed() && fold.getFoldCount() > 0  && fold.getStartOffset()+1<dot) 
                   )
               ){

                   // look for next fold in forward direction
                   Fold nextFold = FoldUtilities.findNearestFold(hierarchy,
                       (fold.getFoldCount()>0) ? fold.getStartOffset()+1 : fold.getEndOffset());
                   if (nextFold!=null && nextFold.getStartOffset()<lineEnd){
                       if (nextFold == fold) return fold;
                       fold = nextFold;
                   }else{
                       break;
                   }
        }

        
        // a fold on the next line was found, returning fold at offset (in most cases inner class)
        if (fold == null || fold.getStartOffset()>lineEnd) {

            // in the case:
            // class A{
            // }     |
            // try to find an offset fold on the offset of the line beginning
            if (caretOffsetFold == null){
                caretOffsetFold = FoldUtilities.findOffsetFold(hierarchy, lineStart);
            }
            
            return caretOffsetFold;
        }
        
        // no fold at offset found, in this case return the fold
        if (caretOffsetFold == null) return fold;
        
        // skip possible inner class members validating if the innerclass fold is collapsed
        if (caretOffsetFold.isCollapsed()) return caretOffsetFold;
        
        // in the case:
        // class A{
        // public vo|id foo(){} }
        // 'fold' (in this case fold of the method foo) will be returned
        if ( caretOffsetFold.getEndOffset()>fold.getEndOffset() && 
             fold.getEndOffset()>dot){
            return fold;
        }
        
        // class A{
        // |} public void method foo(){}
        // inner class fold will be returned
        if (fold.getStartOffset()>caretOffsetFold.getEndOffset()) return caretOffsetFold;
        
        // class A{
        // public void foo(){} |}
        // returning innerclass fold
        if (fold.getEndOffset()<dot) return caretOffsetFold;
        
        return fold;
    }
    
}
