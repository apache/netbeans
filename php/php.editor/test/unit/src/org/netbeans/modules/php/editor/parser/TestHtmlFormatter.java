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
package org.netbeans.modules.php.editor.parser;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;


/**
 * Formatter just for testing purposses
 *
 * @author Petr Pisl
 */
public class TestHtmlFormatter extends HtmlFormatter {

    private StringBuilder sb = new StringBuilder();

    @Override
    public void reset() {
        sb.setLength(0);
    }

    @Override
    public void appendHtml(String html) {
        sb.append(html);
    }

    @Override
    public void appendText(String text, int fromInclusive, int toExclusive) {
        sb.append("ESCAPED{");
        sb.append(text, fromInclusive, toExclusive);
        sb.append("}");
    }

    @Override
    public void name(ElementKind kind, boolean start) {
        if (start) {
            sb.append(kind);
        }
    }

    @Override
    public void active(boolean start) {
        if (start) {
            sb.append("ACTIVE{");
        } else {
            sb.append("}");
        }
    }

    @Override
    public void parameters(boolean start) {
        if (start) {
            sb.append("PARAMETERS{");
        } else {
            sb.append("}");
        }
    }

    @Override
    public void type(boolean start) {
        if (start) {
            sb.append("TYPE{");
        } else {
            sb.append("}");
        }
    }

    @Override
    public void deprecated(boolean start) {
        if (start) {
            sb.append("DEPRECATED{");
        } else {
            sb.append("}");
        }
    }

    @Override
    public String getText() {
        return sb.toString();
    }

    @Override
    public void emphasis(boolean start) {
    }
}
