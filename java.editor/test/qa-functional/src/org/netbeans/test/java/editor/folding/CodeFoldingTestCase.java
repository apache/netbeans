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
