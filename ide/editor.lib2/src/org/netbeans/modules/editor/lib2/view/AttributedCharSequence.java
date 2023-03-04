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
package org.netbeans.modules.editor.lib2.view;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.openide.util.Exceptions;

/**
 * Char sequence with additional attributes.
 * <br>
 * Method operate with javax.swing.text.AttributeSet which they transfer
 * to corresponding TextAttribute constants.
 * <br>
 * After creation the series of addTextRun() is called followed by setText()
 * which completes modification and makes the object ready for clients.
 *
 * @author Miloslav Metelka
 */
public final class AttributedCharSequence implements AttributedCharacterIterator {
    
    private static final Map<Object,AttributeTranslateHandler> attr2Handler
            = new HashMap<Object, AttributeTranslateHandler>(20, 0.5f);
    static {
        attr2Handler.put(StyleConstants.Family, new AttributeTranslateHandler(TextAttribute.FAMILY));
        attr2Handler.put(StyleConstants.Italic,
                new AttributeTranslateHandler(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE));
        attr2Handler.put(StyleConstants.Bold,
                new AttributeTranslateHandler(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD));
        attr2Handler.put(StyleConstants.Size, new AttributeTranslateHandler(TextAttribute.SIZE));

        attr2Handler.put(StyleConstants.Superscript,
                new AttributeTranslateHandler(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER));
        attr2Handler.put(StyleConstants.Subscript,
                new AttributeTranslateHandler(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB));
        attr2Handler.put(StyleConstants.Underline,
                new AttributeTranslateHandler(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON));
        attr2Handler.put(StyleConstants.StrikeThrough, new AttributeTranslateHandler(TextAttribute.STRIKETHROUGH));
        attr2Handler.put(StyleConstants.StrikeThrough, new AttributeTranslateHandler(TextAttribute.STRIKETHROUGH));

        attr2Handler.put(StyleConstants.Foreground, new AttributeTranslateHandler(TextAttribute.FOREGROUND));
        attr2Handler.put(StyleConstants.Background, new AttributeTranslateHandler(TextAttribute.BACKGROUND));
        attr2Handler.put(StyleConstants.Background, new AttributeTranslateHandler(TextAttribute.BACKGROUND));
    }
    
    public static Map<Attribute,Object> translate(AttributeSet attrs) {
        int attrCount = attrs.getAttributeCount();
        Map<Attribute,Object> ret = new HashMap<Attribute, Object>(attrCount);
        for (Enumeration<?> attrNames = attrs.getAttributeNames(); attrNames.hasMoreElements();) {
            Object attrName = attrNames.nextElement();
            AttributeTranslateHandler handler = attr2Handler.get(attrName);
            if (handler != null) {
                Object textAttrValue = handler.getTextAttrValue(attrs.getAttribute(attrName));
                if (textAttrValue != null) {
                    ret.put(handler.getTextAttr(), textAttrValue);
                }
            } // Unknown attributes are ignored
        }
        return ret;
    }
    
    /**
     * Merge textAttrs0 with textAttrs1 (textAttrs1 contents override textAttrs0 for same attributes).
     *
     * @param textAttrs0
     * @param textAttrs1
     * @return merge of two maps.
     */
    public static Map<Attribute,Object> merge(Map<Attribute,Object> textAttrs0, Map<Attribute,Object> textAttrs1) {
        Map<Attribute,Object> ret = new HashMap<Attribute, Object>(textAttrs0);
        ret.putAll(textAttrs1);
        return ret;
    }


    private final List<TextRun> textRuns = new ArrayList<TextRun>(); // 8=super + 4 = 12 bytes

    private CharSequence text; // 12 + 4 = 16 bytes

    private int charIndex; // 16 + 4 = 20 bytes
    
    private int textRunIndex; // 20 + 4 = 24 bytes
    
    private Set<Attribute> allAttributeKeys; // 24 + 4 = 28 bytes

    public AttributedCharSequence() {
    }
    
    void addTextRun(int endIndex, Map<Attribute,Object> textAttrs) {
        if (endIndex <= endIndex()) {
            throw new IllegalArgumentException("endIndex=" + endIndex + ", endIndex()=" + endIndex()); // NOI18N
        }
        textRuns.add(new TextRun(endIndex, textAttrs));
    }
    
    void setText(CharSequence text, Map<Attribute,Object> defaultTextAttrs) {
        this.text = text;
        int endIndex = endIndex();
        int textLen = text.length();
        if (endIndex > textLen) {
            throw new IllegalStateException("endIndex=" + endIndex + " > text.length()=" + textLen); // NOI18N
        }
        if (endIndex < textLen) {
            addTextRun(textLen, defaultTextAttrs);
        }
        ((ArrayList)textRuns).trimToSize();
    }
    
    private int endIndex() {
        return (textRuns.size() > 0) ? textRuns.get(textRuns.size() - 1).endIndex : 0;
    }
    
    @Override
    public int getRunStart() {
        return (textRunIndex > 0)
                ? textRuns.get(textRunIndex - 1).endIndex
                : 0;
    }

    @Override
    public int getRunStart(Attribute attribute) {
        return getRunStart();
    }

    @Override
    public int getRunStart(Set<? extends Attribute> attributes) {
        return getRunStart();
    }

    @Override
    public int getRunLimit() {
        return (textRunIndex < textRuns.size())
                ? textRuns.get(textRunIndex).endIndex
                : textRuns.get(textRuns.size() - 1).endIndex;
    }

    @Override
    public int getRunLimit(Attribute attribute) {
        return getRunLimit();
    }

    @Override
    public int getRunLimit(Set<? extends Attribute> attributes) {
        return getRunLimit();
    }

    @Override
    public Map<Attribute, Object> getAttributes() {
        if (textRunIndex >= textRuns.size()) {
            return Collections.emptyMap();
        }
        return textRuns.get(textRunIndex).attrs;
    }

    @Override
    public Object getAttribute(Attribute attribute) {
        return getAttributes().get(attribute);
    }

    @Override
    public Set<Attribute> getAllAttributeKeys() {
        if (allAttributeKeys == null) {
            HashSet<Attribute> allKeys = new HashSet<Attribute>();
            for (int i = textRuns.size() - 1; i >= 0; i--) {
                allKeys.addAll(textRuns.get(i).attrs.keySet());
            }
            allAttributeKeys = allKeys;
        }
        return allAttributeKeys;
    }

    @Override
    public char first() {
        setIndex(0);
        return current();
    }

    @Override
    public char last() {
        setIndex(Math.max(0, text.length() - 1));
        return current();
    }

    @Override
    public char current() {
        return (charIndex < text.length()) ? text.charAt(charIndex) : DONE;
    }

    @Override
    public char next() {
        if (charIndex == text.length()) {
            return DONE;
        }
        charIndex++;
        if (charIndex == textRuns.get(textRunIndex).endIndex) {
            textRunIndex++;
        }
        return current();
    }

    @Override
    public char previous() {
        if (charIndex == 0) {
            return DONE;
        }
        charIndex--;
        if (textRunIndex > 0 && charIndex < textRuns.get(textRunIndex - 1).endIndex) {
            textRunIndex--;
        }
        return current();
    }

    @Override
    public char setIndex(int position) {
        if (position < 0 || position > text.length()) {
            throw new IllegalArgumentException("position=" + position + " not within <0," + // NOI18N
                    text.length() + ">"); // NOI18N
        }
        return setIndexImpl(position);
    }
    
    private char setIndexImpl(int position) {
        charIndex = position;

        // Find text run
        if (position == 0) {
            textRunIndex = 0;
        } else {
            int last = textRuns.size() - 1;
            int low = 0;
            int high = last;
            while (low <= high) {
                int mid = (low + high) >>> 1; // mid in the binary search
                TextRun textRun = textRuns.get(mid);
                if (textRun.endIndex < position) {
                    low = mid + 1;
                } else if (textRun.endIndex > position) {
                    high = mid - 1;
                } else { // textRun.endIndex == position
                    low = mid + 1;
                    break;
                }
            }
            textRunIndex = Math.min(low, last); // Make sure last item is returned for relOffset above end
        }
        return current();
    }

    @Override
    public int getBeginIndex() {
        return 0;
    }

    @Override
    public int getEndIndex() {
        return text.length();
    }

    @Override
    public int getIndex() {
        return charIndex;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            Exceptions.printStackTrace(ex); // Should never happen
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("text(").append(text.length()).append(")=\"").append( // NOI18N
                CharSequenceUtilities.debugText(text)).append("\"; "). // NOI18N
                append(textRuns.size()).append(" text runs:\n"); // NOI18N
        int textRunsSize = textRuns.size();
        int maxDigitCount = ArrayUtilities.digitCount(textRunsSize);
        for (int i = 0; i < textRunsSize; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, maxDigitCount);
            sb.append(": "); // NOI18N
            textRuns.get(i).appendInfo(sb);
            sb.append("\n"); // NOI18N
        }
        return sb.toString();
    }
    
    private static final class TextRun {
        
        final int endIndex;
        
        final Map<Attribute,Object> attrs;

        public TextRun(int endIndex, Map<Attribute, Object> attrs) {
            this.endIndex = endIndex;
            this.attrs = attrs;
        }

        StringBuilder appendInfo(StringBuilder sb) {
            sb.append("endIndex=").append(endIndex).append(", attrs=").append(attrs); // NOI18N
            return sb;
        }

        @Override
        public String toString() {
            return appendInfo(new StringBuilder()).toString();
        }
        
    }

    private static final class AttributeTranslateHandler {

        AttributeTranslateHandler(Attribute textAttr) {
            this(textAttr, null);
        }

        AttributeTranslateHandler(Attribute textAttr, Object textAttrValue) {
            this.textAttr = textAttr;
            this.textAttrValue = textAttrValue;
        }
        
        private final Attribute textAttr;
        
        private final Object textAttrValue;
        
        Attribute getTextAttr() {
            return textAttr;
        }
        
        Object getTextAttrValue(Object attrValue) {
            Object ret;
            if (textAttrValue == null) { // Use passed value
                ret = attrValue;
            } else { // Translate if Boolean.TRUE
                if (Boolean.TRUE.equals(attrValue)) {
                    ret = textAttrValue;
                } else {
                    ret = null;
                }
            }
            return ret;
        }

    }
}
