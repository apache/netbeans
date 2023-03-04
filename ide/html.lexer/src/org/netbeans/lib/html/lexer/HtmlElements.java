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
package org.netbeans.lib.html.lexer;

import java.util.Arrays;

final class HtmlElements {

    private static final String[] TAGS = {
        "a", "abbr", "acronym", "address", "area", "b", "base", "bdo", "big",
        "blockquote", "body", "br", "button", "caption", "cite", "code", "col",
        "colgroup", "dd", "del", "dfn", "div", "dl", "dt", "em", "fieldset",
        "form", "h1", "h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "i",
        "img", "input", "ins", "kbd", "label", "legend", "li", "link", "map",
        "meta", "noscript", "object", "ol", "optgroup", "option", "p", "param",
        "pre", "q", "samp", "script", "select", "small", "span", "strong", "style",
        "sub", "sup", "table", "tbody", "td", "textarea", "tfoot", "th", "thead",
        "title", "tr", "tt", "ul", "var"
    };
    private static final String[] ATTRS = {
        "abbr", "accept", "accept-charset", "accesskey", "action", "align", "alt",
        "archive", "axis", "border", "cellpadding", "cellspacing", "char", "charoff",
        "charset", "checked", "cite", "class", "classid", "codebase", "codetype",
        "cols", "colspan", "content", "coords", "data", "datapagesize", "datetime",
        "declare", "defer", "dir", "disabled", "enctype", "event", "for", "frame",
        "headers", "height", "href", "hreflang", "http-equiv", "id", "ismap", "label",
        "lang", "longdesc", "maxlength", "media", "method", "multiple", "name",
        "nohref", "onblur", "onchange", "onclick", "ondblclick", "onfocus", "onkeydown",
        "onkeypress", "onkeyup", "onload", "onmousedown", "onmousemove", "onmouseout",
        "onmouseover", "onmouseup", "onreset", "onselect", "onsubmit", "onunload",
        "profile", "readonly", "rel", "rev", "rows", "rowspan", "rules", "scheme",
        "scope", "selected", "shape", "size", "span", "src", "standby", "style",
        "summary", "tabindex", "title", "type", "usemap", "valign", "value",
        "valuetype", "width" //NOI18N
    };
    private static final int INDEX_LEN = (int) 'z' - (int) 'a';
    private static final int INDEX_BASE = (int) 'a';
    private static final int[][] TAGS_INDEX = new int[INDEX_LEN][2];
    private static final int[][] ATTRS_INDEX = new int[INDEX_LEN][2];

    private static void index(String[] source, int[][] index) {
        for (int i = 0; i < index.length; i++) {
            Arrays.fill(index[i], -1);
        }
        //compute first char index
        for (int i = 0; i < source.length; i++) {
            char first = source[i].charAt(0);
            int cAscii = (int) first;
            int cIndex = cAscii - INDEX_BASE;
            if (index[cIndex][0] == -1) {
                index[cIndex][0] = i;
                index[cIndex][1] = 1;
            } else {
                index[cIndex][1]++;
            }
        }
    }

    static {
        index(TAGS, TAGS_INDEX);
        index(ATTRS, ATTRS_INDEX);
    }

    public static String getCachedTagName(CharSequence tagName) {
        return getCachedString(TAGS, TAGS_INDEX, tagName);
    }

    public static String getCachedAttrName(CharSequence attrName) {
        return getCachedString(ATTRS, ATTRS_INDEX, attrName);
    }

    private static String getCachedString(String[] source, int[][] index, CharSequence text) {
        int key = (int) text.charAt(0) - INDEX_BASE;
        if (key < 0 || key >= index.length) {
            return null;
        }

        int low = index[key][0];
        int high = low + index[key][1] - 1;
        if (low == -1) {
            return null;
        }

        if (text.length() == 1 && source[low].length() == 1) {
            return source[low];
        }

        //binary search on each char
        int match = -1;
        int j;
        for (j = 1; j < text.length(); j++) {
            char c = text.charAt(j);

            match = -1;
            boolean counting = false;
            for (int i = low; i <= high; i++) {
                String tag = source[i];
                int len = tag.length();
                if (len <= j) {
                    if (!counting) {
                        low = i;
                    } else {
                        high = i;
                    }

                } else {

                    if (tag.charAt(j) == c) {
                        if (!counting) {
                            match = low = i;
                            counting = true;
                        } else {
                            match = -1; //more occurrences
                        }
                    } else {
                        if (counting) {
                            high = i - 1;
                            break;
                        }
                    }
                }
            }

            if (!counting) {
                return null;
            }
        }

        return match != -1 ? (j == source[match].length() ? source[match] : null) : null;


    }
}
