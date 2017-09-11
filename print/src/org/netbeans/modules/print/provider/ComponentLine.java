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
 * License. When distributing the software, include this License Header
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
package org.netbeans.modules.print.provider;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;

import org.openide.text.AttributedCharacters;

import org.netbeans.modules.print.util.Config;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.05.30
 */
final class ComponentLine {

    ComponentLine(AttributedCharacterIterator it, Font defaultFont, Color defaultColor) {
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            Font font = (Font) it.getAttribute(TextAttribute.FONT);
            Color color = (Color) it.getAttribute(TextAttribute.FOREGROUND);
            mySymbols.add(new Symbol(c, createFont(font, defaultFont), createColor(color, defaultColor)));
        }
        checkSpaces(defaultFont, defaultColor);
    }

    ComponentLine(String text, Font font, Color color) {
        for (int i = 0; i < text.length(); i++) {
            mySymbols.add(new Symbol(text.charAt(i), font, color));
        }
        checkSpaces(font, color);
    }

    private ComponentLine(List<Symbol> symbols, Font font, Color color) {
        mySymbols = symbols;
        checkSpaces(font, color);
    }

    private Font createFont(Font attrFont, Font defaultFont) {
        if ( !Config.getDefault().isUseFont()) {
            return defaultFont;
        }
        String name = defaultFont.getName();
        int size = defaultFont.getSize();
        int style = attrFont.getStyle();
        return new Font(name, style, size);
    }

    private Color createColor(Color attrColor, Color defaultColor) {
        if (Config.getDefault().isUseColor()) {
            return attrColor;
        }
        return defaultColor;
    }

    private void checkSpaces(Font font, Color color) {
        int i = length() - 1;
        myFont = font;
        myColor = color;

        while (i >= 1 && mySymbols.get(i).getChar() == ' ') {
            i--;
        }
        mySymbols = mySymbols.subList(0, i + 1);

        if (length() == 0) {
            mySymbols.add(new Symbol(' ', font, color));
        }
    }

    void prepend(String text) {
        Font firstFont = mySymbols.get(0).getFont();

        String name = firstFont.getName();
        int size = firstFont.getSize();

        Font font = new Font(name, Font.PLAIN, size);

        for (int i = text.length() - 1; i >= 0; i--) {
            mySymbols.add(0, new Symbol(text.charAt(i), font, myColor));
        }
    }

    boolean isEmpty() {
        for (int i = 0; i < length(); i++) {
            if (mySymbols.get(i).getChar() != ' ') {
                return false;
            }
        }
        return true;
    }

    int length() {
        return mySymbols.size();
    }

    ComponentLine substring(int index1, int index2) {
        List<Symbol> list = new ArrayList<Symbol>();

        for (int i = index1; i < index2; i++) {
            list.add(mySymbols.get(i));
        }
        return new ComponentLine(list, myFont, myColor);
    }

    ComponentLine substring(int index) {
        return substring(index, mySymbols.size());
    }

    int getAscent() {
        return (int) Math.ceil(getTextLayout().getAscent());
    }

    int getDescent() {
        return (int) Math.ceil(getTextLayout().getDescent());
    }

    int getLeading() {
        return (int) Math.ceil(getTextLayout().getLeading());
    }

    int getWidth() {
        int offset = getOffset();

        if (offset > 0) {
            offset = 0;
        }
//out(this + " " + getOffset());
        return (int) Math.ceil(getTextLayout().getBounds().getMaxX() - offset);
    }

    int getOffset() {
        return (int) Math.ceil(getTextLayout().getBounds().getX());
    }

    void draw(Graphics2D g, int x, int y) {
        getTextLayout().draw(g, x, y);
    }

    private TextLayout getTextLayout() {
        if (myTextLayout == null) {
//out();
//out("TEXT : '" + this + "'");
            myTextLayout = new TextLayout(getIterator(), Config.FONT_RENDER_CONTEXT);
        }
        return myTextLayout;
    }

    private AttributedCharacterIterator getIterator() {
        AttributedCharacters characters = new AttributedCharacters();

        for (int i = 0; i < length(); i++) {
            characters.append(mySymbols.get(i).getChar(), mySymbols.get(i).getFont(), mySymbols.get(i).getColor());
        }
        return characters.iterator();
    }

    int lastIndexOf(char c, int index) {
        for (int i = index; i >= 0; i--) {
            if (mySymbols.get(i).getChar() == c) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length(); i++) {
            builder.append(mySymbols.get(i).getChar());
        }
        return builder.toString();
    }

    void show() {
        for (int i = 0; i < length(); i++) {
            out(mySymbols.get(i));
        }
    }

    // --------------------------------
    private static final class Symbol {

        Symbol(char c, Font font, Color color) {
            myChar = c;
            myFont = font;
            myColor = color;
        }

        char getChar() {
            return myChar;
        }

        Font getFont() {
            return myFont;
        }

        Color getColor() {
            return myColor;
        }

        void setColor(Color color) {
            myColor = color;
        }

        @Override
        public String toString() {
            return "'" + myChar + "' " + getString(myFont) + " " + getString(myColor); // NOI18N
        }

        private String getString(Color color) {
            return "(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")"; // NOI18N
        }

        private String getString(Font font) {
            String style = ""; // NOI18N

            if (font.isBold()) {
                style += "bold"; // NOI18N
            }
            if (font.isItalic()) {
                style += " italic"; // NOI18N
            }
            else {
                style += " plain"; // NOI18N
            }
            return "[" + font.getName() + ", " + style + ", " + font.getSize() + "]"; // NOI18N
        }

        private char myChar;
        private Font myFont;
        private Color myColor;
    }

    private Font myFont;
    private Color myColor;
    private TextLayout myTextLayout;
    private List<Symbol> mySymbols = new ArrayList<Symbol>();
}
