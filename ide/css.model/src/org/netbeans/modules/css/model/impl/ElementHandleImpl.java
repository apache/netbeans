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
package org.netbeans.modules.css.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.ElementHandle;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.StyleSheet;

/**
 *
 * @author marekfukala
 */
public class ElementHandleImpl implements ElementHandle {

    private static char DELIMITER = '/'; //NOI18N
    private static char INDEX_DELIMITER = '|'; //NOI18N
    private String elementId;

    ElementHandleImpl(ModelElement element) {
        this.elementId = createPath(element);
    }

    @Override
    public Element resolve(Model model) {
        final AtomicReference<Element> elementRef = new AtomicReference<>();
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                StringTokenizer st = new StringTokenizer(elementId, ""+DELIMITER);
                Element base = styleSheet;
                assert(st.hasMoreTokens()); //at least the styleSheet element
                //skip the first stylesheet element
                String styleSheetId = st.nextToken();
                assert styleSheetId.equals(getFQElementID((ModelElement)styleSheet));
                
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int indexDelim = token.indexOf(INDEX_DELIMITER);

                    String lightId = indexDelim >= 0 ? token.substring(0, indexDelim) : token;

                    String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1) : "1";
                    int index = Integer.parseInt(sindex);

                    int count = 0;
                    Element foundLocal = null;
                    Iterator<Element> childrenIterator = base.childrenIterator();
                    while(childrenIterator.hasNext()) {
                        ModelElement child = (ModelElement)childrenIterator.next();
                        if(lightId.equals(getElementID(child)) && ++count == index) {
                            foundLocal = child;
                            break;
                        }
                    }
                    
                    if(foundLocal == null) {
                        return ;
                    } else {
                        base = foundLocal;

                        if (!st.hasMoreTokens()) {
                            //last token, we may return
                            elementRef.set(base);
                            return ;
                        }

                    }
                }
            }
        });

        return elementRef.get();
    }

    /* test */ static String createPath(ModelElement element) {
        StringBuilder sb = new StringBuilder();
        List<ModelElement> elements = new ArrayList<>();
        do {
            elements.add(0, element);
            element = (ModelElement) element.getParent();
        } while (element != null);

        Iterator<ModelElement> elementsItr = elements.iterator();
        while (elementsItr.hasNext()) {
            sb.append(getFQElementID(elementsItr.next()));
            if (elementsItr.hasNext()) {
                sb.append(DELIMITER);
            }
        }

        return sb.toString();
    }

    /**
     * Used to generate the element path in {@link ElementHandle} according to
     * which the element is later resolved.
     *
     * @return non null element's id
     */
    @NonNull
    /* test */ static String getFQElementID(ModelElement element) {
        StringBuilder sb = new StringBuilder();
        sb.append(getElementID(element));
        int index = getIndexInSimilarNodes(element);
        if (index > 1) {
            sb.append(INDEX_DELIMITER);
            sb.append(index);
        }
        return sb.toString();
    }

    /* test */ static String getElementID(ModelElement element) {
        String customElementId = element.getCustomElementID();
        return customElementId != null ? customElementId : element.getModelClass().getSimpleName();
    }

    /* test */ static int getIndexInSimilarNodes(ModelElement element) {
        Element parent = element.getParent();
        if (parent == null) {
            return -1;
        }
        String elementLightID = getElementID(element);
        int index = 0;
        Iterator<Element> childrenIterator = parent.childrenIterator();
        while (childrenIterator.hasNext()) {
            ModelElement child = (ModelElement) childrenIterator.next();
            String childLightID = getElementID(child);
            if (childLightID.equals(elementLightID)) {
                index++;
            }
            if (child == element) {
                break;
            }
        }
        return index;
    }
}
