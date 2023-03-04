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

class AttrSave {
    private static final int BGCOLOR_OFF = 0;
    private static final int BGCOLOR_WIDTH = 5;
    private static final int BGCOLOR_MASK = 0xf;
    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int BGCOLOR = BGCOLOR_MASK << BGCOLOR_OFF;

    private static final int FGCOLOR_OFF = BGCOLOR_OFF + BGCOLOR_WIDTH;
    private static final int FGCOLOR_WIDTH = 5;
    private static final int FGCOLOR_MASK = 0xf;
    public static final int FGCOLOR = FGCOLOR_MASK << FGCOLOR_OFF;

    private static final int HIDDEN_OFF = FGCOLOR_OFF + FGCOLOR_WIDTH;
    private static final int HIDDEN_WIDTH = 1;
    public static final int HIDDEN = 0x1 << HIDDEN_OFF;

    private static final int REVERSE_OFF = HIDDEN_OFF + HIDDEN_WIDTH;
    private static final int REVERSE_WIDTH = 1;
    public static final int REVERSE = 0x1 << REVERSE_OFF;

    private static final int BLINK_OFF = REVERSE_OFF + REVERSE_WIDTH;
    private static final int BLINK_WIDTH = 1;
    public static final int BLINK = 0x1 << BLINK_OFF;

    private static final int UNDERSCORE_OFF = BLINK_OFF + BLINK_WIDTH;
    private static final int UNDERSCORE_WIDTH = 1;
    public static final int UNDERSCORE = 0x1 << UNDERSCORE_OFF;

    private static final int BRIGHT_OFF = UNDERSCORE_OFF + UNDERSCORE_WIDTH;
    private static final int BRIGHT_WIDTH = 1;
    public static final int BRIGHT = 0x1 << BRIGHT_OFF;

    private static final int DIM_OFF = BRIGHT_OFF + BRIGHT_WIDTH;
    private static final int DIM_WIDTH = 1;
    public static final int DIM = 0x1 << DIM_OFF;

    private static final int ACTIVE_OFF = DIM_OFF + DIM_WIDTH;
    private static final int ACTIVE_WIDTH = 1;
    public static final int ACTIVE = 0x1 << ACTIVE_OFF;

    // Since an attr value of 0 means render using default attributes
    // We need a value that signifies that no attribute has been set.
    // Can't use the highest (sign) bit since Java has no unsigned and
    // we get complaints from the compiler.
    public static final int UNSET = 0x40000000;



    /**
     * attr = Attr.setBackgroundColor(attr, 7);
     */

    @SuppressWarnings({"PointlessBitwiseExpression", "AssignmentToMethodParameter"})
    public static int setBackgroundColor(int attr, int code) {
	code &= BGCOLOR_MASK;	// throw all but lowest relevant bits away
	attr &= ~ BGCOLOR;	// 0 out existing bits
	attr |= code << BGCOLOR_OFF;
	return attr;
    } 


    /**
     * attr = Attr.setForegroundColor(attr, 7);
     */

    @SuppressWarnings("AssignmentToMethodParameter")
    public static int setForegroundColor(int attr, int code) {
	code &= FGCOLOR_MASK;	// throw all but lowest relevant bits away
	attr &= ~ FGCOLOR;	// 0 out existing bits
	attr |= code << FGCOLOR_OFF;
	return attr;
    }

    /**
     * Use this to get at the FG color value embedded in an attr.
     */
    public static int foregroundColor(int attr) {
	return (attr >> FGCOLOR_OFF) & FGCOLOR_MASK;
    } 

    /**
     * Use this to get at the BG color value embedded in an attr.
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    public static int backgroundColor(int attr) {
	return (attr >> BGCOLOR_OFF) & BGCOLOR_MASK;
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
		attr &= ~ AttrSave.DIM;
		attr |= AttrSave.BRIGHT;
		break;
	    case 2:
		attr &= ~ AttrSave.BRIGHT;
		attr |= AttrSave.DIM;
		break;
            case 3:
                // Italic - not supported
                break;
	    case 4:
		attr |= AttrSave.UNDERSCORE;
		break;
	    case 7:
		attr |= AttrSave.REVERSE;
		break;
	    case 8:
		attr |= AttrSave.HIDDEN;
		break;

	    case 9:
		// Term specific
		attr |= AttrSave.ACTIVE;
		break;

	    // turn individual attributes off (dtterm specific?)
	    case 25:
		// blinking off
		// FALLTHRU
	    case 22:
		attr &= ~ AttrSave.DIM;
		attr &= ~ AttrSave.BRIGHT;
		break;
	    case 24:
		attr &= ~ AttrSave.UNDERSCORE;
		break;
	    case 27:
		attr &= ~ AttrSave.REVERSE;
		break;
	    case 28:
		attr &= ~ AttrSave.HIDDEN;
		break;

	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:
		attr = AttrSave.setForegroundColor(attr, value-30+1);
		break;

	    case 39:
		// revert to default (dtterm specific)
		attr = AttrSave.setForegroundColor(attr, 0);
		break;

	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:
		attr = AttrSave.setBackgroundColor(attr, value-40+1);
		break;

	    case 49:
		// revert to default (dtterm specific)
		attr = AttrSave.setBackgroundColor(attr, 0);
		break;

            case 50:
	    case 51:
	    case 52:
	    case 53:
	    case 54:
	    case 55:
	    case 56:
	    case 57:
                // custom colors
		attr = AttrSave.setForegroundColor(attr, value-50+9);
		break;

	    case 60:
	    case 61:
	    case 62:
	    case 63:
	    case 64:
	    case 65:
	    case 66:
	    case 67:
		// custom colors
		attr = AttrSave.setBackgroundColor(attr, value-60+9);
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
		attr &= ~ AttrSave.BRIGHT;
		break;
	    case 2:
		attr &= ~ AttrSave.DIM;
		break;
	    case 4:
		attr &= ~ AttrSave.UNDERSCORE;
		break;
	    case 7:
		attr &= ~ AttrSave.REVERSE;
		break;
	    case 8:
		attr &= ~ AttrSave.HIDDEN;
		break;
	    case 9:
		attr &= ~ AttrSave.ACTIVE;
		break;

	    case 30:
	    case 31:
	    case 32:
	    case 33:
	    case 34:
	    case 35:
	    case 36:
	    case 37:
		attr = AttrSave.setForegroundColor(attr, 0);
		break;

	    case 40:
	    case 41:
	    case 42:
	    case 43:
	    case 44:
	    case 45:
	    case 46:
	    case 47:
		attr = AttrSave.setBackgroundColor(attr, 0);
		break;

            case 50:
	    case 51:
	    case 52:
	    case 53:
	    case 54:
	    case 55:
	    case 56:
	    case 57:
		// custom colors
		attr = AttrSave.setForegroundColor(attr, 0);
		break;

	    case 60:
	    case 61:
	    case 62:
	    case 63:
	    case 64:
	    case 65:
	    case 66:
	    case 67:
		// custom colors
		attr = AttrSave.setBackgroundColor(attr, 0);
		break;
                
	    default:
		// silently ignore unrecognized attribute
		break;
	} 
	return attr;
    }
}
