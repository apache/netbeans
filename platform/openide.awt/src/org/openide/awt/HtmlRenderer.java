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

package org.openide.awt;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import org.openide.util.Utilities;

/**
 * A lightweight HTML renderer supporting a minimal subset of HTML used for
 * markup purposes only: basic font styles and some colors.
 * <p>
 * Provides a generic cell renderer implementation which can be used for trees, tables,
 * lists, combo boxes, etc.
 * <p>
 * If you only need to paint some HTML quickly, use the static methods for
 * painting.  These methods behave as follows:
 * <ul>
 * <li>{@link #renderString renderString} will check the string for opening HTML tags
 * (upper or lower but not mixed case) and call either {@link #renderPlainString renderPlainString}
 * or {@link #renderHTML renderHTML} as appropriate.  Note this method does not tolerate
 * whitespace in opening HTML tags - it expects exactly 6 characters to make up
 * the opening tag if present.</li>
 * <li>{@link #renderPlainString renderPlainString} simply renders a string to the graphics context,
 * takes the same arguments as {@link #renderHTML renderHTML}, but will also honor
 * {@link #STYLE_TRUNCATE}, so strings can be rendered with trailing
 * ellipsis if there is not enough space</li>
 * <li>{@link #renderHTML renderHTML} renders whatever is passed to it as HTML, regardless
 * of whether it has opening HTML tags or not.  It can be used to render plain
 * strings, but {@link #renderPlainString renderPlainString} is faster for that. It is useful
 * if you want to render a string you <strong>know</strong> to be compliant
 * HTML markup, but which does not have opening and closing HTML tags (though
 * they are harmless if present). </li>
 * </ul>
 * <p>
 * This parser is designed entirely for performance; there are no separate parsing
 * and rendering loops.  In order to achieve its performance, some trade-offs
 * are required.
 * <strong>This is not a forgiving HTML parser - the HTML supplied
 * must follow the guidelines documented here!</strong>
 * <p>
 * The following tags are supported, in upper or lower (but not mixed) case:
 * </p>
 * <table border="1">
 * <tr>
 *  <td><code>&lt;b&gt;</code></td>
 *  <td>Boldface text</td>
 * </tr>
 * <tr>
 *  <td><code>&lt;s&gt;</code></td>
 *  <td>Strikethrough text</td>
 * </tr>
 * <tr>
 *  <td><code>&lt;u&gt;</code></td>
 *  <td>Underline text</td>
 * </tr>
 * <tr>
 *  <td><code>&lt;i&gt;</code></td>
 *  <td>Italic text</td>
 * </tr>
 * <tr>
 *  <td><code>&lt;a&gt;</code></td>
 *  <td>Link text</td>
 * </tr>
 * <tr>
 *  <td><code>&lt;em&gt;</code></td>
 *  <td>Emphasized text (same as italic)</td>
 * </tr>
 * <tr>
 *  <td><code>&lt;strong&gt;</code></td>
 *  <td>Strong text (same as bold)</td>
 * </tr>
 * <tr>
 *  <td><code>&lt;font&gt;</code></td>
 *  <td>Font color - font attributes other than color are not supported.  Colors
 *  may be specified as hexadecimal strings, such as #FF0000 or as logical colors
 *  defined in the current look and feel by specifying a ! character as the first
 *  character of the color name.  Logical colors are colors available from the
 *  current look and feel's UIManager.  For example,
 *  <code>&lt;font&nbsp;color="!Tree.background"&gt;</code> will set the font color to the
 *  result of {@link UIManager#getColor(Object) UIManager.getColor("Tree.background")}.
 * <strong>Font size tags are not supported.</strong>
 * </td>
 * </tr>
 * </table>
 * <p>
 * The lightweight HTML renderer supports the following named SGML character
 * entities: <code>quot</code>, <code>lt</code>, <code>amp</code>, <code>lsquo</code>,
 * <code>rsquo</code>, <code>ldquo</code>, <code>rdquo</code>, <code>ndash</code>,
 * <code>mdash</code>, <code>ne</code>, <code>le</code>, <code>ge</code>,
 * <code>copy</code>, <code>reg</code>, <code>trade</code>, and <code>nbsp</code>.
 * It also supports numeric entities
 * (e.g. <code>&amp;8822;</code>).
 * <p><b>Why not use the JDK's HTML support?</b> The JDK's HTML support works
 * well for stable components, but suffers from performance problems in the
 * case of cell renderers - each call to set the text (which happens once per
 * cell, per paint) causes a document tree to be created in memory.  For small,
 * markup-only strings, this is overkill.   For rendering short strings
 * (for example, in a tree or table cell renderer)
 * with limited HTML, this method is approximately 10x faster than standard
 * Swing HTML rendering.
 *
 * <P><B><U>Specifying logical colors</U></B><BR>
 * Hardcoded text colors are undesirable, as they can be incompatible (even
 * invisible) on some look and feels or themes, depending on the background
 * color.
 * The lightweight HTML renderer supports a non-standard syntax for specifying
 * font colors via a key for a color in the UI defaults for the current look
 * and feel.  This is accomplished by prefixing the key name with a <code>!</code>
 * character.  For example: <code>&lt;font color='!controlShadow'&gt;</code>.
 *
 * <P><B><U>Modes of operation</U></B><BR>
 * This method supports two modes of operation:
 * <OL>
 * <LI>{@link #STYLE_CLIP} - as much text as will fit in the pixel width passed
 * to the method should be painted, and the text should be cut off at the maximum
 * width or clip rectangle maximum X boundary for the graphics object, whichever is
 * smaller.</LI>
 * <LI>{@link #STYLE_TRUNCATE} - paint as much text as will fit in the pixel
 * width passed to the method, but paint the last three characters as .'s, in the
 * same manner as a JLabel truncates its text when the available space is too
 * small.</LI>
 * <!-- XXX and #STYLE_WORDWRAP? -->
 * </OL>
 * <P>
 * The paint methods can also be used in non-painting mode to establish the space
 * necessary to paint a string.  This is accomplished by passing the value of the
 * <code>paint</code> argument as false.  The return value will be the required
 * width in pixels
 * to display the text.  Note that in order to retrieve an
 * accurate value, the argument for available width should be passed
 * as {@link Integer#MAX_VALUE} or an appropriate maximum size - otherwise
 * the return value will either be the passed maximum width or the required
 * width, whichever is smaller.  Also, the clip shape for the passed graphics
 * object should be null or a value larger than the maximum possible render size,
 * or text size measurement will stop at the clip bounds.
 * <!-- XXX what does the following mean? <code>getGraphics</code>
 * will always return non-null and non-clipped, and is suitable to pass in such a
 * situation. -->
 * <P>
 *
 * <P>
 * <B>Example usages:</B><BR>
 * <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getHtmlDisplayName()">org.openide.nodes.Node.getHtmlDisplayName</a><BR>
 * <a href="@org-openide-filesystems@/org/openide/filesystems/FileSystem.HtmlStatus.html">org.openide.filesystems.FileSystem.HtmlStatus</a>
 * </P>
 *
 * @since 4.30
 * @author  Tim Boudreau
 */
public final class HtmlRenderer {

    /** Stack object used during HTML rendering to hold previous colors in
     * the case of nested color entries. */
    private static LinkedList<Color> colorStack = new LinkedList<Color>();

    /**
     * Constant used by {@link #renderString renderString}, {@link #renderPlainString renderPlainString},
     * {@link #renderHTML renderHTML}, and {@link Renderer#setRenderStyle}
     * if painting should simply be cut off at the boundary of the coordinates passed.
     */
    public static final int STYLE_CLIP = 0;

    /**
     * Constant used by {@link #renderString renderString}, {@link #renderPlainString renderPlainString},
     * {@link #renderHTML renderHTML}, and {@link Renderer#setRenderStyle} if
     * painting should produce an ellipsis (...)
     * if the text would overlap the boundary of the coordinates passed.
     */
    public static final int STYLE_TRUNCATE = 1;

    /**
     * Constant used by {@link #renderString renderString}, {@link #renderPlainString renderPlainString},
     * {@link #renderHTML renderHTML}, and {@link Renderer#setRenderStyle}
     * if painting should word wrap the text.  In
     * this case, the return value of any of the above methods will be the
     * height, rather than width painted.
     */
    private static final int STYLE_WORDWRAP = 2;

    /** System property to cause exceptions to be thrown when unparsable
     * html is encountered */
    private static final boolean STRICT_HTML = Boolean.getBoolean("netbeans.lwhtml.strict"); //NOI18N

    /** Cache for strings which have produced errors, so we don't post an
     * error message more than once */
    private static Set<String> badStrings = null;

    private static Logger LOG = Logger.getLogger(HtmlRenderer.class.getName());

    /** Definitions for a limited subset of SGML character entities */
    private static final Object[] entities = new Object[] {
            new char[] { 'g', 't' }, new char[] { 'l', 't' }, //NOI18N
            new char[] { 'q', 'u', 'o', 't' }, new char[] { 'a', 'm', 'p' }, //NOI18N
            new char[] { 'l', 's', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'r', 's', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'l', 'd', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'r', 'd', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'n', 'd', 'a', 's', 'h' }, //NOI18N
            new char[] { 'm', 'd', 'a', 's', 'h' }, //NOI18N
            new char[] { 'n', 'e' }, //NOI18N
            new char[] { 'l', 'e' }, //NOI18N
            new char[] { 'g', 'e' }, //NOI18N
            new char[] { 'c', 'o', 'p', 'y' }, //NOI18N
            new char[] { 'r', 'e', 'g' }, //NOI18N
            new char[] { 't', 'r', 'a', 'd', 'e' }, //NOI18N
            new char[] { 'n', 'b', 's', 'p' //NOI18N
            }
        }; //NOI18N

    /** Mappings for the array of SGML character entities to characters */
    private static final char[] entitySubstitutions = new char[] {
            '>', '<', '"', '&', 8216, 8217, 8220, 8221, 8211, 8212, 8800, 8804, 8805, //NOI18N
            169, 174, 8482, ' '
        };
    private HtmlRenderer() {
        //do nothing
    }

    /**
     * Returns an instance of Renderer which may be used as a table/tree/list cell renderer.
     * This method must be called on the AWT event thread.  If you <strong>know</strong> you will
     * be passing it legal HTML (legal as documented here), call {@link Renderer#setHtml setHtml(true)} on the
     * result of this call <strong>after calling getNNNCellRenderer</strong> to provide this hint.
     *
     * @return A cell renderer that can render HTML.
     */
    public static final Renderer createRenderer() {
        return new HtmlRendererImpl();
    }

    /**
     * For HTML rendering jobs outside of trees/lists/tables, returns a JLabel which will paint its text using
     * the lightweight HTML renderer.  The result of this call will implement {@link Renderer}.
     * <strong>Do not add the result of this call to the AWT hierarchy</strong>.  It is not a general purpose <code>JLabel</code>, and
     * will not behave correctly.  Use the result of this call to paint or to measure text.  Example:
     * <pre>
     * private final JLabel label = HtmlRenderer.createLabel();
     *
     * public void paint(Graphics g) {
     *    // background/whatever painting code here...
     *    label.setText(someHtmlText);
     *    label.paint(g);
     * }
     * </pre>
     *
     *
     * @return a label which can render a subset of HTML very quickly
     */
    public static final JLabel createLabel() {
        return new HtmlRendererImpl();
    }

    /**
     * Render a string to a graphics instance, using the same API as {@link #renderHTML renderHTML}.
     * Can render a string using JLabel-style ellipsis (...) in the case that
     * it will not fit in the passed rectangle, if the style parameter is
     * {@link #STYLE_CLIP}. Returns the width in pixels successfully painted.
     * <strong>This method is not thread-safe and should not be called off
     * the AWT thread.</strong>
     *
     * @see #renderHTML
     */
    public static double renderPlainString(
        String s, Graphics g, int x, int y, int w, int h, Font f, Color defaultColor, int style, boolean paint
    ) {
        //per Jarda's request, keep the word wrapping code but don't expose it.
        if ((style < 0) || (style > 1)) {
            throw new IllegalArgumentException("Unknown rendering mode: " + style); //NOI18N
        }

        return _renderPlainString(s, g, x, y, w, h, f, defaultColor, style, paint);
    }

    private static double _renderPlainString(
        String s, Graphics g, int x, int y, int w, int h, Font f, Color foreground, int style, boolean paint
    ) {
        if (f == null) {
            f = UIManager.getFont("controlFont"); //NOI18N

            if (f == null) {
                int fs = 11;
                Object cfs = UIManager.get("customFontSize"); //NOI18N

                if (cfs instanceof Integer) {
                    fs = ((Integer) cfs).intValue();
                }

                f = new Font("Dialog", Font.PLAIN, fs); //NOI18N
            }
        }

        FontMetrics fm = g.getFontMetrics(f);
//        Rectangle2D r = fm.getStringBounds(s, g);
        int wid;
        if (Utilities.isMac()) {
            // #54257 - on macosx + chinese/japanese fonts, the getStringBounds() method returns bad value
            wid = fm.stringWidth(s);
        } else {
            wid = (int)fm.getStringBounds(s, g).getWidth();
        }

        if (paint) {
            g.setColor(foreground);
            g.setFont(f);

            if ((wid <= w) || (style == STYLE_CLIP)) {
                g.drawString(s, x, y);
            } else {
                char[] chars = s.toCharArray();

                if (chars.length == 0) {
                    return 0;
                }

                double chWidth = wid / chars.length;
                int estCharsToPaint = new Double(w / chWidth).intValue();
                if( estCharsToPaint > chars.length )
                    estCharsToPaint = chars.length;
                //let's correct the estimate now
                while( estCharsToPaint > 3 ) {
                    if( estCharsToPaint < chars.length )
                        chars[estCharsToPaint-1] = '…';
                    int  newWidth;
                    if (Utilities.isMac()) {
                        // #54257 - on macosx + chinese/japanese fonts, the getStringBounds() method returns bad value
                        newWidth = fm.stringWidth(new String(chars, 0, estCharsToPaint));
                    } else {
                        newWidth = (int)fm.getStringBounds(chars, 0, estCharsToPaint, g).getWidth();
                    }
                    if( newWidth <= w )
                        break;
                    estCharsToPaint--;
                }

                if (style == STYLE_TRUNCATE) {
                    int length = estCharsToPaint;

                    if (length <= 0) {
                        return 0;
                    }

                    if (paint) {
                        if (length > 3) {
                            g.drawChars(chars, 0, length, x, y);
                        } else {
                            Shape shape = g.getClip();
                            // clip only if clipping is supported
                            if (shape != null) {
                                if (s != null) {
                                    Area area = new Area(shape);
                                    area.intersect(new Area(new Rectangle(x, y, w, h)));
                                    g.setClip(area);
                                } else {
                                    g.setClip(new Rectangle(x, y, w, h));
                                }
                            }

                            g.drawString("…", x, y);
                            if (shape != null) {
                                g.setClip(shape);
                            }
                        }
                    }
                } else {
                    //TODO implement plaintext word wrap if we want to support it at some point
                }
            }
        }

        return wid;
    }

    /**
     * Render a string to a graphics context, using HTML markup if the string
     * begins with <code>&lt;html&gt;</code>.  Delegates to {@link #renderPlainString renderPlainString}
     * or {@link #renderHTML renderHTML} as appropriate.  See the class documentation for
     * for details of the subset of HTML that is
     * supported.
     * @param s The string to render
     * @param g A graphics object into which the string should be drawn, or which should be
     * used for calculating the appropriate size
     * @param x The x coordinate to paint at.
     * @param y The y position at which to paint.  Note that this method does not calculate font
     * height/descent - this value should be the baseline for the line of text, not
     * the upper corner of the rectangle to paint in.
     * @param w The maximum width within which to paint.
     * @param h The maximum height within which to paint.
     * @param f The base font to be used for painting or calculating string width/height.
     * @param defaultColor The base color to use if no font color is specified as html tags
     * @param style The wrapping style to use, either {@link #STYLE_CLIP},
     * or {@link #STYLE_TRUNCATE}
     * @param paint True if actual painting should occur.  If false, this method will not actually
     * paint anything, only return a value representing the width/height needed to
     * paint the passed string.
     * @return The width in pixels required
     * to paint the complete string, or the passed parameter <code>w</code> if it is
     * smaller than the required width.
     */
    public static double renderString(
        String s, Graphics g, int x, int y, int w, int h, Font f, Color defaultColor, int style, boolean paint
    ) {
        switch (style) {
        case STYLE_CLIP:
        case STYLE_TRUNCATE:
            break;

        default:
            throw new IllegalArgumentException("Unknown rendering mode: " + style); //NOI18N
        }

        //        System.err.println ("rps: " + y + " " + s);
        if (s.startsWith("<html") || s.startsWith("<HTML")) { //NOI18N

            return _renderHTML(s, 6, g, x, y, w, h, f, defaultColor, style, paint, null);
        } else {
            return renderPlainString(s, g, x, y, w, h, f, defaultColor, style, paint);
        }
    }

    /**
     * Render a string as HTML using a fast, lightweight renderer supporting a limited
     * subset of HTML.  See class Javadoc for details.
     *
     * <P>
     * This method can also be used in non-painting mode to establish the space
     * necessary to paint a string.  This is accomplished by passing the value of the
     * <code>paint</code> argument as false.  The return value will be the required
     * width in pixels
     * to display the text.  Note that in order to retrieve an
     * accurate value, the argument for available width should be passed
     * as {@link Integer#MAX_VALUE} or an appropriate maximum size - otherwise
     * the return value will either be the passed maximum width or the required
     * width, whichever is smaller.  Also, the clip shape for the passed graphics
     * object should be null or a value larger than the maximum possible render size.
     * <P>
     * This method will log a warning if it encounters HTML markup it cannot
     * render.  To aid diagnostics, if NetBeans is run with the argument
     * <code>-J-Dnetbeans.lwhtml.strict=true</code> an exception will be thrown
     * when an attempt is made to render unsupported HTML.
     * @param s The string to render
     * @param g A graphics object into which the string should be drawn, or which should be
     * used for calculating the appropriate size
     * @param x The x coordinate to paint at.
     * @param y The y position at which to paint.  Note that this method does not calculate font
     * height/descent - this value should be the baseline for the line of text, not
     * the upper corner of the rectangle to paint in.
     * @param w The maximum width within which to paint.
     * @param h The maximum height within which to paint.
     * @param f The base font to be used for painting or calculating string width/height.
     * @param defaultColor The base color to use if no font color is specified as html tags
     * @param style The wrapping style to use, either {@link #STYLE_CLIP},
     * or {@link #STYLE_TRUNCATE}
     * @param paint True if actual painting should occur.  If false, this method will not actually
     * paint anything, only return a value representing the width/height needed to
     * paint the passed string.
     * @return The width in pixels required
     * to paint the complete string, or the passed parameter <code>w</code> if it is
     * smaller than the required width.
     */
    public static double renderHTML(
        String s, Graphics g, int x, int y, int w, int h, Font f, Color defaultColor, int style, boolean paint
    ) {
        //per Jarda's request, keep the word wrapping code but don't expose it.
        if ((style < 0) || (style > 1)) {
            throw new IllegalArgumentException("Unknown rendering mode: " + style); //NOI18N
        }

        return _renderHTML(s, 0, g, x, y, w, h, f, defaultColor, style, paint, null);
    }


    /** Implementation of HTML rendering */
    static double _renderHTML(
        String s, int pos, Graphics g, int x, int y, int w, int h, Font f, Color defaultColor, int style, boolean paint,
        Color background
    ) {
        return _renderHTML( s, pos, g, x, y, w, h, f, defaultColor, style, paint, background, false );
    }

    private static void configureRenderingHints(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        Object desktopHints
                = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (desktopHints instanceof Map<?, ?>) {
            g.addRenderingHints((Map<?, ?>) desktopHints);
        } else if (HtmlLabelUI.antialias) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

    }

    /** Implementation of HTML rendering */
    static double _renderHTML(
        String s, int pos, Graphics g, int x, int y, int w, int h, Font f, Color defaultColor, int style, boolean paint,
        Color background, boolean forcedForeground
    ) {
        //        System.err.println ("rhs: " + y + " " + s);
        if (f == null) {
            f = UIManager.getFont("controlFont"); //NOI18N

            if (f == null) {
                int fs = 11;
                Object cfs = UIManager.get("customFontSize"); //NOI18N

                if (cfs instanceof Integer) {
                    fs = ((Integer) cfs).intValue();
                }

                f = new Font("Dialog", Font.PLAIN, fs); //NOI18N
            }
        }

        //Thread safety - avoid allocating memory for the common case
        LinkedList<Color> _colorStack = EventQueue.isDispatchThread() ? HtmlRenderer.colorStack : new LinkedList<Color>();

        g.setColor(defaultColor);
        g.setFont(f);
        configureRenderingHints(g);

        char[] chars = s.toCharArray();
        int origX = x;
        boolean done = false; //flag if rendering completed, either by finishing the string or running out of space
        boolean inTag = false; //flag if the current position is inside a tag, and the tag should be processed rather than rendering
        boolean inClosingTag = false; //flag if the current position is inside a closing tag
        boolean strikethrough = false; //flag if a strikethrough line should be painted
        boolean underline = false; //flag if an underline should be painted
        boolean link = false; //flag if a link should be painted
        boolean bold = false; //flag if text is currently bold
        boolean italic = false; //flag if text is currently italic
        boolean truncated = false; //flag if the last possible character has been painted, and the next loop should paint "..." and return
        double widthPainted = 0; //the total width painted, for calculating needed space
        double heightPainted = 0; //the total height painted, for calculating needed space
        boolean lastWasWhitespace = false; //flag to skip additional whitespace if one whitespace char already painted
        double lastHeight = 0; //the last line height, for calculating total required height

        double dotWidth = 0;
        boolean dotsPainted = false;

        //Calculate the width of a . character if we may need to truncate
        if (style == STYLE_TRUNCATE) {
            dotWidth = g.getFontMetrics().charWidth('.'); //NOI18N
        }

        /* How this all works, for anyone maintaining this code (hopefully it will
          never need it):
          1. The string is converted to a char array
          2. Loop over the characters.  Variable pos is the current point.
            2a. See if we're in a tag by or'ing inTag with currChar == '<'
              If WE ARE IN A TAG:
               2a1: is it an opening tag?
                 If YES:
                   - Identify the tag, Configure the Graphics object with
                     the appropriate font, color, etc.  Set pos = the first
                     character after the tag
                 If NO (it's a closing tag)
                   - Identify the tag.  Reconfigure the Graphics object
                     with the state it should be in outside the tag
                     (reset the font if italic, pop a color off the stack, etc.)
            2b. If WE ARE NOT IN A TAG
               - Locate the next < or & character or the end of the string
               - Paint the characters using the Graphics object
               - Check underline and strikethrough tags, and paint line if
                 needed
            See if we're out of space, and do the right thing for the style
            (paint ..., give up or skip to the next line)
         */
        //Clear any junk left behind from a previous rendering loop
        _colorStack.clear();


        //Enter the painting loop
        while (!done) {
            if (pos == s.length()) {
                if( truncated && paint && !dotsPainted ) {
                    g.setColor(defaultColor);
                    g.setFont(f);
                    g.drawString("…", x, y); //NOI18N
                }
                return widthPainted;
            }

            //see if we're in a tag
            try {
                inTag |= (chars[pos] == '<');
            } catch (ArrayIndexOutOfBoundsException e) {
                //Should there be any problem, give a meaningful enough
                //message to reproduce the problem
                ArrayIndexOutOfBoundsException aib = new ArrayIndexOutOfBoundsException(
                        "HTML rendering failed at position " + pos + " in String \"" //NOI18N
                         +s + "\".  Please report this at http://www.netbeans.org"
                    ); //NOI18N

                if (STRICT_HTML) {
                    throw aib;
                } else {
                    Logger.getLogger(HtmlRenderer.class.getName()).log(Level.WARNING, null, aib);

                    return renderPlainString(s, g, x, y, w, h, f, defaultColor, style, paint);
                }
            }

            inClosingTag = inTag && ((pos + 1) < chars.length) && (chars[pos + 1] == '/'); //NOI18N

            if (truncated) {
                //Then we've almost run out of space, time to print ... and quit
                g.setColor(defaultColor);
                g.setFont(f);

                if (paint) {
                    g.drawString("…", x, y); //NOI18N
                    dotsPainted = true; //make sure we paint the dots only once
                }

                done = true;
            } else if (inTag) {
                //If we're in a tag, don't paint, process it
                pos++;

                int tagEnd = pos;

                //#54237 - if done and end of string -> wrong html
                done = tagEnd >= (chars.length - 1);

                while (!done && (chars[tagEnd] != '>')) {
                    done = tagEnd == (chars.length - 1);
                    tagEnd++;
                }

                if (done) {
                    throwBadHTML("Matching '>' not found", pos, chars);
                    break;
                }

                if (inClosingTag) {
                    //Handle closing tags by resetting the Graphics object (font, etc.)
                    pos++;

                    switch (chars[pos]) {
                    case 'a': //NOI18N
                    case 'A': //NOI18N
                        
                        if (_colorStack.isEmpty() || forcedForeground) {
                            g.setColor(defaultColor);
                        } else {
                            g.setColor(_colorStack.pop());
                        }
                        link = false;

                        break;
                    case 'P': //NOI18N
                    case 'p': //NOI18N
                    case 'H': //NOI18N
                    case 'h':
                        break; //ignore html opening/closing tags

                    case 'B': //NOI18N
                    case 'b': //NOI18N

                        if ((chars[pos + 1] == 'r') || (chars[pos + 1] == 'R')) {
                            break;
                        }

                        if (!bold && !(chars[pos+1] == 'o' || chars[pos+1] == 'O')) {
                            throwBadHTML("Closing bold tag w/o " + //NOI18N
                                "opening bold tag", pos, chars
                            ); //NOI18N
                        }

                        if (italic) {
                            g.setFont(deriveFont(f, Font.ITALIC));
                        } else {
                            g.setFont(deriveFont(f, Font.PLAIN));
                        }

                        bold = false;

                        break;

                    case 'E': //NOI18N
                    case 'e': //em tag
                    case 'I': //NOI18N
                    case 'i': //NOI18N

                        if (bold) {
                            g.setFont(deriveFont(f, Font.BOLD));
                        } else {
                            g.setFont(deriveFont(f, Font.PLAIN));
                        }

                        if (!italic) {
                            throwBadHTML("Closing italics tag w/o" //NOI18N
                                 +"opening italics tag", pos, chars
                            ); //NOI18N
                        }

                        italic = false;

                        break;

                    case 'S': //NOI18N
                    case 's': //NOI18N

                        switch (chars[pos + 1]) {
                        case 'T': //NOI18N
                        case 't':

                            if (italic) { //NOI18N
                                g.setFont(deriveFont(f, Font.ITALIC));
                            } else {
                                g.setFont(deriveFont(f, Font.PLAIN));
                            }

                            bold = false;

                            break;

                        case '>': //NOI18N
                            strikethrough = false;

                            break;
                        }

                        break;

                    case 'U': //NOI18N
                    case 'u':
                        underline = false; //NOI18N

                        break;

                    case 'F': //NOI18N
                    case 'f': //NOI18N

                        if (_colorStack.isEmpty() || forcedForeground) {
                            g.setColor(defaultColor);
                        } else {
                            g.setColor(_colorStack.pop());
                        }

                        break;

                    default:
                        throwBadHTML("Malformed or unsupported HTML", //NOI18N
                            pos, chars
                        );
                    }
                } else {
                    //Okay, we're in an opening tag.  See which one and configure the Graphics object
                    switch (chars[pos]) {
                    case 'a': //NOI18N
                    case 'A': //NOI18N

                        if( !forcedForeground ) {
                            Color linkc = UIManager.getColor("nb.html.link.foreground");    // NOI18N
                            if (linkc == null) {
                                linkc = Color.BLUE;
                            }
                            _colorStack.push(g.getColor());

                            linkc = HtmlLabelUI.ensureContrastingColor(linkc, background);
                            g.setColor(linkc);
                        }
                        link = true;

                        break;
                        
                    case 'B': //NOI18N
                    case 'b': //NOI18N

                        switch (chars[pos + 1]) {
                        case 'R': //NOI18N
                        case 'r': //NOI18N

                            if (style == STYLE_WORDWRAP) {
                                x = origX;

                                int lineHeight = g.getFontMetrics().getHeight();
                                y += lineHeight;
                                heightPainted += lineHeight;
                                widthPainted = 0;
                            }

                            break;

                        case '>':
                            bold = true;

                            if (italic) {
                                g.setFont(deriveFont(f, Font.BOLD | Font.ITALIC));
                            } else {
                                g.setFont(deriveFont(f, Font.BOLD));
                            }

                            break;
                        }

                        break;

                    case 'e': //NOI18N  //em tag
                    case 'E': //NOI18N
                    case 'I': //NOI18N
                    case 'i': //NOI18N
                        italic = true;

                        if (bold) {
                            g.setFont(deriveFont(f, Font.ITALIC | Font.BOLD));
                        } else {
                            g.setFont(deriveFont(f, Font.ITALIC));
                        }

                        break;

                    case 'S': //NOI18N
                    case 's': //NOI18N

                        switch (chars[pos + 1]) {
                        case '>':
                            strikethrough = true;

                            break;

                        case 'T':
                        case 't':
                            bold = true;

                            if (italic) {
                                g.setFont(deriveFont(f, Font.BOLD | Font.ITALIC));
                            } else {
                                g.setFont(deriveFont(f, Font.BOLD));
                            }

                            break;
                        }

                        break;

                    case 'U': //NOI18N
                    case 'u': //NOI18N
                        underline = true;

                        break;

                    case 'f': //NOI18N
                    case 'F': //NOI18N
                        if( !forcedForeground ) {
                            Color c = findColor(chars, pos, tagEnd);
                            _colorStack.push(g.getColor());

                            c = HtmlLabelUI.ensureContrastingColor(c, background);

                            g.setColor(c);
                        }

                        break;

                    case 'P': //NOI18N
                    case 'p': //NOI18N

                        if (style == STYLE_WORDWRAP) {
                            x = origX;

                            int lineHeight = g.getFontMetrics().getHeight();
                            y += (lineHeight + (lineHeight / 2));
                            heightPainted = y + lineHeight;
                            widthPainted = 0;
                        }

                        break;

                    case 'H':
                    case 'h': //Just an opening HTML tag

                        if (pos == 1) {
                            break;
                        } else { // fallthrough warning
                            throwBadHTML("Malformed or unsupported HTML", pos, chars); //NOI18N
                            break;
                        }

                    default:
                        throwBadHTML("Malformed or unsupported HTML", pos, chars); //NOI18N
                    }
                }

                pos = tagEnd + (done ? 0 : 1);
                inTag = false;
            } else {
                //Okay, we're not in a tag, we need to paint
                if (lastWasWhitespace) {
                    //Skip multiple whitespace characters
                    while ((pos < (s.length() - 1)) && Character.isWhitespace(chars[pos])) {
                        pos++;
                    }

                    //Check strings terminating with multiple whitespace -
                    //otherwise could get an AIOOBE here
                    if (pos == (chars.length - 1)) {
                        return (style != STYLE_WORDWRAP) ? widthPainted : heightPainted;
                    }
                }

                //Flag to indicate if an ampersand entity was processed,
                //so the resulting & doesn't get treated as the beginning of
                //another entity (and loop endlessly)
                boolean isAmp = false;

                //Flag to indicate the next found < character really should
                //be painted (it came from an entity), it is not the beginning
                //of a tag
                boolean nextLtIsEntity = false;
                int nextTag = chars.length - 1;

                if ((chars[pos] == '&')) { //NOI18N

                    boolean inEntity = pos != (chars.length - 1);

                    if (inEntity) {
                        int newPos = substEntity(chars, pos + 1);
                        inEntity = newPos != -1;

                        if (inEntity) {
                            pos = newPos;
                            isAmp = chars[pos] == '&'; //NOI18N

                            nextLtIsEntity = chars[pos] == '<';
                        } else {
                            nextLtIsEntity = false;
                            isAmp = true;
                        }
                    }
                } else {
                    nextLtIsEntity = false;
                }

                for (int i = pos; i < chars.length; i++) {
                    if ((chars[i] == '<' && !nextLtIsEntity) || (chars[i] == '&' && !isAmp && i != chars.length - 1)) {
                        nextTag = i - 1;

                        break;
                    }

                    //Reset these flags so we don't skip all & or < chars for the rest of the string
                    isAmp = false;
                    nextLtIsEntity = false;
                }

                FontMetrics fm = g.getFontMetrics();

                //Get the bounds of the substring we'll paint
                Rectangle2D r = fm.getStringBounds(chars, pos, nextTag + 1, g);
                if (Utilities.isMac()) {
                    // #54257 - on macosx + chinese/japanese fonts, the getStringBounds() method returns bad value
                    r.setRect(r.getX(), r.getY(), (double)fm.stringWidth(new String(chars, pos, nextTag - pos + 1)), r.getHeight());
                } 

                //Store the height, so we can add it if we're in word wrap mode,
                //to return the height painted
                lastHeight = r.getHeight();

                //Work out the length of this tag
                int length = (nextTag + 1) - pos;

                //Flag to be set to true if we run out of space
                boolean goToNextRow = false;

                //Flag that the current line is longer than the available width,
                //and should be wrapped without finding a word boundary
                boolean brutalWrap = false;

                //Work out the per-character avg width of the string, for estimating
                //when we'll be out of space and should start the ... in truncate
                //mode
                double chWidth;

                if (truncated) {
                    //if we're truncating, use the width of one dot from an
                    //ellipsis to get an accurate result for truncation
                    chWidth = dotWidth;
                } else {
                    //calculate an average character width
                    chWidth = r.getWidth() / (nextTag+1 - pos);

                    //can return this sometimes, so handle it
                    if ((chWidth == Double.POSITIVE_INFINITY) || (chWidth == Double.NEGATIVE_INFINITY)) {
                        chWidth = fm.getMaxAdvance();
                    }
                }

                if (
                    ((style != STYLE_CLIP) &&
                        ((style == STYLE_TRUNCATE) && ((widthPainted + r.getWidth()) > (w /*- (chWidth * 3)*/)))) ||
                        /** mkleint - commented out the "- (chWidth *3) because it makes no sense to strip the text and add dots when it fits exactly
                         * into the rendering rectangle.. with this condition we stripped even strings that came close to the limit..
                         **/
                        ((style == STYLE_WORDWRAP) && ((widthPainted + r.getWidth()) > w))
                ) {
                    if (chWidth > 3) {
                        double pixelsOff = (widthPainted + (r.getWidth() + 5)) - w;

                        double estCharsOver = pixelsOff / chWidth;

                        if (style == STYLE_TRUNCATE) {
                            int charsToPaint = Math.round(Math.round(Math.ceil((w - widthPainted) / chWidth)));

                            /*                            System.err.println("estCharsOver = " + estCharsOver);
                                                        System.err.println("Chars to paint " + charsToPaint + " chwidth = " + chWidth + " widthPainted " + widthPainted);
                                                        System.err.println("Width painted + width of tag: " + (widthPainted + r.getWidth()) + " available: " + w);
                             */
                            int startPeriodsPos = (pos + charsToPaint) - 3;

                            if (startPeriodsPos >= chars.length) {
                                startPeriodsPos = chars.length - 4;
                            }

                            length = (startPeriodsPos - pos);

                            if (length < 0) {
                                length = 0;
                            }

                            r = fm.getStringBounds(chars, pos, pos + length, g);
                            if (Utilities.isMac()) {
                                // #54257 - on macosx + chinese/japanese fonts, the getStringBounds() method returns bad value
                                r.setRect(r.getX(), r.getY(), (double)fm.stringWidth(new String(chars, pos, length)), r.getHeight());
                            } 

                            //                            System.err.println("Truncated set to true at " + pos + " (" + chars[pos] + ")");
                            truncated = true;
                        } else {
                            //Word wrap mode
                            goToNextRow = true;

                            int lastChar = new Double(nextTag - estCharsOver).intValue();

                            //Unlike Swing's word wrap, which does not wrap on tag boundaries correctly, if we're out of space,
                            //we're out of space
                            brutalWrap = x == 0;

                            for (int i = lastChar; i > pos; i--) {
                                lastChar--;

                                if (Character.isWhitespace(chars[i])) {
                                    length = (lastChar - pos) + 1;
                                    brutalWrap = false;

                                    break;
                                }
                            }

                            if ((lastChar <= pos) && (length > estCharsOver) && !brutalWrap) {
                                x = origX;
                                y += r.getHeight();
                                heightPainted += r.getHeight();

                                boolean boundsChanged = false;

                                while (!done && Character.isWhitespace(chars[pos]) && (pos < nextTag)) {
                                    pos++;
                                    boundsChanged = true;
                                    done = pos == (chars.length - 1);
                                }

                                if (pos == nextTag) {
                                    lastWasWhitespace = true;
                                }

                                if (boundsChanged) {
                                    //recalculate the width we will add
                                    r = fm.getStringBounds(chars, pos, nextTag + 1, g);
                                    if (Utilities.isMac()) {
                                        // #54257 - on macosx + chinese/japanese fonts, the getStringBounds() method returns bad value
                                        r.setRect(r.getX(), r.getY(), (double)fm.stringWidth(new String(chars, pos, nextTag - pos + 1)), r.getHeight());
                                    } 
                                }

                                goToNextRow = false;
                                widthPainted = 0;

                                if (chars[pos - 1 + length] == '<') {
                                    length--;
                                }
                            } else if (brutalWrap) {
                                //wrap without checking word boundaries
                                length = (new Double((w - widthPainted) / chWidth)).intValue();

                                if ((pos + length) > nextTag) {
                                    length = (nextTag - pos);
                                }

                                goToNextRow = true;
                            }
                        }
                    }
                }

                if (!done) {
                    if (paint) {
                        g.drawChars(chars, pos, length, x, y);
                    }

                    if (strikethrough || underline || link) {
                        LineMetrics lm = fm.getLineMetrics(chars, pos, length - 1, g);
                        int lineWidth = new Double(x + r.getWidth()).intValue();

                        if (paint) {
                            if (strikethrough) {
                                int stPos = Math.round(lm.getStrikethroughOffset()) +
                                    g.getFont().getBaselineFor(chars[pos]) + 1;

                                //PENDING - worth supporting with g.setStroke()? A one pixel line is most likely
                                //good enough
                                //int stThick = Math.round (lm.getStrikethroughThickness());
                                g.drawLine(x, y + stPos, lineWidth, y + stPos);
                            }

                            if (underline || link) {
                                int stPos = Math.round(lm.getUnderlineOffset()) +
                                    g.getFont().getBaselineFor(chars[pos]) + 1;

                                //PENDING - worth supporting with g.setStroke()? A one pixel line is most likely
                                //good enough
                                //int stThick = new Float (lm.getUnderlineThickness()).intValue();
                                g.drawLine(x, y + stPos, lineWidth, y + stPos);
                            }
                        }
                    }
                    
                    if (goToNextRow) {
                        //if we're in word wrap mode and need to go to the next
                        //line, reconfigure the x and y coordinates
                        x = origX;
                        y += r.getHeight();
                        heightPainted += r.getHeight();
                        widthPainted = 0;
                        pos += (length);

                        //skip any leading whitespace
                        while ((pos < chars.length) && (Character.isWhitespace(chars[pos])) && (chars[pos] != '<')) {
                            pos++;
                        }

                        lastWasWhitespace = true;
                        done |= (pos >= chars.length);
                    } else {
                        x += r.getWidth();
                        widthPainted += r.getWidth();
                        lastWasWhitespace = Character.isWhitespace(chars[nextTag]);
                        pos = nextTag + 1;
                    }

                    done |= (nextTag == chars.length);
                }
            }
        }

        if (style != STYLE_WORDWRAP) {
            return widthPainted;
        } else {
            return heightPainted + lastHeight;
        }
    }

    /** Parse a font color tag and return an appropriate java.awt.Color instance */
    private static Color findColor(final char[] ch, final int pos, final int tagEnd) {
        int colorPos = pos;
        boolean useUIManager = false;

        for (int i = pos; i < tagEnd; i++) {
            if (ch[i] == 'c') {
                //#195703 - check for broken HTML
                if( i + 6 >= ch.length )
                    break;
                colorPos = i + 6;

                if ((ch[colorPos] == '\'') || (ch[colorPos] == '"')) {
                    colorPos++;
                }

                //skip the leading # character
                if (ch[colorPos] == '#') {
                    colorPos++;
                } else if (ch[colorPos] == '!') {
                    useUIManager = true;
                    colorPos++;
                }

                break;
            }
        }

        if (colorPos == pos) {
            String out = "Could not find color identifier in font declaration"; //NOI18N
            throwBadHTML(out, pos, ch);
        }

        //Okay, we're now on the first character of the hex color definition
        String s;

        if (useUIManager) {
            int end = ch.length - 1;

            for (int i = colorPos; i < ch.length; i++) {
                if ((ch[i] == '"') || (ch[i] == '\'')) { //NOI18N
                    end = i;

                    break;
                }
            }

            s = new String(ch, colorPos, end - colorPos);
        } else {
            s = new String(ch, colorPos, Math.min(ch.length-colorPos, 6));
        }

        Color result = null;

        if (useUIManager) {
            result = UIManager.getColor(s);

            //Not all look and feels will provide standard colors; handle it gracefully
            if (result == null) {
                throwBadHTML("Could not resolve logical font declared in HTML: " + s, //NOI18N
                    pos, ch
                );
                result = UIManager.getColor("textText"); //NOI18N

                //Avoid NPE in headless situation?
                if (result == null) {
                    result = Color.BLACK;
                }
            }
        } else {
            try {
                int rgb = Integer.parseInt(s, 16);
                result = new Color(rgb);
            } catch (NumberFormatException nfe) {
                throwBadHTML("Illegal hexadecimal color text: " + s + //NOI18N
                    " in HTML string", colorPos, ch
                ); //NOI18N
            }
        }

        if (result == null) {
            throwBadHTML("Unresolvable html color: " + s //NOI18N
                 +" in HTML string \n  ", pos, ch
            ); //NOI18N
        }

        return result;
    }

    /**
     * Workaround for Apple bug 3644261 - after using form editor, all boldface
     * fonts start showing up with incorrect metrics, such that all boldface
     * fonts in the entire IDE are displayed 12px below where they should be.
     * Embarrassing and awful.
     */
    private static final Font deriveFont(Font f, int style) {
        //      return f.deriveFont(style);
        // see #49973 for details.
        Font result = Utilities.isMac() ? new Font(f.getName(), style, f.getSize()) : f.deriveFont(style);

        return result;
    }

    /** Find an entity at the passed character position in the passed array.
     * If an entity is found, the trailing ; character will be substituted
     * with the resulting character, and the position of that character
     * in the array will be returned as the new position to render from,
     * causing the renderer to skip the intervening characters */
    private static final int substEntity(char[] ch, int pos) {
        //There are no 1 character entities, abort
        if (pos >= (ch.length - 2)) {
            return -1;
        }

        //if it's numeric, parse out the number
        if (ch[pos] == '#') { //NOI18N

            return substNumericEntity(ch, pos + 1);
        }

        //Okay, we've potentially got a named character entity. Try to find it.
        boolean match;

        for (int i = 0; i < entities.length; i++) {
            char[] c = (char[]) entities[i];
            match = true;

            if (c.length < (ch.length - pos)) {
                for (int j = 0; j < c.length; j++) {
                    match &= (c[j] == ch[j + pos]);
                }
            } else {
                match = false;
            }

            if (match) {
                //if it's a match, we still need the trailing ;
                if (ch[pos + c.length] == ';') { //NOI18N

                    //substitute the character referenced by the entity
                    ch[pos + c.length] = entitySubstitutions[i];

                    return pos + c.length;
                }
            }
        }

        return -1;
    }

    /** Finds a character defined as a numeric entity (e.g. &amp;#8222;)
     * and replaces the trailing ; with the referenced character, returning
     * the position of it so the renderer can continue from there.
     */
    private static final int substNumericEntity(char[] ch, int pos) {
        for (int i = pos; i < ch.length; i++) {
            if (ch[i] == ';') {
                try {
                    ch[i] = (char) Integer.parseInt(new String(ch, pos, i - pos));

                    return i;
                } catch (NumberFormatException nfe) {
                    throwBadHTML("Unparsable numeric entity: " + //NOI18N
                        new String(ch, pos, i - pos), pos, ch
                    ); //NOI18N
                }
            }
        }

        return -1;
    }

    /** Throw an exception for unsupported or bad html, indicating where the problem is
     * in the message  */
    private static void throwBadHTML(String msg, int pos, char[] chars) {
        char[] chh = new char[pos];
        Arrays.fill(chh, ' '); //NOI18N
        chh[pos - 1] = '^'; //NOI18N

        String out = msg + "\n  " + new String(chars) + "\n  " + new String(chh) + "\n Full HTML string:" +
            new String(chars); //NOI18N

        if (!STRICT_HTML) {
            if (LOG.isLoggable(Level.WARNING)) {
                if (badStrings == null) {
                    badStrings = new HashSet<String>();
                }

                if (!badStrings.contains(msg)) {
                    // bug, issue 38372 - log messages containing
                    //newlines are truncated - so for now we iterate the
                    //string we've just constructed
                    StringTokenizer tk = new StringTokenizer(out, "\n", false);

                    while (tk.hasMoreTokens()) {
                        LOG.warning(tk.nextToken());
                    }

                    badStrings.add(msg.intern());   // NOPMD
                }
            }
        } else {
            throw new IllegalArgumentException(out);
        }
    }

    /**
     * Interface aggregating table, tree, and list cell renderers.
     * @see #createRenderer
     * @see #createLabel
     */
    public interface Renderer extends TableCellRenderer, TreeCellRenderer, ListCellRenderer {
        /** Indicate that the component being rendered has keyboard focus.  NetBeans requires that a different
         * selection color be used depending on whether the view has focus.
         *
         * @param parentFocused Whether or not the focused selection color should be used
         */
        void setParentFocused(boolean parentFocused);

        /**
         * Indicate that the text should be painted centered below the icon.  This is primarily used
         * by org.openide.explorer.view.IconView
         *
         * @param centered Whether or not centered painting should be used.
         */
        void setCentered(boolean centered);

        /**
         * Set a number of pixels the icon and text should be indented.  Used by ChoiceView and ListView to
         * fake tree-style nesting.  This value has no effect if {@link #setCentered setCentered(true)} has been called.
         *
         * @param pixels The number of pixels to indent
         */
        void setIndent(int pixels);

        /**
         * Explicitly tell the renderer it is going to receive HTML markup, or it is not.  If the renderer should
         * check the string for opening HTML tags to determine this, don't call this method.  If you <strong>know</strong>
         * the string will be compliant HTML, it is preferable to call this method with true; if you want to intentionally
         * render HTML markup literally, call this method with false.
         *
         * @param val
         */
        void setHtml(boolean val);

        /**
         * Set the rendering style - this can be JLabel-style truncated-with-ellipsis (...) text, or clipped text.
         * The default is {@link #STYLE_CLIP}.
         *
         * @param style The text style
         */
        void setRenderStyle(int style);

        /** Set the icon to be used for painting
         *
         * @param icon An icon or null
         */
        void setIcon(Icon icon);

        /** Clear any stale data from previous use by other components,
         * clearing the icon, text, disabled state and other customizations, returning the component
         * to its initialized state.  This is done automatically when get*CellRenderer() is called,
         * and to the shared instance when {@link #createLabel} is called.<p>
         * Users of the static {@link #createLabel} method may want to call this method if they
         * use the returned instance more than once without again calling {@link #createLabel}.
         */
        void reset();

        /** Set the text to be displayed.  Use this if the object being rendered's toString() does not
         * return a real user-displayable string, after calling get**CellRenderer().  Typically after calling
         * this one calls {@link #setHtml} if the text is known to either be or not be HTML markup.
         *
         * @param txt The text that should be displayed
         */
        void setText(String txt);

        /**
         * Convenience method to set the gap between the icon and text.
         *
         * @param gap an integer number of pixels
         */
        void setIconTextGap(int gap);
    }
    
}
