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
 * MessageArea.java -- synopsis.
 *
 */
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * A JEditorPane used for displaying messages in html format.
 *
 * @author Joe Warzecha
 */

public class MessageArea extends JLabel {

    private static final int WIDTH = 600;
    private static final int [] sizeMap = {8, 10, 12, 14, 18, 24, 36};
    private static final char [] hexVals = {'A', 'B', 'C', 'D', 'E', 'F'};

    private int		width;
    private String	fontString;
    private boolean	isBold;
    private boolean 	isItalic;

    private String 	msgs;
    private String	msgString;
    private String 	endMsgs;
    private String	endMsgString;

    private Vector	bulletItems; 
    private String 	bulletItemString;

    public MessageArea () {
	super ();
	bulletItems = new Vector ();
	width = WIDTH;
    }

    public MessageArea (String text) {
	this ();
	setText (text);
    }
	
    public void addNotify () {
	super.addNotify ();

	UIDefaults defs = UIManager.getDefaults ();
	Color c = defs.getColor ("OptionPane.background");// NOI18N
	if (c != null) {
	    setBackground (c);
	}

	c = defs.getColor ("OptionPane.foreground");	// NOI18N
	if (c != null) {
	    setForeground (c);
	}

	if (fontString == null) {
	    makeFontString (getFont (), getForeground ());
	}
    }

    private void makeEntireMsg () {
	StringBuffer sbuf = new StringBuffer 
				("<html><table width=\"" + width + "\"><tr>");	// NOI18N
	if (fontString != null) {
	    sbuf.append (fontString);
	}
	
	if (msgString != null) {
	    sbuf.append (msgString);
	}

	if (bulletItemString != null) {
	    sbuf.append (bulletItemString);
	}

	if (endMsgString != null) {
	    sbuf.append (endMsgString);
	}

	if (isItalic) {
	    sbuf.append ("</i>");	// NOI18N
	}

	if (isBold) {
	    sbuf.append ("</b>");	// NOI18N
	}

	if (fontString != null) {
	    sbuf.append ("</font>");	// NOI18N
	}

	sbuf.append ("</tr></table></html>");	// NOI18N
	super.setText (sbuf.toString ());
    }

    private void makeMsgString () {
	if ((msgs ==  null) || (msgs.length () < 1)) {
	    msgString = null;
	}
	//msgString = "<p>" + msgs + "</p>";
	msgString = msgs;
	makeEntireMsg ();
    }

    private void makeEndMsgString () {
	if ((endMsgs ==  null) || (endMsgs.length () < 1)) {
	    endMsgString = null;
	}
	//endMsgString = "<p>" + endMsgs + "</p>";
	endMsgString = endMsgs;
	makeEntireMsg ();
    }

    private String toHex (int i) {
	StringBuffer sbuf = new StringBuffer ();
        int val = i / 16;
        if (val > 10) {   
            sbuf.append (hexVals [val - 10]);     
        } else {
            sbuf.append (val);
        }
 
        val = i % 16;
        if (val > 10) {
            sbuf.append (hexVals [val - 10]);
        } else {
            sbuf.append (val);
        }

        return sbuf.toString ();
    }
 
    private String indexToOffset (int idx) {
        int middle = sizeMap.length / 2;

        if (idx < middle) {
            return ("-" + (middle-idx));			//NOI18N
        }

        return ("+" + (idx - middle));				//NOI18N
    }

    private String closestSize (int ptSize) {
	if (ptSize < sizeMap [0]) {
	   return indexToOffset (0);
	}

	if (ptSize >= sizeMap [sizeMap.length - 1]) {
	    return indexToOffset (sizeMap.length - 1);
	}

	for (int i = 0; i < sizeMap.length - 1; i++) {
	    if (ptSize == sizeMap [i]) {
		return indexToOffset (i);
	    } 

 	    if ((ptSize > sizeMap [i]) && (ptSize < sizeMap [i + 1])) {
		int diff1 = ptSize - sizeMap [i];
		int diff2 = sizeMap [i + 1] - ptSize;
		if (diff1 < diff2) {
		    return indexToOffset (i);
		}

		return indexToOffset (i + 1);
	    }
	}

	/* How'd we get here?  return middle value */
	return indexToOffset (sizeMap.length / 2);
    }

    private void makeFontString (Font f, Color c) {	// BEGIN_NOI18N
	StringBuffer sbuf = new StringBuffer ("<font");
	if (f != null) {
	    sbuf.append (" face=");
	    sbuf.append (f.getName ());
	    sbuf.append (" size=");
	    sbuf.append (closestSize (f.getSize ()));
	}

	if (c != null) {
	    sbuf.append (" color=\"#");
	    sbuf.append (toHex (c.getRed ()));
	    sbuf.append (toHex (c.getGreen ()));
	    sbuf.append (toHex (c.getBlue ()));
	    sbuf.append ("\"");
	}
	
	sbuf.append (">");
	if (f != null) {
	    if ((f.getStyle () & Font.BOLD) == Font.BOLD) {
		sbuf.append ("<b>");
		isBold = true;
	    }
		
  	    if ((f.getStyle () & Font.ITALIC) == Font.ITALIC) {
		sbuf.append ("<i>");
		isItalic = true;
	    }
	}

	fontString = sbuf.toString ();
	makeEntireMsg ();
    }							// END_NOI18N

    private void makeBulletItems () {
	if (bulletItems.isEmpty ()) {
	    bulletItemString = null;
	}

	StringBuffer sbuf = new StringBuffer ();
	sbuf.append ("<ul>");	// NOI18N
	for (int i = 0; i < bulletItems.size (); i++) {
	    String s = (String) bulletItems.elementAt (i);
	    sbuf.append ("<li>" + s + "</li>");	// NOI18N
	}
	
	sbuf.append ("</ul>");	// NOI18N
	bulletItemString = sbuf.toString ();
	makeEntireMsg ();
    }

    public void setWidth (int wid) {
	width = wid;
	makeEntireMsg ();
    }

    public void setFont (Font f) {
	super.setFont (f);
	makeFontString (f, getForeground ());
    }

    public void setForeground (Color c) {
	super.setForeground (c);
	makeFontString (getFont (), c);
    }

    private void tokenizeString (String s, Vector v) {
	StringTokenizer st = new StringTokenizer (s, "\n");	// NOI18N
	if (st.countTokens () > 1) {
	    while (st.hasMoreTokens ()) {
		v.add (st.nextToken ());
	    }
	} else {
	    v.add (s);
	}
    }

    public void setText (String s) {
	msgs = s.replace ('\n', ' ');
	makeMsgString ();
    }

    public void setEndText (String s) {
	endMsgs = s.replace ('\n', ' ');
	makeEndMsgString ();
    }

    public void appendText (String s) {
	String noNewLine = s.replace ('\n', ' ');
	msgs = msgs.concat (noNewLine);
	makeMsgString ();
    }

    public void setBulletItems (String s) {
	bulletItems.clear ();
	tokenizeString (s, bulletItems);
	makeBulletItems ();
    }

    public void setBulletItems (java.util.List l) {
	bulletItems.clear ();
	bulletItems.addAll (l);
	makeBulletItems ();
    }

    public void setBulletItems (String [] s) {
	bulletItems.clear ();
	for (int i = 0; i < s.length; i++) {
	    bulletItems.add (s [i]);
	}
	makeBulletItems ();
    }

    public void appendBulletItem (String s) {
	tokenizeString (s, bulletItems);
	makeBulletItems ();
    }

    public void appendBulletItems (String [] s) {
	for (int i = 0; i < s.length; i++) {
	    bulletItems.add (s [i]);
	}
	makeBulletItems ();
    }

    public void appendBulletItems (java.util.List l) {
	bulletItems.addAll (l);
	makeBulletItems ();
    }
}
