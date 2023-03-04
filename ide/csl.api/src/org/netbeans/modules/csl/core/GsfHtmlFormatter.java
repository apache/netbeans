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
package org.netbeans.modules.csl.core;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;


/**
 *
 * @author Tor Norbye
 */
public class GsfHtmlFormatter extends HtmlFormatter {
    protected boolean isDeprecated;
    protected boolean isParameter;
    protected boolean isType;
    protected boolean isName;
    protected boolean isEmphasis;

    protected StringBuilder sb = new StringBuilder();

    public GsfHtmlFormatter() {
    }

    public void reset() {
        textLength = 0;
        sb.setLength(0);
    }

    public void appendHtml(String html) {
        sb.append(html);
        // Not sure what to do about maxLength here... but presumably
    }

    public void appendText(String text, int fromInclusive, int toExclusive) {
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (textLength >= maxLength) {
                if (textLength == maxLength) {
                    sb.append("...");
                    textLength += 3;
                }
                break;
            }
            char c = text.charAt(i);

            switch (c) {
            case '<':
                sb.append("&lt;"); // NOI18N

                break;

            case '>': // Only ]]> is dangerous
                if ((i > 1) && (text.charAt(i - 2) == ']') && (text.charAt(i - 1) == ']')) {
                    sb.append("&gt;"); // NOI18N
                } else {
                    sb.append(c);
                }
                break;

            case '&':
                sb.append("&amp;"); // NOI18N

                break;

            default:
                sb.append(c);
            }
            
            textLength++;
        }
    }

    public void name(ElementKind kind, boolean start) {
        assert start != isName;
        isName = start;

        if (isName) {
            sb.append("<b>");
        } else {
            sb.append("</b>");
        }
    }

    public void parameters(boolean start) {
        assert start != isParameter;
        isParameter = start;

        if (isParameter) {
            sb.append("<font color=\"#808080\">");
        } else {
            sb.append("</font>");
        }
    }

    @Override
    public void active(boolean start) {
        emphasis(start);
    }

    public void type(boolean start) {
        assert start != isType;
        isType = start;

        if (isType) {
            sb.append("<font color=\"#808080\">");
        } else {
            sb.append("</font>");
        }
    }

    public void deprecated(boolean start) {
        assert start != isDeprecated;
        isDeprecated = start;

        if (isDeprecated) {
            sb.append("<s>");
        } else {
            sb.append("</s>");
        }
    }

    public String getText() {
        assert !isParameter && !isDeprecated && !isName && !isType;

        return sb.toString();
    }

    public void emphasis(boolean start) {
        assert start != isEmphasis;
        isEmphasis = start;

        if (isEmphasis) {
            sb.append("<b>");
        } else {
            sb.append("</b>");
        }
    }

}
