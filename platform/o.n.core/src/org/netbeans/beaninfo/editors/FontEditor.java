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

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.netbeans.core.UIExceptions;
import org.openide.DialogDisplayer;

import org.openide.NotifyDescriptor;
import org.openide.awt.GraphicsUtils;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
* A property editor for Font class.
*
* @author Ian Formanek
*/
public class FontEditor implements PropertyEditor, XMLPropertyEditor {

    // static .....................................................................................

    static final Integer[] sizes = new Integer [] {
                                       Integer.valueOf (3),
                                       Integer.valueOf (5),
                                       Integer.valueOf (8),
                                       Integer.valueOf (10),
                                       Integer.valueOf (12),
                                       Integer.valueOf (14),
                                       Integer.valueOf (18),
                                       Integer.valueOf (24),
                                       Integer.valueOf (36),
                                       Integer.valueOf (48)
                                   };

    static final String[] styles = new String [] {
                                       NbBundle.getMessage(FontEditor.class, "CTL_Plain"),
                                       NbBundle.getMessage(FontEditor.class, "CTL_Bold"),
                                       NbBundle.getMessage(FontEditor.class, "CTL_Italic"),
                                       NbBundle.getMessage(FontEditor.class, "CTL_BoldItalic")
                                   };

    // variables ..................................................................................

    private Font font;
    private String fontName;
    private PropertyChangeSupport support;


    // init .......................................................................................

    public FontEditor() {
        support = new PropertyChangeSupport (this);
    }


    // main methods .......................................................................................

    public Object getValue () {
        return font;
    }

    public void setValue (Object object) {
        if (font != null && font.equals (object)) {
            return ;
        } else if (font == null && object == null) {
            return ;
        }
        
        if (object instanceof Font) {
            font = (Font) object;
        } else if (object == null) {
            font = null;
        } else {
            assert false : "Object " + object + " is instanceof Font or null";
        }
        
        if (font != null) {
            fontName = font.getName () + " " + font.getSize () + " " + getStyleName (font.getStyle ()); // NOI18N
        } else {
            fontName = null;
        }
        
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        return fontName;
    }

    public void setAsText (String string) {
        return;
    }

    public String getJavaInitializationString () {
        return "new java.awt.Font(\"" + font.getName () + "\", " + font.getStyle () + // NOI18N
               ", " + font.getSize () + ")"; // NOI18N
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return true;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
        paintText(g, rectangle, fontName == null ? "null" : fontName); //NOI18N
    }

    private void paintText (Graphics g, Rectangle rectangle, String text) {
        GraphicsUtils.configureDefaultRenderingHints(g);
        Font originalFont = g.getFont ();
        
        // Fix of 21713, set default value
        if ( font == null ) setValue( null );
        
        Font paintFont = font == null ? originalFont : font; // NOI18N
        assert paintFont != null : "paintFont must exist.";
        
        FontMetrics fm = g.getFontMetrics (paintFont);
        if (fm.getHeight() > rectangle.height) {
            if (Utilities.isMac()) {
                // don't use deriveFont() - see #49973 for details
                paintFont = new Font(paintFont.getName(), paintFont.getStyle(), 12);
            } else {
                paintFont = paintFont.deriveFont(12f);
            }
            fm = g.getFontMetrics (paintFont);
        }
        g.setFont (paintFont);
        g.drawString (text,
                      rectangle.x,
                      rectangle.y + (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
        g.setFont (originalFont);
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {
        return new FontPanel ();
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }


    // helper methods .......................................................................................

    String getStyleName (int i) {
        if ((i & Font.BOLD) > 0)
            if ((i & Font.ITALIC) > 0) return NbBundle.getMessage(FontEditor.class, "CTL_BoldItalic");
            else return NbBundle.getMessage(FontEditor.class, "CTL_Bold");
        else
            if ((i & Font.ITALIC) > 0) return NbBundle.getMessage(FontEditor.class, "CTL_Italic");
            else return NbBundle.getMessage(FontEditor.class, "CTL_Plain");
    }
    
    private static String[] fonts;
    private static String [] getFonts () {
        if (fonts == null) {
            try {
                fonts = GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames();
            } catch (RuntimeException e) {
                fonts = new String[0]; //NOI18N
                if (org.openide.util.Utilities.isMac()) {
                    String msg = NbBundle.getMessage(FontEditor.class, "MSG_AppleBug"); //NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                } else {
                    throw e;
                }
            }
        }
        return fonts;
    }


    // innerclasses ............................................................................................

    class FontPanel extends JPanel {

        JTextField tfFont, tfStyle, tfSize;
        JList lFont, lStyle, lSize;
        boolean dontSetValue = false;

        static final long serialVersionUID =8377025140456676594L;

        FontPanel () {
            dontSetValue = false;
            setLayout (new BorderLayout ());
            setBorder(new EmptyBorder(12, 12, 0, 11));
            
            Font font = (Font) getValue ();
            if (font == null) {
                if (getFonts ().length > 0) {
                    font = new Font (fonts[0], Font.PLAIN, 10);
                } else {
                    font = UIManager.getFont ("Label.font"); // NOI18N
                }
            }

            lFont = new JList (getFonts ());
            lFont.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
            lFont.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_CTL_Font"));
            lStyle = new JList (styles);
            lStyle.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
            lStyle.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_CTL_FontStyle"));
            lSize = new JList (sizes);
            lSize.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
            lSize.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_CTL_Size"));
            tfSize = new JTextField (String.valueOf(font.getSize ()));
            tfSize.getAccessibleContext().setAccessibleDescription(lSize.getAccessibleContext().getAccessibleDescription());
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FontEditor.class, "ACSD_FontCustomEditor"));

            GridBagLayout la = new GridBagLayout ();
            GridBagConstraints c = new GridBagConstraints ();
            setLayout (la);

            c.gridwidth = 1;
            c.weightx = 1.0;
            c.insets = new Insets (0, 0, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            JLabel l = new JLabel ();
            Mnemonics.setLocalizedText(l, NbBundle.getMessage(FontEditor.class, "CTL_Font"));                    //NoI18N
            l.setLabelFor(lFont);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (0, 5, 0, 0);
            l = new JLabel ();                           //NoI18N
            Mnemonics.setLocalizedText(l, NbBundle.getMessage(FontEditor.class, "CTL_FontStyle")); //NoI18N
            l.setLabelFor(lStyle);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (0, 5, 0, 0);
            c.gridwidth = GridBagConstraints.REMAINDER;
            l = new JLabel ();
            Mnemonics.setLocalizedText(l, NbBundle.getMessage(FontEditor.class, "CTL_Size"));                           //NoI18N
            l.setLabelFor(tfSize);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (5, 0, 0, 0);
            c.gridwidth = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            tfFont = new JTextField (font.getName ());
            tfFont.setEnabled (false);
            la.setConstraints (tfFont, c);
            add (tfFont);

            c.insets = new Insets (5, 5, 0, 0);
            tfStyle = new JTextField (getStyleName (font.getStyle ()));
            tfStyle.setEnabled (false);
            la.setConstraints (tfStyle, c);
            add (tfStyle);

            c.insets = new Insets (5, 5, 0, 0);
            c.gridwidth = GridBagConstraints.REMAINDER;
            
            tfSize.addKeyListener( new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
                        setValue ();
                    }
                }
            });
            
            tfSize.addFocusListener (new FocusAdapter () {
                @Override
                 public void focusLost (FocusEvent evt) {
                    if (dontSetValue) {
                        return ;
                    } else {
                        dontSetValue = true;
                    }
                    Component c = evt.getOppositeComponent ();
                    if (c != null) {
                        if (c instanceof JButton) {
                            if (((JButton) c).getText ().equals (NbBundle.getMessage (FontEditor.class, "CTL_OK"))) { // NOI18N
                                setValue ();
                            }
                        } else {
                            setValue ();
                        }
                    }
                 }

                @Override
                public void focusGained (FocusEvent evt) {
                    dontSetValue = false;
                }
            });
            la.setConstraints (tfSize, c);
            add (tfSize);

            c.gridwidth = 1;
            c.insets = new Insets (5, 0, 0, 0);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            lFont.setVisibleRowCount (5);
            lFont.setSelectedValue(font.getName (), true);
            lFont.addListSelectionListener (new ListSelectionListener () {
                                                public void valueChanged (ListSelectionEvent e) {
                                                    if (!lFont.isSelectionEmpty ()) {
                                                        if (getFonts ().length > 0) { //Mac bug workaround
                                                            int i = lFont.getSelectedIndex ();
                                                            tfFont.setText (getFonts () [i]);
                                                            setValue ();
                                                        }
                                                    }
                                                }
                                            }
                                           );
            JScrollPane sp = new JScrollPane (lFont);
            sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            la.setConstraints (sp, c);
            positionScrollPaneOnSelected(sp, lFont);
            add (sp);

            lStyle.setVisibleRowCount (5);
            lStyle.setSelectedValue(getStyleName (font.getStyle ()), true);
            lStyle.addListSelectionListener (new ListSelectionListener () {
                                                 public void valueChanged (ListSelectionEvent e) {
                                                     if (!lStyle.isSelectionEmpty ()) {
                                                         int i = lStyle.getSelectedIndex ();
                                                         tfStyle.setText (styles [i]);
                                                         setValue ();
                                                     }
                                                 }
                                             }
                                            );
            sp = new JScrollPane (lStyle);
            sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            c.insets = new Insets (5, 5, 0, 0);
            la.setConstraints (sp, c);
            positionScrollPaneOnSelected(sp, lStyle);
            add (sp);

            c.gridwidth = GridBagConstraints.REMAINDER;
            lSize.getAccessibleContext().setAccessibleName(tfSize.getAccessibleContext().getAccessibleName());
            lSize.setVisibleRowCount (5);
            updateSizeList(font.getSize ());
            lSize.addListSelectionListener (new ListSelectionListener () {
                                                public void valueChanged (ListSelectionEvent e) {
                                                    if (!lSize.isSelectionEmpty ()) {
                                                        int i = lSize.getSelectedIndex ();
                                                        tfSize.setText (String.valueOf(sizes [i]));
                                                        setValue ();
                                                    }
                                                }
                                            }
                                           );
            sp = new JScrollPane (lSize);
            sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            c.insets = new Insets (5, 5, 0, 0);
            la.setConstraints (sp, c);
            positionScrollPaneOnSelected(sp, lSize);
            add (sp);

            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weighty = 0.0;
            JPanel p = new JPanel (new BorderLayout());
            p.setBorder (new TitledBorder (" " + NbBundle.getMessage(FontEditor.class, "CTL_Preview") + " "));

            JPanel pp = new JPanel () {
                @Override
                public Dimension getPreferredSize () {
                    return new Dimension (150, 60);
                }

                @Override
                public void paint (Graphics g) {
                    FontEditor.this.paintText(
                        g,
                        new Rectangle (0, 0, this.getSize().width - 1, this.getSize().height - 1),
                        NbBundle.getMessage(FontEditor.class, "MSG_Preview") // NOI18N
                    );
                }
            };
            p.add ("Center", pp); // NOI18N
            c.insets = new Insets (12, 0, 0, 0);
            la.setConstraints (p, c);
            add (p);
        }

        @Override
        public Dimension getPreferredSize () {
            return new Dimension (400, 300);
        }

        private void updateSizeList(int size) {
            if (java.util.Arrays.asList(sizes).contains(Integer.valueOf(size)))
                lSize.setSelectedValue(Integer.valueOf(size), true);
            else
                lSize.clearSelection();
        }

        private void setValue () {
            int size = 12;
            try {
                size = Integer.parseInt (tfSize.getText ());
                if (size <= 0) {
                    IllegalArgumentException iae = new IllegalArgumentException ();
                    UIExceptions.annotateUser (iae, null,
                            size == 0 ? NbBundle.getMessage (FontEditor.class, "CTL_InvalidValueWithParam", tfSize.getText ()) : // NOI18N
                                NbBundle.getMessage (FontEditor.class, "CTL_NegativeSize"), // NOI18N
                            null, null);
                    tfSize.setText (String.valueOf (font.getSize ()));
                    throw iae;
                }
                updateSizeList(size);
            } catch (NumberFormatException e) {
                UIExceptions.annotateUser (e, null,
                        NbBundle.getMessage (FontEditor.class, "CTL_InvalidValueWithExc", e), // NOI18N
                        null, null);
                tfSize.setText (String.valueOf (font.getSize ()));
                throw e;
            }
            int i = lStyle.getSelectedIndex (), ii = Font.PLAIN;
            switch (i) {
            case 0: ii = Font.PLAIN;break;
            case 1: ii = Font.BOLD;break;
            case 2: ii = Font.ITALIC;break;
            case 3: ii = Font.BOLD | Font.ITALIC;break;
            }
            FontEditor.this.setValue (new Font (tfFont.getText (), ii, size));
            invalidate();
            java.awt.Component p = getParent();
            if (p != null) {
                p.validate();
            } 
            repaint();
        }

        private void positionScrollPaneOnSelected(JScrollPane scroll, JList list) {
            if (list.getSelectedIndex() != -1) {
                int start = list.getSelectedIndex() - list.getVisibleRowCount() / 2;
                Rectangle selected = list.getCellBounds(start < 0 ? 0 : start, list.getSelectedIndex());
                scroll.getViewport().setViewPosition(new Point(selected.x, selected.y));
            }
        }
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_FONT = "Font"; // NOI18N

    public static final String ATTR_NAME = "name"; // NOI18N
    public static final String ATTR_STYLE = "style"; // NOI18N
    public static final String ATTR_SIZE = "size"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @throws java.io.IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_FONT.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String name = attributes.getNamedItem (ATTR_NAME).getNodeValue ();
            String style = attributes.getNamedItem (ATTR_STYLE).getNodeValue (); // [PENDING - style names]
            String size = attributes.getNamedItem (ATTR_SIZE).getNodeValue ();
            setValue (new Font (name, Integer.parseInt (style), Integer.parseInt (size)));
        } catch (NullPointerException e) {
            throw new java.io.IOException ();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        if (font == null) {
            IllegalArgumentException iae = new IllegalArgumentException();
            Exceptions.attachLocalizedMessage(iae,
                                              NbBundle.getMessage(FontEditor.class,
                                                                  "MSG_FontIsNotInitialized")); // NOI18N
            Exceptions.printStackTrace(iae);
            return null;
        }
        
        org.w3c.dom.Element el = doc.createElement (XML_FONT);
        el.setAttribute (ATTR_NAME, font.getName ());
        el.setAttribute (ATTR_STYLE, Integer.toString (font.getStyle ()));
        el.setAttribute (ATTR_SIZE, Integer.toString (font.getSize ()));
        return el;
    }
}
