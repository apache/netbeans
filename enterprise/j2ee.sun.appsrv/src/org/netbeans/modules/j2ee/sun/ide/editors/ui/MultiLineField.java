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
/*
 * MultiLineField.java -- synopsis.
 *
 *
 */
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.awt.*;
import java.awt.font.*;
import java.text.*;
import java.util.*;
import javax.accessibility.*;
import javax.swing.*;

/**
 * A specialized JComponent for displaying Method declarations.
 *
 * @author Joe Warzecha
 */

public class MultiLineField extends JComponent implements Accessible {

    /**
     * The number of characters to display per line.
     */
    private static final int	CHARS_DISPLAYED = 80;

    /**
     * The number of lines of text to display.
     */
    private static final int	LINES_DISPLAYED = 4;

    /**
     * The default amount of space between lines of text.
     */ 
    private static final int	LINE_SPACING = 2;

    /**
     * The default line width.
     */
    private static final int	DEFAULT_WIDTH = 400;

    /**
     * The minimum line width.
     */
    private static final int	MIN_WIDTH = 50;

    /**
     * A blank space
     */
    private static final String  SPACE = " ";	// NOI18N

    /**
     * The default insets for this component.
     */
    private static final Insets INSETS = new Insets (0, 5, 5, 5);

    /**
     * The color to display errors in.
     */
    protected static final Color ERROR_COLOR = Color.red;

    private String []		stringsToDisplay;
    private AttributedString []	attributedStrings;
    private Object		lockObject;

    protected FontMetrics	fontMetrics;
    protected int		totalWidth;
    protected Dimension 	preferredSize;

    private Insets		insets;
    private int			numberLines;
    private int			lineHeight;
    private int			charHeight;
    private int			lineSpacing;
    private boolean		useMonospacedFont;

    private boolean		formatText;

    public MultiLineField () {
	super ();
	lockObject = new Object ();
	setText(new String[0]);
	init (LINES_DISPLAYED);
    }

    public MultiLineField (String text) {
        super ();

	lockObject = new Object ();
	setText (text);
	init (stringsToDisplay.length);
    }

    public MultiLineField (String text, boolean format) {
        this (text);

	formatText = format;
    }

    public MultiLineField (String [] strs) {
	super ();

	lockObject = new Object ();
	setText (strs);
	init (stringsToDisplay.length);
    }

    private void init (int nLines) {
	formatText = false;
	insets = INSETS;
	numberLines = nLines;
	useMonospacedFont = false;
	
	/* Initiliaze the minimum/preferred size to something */
	preferredSize = new Dimension (100, 20);
    }

    public void setInsets (Insets i) {
	insets = i;
    }

    public void useMonospacedFont (boolean b) {
	useMonospacedFont = b;
    }

    public void addNotify () {
	super.addNotify ();
	
	int ptSize = getFont ().getSize ();
	if (useMonospacedFont) {
	    Font font = new Font ("Monospaced", Font.PLAIN, ptSize);	//NOI18N
	    setFont (font);
	}
	fontMetrics = getFontMetrics (getFont ());

	/* 
	 * If we know the strings, then assume that we just want
	 * them displayed as is and figure out the preferred size 
	 */

	int width = 0;
	if (stringsToDisplay == null) {
	    /* Default size is 4 lines high, 80 characters wide */
	    width = fontMetrics.charWidth ('v') * CHARS_DISPLAYED;
	} else {
	    if (formatText) {
		width = DEFAULT_WIDTH;
	    } else {
	        for (int i = 0; i < stringsToDisplay.length; i++) {
		    int lineWidth = fontMetrics.stringWidth 
							(stringsToDisplay [i]);
		    if (lineWidth > width) {
		        width = lineWidth;
		    }
		}
	    }	
	}
	
	charHeight = fontMetrics.getMaxAscent () + fontMetrics.getMaxDescent ();
	
	/* 
	 * If the  ptSize is greater than 12, then for each 4 pts,
	 * we add one more space between lines.
	 */
	lineSpacing = 0;
	if (ptSize > 12) {
	    lineSpacing = LINE_SPACING + ((ptSize - 4) / 4);
	} else {
	    lineSpacing = LINE_SPACING;
     	}
	
	totalWidth = insets.left + insets.right + width; 
	lineHeight = charHeight + lineSpacing;
	if (formatText) {
	    reformat ();
	} 

	int totalHeight = insets.top + insets.bottom + 
			  (charHeight * numberLines) +
		          (lineSpacing * (numberLines - 1));

	preferredSize = new Dimension (totalWidth, totalHeight);
    }

    private void reformat () {
	if ((stringsToDisplay == null) || (stringsToDisplay.length < 1)) {
	    return;
	}

	String entireString = stringsToDisplay [0];
	for (int i = 1; i < stringsToDisplay.length; i++) {
	    entireString = entireString.concat (SPACE + stringsToDisplay [i]);
	}

	reformat (entireString);
    }

    private void reformat (String s) {
	if (totalWidth < MIN_WIDTH) {
	    return;
	}

	int lineWidth = totalWidth - (insets.left + insets.right);

	StringTokenizer st = new StringTokenizer (s, SPACE);
	Vector v = new Vector ();
	String prevString = st.nextToken ();
	int curWidth;
	String curString;
	String curToken;
	
	while (st.hasMoreTokens ()) {
	    curToken = st.nextToken ();
	    curString = prevString + SPACE + curToken;
	    curWidth = fontMetrics.stringWidth (curString);
	    if (curWidth > lineWidth) {
		v.addElement (prevString);
		prevString = curToken;
	    } else {
		prevString = curString;
	    }
	}

	v.addElement (prevString);

	numberLines = v.size ();
	stringsToDisplay = new String [numberLines];
	v.copyInto (stringsToDisplay);
    }
 
    public Dimension getPreferredSize () {
	return preferredSize;
    }

    public Dimension getMinimumSize () {
	return preferredSize;
    }

    public void setSize (Dimension d) {
	if (d.width != totalWidth) {
	    totalWidth = d.width;
	    if (formatText) {
		reformat ();
	    }
	}

	super.setSize (d);
    }

    public void setBounds (Rectangle r) {
	if (r.width != totalWidth) {
	    totalWidth = r.width;
	    if (formatText) {
		reformat ();
	    }
	}

	super.setBounds (r);
    }

    public void setBounds (int x, int y, int width, int height) {
	if (width != totalWidth) {
	    totalWidth = width;
	    if (formatText) {
		reformat ();
	    }
	}

	super.setBounds (x, y, width, height);
    }

    public void paint (Graphics g) {
	Dimension size = getSize ();
	if (size.width != totalWidth) {
	    totalWidth = size.width;
	}
	
	g.clearRect (0, 0, size.width, size.height);

	/* 
	 * If number of lines of text to display is less than the
	 * total number specified, see if it should start being
	 * displayed on a line other than line 1.
	 */
	synchronized (lockObject) {
	    int offset = insets.top + fontMetrics.getMaxAscent ();
	    int blankLines = (numberLines - stringsToDisplay.length) / 2;
	    if (blankLines > 0) {
		offset += (lineHeight * blankLines);
	    }

	    for (int i = 0; i < stringsToDisplay.length; i++) {
		if (attributedStrings != null) {
		    attributedStrings [i].addAttribute (TextAttribute.FONT,
							getFont ());	
		    g.drawString (attributedStrings [i].getIterator (),
				  insets.left, offset);
		} else {
		    g.drawString (stringsToDisplay [i], insets.left, offset);
		}
		offset += lineHeight;
	    }
	}
    }

    public void setText (AttributedString [] strs) {
	synchronized (lockObject) {
	    attributedStrings = strs;
	}
	repaint();
    }

    public void setText (String [] strs) {
	synchronized (lockObject) {
	    stringsToDisplay = strs;
	}
	repaint();
    }

    public String [] getText () {
	String [] retStrings = null;
	synchronized (lockObject) {
	    if ((stringsToDisplay != null) && (stringsToDisplay.length > 0)) {
	        retStrings = new String [stringsToDisplay.length];
	        System.arraycopy (stringsToDisplay, 0, retStrings, 0, 
			          stringsToDisplay.length);
	    }
	}

	return retStrings;
    }
	
    public void setText (String s) {
	synchronized (lockObject) {
	    attributedStrings = null;
	    if (s == null) {
		stringsToDisplay = new String [1];
		stringsToDisplay [0] = SPACE;
	    } else {
		if (formatText) {
		    if (fontMetrics != null) {
			reformat (s);
		    } else {
			stringsToDisplay = new String [1];
			stringsToDisplay [0] = s;
		    }
		} else {
	            StringTokenizer st = new StringTokenizer (s, "\n");	// NOI18N
	            int nTokens = st.countTokens ();
	            stringsToDisplay = new String [nTokens];
	            for (int i = 0; i < nTokens; i++) {
		        stringsToDisplay [i] = st.nextToken ();
		    }
		}
	    }
	}
    }

    public void setError (boolean val) {
	if (val) {
	    if (stringsToDisplay == null) {
		return;
	    }
	    attributedStrings = new AttributedString [stringsToDisplay.length];
	    for (int i = 0; i < stringsToDisplay.length; i++) {
		attributedStrings [i] = 
				new AttributedString (stringsToDisplay [i]); 
		attributedStrings [i].addAttribute (TextAttribute.FOREGROUND,
						    ERROR_COLOR);
	    }
	} else {
	    attributedStrings = null;
	}
    }

    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleMultiLineField();
        }
        return accessibleContext;
    }

    protected class AccessibleMultiLineField extends AccessibleJComponent { 

	public AccessibleRole getAccessibleRole () {
	    return AccessibleRole.LABEL;
	}
    }
}
