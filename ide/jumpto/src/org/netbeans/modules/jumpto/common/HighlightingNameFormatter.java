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
package org.netbeans.modules.jumpto.common;

import java.awt.Color;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 * Highlight the match of text patterns. The matching parts will be placed
 * within HTML-tags, so it can be used easily within the Swing UI.
 * @author markiewb
 */
public class HighlightingNameFormatter {

    private static final String COLOR_FORMAT_PATTERN = "<font style=\"background-color:%s; font-weight:bold; color:%s; white-space:nowrap\">%s</font>"; //NOI18N
    private static final String BOLD_FORMAT_PATTERN = "<b>%s</b>";    //NOI18N
    private static final String BASE_COLOR_FORMAT_PATTERN = "<font color=\"#%s\">%s</font>";    //NOI18N
    private final String formatPattern;
    private final Pattern camelCasePattern;

    private HighlightingNameFormatter(
            @NonNull final String pattern,
            @NullAllowed final String camelCaseStart) {
        Parameters.notNull("pattern", pattern); //NOI18N
        this.formatPattern = pattern;
        this.camelCasePattern = camelCaseStart == null ?
                null :
                Pattern.compile(camelCaseStart);
    }

    @NonNull
    public String formatName(
            @NonNull final String name,
            @NonNull final String textToFind,
            final boolean caseSensitive) {
        if (null == textToFind || "".equals(textToFind)) {
            return name;
        }
        BitSet bitSet = new BitSet(name.length());
        List<String> parts = splitByCamelCaseAndWildcards(textToFind);

        String convertedTypeName = caseSensitive ? name : name.toLowerCase();
        //mark the chars to be highlighted
        int startIndex = 0;
        for (String camelCasePart : parts) {

            int indexOf = convertedTypeName.indexOf(caseSensitive ? camelCasePart : camelCasePart.toLowerCase(), startIndex);
            if (indexOf != -1) {

                //mark the chars 
                bitSet.set(indexOf, indexOf + camelCasePart.length(), true);
            } else {
                break;
            }
            startIndex = indexOf + camelCasePart.length();
        }

        //highlight the marked chars via  tags
        StringBuilder formattedTypeName = new StringBuilder();
        int i = 0;
        while (i < name.length()) {

            boolean isMarked = bitSet.get(i);

            if (isMarked) {
                int numberOfContinuousHighlights = bitSet.nextClearBit(i) - i;
                String part = name.substring(i, i + numberOfContinuousHighlights);
                formattedTypeName.append(String.format(formatPattern, part));
                i += numberOfContinuousHighlights;
            } else {
                formattedTypeName.append(name.charAt(i));
                i++;
            }
        }
        return formattedTypeName.toString();
    }

    @NonNull
    public String formatName(
            @NonNull final String name,
            @NonNull final String textToFind,
            final boolean caseSensitive,
            @NonNull final Color baseColor) {
        final String res = formatName(name, textToFind, caseSensitive);
        return String.format(
            BASE_COLOR_FORMAT_PATTERN,
            Integer.toHexString(baseColor.getRGB()).substring(2),
            res);
    }

    private List<String> splitByCamelCaseAndWildcards(String searchText) {
        //AbcDeFGhiJo -> [Abc, De, F, Ghi, Jo]
        StringBuilder sb = new StringBuilder(searchText.length());
        for (char c : searchText.toCharArray()) {
            if (isCamelCaseStart(c)) {
                //add split marker into text before the uppercase char
                //example: AbcDeFGhiJo -> *Abc*De*F*Ghi*Jo
                sb.append('*'); //NOI18N
                sb.append(c);
            } else if (c == '?') {    //NOI18N
                //replace '?' by '*'
                sb.append('*'); //NOI18N
            } else {
                sb.append(c);
            }
        }
        //split by camelcase (using the split marker) or the wildcards *,?
        String[] split = sb.toString().split("\\*");    //NOI18N
        return Arrays.asList(split);
    }

    private boolean isCamelCaseStart(final char c) {
        if (camelCasePattern == null) {
            return Character.isUpperCase(c);
        } else {
            return camelCasePattern.matcher(Character.toString(c)).matches();
        }
    }

    public static final class Builder {
        private String camelCaseStart;

        private Builder() {}

        @NonNull
        public Builder setCamelCaseSeparator(@NullAllowed final String separatorPattern) {
            this.camelCaseStart = separatorPattern;
            return this;
        }

        @NonNull
        public HighlightingNameFormatter buildColorFormatter(
                @NonNull final Color bgColor,
                @NonNull final Color fgColor) {
            final String bgColorHighlight = Integer.toHexString(bgColor.getRGB()).substring(2);
            final String fgColorHighlight = Integer.toHexString(fgColor.getRGB()).substring(2);
            return new HighlightingNameFormatter(
                String.format(COLOR_FORMAT_PATTERN, bgColorHighlight, fgColorHighlight, "%s"),   //NOI18N
                camelCaseStart);
        }

        @NonNull
        public HighlightingNameFormatter buildBoldFormatter() {
            return new HighlightingNameFormatter(
                String.format(BOLD_FORMAT_PATTERN, "%s"),     //NOI18N
                camelCaseStart);
        }

        @NonNull
        /*test*/ HighlightingNameFormatter buildCustomFormatter(
                @NonNull final String format) {
            return new HighlightingNameFormatter(format, camelCaseStart);
        }

        @NonNull
        public static Builder create () {
            return new Builder();
        }
    }
}