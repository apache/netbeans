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
package org.netbeans.modules.html.angular;

import org.netbeans.modules.html.angular.model.AngularModel;
import org.netbeans.modules.html.angular.model.Directive;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 * AngularJS extension to the html editor.
 *
 * @author marekfukala
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/xhtml", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-jsp", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-tag", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-php5", service = HtmlExtension.class)
})
public class AngularHtmlExtension extends HtmlExtension {

    @Override
    public boolean isApplicationPiece(HtmlParserResult result) {
        return AngularModel.getModel(result).isAngularPage();
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event) {
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();
        AngularModel model = AngularModel.getModel(result);
        for(Attribute ngAttr : model.getNgAttributes()) {
            OffsetRange dor = Utils.getValidDocumentOffsetRange(ngAttr.from(), ngAttr.from() + ngAttr.name().length(), result.getSnapshot());
            if(dor != null) {
                highlights.put(dor, ColoringAttributes.CONSTRUCTOR_SET);
            }
        }
        return highlights;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        AngularModel model = AngularModel.getModel(context.getResult());
        List<CompletionItem> items = new ArrayList<>();
        Element element = context.getCurrentNode();

        if (element != null) {
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    String name = ot.unqualifiedName().toString();
                    Collection<CustomAttribute> customAttributes = AngularCustomAttribute.getCustomAttributes(model.getPrevailingAttributeConvention());
                    for(CustomAttribute ca : customAttributes) {
                        items.add(new AngularAttributeCompletionItem(ca, context.getCCItemStartOffset(), model.isAngularPage()));
                    }
                    break;
            }
        }

        //XXX copied - needs more elegant solution!
        if (context.getPrefix().length() > 0) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                CharSequence insertPrefix = itr.next().getInsertPrefix();
                if(insertPrefix != null) {
                    if (!LexerUtils.startsWith(insertPrefix, context.getPrefix(), true, false)) {
                        itr.remove();
                    }
                }
            }
        }

        return items;
    }

    @Override
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
        return Directive.isAngularAttribute(attribute);
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes(String elementName) {
        return AngularCustomAttribute.getCustomAttributes();
    }
}
