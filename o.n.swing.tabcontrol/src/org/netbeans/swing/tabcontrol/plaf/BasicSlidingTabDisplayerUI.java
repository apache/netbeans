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

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Comparator;
import org.netbeans.swing.tabcontrol.TabData;

/** Common UI for sliding tabs.  Simply uses JToggleButtons for displayers,
 * since the contents of the data model are not expected to change often,
 * and no scrolling behavior needs to be supported for the tabs area.
 * <p>
 * Note that the &quot;sliding&quot; is provided by an instance of <code>FxProvider</code>
 * provided in the <code>DefaultTabbedContainerUI</code>, not here.
 * <p>
 * To change the appearance of the buttons, simply provide a subclass of <code>SlidingTabDisplayerButtonUI</code>
 * via UIDefaults.  This class is final.
 *
 * @author  Tim Boudreau
 */
public final class BasicSlidingTabDisplayerUI extends AbstractTabDisplayerUI {
    private Rectangle scratch = new Rectangle();
    
    /** Creates a new instance of BasicSlidingTabDisplayerUI */
    public BasicSlidingTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }
    
    public static ComponentUI createUI (JComponent c) {
        return new BasicSlidingTabDisplayerUI((TabDisplayer) c);
    }
    
    @Override
    protected void install() {
        displayer.setLayout (new OrientedLayoutManager());
        syncButtonsWithModel();
    }
    
    @Override
    protected Font createFont() {
        //XXX Sideways text is more readable with a slightly larger bold font
        Font f = super.createFont();
                // don't use deriveFont() - see #49973 for details
        f = new Font(f.getName(), Font.BOLD, f.getSize() + 1);
        return f;
    }

    @Override
    protected void uninstall() {
        displayer.removeAll();
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        return displayer.getLayout().preferredLayoutSize(c);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return displayer.getLayout().minimumLayoutSize(c);
    }

    private int buttonCount = 0;
    private boolean syncButtonsWithModel() {
        assert SwingUtilities.isEventDispatchThread();
        
        int count = displayer.getModel().size();
        boolean changed = false;
        buttonCount = displayer.getComponentCount();

        if (count != buttonCount) {
            synchronized (displayer.getTreeLock()) {
                while (count < buttonCount) {
                    if (buttonCount-- > 0) {
                        displayer.remove (buttonCount- 1);
                        changed = true;
                    }
                }
                while (count > buttonCount) {
                    IndexButton ib = new IndexButton (buttonCount++);
                    ib.setFont (displayer.getFont());
                    displayer.add (ib);
                    changed = true;
                }
                Component[] c = displayer.getComponents();
                for (int i=0; i < c.length; i++) {
                    if (c[i] instanceof IndexButton) {
                        changed |= ((IndexButton) c[i]).checkChanged();
                    }
                }
            }
        }
        return changed;
    }
    
    /** Not used so much to determine layout as to calculate preferred sizes
     * here
     */
    @Override
    protected TabLayoutModel createLayoutModel() {
       DefaultTabLayoutModel result = new DefaultTabLayoutModel(displayer.getModel(), displayer);
       result.setPadding (new Dimension (15, 2));
       return result;
    }
    
    @Override
    protected MouseListener createMouseListener() {
        return new MouseAdapter() {};
    }
    
    @Override
    public void requestAttention (int tab) {
        //not implemented
    }
    
    @Override
    public void cancelRequestAttention (int tab) {
        //not implemented
    }
    
    @Override
    protected ChangeListener createSelectionListener() {
        return new ChangeListener() {
            private int lastKnownSelection = -1;
            @Override
            public void stateChanged (ChangeEvent ce) {
                int selection = selectionModel.getSelectedIndex();
                if (selection != lastKnownSelection) {
                    if (lastKnownSelection != -1) {
                        IndexButton last = findButtonFor(lastKnownSelection);
                        if (last != null) {
                            last.getModel().setSelected(false);
                        }
                    }
                    if (selection != -1) {
                        IndexButton current = findButtonFor (selection);
                        if (displayer.getComponentCount() == 0) {
                            syncButtonsWithModel();
                        }
                        if (current != null) {
                            current.getModel().setSelected (true);
                        }
                    }
                }
                lastKnownSelection = selection;
            }
        };
    }
    
    @Override
    public Polygon getExactTabIndication(int index) {
        return new EqualPolygon (findButtonFor(index).getBounds());
    }
    
    @Override
    public Polygon getInsertTabIndication(int index) {
        Rectangle r = findButtonFor (index).getBounds();
        Polygon result = new EqualPolygon (findButtonFor(index).getBounds());
        return result;
    }
    
    private IndexButton findButtonFor (int index) {
        Component[] c = displayer.getComponents();
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof IndexButton && ((IndexButton) c[i]).getIndex() == index) {
                return (IndexButton) c[i];
            }
        }
        return null;
    }
    
    @Override
    public Rectangle getTabRect(int index, Rectangle destination) {
        if (destination == null) {
            destination = new Rectangle();
        }
        IndexButton ib = findButtonFor(index);
        if (ib != null) {
            destination.setBounds (ib.getBounds());
        } else {
            destination.setBounds (-20, -20, 0, 0);
        }
        return destination;
    }
    
    @Override
    public int tabForCoordinate(Point p) {
        Component[] c = displayer.getComponents();
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof IndexButton) {
                if (c[i].contains(p)) {
                    return ((IndexButton) c[i]).getIndex();
                }
            }
        }
        return -1;
    }

    protected final class SlidingPropertyChangeListener extends AbstractTabDisplayerUI.DisplayerPropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            if (TabDisplayer.PROP_ORIENTATION.equals(e.getPropertyName())) {
                displayer.revalidate();
            }
        }
    }
    
    private Object getDisplayerOrientation() {
        return displayer.getClientProperty (TabDisplayer.PROP_ORIENTATION);
    }

     /** Paints the rectangle occupied by a tab into an image and returns the result */
    @Override
    public Image createImageOfTab(int index) {
        TabData td = displayer.getModel().getTab(index);
        
        JLabel lbl = new JLabel(td.getText());
        int width = lbl.getFontMetrics(lbl.getFont()).stringWidth(td.getText());
        int height = lbl.getFontMetrics(lbl.getFont()).getHeight();
        width = width + td.getIcon().getIconWidth() + 6;
        height = Math.max(height, td.getIcon().getIconHeight()) + 5;
        
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                        .getDefaultScreenDevice().getDefaultConfiguration();
        
        BufferedImage image = config.createCompatibleImage(width, height);
        Graphics2D g = image.createGraphics();
        g.setColor(lbl.getForeground());
        g.setFont(lbl.getFont());
        td.getIcon().paintIcon(lbl, g, 0, 0);
        g.drawString(td.getText(), 18, height / 2);
        
        return image;

    }

    /**
     * JToggleButton subclass which maps to an index in the data model, and displays
     * whatever the content of the data model at that index is.  Buttons are added or removed
     * from the tab displayer as the model changes.  This class is public to allow
     * alternate UIs for the buttons to be provided via subclasses of <code>SlidingTabDisplayerButtonUI</code>.
     */
    public final class IndexButton extends JToggleButton implements ActionListener {
        private int index;
        private String lastKnownText = null;
        private Icon lastKnownIcon = null;

        /** UI Class ID for IndexButtons, to be used by providers of UI delegates */
        public static final String UI_KEY = "IndexButtonUI"; //NOI18N

        /** Create a new button representing an index in the model.  The index is immutable for the life of the
         * button.
         *
         * @param index The index
         */
        public IndexButton (int index) {
            this.index = index;
            addActionListener(this);
            setFont (displayer.getFont());
            setFocusable(false);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }

        /** Accessor for the UI delegate to determine if the tab displayer is currently active */
        public boolean isActive() {
            return displayer.isActive();
        }

        @Override
        public void updateUI () {
            SlidingTabDisplayerButtonUI ui = null;
            try {
                ui = (SlidingTabDisplayerButtonUI) UIManager.getUI(this);
                setUI (ui);
                return;
            } catch (Error e) {
                System.err.println ("Error getting sliding button UI: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println ("Exception getting button UI: " + ex.getMessage());
            }
            setUI ((ButtonUI) SlidingTabDisplayerButtonUI.createUI(this));
        }

        @Override
        public String getUIClassID() {
            return UI_KEY;
        }

        /** Accessor for the UI delegate - orientation will be one of the constants defined on
         * TabDisplayer */
        public Object getOrientation() {
            return getDisplayerOrientation();
        }
        
        @Override
        public String getText() {
            if (index == -1) {
                //We're being called in the superclass constructor when the UI is
                //assigned
                return "";
            }
            if (index < displayer.getModel().size()) {
                lastKnownText = displayer.getModel().getTab(index).getText();
            } else {
                return "This tab doesn't exist."; //NOI18N
            }
            return lastKnownText;
        }
        
        @Override
        public String getToolTipText() {
            return displayer.getModel().getTab(index).getTooltip();
        }

        /** Implementation of ActionListener - sets the selected index in the selection model */
        @Override
        public final void actionPerformed(ActionEvent e) {
            if (!isSelected()) {
                selectionModel.setSelectedIndex (-1);
            } else {
                selectionModel.setSelectedIndex (index);
            }
        }

        /** Get the index into the data model that this button represents */
        public int getIndex() {
            return index;
        }
        
        @Override
        public Icon getIcon() {
            if (index == -1) {
                //We're being called in the superclass constructor when the UI is
                //assigned
                return null;
            }
            if (index < displayer.getModel().size()) {
                lastKnownIcon = displayer.getModel().getTab(index).getIcon();
            }
            return lastKnownIcon;
        }

        /**
         * Test if the text or icon in the model has changed since the last time <code>getText()</code> or
         * <code>getIcon()</code> was called.  If a change has occured, the button will fire the appropriate
         * property changes, including preferred size, to ensure the tab displayer is re-laid out correctly.
         * This method is called when a change happens in the model over the index this button represents.
         *
         * @return true if something has changed
         */
        final boolean checkChanged() {
            boolean result = false;
            Icon ic = lastKnownIcon;
            Icon nue = getIcon();
            if (nue != ic) {
                firePropertyChange ("icon", lastKnownIcon, nue); //NOI18N
                result = true;
            }
            String txt = lastKnownText;
            String nu = getText();
            if (nu != txt) { //Equality compare probably not needed
                firePropertyChange ("text", lastKnownText, getText()); //NOI18N
                result = true;
            }
            if (result) {
                firePropertyChange ("preferredSize", null, null); //NOI18N
            }
            return result;
        }
    }

    @Override
    protected void modelChanged() {
        if (syncButtonsWithModel()) {
            displayer.validate();
        }
    }

    @Override
    public Icon getButtonIcon(int buttonId, int buttonState) {
        return null;
    }

    private static final Comparator<Component> BUTTON_COMPARATOR = new IndexButtonComparator();
    private static class IndexButtonComparator implements Comparator<Component> {
        @Override
        public int compare(Component o1, Component o2) {
            if (o2 instanceof IndexButton && o1 instanceof IndexButton) {
                return ((IndexButton) o1).getIndex() - ((IndexButton) o2).getIndex();
            }
            return 0;
        }
    }
    
    private final class OrientedLayoutManager implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
            //do nothing
        }
        
        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                syncButtonsWithModel();
                Component[] c = parent.getComponents();
                Arrays.sort (c, BUTTON_COMPARATOR);
                for (int i=0; i < c.length; i++) {
                    if (c[i] instanceof IndexButton) {
                        boundsFor ((IndexButton) c[i], scratch);
                        c[i].setBounds (scratch);
                    }
                }
            }
        }
        
        private void boundsFor (IndexButton b, Rectangle r) {
            Object orientation = getDisplayerOrientation();
            boolean flip = orientation == TabDisplayer.ORIENTATION_EAST || 
                orientation == TabDisplayer.ORIENTATION_WEST;
            int index = b.getIndex();
            
            if (index >= displayer.getModel().size() || index < 0) {
                r.setBounds (-20, -20, 0, 0);
                return;
            }

            r.x = layoutModel.getX(index);
            r.y = layoutModel.getY(index);
            r.width = layoutModel.getW(index);
            r.height = layoutModel.getH(index);
            if (flip) {
                int tmp = r.x;
                r.x = r.y;
                r.y = tmp;
                
                tmp = r.width;
                r.width = r.height;
                r.height = tmp;
            }
        }
        
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }
        
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Object orientation = getDisplayerOrientation();
            boolean flip = orientation == TabDisplayer.ORIENTATION_EAST || 
                orientation == TabDisplayer.ORIENTATION_WEST;
            
            int max = displayer.getModel().size();
            Dimension result = new Dimension();
            for (int i=0; i < max; i++) {
                result.height = Math.max (result.height, layoutModel.getH(i));
                result.width += layoutModel.getW(i);
            }
            if (flip) {
                int tmp = result.height;
                result.height = result.width;
                result.width = tmp;
            }
            return result;
        }
        
        @Override
        public void removeLayoutComponent(Component comp) {
            //do nothing
        }
    }
}
