package org.netbeans.api.java.source.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SnippetMarkupTagExtractor {

    private int bufferPointer;
    private int bufferLength;
    private char currentChar;
    private char[] charBuffer;
    private static final char MARKUP_TAG_START_CHAR = '@';

    public List<MarkUpTag> extract(String tagLine) {

        int tagLineLength = tagLine.length();
        charBuffer = new char[tagLineLength + 1];
        tagLine.getChars(0, tagLineLength, charBuffer, 0);
        bufferLength = tagLineLength;
        bufferPointer = -1;

        nextChar();
        return extractTag();
    }

    private List<MarkUpTag> extractTag() {
        List<MarkUpTag> markUpTags = new ArrayList<>();
        while (bufferPointer < bufferLength) {
            if (currentChar == MARKUP_TAG_START_CHAR) {
                markUpTags.add(readMarkUpTag());
            } else {
                nextChar();
            }
        }

        return markUpTags;
    }

    private MarkUpTag readMarkUpTag() {
        nextChar();
        int nameBufferPointer = bufferPointer;
        String tagName = readMarkUpTagName();
        skipWhitespace();

        boolean isTagApplicableToNextLine = false;
        List<MarkUpTagAttribute> markUpTagAttributes = new ArrayList<>();

        if (currentChar == ':') {// @highlight: regex = "\barg\b", for this markup tag consider only the tag i.e.
            // highlight and skipped all attributes
            isTagApplicableToNextLine = true;
            nextChar();
        } else {
            markUpTagAttributes = getAllMarkUpTagAttributes();
            skipWhitespace();
            if (currentChar == ':') {//This colon check is for end of the markup tag attributes
                isTagApplicableToNextLine = true;
                nextChar();
            }
        }

        return new MarkUpTag(tagName, markUpTagAttributes, isTagApplicableToNextLine);
    }

    private List<MarkUpTagAttribute> getAllMarkUpTagAttributes() {
        List<MarkUpTagAttribute> attrs = new ArrayList<>();
        skipWhitespace();

        while (bufferPointer < bufferLength && Character.isUnicodeIdentifierStart(currentChar)) {
            StringBuilder value = new StringBuilder();
            int nameStartPos = bufferPointer;
            String attributeName = readAttributeName();
            skipWhitespace();
            int valueStartPos = -1;
            // what if value start with ==, instead of = ?, handled
            if (currentChar == '=') {
                nextChar();
                skipWhitespace();
                if (currentChar == '\'' || currentChar == '"') {
                    char charQuote = currentChar;
                    nextChar();
                    valueStartPos = bufferPointer;
                    while (bufferPointer < bufferLength && currentChar != charQuote) {
                        nextChar();
                    }
                    if (bufferPointer >= bufferLength) {
                        return null;//return meaning ful message here
                    }
                    addRemainingText(value, valueStartPos, bufferPointer - 1);
                    nextChar();
                } else {
                    valueStartPos = bufferPointer;
                    while (bufferPointer < bufferLength && !isValueTerminateWithoutQuote(currentChar)) {
                        nextChar();
                    }
                    addRemainingText(value, valueStartPos, bufferPointer - 1);
                }
                skipWhitespace();
            }
            //some attribute doesn't have value e.g. // @highlight region
            MarkUpTagAttribute markUpTagAttribute = new MarkUpTagAttribute(attributeName, nameStartPos, value.toString(), valueStartPos);

            attrs.add(markUpTagAttribute);
        }
        return attrs;
    }

    private void addRemainingText(StringBuilder b, int textStart, int textEnd) {
        if (textStart != -1) {
            if (textStart <= textEnd) {
                b.append(charBuffer, textStart, (textEnd - textStart) + 1);
            }
        }
    }

    private String readAttributeName() {
        return readName();
    }

    private String readMarkUpTagName() {
        return readName();
    }

    private String readName() {
        int start = bufferPointer;
        nextChar();
        while (bufferPointer < bufferLength && (Character.isUnicodeIdentifierPart(currentChar) || currentChar == '-')) {
            nextChar();
        }
        return new String(charBuffer, start, bufferPointer - start);
    }

    private static boolean isValueTerminateWithoutQuote(char ch) {
        return Arrays.asList(':', '\t', ' ', '"', '`', '\'', '=', '>', '<').contains(ch);
    }

    private void skipWhitespace() {
        while (bufferPointer < bufferLength && Character.isWhitespace(currentChar)) {
            nextChar();
        }
    }

    private void nextChar() {
        currentChar = charBuffer[bufferPointer < bufferLength ? ++bufferPointer : bufferLength];
    }
}
