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

package org.netbeans.modules.refactoring.java.ui.instant;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.util.Utilities;


/**
 * Completion popup - either completion, documentation or tooltip
 * popup implementations.
 *
 *  @author Dusan Balek, Miloslav Metelka
 */
abstract class CompletionLayoutPopup {
    
    /** Relative width of screen covered by CC */
    static final double COMPL_COVERAGE = 0.4;
    
    /** Relative maximum width of screen covered by CC */
    static final double MAX_COMPL_COVERAGE = 0.9;
    
    private CompletionLayout layout;
    
    private Popup popup;
    
    /** Bounds at which the visible popup has. */
    private Rectangle popupBounds;

    private JComponent contentComponent;
    
    private int anchorOffset;
    
    private Rectangle anchorOffsetBounds;
    
    private boolean displayAboveCaret;
    
    private boolean preferDisplayAboveCaret;
    
    private boolean showRetainedPreferredSize;
    
    public final boolean isVisible() {
        return (popup != null);
    }
    
    public final boolean isActive() {
        return (contentComponent != null);
    }
    
    public final void hide() {
        if (isVisible()) {
            popup.hide();
            popup = null;
            popupBounds = null;
            contentComponent = null;
            anchorOffset = -1;
        }
    }
    
    public final boolean isDisplayAboveCaret() {
        return displayAboveCaret;
    }
    
    public final Rectangle getPopupBounds() {
        return popupBounds;
    }
    
    final void setLayout(CompletionLayout layout) {
        assert (layout != null);
        this.layout = layout;
    }
    
    final void setPreferDisplayAboveCaret(boolean preferDisplayAboveCaret) {
        this.preferDisplayAboveCaret = preferDisplayAboveCaret;
    }
    
    final void setContentComponent(JComponent contentComponent) {
        assert (contentComponent != null);
        this.contentComponent = contentComponent;
    }
    
    final void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
        anchorOffsetBounds = null;
    }
    
    final void setLocation(Point location) {
        this.anchorOffset = -1;
        anchorOffsetBounds = new Rectangle(location);
    }
    
    final int getAnchorOffset() {
	int offset = anchorOffset;
	if (offset == -1) {
	    // Get caret position
	    JTextComponent editorComponent = getEditorComponent();
	    if (editorComponent != null) {
		offset = editorComponent.getSelectionStart();
	    }
	}
	return offset;
    }
    
    final JComponent getContentComponent() {
        return contentComponent;
    }
    
    final Dimension getPreferredSize() {
        JComponent comp = getContentComponent();

        if (comp == null)
            return new Dimension(0, 0);
        
        int screenWidth = Utilities.getUsableScreenBounds().width;
        
        Dimension maxSize = new Dimension((int) (screenWidth * MAX_COMPL_COVERAGE),
                comp.getMaximumSize().height); //set maximum part of screen covered
        setMaxSize(comp, maxSize);
        
        return comp.getPreferredSize();
    }
    
    /** 
     * Sets maximum size for appropriate JComponent, depending on
     * wheteher additional items are present
     */
    private final void setMaxSize(JComponent comp, Dimension maxSize) {
        if (comp instanceof JPanel) {
            comp.getComponent(0).setMaximumSize(maxSize); // JScrollPane
        } else {
            comp.setMaximumSize(maxSize);
        }
    }
    
    final void resetPreferredSize() {
        JComponent comp = getContentComponent();
        if (comp == null){
            return;
        }
        comp.setPreferredSize(null);
    }
    
    final boolean isShowRetainedPreferredSize() {
        return showRetainedPreferredSize;
    }
    
    final CompletionLayout getLayout() {
        return layout;
    }
    
    final JTextComponent getEditorComponent() {
        return layout.getEditorComponent();
    }
    
    protected int getAnchorHorizontalShift() {
        return 0;
    }

    final Rectangle getAnchorOffsetBounds() {
	JTextComponent editorComponent = getEditorComponent();
	if (editorComponent == null) {
	    return new Rectangle();
	}
        if (anchorOffsetBounds == null){ 
            int anchorOffset = getAnchorOffset();
            try {
                anchorOffsetBounds = editorComponent.modelToView(anchorOffset);
                if (anchorOffsetBounds != null){
                    anchorOffsetBounds.x -= getAnchorHorizontalShift();
                } else {
                    anchorOffsetBounds = new Rectangle(); // use empty rectangle
                }
            } catch (BadLocationException e) {
                anchorOffsetBounds = new Rectangle(); // use empty rectangle
            }
            Point anchorOffsetPoint = anchorOffsetBounds.getLocation();
            SwingUtilities.convertPointToScreen(anchorOffsetPoint, editorComponent);
            anchorOffsetBounds.setLocation(anchorOffsetPoint);
        }
        return anchorOffsetBounds;
    }
    
    final Popup getPopup() {
        return popup;
    }
    
    /**
     * Find bounds of the popup based on knowledge of the preferred size
     * of the content component and the preference of the displaying
     * of the popup either above or below the occupied bounds.
     *
     * @param occupiedBounds bounds of the rectangle above or below which
     *   the bounds should be found.
     * @param aboveOccupiedBounds whether the bounds should be found for position
     *   above or below the occupied bounds.
     * @return rectangle with absolute screen bounds of the popup.
     */
    private Rectangle findPopupBounds(Rectangle occupiedBounds, boolean aboveOccupiedBounds) {
        Rectangle screen = Utilities.getUsableScreenBounds();
        Dimension prefSize = getPreferredSize();
        Rectangle popupBounds = new Rectangle();
        
        popupBounds.x = Math.min(occupiedBounds.x,
                (screen.x + screen.width) - prefSize.width);
        popupBounds.x = Math.max(popupBounds.x, screen.x);
        popupBounds.width = Math.min(prefSize.width, screen.width);
        
        if (aboveOccupiedBounds) {
            popupBounds.height = Math.min(prefSize.height,
                    occupiedBounds.y - screen.y - CompletionLayout.POPUP_VERTICAL_GAP);
            popupBounds.y = occupiedBounds.y - CompletionLayout.POPUP_VERTICAL_GAP - popupBounds.height;
        } else { // below caret
            popupBounds.y = occupiedBounds.y
                    + occupiedBounds.height + CompletionLayout.POPUP_VERTICAL_GAP;
            popupBounds.height = Math.min(prefSize.height,
                    (screen.y + screen.height) - popupBounds.y);
        }
        return popupBounds;
    }
    
    /**
     * Create and display the popup at the given bounds.
     *
     * @param popupBounds location and size of the popup.
     * @param displayAboveCaret whether the popup is displayed above the anchor
     *  bounds or below them (it does not be right above them).
     */
    private void show(Rectangle popupBounds, boolean displayAboveCaret) {
        // Hide the original popup if exists
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        
        // Explicitly set the preferred size
        Dimension origPrefSize = getPreferredSize();
        Dimension newPrefSize = popupBounds.getSize();
        JComponent contComp = getContentComponent();
        if (contComp == null){
            return;
        }
        contComp.setPreferredSize(newPrefSize);
        showRetainedPreferredSize = newPrefSize.equals(origPrefSize);
        
        PopupFactory factory = PopupFactory.getSharedInstance();
        // Lightweight completion popups don't work well on the Mac - trying
        // to click on its scrollbars etc. will cause the window to be hidden,
        // so force a heavyweight parent by passing in owner==null. (#96717)
        
        JTextComponent owner = layout.getEditorComponent();
        if(owner != null && owner.getClientProperty("ForceHeavyweightCompletionPopup") != null) {
            owner = null;
        }
        
        // #76648: Autocomplete box is too close to text
        if(displayAboveCaret && Utilities.isMac()) {
            popupBounds.y -= 10;
        }
        
        popup = factory.getPopup(owner, contComp, popupBounds.x, popupBounds.y);
        popup.show();

        this.popupBounds = popupBounds;
        this.displayAboveCaret = displayAboveCaret;
    }
    
    /**
     * Show the popup along the anchor bounds and take
     * the preferred location (above or below caret) into account.
     */
    void showAlongAnchorBounds() {
        showAlongOccupiedBounds(getAnchorOffsetBounds());
    }
    
    void showAlongAnchorBounds(boolean aboveCaret) {
        showAlongOccupiedBounds(getAnchorOffsetBounds(), aboveCaret);
    }
    
    /**
     * Show the popup along the anchor bounds and take
     * the preferred location (above or below caret) into account.
     */
    void showAlongOccupiedBounds(Rectangle occupiedBounds) {
        boolean aboveCaret;
        if (isEnoughSpace(occupiedBounds, preferDisplayAboveCaret)) {
            aboveCaret = preferDisplayAboveCaret;
        } else { // not enough space at preferred location
            // Choose the location with more space
            aboveCaret = isMoreSpaceAbove(occupiedBounds);
        }
        Rectangle bounds = findPopupBounds(occupiedBounds, aboveCaret);
        show(bounds, aboveCaret);
    }

    /**
     * Displays popup right, left of currently occupied bounds if possible,
     * otherwise fallback to above/below
     * @param occupiedBounds bounds of CC popup
     * @param unionBounds bounds occupied by all popups
     */
    void showAlongOrNextOccupiedBounds(Rectangle occupiedBounds, Rectangle unionBounds) {
        if (occupiedBounds != null) {
            Rectangle screen = Utilities.getUsableScreenBounds();
            Dimension prefSize = getPreferredSize();
            Rectangle bounds = new Rectangle();
            boolean aboveCaret;

            if (isEnoughSpace(occupiedBounds, preferDisplayAboveCaret)) {
                aboveCaret = preferDisplayAboveCaret;
            } else
                aboveCaret = false;

            boolean left = false;
            boolean right = false;

            // Right of CC
            if (occupiedBounds.x + occupiedBounds.width + prefSize.width < screen.width &&
                    occupiedBounds.y + prefSize.height < screen.height) {
                bounds.x = occupiedBounds.x + occupiedBounds.width + CompletionLayout.POPUP_VERTICAL_GAP;
                right = true;
            }

            // Left of CC
            if (!right && occupiedBounds.x - prefSize.width > 0 && occupiedBounds.y + prefSize.height < screen.height) {
                bounds.x = occupiedBounds.x - prefSize.width - CompletionLayout.POPUP_VERTICAL_GAP;
                left = true;
            }

            if (right || left) {
                bounds.width = prefSize.width;
                bounds.height = Math.min(prefSize.height, screen.height);
                if (aboveCaret) {
                    bounds.y = occupiedBounds.y + occupiedBounds.height - prefSize.height;
                } else {
                    bounds.y = occupiedBounds.y;
                }
                show(bounds, aboveCaret);
                return;
            }
        }

        // Fallback to Above/Below
        showAlongOccupiedBounds(unionBounds);
    }
    
    void showAlongOccupiedBounds(Rectangle occupiedBounds, boolean aboveCaret) {
        Rectangle bounds = findPopupBounds(occupiedBounds, aboveCaret);
        show(bounds, aboveCaret);
    }
    
    boolean isMoreSpaceAbove(Rectangle bounds) {
        Rectangle screen = Utilities.getUsableScreenBounds();
        int above = bounds.y - screen.y;
        int below = (screen.y + screen.height) - (bounds.y + bounds.height);
        return (above > below);
    }
    
    /**
     * Check whether there is enough space for this popup
     * on its preferred location related to caret.
     */
    boolean isEnoughSpace(Rectangle occupiedBounds) {
        return isEnoughSpace(occupiedBounds, preferDisplayAboveCaret);
    }
    
    /**
     * Check whether there is enough space for this popup above
     * or below the given occupied bounds.
     * 
     * @param occupiedBounds bounds above or below which the available
     *  space should be determined.
     * @param aboveOccupiedBounds whether the space should be checked above
     *  or below the occupiedBounds.
     * @return true if there is enough space for the preferred size of this popup
     *  on the requested side or false if not.
     */
    boolean isEnoughSpace(Rectangle occupiedBounds, boolean aboveOccupiedBounds) {
        Rectangle screen = Utilities.getUsableScreenBounds();
        
        int freeHeight = aboveOccupiedBounds
            ? occupiedBounds.y - screen.y
            : (screen.y + screen.height) - (occupiedBounds.y + occupiedBounds.height);
        Dimension prefSize = getPreferredSize();
        return (prefSize.height < freeHeight);
    }

    boolean isEnoughSpace(boolean aboveCaret) {
        return isEnoughSpace(getAnchorOffsetBounds(), aboveCaret);
    }
    
    public boolean isOverlapped(Rectangle bounds) {
        return isVisible() ? popupBounds.intersects(bounds) : false;
    }

    public boolean isOverlapped(CompletionLayoutPopup popup) {
        return popup.isVisible() ? isOverlapped(popup.getPopupBounds()) : false;
    }
    
    public Rectangle unionBounds(Rectangle bounds) {
        return isVisible() ? bounds.union(getPopupBounds()) : bounds;
    }

    public abstract void processKeyEvent(KeyEvent evt);
}
