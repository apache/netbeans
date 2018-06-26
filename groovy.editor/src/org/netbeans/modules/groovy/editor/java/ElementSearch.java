/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        TypeElement typeElement = elements.getTypeElement(name);
        if (typeElement == null) {
            typeElement = getInnerClass(elements, name);
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
