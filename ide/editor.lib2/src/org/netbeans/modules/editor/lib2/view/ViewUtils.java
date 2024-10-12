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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.highlighting.CompoundAttributes;

/**
 * Various view utilities.
 * 
 * @author Miloslav Metelka
 */

public final class ViewUtils {

    public static final String KEY_VIRTUAL_TEXT_PREPEND = "virtual-text-prepend"; //NOI18N

    private ViewUtils() { // No instances
    }

    public static Rectangle2D.Double shape2Bounds(Shape s) {
        Rectangle2D r;
        if (s instanceof Rectangle2D) {
            r = (Rectangle2D) s;
        } else {
            r = s.getBounds2D();
        }
        return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    
    public static Rectangle2D shapeAsRect(Shape s) {
        Rectangle2D r;
        if (s instanceof Rectangle2D) {
            r = (Rectangle2D) s;
        } else {
            r = s.getBounds2D();
        }
        return r;
    }

    public static Rectangle toRect(Rectangle2D r2d) {
        Rectangle r = new Rectangle();
        r.setRect(r2d);
        return r;
    }

    public static void fillRect(Graphics2D g, Rectangle2D r) {
        g.fillRect(
                (int) r.getX(),
                (int) r.getY(),
                (int) Math.ceil(r.getWidth()), // Fill will end at (including) width-1
                (int) Math.ceil(r.getHeight()) // Fill will end at (including) height-1
        );
    }

    public static boolean applyBackgroundColor(Graphics2D g, AttributeSet attrs, JTextComponent c) {
        boolean overrides = false;
        Color background;
        if (attrs != null) {
            background = (Color) attrs.getAttribute(StyleConstants.Background);
            if (background != null) {
                if (!background.equals(c.getBackground())) {
                    overrides = true;
                }
            } else {
                background = c.getBackground();
            }
        } else {
            background = c.getBackground();
        }
        if (background != null) {
            g.setColor(background);
        }
        return overrides;
    }

    public static void applyForegroundColor(Graphics2D g, AttributeSet attrs, JTextComponent c) {
        Color foreground;
        if (attrs != null) {
            foreground = (Color) attrs.getAttribute(StyleConstants.Foreground);
            if (foreground == null) {
                foreground = c.getForeground();
            }
        } else {
            foreground = c.getForeground();
        }
        if (foreground != null) {
            g.setColor(foreground);
        }
    }

    public static void applyFont(Graphics2D g, AttributeSet attrs, JTextComponent c) {
        Font font = c.getFont();
        if (attrs != null) {
            font = getFont(attrs, font);
        }
        if (font != null) {
            g.setFont(font);
        }
    }

    public static Font getFont(AttributeSet attrs, Font defaultFont) {
        Font font = defaultFont;
        if (attrs != null) {
            String fontName = (String) attrs.getAttribute(StyleConstants.FontFamily);
            Boolean bold = (Boolean) attrs.getAttribute(StyleConstants.Bold);
            Boolean italic = (Boolean) attrs.getAttribute(StyleConstants.Italic);
            Integer fontSizeInteger = (Integer) attrs.getAttribute(StyleConstants.FontSize);
            if (fontName != null || bold != null || italic != null || fontSizeInteger != null) {
                if (fontName == null) {
                    fontName = defaultFont.getFontName();
                }
                int fontStyle = (defaultFont != null) ? defaultFont.getStyle() : Font.PLAIN;
                if (bold != null) {
                    fontStyle &= ~Font.BOLD;
                    if (bold) {
                        fontStyle |= Font.BOLD;
                    }
                }
                if (italic != null) {
                    fontStyle &= ~Font.ITALIC;
                    if (italic) {
                        fontStyle |= Font.ITALIC;
                    }
                }
                int fontSize = (fontSizeInteger != null) ? fontSizeInteger : defaultFont.getSize();
                font = StyleContext.getDefaultStyleContext().getFont(fontName, fontStyle, fontSize);
            }
        }
        return font;
    }

    /**
     * Get font or null if attributes do not provide enough info for font composition.
     *
     * @param attrs non-null attrs.
     * @return font or null.
     */
    public static Font getFont(AttributeSet attrs) {
        Font font = null;
        if (attrs != null) {
            String fontName = (String) attrs.getAttribute(StyleConstants.FontFamily);
            Integer fontSizeInteger = (Integer) attrs.getAttribute(StyleConstants.FontSize);
            if (fontName != null && fontSizeInteger != null) {
                Boolean bold = (Boolean) attrs.getAttribute(StyleConstants.Bold);
                Boolean italic = (Boolean) attrs.getAttribute(StyleConstants.Italic);
                int fontStyle = Font.PLAIN;
                if (bold != null && bold) {
                    fontStyle |= Font.BOLD;
                }
                if (italic != null && italic) {
                    fontStyle |= Font.ITALIC;
                }
                font = StyleContext.getDefaultStyleContext().getFont(fontName, fontStyle, fontSizeInteger);
            }
        }
        return font;
    }

    public static String toStringAxis(int axis) {
        return (axis == View.X_AXIS) ? "X" : "Y";
    }
    
    public static String toStringDirection(int direction) {
        switch (direction) {
            case SwingConstants.WEST: return "WEST";
            case SwingConstants.EAST: return "EAST";
            case SwingConstants.NORTH: return "NORTH";
            case SwingConstants.SOUTH: return "SOUTH";
            default: return "<INVALID-DIRECTION>";
        }
    }

    public static void repaint(JComponent component, Rectangle2D r) {
        component.repaint((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
    }

    public static String toString(Color c) {
        return (c != null)
                ? "RGB[" + c.getRed() + ';' + c.getGreen() + ';' + c.getBlue() + ']' // NOI18N
                : "<NULL>"; // NOI18N
    }

    public static String toString(Rectangle2D r) {
        return "XYWH[" + r.getX() + ';' + r.getY() + ';' + r.getWidth() + ';' + r.getHeight() + ']'; // NOI18N
    }
    
    public static String toString(Shape s) {
        if (s instanceof Rectangle2D) {
            return toString((Rectangle2D)s);
        } else if (s != null) {
            return appendPath(new StringBuilder(200), 0, s.getPathIterator(null)).toString();
        } else {
            return "<NULL>"; // NOI18N
        }
    }

    public static String toStringHex8(int i) {
        String s = Integer.toHexString(i);
        while (s.length() < 8) {
            s = "0" + s; // NOI18N
        }
        return s;
    }

    public static String toStringId(Object o) {
        return (o != null)
                ? toStringHex8(System.identityHashCode(o))
                : "<NULL>"; // NOI18N
    }

    public static String toStringNameId(Object o) {
        if (o == null) {
            return "<NULL>"; // NOI18N
        }
        // Use last part (after '.') of class name
        String className = o.getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);
        return className + "@" + toStringId(o); // NOI18N
    }

    public static String toStringPrec1(double d) {
        String s = Double.toString(d);
        int dotIndex = s.indexOf('.');
        if (dotIndex >= 0 && dotIndex < s.length() - 2) {
            s = s.substring(0, dotIndex + 2);
        }
        return s;
    }

    public static String toString(AttributeSet attributes) {
        boolean nonFirst = false;
        StringBuilder sb = new StringBuilder(200);
        String fontName = (String) attributes.getAttribute(StyleConstants.FontFamily);
        Boolean bold = (Boolean) attributes.getAttribute(StyleConstants.Bold);
        Boolean italic = (Boolean) attributes.getAttribute(StyleConstants.Italic);
        Integer fontSizeInteger = (Integer) attributes.getAttribute(StyleConstants.FontSize);
        if (fontName != null || bold != null || italic != null || fontSizeInteger != null) {
            sb.append("Font[");
            sb.append((fontName != null) ? '"' + fontName + '"' : '?').append(',');
            if (bold != null || italic != null) {
                if (bold != null) {
                    sb.append('B');
                }
                if (italic != null) {
                    sb.append('I');
                }
            } else {
                sb.append('?');
            }
            sb.append(',');
            sb.append((fontSizeInteger != null) ? fontSizeInteger : '?');
            sb.append("], "); // NOI18N
            nonFirst = true;
        }
        Color foreColor = (Color) attributes.getAttribute(StyleConstants.Foreground);
        if (foreColor != null) {
            if (nonFirst) {
                sb.append(", "); // NOI18N
            }
            sb.append("fg=").append(toString(foreColor));
            nonFirst = true;
        }
        Color backColor = (Color) attributes.getAttribute(StyleConstants.Background);
        if (backColor != null) {
            if (nonFirst) {
                sb.append(", "); // NOI18N
            }
            sb.append("bg=").append(toString(backColor));
            nonFirst = true;
        }
        return sb.toString();
    }

    private static StringBuilder appendPath(StringBuilder sb, int indent, PathIterator pathIterator) {
        double[] coords = new double[6];
        while (!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);
            String typeStr;
            int endIndex;
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    typeStr = "SEG_CLOSE";
                    endIndex = 0;
                    break;
                case PathIterator.SEG_CUBICTO:
                    typeStr = "SEG_CUBICTO";
                    endIndex = 6;
                    break;
                case PathIterator.SEG_LINETO:
                    typeStr = "SEG_LINETO";
                    endIndex = 2;
                    break;
                case PathIterator.SEG_MOVETO:
                    typeStr = "SEG_MOVETO";
                    endIndex = 2;
                    break;
                case PathIterator.SEG_QUADTO:
                    typeStr = "SEG_QUADTO";
                    endIndex = 4;
                    break;
                default:
                    throw new IllegalStateException("Invalid type=" + type);
            }
            ArrayUtilities.appendSpaces(sb, indent);
            sb.append(typeStr).append(": ");
            for (int i = 0; i < endIndex;) {
                sb.append("[").append(coords[i++]).append(",").append(coords[i++]).append("] ");
            }
            sb.append('\n');
            pathIterator.next();
        }
        return sb;
    }
 
    public static String toString(JComponent component) {
        if (component == null) {
            return "<NULL>"; // NOI18N
        }
        StringBuilder sb = new StringBuilder(100);
        sb.append(toStringNameId(component));
        if (component instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) component;
            sb.append("\n  doc: ").append(toString(textComponent.getDocument())); // NOI18N
        }
        return sb.toString();
    }

    public static String toString(Document doc) {
        if (doc == null) {
            return "<NULL>"; // NOI18N
        }
        StringBuilder sb = new StringBuilder(100);
        sb.append(toStringNameId(doc));
        sb.append(", Length=").append(doc.getLength()); // NOI18N
        sb.append(", Version=").append(DocumentUtilities.getDocumentVersion(doc)); // NOI18N
        sb.append("\n    StreamDesc: "); // NOI18N
        Object streamDesc = doc.getProperty(Document.StreamDescriptionProperty);
        if (streamDesc != null) {
            sb.append(streamDesc);
        } else {
            sb.append("<NULL>"); // NOI18N
        }
        return sb.toString();
    }
    
    public static void checkFragmentBounds(int p0, int p1, int startOffset, int length) {
        if (p0 < startOffset || p0 > p1 || p1 > startOffset + length) {
            throw new IllegalArgumentException("Illegal bounds: <" + p0 + "," + p1 + // NOI18N
                    "> outside of <" + startOffset + "," + (startOffset+length) + ">"); // NOI18N
        }
    }

    /**
     * Useful for checking whether a view uses a compound attributes in which case
     * it should use another way of displaying itself.
     * @param attrs regular attributes or compound attributes (or null).
     * @return true if attributes are compound or false otherwise.
     */
    public static boolean isCompoundAttributes(AttributeSet attrs) {
        return (attrs instanceof CompoundAttributes);
    }

    /**
     * Get first attribute set of a compound attribute set or the given attribute set
     * if it's not compound.
     * @param attrs regular attributes or compound attributes (or null).
     * @return first attribute set.
     */
    public static AttributeSet getFirstAttributes(AttributeSet attrs) {
        return (attrs instanceof CompoundAttributes)
                ? ((CompoundAttributes)attrs).highlightItems()[0].getAttributes()
                : attrs;
    }

    /**
     * Run directly (if this is event dispatch thread) or post the runnable to EDT.
     *
     * @param r 
     */
    public static void runInEDT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    /**
     * Create position for the given offset.
     * This method is 
     * 
     * @param doc document in which the position should be created.
     * @param offset offset at which the position should be created.
     * @return non-null position
     * @throws IndexOutOfBoundsException in case the {@link Document#createPosition(int)}
     *   throws {@link BadLocationException}.
     */
    public static Position createPosition(Document doc, int offset) {
        try {
            return doc.createPosition(offset);
        } catch (BadLocationException ex) {
            throw new IndexOutOfBoundsException(ex.getMessage());
        }
    }

    /**
     * Log msg with FINE level or dump a stack if the logger has FINEST level.
     *
     * @param logger
     * @param msg message to log
     */
    public static void log(Logger logger, String msg) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.INFO, "Cause of " + msg, new Exception());
        } else {
            logger.fine(msg);
        }
    }

}
