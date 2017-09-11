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


package org.netbeans.beaninfo.editors;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.*;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;


/** A property editor for Color class.
 * (Final only for performance, can be unfinaled if desired).
 *
 * @author   Jan Jancura, Ian Formanek
 */
public final class ColorEditor implements PropertyEditor, XMLPropertyEditor {
    
    // static .....................................................................................
    
    /** AWT Palette mode. */
    public static final int AWT_PALETTE = 1;
    /** System Palette mode. */
    public static final int SYSTEM_PALETTE = 2;
    /** Swing Palette mode. */
    public static final int SWING_PALETTE = 3;

    /** Localized names of AWT colors. */
    /* package */ static String awtColorNames[]; // for testing

    /** AWT colors used in AWT Palette. */
    private static final Color awtColors[] = {
        Color.white, Color.lightGray, Color.gray, Color.darkGray,
        Color.black, Color.red, Color.pink, Color.orange, Color.yellow,
        Color.green, Color.magenta, Color.cyan, Color.blue };

    /** Names of system colors. <em>Note:</em> not localizable,
     * those names corresponds to programatical names. */
    private static final String awtGenerate[] = {
        "white", "lightGray", "gray", "darkGray", // NOI18N
        "black", "red", "pink", "orange", "yellow", // NOI18N
        "green", "magenta", "cyan", "blue" }; // NOI18N
    
    /** Localized names of system colors. */
    private static String systemColorNames[];

    /** Names of system colors. <em>Note:</em> not localizable,
     * those names corresponds to programatical names. */
    private static final String systemGenerate[] = {
        "activeCaption", "activeCaptionBorder", // NOI18N
        "activeCaptionText", "control", "controlDkShadow", // NOI18N
        "controlHighlight", "controlLtHighlight", // NOI18N
        "controlShadow", "controlText", "desktop", // NOI18N
        "inactiveCaption", "inactiveCaptionBorder", // NOI18N
        "inactiveCaptionText", "info", "infoText", "menu", // NOI18N
        "menuText", "scrollbar", "text", "textHighlight", // NOI18N
        "textHighlightText", "textInactiveText", "textText", // NOI18N
        "window", "windowBorder", "windowText"}; // NOI18N

    /** System colors used in System Palette. */
    private static final Color systemColors[] = {
        SystemColor.activeCaption, SystemColor.activeCaptionBorder,
        SystemColor.activeCaptionText, SystemColor.control,
        SystemColor.controlDkShadow, SystemColor.controlHighlight,
        SystemColor.controlLtHighlight, SystemColor.controlShadow,
        SystemColor.controlText, SystemColor.desktop,
        SystemColor.inactiveCaption, SystemColor.inactiveCaptionBorder,
        SystemColor.inactiveCaptionText, SystemColor.info,
        SystemColor.infoText, SystemColor.menu,
        SystemColor.menuText, SystemColor.scrollbar, SystemColor.text,
        SystemColor.textHighlight, SystemColor.textHighlightText,
        SystemColor.textInactiveText, SystemColor.textText,
        SystemColor.window, SystemColor.windowBorder,
        SystemColor.windowText};

    /** Swing colors names and values are static and lazy initialized.
     * They are also cleared when l&f changes. */
    private static String swingColorNames[];
    
    /** Swing colors used in Swing Palette. */
    private static Color swingColors[];

    static final boolean GTK = "GTK".equals(UIManager.getLookAndFeel().getID());//NOI18N
    static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID());//NOI18N
    
    private static final boolean antialias = Boolean.getBoolean("nb.cellrenderer.antialiasing") // NOI18N
         ||Boolean.getBoolean("swing.aatext") // NOI18N
         ||(GTK && gtkShouldAntialias()) // NOI18N
         ||AQUA; 

    private static Boolean gtkAA;
    private static Map hintsMap;
    
    
    // static initializer .........................................
    
    static {
        UIManager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                swingColorNames = null;
                swingColors = null;
            }
        });
        
        swingColorNames = null;
        swingColors = null;
    }

    // variables ..................................................................................
    
    /** Selected color. */
    private SuperColor superColor;
    /** Property change support. Helper field. */
    private PropertyChangeSupport support;


    /** Gets <code>staticChooser</code> instance. */
    public static JColorChooser getStaticChooser(ColorEditor ce) {
        JColorChooser staticChooser = new JColorChooser(new SuperColorSelectionModel())
                            {
                                public void setColor (Color c) {
                                    if (c == null) return;
                                    super.setColor (c);
                                }
                            };
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (AWT_PALETTE, getAWTColorNames(), awtColors,
                                         getString ("CTL_AWTPalette"), ce)
            );
            initSwingConstants();
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (SWING_PALETTE, swingColorNames, swingColors,
                                         getString ("CTL_SwingPalette"), ce)
            );
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (SYSTEM_PALETTE, getSystemColorNames(), systemColors,
                                         getString ("CTL_SystemPalette"), ce)
            );
        return staticChooser;
    }

    private static class SuperColorSelectionModel extends DefaultColorSelectionModel {
        private int reenter = -1;
        @Override
        public void setSelectedColor(Color color) {
            try {
                reenter++;
                if (color instanceof SuperColor) {
                    super.setSelectedColor(color);
                } else if (color instanceof Color && reenter == 0) {
                    // Bug 230371: ColorChooserPanel.propertyChange in JDK 1.7
                    // causes re-entrant call losing the SuperColor set above.
                    super.setSelectedColor(new SuperColor(color));
                }
            } finally {
                reenter--;
            }
        }
    }

    // init .......................................................................................

    /** Creates color editor. */
    public ColorEditor() {
        support = new PropertyChangeSupport (this);
    }


    // main methods .......................................................................................

    /** Gets value. Implements <code>PropertyEditor</code> interface.
     * @return <code>Color</code> value or <code>null</code> */
    public Object getValue () {
        if (superColor != null) {
            if (superColor.getID () != null) {
                return superColor;
            } else {
                return superColor.getColor ();
            }
            
        } else {
            return null;
        }
    }

    /** Sets value. Implements <code>PropertyEditor</code> interface.
     * @param object object to set, accepts <code>Color</code> 
     * or <code>SuperColor<code> types */
    public void setValue (Object object) {
        if(object != null) {
            if (object instanceof SuperColor) {
                superColor = (SuperColor) object;
            } 
            else if (object instanceof Color) {
                superColor = new SuperColor((Color) object);
            }
        } 
        else {
            superColor = null;
        }

        support.firePropertyChange ("", null, null); // NOI18N
    }

    /** Gets value as text. Implements <code>PropertyEditor</code> interface. */
    public String getAsText () {
        if (superColor == null)
            return "null"; // NOI18N
        return superColor.getAsText ();
    }

    /** Sets value ad text. Implements <code>PropertyEditor</code> interface. */
    public void setAsText(String text) throws IllegalArgumentException {
        if(text == null) {
            throw new IllegalArgumentException("null parameter"); // NOI18N
        }
        
        text = text.trim();
        
        if("null".equals(text)) { // NOI18N
            setValue(null);
            return;
        }

        try { // try to extract RGB values - represented as [r,g,b] or r,g,b
            int len = text.length();
            if (len > 0) {
                int start = -1;
                int end = -1;

                char c1 = text.charAt(0);
                char c2 = text.charAt(len-1);
                if (c1 == '[' && c2 == ']') {
                    start = 1;
                    end = len - 1;
                }
                else if (c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
                    start = 0;
                    end = len;
                }

                if (start >= 0) {
                    int index1 = text.indexOf(',');
                    int index2 = index1 < 0 ? -1 : text.indexOf(',', index1+1);

                    if (index1 >= 0 && index2 >= 0) {
                        int red = Integer.parseInt(text.substring(
                                                        start, index1).trim());
                        int green = Integer.parseInt(text.substring(
                                                          index1 + 1, index2).trim());
                        int blue = Integer.parseInt(text.substring(
                                                         index2 + 1, end).trim());

                        try {
                            setValue(new SuperColor(null,
                                                    0,
                                                    new Color(red, green, blue)));
                            return;
                        } catch( IllegalArgumentException iaE ) {
                            UIExceptions.annotateUser(iaE, null,
                                                     iaE.getLocalizedMessage(),
                                                     null, null);
                            throw iaE;
                        }
                    }
                }
            }
        } catch(NumberFormatException nfe) {
            // Ignore it and try out from palette's next.
        }
 
        int index;
        int palette = 0;
        Color color = null;

        if ((index = getIndex(awtGenerate, text)) >= 0) {
            text = getAWTColorNames()[index];
        }
        if((index = getIndex(getAWTColorNames(), text)) >= 0) {
            palette = AWT_PALETTE;
            color = awtColors[index];
        }

        if(index < 0 && ((index = getIndex(getSystemColorNames(), text)) >= 0)) {
            palette = SYSTEM_PALETTE;
            color = systemColors[index];
        }

        if(index < 0) {
            initSwingConstants();
            if((index = getIndex(swingColorNames, text)) >= 0) {
                palette = SWING_PALETTE;
                color = swingColors[index];
            }
        }

        if(index < 0) {
            String msg = MessageFormat.format (
                NbBundle.getMessage (ColorEditor.class, "FMT_IllegalEntry"), 
                new Object[]{text});
            IllegalArgumentException iae = new IllegalArgumentException (text);
            UIExceptions.annotateUser(iae, text, msg, null, null);
            throw iae;
        }

        setValue(new SuperColor(text, palette, color));
    }

    /** Gets java inititalization string. Implements <code>PropertyEditor</code> interface. */
    public String getJavaInitializationString() {
        if (superColor == null)
            return "null"; // NOI18N
        if (superColor.getID() == null)
            return "new java.awt.Color(" + superColor.getRed() + ", " + superColor.getGreen() + // NOI18N
                   ", " + superColor.getBlue() + ")"; // NOI18N

        switch (superColor.getPalette()) {
        default:
        case AWT_PALETTE:
            return "java.awt.Color." + awtGenerate [getIndex (getAWTColorNames(), superColor.getID())]; // NOI18N
        case SYSTEM_PALETTE:
            return "java.awt.SystemColor." + systemGenerate [getIndex (getSystemColorNames(), superColor.getID())]; // NOI18N
        case SWING_PALETTE:
            if (superColor.getID() == null) return "new java.awt.Color(" + superColor.getRed() + ", " + superColor.getGreen() + // NOI18N
                                  ", " + superColor.getBlue() + ")"; // NOI18N
            return "javax.swing.UIManager.getDefaults().getColor(\"" + // NOI18N
                   superColor.getID() + "\")"; // NOI18N
        }
    }

    /** Get tags possible for choosing value. Implements <code>PropertyEditor</code> interface. */
    public String[] getTags() {
        if (superColor == null) {
            return getAWTColorNames();
        }
        switch (superColor.getPalette()) {
            case AWT_PALETTE:
                return getAWTColorNames();
            case SYSTEM_PALETTE:
                return getSystemColorNames();
            case SWING_PALETTE:
                initSwingConstants();
                return swingColorNames;
            default: 
                return null;
        }
    }

    /** Insicates whether this editor is paintable. Implements <code>PropertyEditor</code> interface.
     * @return <code>true</code> */
    public boolean isPaintable () {
        return true;
    }

    /** Paints the current value. Implements <code>ProepertyEditor</code> interface. */
    public void paintValue(Graphics g, Rectangle rectangle) {
        int px;

        ((Graphics2D)g).setRenderingHints (getHints ());
        
        if (this.superColor != null) {
            Color color = g.getColor();
            g.drawRect(rectangle.x, rectangle.y + rectangle.height / 2 - 5 , 10, 10);
            g.setColor(this.superColor);
            g.fillRect(rectangle.x + 1, rectangle.y + rectangle.height / 2 - 4 , 9, 9);
            g.setColor(color);
            px = 18;
        }
        else px = 0;

        FontMetrics fm = g.getFontMetrics();
        g.drawString(getAsText(), rectangle.x + px, rectangle.y +
                      (rectangle.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    /** Indicates whether this editor supports custom editing. 
     * Implements <code>PropertyEditor</code> interface.
     * @return <code>true</code> */
    public boolean supportsCustomEditor () {
        return true;
    }

    /** Gets custom editor. Implements <code>PropertyEditor</code> interface.
     * *return <code>NbColorChooser</code> instance */
    public Component getCustomEditor () {
        return new NbColorChooser (this, getStaticChooser(this));
    }

    /** Adds property change listener. */
    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    /** Removes property change listner. */
    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    // helper methods .......................................................................................
    /** Gets array of localized AWT color names. */
    private static synchronized String[] getAWTColorNames() {
        if(awtColorNames == null) {
            awtColorNames = new String[] {
                getString("LAB_White"),
                getString("LAB_LightGray"),
                getString("LAB_Gray"),
                getString("LAB_DarkGray"),
                getString("LAB_Black"),
                getString("LAB_Red"),
                getString("LAB_Pink"),
                getString("LAB_Orange"),
                getString("LAB_Yellow"),
                getString("LAB_Green"),
                getString("LAB_Magenta"),
                getString("LAB_Cyan"),
                getString("LAB_Blue")
            };
        }
        
        return awtColorNames;
    }

    /** Gets array of localize system color names. */
    private static synchronized String[] getSystemColorNames() {
        if(systemColorNames == null) {
            systemColorNames = new String[] {
                getString("LAB_ActiveCaption"),
                getString("LAB_ActiveCaptionBorder"),
                getString("LAB_ActiveCaptionText"),
                getString("LAB_Control"),
                getString("LAB_ControlDkShadow"),
                getString("LAB_ControlHighlight"),
                getString("LAB_ControlLtHighlight"),
                getString("LAB_ControlShadow"),
                getString("LAB_ControlText"),
                getString("LAB_Desktop"),
                getString("LAB_InactiveCaption"),
                getString("LAB_InactiveCaptionBorder"),
                getString("LAB_InactiveCaptionText"),
                getString("LAB_Info"),
                getString("LAB_InfoText"),
                getString("LAB_Menu"),
                getString("LAB_MenuText"),
                getString("LAB_Scrollbar"),
                getString("LAB_Text"),
                getString("LAB_TextHighlight"),
                getString("LAB_TextHighlightText"),
                getString("LAB_TextInactiveText"),
                getString("LAB_TextText"),
                getString("LAB_Window"),
                getString("LAB_WindowBorder"),
                getString("LAB_WindowText")
            };
        }
        
        return systemColorNames;
    }

    /** Gets localized string. 
     * @param key key from bundle from the package like this source */
    private static String getString(String key) {
        return NbBundle.getBundle(ColorEditor.class).getString(key);
    }

    /** Gets index of name from array. */
    private static int getIndex (Object[] names, Object name) {
        for(int i = 0; i < names.length; i++) {
            if(name.equals(names[i])) {
                return i;
            }
        }
        
        return -1;
    }

    /** Initialized fields used in Swing Palette. */
    private static void initSwingConstants() {
        if (swingColorNames != null)
            return;

        UIDefaults def = UIManager.getDefaults ();
        Enumeration e = def.keys ();
        
        java.util.TreeSet<String> names = new java.util.TreeSet<String>();
        
        while (e.hasMoreElements ()) {
            Object k = e.nextElement ();
            if (! (k instanceof String))
                continue;
            Object v = def.get (k);
            if (! (v instanceof Color))
                continue;
            names.add((String)k);
        }
        
        swingColorNames = new String [names.size ()];
        names.toArray(swingColorNames);
        swingColors = new Color [swingColorNames.length];
        
        int i, k = swingColorNames.length;
        for (i = 0; i < k; i++)
            swingColors [i] = (Color) def.get (swingColorNames [i]);
            }

    private SuperColor getSuperColor () {
        return superColor;
    }


    // innerclasses ............................................................................................
    /** Panel used as custom property editor. */
    private static class NbColorChooser extends JPanel implements ChangeListener {
        /** Color property editor */
        private final ColorEditor editor;
        /** Reference to model which holds the color selected in the color chooser */
        private final ColorSelectionModel selectionModel;

        static final long serialVersionUID =-6230228701104365037L;
        
        
        /** Creates new <code>NbColorChooser</code>. */
        public NbColorChooser (final ColorEditor editor,
                               final JColorChooser chooser) {
            this.editor = editor;
            selectionModel = chooser.getSelectionModel();
            setLayout (new BorderLayout ());
            add (chooser, BorderLayout.CENTER);
            chooser.setColor ((Color)editor.getValue ());
            selectionModel.addChangeListener (this);
            
            getAccessibleContext().setAccessibleDescription(getString("ACSD_CustomColorEditor"));
        }

        /** Overrides superclass method. Adds removing of change listener. */
        public void removeNotify () {
            super.removeNotify();
            selectionModel.removeChangeListener (this);
        }

        /** Overrides superclass method. Adds 50 pixels to each side. */
        public Dimension getPreferredSize () {
            Dimension s = super.getPreferredSize ();
            return new Dimension (s.width + 50, s.height + 10);
        }

        /** Implementats <code>ChangeListener</code> interface */
        public void stateChanged (ChangeEvent evt) {
            editor.setValue(selectionModel.getSelectedColor());
        }

    } // End of class NbColorChooser.


    /** Color belonging to palette and keeping its ID. */
    static class SuperColor extends Color {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 6147637669184334151L;

        /** ID of this color. */
        private String id = null;
        /** Palette where it belongs. */
        private int palette = 0;

        private Color color;

        SuperColor (Color color) {
            super (color.getRed (), color.getGreen (), color.getBlue ());
            this.color = color;
            
            //jkozak: When user sets color by RGB values, maybe we shouldn't
            //        change the color to AWT-Palette constant.
            /*
            int i = getIndex (ColorEditor.awtColors, color);
            if (i < 0) return;
            id = getAWTColorNames()[i];
             */
        }

        SuperColor (String id, int palette, Color color) {
            super (color.getRed (), color.getGreen (), color.getBlue ());
            this.color = color;
            this.id = id;
            this.palette = palette;
        }

        /** Overrides the equals(Object obj) method of java.awt.Color */
        public boolean equals(Object obj) {
            boolean superEquals = super.equals(obj);
            String objID = null;
            int objPalette = -1;
            
            if (obj instanceof SuperColor) {
                objID = ((SuperColor)obj).getID();
                objPalette = ((SuperColor)obj).getPalette();
            }
            else return superEquals;
            
            if (objID != null) {
                return superEquals && objID.equals(getID()) && (objPalette == getPalette());
            }
            else {
                return superEquals && (null == getID()) && (objPalette == getPalette());
            }
        }

        /** Gets ID of this color. */
        private String getID () {
            return id;
        }

        /** Gets palette of this color. */
        private int getPalette () {
            return palette;
        }

        /** Returns original color object */
        private Color getColor () {
            return this.color;
        }

        /** Gets as text this color value. */
        private String getAsText () {
            if (id != null) return id;
            return "[" + getRed () + "," + getGreen () + "," + getBlue () + "]"; // NOI18N
        }
    } // End of class SuperColor.

    /** Color chooser panel which can be added into JColorChooser */
    private static final class NbColorChooserPanel extends AbstractColorChooserPanel
    implements ListSelectionListener {
        /** Generated Serialized Version UID */
        static final long serialVersionUID = -2792992315444428631L;
        /** List holding palette colors */
        private JList list;

        /** Arraay of names of colors. */
        String [] names;
        /** Arraay of colors. */
        Color [] colors;
        /** Palette type. */
        int palette;
        /** Current ColorEditor. */
        ColorEditor ce;
        
        /** Name for display of this chooser panel. */
        private String displayName;
        

        /** Constructs our chooser panel with specified
        * palette, names and colors to be shown in the list */
        NbColorChooserPanel (final int palette, final String[] names,
                             final Color[] colors, final String displayName, final ColorEditor ce) {
            this.names = names;
            this.colors = colors;
            this.palette = palette;
            this.displayName = displayName;
            this.ce = ce;
        }

        /** Builds - creates a chooser */
        protected void buildChooser () {
            setLayout (new BorderLayout ());
            add (BorderLayout.CENTER,
                 new JScrollPane (list = new JList (names)));
            list.setCellRenderer (new MyListCellRenderer ());
            list.addListSelectionListener (this);
            
            list.getAccessibleContext().setAccessibleName(displayName);
        }

        /** Get called when state of selected color changes */
        public void updateChooser () {
            SuperColor sc = ce.getSuperColor ();
            
            String id;
            if (sc != null && palette == sc.getPalette () && (id = sc.getID()) != null && names != null) {
                int i = getIndex (names, id);
                list.setSelectedIndex (i);
            } else 
                list.clearSelection ();
        }

        /** @return display name of the chooser */
        public String getDisplayName() {
            return displayName;
        }

        /** No icon */
        public Icon getSmallDisplayIcon() {
            return null;
        }

        /** No icon */
        public Icon getLargeDisplayIcon() {
            return null;
        }

        /** ListSelectionListener interface implementation */
        public void valueChanged(ListSelectionEvent e) {
            if (!list.isSelectionEmpty ()) {
                int i = list.getSelectedIndex ();
                getColorSelectionModel().setSelectedColor(
                    new SuperColor (names [i], palette, colors [i]));
            }
        }

        /** Setter for <code>color</code> property. */
        public void setColor (final Color newColor) {
            getColorSelectionModel().setSelectedColor(newColor);
        }

        /** Getter for <code>color</code> property. */
        public Color getColor () {
            return getColorFromModel();
        }

        
        /** Renderer for cell of the list showing palette colors */
        private final class MyListCellRenderer extends JPanel implements ListCellRenderer {

            /** Selected flag. */
            private boolean selected;
            /** Focus flag. */
            private boolean hasFocus;
            /** Selected index. */
            private int index;

            /** Generated serial version UID. */
            static final long serialVersionUID =-8877709520578055594L;
            
            
            /** Creates a new MyListCellRenderer */
            public MyListCellRenderer () {
                this.setOpaque (true);
                this.setBorder (new EmptyBorder (1, 1, 1, 1));
            }

            /** Overrides default preferredSize impl.
             * @return Standard method returned preferredSize
             * (depends on font size only).
             */
            @Override
            public Dimension getPreferredSize () {
                try {
                    FontMetrics fontMetrics = this.getFontMetrics(this.getFont());
                    return new Dimension (
                               fontMetrics.stringWidth (names [index]) + 30,
                               fontMetrics.getHeight () + 4
                           );
                } catch (NullPointerException e) {
                    return new Dimension (10, 10);
                }
            }

            /** Paints this component. */
            @Override
            public void paint (Graphics g) {
                ((Graphics2D)g).setRenderingHints (getHints ());
                
                Dimension rectangle = this.getSize ();
                Color color = g.getColor ();

                if(selected) {
                    g.setColor (UIManager.getColor ("List.selectionBackground")); // NOI18N
                } else {
                    g.setColor (UIManager.getColor ("List.background")); // NOI18N
                }
                
                g.fillRect (0, 0, rectangle.width - 1, rectangle.height - 1);

                if (hasFocus) {
                    g.setColor (Color.black);
                    g.drawRect (0, 0, rectangle.width - 1, rectangle.height - 1);
                }

                g.setColor (Color.black);
                g.drawRect (6, rectangle.height / 2 - 5 , 10, 10);
                g.setColor (colors [index]);
                g.fillRect (7, rectangle.height / 2 - 4 , 9, 9);
                
                if(selected) {
                    g.setColor (UIManager.getColor ("List.selectionForeground")); // NOI18N
                } else {
                    g.setColor (UIManager.getColor ("List.foreground")); // NOI18N
                }
                
                FontMetrics fm = g.getFontMetrics ();
                g.drawString (names [index], 22, (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
                g.setColor (color);
            }

            /** This is the only method defined by ListCellRenderer.  We just
             * reconfigure the Jlabel each time we're called.
             */
            public Component getListCellRendererComponent (
                JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus     // the list and the cell have the focus
            ) {
                this.index = index;
                selected = isSelected;
                hasFocus = cellHasFocus;
                getAccessibleContext().setAccessibleName(names[index]);
                return this;
            }
        } // End of class MyListCellRenderer.
    } // End of class NbColorChooserPanel.

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    /** Name of color element. */
    public static final String XML_COLOR = "Color"; // NOI18N

    /** Name of type attribute. */
    public static final String ATTR_TYPE = "type"; // NOI18N
    /** Name of red attribute. */
    public static final String ATTR_RED = "red"; // NOI18N
    /** Name of green attribute. */
    public static final String ATTR_GREEN = "green"; // NOI18N
    /** Name of blue attribute. */
    public static final String ATTR_BLUE = "blue"; // NOI18N
    /** Name of id attribute. */
    public static final String ATTR_ID = "id"; // NOI18N
    /** Name of palette attribute. */
    public static final String ATTR_PALETTE = "palette"; // NOI18N

    /** Value of palette. */
    public static final String VALUE_PALETTE = "palette"; // NOI18N
    /** Value of rgb. */
    public static final String VALUE_RGB = "rgb"; // NOI18N
    /** Null value. */
    public static final String VALUE_NULL = "null"; // NOI18N

    
    /** Called to load property value from specified XML subtree. If succesfully loaded,
     * Implements <code>XMLPropertyEditor</code> interface.
     * the value should be available via the getValue method.
     * An IOException should be thrown when the value cannot be restored from the specified XML element
     * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from the specified XML element
     */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_COLOR.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String type = attributes.getNamedItem (ATTR_TYPE).getNodeValue ();
            if (VALUE_NULL.equals(type)) {
                setValue(null);
            } else {
                String red = attributes.getNamedItem (ATTR_RED).getNodeValue ();
                String green = attributes.getNamedItem (ATTR_GREEN).getNodeValue ();
                String blue = attributes.getNamedItem (ATTR_BLUE).getNodeValue ();
                if (VALUE_PALETTE.equals (type)) {
                    int palette = Integer.parseInt (attributes.getNamedItem (ATTR_PALETTE).getNodeValue ());
                    String id = attributes.getNamedItem (ATTR_ID).getNodeValue ();
                    if (palette == AWT_PALETTE) { // old colors stored localized name of color, newer has original 'en' name
                        int idx = getIndex(awtGenerate, id);
                        id = idx >= 0? getAWTColorNames()[idx]: (getIndex(getAWTColorNames(), id)>=0? id: null);
                    }
                    setValue (new SuperColor (id,  palette, new Color (Integer.parseInt (red, 16), Integer.parseInt (green, 16), Integer.parseInt (blue, 16))));
                } else {
                    setValue (new SuperColor (new Color (Integer.parseInt (red, 16), Integer.parseInt (green, 16), Integer.parseInt (blue, 16))));
                }
            }
        } catch (NullPointerException e) {
            throw new java.io.IOException ();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
     * Implemtns <code>XMLPropertyEdtitor</code> interface.
     * setValue method prior to calling this method.
     * @param doc The XML document to store the XML in - should be used for creating nodes only
     * @return the XML DOM element representing a subtree of XML from which the value should be loaded
     */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement (XML_COLOR);
        el.setAttribute (ATTR_TYPE, (superColor == null) ? VALUE_NULL : ((superColor.getID () == null) ? VALUE_RGB : VALUE_PALETTE));
        if (superColor != null) {
            el.setAttribute (ATTR_RED, Integer.toHexString (superColor.getRed ()));
            el.setAttribute (ATTR_GREEN, Integer.toHexString (superColor.getGreen ()));
            el.setAttribute (ATTR_BLUE, Integer.toHexString (superColor.getBlue ()));
            if (superColor.getID () != null) {
                if (superColor.getPalette () == AWT_PALETTE) {
                    el.setAttribute (ATTR_ID, awtGenerate[getIndex(getAWTColorNames(), superColor.getID ())]);
                }
                else {
                    el.setAttribute (ATTR_ID, superColor.getID ());
                }
                el.setAttribute (ATTR_PALETTE, Integer.toString (superColor.getPalette ()));
            }
        }
        return el;
    }

    public static final boolean gtkShouldAntialias() {
        if (gtkAA == null) {
            Object o = Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/Antialias"); //NOI18N
            gtkAA = Boolean.valueOf(Integer.valueOf(1).equals(o));
        }

        return gtkAA.booleanValue();
    }

    // copied from openide/awt/HtmlLabelUI
    @SuppressWarnings("unchecked") // need to use reflective access, no idea of type
    private static Map getHints () {
        if (hintsMap == null) {
            hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap();
                if (antialias) {
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
            }
        }
        return hintsMap;
    }
}
