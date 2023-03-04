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

package org.netbeans.modules.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.PrintContainer;
import org.netbeans.modules.editor.lib.ColoringMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class HtmlPrintContainer implements PrintContainer {

    private static final String DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"; // NOI18N
    private static final String T_HTML_S = "<html>";    //NOI18N
    private static final String T_HTML_E = "</html>";   //NOI18N
    private static final String T_HEAD_S = "<head>";    //NOI18N
    private static final String T_HEAD_E = "</head>";   //NOI18N
    private static final String T_BODY_S = "<body>";    //NOI18N
    private static final String T_BODY_E = "</body>";   //NOI18N
    private static final String T_TITLE = "<title>{0}</title>";    //NOI18N
    private static final String T_PRE_S = "<pre>";   //NOI18N
    private static final String T_PRE_E = "</pre>";  //NOI18N
    private static final String T_BLOCK_S = "<span class=\"{0}\">";  //NOI18N
    private static final String T_BLOCK_E = "</span>";   //NOI18N
    private static final String T_NAME_TABLE = "<table width=\"100%\"><tr><td align=\"center\">{0}</td></tr></table>";    //NOI18N
    private static final String T_CHARSET = "<meta http-equiv=\"content-type\" content=\"text/html; charset={0}\">";    //NOI18N
    private static final String T_STYLE_S = "<style type=\"text/css\">";    //NOI18N
    private static final String T_STYLE_E = "</style>"; //NOI18N
    private static final String T_COMMENT_S = "<!--";   //NOI18N
    private static final String T_COMMENT_E = "-->";    //NOI18N
    private static final String ST_BODY = "body";       //NOI18N
    private static final String ST_PRE = "pre";         //NOI18N
    private static final String ST_TABLE = "table";     //NOI18N
    private static final String ST_BEGIN = "{";        //NOI18N
    private static final String ST_COLOR = "color: "; //NOI18N
    private static final String ST_BGCOLOR = "background-color: ";    //NOI18N
    private static final String ST_BOLD = "font-weight: bold";    //NOI18N
    private static final String ST_ITALIC = "font-style: italic"; //NOI18N
    private static final String ST_SIZE = "font-size: "; //NOI18N
    private static final String ST_FONT_FAMILY = "font-family: ";    //NOI18N
    private static final String ST_SEPARATOR = "; ";    //NOI18N
    private static final String ST_END = "}";           //NOI18N
    private static final String EOL = "\n";             //NOI18N
    private static final String WS = " ";               //NOI18N
    private static final String ESC_LT = "&lt;";        //NOI18N
    private static final String ESC_GT = "&gt;";        //NOI18N
    private static final String ESC_AMP = "&amp;";      //NOI18N
    private static final String ESC_QUOT = "&quot;";    //NOI18N
    private static final String ESC_APOS = "&#39;"; // IZ #74203 "&apos;";    //NOI18N
    private static final String FF_SANSSERIF = "sans-serif"; //NOI18N
    private static final String FF_SERIF = "serif";          //NOI18N
    private static final String FF_MONOSPACE = "monospace";  //NOI18N
    private static final char   ZERO    = '0';          //NOI18N
    private static final char   DOT = '.';              //NOI18N
    private static final String STYLE_PREFIX = "ST";    //NOI18N

    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;
    private Color headerBackgroundColor;
    private Color headerForegroundColor;
    private Font defaultFont;
    private StringBuffer buffer;
    private String fileName;
    private String shortFileName;
    private Styles styles;
    private boolean[] boolHolder;
    private Map syntaxColoring;
    private String charset;

    public HtmlPrintContainer () {
    }

    public final void begin (FileObject fo, Font font, Color fgColor, Color bgColor, Color hfgColor, Color hbgColor, Class kitClass, String charset) {
        begin(fo, font, fgColor, bgColor, hfgColor, hbgColor, MimePath.parse(BaseKit.getKit(kitClass).getContentType()), charset);
    }

    /* package */ final void begin (FileObject fo, Font font, Color fgColor, Color bgColor, Color hfgColor, Color hbgColor, MimePath mimePath, String charset) {
        styles = new Styles ();
        buffer = new StringBuffer();
        fileName = FileUtil.getFileDisplayName(fo);
        shortFileName = fo.getNameExt();
        boolHolder = new boolean [1];
        this.defaultForegroundColor = fgColor;
        this.defaultBackgroundColor = bgColor;
        this.defaultFont = font;
        this.headerForegroundColor = hfgColor;
        this.headerBackgroundColor = hbgColor;
        this.syntaxColoring = ColoringMap.get(mimePath.getPath()).getMap();
        this.charset = charset;
    }

    public void addLines(List<AttributedCharacterIterator> lines) {
        for (int i = 0; i < lines.size(); i++) {
            AttributedCharacterIterator line = lines.get(i);
            int endIndex = line.getEndIndex();
            int index = 0;
            line.setIndex(0);
            while (index < endIndex) { // Service a single text run
                Font font = (Font) line.getAttribute(TextAttribute.FONT);
                if (font == null) {
                    String family = (String) line.getAttribute(TextAttribute.FAMILY);
                    boolean italic = TextAttribute.POSTURE_OBLIQUE.equals(line.getAttribute(TextAttribute.POSTURE));
                    boolean bold = TextAttribute.WEIGHT_BOLD.equals(line.getAttribute(TextAttribute.WEIGHT));
                    Integer size = (Integer) line.getAttribute(TextAttribute.SIZE);
                    // Both family and size are expected to be non-null
//                    assert (family != null && size != null) : "family=" + family + ", size=" + size; // NOI18N
                    if (family != null && size != null) {
                        font = new Font(family, (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), size);
                    } else {
                        font = getDefaultFont();
                    }
                }
                Color foreground = (Color) line.getAttribute(TextAttribute.FOREGROUND);
                if (foreground == null) {
                    foreground = getDefaultColor();
                }
                Color background = (Color) line.getAttribute(TextAttribute.BACKGROUND);
                if (background == null) {
                    background = getDefaultBackgroundColor();
                }
                // Get text
                int runEndIndex = line.getRunLimit();
                char text[] = new char[runEndIndex - index];
                int j = 0;
                while (index < runEndIndex) {
                    // First char is current
                    text[j] = (j == 0) ? line.current() : line.next();
                    j++;
                    index++;
                }
                line.next(); // One char forward in order to make line.current() in next iter
                add(text, font, foreground, background);
            }
            eol();
        }
    }

    public final void add(char[] chars, Font font, Color foreColor, Color backColor) {
        String text = escape(chars, boolHolder);
        String styleId = this.styles.getStyleId (font, foreColor, backColor);
        boolHolder[0]&= (styleId!=null);
        if (boolHolder[0]) {
            buffer.append(MessageFormat.format(T_BLOCK_S,new Object[]{styleId}));
        }
        buffer.append (text);
        if (boolHolder[0]) {
            buffer.append(T_BLOCK_E);
        }
    }

    public final void eol() {
        buffer.append (EOL);
    }

    public final String end () {
        StringBuffer result = new StringBuffer ();
        result.append (DOCTYPE);
        result.append (EOL);
        result.append (T_HTML_S);
        result.append (EOL);
        result.append (T_HEAD_S);
        result.append (EOL);
        result.append (MessageFormat.format (T_TITLE, new Object[] {this.shortFileName}));
        result.append (EOL);
        result.append (MessageFormat.format (T_CHARSET, new Object[] {this.charset}));
        result.append (EOL);
        result.append (T_STYLE_S);
        result.append (EOL);
        result.append (T_COMMENT_S);
        result.append (EOL);
        result.append (createStyle(ST_BODY,null,getDefaultFont(),getDefaultColor(),getDefaultBackgroundColor(),false));
        result.append (EOL);
        result.append (createStyle(ST_PRE,null,getDefaultFont(),getDefaultColor(),getDefaultBackgroundColor(),false));
        result.append (EOL);
        result.append (createStyle(ST_TABLE,null,getDefaultFont(),headerForegroundColor,headerBackgroundColor,false));
        result.append (EOL);
        result.append (styles.toExternalForm());
        result.append (T_COMMENT_E);
        result.append (EOL);
        result.append (T_STYLE_E);
        result.append (EOL);
        result.append (T_HEAD_E);
        result.append (EOL);
        result.append (T_BODY_S); //NOI18N
        result.append (EOL);
        result.append (MessageFormat.format (T_NAME_TABLE, new Object[] {this.fileName}));
        result.append (EOL);
        result.append (T_PRE_S);
        result.append (EOL);
        result.append (this.buffer);
        result.append (T_PRE_E);
        result.append (T_BODY_E);
        result.append (EOL);
        result.append (T_HTML_E);
        result.append (EOL);
        this.styles = null;
        this.buffer = null;
        this.fileName = null;
        this.shortFileName = null;
        this.defaultBackgroundColor = null;
        this.defaultForegroundColor = null;
        this.defaultFont = null;
        return result.toString();
    }

    public final boolean initEmptyLines() {
        return false;
    }

    private String escape (char[] buffer, boolean[] boolHolder) {
        StringBuffer result = new StringBuffer();
        boolHolder[0] = false;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == '<') {         //NOI18N
                result.append(ESC_LT);
                boolHolder[0]|=true;
            }
            else if (buffer[i] == '>') {    //NOI18N
                result.append(ESC_GT);
                boolHolder[0]|=true;
            }
            else if (buffer[i] =='&') {     //NOI18N
                result.append(ESC_AMP);
                boolHolder[0]|=true;
            }
            else if (buffer[i] =='\'') {    //NOI18N
                result.append(ESC_APOS);
                boolHolder[0]|=true;
            }
            else if (buffer[i] =='\"') {    //NOI18N
                result.append(ESC_QUOT);
                boolHolder[0]|=true;
            }
            else if (Character.isWhitespace(buffer[i])) {
                result.append (buffer[i]);
            }
            else {
                result.append (buffer[i]);
                boolHolder[0]|=true;
            }
        }
        return result.toString();
    }

    private Color getDefaultColor () {
        return this.defaultForegroundColor;
    }

    private Color getDefaultBackgroundColor () {
        return this.defaultBackgroundColor;
    }

    private Font getDefaultFont () {
        return this.defaultFont;
    }

    private String createStyle (String element, String selector, Font font, Color fg, Color bg, boolean useDefaults) {
        StringBuffer sb = new StringBuffer();
        if (element != null) {
            sb.append (element);
            sb.append (WS);
        }

        if (selector != null) {
            sb.append (DOT);
            sb.append (selector);
            sb.append (WS);
        }

        sb.append (ST_BEGIN);
        boolean first = true;
        if ((!useDefaults || !fg.equals(getDefaultColor())) && fg != null) {
            sb.append (ST_COLOR);
            sb.append (getHtmlColor(fg));
            first = false;
        }

        if ((!useDefaults || !bg.equals (getDefaultBackgroundColor())) && bg != null) {
            if (!first) {
                sb.append (ST_SEPARATOR);
            }
            sb.append (ST_BGCOLOR);
            sb.append (getHtmlColor(bg));
            first = false;
        }

        if ((!useDefaults || !font.equals (getDefaultFont())) && font != null) {
            if (!first) {
                sb.append (ST_SEPARATOR);
            }
            sb.append (ST_FONT_FAMILY);
            switch(font.getFamily()) {
                case Font.MONOSPACED:
                    sb.append (FF_MONOSPACE);
                    break;
                case Font.SERIF:
                    sb.append (FF_SERIF);
                    break;
                case Font.SANS_SERIF:
                    sb.append (FF_SANSSERIF);
                    break;
                case Font.DIALOG:
                    sb.append (FF_SANSSERIF);
                    break;
                case Font.DIALOG_INPUT:
                    sb.append (FF_MONOSPACE);
                    break;
                default:
                    sb.append (font.getFamily()); //TODO: Locale should go here
            }
            if (font.isBold()) {
                sb.append (ST_SEPARATOR);
                sb.append (ST_BOLD);
            }
            if (font.isItalic()) {
                sb.append (ST_SEPARATOR);
                sb.append (ST_ITALIC);
            }
            Font df = getDefaultFont();
            if (df != null && df.getSize() != font.getSize()) {
                sb.append (ST_SEPARATOR);
                sb.append (ST_SIZE);
                sb.append (String.valueOf(font.getSize()));
            }

        }
        sb.append (ST_END);
        return sb.toString();
    }

    private static String getHtmlColor (Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        StringBuffer result = new StringBuffer();
        result.append ("#");        //NOI18N
        String rs = Integer.toHexString (r);
        String gs = Integer.toHexString (g);
        String bs = Integer.toHexString (b);
        if (r < 0x10)
            result.append(ZERO);
        result.append(rs);
        if (g < 0x10)
            result.append (ZERO);
        result.append(gs);
        if (b < 0x10)
            result.append (ZERO);
        result.append(bs);
        return result.toString();
    }

    private class Styles {
        private Map<StyleDescriptor, String> descs;
        private int sequence;

        public Styles () {
            this.descs = new HashMap<StyleDescriptor, String>();
        }

        private boolean coloringEquals(Coloring coloring, Font f, Color fc, Color bc){
            if (coloring == null) return false;
            Font coloringFont = coloring.getFont();
            if (coloringFont == null) coloringFont = getDefaultFont();
            Color coloringForeColor = coloring.getForeColor();
            if (coloringForeColor == null) coloringForeColor = getDefaultColor();
            Color coloringBackColor = coloring.getBackColor();
            if (coloringBackColor == null) coloringBackColor = getDefaultBackgroundColor();

            return f.equals(coloringFont) && fc.equals(coloringForeColor) && bc.equals(coloringBackColor);
        }

        public final String getStyleId (Font f, Color fc, Color bc) {
            if (!fc.equals(getDefaultColor()) || !bc.equals(getDefaultBackgroundColor()) || !f.equals(getDefaultFont())) {
                StyleDescriptor sd = new StyleDescriptor (f, fc, bc);
                String id = this.descs.get(sd);
                if (id == null) {
                    java.util.Set keySet = syntaxColoring.keySet();
                    Iterator iter = keySet.iterator();
                    while(iter.hasNext()){
                        Object key = iter.next();
                        if (coloringEquals((Coloring)syntaxColoring.get(key), f, fc, bc)){
                            id = (String) key;
                            break;
                        }
                    }

                    if (id == null){
                        id = STYLE_PREFIX + this.sequence++;
                    }
                    sd.name = id;
                    this.descs.put (sd, id);
                }
                return id;
            }
            else {
                return null;   //No style needed
            }
        }

        public final String toExternalForm () {
            StringBuffer result = new StringBuffer();
            for(StyleDescriptor sd : descs.keySet()) {
                result.append(sd.toExternalForm());
                result.append(EOL);
            }
            return result.toString();
        }

        public @Override final String toString () {
            return this.toExternalForm();
        }

        private class StyleDescriptor {

            String name;
            private Font font;
            private Color fgColor;
            private Color bgColor;

            public StyleDescriptor (Font font, Color fgColor, Color bgColor) {
                this.font = font;
                this.fgColor = fgColor;
                this.bgColor = bgColor;
            }

            public final String getName () {
                return this.name;
            }

            public final String toExternalForm () {
                return createStyle (null,name,font,fgColor,bgColor,true);
            }

            public @Override final String toString () {
                return this.toExternalForm();
            }

            public @Override final boolean equals (Object object) {
                if (!(object instanceof StyleDescriptor))
                    return false;
                StyleDescriptor od = (StyleDescriptor) object;
                return coloringEquals(new Coloring(font, fgColor, bgColor), od.font, od.fgColor, od.bgColor);
            }

            public @Override final int hashCode () {
                return this.font.hashCode() ^ this.fgColor.hashCode() ^ this.bgColor.hashCode();
            }
        } // End of StyleDescriptor class
    }
}
