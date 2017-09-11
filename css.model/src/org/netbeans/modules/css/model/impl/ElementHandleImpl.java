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

                    String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1, token.length()) : "1";
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
