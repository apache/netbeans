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
 * "Attr.java"
 * Attr.java 1.7 01/07/23
 */

package org.netbeans.lib.terminalemulator;

@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
enum Attr {
    BGCOLOR(9),		// encodes 0 or palette index+1 (was: 5)
    FGCOLOR(9),		// encodes 0 or palette index+1 (was: 5)
    HIDDEN(1),
    REVERSE(1),
    BLINK(1),
    UNDERSCORE(1),
    BRIGHT(1),
    DIM(1),
    ACTIVE(1),

    // Since an attr value of 0 means render using default attributes
    // We need a value that signifies that no attribute has been set.
    // Can't use the highest (sign) bit since Java has no unsigned and
    // we get complaints from the compiler.
    UNSET(30, 1);

    public static final int ALT;

    //
    // Bases of various color ranges.
    // These are indexes into Term.palette
    //
    // First 8 colors
    public static final int PAL_ANSI = 0;

    // Second "bright" 8 colors
    public static final int PAL_BRIGHT = 8;

    // 6x6x6 RGB cube (number = 16 + 36 * r + 6 * g + b)
    // where r, g and b are in the range 0-5
    public static final int PAL_RGB = 16;

    // 24 shades of grey from dark to light
    public static final int PAL_GREY = 232;

    // Default foreground color
    public static final int PAL_FG = 256;
    // Default background color
    public static final int PAL_BG = 257;
    // Default bold color
    public static final int PAL_BOLD = 258;

    public static final int PAL_SIZE = 259;

    private final int width;	// ... of field
    private final int fmask;	// ... corresponding to width

    private int offset;		// ... of field from the "left"
    private int wmask;		// ... over the word. 1 where bits are set.

    static {
	// System.out.printf("Attr.static()\n");
	for (Attr a : values())
	    a.init();
	ALT = Attr.BGCOLOR.wmask | Attr.FGCOLOR.wmask | Attr.REVERSE.wmask | Attr.ACTIVE.wmask;
    }

    /*
     * Explicitly set the offset.
     */
    private Attr(int offset, int width) {
	// System.out.printf("Attr(%d, %d) -> %d\n", offset, width, ordinal());
	this.offset = offset;
	this.width = width;
	this.fmask = (1<<width)-1;
    }

    /*
     * Automatically set the offset.
     */
    private Attr(int width) {
	// System.out.printf("Attr(%d) -> %d\n", width, ordinal());
	this.offset = -1;
	this.width = width;
	this.fmask = (1<<width)-1;
    }

    private void init() {
	if (this.offset == -1) {
	    if (ordinal() == 0) {
		this.offset = 0;
	    } else {
		Attr prev = Attr.values()[ordinal()-1];
		offset = prev.offset + prev.width;
	    }
	}
	wmask = fmask << offset;
	// System.out.printf("%s\n", this);
    }

    @Override
    public String toString() {
	return String.format("%d %10s(%2d, %2d, 0x%02x %8s, 0x%08x %32s)", // NOI18N
		             ordinal(), name(),
			     offset, width,
			     fmask, Integer.toBinaryString(fmask),
			     wmask, Integer.toBinaryString(wmask) );
    }

    public static String toString(int attr) {
	return String.format("%32s", Integer.toBinaryString(attr)).replace(" ", "0"); // NOI18N

    }

    public final int get(int attr) {
	return (attr >> offset) & fmask;
    }

    /*
     * Use for setting a wide field.
     * Works for 1-bit field but set(int) is more efficient.
     */
    public final int set(int attr, int value) {
	// value &= fmask;	// throw all but lowest relevant bits away
	// attr &= ~ wmask;	// 0 out existing bits
	// attr |= value << offset;// set new value

	return (attr & ~wmask) | ((value & fmask) << offset);
    }

    /*
     * Use for setting a 1 bit field
     */
    public final int set(int attr) {
	assert width == 1;
	return attr | (1 << offset);
    }

    /*
     * Use for clearing any width field.
     */
    public final int clear(int attr) {
	return attr & ~wmask;
    }


    public final boolean isSet(int attr) {
	return (attr & wmask) != 0;
    }

    /**
     * Use this to get at the FG palette index embedded in an attr.
     */
    public static int foregroundColor(int attr) {
	final int cx = FGCOLOR.get(attr);
	if (cx == 0)
	    return PAL_FG;
	else
	    return cx-1;
    } 

    /**
     * Use this to get at the BG palette index embedded in an attr.
     */
    public static int backgroundColor(int attr) {
	final int cx = BGCOLOR.get(attr);
	if (cx == 0)
	    return PAL_BG;
	else
	    return cx-1;
    }

    /*
     * Read-modify-write utility for setting bitfields in 'attr'.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public static int setAttribute(int attr, int value) {
	switch (value) {
	    case 0:
		// Reset all attributes
		attr = 0;
		break;
	    case 5:             // slow blink
	    case 6:             // fast blink
		// Attr.BLINK
		// FALLTHRU
	    case 1:
		attr = DIM.clear(attr);
		attr = BRIGHT.set(attr);
		break;
	    case 2:
                // Faint - not implemented
		attr = BRIGHT.clear(attr);
		attr = DIM.set(attr);
		break;
            case 3:
                // Italic - not supported
                break;
	    case 4:
		attr = UNDERSCORE.set(attr);
		break;
	    case 7:
		attr = REVERSE.set(attr);
		break;
	    case 8:
		attr = HIDDEN.set(attr);
		break;

	    case 9:
		// Term specific
		attr = ACTIVE.set(attr);
		break;

	    // turn individual attributes off (dtterm specific?)
	    case 25:
		// blinking off
		// FALLTHRU
	    case 22:
		attr = DIM.clear(attr);
		attr = BRIGHT.clear(attr);
		break;
	    case 24:
		attr = UNDERSCORE.clear(attr);
		break;
	    case 27:
		attr = REVERSE.clear(attr);
		break;
	    case 28:
		attr = HIDDEN.clear(attr);
		break;

	    // default color
	    case 39:
		attr = FGCOLOR.clear(attr);
		break;
	    case 49:
		attr = BGCOLOR.clear(attr);
		break;


	    // ANSI fg
	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:

	    // ANSI bg
	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:

	    // custom fg
            case 50:
	    case 51:
	    case 52:
	    case 53:
	    case 54:
	    case 55:
	    case 56:
	    case 57:

	    // custom bg (was: 60-67)
	    case 58:
	    case 59:
	    case 60:
	    case 61:
	    case 62:
	    case 63:
	    case 64:
	    case 65:

	    // bright fg
	    case 90:
	    case 91:
	    case 92:
	    case 93:
	    case 94:
	    case 95:
	    case 96:
	    case 97:

	    // bright bg
	    case 100:
	    case 101:
	    case 102:
	    case 103:
	    case 104:
	    case 105:
	    case 106:
	    case 107:
		try {
		    Info i = map[value];
		    if (i != null)
			attr = i.field.set(attr, i.pindex+1);
		} catch (ArrayStoreException x) {
		    // silently ignore unrecognized attribute
		    break;
		}
		break;

	    default:
		// silently ignore unrecognized attribute
		break;
	} 
	return attr;
    }

    /*
     * Read-modify-write utility for unsetting bitfields in 'attr'.
     * Note: this doesn't cover the unsetting operations which
     * setAttributes does.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public static int unsetAttribute(int attr, int value) {
	switch (value) {
	    case 0:
		// Reset all attributes
		attr = 0;
		break;
	    case 5:
		// Attr.BLINK
		// FALLTHRU
	    case 1:
		attr = BRIGHT.clear(attr);
		break;
	    case 2:
		attr = DIM.clear(attr);
		break;
	    case 4:
		attr = UNDERSCORE.clear(attr);
		break;
	    case 7:
		attr = REVERSE.clear(attr);
		break;
	    case 8:
		attr = HIDDEN.clear(attr);
		break;
	    case 9:
		attr = ACTIVE.clear(attr);
		break;

	    // ANSI fg
	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:

	    // ANSI bg
	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:

	    // custom colors dtterm fg
            case 50:
	    case 51:
	    case 52:
	    case 53:
	    case 54:
	    case 55:
	    case 56:
	    case 57:

	    // custom colors dtterm bg
	    case 58:
	    case 59:
	    case 60:
	    case 61:
	    case 62:
	    case 63:
	    case 64:
	    case 65:
                
	    // bright fg
	    case 90:
	    case 91:
	    case 92:
	    case 93:
	    case 94:
	    case 95:
	    case 96:
	    case 97:

	    // bright bg
	    case 100:
	    case 101:
	    case 102:
	    case 103:
	    case 104:
	    case 105:
	    case 106:
	    case 107:
		try {
		    Info i = map[value];
		    if (i != null)
			attr = i.field.clear(attr);
		} catch (ArrayStoreException x) {
		    // silently ignore unrecognized attribute
		    break;
		}
		break;
	    default:
		// silently ignore unrecognized attribute
		break;
	} 
	return attr;
    }

    public static final int rendition_to_pindex(int rendition) {
	try {
	    Info i = map[rendition];
	    if (i == null)
		return -1;
	    else
		return i.pindex;
	} catch (ArrayIndexOutOfBoundsException x) {
	    return -1;
	}
    }

    private static final class Info {
	public final int rendition;	// map[r].rendition == r
	public final Attr field;	// ... to set
	public final int pindex;	// palette index

	Info(int rendition, Attr field, int pindex) {
	    this.rendition = rendition;
	    this.field = field;
	    this.pindex = pindex;
	}
    }

    // Map a rendition to info needed for settig it's value
    private static final Info map[];

    static {
	map = new Info[] {
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,	// 9

	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,	// 19

	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,	// 29

	    // ANSI fg
	    new Info(30, FGCOLOR, PAL_ANSI+0),
	    new Info(31, FGCOLOR, PAL_ANSI+1),
	    new Info(32, FGCOLOR, PAL_ANSI+2),
	    new Info(33, FGCOLOR, PAL_ANSI+3),
	    new Info(34, FGCOLOR, PAL_ANSI+4),
	    new Info(35, FGCOLOR, PAL_ANSI+5),
	    new Info(36, FGCOLOR, PAL_ANSI+6),
	    new Info(37, FGCOLOR, PAL_ANSI+7),
	    null,	// 38
	    new Info(39, FGCOLOR, PAL_FG),

	    // ANSI bg
	    new Info(40, BGCOLOR, PAL_ANSI+0),
	    new Info(41, BGCOLOR, PAL_ANSI+1),
	    new Info(42, BGCOLOR, PAL_ANSI+2),
	    new Info(43, BGCOLOR, PAL_ANSI+3),
	    new Info(44, BGCOLOR, PAL_ANSI+4),
	    new Info(45, BGCOLOR, PAL_ANSI+5),
	    new Info(46, BGCOLOR, PAL_ANSI+6),
	    new Info(47, BGCOLOR, PAL_ANSI+7),
	    null,	// 48
	    new Info(49, BGCOLOR, PAL_BG),

	    // dtterm/custom fg
	    new Info(50, FGCOLOR, PAL_BRIGHT+0),
	    new Info(51, FGCOLOR, PAL_BRIGHT+1),
	    new Info(52, FGCOLOR, PAL_BRIGHT+2),
	    new Info(53, FGCOLOR, PAL_BRIGHT+3),
	    new Info(54, FGCOLOR, PAL_BRIGHT+4),
	    new Info(55, FGCOLOR, PAL_BRIGHT+5),
	    new Info(56, FGCOLOR, PAL_BRIGHT+6),
	    new Info(57, FGCOLOR, PAL_BRIGHT+7),

	    // dtterm/custom bg
	    new Info(58, BGCOLOR, PAL_BRIGHT+0),
	    new Info(59, BGCOLOR, PAL_BRIGHT+1),
	    new Info(60, BGCOLOR, PAL_BRIGHT+2),
	    new Info(61, BGCOLOR, PAL_BRIGHT+3),
	    new Info(62, BGCOLOR, PAL_BRIGHT+4),
	    new Info(63, BGCOLOR, PAL_BRIGHT+5),
	    new Info(64, BGCOLOR, PAL_BRIGHT+6),
	    new Info(65, BGCOLOR, PAL_BRIGHT+7),

	    null,	// 66
	    null,
	    null,
	    null,	// 69

	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,	// 79

	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,
	    null,	// 89

	    // BRIGHT fg
	    new Info(90, FGCOLOR, PAL_BRIGHT+0),
	    new Info(91, FGCOLOR, PAL_BRIGHT+1),
	    new Info(92, FGCOLOR, PAL_BRIGHT+2),
	    new Info(93, FGCOLOR, PAL_BRIGHT+3),
	    new Info(94, FGCOLOR, PAL_BRIGHT+4),
	    new Info(95, FGCOLOR, PAL_BRIGHT+5),
	    new Info(96, FGCOLOR, PAL_BRIGHT+6),
	    new Info(97, FGCOLOR, PAL_BRIGHT+7),
	    null,	// 98
	    null,	// 99

	    // BRIGHT bg
	    new Info(100, BGCOLOR, PAL_BRIGHT+0),
	    new Info(101, BGCOLOR, PAL_BRIGHT+1),
	    new Info(102, BGCOLOR, PAL_BRIGHT+2),
	    new Info(103, BGCOLOR, PAL_BRIGHT+3),
	    new Info(104, BGCOLOR, PAL_BRIGHT+4),
	    new Info(105, BGCOLOR, PAL_BRIGHT+5),
	    new Info(106, BGCOLOR, PAL_BRIGHT+6),
	    new Info(107, BGCOLOR, PAL_BRIGHT+7),
	};

	for (int mx = 0; mx < map.length; mx++) {
	    assert map[mx] == null ||
		   map[mx].rendition == mx;
	}
    }
}