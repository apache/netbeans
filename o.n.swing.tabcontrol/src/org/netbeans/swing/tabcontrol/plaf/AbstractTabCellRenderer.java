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
/*
 * AbstractTabCellRenderer.java
 *
 * Created on December 2, 2003, 4:13 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDisplayer;

import org.openide.awt.HtmlRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ContainerListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;

/**
 * Base class for tab renderers for the tab control.  This is a support class
 * which will allow authors who want to provide a different look or behavior for
 * tabbed controls with a minimum of coding.  The main methods of interest are
 * <ul><li><code>stateChanged()</code> - where the component should be
 * configured to render a given tab</li><li><code>getState()</code> - where the
 * current state is to be found at the time stateChanged is called</li>
 * </ul>.
 * <p>
 * Typical usage is to pass one or more TabPainter objects to the constructor
 * which will be responsible for doing the actual painting, calling the convenience
 * getters in this class (such as <code>isSelected</code>) to determine how
 * to paint.
 *
 *
 * @author Tim Boudreau
 */
public abstract class AbstractTabCellRenderer extends JLabel
        implements TabCellRenderer {
    private int state = TabState.NOT_ONSCREEN;
    TabPainter leftBorder;
    TabPainter rightBorder;
    TabPainter normalBorder;
    private Dimension padding;

    /**
     * Creates a new instance of AbstractTabCellRenderer
     */
    public AbstractTabCellRenderer(TabPainter leftClip, TabPainter noClip,
                                   TabPainter rightClip, Dimension padding) {
        setOpaque(false);
        setFocusable(false);
        setBorder(noClip);
        normalBorder = noClip;
        leftBorder = leftClip;
        rightBorder = rightClip;
        this.padding = padding;
    }

    public AbstractTabCellRenderer (TabPainter painter, Dimension padding) {
        this (painter, painter, painter, padding);
    }
    
    private boolean showClose = true;
    public final void setShowCloseButton (boolean b) {
        showClose = b;
    }
    
    public final boolean isShowCloseButton() {
        return showClose;
    }

    private Rectangle scratch = new Rectangle();
    public String getCommandAtPoint(Point p, int tabState, Rectangle bounds) {
        setBounds (bounds);
        setState (tabState);
        if (supportsCloseButton(getBorder()) && isShowCloseButton()) {
            TabPainter cbp = (TabPainter) getBorder();
            cbp.getCloseButtonRectangle (this, scratch, bounds);
            if (getClass() != AquaEditorTabCellRenderer.class) {
                //#47408 - hit test area of close button is too small
                scratch.x -=3;
                scratch.y -=3;
                scratch.width += 6;
                scratch.height += 6;
            }
            if (scratch.contains(p)) {
                return TabDisplayer.COMMAND_CLOSE;
            }
        }
        Polygon tabShape = getTabShape (tabState, bounds);
        if (tabShape.contains(p)) {
            return TabDisplayer.COMMAND_SELECT;
        }
        return null;
    }

    public String getCommandAtPoint(Point p, int tabState, Rectangle bounds, int mouseButton, int eventType, int modifiers) {
		String result = null;
		if (mouseButton == MouseEvent.BUTTON2 && eventType == MouseEvent.MOUSE_RELEASED) {
			result = TabDisplayer.COMMAND_CLOSE;
		}
		else {
			result = getCommandAtPoint (p, tabState, bounds);
		}
        if (result != null) {
             if (TabDisplayer.COMMAND_SELECT == result) {
                 boolean clipped = isClipLeft() || isClipRight();
                 if ((clipped && eventType == MouseEvent.MOUSE_RELEASED && mouseButton == MouseEvent.BUTTON1) ||
                     (!clipped && eventType == MouseEvent.MOUSE_PRESSED && mouseButton == MouseEvent.BUTTON1)) {
                     
                     return result;
                 }
             } else if (TabDisplayer.COMMAND_CLOSE == result && eventType == MouseEvent.MOUSE_RELEASED && isShowCloseButton()) {
                 if ((modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0) {
                     return TabDisplayer.COMMAND_CLOSE_ALL;
                 } else if ((modifiers & MouseEvent.ALT_DOWN_MASK) != 0 && mouseButton != MouseEvent.BUTTON2) {
                     return TabDisplayer.COMMAND_CLOSE_ALL_BUT_THIS;
                 } else if( ((tabState & TabState.CLOSE_BUTTON_ARMED) == 0 || (tabState & TabState.MOUSE_PRESSED_IN_CLOSE_BUTTON) == 0)
                         && mouseButton == MouseEvent.BUTTON1 )  {
                     //#208732
                     result = TabDisplayer.COMMAND_SELECT;
                 }
                 return result;
             }
        }
        return null;
    }

    //********************** Subclass convenience API methods*****************
    
    /**
     * Convenience getter to determine if the current state includes the armed
     * state (the mouse is in the tab the component is currently configured to
     * render).
     */
    protected final boolean isArmed() {
        return isPressed() || (state & TabState.ARMED) != 0;
    }

    /**
     * Convenience getter to determine if the current state includes the active
     * state (a component in the container or the container itself has keyboard
     * focus)
     */
    protected final boolean isActive() {
        return (state & TabState.ACTIVE) != 0;
    }

    /**
     * Convenience getter to determine if the current state includes the pressed
     * state (the mouse is in the tab this component is currently configured to
     * render, and the mouse button is currently down)
     */
    protected final boolean isPressed() {
        return (state & TabState.PRESSED) != 0;
    }

    /**
     * Convenience getter to determine if the current state includes the
     * selected state (the tab this component is currently configured to render
     * is the selected tab in a container)
     */
    protected final boolean isSelected() {
        return (state & TabState.SELECTED) != 0;
    }

    /**
     * Convenience getter to determine if the current state includes the
     * right-clipped state (the right hand side of the tab is not visible).
     */
    protected final boolean isClipRight() {
        return (state & TabState.CLIP_RIGHT) != 0;
    }

    /**
     * Convenience getter to determine if the current state includes the
     * left-clipped state (the right hand side of the tab is not visible).
     */
    protected final boolean isClipLeft() {
        return (state & TabState.CLIP_LEFT) != 0;
    }

    /**
     * Convenience getter to determine if the current state indicates
     * that the renderer is currently configured as the leftmost (non-clipped).
     */
    protected final boolean isLeftmost() {
        return (state & TabState.LEFTMOST) != 0;
    }

    /**
     * Convenience getter to determine if the current state indicates
     * that the renderer is currently configured as the rightmost (non-clipped).
     */
    protected final boolean isRightmost() {
        return (state & TabState.RIGHTMOST) != 0;
    }

    protected final boolean isAttention() {
        return (state & TabState.ATTENTION) != 0
                || (state & TabState.HIGHLIGHT) != 0;
    }

    /**
     * 
     * @return True if the tab should be highlighted, false otherwise.
     * @since 1.38
     */
    protected final boolean isHighlight() {
        return (state & TabState.HIGHLIGHT) != 0;
    }

    /**
     * Convenience getter to determine if the current state indicates
     * that the renderer is currently configured appears to the left of
     * the selected tab.
     */
    protected final boolean isNextTabSelected() {
        return (state & TabState.BEFORE_SELECTED) != 0;
    }

    /**
     * Convenience getter to determine if the current state indicates
     * that the renderer is currently configured appears to the left of
     * the armed tab.
     */
    protected final boolean isNextTabArmed() {
        return (state & TabState.BEFORE_ARMED) != 0;
    }

    /**
     * Convenience getter to determine if the current state indicates
     * that the renderer is currently configured appears to the right of
     * the selected tab.
     */
    protected final boolean isPreviousTabSelected() {
        return (state & TabState.AFTER_SELECTED) != 0;
    }

    /**
     * @return True if the tab is busy.
     */
    protected final boolean isBusy() {
        return (state & TabState.BUSY) != 0;
    }
    
    public Dimension getPadding() {
        return new Dimension(padding);
    }

    /** Set the state of the renderer, in preparation for painting it or evaluating a condition
     *  (such as the position of the close button) for which it must be correctly configured).
     *  This method will call stateChanged(), allowing the renderer to reconfigure itself if
     *  necessary, when the state changes.
     *
     * @param state
     */
    protected final void setState(int state) {
        //System.err.println("Renderer SetState " + TabState.stateToString(state));
        boolean needChange = this.state != state;
        if (needChange) {
            int old = this.state;
            //Set the state value here, so isArmed(), etc. will return
            //correct values in stateChanged(), so subclasses can set
            //up colors correctly
            this.state = state;
            int newState = stateChanged(old, state);
            if ((newState & this.state) != state) {
                this.state = state;
                throw new IllegalStateException("StateChanged may add, but not remove bits from the " +
                        "state bitmask.  Expected state: " + TabState.stateToString(
                        state) + " but got " + TabState.stateToString(this.state));
            }
            this.state = newState;
        }
    }

    /**
     * Returns the state as set up in getRendererComponent
     */
    public final int getState() {
        return state;
    }

    /**
     * Implementation of getRendererComponent from TabCellRenderer. This
     * method is final, and will configure the text, bounds and icon correctly
     * according to the passed values, and call setState to set the state of the
     * tab.  Implementers must implement <code>stateChanged()</code> to handle
     * any changes (background color, border, etc) necessary to reflect the
     * current state as returned by <code>getState()</code>.
     */
    public final javax.swing.JComponent getRendererComponent(TabData data,
                                                             Rectangle bounds,
                                                             int state) {
        setBounds(bounds);
        setText(data.getText());
        setIcon(data.getIcon());
        setState(state);
        return this;
    }

    //***************SPI METHODS********************************************
    /*
     * Implementations of this method <strong>may not remove</strong> state bits
     * that were passed in.  A runtime check of the result will be performed,
     * and in the case that some states were removed, a runtime exception will
     * be thrown after this method exits.
     */

    protected int stateChanged(int oldState, int newState) {
        Color bg = isSelected() ?
                isActive() ?
                getSelectedActivatedBackground() : getSelectedBackground() :
                UIManager.getColor("control");
        Color fg = isSelected() ?
                isActive() ?
                getSelectedActivatedForeground() : getSelectedForeground() :
                UIManager.getColor("textText");

        if (isArmed() && isPressed() && (isClipLeft() || isClipRight())) {
            //Create an armed appearance for clipped, pressed tabs, which will respond
            //to mouseReleased, not mousePressed
            bg = getSelectedActivatedBackground();
            fg = getSelectedActivatedForeground();
        }

        if (isClipLeft()) {
            setIcon(null);
            setBorder(leftBorder);
        } else if (isClipRight()) {
            setBorder(rightBorder);
        } else {
            setBorder(normalBorder);
        }

        setBackground(bg);
        setForeground(fg);
        return newState;
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public void revalidate() {
        //do nothing - performance
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public void repaint() {
        //do nothing - performance
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public void validate() {
        //do nothing - performance
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public void repaint(long tm) {
        //do nothing - performance
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public void repaint(long tm, int x, int y, int w, int h) {
        //do nothing - performance
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    protected final void firePropertyChange(String s, Object a, Object b) {
        //do nothing - performance
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public final void addHierarchyBoundsListener(HierarchyBoundsListener hbl) {
        //do nothing
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public final void addHierarchyListener(HierarchyListener hl) {
        //do nothing
    }

    /** Overridden to be a no-op for performance reasons */
    @Override
    public final void addContainerListener(ContainerListener cl) {
        //do nothing
    }

    /**
     * Overridden to paint the interior of the polygon if the border is an instance of TabPainter.
     */
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        if (getBorder() instanceof TabPainter) {
            ((TabPainter) getBorder()).paintInterior(g, this);
        }
        paintIconAndText(g);
    }

    /** Return non-zero to shift the text up or down by the specified number of pixels when painting.
     *
     * @return A positive or negative number of pixels
     */
    protected int getCaptionYAdjustment() {
        return -1;
    }

    /** Return non-zero to shift the icon up or down by the specified number of pixels when painting.
     *
     * @return A positive or negative number of pixels
     */
    protected int getIconYAdjustment() {
        return -1;
    }

    /**
     * Actually paints the icon and text (using the lightweight HTML renderer)
     *
     * @param g The graphics context
     */
    protected void paintIconAndText(Graphics g) {
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics(getFont());
        //Find out what height we need
        int txtH = fm.getHeight();
        Insets ins = getInsets();
        //find out the available height
        int availH = getHeight() - (ins.top + ins.bottom);
        int txtY;
        if (availH > txtH) {
            txtY = txtH + ins.top + ((availH / 2) - (txtH / 2)) - 3;
        } else {
            txtY = txtH + ins.top;
        }
        int txtX;

        int centeringToAdd = getPixelsToAddToSelection() != 0 ?
                getPixelsToAddToSelection() / 2 : 0;

        Icon icon = getIcon();
        //Check the icon non-null and height (see TabData.NO_ICON for why)
        if (!isClipLeft() && icon != null && icon.getIconWidth() > 0
                && icon.getIconHeight() > 0) {
            int iconY;
            if (availH > icon.getIconHeight()) {
                //add 2 to make sure icon top pixels are not cut off by outline
                iconY = ins.top
                        + ((availH / 2) - (icon.getIconHeight() / 2))
                        + 2;
            } else {
                //add 2 to make sure icon top pixels are not cut off by outline
                iconY = ins.top + 2;
            }
            int iconX = ins.left + centeringToAdd;

            iconY += getIconYAdjustment();

            icon.paintIcon(this, g, iconX, iconY);
            txtX = iconX + icon.getIconWidth() + getIconTextGap();
        } else {
            txtX = ins.left + centeringToAdd;
        }

        if (icon != null && icon.getIconWidth() == 0) {
            //Add some spacing so the text isn't flush for, e.g., the
            //welcome screen tab
            txtX += 5;
        }

        txtY += getCaptionYAdjustment();

        //Get the available horizontal pixels for text
        int txtW = getWidth() - (txtX + ins.right);
        if (isClipLeft()) {
            //fiddle with the string to get "...blah"
            String s = preTruncateString(getText(), g, txtW - 4); //subtract 4 so it's not flush w/ tab edge
            Graphics2D g2d = null;
            Shape clip = null;
            if( g instanceof Graphics2D ) {
                g2d = ( Graphics2D ) g;
                clip = g2d.getClip();
                g2d.clipRect( ins.left, ins.top, getWidth()-ins.left-ins.right, getHeight()-ins.top-ins.bottom);
            }
            txtW = (int)HtmlRenderer.renderString(s, g, txtX, txtY, Integer.MAX_VALUE, Integer.MAX_VALUE, getFont(),
                              getForeground(), HtmlRenderer.STYLE_CLIP, false);
            txtX = getWidth()-ins.right-txtW;
            txtW = (int)HtmlRenderer.renderString(s, g, txtX, txtY, txtW, txtH, getFont(),
                              getForeground(), HtmlRenderer.STYLE_CLIP, true);
            if( null != g2d ) {
                g2d.setClip( clip );
            }
        } else {
            String s;
            if (isClipRight()) {
                //Jano wants to always show a "..." for cases where a tab is truncated,
                //even if we've really painted all the text.
                s = getText() + "..."; //NOI18N
            } else {
                s = getText();
            }
            txtW = (int)HtmlRenderer.renderString(s, g, txtX, txtY, txtW, txtH, getFont(),
                              getForeground(), HtmlRenderer.STYLE_TRUNCATE, true);
        }
    }

    static String preTruncateString(String s, Graphics g, int availPixels) {
        if (s.length() < 3) {
            return s;
        }
        s = stripHTML(s);
        if (s.length() < 2) {
            return "..." + s; //NOI18N
        }
        FontMetrics fm = g.getFontMetrics();
        int dotsWidth = fm.stringWidth("..."); //NOI18N
        int beginIndex = s.length() - 2;
        String test = s.substring(beginIndex);
        String result = test;
        while (fm.stringWidth(test) + dotsWidth < availPixels) {
            beginIndex--;
            if (beginIndex <= 0) {
                break;
            } else {
                result = test;
                test = s.substring(beginIndex);
            }
        }
        return "..." + result; //NOI18N
    }

    static boolean isHTML(String s) {
        boolean result = s.startsWith("<html>")
                || s.startsWith("<HTML>"); //NOI18N
        return result;
    }

    static String stripHTML(String s) {
        if (isHTML(s)) {
            StringBuffer result = new StringBuffer(s.length());
            char[] c = s.toCharArray();
            boolean inTag = false;
            for (int i = 0; i < c.length; i++) {
                //XXX need to handle entity includes
                boolean wasInTag = inTag;
                if (!inTag) {
                    if (c[i] == '<') {
                        inTag = true;
                    }
                } else {
                    if (c[i] == '>') {
                        inTag = false;
                    }
                }
                if (!inTag && wasInTag == inTag) {
                    result.append(c[i]);
                }
            }
            return result.toString();
        } else {
            return s;
        }
    }

    /**
     * Get the shape of the tab.  The implementation here will check if the
     * border is an instance of TabPainter, and if so, use the polygon it
     * returns, translating it to the position of the passed-in rectangle. If
     * you are subclassing but do not intend to use TabPainter, you need to
     * override this method
     */
    public Polygon getTabShape(int tabState, Rectangle bounds) {
        setBounds(bounds);
        setState(tabState);
        if (getBorder() instanceof TabPainter) {
            TabPainter pb = (TabPainter) getBorder();
            Polygon p = pb.getInteriorPolygon(this);
            p.translate(bounds.x, bounds.y);
            return p;
        } else {
            //punt and return the bounds as a polygon - what else to do?
            return new Polygon(new int[]{
                bounds.x, bounds.x + bounds.width - 1,
                bounds.x + bounds.width - 1, bounds.x}, new int[]{
                    bounds.y, bounds.y, bounds.y + bounds.height - 1,
                    bounds.y + bounds.height - 1}, 4);
        }
    }




    public Color getSelectedBackground() {
        Color base = UIManager.getColor("control"); //NOI18N
        Color towards = UIManager.getColor("controlHighlight"); //NOI18N

        if (base == null) {
            base = Color.GRAY;
        }
        if (towards == null) {
            towards = Color.WHITE;
        }

        Color result = ColorUtil.adjustTowards(base, 30, towards);
        return result;
    }

    public Color getSelectedActivatedBackground() {
        return UIManager.getColor("TabRenderer.selectedActivatedBackground");
    }

    public Color getSelectedActivatedForeground() {
        return UIManager.getColor("TabRenderer.selectedActivatedForeground");
    }

    public Color getSelectedForeground() {
        return UIManager.getColor("TabRenderer.selectedForeground");
    }

    protected boolean inCloseButton() {
        return (state & TabState.CLOSE_BUTTON_ARMED) != 0;
    }

    /**
     * Subclasses which want to make the selected tab wider than it would otherwise be should return a value
     * greater than 0 here.  The default implementation returns 0.
     */
    public int getPixelsToAddToSelection() {
        return 0;
    }

    private boolean supportsCloseButton(Border b) {
        if (b instanceof TabPainter) {
            return ((TabPainter) b).supportsCloseButton(this);
        } else {
            return false;
        }
    }
}
