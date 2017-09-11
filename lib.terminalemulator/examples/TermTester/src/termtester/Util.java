/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package termtester;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ivan
 */
public final class Util {
    public  static enum FillPattern {
        NONE, X, ROWCOL, ZIGZAG
    }

    public static enum Direction {
        NW, N, NE,
        W, C, E,
        SW, S, SE
    }

    public static enum MarginDirection {
        ANW, AN, ANE,           // Above ...
        NW, N, NE,              // On ...
        BNW, BN, BNE,           // Below ...
        W, C, E,                // Between
        ASW, AS, ASE,           // Above
        SW, S, SE,              // On
        BSW, BS, BSE            // Below
    }

    private static String fillLine(Context context, Context.Margin margin, int r, int end) {
        char[] buf = new char[context.width()];
        char fillchar = '.';
        if (margin != null) {
            if (r == margin.low)
                fillchar = 'm';
            else if (r == margin.hi)
                fillchar = 'M';
        }
        for (int c = 0; c < end; c++) {
            if ((c + 1) % 10 == 0) {
                buf[c] = ("" + (((c + 1) / 10))).charAt(0);
            } else {
                buf[c] = fillchar;
            }
        }
        String rowInfo = String.format(" %2d ", r);
        for (int rix = 0; rix < rowInfo.length(); rix++) {
            buf[3 + rix] = rowInfo.charAt(rix);
        }
        return String.valueOf(buf);
    }

    public static void fill(Context context, FillPattern fillPattern) {
        Context.Margin margin = context.getMargin();
        String line;

        // home
        context.send("\\ESC[;H");
        switch (fillPattern) {
            case NONE:
                context.sendQuiet("\\ESC[2J");  // erase all screen
                break;
            case X:
                for (int r = 1; r <= context.height(); r++) {
                    for (int c = 0; c < context.width(); c++) {
                        context.sendQuiet("X");
                    }
                }
                break;
            case ROWCOL:
                for (int r = 1; r <= context.height(); r++) {
                    line = fillLine(context, margin, r, context.width());
                    context.sendQuiet("%s", line);
                }
                break;
            case ZIGZAG:
                int end1 = context.width()/3;
                int end2 = (context.width()*2)/3;
                for (int r = 1; r <= context.height(); r++) {
                    int end = (r % 2 ==0)? end1: end2;
                    line = fillLine(context, margin, r, end);
                    if (r == context.height())
                        context.sendQuiet("%s", line);
                    else
                        context.sendQuiet("%s\n\r", line);
                }
                break;
        }
        // home
        context.send("\\ESC[;H");
    }

    public static boolean go(Context context, MarginDirection direction) {
        Context.Margin margin = context.getMargin();
        if (margin == null)
            return false;
        int row;
        switch (direction) {
            case ANW:
            case AN:
            case ANE:
                row = margin.low - 1;
                break;
            case NW:
            case N:
            case NE:
                row = margin.low;
                break;
            case BNW:
            case BN:
            case BNE:
                row = margin.low + 1;
                break;

            case W:
            case C:
            case E:
                row = margin.low + (margin.hi-margin.low)/2;
                if (row == margin.low || row == margin.hi)
                    return false;
                break;

            case ASW:
            case AS:
            case ASE:
                row = margin.hi - 1;
                break;
            case SW:
            case S:
            case SE:
                row = margin.hi;
                break;
            case BSW:
            case BS:
            case BSE:
                row = margin.hi + 1;
                break;
            default:
                row = 1;
                break;
        }

        if (row < 1)
            return false;
        if (row > context.height())
            return false;

        int col;
        switch (direction) {
            case ANW:
            case NW:
            case BNW:
            case W:
            case ASW:
            case SW:
            case BSW:
                col = 1;
                break;

            case AN:
            case N:
            case BN:
            case C:
            case AS:
            case S:
            case BS:
                col = context.width()/2;
                break;

            case ANE:
            case NE:
            case BNE:
            case E:
            case ASE:
            case SE:
            case BSE:
                col = context.width();
                break;
            default:
                col = 1;
                break;
        }
        context.sendQuiet("\\ESC[%d;%dH", row, col);
        return true;
    }

    public static void go(Context context, Direction direction) {
        switch (direction) {
            case N:
                context.send("\\ESC[%d;%dH", 1, context.width()/2);
                break;
            case NE:
                context.send("\\ESC[%d;%dH", 1, context.width());
                break;
            case E:
                context.send("\\ESC[%d;%dH", context.height()/2, context.width());
                break;
            case SE:
                context.send("\\ESC[%d;%dH", context.height(), context.width());
                break;
            case S:
                context.send("\\ESC[%d;%dH", context.height(), context.width()/2);
                break;
            case SW:
                context.send("\\ESC[%d;%dH", context.height(), 1);
                break;
            case W:
                context.send("\\ESC[%d;%dH", context.height()/2, 1);
                break;
            case NW:
                context.send("\\ESC[%d;%dH", 1, 1);
                break;
            case C:
                context.send("\\ESC[%d;%dH", context.height()/2, context.width()/2);
                break;
            default:
                context.interp.error("unrecognized compass direction '%s'", direction);
                break;
        }
    }

    public static void go(Context context, int row, int col) {
        context.send("\\ESC[%s;%sH", row, col);
    }
    public static void attr(Context context, int n) {
        context.send("\\ESC[%dm", n);
    }

    private static final Map<String, Integer> mnemonicMap = new HashMap<String, Integer>();
    private static final Map<Character, String> charMap = new HashMap<Character, String>();

    /*
     * Map a mnemonic to the actual character.
     */
    public static int mnemonicToChar(String mnemonic) {
        Integer c = mnemonicMap.get(mnemonic);
        if (c == null)
            return -1;
        else
            return c;
    }

    public static String charToMnemonic(char c) {
        String ch = charMap.get(c);
        if (ch == null) {
            if (c > 127)
                return String.format("%#02x/%d", (int)c, (int)c);
            else
                return "" + c;
        } else {
            return "\\" + ch;
        }
    }

    private static void map(String mnemonic, int code, boolean ambig) {
        mnemonicMap.put(mnemonic, ambig? -2: code);
        charMap.put((char) code, mnemonic);
    }

    private static void map(String mnemonic, int code) {
        mnemonicMap.put(mnemonic, code);
        charMap.put((char) code, mnemonic);
    }

    static {
        map("NUL", 0x00);
        map("SOH", 0x01, true);
        map("STX", 0x02);
        map("ETX", 0x03);
        map("EOT", 0x04);
        map("ENQ", 0x05);
        map("ACK", 0x06);
        map("BEL", 0x07);
        map("BS",  0x08);
        map("HT",  0x09);
        map("LF",  0x0a);
        map("VT",  0x0b);
        map("FF",  0x0c);
        map("CR",  0x0d);
        map("SO", 0x0e, true);
        map("SI",  0x0f);
        map("DLE", 0x10);
        map("DC1", 0x11);
        map("DC2", 0x12);
        map("DC3", 0x13);
        map("DC4", 0x14);
        map("NAK", 0x15);
        map("SYN", 0x16);
        map("ETB", 0x17);
        map("CAN", 0x18);
        map("EM",  0x19);
        map("SUB", 0x1a);
        map("ESC", 0x1b);
        map("FS",  0x1c);
        map("GS",  0x1d);
        map("RS",  0x1e);
        map("US",  0x1f);
        map("SP",  0x20);
        map("DEL", 0x7f);
    }
}
