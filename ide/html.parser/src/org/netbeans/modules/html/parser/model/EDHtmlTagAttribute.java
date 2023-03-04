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
package org.netbeans.modules.html.parser.model;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.html.editor.lib.api.DefaultHelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttributeType;
import org.netbeans.modules.html.parser.HtmlDocumentation;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class EDHtmlTagAttribute implements HtmlTagAttribute {

    private Attribute attr;

    public EDHtmlTagAttribute(Attribute name) {
        this.attr = name;
    }

    @Override
    public String getName() {
        return attr.getName();
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public HtmlTagAttributeType getType() {
        return HtmlTagAttributeType.GENERIC;
    }

    @Override
    public Collection<String> getPossibleValues() {
        return Collections.emptyList();
    }

    @Override
    public HelpItem getHelp() {
        StringBuilder header = new StringBuilder();
        header.append("<h2>");//NOI18N
        header.append(NbBundle.getMessage(HtmlTagProvider.class, "MSG_AttributePrefix"));//NOI18N
        header.append(" '");//NOI18N
        header.append(attr.getName());
        header.append("'</h2>");//NOI18N

        return new DefaultHelpItem(
                HtmlDocumentation.getDefault().resolveLink(attr.getHelpLink()),
                HtmlDocumentation.getDefault(),
                header.toString());
    }
}
