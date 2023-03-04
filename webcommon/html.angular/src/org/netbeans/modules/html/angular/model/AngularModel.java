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
package org.netbeans.modules.html.angular.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;

/**
 * So far just holds a list of ng attributes.
 *
 * @author marekfukala
 */
public class AngularModel {

    private static final Map<HtmlParserResult, AngularModel> INSTANCES =
            new WeakHashMap<>();

    /**
     * Gets cached angular model for the given parser result.
     * @param result
     */
    @NonNull
    public static synchronized AngularModel getModel(HtmlParserResult result) {
        assert result != null;
        assert result.getSyntaxAnalyzerResult() != null;
        
        AngularModel model = INSTANCES.get(result);
        if (model == null) {
            model = new AngularModel(result);
            INSTANCES.put(result, model);
        }
        return model;
    }
    
    /**
     * Maps html elements to ng attributes.
     */
    private Map<OpenTag, Collection<Attribute>> elements2ngAttributes = new HashMap<>();
    
    private EnumMap<DirectiveConvention, Integer> directiveConventionOccurrenceCount = new EnumMap<>(DirectiveConvention.class);
    private DirectiveConvention mostUsedConvention;
    
    /**
     * All ng attributes.
     * 
     * XXX maybe use attribute -> container element map.
     */
    private Collection<Attribute> ngAttributes = new ArrayList<>();

    //do not hold the parser result!
    private AngularModel(HtmlParserResult result) {
        Iterator<Element> elementsIterator = result.getSyntaxAnalyzerResult().getElementsIterator();
        while (elementsIterator.hasNext()) {
            Element element = elementsIterator.next();
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
            for (Iterator<Attribute> it = ot.attributes(new AttributeFilter() {
                      @Override
                      public boolean accepts(Attribute attribute) {
                          DirectiveConvention convention = DirectiveConvention.getConvention(attribute.unqualifiedName());
                          if(convention != null) {
                              //count the occurrences
                              Integer i = directiveConventionOccurrenceCount.get(convention);
                              if(i == null) {
                                  directiveConventionOccurrenceCount.put(convention, 1);
                              } else {
                                  directiveConventionOccurrenceCount.put(convention, i + 1);
                              }
                              return true;
                          }
                          return false;
                      }
                  }).iterator(); it.hasNext();) {
                Attribute ngAttr = it.next();
                Collection<Attribute> attrs = elements2ngAttributes.get(ot);
                if(attrs == null) {
                    attrs = new ArrayList<>();
                    elements2ngAttributes.put(ot, attrs);
                }
                attrs.add(ngAttr);
                ngAttributes.add(ngAttr);
            }
            }
        }
    }
    
    /**
     * Gets the most used attribute convention.
     */
    public synchronized DirectiveConvention getPrevailingAttributeConvention() {
        if(mostUsedConvention == null) {
            DirectiveConvention winner = DirectiveConvention.base_dash; //default
            int count = 0;
            for(Map.Entry<DirectiveConvention, Integer> entry : directiveConventionOccurrenceCount.entrySet()) {
                Integer occurrences = entry.getValue();
                if(count <= occurrences) { //same occurrences cound -> last wins
                    winner = entry.getKey();
                    count = occurrences;
                }
            }
            mostUsedConvention = winner;
        }
        return mostUsedConvention;
    }
    
    /**
     * Gets a list of all angular attributes in the page.
     */
    @NonNull
    public Collection<Attribute> getNgAttributes() {
        return ngAttributes;
    }
            
    /**
     * Checks whether the parser result contains any angular code.
     */
    public boolean isAngularPage() {
        return !getNgAttributes().isEmpty();
    }
    
}
