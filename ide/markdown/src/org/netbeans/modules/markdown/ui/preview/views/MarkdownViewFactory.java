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
package org.netbeans.modules.markdown.ui.preview.views;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * This class creates Views for a JEditorPane, it receives and HTML element and
 * you must create the most similar swing component to this html element. For
 * example a HTML input type checkbox, its a JCheckBox, and image can be a
 * JLabel and so on.
 *
 * @author moacirrf
 */
public class MarkdownViewFactory extends HTMLEditorKit.HTMLFactory {

    @Override
    public View create(Element elem) {

        if (isElementOfTag(elem, HTML.Tag.INPUT)) {
            return new CheckboxView(elem);
        }

        return super.create(elem);
    }

    public boolean isElementOfTag(Element elem, HTML.Tag tag) {

        AttributeSet attrs = elem.getAttributes();
        Object elementName
                = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
        Object o = (elementName != null)
                ? null : attrs.getAttribute(StyleConstants.NameAttribute);
        HTML.Tag kind = (HTML.Tag) o;

        return (o instanceof HTML.Tag) && (kind == tag);

    }
}
