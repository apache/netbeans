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