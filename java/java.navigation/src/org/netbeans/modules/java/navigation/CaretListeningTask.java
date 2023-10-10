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

package org.netbeans.modules.java.navigation;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.navigation.base.Utils;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 * This task is called every time the caret position changes in a Java editor.
 * <p>
 * The task finds the TreePath of the Tree under the caret, converts it to
 * an Element and then shows the javadoc in the Javadoc window.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class CaretListeningTask implements CancellableTask<CompilationInfo> {
    
    private FileObject fileObject;
    private final AtomicBoolean canceled;
    
    private static ElementHandle<Element> lastEh;
    private static ElementHandle<Element> lastEhForNavigator;
    
    private static final Set<JavaTokenId> TOKENS_TO_SKIP = EnumSet.of(JavaTokenId.WHITESPACE, 
                                  JavaTokenId.BLOCK_COMMENT, 
                                  JavaTokenId.LINE_COMMENT, 
                                  JavaTokenId.JAVADOC_COMMENT);
    
    
    CaretListeningTask(FileObject fileObject) {
        this.fileObject = fileObject;
        this.canceled = new AtomicBoolean();
    }
    
    static void resetLastEH() {
        lastEh = null;
    }
    
    public void run(CompilationInfo compilationInfo) throws Exception {
        // System.out.println("running " + fileObject);
        resume();
        
        boolean navigatorShouldUpdate = ClassMemberPanel.getInstance() != null; // XXX set by navigator visible
        boolean javadocShouldUpdate = JavadocTopComponent.shouldUpdate();
        
        if ( isCancelled() || ( !navigatorShouldUpdate && !javadocShouldUpdate ) ) {
            return;
        }
                        
        int lastPosition = CaretListeningFactory.getLastPosition(fileObject);
        
        TokenHierarchy tokens = compilationInfo.getTokenHierarchy();
        TokenSequence ts = tokens.tokenSequence();
        boolean inJavadoc = false;
        int offset = ts.move(lastPosition);
        if (ts.moveNext() && ts.token() != null ) {
            
            Token token = ts.token();
            TokenId tid = token.id();
            if ( tid == JavaTokenId.JAVADOC_COMMENT ) {
                inJavadoc = true;                
            }
            
            if ( tid == JavaTokenId.WHITESPACE && shouldGoBack(token.text().toString(), offset < 0 ? 0 : offset ) ) {
                if ( ts.movePrevious() ) {
                    token = ts.token();
                    tid = token.id();
                }
            }
            
            if ( TOKENS_TO_SKIP.contains(tid) ) {
                skipTokens(ts, TOKENS_TO_SKIP);                
            }
            lastPosition = ts.offset();
        }
                
        if (ts.token() != null && (ts.token().length() > 1 || ts.token().id() == JavaTokenId.AT)) {
            // it is magic for TreeUtilities.pathFor to proper tree
            ++lastPosition;
        }
                
        // Find the TreePath for the caret position
        TreePath tp =
                compilationInfo.getTreeUtilities().pathFor(lastPosition);  
        
        Tree.Kind k = tp.getLeaf().getKind();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(k) && ts.token() != null && ts.token().length() == 1) {
            // in case of single-char token, try next position, since e.g. method or a field
            // can have single-char type declaration.
            TreePath tp2 = compilationInfo.getTreeUtilities().pathFor(lastPosition + 1);
            SourcePositions sp = compilationInfo.getTrees().getSourcePositions();
            long e1 = sp.getEndPosition(compilationInfo.getCompilationUnit(), tp2.getLeaf());
            long e2 = sp.getEndPosition(compilationInfo.getCompilationUnit(), tp.getLeaf());
            // the "inner" member does not extend beyond the class
            if (e2 != -1 && e1 != -1 && e1 <= e2) {
                TreePath p = tp2;
                while (p != null && p.getLeaf() != tp.getLeaf() && p.getParentPath() != null) {
                    if (p.getParentPath().getLeaf() == tp.getLeaf()) {
                        // class member found in between position+1 and position. Use the class member
                        tp = tp2;
                        break;
                    }
                    p = p.getParentPath();
                }
            }
        }
        // if cancelled, return
        if (isCancelled()) {
            return;
        }
        
        // Update the navigator
        if ( navigatorShouldUpdate ) {
            updateNavigatorSelection(compilationInfo, tp); 
        }
        
        // Get Element
        Element element = compilationInfo.getTrees().getElement(tp);
                       
        // if cancelled or no element, return
        if (isCancelled() ) {
            return;
        }
    
        if ( element == null || inJavadoc ) {
            final Pair<Element,TreePath> p = outerElement(compilationInfo, tp);
            element = p != null ? p.first() : null;
        }
        
        // if is canceled or no element
        if (isCancelled() || element == null) {            
            return;
        }
        
        // Don't update when element is the same
        if (Utils.signatureEquals(lastEh, element) && !inJavadoc) {
            // System.out.println("  stoped because os same eh");
            return;
        } else {
            switch (element.getKind()) {
            case PACKAGE:
            case CLASS:
            case INTERFACE:
            case RECORD:
            case ENUM:
            case ANNOTATION_TYPE:
            case METHOD:
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case STATIC_INIT:
            case FIELD:
            case ENUM_CONSTANT:
            case MODULE:
                lastEh = Utils.createElementHandle(element);
                // Different element clear data
                setJavadoc(null, null); // NOI18N
                break;
            case TYPE_PARAMETER:
            case PARAMETER:
                element = element.getEnclosingElement(); // Take the enclosing method
                if (element != null && element.asType() != null) {
                    lastEh = Utils.createElementHandle(element);
                } else {
                    lastEh = null;
                }
                setJavadoc(null, null); // NOI18N
                break;
            case LOCAL_VARIABLE:
                lastEh = null; // ElementHandle not supported 
                setJavadoc(null, null); // NOI18N
                return;
            default:
                // clear
                setJavadoc(null, null); // NOI18N
                return;
            }
        }
            
        
        // Compute and set javadoc
        if ( javadocShouldUpdate ) {
            // System.out.println("Updating JD");
            computeAndSetJavadoc(compilationInfo, element);
        }
        
        if ( isCancelled() ) {
            return;
        }
        
    }
        
    private void setJavadoc(final FileObject owner, final ElementJavadoc javadoc) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JavadocTopComponent javadocTopComponent = JavadocTopComponent.findInstance();
                if (javadocTopComponent != null && javadocTopComponent.isOpened()) {
                    javadocTopComponent.setJavadoc(owner, javadoc);
                }
            }
        });
    }
    
    /**
     * After this method is called the task if running should exit the run
     * method immediately.
     */
    @Override
    public final void cancel() {
        canceled.set(true);
    }
    
    protected final boolean isCancelled() {
        return canceled.get();
    }
    
    protected final void resume() {
        canceled.set(false);
    }
    
   
    private void computeAndSetJavadoc(CompilationInfo compilationInfo, Element element) {        
        if (isCancelled()) {
            return;
        }
        setJavadoc(compilationInfo.getFileObject(), ElementJavadoc.create(
                compilationInfo,
                element,
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return isCancelled();
                    }
                }));
    }
    
    private void updateNavigatorSelection(CompilationInfo ci, TreePath tp) throws Exception {
        final ClassMemberPanel cmp = ClassMemberPanel.getInstance();
        if (cmp == null) {
            return;
        }
        final ClassMemberPanelUI cmpUi = cmp.getClassMemberPanelUI();
        if (!cmpUi.isAutomaticRefresh()) {
            cmpUi.getTask().runImpl(ci, false);
            lastEhForNavigator = null;
        }
        // Try to find the declaration we are in
        final Pair<Element,TreePath> p = outerElement(ci, tp);
        if (p != null) {
            final Element e = p.first();
            Runnable action = null;
            if (e == null) {
                //Directive
                lastEhForNavigator = null;
                action = () -> {
                    cmp.selectTreePath(TreePathHandle.create(p.second(), ci));
                };
            } else if (e.getKind() != ElementKind.OTHER) {
                final ElementHandle<Element> eh = ElementHandle.create(e);
                if (lastEhForNavigator != null && eh.signatureEquals(lastEhForNavigator)) {
                    return;
                }
                lastEhForNavigator = eh;
                action = () -> {
                    cmp.selectElement(eh);
                };
            }
            if (action != null) {
                SwingUtilities.invokeLater(action);
            }
        }
    }

    private static Pair<Element,TreePath> outerElement( CompilationInfo ci, TreePath tp ) {
        Pair<Element,TreePath> res = null;
        while (tp != null) {
            switch( tp.getLeaf().getKind()) {
                case METHOD:
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case COMPILATION_UNIT:
                case MODULE:
                {
                    final Element e = ci.getTrees().getElement(tp);
                    if (e != null) {
                        res = Pair.of(e,tp);
                    }
                    break;
                }
                case REQUIRES:
                case EXPORTS:
                case OPENS:
                case PROVIDES:
                case USES:
                {
                    res = Pair.of(null, tp);
                    break;
                }
                case VARIABLE:
                {
                    final Element e = ci.getTrees().getElement(tp);
                    if (e != null && e.getKind().isField()) {
                        res = Pair.of(e,tp);
                    }
                    break;
                }
            }
            if ( res != null ) {
                break;
            }
            tp = tp.getParentPath();
        }
        return res;
    }

    private void skipTokens( TokenSequence ts, Set<JavaTokenId> typesToSkip ) {
                  
        while(ts.moveNext()) {
            if ( !typesToSkip.contains(ts.token().id()) ) {
                return;
            }
        }
        
        return;
    }
    
    private boolean shouldGoBack( String s, int offset ) {
        
        int nlBefore = 0;
        int nlAfter = 0;
        
        for( int i = 0; i < s.length(); i++ ) {
            if ( s.charAt(i) == '\n' ) { // NOI18N
                if ( i < offset ) {
                    nlBefore ++; 
                }
                else { 
                    nlAfter++; 
                }
                
                if ( nlAfter > nlBefore ) {
                    return true;
                }                
            }
        }
        
        if ( nlBefore < nlAfter ) {
            return false;
        }
        
        return offset < (s.length() - offset);
        
    }
    
    
    
}
