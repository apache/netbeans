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
package org.netbeans.api.java.source.ui.snippet;

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

    public List<MarkupTag> extract(String tagLine) {

        int tagLineLength = tagLine.length();
        charBuffer = new char[tagLineLength + 1];
        tagLine.getChars(0, tagLineLength, charBuffer, 0);
        bufferLength = tagLineLength;
        bufferPointer = -1;

        nextChar();
        return extractTag();
    }

    private List<MarkupTag> extractTag() {
        List<MarkupTag> markUpTags = new ArrayList<>();
        while (bufferPointer < bufferLength) {
            if (currentChar == MARKUP_TAG_START_CHAR) {
                markUpTags.add(readMarkUpTag());
            } else {
                nextChar();
            }
        }

        return markUpTags;
    }

    private MarkupTag readMarkUpTag() {
        nextChar();
        int nameBufferPointer = bufferPointer;
        String tagName = readMarkUpTagName();
        skipWhitespace();

        boolean isTagApplicableToNextLine = false;
        List<MarkupTagAttribute> markUpTagAttributes = new ArrayList<>();

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

        return new MarkupTag(tagName, markUpTagAttributes, isTagApplicableToNextLine);
    }

    private List<MarkupTagAttribute> getAllMarkUpTagAttributes() {
        List<MarkupTagAttribute> attrs = new ArrayList<>();
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
            MarkupTagAttribute markUpTagAttribute = new MarkupTagAttribute(attributeName, nameStartPos, value.toString(), valueStartPos);

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
