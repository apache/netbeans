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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.diff.Difference;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.diff.Utils;

/**
 * Editor pane with added decorations (diff lines).
 * 
 * @author Maros Sandor
 */
class DecoratedEditorPane extends JEditorPane implements PropertyChangeListener {

    private Difference[]        currentDiff;
    private DiffContentPanel    master;
    
    private final RequestProcessor.Task repaintTask;
    private static final RequestProcessor FONT_RP = new RequestProcessor("DiffFontLoadingRP", 1); //NOI18N

    private int                 fontHeight = -1;
    private int                 charWidth;

    public DecoratedEditorPane(DiffContentPanel master) {
        repaintTask = Utils.createParallelTask(new RepaintPaneTask());
        setBorder(null);
        this.master = master;
        master.getMaster().addPropertyChangeListener(this);
    }

    public boolean isFirst() {
        return master.isFirst();
    }

    public DiffContentPanel getMaster() {
        return master;
    }

    void setDifferences(Difference [] diff) {
        currentDiff = diff;
        repaint();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        setFontHeightWidth(getFont());
    }
    
    private void setFontHeightWidth(final Font font) {
        FONT_RP.post(new Runnable() {
            @Override
            public void run() {
                FontMetrics metrics = getFontMetrics(font);
                charWidth = metrics.charWidth('m');
                fontHeight = metrics.getHeight();
            }
        });
    }
    
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (fontHeight == -1) {
            return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
        }
        switch (orientation) {
        case SwingConstants.VERTICAL:
            return fontHeight;
        case SwingConstants.HORIZONTAL:
            return charWidth;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation); // discrimination
        }
    }

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        if (currentDiff == null) return;

        EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(this);
        if (editorUI == null) return;
        
        Graphics2D g = (Graphics2D) gr.create();
        Rectangle clip = g.getClipBounds();
        Stroke cs = g.getStroke();
        // compensate for cursor drawing, it is needed for catching a difference on the cursor line 
        clip.y -= 1;
        clip.height += 1; 
        
        
        FoldHierarchy foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        JTextComponent component = editorUI.getComponent();
        if (component == null) return;
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try{
            foldHierarchy.lock();
            try{
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    // find the nearest visible line with an annotation
                    Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                    int y = (rec == null) ? 0 : rec.y;

                    int clipEndY = clip.y + clip.height;
                    Element rootElem = textUI.getRootView(component).getElement();

                    View view = rootView.getView(startViewIndex);
                    int line = rootElem.getElementIndex(view.getStartOffset());
                    line++; // make it 1-based

                    int curDif = master.getMaster().getCurrentDifference();
                    
                    g.setColor(master.getMaster().getColorLines());
                    for (int i = startViewIndex; i < rootViewCount; i++) {
                        view = rootView.getView(i);
                        line = rootElem.getElementIndex(view.getStartOffset());
                        line++; // make it 1-based
                        Difference ad = master.isFirst() ? EditableDiffView.getFirstDifference(currentDiff, line) : EditableDiffView.getSecondDifference(currentDiff, line);
                        Rectangle rec1 = component.modelToView(view.getStartOffset());
                        Rectangle rec2 = component.modelToView(view.getEndOffset() - 1);
                        if (rec1 == null || rec2 == null) {
                            break;
                        }
                        y = (int)rec1.getY();
                        int height = (int) (rec2.getY() + rec2.getHeight() - rec1.getY());
                        if (ad != null) {
                            // TODO: can cause AIOOBE, synchronize "currentDiff" and "curDif" variables
                            g.setStroke(curDif >= 0 && curDif < currentDiff.length && currentDiff[curDif] == ad ? master.getMaster().getBoldStroke() : cs);
                            int yy = y + height;
                            if (ad.getType() == (master.isFirst() ? Difference.ADD : Difference.DELETE)) {
                                g.drawLine(0, yy, getWidth(), yy);
                                ad = null;
                            } else {
                                if ((master.isFirst() ? ad.getFirstStart() : ad.getSecondStart()) == line) {
                                    g.drawLine(0, y, getWidth(), y);
                                }
                                if ((master.isFirst() ? ad.getFirstEnd() : ad.getSecondEnd()) == line) {
                                    g.drawLine(0, yy, getWidth(), yy);
                                }
                            }
                        }
                        y += height;
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }
            } finally {
                foldHierarchy.unlock();
            }
        } catch (BadLocationException ble){
            ErrorManager.getDefault().notify(ble);
        } finally {
            doc.readUnlock();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        repaintTask.schedule(150);
    }
    
    private class RepaintPaneTask implements Runnable {
        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    repaint();
                }
            });
        }
    }
}