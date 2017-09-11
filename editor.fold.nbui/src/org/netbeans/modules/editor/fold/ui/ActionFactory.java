/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.fold.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.openide.util.Exceptions;

/**
 * Factory for editor code folding actions.
 * Migrated from editor.lib module
 *
 * @author sdedic
 */
final class ActionFactory {
    
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
    
    /** Collapse a fold. Depends on the current caret position. */
    @EditorActionRegistration(name = BaseKit.collapseFoldAction,
            menuText = "#" + BaseKit.collapseFoldAction + "_menu_text")
    public static class CollapseFold extends LocalBaseAction {
        public CollapseFold(){
        }
        
        private boolean dotInFoldArea(JTextComponent target, Fold fold, int dot) throws BadLocationException{
            int foldStart = fold.getStartOffset();
            int foldEnd = fold.getEndOffset();
            int foldRowStart = javax.swing.text.Utilities.getRowStart(target, foldStart);
            int foldRowEnd = javax.swing.text.Utilities.getRowEnd(target, foldEnd);
            if (foldRowStart > dot || foldRowEnd < dot) return false; // it's not fold encapsulating dot
            return true;
            }

        
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            Document doc = target.getDocument();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    FoldHierarchy hierarchy = FoldHierarchy.get(target);
                    int dot = target.getCaret().getDot();
                    hierarchy.lock();
                    try{
                        try{
                            int rowStart = javax.swing.text.Utilities.getRowStart(target, dot);
                            int rowEnd = javax.swing.text.Utilities.getRowEnd(target, dot);
                            Fold fold = FoldUtilities.findNearestFold(hierarchy, rowStart);
                            fold = getLineFold(hierarchy, dot, rowStart, rowEnd);
                            if (fold==null){
                                return; // no success
                            }
                            // ensure we' got the right fold
                            if (dotInFoldArea(target, fold, dot)){
                                hierarchy.collapse(fold);
                            }
                        }catch(BadLocationException ble){
                            Exceptions.printStackTrace(ble);
                        }
                    }finally {
                        hierarchy.unlock();
                    }
                }
            });
        }
    }
    
    /** Expand a fold. Depends on the current caret position. */
    @EditorActionRegistration(name = BaseKit.expandFoldAction,
            menuText = "#" + BaseKit.expandFoldAction + "_menu_text")
    public static class ExpandFold extends LocalBaseAction {
        public ExpandFold(){
        }
        
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            Document doc = target.getDocument();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    FoldHierarchy hierarchy = FoldHierarchy.get(target);
                    int dot = target.getCaret().getDot();
                    hierarchy.lock();
                    try {
                        try {
                            int rowStart = javax.swing.text.Utilities.getRowStart(target, dot);
                            int rowEnd = javax.swing.text.Utilities.getRowEnd(target, dot);
                            Fold fold = getLineFold(hierarchy, dot, rowStart, rowEnd);
                            if (fold != null) {
                                hierarchy.expand(fold);
                            }
                        } catch (BadLocationException ble) {
                            Exceptions.printStackTrace(ble);
                        }
                    } finally {
                        hierarchy.unlock();
                    }
                }
            });
        }
    }
    
    
    @EditorActionRegistration(name = "expand-fold-tree",
            menuText = "#expand-fold-tree_menu_text"
    )
    public static class ExpandFoldsTree extends LocalBaseAction {
        @Override
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            Document doc = target.getDocument();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    FoldHierarchy hierarchy = FoldHierarchy.get(target);
                    int dot = target.getCaret().getDot();
                    hierarchy.lock();
                    try {
                        try {
                            int rowStart = javax.swing.text.Utilities.getRowStart(target, dot);
                            int rowEnd = javax.swing.text.Utilities.getRowEnd(target, dot);
                            Fold fold = getLineFold(hierarchy, dot, rowStart, rowEnd);
                            if (fold == null) {
                                return;
                            }
                            List allFolds = new ArrayList<Fold>(FoldUtilities.findRecursive(fold));
                            Collections.reverse(allFolds);
                            allFolds.add(0, fold);
                            hierarchy.expand(allFolds);
                        } catch (BadLocationException ble) {
                            Exceptions.printStackTrace(ble);
                        }
                    } finally {
                        hierarchy.unlock();
                    }
                }
            });
        }
    }
            
    @EditorActionRegistration(name = "collapse-fold-tree",
            menuText = "#collapse-fold-tree_menu_text"
    )
    public static class CollapseFoldsTree extends LocalBaseAction {
        @Override
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            Document doc = target.getDocument();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    FoldHierarchy hierarchy = FoldHierarchy.get(target);
                    int dot = target.getCaret().getDot();
                    hierarchy.lock();
                    try {
                        try {
                            int rowStart = javax.swing.text.Utilities.getRowStart(target, dot);
                            int rowEnd = javax.swing.text.Utilities.getRowEnd(target, dot);
                            Fold fold = getLineFold(hierarchy, dot, rowStart, rowEnd);
                            if (fold == null) {
                                return;
                            }
                            List allFolds = new ArrayList<Fold>(FoldUtilities.findRecursive(fold));
                            Collections.reverse(allFolds);
                            allFolds.add(0, fold);
                            hierarchy.collapse(allFolds);
                        } catch (BadLocationException ble) {
                            Exceptions.printStackTrace(ble);
                        }
                    } finally {
                        hierarchy.unlock();
                    }
                }
            });
        }
    }
    
    /** Collapse all existing folds in the document. */
    @EditorActionRegistration(name = BaseKit.collapseAllFoldsAction,
            menuText = "#" + BaseKit.collapseAllFoldsAction + "_menu_text")
    public static class CollapseAllFolds extends LocalBaseAction {
        public CollapseAllFolds(){
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.collapseAll(hierarchy);
        }
    }

    /** Expand all existing folds in the document. */
    @EditorActionRegistration(name = BaseKit.expandAllFoldsAction,
            menuText = "#" + BaseKit.expandAllFoldsAction + "_menu_text")
    public static class ExpandAllFolds extends LocalBaseAction {
        public ExpandAllFolds(){
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            FoldUtilities.expandAll(hierarchy);
        }
    }


    static abstract class LocalBaseAction extends BaseAction {
        public LocalBaseAction() {
            super();
        }

        @Override
        protected Class getShortDescriptionBundleClass() {
            return ActionFactory.class;
        }

    }
}
