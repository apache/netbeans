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

package org.netbeans.modules.xml.text.syntax.dom;

import org.netbeans.editor.TokenItem;

/**
 * Library of shared complexities.
 *
 * @author  Petr Kuzel
 * @author  asgeir@dimonsoftware.com
 */
public class Util {

    public static String[] knownEntityStrings = {"&lt;", "&gt;", "&apos;", "&quot;", "&amp;"};

    public static char[] knownEntityChars = {'<', '>', '\'', '"', '&'};

    /**
     * Handle fuzziness of attribute end detection.
     * @return TokenItem after attribute value or null.
     */
    public static TokenItem skipAttributeValue(TokenItem attribute, char delim) {
        TokenItem next = attribute;
        for (; next != null; next = next.getNext()) {
            String image = next.getImage();
            if (image.endsWith("" + delim)) {
                return next.getNext();
            }
        }
        return null;
    }
    
    /**
     * This method looks for '<' and '>' characters in attributes values and
     * returns whitespace-stripped substring which does not contain '<' or '>'.
     * This method should be used to calculate an attribute value which has
     * not currently been closed.
     * @param attributeValue an original attribute value
     * @return the same value of stripped substring of it.
     */
    public static String actualAttributeValue(String attributeValue) {
        return org.netbeans.modules.xml.text.api.XMLTextUtils.actualAttributeValue(attributeValue);
    }
    
    /**
     * Replaces "&lt;", "&gt;", "&apos;", "&quot;", "&amp;" with
     * '<', '>', '\'', '"', '&'.
     * @param a string that may contain &lt;", "&gt;", "&apos;", "&quot;" and "&amp;"
     * @return a string that may contain '<', '>', '\'', '"', '&'.
     */
    public static String replaceEntityStringsWithChars(String value) {
        return org.netbeans.modules.xml.text.api.XMLTextUtils.replaceCharsWithEntityStrings(value);
    }
    
    /**
     * Replaces '<', '>', '\'', '"', '&' with
     * "&lt;", "&gt;", "&apos;", "&quot;", "&amp;".
     * @param a string that may contain '<', '>', '\'', '"', '&'.
     * @return a string that may contain &lt;", "&gt;", "&apos;", "&quot;" and "&amp;"
     */
    public static String replaceCharsWithEntityStrings(String value) {
        return org.netbeans.modules.xml.text.api.XMLTextUtils.replaceCharsWithEntityStrings(value);
    }
    
}
