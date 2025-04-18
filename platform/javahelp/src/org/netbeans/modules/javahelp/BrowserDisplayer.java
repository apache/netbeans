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

package org.netbeans.modules.javahelp;

import com.sun.java.help.impl.ViewAwareComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.View;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JPopupMenu;
import org.netbeans.modules.javahelp.CopyLinkLocationAction.LinkOwner;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * This class is a lightweight component to be included in HTML content within
 * JHContentViewer. It invokes default IDE html browser to show external URL.
 * (Default browser should be external browser to show external URL properly.
 * Component is displayed as a mouse enabled Label. Only text is supported.
 * <p>
 * To use this class within HTML content use the &ltobject&gt tag. Below is an
 * example usage:
 * <p><pre>
 * &ltobject CLASSID="java:org.netbeans.module.javahelp.BrowserDisplayer"&gt
 * &ltparam name="content" value="http://www.netbeans.org"&gt
 * &ltparam name="text" value="Click here"&gt
 * &ltparam name="textFontFamily" value="SansSerif"&gt
 * &ltparam name="textFontSize" value="x-large"&gt
 * &ltparam name="textFontWeight" value="plain"&gt
 * &ltparam name="textFontStyle" value="italic"&gt
 * &ltparam name="textColor" value="red"&gt
 * &lt/object&gt
 * </pre><p>
 * Valid parameters are:
 * <ul>
 * <li>content - a valid external url like http://java.sun.com
 * @see setContent
 * <li>text - the text of the activator
 * @see setText
 * <li>textFontFamily - the font family of the activator text
 * @see setTextFontFamily
 * <li>textFontSize - the size of the activator text font. Size is specified
 * in a css terminology. See the setTextFontSize for acceptable syntax.
 * @see setTextFontSize
 * <li>textFontWeight - the activator text font weight
 * @see setTextFontWeight
 * <li>textFontStyle - the activator text font style
 * @see setTextFontStyle
 * <li>textColor - the activator text color
 * @see setTextColor
 * <ul>
 *
 * @author Marek Slama
 */
public class BrowserDisplayer extends JButton 
        implements ActionListener, ViewAwareComponent, LinkOwner {
    private View myView;
    private SimpleAttributeSet textAttribs;
    private HTMLDocument doc;
    private URL base;

    private static final Cursor handCursor =
	Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private Cursor origCursor;

    private JPopupMenu popupMenu;

    /**
     * Create a secondaryviewer. By default the viewer creates a button with
     * the text of ">"
     */
    public BrowserDisplayer() {
    	super();
        setMargin(new Insets(0,0,0,0));
        createLinkLabel();
        addActionListener(this);
        origCursor = getCursor();
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(BrowserDisplayer.class,"ACSD_Label"));
        this.popupMenu =
                HyperlinkEventProcessor.getPopupMenu(
                                              new CopyLinkLocationAction(this));
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                setToolTipText(null);
                if (Utils.isMouseRightClick(e)) {
                    Utils.showPopupMenu(e, popupMenu, BrowserDisplayer.this);
                }
            }

            public void mouseEntered(MouseEvent e) {
                setCursor(handCursor);
                setToolTipText(getContent());
            }

            public void mouseExited(MouseEvent e) {
                setCursor(origCursor);
                setToolTipText(null);
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }
        });
    }

    /**
     * Sets data optained from the View
     */
    public void setViewData(View v) {
	myView = v;
	doc = (HTMLDocument) myView.getDocument();
	base = doc.getBase();

	// Set the current font information in the local text attributes
	Font font = getFont();
	textAttribs = new SimpleAttributeSet();
	textAttribs.removeAttribute(StyleConstants.FontSize);
	textAttribs.removeAttribute(StyleConstants.Bold);
	textAttribs.removeAttribute(StyleConstants.Italic);
	textAttribs.addAttribute(StyleConstants.FontFamily,
				 font.getName());
	textAttribs.addAttribute(StyleConstants.FontSize,
				 new Integer(font.getSize()));
	textAttribs.addAttribute(StyleConstants.Bold,
				 Boolean.valueOf(font.isBold()));
	textAttribs.addAttribute(StyleConstants.Italic,
				 Boolean.valueOf(font.isItalic()));
    }
    
    /**
     *  properties
     */
    private String content = "";

    /**
     * Set the content for the secondary viewer
     * @param content a valid URL
     */
    public void setContent(String content) {
	this.content = content;
    }

    /**
     * Returns the content of the secondary viewer
     */
    public String getContent() {
	return content;
    }

    /**
     * Creates a link label. A link label is a form of a JButton but without a
     * button like appearance.
     */
    private void createLinkLabel() {
        setBorder(new EmptyBorder(1,1,1,1));
        setBorderPainted(false);
        setFocusPainted(false);
        setAlignmentY(getPreferredAlignmentY());
        setContentAreaFilled(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBackground(UIManager.getColor("EditorPane.background"));
        if (textAttribs != null &&
            textAttribs.isDefined(StyleConstants.Foreground)) {
            setForeground((Color)textAttribs.getAttribute(StyleConstants.Foreground));
        } else {
            setForeground(Color.blue);
        }
        invalidate();
    }

    /**
     * Determine the alignment offset so the text is aligned with other views
     * correctly.
     */
    private float getPreferredAlignmentY() {
        // Fix for the Issue #68316
        Font font = getFont();
        FontMetrics fm = getToolkit().getFontMetrics(font);
        float h = fm.getHeight();
        float d = fm.getDescent();
        return (h - d) / h;

// Original implementation below is commented out due to Issue #68316.
// It tries also take into account a size of the icon, but according to javadoc
// of the class "Only text is supported".
// Original implementation is commented out below due to the bug #68316.
// It tries also to take into account a size of the icon, but according to
// javadoc of the class "Only text is supported".
//
//        Icon icon = (Icon)getIcon();
//        String text = getText();
//
//        Font font = getFont();
//        FontMetrics fm = getToolkit().getFontMetrics(font);
//
//        Rectangle iconR = new Rectangle();
//        Rectangle textR = new Rectangle();
//        Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);
//
//        SwingUtilities.layoutCompoundLabel(
//            this, fm, text, icon,
//            getVerticalAlignment(), getHorizontalAlignment(),
//            getVerticalTextPosition(), getHorizontalTextPosition(),
//            viewR, iconR, textR,
//	    (text == null ? 0 : ((BasicButtonUI)ui).getDefaultTextIconGap(this))
//        );
//
//        // The preferred size of the button is the size of
//        // the text and icon rectangles plus the buttons insets.
//        Rectangle r = iconR.union(textR);
//
//        Insets insets = getInsets();
//        r.height += insets.top + insets.bottom;
//
//        // Ensure that the height of the button is odd,
//        // to allow for the focus line.
//        if(r.height % 2 == 0) {
//            r.height += 1;
//        }
//
//        float offAmt = fm.getMaxAscent() + insets.top;
//        return offAmt/(float)r.height;
    }
    
    /**
     * Sets the text Font family for the activator text.
     * For JDK 1.1 this must a family name of Dialog, DialogInput, Monospaced, 
     * Serif, SansSerif, or Symbol.
     */
    public void setTextFontFamily(String family) {
	textAttribs.removeAttribute(StyleConstants.FontFamily);
	textAttribs.addAttribute(StyleConstants.FontFamily, family);
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }

    /**
     * Returns the text Font family name of the activator text
     */
    public String getTextFontFamily() {
	return StyleConstants.getFontFamily(textAttribs);
    }

    /**
     * Sets the text size for the activator text.
     * The String size is a valid Cascading Style Sheet value for
     * text size. Acceptable values are as follows:
     * <ul>
     * <li>xx-small
     * <li>x-small
     * <li>small
     * <li>medium
     * <li>large
     * <li>x-large
     * <li>xx-large
     * <li>bigger - increase the current base font size by 1
     * <li>smaller - decrease the current base font size by 1
     * <li>xxpt - set the font size to a specific pt value of "xx"
     * <li>+x - increase the current base font size by a value of "x"
     * <li>-x - decrease the current base font size by a value of "x"
     * <li>x - set the font size to the point size associated with 
     * the index "x"
     * </ul>
     */
    public void setTextFontSize(String size) {
	int newsize;
	StyleSheet css = doc.getStyleSheet();
	try {
	    if (size.equals("xx-small")) {
		newsize = (int)css.getPointSize(0);
	    } else if (size.equals("x-small")) {
		newsize = (int)css.getPointSize(1);
	    } else if (size.equals("small")) {
		newsize = (int)css.getPointSize(2);
	    } else if (size.equals("medium")) {
		newsize = (int)css.getPointSize(3);
	    } else if (size.equals("large")) {
		newsize = (int)css.getPointSize(4);
	    } else if (size.equals("x-large")) {
		newsize = (int)css.getPointSize(5);
	    } else if (size.equals("xx-large")) {
		newsize = (int)css.getPointSize(6);
	    } else if (size.equals("bigger")) {
		newsize = (int)css.getPointSize("+1");
	    } else if (size.equals("smaller")) {
		newsize = (int)css.getPointSize("-1");
	    } else if (size.endsWith("pt")) {
		String sz = size.substring(0, size.length() - 2);
		newsize = Integer.parseInt(sz);
	    } else {
		newsize = (int) css.getPointSize(size);
	    }
	} catch (NumberFormatException nfe) {
	    return;
	}
	if (newsize == 0) {
	    return;
	}
	textAttribs.removeAttribute(StyleConstants.FontSize);
	textAttribs.addAttribute(StyleConstants.FontSize,
				 new Integer(newsize));
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }
    
    /**
     * Returns the text Font family name of the activator text
     */
    public String getTextFontSize() {
	return Integer.toString(StyleConstants.getFontSize(textAttribs));
    }

    /**
     * Sets the text Font Weigth for the activator text.
     * Valid weights are
     * <ul>
     * <li>plain
     * <li>bold
     * </ul>
     */
    public void setTextFontWeight(String weight) {
	boolean isBold=false;
	if ("bold".equals(weight)) {
	    isBold = true;
	} else {
	    isBold = false;
	}
	textAttribs.removeAttribute(StyleConstants.Bold);
	textAttribs.addAttribute(StyleConstants.Bold, Boolean.valueOf(isBold));
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }

    /**
     * Returns the text Font weight of the activator text
     */
    public String getTextFontWeight() {
	if (StyleConstants.isBold(textAttribs)) {
	    return "bold";
	}
	return "plain";
    }

    /**
     * Sets the text Font Style for the activator text.
     * Valid font styles are
     * <ul>
     * <li>plain
     * <li>italic
     * </ul>
     */
    public void setTextFontStyle(String style) {
	boolean isItalic=false;
	if ("italic".equals(style)) {
	    isItalic = true;
	} else {
	    isItalic = false;
	}
	textAttribs.removeAttribute(StyleConstants.Italic);
	textAttribs.addAttribute(StyleConstants.Italic, Boolean.valueOf(isItalic));
	setFont(getAttributeSetFont(textAttribs));
	Font font = getFont();
    }

    /**
     * Returns the text Font style of the activator text
     */
    public String getTextFontStyle() {
	if (StyleConstants.isItalic(textAttribs)) {
	    return "italic";
	}
	return "plain";
    }

    /**
     * Sets the text Color for the activator text.
     * The following is a list of supported Color names
     * <ul>
     * <li>black
     * <li>blue
     * <li>cyan
     * <li>darkGray
     * <li>gray
     * <li>green
     * <li>lightGray
     * <li>magenta
     * <li>orange
     * <li>pink
     * <li>red
     * <li>white
     * <li>yellow
     * </ul>
     */
    public void setTextColor(String name) {
	Color color=null;
	if ("black".equals(name)) {
	    color = Color.black;
	} else if ("blue".equals(name)) {
	    color = Color.blue;
	} else if ("cyan".equals(name)) {
	    color = Color.cyan;
	} else if ("darkGray".equals(name)) {
	    color = Color.darkGray;
	} else if ("gray".equals(name)) {
	    color = Color.gray;
	} else if ("green".equals(name)) {
	    color = Color.green;
	} else if ("lightGray".equals(name)) {
	    color = Color.lightGray;
	} else if ("magenta".equals(name)) {
	    color = Color.magenta;
	} else if ("orange".equals(name)) {
	    color = Color.orange;
	} else if ("pink".equals(name)) {
	    color = Color.pink;
	} else if ("red".equals(name)) {
	    color = Color.red;
	} else if ("white".equals(name)) {
	    color = Color.white;
	} else if ("yellow".equals(name)) {
	    color = Color.yellow;
	}

	if (color == null) {
	    return;
	}
	textAttribs.removeAttribute(StyleConstants.Foreground);
	textAttribs.addAttribute(StyleConstants.Foreground, color);
	setForeground(color);
    }

    /**
     * Returns the text Color of the activator text
     */
    public String getTextColor() {
	Color color = getForeground();
	return color.toString();
    }

    /**
     * Gets the font from an attribute set.  This is
     * implemented to try and fetch a cached font
     * for the given AttributeSet, and if that fails 
     * the font features are resolved and the
     * font is fetched from the low-level font cache.
     * Font's are cached in the StyleSheet of a document
     *
     * @param attr the attribute set
     * @return the font
     */
    private Font getAttributeSetFont(AttributeSet attr) {
        // PENDING(prinz) add cache behavior
        int style = Font.PLAIN;
        if (StyleConstants.isBold(attr)) {
            style |= Font.BOLD;
        }
        if (StyleConstants.isItalic(attr)) {
            style |= Font.ITALIC;
        }
        String family = StyleConstants.getFontFamily(attr);
        int size = StyleConstants.getFontSize(attr);

	/**
	 * if either superscript or subscript is
	 * is set, we need to reduce the font size
	 * by 2.
	 */
	if (StyleConstants.isSuperscript(attr) ||
	    StyleConstants.isSubscript(attr)) {
	    size -= 2;
	}

	// fonts are cached in the StyleSheet so use that
        return doc.getStyleSheet().getFont(family, style, size);
    }

    /**
     * Displays the viewer according to the viewerType
     */
    public void actionPerformed(ActionEvent e) {
        URL link;
        try {
            link = new URL(content);
        } catch (MalformedURLException exc) {
            //XXX log something to ide.log??
            return;
        }
        HtmlBrowser.URLDisplayer.getDefault().showURL(link);
    }

    public String getURLExternalForm() {
        return getContent();
    }

    @Override
    public Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);

        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        return c;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // do nothing
    }

}
