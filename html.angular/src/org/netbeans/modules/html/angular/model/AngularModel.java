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
