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

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.PropertyChangeListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.EditorUI;
import org.netbeans.lib.editor.view.GapDocumentView;
import org.netbeans.editor.view.spi.LockView;

/**
 * View of the whole document supporting the code folding.
 *
 * @author Miloslav Metelka
 */
public class DrawEngineDocView extends GapDocumentView
implements PropertyChangeListener {
    
    private static final boolean debugRebuild
        = Boolean.getBoolean("netbeans.debug.editor.view.rebuild"); // NOI18N

    /** Editor UI listening to */
    private EditorUI editorUI;
    
    private int collapsedFoldStartOffset;
    
    private boolean collapsedFoldsInPresentViews;
    
    private boolean estimatedSpanResetInitiated;
    
    public DrawEngineDocView(Element elem) {
        this(elem, false);
    }
    
    public DrawEngineDocView(Element elem, boolean hideBottomPadding) {
        super(elem, hideBottomPadding);
        
        setEstimatedSpan(true);
    }
    
    public @Override void setParent(View parent) {
        if (parent != null) { // start listening
            JTextComponent component = (JTextComponent)parent.getContainer();
            TextUI tui = component.getUI();
            if (tui instanceof BaseTextUI){
                editorUI = ((BaseTextUI)tui).getEditorUI();
                if (editorUI!=null){
                    editorUI.addPropertyChangeListener(this);
                }
            }
        }

        super.setParent(parent);
        
        if (parent == null) {
            if (editorUI!=null){
                editorUI.removePropertyChangeListener(this);
                editorUI = null;
            }
        }
    }
    
    protected void attachListeners(){
    }
    
    protected @Override boolean useCustomReloadChildren() {
        return true;
    }

    protected @Override View createCustomView(ViewFactory f,
    int startOffset, int maxEndOffset, int elementIndex) {
        if (elementIndex == -1) {
            throw new IllegalStateException("Need underlying line element structure"); // NOI18N
        }
        
        View view = null;

        Element elem = getElement();
        Element lineElem = elem.getElement(elementIndex);
        view = f.create(lineElem);
        return view;
    }            

    public @Override void paint(Graphics g, Shape allocation) {
        java.awt.Component c = getContainer();
        if (c instanceof javax.swing.text.JTextComponent){
            TextUI textUI = ((javax.swing.text.JTextComponent)c).getUI();
            if (textUI instanceof BaseTextUI){
                EditorUiAccessor.get().paint(((BaseTextUI) textUI).getEditorUI(), g);
            }
        }

        super.paint(g, allocation);

        // #114712 - set the color to foreground so that the JTextComponent.ComposedTextCaret.paint()
        // does not render white-on-white.
        if (c != null) {
            g.setColor(c.getForeground());
        }
    }
    
    public @Override void setSize(float width, float height) {
        super.setSize(width, height);

        /* #69446 - disabled estimated span reset
        // Schedule estimated span reset
        if (!estimatedSpanResetInitiated && isEstimatedSpan()) {
            estimatedSpanResetInitiated = true;
            Timer timer = new Timer(4000, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    AbstractDocument doc = (AbstractDocument)getDocument();
                    if (doc!=null) {
                        doc.readLock();
                        try {
                            LockView lockView = LockView.get(DrawEngineDocView.this);
                            if (lockView != null) { // if there is no lock view no async layout is done
                                lockView.lock();
                                try {
                                    setEstimatedSpan(false);
                                } finally {
                                    lockView.unlock();
                                }
                            }
                        } finally {
                            doc.readUnlock();
                        }
                    }
                }
            });
            
            timer.setRepeats(false);
            timer.start();
        }
         */
    }

    protected @Override boolean isChildrenResizeDisabled() {
        return true;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        JTextComponent component = (JTextComponent)getContainer();
        if (component==null || evt==null || 
            (!EditorUI.LINE_HEIGHT_CHANGED_PROP.equals(evt.getPropertyName()) &&
             !EditorUI.TAB_SIZE_CHANGED_PROP.equals(evt.getPropertyName())
            )
        ) {
            return;
        }
        
        AbstractDocument doc = (AbstractDocument)getDocument();
        if (doc!=null) {
            doc.readLock();
            try{
                LockView lockView = LockView.get(this);
                lockView.lock();
                try {
                    rebuild(0, getViewCount());
                } finally {
                    lockView.unlock();
                }
            } finally {
                doc.readUnlock();
            }
        component.revalidate();
        }
    }
    
    public int getYFromPos(int offset, Shape a) {
        int index = getViewIndex(offset);
        if (index >= 0) {
            Shape ca = getChildAllocation(index, a);
            return (ca instanceof Rectangle)
                    ? ((Rectangle)ca).y
                    : (ca != null) ? ca.getBounds().y : 0;
        }
        return 0;
    }
    
}
