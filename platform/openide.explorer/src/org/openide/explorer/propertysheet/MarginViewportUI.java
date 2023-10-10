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
package org.openide.explorer.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ViewportUI;
import org.openide.awt.GraphicsUtils;


/** Viewport UI which will paint a margin if the contained
 * SheetTable is showing one, so the margin appears to continue
 * down to the bottom of the component.
 *
 * @author  Tim Boudreau
 */
class MarginViewportUI extends ViewportUI implements ComponentListener, ContainerListener {
    private JViewport viewport;
    private int lastHeight = -1;
    private int stringWidth = -1;
    private int stringHeight = -1;
    private int ascent = -1;
    Rectangle scratch = new Rectangle();
    private String emptyString = "THIS IS A BUG"; //NOI18N
    Color marginColor = UIManager.getColor("controlShadow"); //NOI18N
    private int marginWidth = PropUtils.getMarginWidth();
    private boolean marginPainted = false;
    Dimension lastKnownSize = new Dimension();

    /** Creates a new instance of MarginViewportUI */
    private MarginViewportUI(JViewport jv) {
        this.viewport = jv;
    }

    /** Uses a single shared instance, as does BasicViewportUI */
    public static ComponentUI createUI(JComponent c) {
        return new MarginViewportUI((JViewport) c);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        //Fetch the "no properties" string - it's not going to change
        //for the life of the session
        //        noPropsString = NbBundle.getMessage(MarginViewportUI.class,
        //            "CTL_NoProperties"); //NOI18N
        //Set an appropriate font and color.  Only really relevant on OS-X to
        //keep the font consistent with other NB fonts
        Color fg = UIManager.getColor("controlShadow"); //NOI18N

        if (fg == null) {
            fg = Color.LIGHT_GRAY;
        }

        c.setForeground(fg);

        Color bg = UIManager.getColor("Tree.background"); //NOI18N

        if (bg == null) {
            bg = Color.WHITE;
        }

        c.setBackground(bg);

        Font f = UIManager.getFont("Tree.font"); //NOI18N

        if (f == null) {
            f = UIManager.getFont("controlFont"); //NOI18N
        }

        if (f != null) {
            c.setFont(f);
        }

        c.addContainerListener(this);

        Component[] kids = c.getComponents();

        for (int i = 0; i < kids.length; i++) {
            //Should almost always be empty anyway, if not only one component,
            //but for completeness...
            kids[i].addComponentListener(this);
        }
    }

    @Override
    public void uninstallUI(JComponent vp) {
        JViewport jv = (JViewport) vp;
        Component[] c = jv.getComponents();

        for (int i = 0; i < c.length; i++) {
            c[i].removeComponentListener(this);
        }

        jv.removeContainerListener(this);
    }

    public void setEmptyString(String s) {
        emptyString = s;
        stringWidth = -1;
        stringHeight = -1;
    }

    public void setMarginColor(Color c) {
        marginColor = c;
    }

    public void setMarginWidth(int margin) {
        this.marginWidth = margin;
    }

    public void setMarginPainted(boolean val) {
        if (marginPainted != val) {
            marginPainted = val;
            viewport.repaint();
        }
    }

    /** Overridden to draw "no properties" if necessary */
    @Override
    public void paint(Graphics g, JComponent c) {
        GraphicsUtils.configureDefaultRenderingHints(g);
        Component view = ((JViewport) c).getView();

        if (view != null) {
            lastKnownSize = view.getSize();
        }

        if (stringWidth == -1) {
            calcStringSizes(c.getFont(), g);
        }

        //Update will have set paintNoProps to the correct value
        if (shouldPaintEmptyMessage()) {
            //We need to paint centered "<No Properties>" text
            g.setFont(c.getFont());
            g.setColor(c.getForeground());

            Rectangle r = getEmptyMessageBounds();

            //See if we really need to do any painting
            if (g.hitClip(r.x, r.y, r.width, r.height)) {
                //Paint the string
                g.drawString(emptyString, r.x, r.y + ascent);
            }
        }
    }

    private void calcStringSizes(Font f, Graphics g) {
        FontMetrics fm = g.getFontMetrics(f);
        stringWidth = fm.stringWidth(emptyString);
        stringHeight = fm.getHeight();
        ascent = fm.getMaxAscent();
    }

    private Rectangle getEmptyMessageBounds() {
        Insets ins = viewport.getInsets();

        scratch.x = ins.left + (((viewport.getWidth() - (ins.left + ins.right)) / 2) - (stringWidth / 2));

        scratch.y = ins.top + (((viewport.getHeight() - (ins.top + ins.bottom)) / 2) - (stringHeight / 2));

        scratch.width = stringWidth;
        scratch.height = stringHeight;

        return scratch;
    }

    @Override
    public void update(Graphics g, JComponent c) {
        g.setColor(c.getBackground());

        boolean margin = shouldPaintMargin();

        int leftEdge = margin ? marginWidth : 0;
        g.fillRect(leftEdge, 0, c.getWidth() - leftEdge, c.getHeight());

        if (margin) {
            g.setColor(marginColor);
            g.fillRect(0, 0, marginWidth, c.getHeight());
        }

        paint(g, c);
    }

    private void scheduleRepaint(Dimension nuSize) {
        if (!marginPainted && ((nuSize.height > 10) == (lastKnownSize.height > 10))) {
            //            return;
        }

        int heightDif = Math.abs(nuSize.height - lastKnownSize.height);

        if (heightDif == 0) {
            //            return;
        }

        //        if (heightDif != 0) {
        Insets ins = viewport.getInsets();

        /*
                    int left = ins.left;
                    int top = nuSize.height + ins.top;
                    int width = marginWidth;
                    int height = lastKnownSize.height - nuSize.height;

                    viewport.repaint (left, top, width, height);
                    */
        viewport.repaint(ins.left, ins.top, marginWidth, viewport.getHeight() - (ins.top + ins.bottom));

        //        }
        //        if (nuSize.height < 10) {
        Rectangle r = getEmptyMessageBounds();
        viewport.repaint(r.x, r.y, r.width, r.height);

        //        }
    }

    private boolean shouldPaintEmptyMessage() {
        Dimension d = viewport.getView().getSize();

        return d.height < 10;
    }

    private boolean shouldPaintMargin() {
        return marginPainted & !shouldPaintEmptyMessage();
    }

    public void componentAdded(ContainerEvent e) {
        e.getChild().addComponentListener(this);
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentRemoved(ContainerEvent e) {
        e.getChild().removeComponentListener(this);
    }

    public void componentResized(ComponentEvent e) {
        scheduleRepaint(((Component) e.getSource()).getSize());
    }

    public void componentShown(ComponentEvent e) {
        scheduleRepaint(((Component) e.getSource()).getSize());
    }
}
