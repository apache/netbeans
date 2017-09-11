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

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.accessibility.AccessibleContext;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Represents left and/or right side of the main split pane.
 *
 * @author Maros Sandor
 */
class DiffContentPanel extends JPanel implements HighlightsContainer,Lookup.Provider {

    private final EditableDiffView master;
    private final boolean isFirst;

    private final DecoratedEditorPane     editorPane;
    private JScrollPane                   scrollPane;
    private final LineNumbersActionsBar   linesActions;
    private final JScrollPane             actionsScrollPane;

    private Difference[] currentDiff;
    
    public DiffContentPanel(EditableDiffView master, boolean isFirst) {
        this.master = master;
        this.isFirst = isFirst;

        setLayout(new BorderLayout());

        editorPane = new DecoratedEditorPane(this);
        editorPane.setEditable(false);
        scrollPane = new JScrollPane(editorPane);
        add(scrollPane);
        
        linesActions = new LineNumbersActionsBar(this, master.isActionsEnabled());
        actionsScrollPane = new JScrollPane(linesActions);
        actionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        actionsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        actionsScrollPane.setBorder(null);
        add(actionsScrollPane, isFirst ? BorderLayout.LINE_END : BorderLayout.LINE_START);
        
        editorPane.putClientProperty(DiffHighlightsLayerFactory.HIGHLITING_LAYER_ID, this);
        if (!isFirst) {
            // disable focus traversal, but permit just the up-cycle on ESC key
            editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
            editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
            editorPane.setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.singleton(
                    KeyStroke.getAWTKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.SHIFT_DOWN_MASK)));
            
            editorPane.putClientProperty("errorStripeOnly", Boolean.TRUE);
            editorPane.putClientProperty("code-folding-enable", false);
        }
    }
    
    void initActions() {
        //TODO: copied from CloneableEditor - this has no effect
        ActionMap paneMap = editorPane.getActionMap();
        ActionMap am = getActionMap();
        am.setParent(paneMap);
        paneMap.put(DefaultEditorKit.cutAction, getAction(DefaultEditorKit.cutAction));
        paneMap.put(DefaultEditorKit.copyAction, getAction(DefaultEditorKit.copyAction));
        paneMap.put("delete", getAction(DefaultEditorKit.deleteNextCharAction)); // NOI18N
        paneMap.put(DefaultEditorKit.pasteAction, getAction(DefaultEditorKit.pasteAction));
    }
    
    private Action getAction(String key) {
        if (key == null) {
            return null;
        }

        // Try to find the action from kit.
        EditorKit kit = editorPane.getEditorKit();

        if (kit == null) { // kit is cleared in closeDocument()

            return null;
        }

        Action[] actions = kit.getActions();

        for (int i = 0; i < actions.length; i++) {
            if (key.equals(actions[i].getValue(Action.NAME))) {
                return actions[i];
            }
        }

        return null;
    }
    
    LineNumbersActionsBar getLinesActions() {
        return linesActions;
    }

    public JScrollPane getActionsScrollPane() {
        return actionsScrollPane;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public Difference[] getCurrentDiff() {
        return currentDiff;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setCurrentDiff(Difference[] currentDiff) {
        this.currentDiff = currentDiff;
        editorPane.setDifferences(currentDiff);
        linesActions.onDiffSetChanged();
        fireHilitingChanged();
//        revalidate();
    }

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Container parent = getParent();
        if (parent instanceof JViewport) {
            if (parent.getWidth() > d.width) {
                d = new Dimension(parent.getWidth(), d.height);
            }
        }
        return d;
    }

    public DecoratedEditorPane getEditorPane() {
        return editorPane;
    }

    public AccessibleContext getAccessibleContext() {
        return editorPane.getAccessibleContext();
    }

    public EditableDiffView getMaster() {
        return master;
    }

    // === Highliting ======================================================================== 
    
    HighlightsContainer getHighlightsContainer() {
        return this;
    }

    public HighlightsSequence getHighlights(int start, int end) {
        return new DiffHighlightsSequence(start, end);
    }

    private final List<HighlightsChangeListener> listeners = new ArrayList<HighlightsChangeListener>(1);
    
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }
    
    void fireHilitingChanged() {
        synchronized(listeners) {
            for (HighlightsChangeListener listener : listeners) {
              listener.highlightChanged(new HighlightsChangeEvent(this, 0, Integer.MAX_VALUE));
            }
        }
    }

    void onUISettingsChanged() {
        editorPane.repaint();
        linesActions.onUISettingsChanged();
        actionsScrollPane.revalidate();
        actionsScrollPane.repaint();
        revalidate();
        repaint();
    }
    
    public void setCustomEditor(JComponent c) {
        remove(scrollPane);
        // The present editorPane will already be wrapped with the new custom editor
        // including the new scrollpane that needs to be re-assigned
        Component viewPort = findViewPort(editorPane);
        if (viewPort instanceof JViewport) {
            viewPort = viewPort.getParent();
            if (viewPort instanceof JScrollPane) {
                scrollPane = (JScrollPane)viewPort;
            }
        }
        add(c);
        c.setFocusTraversalKeysEnabled(false);
        c.setFocusTraversalPolicyProvider(true);
    }

    public Lookup getLookup() {
        return Lookups.singleton(getActionMap());
    }

    private Component findViewPort (Container container) {
        if (container == null) {
            return null;
        } else if (container instanceof JViewport) {
            return container;
        } else {
            return findViewPort(container.getParent());
        }
    }

    /**
     * Iterates over all found differences.
     */
    private class DiffHighlightsSequence implements HighlightsSequence {
        
        private final int       endOffset;
        private final int       startOffset;

        private int             currentHiliteIndex = -1;             
        private DiffViewManager.HighLight [] hilites;

        public DiffHighlightsSequence(int start, int end) {
            this.startOffset = start;
            this.endOffset = end;
            lookupHilites();
        }

        private void lookupHilites() {
            List<DiffViewManager.HighLight> list = new ArrayList<DiffViewManager.HighLight>();
            DiffViewManager.HighLight[] allHilites = isFirst ? master.getManager().getFirstHighlights() : master.getManager().getSecondHighlights(); 
            for (DiffViewManager.HighLight hilite : allHilites) {
                if (hilite.getEndOffset() < startOffset) continue;
                if (hilite.getStartOffset() > endOffset) break;
                list.add(hilite);
            }
            hilites = list.toArray(new DiffViewManager.HighLight[list.size()]);
        }

        public boolean moveNext() {
            if (currentHiliteIndex >= hilites.length - 1) return false;
            currentHiliteIndex++;
            return true;
        }

        public int getStartOffset() {
            return Math.max(hilites[currentHiliteIndex].getStartOffset(), this.startOffset);
        }

        public int getEndOffset() {
            return Math.min(hilites[currentHiliteIndex].getEndOffset(), this.endOffset);
        }

        public AttributeSet getAttributes() {
            return hilites[currentHiliteIndex].getAttrs();
        }
    }
}
