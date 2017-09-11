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

package org.netbeans.modules.apisupport.project.ui.branding;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.apisupport.project.spi.BrandingSupport.BundleKey;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
class SplashUISupport {
    private SplashUISupport() {}
    
    static Rectangle stringToBounds(final String  bounds) throws NumberFormatException {
        StringTokenizer st = new StringTokenizer(bounds, " ,"); // NOI18N
        int x, y, width, height;
        x = y = width = height =0;
        for (int i = 0; i < 4; i++) {
            if (!st.hasMoreElements()) {
                throw new NumberFormatException();
            }
            switch (i) {
                case 0:
                    x = Integer.parseInt(st.nextToken());
                    break;
                case 1:
                    y = Integer.parseInt(st.nextToken());
                    break;
                case 2:
                    width = Integer.parseInt(st.nextToken());
                    break;
                case 3:
                    height = Integer.parseInt(st.nextToken());
                    break;
                    
            }
        }
        return new Rectangle(x,y,width,height);
    }

    static Rectangle bundleKeyToBounds(@NullAllowed BundleKey bk) throws NumberFormatException {
        return bk != null ? stringToBounds(bk.getValue()) : new Rectangle();
    }
    
    static String boundsToString(final Rectangle  bounds) {
        if (bounds == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(bounds.x)).append(",");//NOI18N
        sb.append(String.valueOf(bounds.y)).append(",");//NOI18N
        sb.append(String.valueOf(bounds.width)).append(",");//NOI18N
        sb.append(String.valueOf(bounds.height));//NOI18N
        return sb.toString();
    }
    
    static Color stringToColor(final String color) throws NumberFormatException {
        return new Color(Integer.decode(color).intValue());
    }

    static Color bundleKeyToColor(@NullAllowed BundleKey bk) throws NumberFormatException {
        return bk != null ? stringToColor(bk.getValue()) : Color.BLACK;
    }
    
    static String colorToString(final Color  color) {
        return "0x" + Integer.toString((~0xff000000 & color.getRGB()), 16).toUpperCase(Locale.ENGLISH); // NOI18N
    }
    
    static int stringToInteger(final String integer) throws NumberFormatException {
        return Integer.decode(integer).intValue();
    }

    static int bundleKeyToInteger(@NullAllowed BundleKey bk) throws NumberFormatException {
        return bk != null ? stringToInteger(bk.getValue()) : 0;
    }
    
    static String integerToString(final int  integer) {
        return Integer.toString(integer, 10);
    }

    static String numberToString(@NullAllowed Number n) {
        return n != null ? integerToString(n.intValue()) : "";
    }

    static boolean bundleKeyToBoolean(@NullAllowed BundleKey bk) {
        return bk != null ? Boolean.parseBoolean(bk.getValue()) : false;
    }

    static void setValue(@NullAllowed BundleKey bk, @NonNull String value) {
        if (bk != null) {
            bk.setValue(value);
        }
    }
    
    static JFormattedTextField getIntegerField() {
        JFormattedTextField retval = new JFormattedTextField(NumberFormat.getIntegerInstance());
        retval = new JFormattedTextField(new FontFormatter(retval.getFormatter()));
        return retval;
    }
        
    static JFormattedTextField getBoundsField() {
        JFormattedTextField retval = new JFormattedTextField(new BoundsFormatter());
        return retval;
    }
    
    static SplashUISupport.ColorComboBox getColorComboBox() {
        SplashUISupport.ColorComboBox retval = new SplashUISupport.ColorComboBox();
        return retval;
    }
    
    private static class FontFormatter extends DefaultFormatter {
        private AbstractFormatter deleg;
        FontFormatter(AbstractFormatter deleg) {
            setOverwriteMode(false);
            this.deleg = deleg;
        }
        public Object stringToValue(String string) throws java.text.ParseException {
            Object retval = deleg.stringToValue(string);
            int i = ((Number)retval).intValue();
            if (i < 0) {
                throw new java.text.ParseException(string,0);
            }
            return retval;
        }
        
        public String valueToString(Object value) throws java.text.ParseException {
            return deleg.valueToString(value);
        }
    }
    
    private static class BoundsFormatter extends DefaultFormatter {
        BoundsFormatter() {
            setOverwriteMode(false);
        }
        public Object stringToValue(String string) throws java.text.ParseException {
            if (string == null) {
                return super.stringToValue(string);
            } else {
                try {
                    return SplashUISupport.stringToBounds(string);
                } catch (NumberFormatException ex) {
                    throw new java.text.ParseException(string,0);
                }
            }
        }
        
        public String valueToString(Object value) throws java.text.ParseException {
            if (value == null) {
                return super.valueToString(value);
            } else {
                try {
                    return SplashUISupport.boundsToString((Rectangle)value);
                } catch (NumberFormatException ex) {
                    throw new java.text.ParseException(value.toString(),0);
                }
            }
        }
    }
    
    /**
     * copy & pasted from editor/options org.netbeans.modules.options.colors.ColorComboBox
     */
    static class ColorComboBox extends JComboBox {
        
        public static final String PROP_COLOR = "color"; // NOI18N
        public static final Value  CUSTOM_COLOR =
                new Value(loc("Custom"), null); //NOI18N
        
        private static Map<Color,String> colorMap = new HashMap<Color,String>();
        static {
            colorMap.put(Color.BLACK,      loc("Black"));         //NOI18N
            colorMap.put(Color.BLUE,       loc("Blue"));          //NOI18N
            colorMap.put(Color.CYAN,       loc("Cyan"));          //NOI18N
            colorMap.put(Color.DARK_GRAY,  loc("Dark_Gray"));     //NOI18N
            colorMap.put(Color.GRAY,       loc("Gray"));          //NOI18N
            colorMap.put(Color.GREEN,      loc("Green"));         //NOI18N
            colorMap.put(Color.LIGHT_GRAY, loc("Light_Gray"));    //NOI18N
            colorMap.put(Color.MAGENTA,    loc("Magenta"));       //NOI18N
            colorMap.put(Color.ORANGE,     loc("Orange"));        //NOI18N
            colorMap.put(Color.PINK,       loc("Pink"));          //NOI18N
            colorMap.put(Color.RED,        loc("Red"));           //NOI18N
            colorMap.put(Color.WHITE,      loc("White"));         //NOI18N
            colorMap.put(Color.YELLOW,     loc("Yellow"));        //NOI18N
        }
        
        private static Object[] content = new Object[] {
            new Value(Color.BLACK),
                    new Value(Color.BLUE),
                    new Value(Color.CYAN),
                    new Value(Color.DARK_GRAY),
                    new Value(Color.GRAY),
                    new Value(Color.GREEN),
                    new Value(Color.LIGHT_GRAY),
                    new Value(Color.MAGENTA),
                    new Value(Color.ORANGE),
                    new Value(Color.PINK),
                    new Value(Color.RED),
                    new Value(Color.WHITE),
                    new Value(Color.YELLOW),
                    CUSTOM_COLOR,
                    new Value(NbBundle.getMessage(SplashUISupport.class, "SplashUISupport_color_none"), null),
        };
        
        
        /** Creates a new instance of ColorChooser */
        public ColorComboBox() {
            super(content);
            setRenderer(new Renderer());
            setEditable(true);
            setEditor(new Renderer());
            setSelectedItem(new Value(null, null));
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (getSelectedItem() == CUSTOM_COLOR) {
                        Color c = JColorChooser.showDialog(
                                SwingUtilities.getAncestorOfClass
                                (Dialog.class, ColorComboBox.this),
                                loc("SelectColor"), // NOI18N
                                null
                                );
                        setColor(c);
                    }
                    ColorComboBox.this.firePropertyChange(PROP_COLOR, null, null);
                }
            });
        }
        
        public void setDefaultColor(Color color) {
            Object[] ncontent = new Object [content.length];
            System.arraycopy(content, 0, ncontent, 0, content.length);
            ncontent [content.length - 1] = new Value(
                    NbBundle.getMessage(SplashUISupport.class, "SplashUISupport_color_default"), color // NOI18N
                    );
            setModel(new DefaultComboBoxModel(ncontent));
        }
        
        public void setColor(Color color) {
            if (color == null)
                setSelectedIndex(content.length - 1);
            else
                setSelectedItem(new Value(color));
        }
        
        public Color getColor() {
            if (getSelectedIndex() == (content.length - 1)) return null;
            return ((Value) getSelectedItem()).color;
        }
        
        private static String loc(String key) {
            return NbBundle.getMessage(ColorComboBox.class, key);
        }
        
        
        // innerclasses ............................................................
        
        public static class Value {
            String text;
            Color color;
            
            Value(Color color) {
                this.color = color;
                text = colorMap.get(color);
                if (text != null) return;
                StringBuffer sb = new StringBuffer();
                sb.append('[').append(color.getRed()).
                        append(',').append(color.getGreen()).
                        append(',').append(color.getBlue()).
                        append(']');
                text = sb.toString();
            }
            
            Value(String text, Color color) {
                this.text = text;
                this.color = color;
            }
        }
        
        private static class Editor extends JLabel implements ComboBoxEditor {
            
            private Object value;
            
            Editor() {
                //setOpaque (false);
            }
            
            public Component getEditorComponent() {
                return this;
            }
            
            public void setItem(Object anObject) {
                value = anObject;
                if (value instanceof String) {
                    setText(NbBundle.getMessage(SplashUISupport.class, "SplashUISupport_color_default"));
                    super.setForeground(SystemColor.textText);
                    super.setBackground(SystemColor.text);
                } else {
                    setText("");
                    super.setBackground((Color) value);
                }
            }
            
            public Object getItem() {
                return value;
            }
            
            public void setBackground(Color c) {}
            public void setForeground(Color c) {}
            
            public void selectAll() {}
            public void addActionListener(ActionListener l) {}
            public void removeActionListener(ActionListener l) {}
        }
        
        private class Renderer extends JComponent implements
                ListCellRenderer, ComboBoxEditor {
            
            private int             SIZE = 9;
            private Value           value;
            
            Renderer() {
                setPreferredSize(new Dimension(
                        50, getFontMetrics(ColorComboBox.this.getFont()).getHeight() + 2
                        ));
                setOpaque(true);
            }
            
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                if (!isEnabled()) {
                    g2d.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, 0.3f));
                }                
                Color oldColor = g.getColor();
                Dimension size = getSize();
                g.setColor(getBackground());
                g.fillRect(0, 0, size.width, size.height);
                int i = (size.height - SIZE) / 2;
                if (value.color != null) {
                    g.setColor(Color.black);
                    g.drawRect(i, i, SIZE, SIZE);
                    g.setColor(value.color);
                    g.fillRect(i + 1, i + 1, SIZE - 1, SIZE - 1);
                }
                if (value.text != null) {
                    g.setColor(Color.black);
                    if (value.color != null)
                        g.drawString(value.text, i + SIZE + 5, i + SIZE);
                    else
                        g.drawString(value.text, 5, i + SIZE);
                }
                g.setColor(oldColor);
            }
            
            public void setEnabled(boolean enabled) {
                setBackground(enabled ?
                    SystemColor.text : SystemColor.control
                        );
                super.setEnabled(enabled);
            }
            
            public Component getListCellRendererComponent(
                    JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
                    ) {
                this.value = (Value) value;
                setEnabled(list.isEnabled());
                return this;
            }
            
            public Component getEditorComponent() {
                setEnabled(ColorComboBox.this.isEnabled());
                return this;
            }
            
            public void setItem(Object anObject) {
                this.value = (Value) anObject;
            }
            
            public Object getItem() {
                return value;
            }
            public void selectAll() {}
            public void addActionListener(ActionListener l) {}
            public void removeActionListener(ActionListener l) {}   }
    }
    
}
