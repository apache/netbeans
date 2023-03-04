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

package org.netbeans.modules.groovy.editor.java;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 *
 * @author Petr Hejl
 */
public final class ElementSearch {

    private ElementSearch() {
        super();
    }

    @CheckForNull
    public static TypeElement getClass(Elements elements, String name) {
        int index = name.indexOf("<"); //NOI18N
        if (index > 0) {
            name = name.substring(0, index);
        }
        TypeElement typeElement = getInnerClass(elements, name);
        if (typeElement == null) {
            typeElement = elements.getTypeElement(name);
        }
        return typeElement;
    }

    private static TypeElement getInnerClass(Elements elements, String name) {
        int index = name.indexOf("$"); // NOI18N
        TypeElement typeElement = null;
        if (index > 0 && name.length() > index + 1) {
            TypeElement enclosingElement = elements.getTypeElement(name.substring(0, index));

            int nextIndex = index;
            while (enclosingElement != null && nextIndex >= 0) {
                String subName = name.substring(nextIndex + 1);
                int subIndex = subName.indexOf("$"); // NOI18N
                if (subIndex >= 0) {
                    subName = subName.substring(0, subIndex);
                    nextIndex = nextIndex + 1 + subIndex;
                } else {
                    nextIndex = -1;
                }

                boolean found = false;
                for (TypeElement elem : ElementFilter.typesIn(enclosingElement.getEnclosedElements())) {
                    Name elemName = elem.getSimpleName();

                    if (elemName.toString().equals(subName)) {
                        enclosingElement = elem;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    enclosingElement = null;
                }
            }
            typeElement = enclosingElement;
        }
        return typeElement;
    }
}
