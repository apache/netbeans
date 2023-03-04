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
package org.netbeans.modules.php.latte.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@MimeRegistration(mimeType = LatteLanguage.LATTE_MIME_TYPE, service = HtmlExtension.class)
public class LatteHtmlExtension extends HtmlExtension {
    private static final Collection<CustomAttribute> CUSTOM_ATTRIBUTES = new ArrayList<>();
    static {
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:href")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:class")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:input")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:attr")); //NOI18N

        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:if")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:ifset")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:ifCurrent")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:for")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:foreach")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:while")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:cache")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:snippet")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:block")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:define")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:capture")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:syntax")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:form")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:label")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:input")); //NOI18N

        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-if")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-ifset")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-ifCurrent")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-for")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-foreach")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-while")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-cache")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-snippet")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-block")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-define")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-capture")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-syntax")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-form")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-label")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-input")); //NOI18N

        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-if")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-ifset")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-ifCurrent")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-for")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-foreach")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-while")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-cache")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-snippet")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-block")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-define")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-capture")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-syntax")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-form")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-label")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-input")); //NOI18N
    }

    @Override
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
        return attribute.name().toString().startsWith("n:"); //NOI18N
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes(String elementName ) {
        return CUSTOM_ATTRIBUTES;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        // COPIED FROM AngularHtmlExtension
        /********** ++ **********/
        List<CompletionItem> items = new ArrayList<>();
        Element element = context.getCurrentNode();
        if (element != null) {
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    String name = ot.unqualifiedName().toString();
                    Collection<CustomAttribute> customAttributes = getCustomAttributes(name);
                    for (CustomAttribute ca : customAttributes) {
                        items.add(new LatteAttributeCompletionItem(ca, context.getCCItemStartOffset()));
                    }
                    break;
                default:
                    // no-op
            }
        }
        if (context.getPrefix().length() > 0) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                CharSequence insertPrefix = itr.next().getInsertPrefix();
                if (insertPrefix != null) {
                    if (!LexerUtils.startsWith(insertPrefix, context.getPrefix(), true, false)) {
                        itr.remove();
                    }
                }
            }
        }
        return items;
        /********** -- **********/
    }

}
