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

package org.netbeans.modules.editor.hints.borrowed;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.modules.editor.hints.FixData;
import org.netbeans.modules.editor.hints.HintsControllerImpl;
import org.netbeans.modules.editor.hints.HintsUI;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;

/**
* @author Miloslav Metelka, Dusan Balek
* @version 1.00
*/

public class ListCompletionView extends JList {

    private static final Logger LOG = Logger.getLogger(ListCompletionView.class.getName());
    public static final int COMPLETION_ITEM_HEIGHT = 16;
    private static final int DARKER_COLOR_COMPONENT = 5;
    private final static Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/editor/hints/resources/suggestion.gif", false); // NOI18N
    private final static Icon subMenuIcon;
    private final int fixedItemHeight;
    private final HtmlRenderer.Renderer defaultRenderer = HtmlRenderer.createRenderer();
    private Font font;
    private final RenderComponent renderComponent;
                
    public ListCompletionView() {
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        font = getFont();
        if (font.getSize() < 15 ) {
            font = font.deriveFont(font.getSize2D() + 1);
        }
        
        setFont( font );
        setFixedCellHeight(fixedItemHeight = Math.max(COMPLETION_ITEM_HEIGHT, getFontMetrics(getFont()).getHeight()));
        renderComponent = new RenderComponent();
        setCellRenderer(new ListCellRenderer() {
            private final ListCellRenderer defaultRenderer = new DefaultListCellRenderer();
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if( value instanceof Fix ) {
                    Fix fix = (Fix)value;
                    renderComponent.setFix(fix);
                    renderComponent.setSelected(isSelected);
//                    renderComponent.setSeparator(smartIndex > 0 && smartIndex == index);
                    Color bgColor;
                    Color fgColor;
                    if (isSelected) {
                        bgColor = list.getSelectionBackground();
                        fgColor = list.getSelectionForeground();
                    } else { // not selected
                        bgColor = list.getBackground();
                        if ((index % 2) == 0) { // every second item slightly different
                            bgColor = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
                        }
                        fgColor = list.getForeground();
                    }
                    // quick check Component.setBackground() always fires change
                    if (renderComponent.getBackground() != bgColor) {
                        renderComponent.setBackground(bgColor);
                    }
                    if (renderComponent.getForeground() != fgColor) {
                        renderComponent.setForeground(fgColor);
                    }
                    return renderComponent;

                } else {
                    return defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
                }
            }
        });
        setBorder( BorderFactory.createEmptyBorder() );
        getAccessibleContext().setAccessibleName(LocaleSupport.getString("ACSN_CompletionView"));
        getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_CompletionView"));
    }

    public void setResult(FixData data) {
        if (data != null) {
            Model model = new Model(data);
            
            setModel(model);
            if (model.fixes != null && !model.fixes.isEmpty()) {
                setSelectedIndex(0);
            }
        }
    }

    /** Force the list to ignore the visible-row-count property */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public void up() {
        int size = getModel().getSize();
        if (size > 0) {
            int idx = (getSelectedIndex() - 1 + size) % size;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
            repaint();
        }
    }

    public void down() {
        int size = getModel().getSize();
        if (size > 0) {
            int idx = (getSelectedIndex() + 1) % size;
            if (idx == size)
                idx = 0;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
            validate();
        }
    }

    public void pageUp() {
        if (getModel().getSize() > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.max(getSelectedIndex() - pageSize, 0);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void pageDown() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.min(getSelectedIndex() + pageSize, lastInd);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void begin() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(0);
            ensureIndexIsVisible(0);
        }
    }

    public void end() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(lastInd);
            ensureIndexIsVisible(lastInd);
        }
    }

    public boolean right() {
        Fix f = (Fix) getSelectedValue();
        Iterable<? extends Fix> subfixes = HintsControllerImpl.getSubfixes(f);

        if (subfixes.iterator().hasNext()) {
            Rectangle r = getCellBounds(getSelectedIndex(), getSelectedIndex());
            Point p = new Point(r.getLocation());
            SwingUtilities.convertPointToScreen(p, this);
            p.x += r.width;
//            p.y += r.height;
            HintsUI.getDefault().openSubList(subfixes, p);
            return true;
        }

        return false;
    }

    public @Override void paint(Graphics g) {
        Object value = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
        Map renderingHints = (value instanceof Map) ? (java.util.Map)value : null;
        if (renderingHints != null && g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            RenderingHints oldHints = g2d.getRenderingHints();
            g2d.setRenderingHints(renderingHints);
            try {
                super.paint(g2d);
            } finally {
                g2d.setRenderingHints(oldHints);
            }
        } else {
            super.paint(g);
        }
    }
    
    static class Model extends AbstractListModel implements PropertyChangeListener {

        private FixData data;
        private List<Fix> fixes;
        private boolean computed;
        

        static final long serialVersionUID = 3292276783870598274L;

        public Model(FixData data) {
            this.data = data;
            data.addPropertyChangeListener(this);
            update();
        }

        private synchronized void update() {
            computed = data.isComputed();
            if (computed)
                fixes = data.getSortedFixes();
            else
                data.getSortedFixes();
        }
        
        public synchronized int getSize() {
            return computed ? fixes.size() : 1;
        }

        public synchronized Object getElementAt(int index) {
            if (!computed) {
                return "computing...";
            } else {
                return fixes.get(index);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
//            update();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    HintsUI.getDefault().removePopups();
                    HintsUI.getDefault().showPopup(data);
                }
            });
        }
        
    }

    private Graphics cellPreferredSizeGraphics;
    
    private final class RenderComponent extends JComponent {

        private Fix fix;

        private boolean selected;

        void setFix(Fix fix) {
            this.fix = fix;
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        public @Override void paintComponent(Graphics g) {
            // Although the JScrollPane without horizontal scrollbar
            // is explicitly set with a preferred size
            // it does not force its items with the only width into which
            // they can render (and still leaves them with the preferred width
            // of the widest item).
            // Therefore the item's render width is taken from the viewport's width.
            int itemRenderWidth = ((JViewport)ListCompletionView.this.getParent()).getWidth();
            Color bgColor = getBackground();
            Color fgColor = getForeground();
            int height = getHeight();

            // Clear the background
            g.setColor(bgColor);
            g.fillRect(0, 0, itemRenderWidth, height);
            g.setColor(fgColor);

            // Render the item
            renderHtml(fix, g, ListCompletionView.this.getFont(), getForeground(),
                    itemRenderWidth, getHeight(), selected);

//            if (separator) {
//                g.setColor(Color.gray);
//                g.drawLine(0, 0, itemRenderWidth, 0);
//                g.setColor(fgColor);
//            }
        }

        public @Override Dimension getPreferredSize() {
            if (cellPreferredSizeGraphics == null) {
                // CompletionJList.this.getGraphics() is null
                cellPreferredSizeGraphics = java.awt.GraphicsEnvironment.
                        getLocalGraphicsEnvironment().getDefaultScreenDevice().
                        getDefaultConfiguration().createCompatibleImage(1, 1).getGraphics();
                assert (cellPreferredSizeGraphics != null);
            }
            return new Dimension(getPreferredWidth(fix, cellPreferredSizeGraphics, ListCompletionView.this.getFont()),
                    fixedItemHeight);
        }

    }

    private static final int BEFORE_ICON_GAP = 1;

    private static final int AFTER_ICON_GAP = 4;

    private static final int AFTER_TEXT_GAP = 5;

    private static final int AFTER_RIGHT_ICON_GAP = 3;

    private static int getPreferredWidth(Fix f, Graphics g, Font defaultFont) {
        int width = BEFORE_ICON_GAP + icon.getIconWidth() + AFTER_ICON_GAP + AFTER_TEXT_GAP;
        width += (int)HtmlRenderer.renderHTML(f.getText(), g, 0, 0, Integer.MAX_VALUE, 0,
                defaultFont, Color.black, HtmlRenderer.STYLE_CLIP, false);

        if (HintsControllerImpl.getSubfixes(f).iterator().hasNext()) {
            width += subMenuIcon.getIconWidth() + AFTER_RIGHT_ICON_GAP;
        }
        
        return width;
    }

    public static int arrowSpan() {
        return AFTER_TEXT_GAP + subMenuIcon.getIconWidth() + AFTER_RIGHT_ICON_GAP;
    }

    private void renderHtml(Fix f, Graphics g, Font defaultFont, Color defaultColor,
    int width, int height, boolean selected) {
        if (icon != null) {
            // The image of the ImageIcon should already be loaded
            // so no ImageObserver should be necessary
            g.drawImage(ImageUtilities.icon2Image(icon), BEFORE_ICON_GAP, (height - icon.getIconHeight()) /2, this);
        }
        int iconWidth = BEFORE_ICON_GAP + icon.getIconWidth() + AFTER_ICON_GAP;
        int textEnd = width - AFTER_ICON_GAP - subMenuIcon.getIconWidth() - AFTER_TEXT_GAP;
        FontMetrics fm = g.getFontMetrics(defaultFont);
        int textY = (height - fm.getHeight())/2 + fm.getHeight() - fm.getDescent();

        // Render left text
        if (textEnd > iconWidth) { // any space for left text?
            HtmlRenderer.renderHTML(f.getText(), g, iconWidth, textY, textEnd, textY,
                defaultFont, defaultColor, HtmlRenderer.STYLE_TRUNCATE, true);//, selected);
        }

        if (HintsControllerImpl.getSubfixes(f).iterator().hasNext()) {
            paintArrowIcon(g, textEnd + AFTER_TEXT_GAP, (height - subMenuIcon.getIconHeight()) /2);
        }
    }

    private static void paintArrowIcon(Graphics g, int x, int y) {
        JMenuItem menu = new JMenuItem();

        if (subMenuIconIsSynthIcon) {
            //#181206: the subMenuIcon is a sun.swing.plaf.synth.SynthIcon, whose paintIcon method requires a SynthContext.
            //see also 6635110
            try {
                Region region = SynthLookAndFeel.getRegion(menu);
                SynthStyle style = SynthLookAndFeel.getStyle(menu, region);
                SynthContext c = new SynthContext(menu, region, style, SynthConstants.ENABLED);

                Method paitIcon = synthIcon.getDeclaredMethod("paintIcon", SynthContext.class, Graphics.class, int.class, int.class, int.class, int.class);
                paitIcon.invoke(subMenuIcon, c, g, x, y, subMenuIcon.getIconWidth(), subMenuIcon.getIconHeight());

                return;
            } catch (IllegalAccessException ex) {
                LOG.log(Level.FINE, null, ex);
                return ;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, null, ex);
                return ;
            } catch (InvocationTargetException ex) {
                LOG.log(Level.FINE, null, ex);
                return ;
            } catch (NoSuchMethodException ex) {
                LOG.log(Level.FINE, null, ex);
                return ;
            }
        }

        subMenuIcon.paintIcon( menu, g, x, y);
    }

    private static final Class<?> synthIcon;
    private static final boolean subMenuIconIsSynthIcon;

    static {
        Class<?> icon;
        
        try {
            icon = ClassLoader.getSystemClassLoader().loadClass("sun.swing.plaf.synth.SynthIcon");
        } catch (ClassNotFoundException ex) {
            //OK:
            LOG.log(Level.FINEST, null, ex);
            icon = null;
        }
        
        Icon subMenuIconTemp = UIManager.getIcon("Menu.arrowIcon");
        
        if (subMenuIconTemp == null) {
            LookAndFeel laf = UIManager.getLookAndFeel();
            LOG.log(Level.INFO, "emptyMenuIcon, look and feel: {0}", laf != null ? laf.getClass().getName() : "<null>");
            subMenuIconTemp = new Icon() {
                @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
                @Override public int getIconWidth() {
                    return 0;
                }
                @Override public int getIconHeight() {
                    return 0;
                }
            };
        }
        
        subMenuIcon = subMenuIconTemp;

        synthIcon = icon;
        subMenuIconIsSynthIcon = synthIcon != null && subMenuIcon != null && synthIcon.isAssignableFrom(subMenuIcon.getClass());
    }
}
