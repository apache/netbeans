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

package org.netbeans.modules.editor.completion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompositeCompletionItem;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**
 * Layout of the completion, documentation and tooltip popup windows.
 *
 * @author Dusan Balek, Miloslav Metelka
 */
public final class CompletionLayout {
    
    public static final int COMPLETION_ITEM_HEIGHT = 16;
    
    /**
     * Visual shift of the completion window to the left
     * so that the text in the rendered completion items.aligns horizontally
     * with the text in the document.
     */
    private static final int COMPLETION_ANCHOR_HORIZONTAL_SHIFT = 22;
    
    /**
     * Gap between caret and the displayed popup.
     */
    static final int POPUP_VERTICAL_GAP = 1;

    private Reference<JTextComponent> editorComponentRef;

    private final CompletionPopup completionPopup;
    private final DocPopup docPopup;
    private final TipPopup tipPopup;
    
    private LinkedList<CompletionLayoutPopup> visiblePopups;
    
    CompletionLayout() {
        completionPopup = new CompletionPopup();
        completionPopup.setLayout(this);
        completionPopup.setPreferDisplayAboveCaret(false);
        docPopup = new DocPopup();
        docPopup.setLayout(this);
        docPopup.setPreferDisplayAboveCaret(false);
        tipPopup = new TipPopup();
        tipPopup.setLayout(this);
        tipPopup.setPreferDisplayAboveCaret(true);
        visiblePopups = new LinkedList<CompletionLayoutPopup>();
    }
    
    public JTextComponent getEditorComponent() {
        return (editorComponentRef != null)
	    ? editorComponentRef.get()
	    : null;
    }

    public void setEditorComponent(JTextComponent editorComponent) {
        hideAll();
        this.editorComponentRef = new WeakReference<JTextComponent>(editorComponent);
    }

    private void hideAll() {
        for (CompletionLayoutPopup popup : visiblePopups) {
            popup.hide();
        }
        visiblePopups.clear();
    }

    public void showCompletion(List data, String title, int anchorOffset,
    ListSelectionListener listSelectionListener, String additionalItemsText, String shortcutHint, int selectedIndex) {
        completionPopup.show(data, title, anchorOffset, listSelectionListener, additionalItemsText, shortcutHint, selectedIndex);
        for (Iterator<CompletionLayoutPopup> iterator = visiblePopups.iterator(); iterator.hasNext();) {
            CompletionLayoutPopup next = iterator.next();
            if (next instanceof CompletionPopup && next != completionPopup) {
                next.hide();
                iterator.remove();
            }
        }
        if (!visiblePopups.contains(completionPopup))
            visiblePopups.push(completionPopup);
    }
    
    public void showCompletionSubItems() {
        CompletionItem item = getSelectedCompletionItem();
        List<? extends CompletionItem> subItems = item instanceof CompositeCompletionItem ? ((CompositeCompletionItem)item).getSubItems() : null;
        if (subItems != null && !subItems.isEmpty()) {
            Point p = getSelectedLocation();
            if (p != null) {
                CompletionPopup popup = new CompletionPopup();
                popup.setLayout(this);
                popup.show(subItems, p);
                if (!visiblePopups.contains(popup))
                    visiblePopups.push(popup);
            }
        }
    }
    
    public boolean hideCompletion(boolean completionOnly) {
        for (Iterator<CompletionLayoutPopup> it = visiblePopups.iterator(); it.hasNext();) {
            CompletionLayoutPopup popup = it.next();
            if (popup instanceof CompletionPopup && popup.isVisible()) {
                popup.hide();
                ((CompletionPopup)popup).completionScrollPane = null;
                it.remove();
                if (!completionOnly) {
                    return true;
                }
            }            
        }
        return false;
    }
    
    public boolean isCompletionVisible() {
        return completionPopup.isVisible();
    }
    
    public CompletionItem getSelectedCompletionItem() {
        for (CompletionLayoutPopup popup : visiblePopups) {
            if (popup instanceof CompletionPopup && popup.isVisible()) {
                return ((CompletionPopup)popup).getSelectedCompletionItem();
            }
        }
        return null;
    }
    
    public int getSelectedIndex() {
        for (CompletionLayoutPopup popup : visiblePopups) {
            if (popup instanceof CompletionPopup && popup.isVisible()) {
                return ((CompletionPopup)popup).getSelectedIndex();
            }
        }
        return -1;
    }
    
    public Point getSelectedLocation() {
        for (CompletionLayoutPopup popup : visiblePopups) {
            if (popup instanceof CompletionPopup && popup.isVisible()) {
                return ((CompletionPopup)popup).getSelectedLocation();
            }
        }
        return null;
    }
    
    public void processKeyEvent(KeyEvent evt) {
        for (CompletionLayoutPopup popup : visiblePopups) {
            popup.processKeyEvent(evt);
            if (evt.isConsumed())
                return;
        }
    }

    public void showDocumentation(CompletionDocumentation doc, int anchorOffset) {
        docPopup.show(doc, anchorOffset);
        if (!visiblePopups.contains(docPopup))
            visiblePopups.push(docPopup);
    }
    
    public boolean hideDocumentation() {
        if (docPopup.isVisible()) {
            docPopup.getDocumentationScrollPane().setData(null);
            docPopup.clearHistory();
            docPopup.hide();
            visiblePopups.remove(docPopup);
            return true;
        } else { // not visible
            return false;
        }
    }
    
    public boolean isDocumentationVisible() {
        return docPopup.isVisible();
    }
    
    public void clearDocumentationHistory() {
        docPopup.clearHistory();
    }
    
    public void showToolTip(JToolTip toolTip, int anchorOffset) {
        tipPopup.show(toolTip, anchorOffset);
        if (!visiblePopups.contains(tipPopup))
            visiblePopups.push(tipPopup);
    }
    
    public boolean hideToolTip() {
        if (tipPopup.isVisible()) {
            tipPopup.hide();
            visiblePopups.remove(tipPopup);
            return true;
        } else { // not visible
            return false;
        }
    }
    
    public boolean isToolTipVisible() {
        return tipPopup.isVisible();
    }

    /**
     * Layout either of the copmletion, documentation or tooltip popup.
     * <br>
     * This method can be called recursively to update other popups
     * once certain popup was updated.
     *
     * <p>
     * The rules for the displayment are the following:
     * <ul>
     *  <li> The tooltip popup should be above caret if there is enough space.
     *  <li> The completion popup should be above caret if there is enough space
     *       and the tooltip window is not displayed.
     *  <li> If both tooltip and completion popups are visible then vertically
     *       each should be on opposite side of the anchor bounds (caret).
     *  <li> Documentation should be preferrably shrinked if there is not enough
     *       vertical space.
     *  <li> Documentation anchoring should be aligned with completion.
     * </ul>
     */
    void updateLayout(CompletionLayoutPopup popup) {
        // Make sure the popup returns its natural preferred size
        popup.resetPreferredSize();

        if (popup == completionPopup) { // completion popup
            if (isToolTipVisible()) {
                // Display on opposite side than tooltip
                boolean wantAboveCaret = !tipPopup.isDisplayAboveCaret();
                if (completionPopup.isEnoughSpace(wantAboveCaret)) {
                    completionPopup.showAlongAnchorBounds(wantAboveCaret);
                } else { // not enough space -> show on same side
                    Rectangle occupiedBounds = popup.getAnchorOffsetBounds();
                    occupiedBounds = tipPopup.unionBounds(occupiedBounds);
                    completionPopup.showAlongOccupiedBounds(occupiedBounds,
                            tipPopup.isDisplayAboveCaret());
                }
                
            } else { // tooltip not visible
                popup.showAlongAnchorBounds();
            }
            
            // Update docPopup layout if necessary
            if (docPopup.isVisible()
                && (docPopup.isOverlapped(popup) || docPopup.isOverlapped(tipPopup)
                    || docPopup.getAnchorOffset() != completionPopup.getAnchorOffset()
                    || !docPopup.isShowRetainedPreferredSize())
            ) {
                updateLayout(docPopup);
            }
        } else if (popup == docPopup) { // documentation popup
            if (isCompletionVisible()) {
                // Documentation must sync anchoring with completion
                popup.setAnchorOffset(completionPopup.getAnchorOffset());
            }
            
            Rectangle occupiedBounds = popup.getAnchorOffsetBounds();
            occupiedBounds = tipPopup.unionBounds(completionPopup.unionBounds(occupiedBounds));

            if(CompletionSettings.getInstance(getEditorComponent()).documentationPopupNextToCC()) {
                docPopup.showAlongOrNextOccupiedBounds(completionPopup.getPopupBounds(), occupiedBounds);
            } else {
                docPopup.showAlongOccupiedBounds(occupiedBounds);
            }


        } else if (popup == tipPopup) { // tooltip popup
            popup.showAlongAnchorBounds(); // show possibly above the caret
            if (completionPopup.isOverlapped(popup) || docPopup.isOverlapped(popup)) {
                // docPopup layout will be handled as part of completion popup layout
                updateLayout(completionPopup);
            }
        } else { // completion sub-items popup
            Rectangle occupiedBounds = popup.getAnchorOffsetBounds();
            popup.showAlongOrNextOccupiedBounds(occupiedBounds, occupiedBounds);
        }
    }
    
    CompletionPopup testGetCompletionPopup() {
        return completionPopup;
    }
    
     void repaintCompletionView() {
        assert EventQueue.isDispatchThread();
        JComponent completionView = completionPopup.completionScrollPane;
        if(completionView != null && completionView.isVisible()) {
            completionView.repaint();
        }
    }
    
    private static final class CompletionPopup extends CompletionLayoutPopup {
        
        private CompletionScrollPane completionScrollPane;
        
        public void show(List data, Point location) {
            show(data, null, -1, location, null, null, null, 0);
        }
        
        public void show(List data, String title, int anchorOffset,
        ListSelectionListener listSelectionListener, String additionalItemsText, String shortcutHint, int selectedIndex) {
            show(data, title, anchorOffset, null, listSelectionListener, additionalItemsText, shortcutHint, selectedIndex);
        }
            
        private void show(List data, String title, int anchorOffset, Point location,
        ListSelectionListener listSelectionListener, String additionalItemsText, String shortcutHint, int selectedIndex) {
            
	    JTextComponent editorComponent = getEditorComponent();
	    if (editorComponent == null) {
		return;
	    }

            Dimension lastSize;
            int lastAnchorOffset = getAnchorOffset();

            if (isVisible() && ((getContentComponent() == completionScrollPane)^(shortcutHint != null))) {
                lastSize = getContentComponent().getSize();
                resetPreferredSize();

            } else { // not yet visible => create completion scrollpane
                lastSize = new Dimension(0, 0); // no last size => use (0,0)

                completionScrollPane = new CompletionScrollPane(
                    editorComponent, listSelectionListener,
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent evt) {
			    JTextComponent c = getEditorComponent();
                            if (SwingUtilities.isLeftMouseButton(evt)) {
                                if (completionScrollPane.getView().getSize().width - CompletionJList.arrowSpan() <= evt.getPoint().x) {
                                    CompletionItem selectedItem = completionScrollPane.getSelectedCompletionItem();
                                    if (selectedItem instanceof CompositeCompletionItem && !((CompositeCompletionItem)selectedItem).getSubItems().isEmpty()) {
                                        CompletionImpl.get().showCompletionSubItems();
                                        evt.consume();
                                        return;
                                    }
                                }
                                for (CompletionLayoutPopup popup : getLayout().visiblePopups) {
                                    if (popup instanceof CompletionPopup) {
                                        if (popup == CompletionPopup.this) {
                                            break;
                                        } else {
                                            popup.hide();
                                        }
                                    }
                                }
                                if (c != null && evt.getClickCount() == 2 ) {
                                    CompletionItem selectedItem
                                            = completionScrollPane.getSelectedCompletionItem();
                                    if (selectedItem != null) {
                                        Document doc = c.getDocument();
                                        if (doc instanceof GuardedDocument && ((GuardedDocument)doc).isPosGuarded(c.getSelectionEnd())) {
                                            Toolkit.getDefaultToolkit().beep();
                                        } else {
                                            LogRecord r = new LogRecord(Level.FINE, "COMPL_MOUSE_SELECT"); // NOI18N
                                            r.setParameters(new Object[] { null, completionScrollPane.getSelectedIndex(), selectedItem.getClass().getSimpleName()});
                                            CompletionImpl.uilog(r);
                                            CompletionImpl.sendUndoableEdit(doc, CloneableEditorSupport.BEGIN_COMMIT_GROUP);
                                            MulticaretHandler mch = MulticaretHandler.create(c);
                                            try {
                                                selectedItem.defaultAction(c);
                                            } finally {
                                                mch.release();
                                                CompletionImpl.sendUndoableEdit(doc, CloneableEditorSupport.END_COMMIT_GROUP);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                );
                
                if (shortcutHint != null) {
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    panel.add(completionScrollPane, BorderLayout.CENTER);
                    JLabel label = new JLabel();
                    label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.white),
                            BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.gray), BorderFactory.createEmptyBorder(2, 2, 2, 2))));
                    label.setFont(label.getFont().deriveFont((float)label.getFont().getSize() - 2));
                    label.setHorizontalAlignment(SwingConstants.RIGHT);
                    label.setText(NbBundle.getMessage(CompletionLayout.class, "TXT_completion_shortcut_tips", additionalItemsText, shortcutHint)); //NOI18N
                    panel.add(label, BorderLayout.SOUTH);
                    setContentComponent(panel);
                } else {
                    setContentComponent(completionScrollPane);
                }
            }
            // Set the new data
            getPreferredSize();
            completionScrollPane.setData(data, title, selectedIndex);
            if (location != null) {
                setLocation(location);
            } else {
                setAnchorOffset(anchorOffset);
            }

            Dimension prefSize = getPreferredSize();

            boolean changePopupSize;
            if (isVisible()) {
                changePopupSize = (prefSize.height != lastSize.height)
                        || (prefSize.width != lastSize.width)
                        || anchorOffset != lastAnchorOffset;

            } else { // not visible yet
                changePopupSize = true;
            }

            if (changePopupSize) {
                // Do not change the popup's above/below caret positioning
                // when the popup is already displayed
                getLayout().updateLayout(this);
                
            } // otherwise present popup size will be retained
        }
        
        public CompletionItem getSelectedCompletionItem() {
            return isVisible() ? completionScrollPane.getSelectedCompletionItem() : null;
        }

        public int getSelectedIndex() {
            return isVisible() ? completionScrollPane.getSelectedIndex() : -1;
        }

        public Point getSelectedLocation() {
            return isVisible() ? completionScrollPane.getSelectedLocation() : null;
        }

        public void processKeyEvent(KeyEvent evt) {
            if (isVisible()) {
                Object actionMapKey = completionScrollPane.getInputMap().get(
                        KeyStroke.getKeyStrokeForEvent(evt));
                
                if (actionMapKey != null) {
                    Action action = completionScrollPane.getActionMap().get(actionMapKey);
                    if (action != null) {
                        action.actionPerformed(new ActionEvent(completionScrollPane, 0, null));
                        evt.consume();
                    }
                }
            }
        }

        @Override
        protected int getAnchorHorizontalShift() {
            return COMPLETION_ANCHOR_HORIZONTAL_SHIFT;
        }
    }
    
    private static final class DocPopup extends CompletionLayoutPopup {
        
        private DocumentationScrollPane getDocumentationScrollPane() {
            return (DocumentationScrollPane)getContentComponent();
        }
        
        protected void show(CompletionDocumentation doc, int anchorOffset) {
	    JTextComponent editorComponent = getEditorComponent();
	    if (editorComponent == null) {
		return;
	    }

            if (!isVisible()) { // documentation already visible
                setContentComponent(new DocumentationScrollPane(editorComponent));
            }
            
            getDocumentationScrollPane().setData(doc);
            
            if (!isVisible()) { // do not check for size as it should remain the same
                // Set anchoring only if not displayed yet because completion
                // may have overriden the anchoring
                setAnchorOffset(anchorOffset);
                getLayout().updateLayout(this);
            } // otherwise leave present doc displayed
        }
        
        public void processKeyEvent(KeyEvent evt) {
            if (isVisible()) {
                Object actionMapKey = getDocumentationScrollPane().getInputMap().get(
                        KeyStroke.getKeyStrokeForEvent(evt));
                
                if (actionMapKey != null) {
                    Action action = getDocumentationScrollPane().getActionMap().get(actionMapKey);
                    if (action != null) {
                        action.actionPerformed(new ActionEvent(getDocumentationScrollPane(), 0, null));
                        evt.consume();
                    }
                }
            }
        }
        
        public void clearHistory() {
            if (isVisible()) {
                getDocumentationScrollPane().clearHistory();
            }
        }

        protected int getAnchorHorizontalShift() {
            return COMPLETION_ANCHOR_HORIZONTAL_SHIFT;
        }
    }
    
    private static final class TipPopup extends CompletionLayoutPopup {
        
        protected void show(JToolTip toolTip, int anchorOffset) {
            JComponent lastComponent = null;
            if (isVisible()) { // tooltip already visible
                lastComponent = getContentComponent();
            }
            
            setContentComponent(toolTip);
            setAnchorOffset(anchorOffset);

            // Check whether doc is visible and if so then display
            // on the opposite side
            if (lastComponent != toolTip) {
                getLayout().updateLayout(this);
            }
	}

        public void processKeyEvent(KeyEvent evt) {
            if (isVisible()) {
		if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0).equals(
			KeyStroke.getKeyStrokeForEvent(evt))
		) {
		    evt.consume();
		    CompletionImpl.get().hideToolTip();
		}
            }
        }        
    }    
}
