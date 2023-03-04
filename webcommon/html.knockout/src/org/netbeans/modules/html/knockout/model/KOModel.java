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
package org.netbeans.modules.html.knockout.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import org.netbeans.modules.html.knockout.KOUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * So far just holds a list of ng attributes.
 *
 * @author marekfukala
 */
public class KOModel {
    
    private static final Map<HtmlParserResult, KOModel> INSTANCES =
            new WeakHashMap<>();

    /**
     * Gets cached angular model for the given parser result.
     * @param result
     */
    @NonNull
    public static synchronized KOModel getModel(HtmlParserResult result) {
        KOModel model = INSTANCES.get(result);
        if (model == null) {
            model = new KOModel(result);
            INSTANCES.put(result, model);
        }
        return model;
    }
    
    /**
     * Maps html elements to ng attributes.
     */
    private Map<OpenTag, Collection<Attribute>> elements2attributes = new HashMap<>();
    
    /**
     * All ng attributes.
     * 
     * XXX maybe use attribute -> container element map.
     */
    private Collection<Attribute> attributes = new ArrayList<>();

    private KOModel(final HtmlParserResult result) {
        Iterator<Element> elementsIterator = result.getSyntaxAnalyzerResult().getElementsIterator();
        while (elementsIterator.hasNext()) {
            Element element = elementsIterator.next();
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    for (Attribute ngAttr : ot.attributes(new AttributeFilter() {
                        @Override
                        public boolean accepts(Attribute attribute) {
                            //the data-bind attribute can contain custom directives which we do not have any metadata for
                            //so the data-bind attribute is always considered as a knockout regardless the content, 
                            //at least until we have some custom directives metadata facility.
                            return isKODataBindingAttribute(attribute) || isKOParamsAttribute(attribute);
//                            return isKODataBindingAttribute(attribute) && containsKODirective(result.getSnapshot(), attribute);
                        }
                    })) {
                        Collection<Attribute> attrs = elements2attributes.get(ot);
                        if(attrs == null) {
                            attrs = new ArrayList<>();
                            elements2attributes.put(ot, attrs);
                        }
                        attrs.add(ngAttr);
                        attributes.add(ngAttr);
                    }
            }
        }
    }
    
    public static boolean isKODataBindingAttribute(Attribute attribute) {
        return LexerUtils.equals(KOUtils.KO_DATA_BIND_ATTR_NAME, attribute.unqualifiedName(), true, true);
    }

    public static boolean isKOParamsAttribute(Attribute attribute) {
        return LexerUtils.equals(KOUtils.KO_PARAMS_ATTR_NAME, attribute.unqualifiedName(), true, true);
    }
    
    private static boolean containsKODirective(Snapshot snapshot, Attribute attribute) {
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        TokenSequence<HTMLTokenId> tokenSequence = tokenHierarchy.tokenSequence(HTMLTokenId.language());
        if(tokenSequence != null) {
            tokenSequence.move(attribute.valueOffset() + (attribute.isValueQuoted() ? 1 : 0));
            if(tokenSequence.moveNext()) {
                TokenSequence<KODataBindTokenId> embedded = tokenSequence.embedded(KODataBindTokenId.language());
                if(embedded != null) {
                    embedded.moveStart();
                    while(embedded.moveNext()) {
                        switch(embedded.token().id()) {
                            case KEY:
                                String img = embedded.token().text().toString();
                                if(Binding.getBinding(img) != null) {
                                    return true;
                                }
                                break;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Gets a list of all angular attributes in the page.
     */
    @NonNull
    public Collection<Attribute> getBindings() {
        return attributes;
    }
            
    /**
     * Checks whether the parser result contains any knockout code.
     * 
     * TODO - check for the ko.applyBindings() presence instead?
     */
    public boolean containsKnockout() {
        return !getBindings().isEmpty();
    }
    
}
