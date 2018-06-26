/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
