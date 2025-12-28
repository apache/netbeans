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

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import org.openide.awt.Actions;
import org.openide.awt.MouseUtils;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * A wrapper panel for a single Toolbar. Also contains 'dragger' according to
 * current l&f and is responsible for painting of button dnd feedback.
 *
 * @author S. Aubrecht
 */
final class ToolbarContainer extends JPanel {

    static final String PROP_DRAGGER = "_toolbar_dragger_"; //NOI18N

    private static final Logger LOG = Logger.getLogger(Toolbar.class.getName());

    private final Toolbar toolbar;
    private JComponent dragger;
    private final DnDSupport dnd;
    private final boolean draggable;
    private DropTarget dropTarget;


    private int dropIndex = -1;
    private boolean dropBefore;

    /** TOP of toolbar empty border. */
    private static final int TOP = 2;
    /** LEFT of toolbar empty border. */
    private static final int LEFT = 3;
    /** BOTTOM of toolbar empty border. */
    private static final int BOTTOM = 2;
    /** RIGHT of toolbar empty border. */
    private static final int RIGHT = 3;

    public ToolbarContainer( Toolbar toolbar, final DnDSupport dnd, boolean draggable ) {
        super( new BorderLayout() );
        setOpaque(false);
        this.toolbar = toolbar;
        this.dnd = dnd;
        this.draggable = draggable;
        add( toolbar, BorderLayout.CENTER );
        toolbar.addContainerListener( new ContainerListener() {

            public void componentAdded(ContainerEvent e) {
                dnd.register(e.getChild());
            }

            public void componentRemoved(ContainerEvent e) {
                dnd.unregister(e.getChild());
            }
        });

        String lAndF = UIManager.getLookAndFeel().getID();

        if( lAndF.equals("Windows") ) { //NOI18N
            //Get rid of extra height, also allow for minimalist main
            //window
            toolbar.setBorder( Boolean.getBoolean("netbeans.small.main.window") //NOI18N
                    ? BorderFactory.createEmptyBorder(1,1,1,1)
                    : BorderFactory.createEmptyBorder()); //NOI18N

        } else if( !"Aqua".equals(lAndF) && !"GTK".equals(lAndF) ){ //NOI18N
            Border b = UIManager.getBorder ("Nb.ToolBar.border"); //NOI18N
            if( null == b )
                b = UIManager.getBorder ("ToolBar.border"); //NOI18N

            if( b==null || b instanceof javax.swing.plaf.metal.MetalBorders.ToolBarBorder )
                b = BorderFactory.createEtchedBorder( EtchedBorder.LOWERED );

            toolbar.setBorder( new CompoundBorder( b, new EmptyBorder (TOP, LEFT, BOTTOM, RIGHT) ) );
        } else if( "Aqua".equals(lAndF) ) { //NOI18N
            toolbar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
            toolbar.setOpaque(false);
        }

        if( !"Aqua".equals(lAndF) ) { //NOI18N
            toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if( null == dragger && isDraggable() ) {
            dragger = createDragger();
            dragger.setToolTipText(Actions.cutAmpersand(toolbar.getDisplayName()));
            dragger.addMouseListener(new MouseUtils.PopupMouseAdapter() {

                @Override
                protected void showPopup(MouseEvent evt) {
                    ToolbarConfiguration config = ToolbarConfiguration.findConfiguration( ToolbarPool.getDefault().getConfiguration() );
                    if( null != config ) {
                        config.getContextMenu().show(dragger, evt.getX(), evt.getY());
                    }
                }

            });
            addToolbarDragger();
        }
        registerDnd();
        if( null == dropTarget ) {
            dropTarget = new DropTarget(toolbar, dnd);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = new Dimension(0,0);

        d.height = toolbar.getMinimumSize().height;
        if( toolbar.getComponentCount() <= 1 ) {
            d.width += ToolbarPool.getDefault().getPreferredIconSize();
        } else {
            d.width += toolbar.getComponent(0).getMinimumSize().width;
            if( toolbar.getComponentCount() > 1 )
                d.width += toolbar.getComponent(1).getMinimumSize().width;
        }
        Insets insets = toolbar.getInsets();
        if( null != insets )
            d.width += insets.left + insets.right;
        return d;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        unregisterDnd();
        if( null != dropTarget ) {
            dropTarget.removeDropTargetListener(dnd);
            dropTarget = null;
        }
    }

    Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public String getName() {
        return null == toolbar ? super.getName() : toolbar.getName();
    }

    private void addToolbarDragger() {
        Component oldDragger = null;
        for( Component c : toolbar.getComponents() ) {
            if( !(c instanceof JComponent) )
                continue;
            JComponent jc = (JComponent) c;
            if( Boolean.TRUE.equals( jc.getClientProperty(PROP_DRAGGER) ) ) {
                oldDragger = c;
                break;
            }
        }
        if( null != oldDragger ) {
            dragger = (JComponent) oldDragger;
        } else {
            toolbar.add( dragger, 0 );
        }
    }

    /**
     * Register toolbar content for drag and drop.
     */
    private void registerDnd() {
        for( Component c : toolbar.getComponents() ) {
            if( !(c instanceof JComponent) )
                continue;
            JComponent jc = (JComponent) c;
            Object o = jc.getClientProperty("file");
            //is the component to register a draggable toolbar button?
            if( !(o instanceof DataObject) )
                continue;
            dnd.register(c);
        }
        if( isDraggable() && null != dragger )
            dnd.register(dragger);
    }

    /**
     * Unregister toolbar content for drag and drop to avoid memory leaks.
     */
    private void unregisterDnd() {
        for( Component c : toolbar.getComponents() ) {
            dnd.unregister(c);
        }
        if( null != dragger )
            dnd.unregister(dragger);
    }

    private boolean isDraggable() {
        return draggable;
    }

    private JComponent createDragger() {
        String className = UIManager.getString( "Nb.MainWindow.Toolbar.Dragger" ); //NOI18N
        if( null != className ) {
            try {
                Class klzz = Lookup.getDefault().lookup( ClassLoader.class ).loadClass( className );
                Object inst = klzz.getDeclaredConstructor().newInstance();
                if( inst instanceof JComponent ) {
                    JComponent dragarea = ( JComponent ) inst;
                    dragarea.setCursor( Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );
                    dragarea.putClientProperty(PROP_DRAGGER, Boolean.TRUE);
                    return dragarea;
                }
            } catch( Exception e ) {
                Logger.getLogger(ToolbarContainer.class.getName()).log( Level.INFO, null, e );
            }
        }
        /** Uses L&F's grip **/
        String lfID = UIManager.getLookAndFeel().getID();
        JPanel dragarea = null;
        // #98888: recognize JGoodies L&F properly
        if (lfID.endsWith("Windows")) { //NOI18N
            if (isXPTheme()) {
                dragarea = (JPanel) new ToolbarXP();
            } else {
                dragarea = (JPanel) new ToolbarGrip();
            }
        } else if (lfID.equals("Aqua")) { //NOI18N
            dragarea = (JPanel) new ToolbarAqua();
        } else if (lfID.equals("GTK")) { //NOI18N
            dragarea = (JPanel) new ToolbarGtk();
            //setFloatable(true);
        } else {
            //Default for Metal and uknown L&F
            dragarea = (JPanel)new ToolbarBump();
        }
        dragarea.setCursor( Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );
        dragarea.putClientProperty(PROP_DRAGGER, Boolean.TRUE);
        return dragarea;
    }

    void setDropGesture(int dropIndex, boolean dropBefore) {
        this.dropIndex = dropIndex;
        this.dropBefore = dropBefore;
        repaint();
    }

    @Override
    public void paint( Graphics g ) {
        super.paint(g);
        //paint drop gesture
        if( dropIndex >= 0 ) {
            paintDropGesture(g);
        }
    }

    private void paintDropGesture( Graphics g ) {
        Component c = toolbar.getComponentAtIndex( dropIndex );
        if( null == c )
            return;

        Point location = c.getLocation();
        int cursorLocation = location.x;
        if( !dropBefore ) {
            cursorLocation += c.getWidth();
            if( dropIndex == toolbar.getComponentCount()-1 )
                cursorLocation -= 3;
        }
        drawDropLine( g, cursorLocation );
    }

    private void drawDropLine( Graphics g, int x ) {
        Color oldColor = g.getColor();
        g.setColor( Color.black );
        int height = getHeight();
        g.drawLine( x, 3, x, height-4 );
        g.drawLine( x-1, 3, x-1, height-4 );

        g.drawLine( x+1, 2, x+1+2, 2 );
        g.drawLine( x+1, height-3, x+1+2, height-3 );

        g.drawLine( x-2, 2, x-2-2, 2 );
        g.drawLine( x-2, height-3, x-2-2, height-3 );
        g.setColor( oldColor );
    }

    // ------------------------------------------------------------------------
    // Toolbar draggers for various look and feels
    // ------------------------------------------------------------------------

    /** Bumps for floatable toolbar */
    private final class ToolbarBump extends JPanel {
        /** Top gap. */
        static final int TOPGAP = 2;
        /** Bottom gap. */
        static final int BOTGAP = 2;
        /** Width of bump element. */
        private static final int GRIP_WIDTH = 6;

        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        /** Create new ToolbarBump. */
        public ToolbarBump () {
            super();
            int width = GRIP_WIDTH;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
        }

        /** Paint bumps to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            Dimension size = this.getSize ();
            int height = size.height - BOTGAP;
            g.setColor (this.getBackground ());

            for (int x = 0; x+1 < size.width; x+=4) {
                for (int y = TOPGAP; y+1 < height; y+=4) {
                    g.setColor (this.getBackground ().brighter ());
                    g.drawLine (x, y, x, y);
                    if (x+5 < size.width && y+5 < height) {
                        g.drawLine (x+2, y+2, x+2, y+2);
                    }
                    g.setColor (this.getBackground ().darker ().darker ());
                    g.drawLine (x+1, y+1, x+1, y+1);
                    if (x+5 < size.width && y+5 < height) {
                        g.drawLine (x+3, y+3, x+3, y+3);
                    }
                }
            }
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    } // end of inner class ToolbarBump

    /** Bumps for floatable toolbar GTK L&F */
    private final class ToolbarGtk extends JPanel {
        /** Width of bump element. */
        private static final int GRIP_WIDTH = 6;

        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        /** Create new ToolbarBump. */
        public ToolbarGtk () {
            super();
            int width = GRIP_WIDTH;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
        }

        /** Paint bumps to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            Icon icon = UIManager.getIcon("ToolBar.handleIcon");
            Region region = Region.TOOL_BAR;
            SynthStyleFactory sf = SynthLookAndFeel.getStyleFactory();
            SynthStyle style = sf.getStyle(toolbar, region);
            SynthContext context = new SynthContext(toolbar, region, style, SynthConstants.DEFAULT);

            SynthGraphicsUtils sgu = context.getStyle().getGraphicsUtils(context);
            sgu.paintText(context, g, null, icon, SwingConstants.LEADING, SwingConstants.LEADING, 0, 0, 0, -1, 0);
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            //#154970 for some reason the toolbar's preferred size keeps growing on GTK
            //return new Dimension(GRIP_WIDTH,toolbar.getHeight() - BOTGAP - TOPGAP);
            return new Dimension(GRIP_WIDTH,ToolbarPool.getDefault().getPreferredIconSize());
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    } // end of inner class ToolbarGtk

    /**
     * Recognizes if XP theme is set.
     * @return true if XP theme is set, false otherwise
     */
    private static Boolean isXP = null;
    private static boolean isXPTheme () {
        if (isXP == null) {
            Boolean xp = (Boolean)Toolkit.getDefaultToolkit().
            getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
            isXP = Boolean.TRUE.equals(xp)? Boolean.TRUE : Boolean.FALSE;
        }
        return isXP.booleanValue();
    }

    private final class ToolbarAqua extends JPanel {
        /** Width of grip */
        private static final int GRIP_WIDTH = 8;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        public ToolbarAqua() {
            super( new BorderLayout() );
            JSeparator sep = new JToolBar.Separator();
            sep.setOrientation(JSeparator.VERTICAL);
            sep.setForeground(UIManager.getColor("NbSplitPane.background")); //NOI18N
            add( sep, BorderLayout.CENTER );
            dim = new Dimension (GRIP_WIDTH, GRIP_WIDTH);
            max = new Dimension (GRIP_WIDTH, Integer.MAX_VALUE);
            setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    }

    private final class ToolbarXP extends JPanel {
        /** Width of grip */
        private static final int GRIP_WIDTH = 7;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        public ToolbarXP() {
            dim = new Dimension (GRIP_WIDTH, GRIP_WIDTH);
            max = new Dimension (GRIP_WIDTH, Integer.MAX_VALUE);
        }

        @Override
        public void paintComponent (Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vertical spacing between the dots.
                final int dotSpacingPixels = 4;
                // Minimum vertical space before the first dot and after the last dot.
                final int verticalMargin = 6;
                // The resulting number of dots.
                final int dotCount = (getHeight() - 2 * verticalMargin) / dotSpacingPixels + 1;
                // Center the dots vertically.
                final int startY = (getHeight() - (dotCount - 1) * dotSpacingPixels) / 2;
                final int x = 4;
                g.setColor(UIManager.getColor("controlShadow"));
                for (int i = 0; i < dotCount; i++) {
                    final int y = startY + i * dotSpacingPixels;
                    g.fill(new Ellipse2D.Float(x, y, 1.8f, 1.8f));
                }
            } finally {
              g.dispose();
            }
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize() {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return new Dimension(GRIP_WIDTH,toolbar.getHeight() - 4);
//            return this.getMinimumSize ();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    }

    /** Grip for floatable toolbar, used for Windows Classic L&F */
    private final class ToolbarGrip extends JPanel {
        /** Horizontal gaps. */
        static final int HGAP = 1;
        /** Vertical gaps. */
        static final int VGAP = 2;
        /** Step between two grip elements. */
        static final int STEP = 1;
        /** Width of grip element. */
        private static final int GRIP_WIDTH = 2;

        /** Number of grip elements. */
        int columns;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        /** Create new ToolbarGrip for default number of grip elements. */
        public ToolbarGrip () {
            this(1);
        }

        /** Create new ToolbarGrip for specific number of grip elements.
         * @param col number of grip elements
         */
        public ToolbarGrip (int col) {
            super ();
            columns = col;
            int width = (col - 1) * STEP + col * GRIP_WIDTH + 2 * HGAP;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
            this.setBorder (new EmptyBorder (VGAP, HGAP, VGAP, HGAP));
        }

        /** Paint grip to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            Dimension size = this.getSize();
            int top = VGAP;
            int bottom = size.height - 1 - VGAP;
            int height = bottom - top;
            g.setColor ( this.getBackground() );

            for (int i = 0, x = HGAP; i < columns; i++, x += GRIP_WIDTH + STEP) {
                g.draw3DRect (x, top, GRIP_WIDTH, height, true); // grip element is 3D rectangle now
            }

        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }

    } // end of inner class ToolbarGrip
}
