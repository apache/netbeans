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

/*
 * "Attr.java"
 * Attr.java 1.7 01/07/23
 */

package org.netbeans.lib.terminalemulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
class Attr {
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

    private final Color fgColor;

    private final Color bgColor;

    private final int attributes;

    public Attr() {
        this(0, null, null);
    }

    public Attr(int attributes, Color fgColor, Color bgColor) {
        this.attributes = attributes;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
    }

    public boolean isEmpty() {
        return attributes == 0 && fgColor == null && bgColor == null;
    }

    public boolean isHidden() {
        return AttrEncoding.HIDDEN.isSet(this.attributes);
    }

    public boolean isReverse() {
        return AttrEncoding.REVERSE.isSet(this.attributes);
    }

    public boolean isBlink() {
        return AttrEncoding.BLINK.isSet(this.attributes);
    }

    public boolean isUnderscore() {
        return AttrEncoding.UNDERSCORE.isSet(this.attributes);
    }

    public boolean isBright() {
        return AttrEncoding.BRIGHT.isSet(this.attributes);
    }

    public boolean isDim() {
        return AttrEncoding.DIM.isSet(this.attributes);
    }

    public boolean isActive() {
        return AttrEncoding.ACTIVE.isSet(this.attributes);
    }


    public boolean isBGcolor() {
        return bgColor != null || AttrEncoding.BGCOLOR.isSet(this.attributes);
    }

    public boolean isFGcolor() {
        return fgColor != null || AttrEncoding.FGCOLOR.isSet(this.attributes);
    }

    /**
     * Use this to get at the FG palette index embedded in an attr.
     */
    public Color getFGcolor(Color[] palette) {
        if (fgColor != null) {
            return fgColor;
        }
        final int cx = AttrEncoding.FGCOLOR.get(this.attributes);
        if (cx == 0) {
            return palette[PAL_FG];
        } else {
            return palette[cx - 1];
        }
    }

    /**
     * Use this to get at the BG palette index embedded in an attr.
     */
    public Color getBGColor(Color[] palette) {
        if (bgColor != null) {
            return bgColor;
        }
        final int cx = AttrEncoding.BGCOLOR.get(this.attributes);
        if (cx == 0) {
            return palette[PAL_BG];
        } else {
            return palette[cx - 1];
        }
    }

    public Attr setAttribute(List<Integer> parameterList) {
        int attributesUpdated = this.attributes;
        Color fgColorUpdated = this.fgColor;
        Color bgColorUpdated = this.bgColor;

        // Loop over the parameter list as multiple operations can be combined
        // into a single call
        List<Integer> workList = new ArrayList<>(parameterList);
        if (workList.isEmpty()) {
            workList.add(0); // No parameter implies reset
        }
        while (!workList.isEmpty()) {
            Integer value = workList.removeFirst();
            switch (value) {
                case 0 -> {
                    // Reset all attributes
                    attributesUpdated = 0;
                    fgColorUpdated = null;
                    bgColorUpdated = null;
                }
                case 1, 5, 6 -> { // bold/bright // slow blink // fast blink (blink is not supported)
                    attributesUpdated = AttrEncoding.DIM.clear(attributesUpdated);
                    attributesUpdated = AttrEncoding.BRIGHT.set(attributesUpdated);
                }
                case 2 -> {
                    // Faint - not implemented
                    attributesUpdated = AttrEncoding.BRIGHT.clear(attributesUpdated);
                    attributesUpdated = AttrEncoding.DIM.set(attributesUpdated);
                }
                case 3 -> {
                    // Italic - not supported
                }
                case 4 -> {
                    attributesUpdated = AttrEncoding.UNDERSCORE.set(attributesUpdated);
                }
                case 7 -> {
                    attributesUpdated = AttrEncoding.REVERSE.set(attributesUpdated);
                }
                case 8 -> {
                    attributesUpdated = AttrEncoding.HIDDEN.set(attributesUpdated);
                }
                case 9 -> {
                    // Term specific
                    attributesUpdated = AttrEncoding.ACTIVE.set(attributesUpdated);
                }
                case 22, 25 -> { // reset bold/dim // reset blink (not supported)
                    attributesUpdated = AttrEncoding.DIM.clear(attributesUpdated);
                    attributesUpdated = AttrEncoding.BRIGHT.clear(attributesUpdated);
                }
                case 24 -> {
                    attributesUpdated = AttrEncoding.UNDERSCORE.clear(attributesUpdated);
                }
                case 27 -> {
                    attributesUpdated = AttrEncoding.REVERSE.clear(attributesUpdated);
                }
                case 28 -> {
                    attributesUpdated = AttrEncoding.HIDDEN.clear(attributesUpdated);
                }
                case 39 -> { // default color
                    attributesUpdated = AttrEncoding.FGCOLOR.clear(attributesUpdated);
                    fgColorUpdated = null;
                }
                case 49 -> { // default color
                    attributesUpdated = AttrEncoding.BGCOLOR.clear(attributesUpdated);
                    bgColorUpdated = null;
                }
                case 30, 31, 32, 33, 34, 35, 36, 37, // ANSI fg
                     40, 41, 42, 43, 44, 45, 46, 47, // ANSI bg
                     50, 51, 52, 53, 54, 55, 56, 57, // custom fg
                     58, 59, 60, 61, 62, 63, 64, 65, // custom bg (was: 60-67)
                     90, 91, 92, 93, 94, 95, 96, 97, // bright fg
                     100, 101, 102, 103, 104, 105, 106, 107 -> { // bright bg
                    try {
                        Info i = map[value];
                        if (i != null) {
                            attributesUpdated = i.field.set(attributesUpdated, i.pindex + 1);
                            // If color is switched to a predefined value, clear
                            // clear the corresponding truecolor value
                            if (i.field == AttrEncoding.FGCOLOR) {
                                fgColorUpdated = null;
                            } else if (i.field == AttrEncoding.BGCOLOR) {
                                bgColorUpdated = null;
                            }
                        }
                    } catch (ArrayStoreException x) {
                        // silently ignore unrecognized attribute
                        break;
                    }
                }
                case 38, 48 -> { // Extended set color foreground / background
                    if (workList.size() > 1) {
                        boolean background  = value == 48;
                        value = workList.removeFirst();
                        if (value == 5) { // Color format indexed into 256 color palette
                            value = workList.removeFirst();
                            attributesUpdated =
                                    (background ? AttrEncoding.BGCOLOR : AttrEncoding.FGCOLOR)
                                    .set(attributesUpdated, value + 1);
                        } else if (value == 2) { // Color format RGB values
                            int r = workList.removeFirst();
                            int g = workList.removeFirst();
                            int b = workList.removeFirst();
                            if (background) {
                                bgColorUpdated = new Color(r, g, b);
                            } else {
                                fgColorUpdated = new Color(r, g, b);
                            }
                        }
                    }
                }
                default -> {
                    // silently ignore unrecognized attribute
                }
            }
        }
        return new Attr(attributesUpdated, fgColorUpdated, bgColorUpdated);
    }

    public Attr unsetAttribute(int value) {
        int attrUpdated = attributes;
        Color fgColorUpdated = this.fgColor;
        Color bgColorUpdated = this.bgColor;
        switch (value) {
            case 0 -> {
                // Reset all attributes
                attrUpdated = 0;
                fgColorUpdated = null;
                bgColorUpdated = null;
            }
            case 1, 5, 6 -> { // bold/bright // slow blink // fast blink (blink is not supported)
                attrUpdated = AttrEncoding.BRIGHT.clear(attrUpdated);
            }
            case 2 -> {
                attrUpdated = AttrEncoding.DIM.clear(attrUpdated);
            }
            case 4 -> {
                attrUpdated = AttrEncoding.UNDERSCORE.clear(attrUpdated);
            }
            case 7 -> {
                attrUpdated = AttrEncoding.REVERSE.clear(attrUpdated);
            }
            case 8 -> {
                attrUpdated = AttrEncoding.HIDDEN.clear(attrUpdated);
            }
            case 9 -> {
                attrUpdated = AttrEncoding.ACTIVE.clear(attrUpdated);
            }
            case 30, 31, 32, 33, 34, 35, 36, 37, // ANSI fg
                 40, 41, 42, 43, 44, 45, 46, 47, // ANSI bg
                 50, 51, 52, 53, 54, 55, 56, 57, // custom fg
                 58, 59, 60, 61, 62, 63, 64, 65, // custom bg (was: 60-67)
                 90, 91, 92, 93, 94, 95, 96, 97, // bright fg
                 100, 101, 102, 103, 104, 105, 106, 107 -> { // bright bg
                try {
                    Info i = map[value];
                    if (i != null) {
                        attrUpdated = i.field.clear(attrUpdated);
                        if (i.field == AttrEncoding.BGCOLOR) {
                            fgColorUpdated = null;
                        } else if (i.field == AttrEncoding.BGCOLOR) {
                            bgColorUpdated = null;
                        }
                    }
                } catch (ArrayStoreException x) {
                    // silently ignore unrecognized attribute
                    break;
                }
            }
            default -> {
                // silently ignore unrecognized attribute
            }
        }
        return new Attr(attrUpdated, fgColorUpdated, bgColorUpdated);
    }

    public Attr altVariant() {
        return new Attr(this.attributes & ~ AttrEncoding.ALT, null, null);
    }

    public Attr bgColorVariant() {
        return new Attr(AttrEncoding.BGCOLOR.get(this.attributes), null, this.bgColor);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.fgColor);
        hash = 53 * hash + Objects.hashCode(this.bgColor);
        hash = 53 * hash + this.attributes;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Attr other = (Attr) obj;
        if (this.attributes != other.attributes) {
            return false;
        }
        if (!Objects.equals(this.fgColor, other.fgColor)) {
            return false;
        }
        return Objects.equals(this.bgColor, other.bgColor);
    }

    

    @Override
    public String toString() {
        return "Attr {" +
                "isActive=" + isActive() + ", " +
                "isBGcolor=" + isBGcolor()+ ", " +
                "isBlink=" + isBlink() + ", " +
                "isDim=" + isDim() + ", " +
                "isEmpty=" + isEmpty() + ", " +
                "isFGcolor=" + isFGcolor() + ", " +
                "isHidden=" + isHidden() + ", " +
                "isReverse=" + isReverse() + ", " +
                "isUnderscore=" + isUnderscore() + ", " +
                "fgColor=" + fgColor + ", " +
                "bgColor=" + bgColor +
                "}";
    }

    private enum AttrEncoding {
        BGCOLOR(9),		// encodes 0 or palette index+1 (was: 5), offset 0
        FGCOLOR(9),		// encodes 0 or palette index+1 (was: 5), offset 9
        HIDDEN(1),          // offset 10
        REVERSE(1),         // offset 11
        BLINK(1),           // offset 12
        UNDERSCORE(1),      // offset 13
        BRIGHT(1),          // offset 14
        DIM(1),             // offset 15
        ACTIVE(1),          // offset 16

        // Since an attr value of 0 means render using default attributes
        // We need a value that signifies that no attribute has been set.
        // Can't use the highest (sign) bit since Java has no unsigned and
        // we get complaints from the compiler.
        UNSET(30, 1);

        public static final int ALT;

        private final int width;	// ... of field
        private final int fmask;	// ... corresponding to width

        private int offset;		// ... of field from the "left"
        private int wmask;		// ... over the word. 1 where bits are set.

        static {
            // System.out.printf("Attr.static()\n");
            for (AttrEncoding a : values())
                a.init();
            ALT = AttrEncoding.BGCOLOR.wmask | AttrEncoding.FGCOLOR.wmask | AttrEncoding.REVERSE.wmask | AttrEncoding.ACTIVE.wmask;
        }

        /*
         * Explicitly set the offset.
         */
        private AttrEncoding(int offset, int width) {
            // System.out.printf("Attr(%d, %d) -> %d\n", offset, width, ordinal());
            this.offset = offset;
            this.width = width;
            this.fmask = (1<<width)-1;
        }

        /*
         * Automatically set the offset.
         */
        private AttrEncoding(int width) {
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
                    AttrEncoding prev = AttrEncoding.values()[ordinal()-1];
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

    }

    public static final int rendition_to_pindex(int rendition) {
        try {
            Info i = map[rendition];
            if (i == null) {
                return -1;
            } else {
                return i.pindex;
            }
        } catch (ArrayIndexOutOfBoundsException x) {
            return -1;
        }
    }

    private static final class Info {

        public final int rendition;	// map[r].rendition == r
        public final AttrEncoding field; // ... to set
        public final int pindex;	// palette index

        Info(int rendition, AttrEncoding field, int pindex) {
            this.rendition = rendition;
            this.field = field;
            this.pindex = pindex;
        }
    }

    // Map a rendition to info needed for settig it's value
    private static final Info map[];

    static {
        map = new Info[]{
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null, // 9

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null, // 19

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null, // 29

            // ANSI fg
            new Info(30, AttrEncoding.FGCOLOR, PAL_ANSI + 0),
            new Info(31, AttrEncoding.FGCOLOR, PAL_ANSI + 1),
            new Info(32, AttrEncoding.FGCOLOR, PAL_ANSI + 2),
            new Info(33, AttrEncoding.FGCOLOR, PAL_ANSI + 3),
            new Info(34, AttrEncoding.FGCOLOR, PAL_ANSI + 4),
            new Info(35, AttrEncoding.FGCOLOR, PAL_ANSI + 5),
            new Info(36, AttrEncoding.FGCOLOR, PAL_ANSI + 6),
            new Info(37, AttrEncoding.FGCOLOR, PAL_ANSI + 7),
            null, // 38
            new Info(39, AttrEncoding.FGCOLOR, PAL_FG),
            // ANSI bg
            new Info(40, AttrEncoding.BGCOLOR, PAL_ANSI + 0),
            new Info(41, AttrEncoding.BGCOLOR, PAL_ANSI + 1),
            new Info(42, AttrEncoding.BGCOLOR, PAL_ANSI + 2),
            new Info(43, AttrEncoding.BGCOLOR, PAL_ANSI + 3),
            new Info(44, AttrEncoding.BGCOLOR, PAL_ANSI + 4),
            new Info(45, AttrEncoding.BGCOLOR, PAL_ANSI + 5),
            new Info(46, AttrEncoding.BGCOLOR, PAL_ANSI + 6),
            new Info(47, AttrEncoding.BGCOLOR, PAL_ANSI + 7),
            null, // 48
            new Info(49, AttrEncoding.BGCOLOR, PAL_BG),
            // dtterm/custom fg
            new Info(50, AttrEncoding.FGCOLOR, PAL_BRIGHT + 0),
            new Info(51, AttrEncoding.FGCOLOR, PAL_BRIGHT + 1),
            new Info(52, AttrEncoding.FGCOLOR, PAL_BRIGHT + 2),
            new Info(53, AttrEncoding.FGCOLOR, PAL_BRIGHT + 3),
            new Info(54, AttrEncoding.FGCOLOR, PAL_BRIGHT + 4),
            new Info(55, AttrEncoding.FGCOLOR, PAL_BRIGHT + 5),
            new Info(56, AttrEncoding.FGCOLOR, PAL_BRIGHT + 6),
            new Info(57, AttrEncoding.FGCOLOR, PAL_BRIGHT + 7),
            // dtterm/custom bg
            new Info(58, AttrEncoding.BGCOLOR, PAL_BRIGHT + 0),
            new Info(59, AttrEncoding.BGCOLOR, PAL_BRIGHT + 1),
            new Info(60, AttrEncoding.BGCOLOR, PAL_BRIGHT + 2),
            new Info(61, AttrEncoding.BGCOLOR, PAL_BRIGHT + 3),
            new Info(62, AttrEncoding.BGCOLOR, PAL_BRIGHT + 4),
            new Info(63, AttrEncoding.BGCOLOR, PAL_BRIGHT + 5),
            new Info(64, AttrEncoding.BGCOLOR, PAL_BRIGHT + 6),
            new Info(65, AttrEncoding.BGCOLOR, PAL_BRIGHT + 7),
            null, // 66
            null,
            null,
            null, // 69

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null, // 79

            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null, // 89

            // BRIGHT fg
            new Info(90, AttrEncoding.FGCOLOR, PAL_BRIGHT + 0),
            new Info(91, AttrEncoding.FGCOLOR, PAL_BRIGHT + 1),
            new Info(92, AttrEncoding.FGCOLOR, PAL_BRIGHT + 2),
            new Info(93, AttrEncoding.FGCOLOR, PAL_BRIGHT + 3),
            new Info(94, AttrEncoding.FGCOLOR, PAL_BRIGHT + 4),
            new Info(95, AttrEncoding.FGCOLOR, PAL_BRIGHT + 5),
            new Info(96, AttrEncoding.FGCOLOR, PAL_BRIGHT + 6),
            new Info(97, AttrEncoding.FGCOLOR, PAL_BRIGHT + 7),
            null, // 98
            null, // 99

            // BRIGHT bg
            new Info(100, AttrEncoding.BGCOLOR, PAL_BRIGHT + 0),
            new Info(101, AttrEncoding.BGCOLOR, PAL_BRIGHT + 1),
            new Info(102, AttrEncoding.BGCOLOR, PAL_BRIGHT + 2),
            new Info(103, AttrEncoding.BGCOLOR, PAL_BRIGHT + 3),
            new Info(104, AttrEncoding.BGCOLOR, PAL_BRIGHT + 4),
            new Info(105, AttrEncoding.BGCOLOR, PAL_BRIGHT + 5),
            new Info(106, AttrEncoding.BGCOLOR, PAL_BRIGHT + 6),
            new Info(107, AttrEncoding.BGCOLOR, PAL_BRIGHT + 7),};

        for (int mx = 0; mx < map.length; mx++) {
            assert map[mx] == null
                    || map[mx].rendition == mx;
        }
    }
}
