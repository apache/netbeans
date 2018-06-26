/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.navigation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.api.Occurrence;

/**
 *
 * @author Petr Pisl
 */
public class OccurrencesSupport {

    private static final Logger LOGGER = Logger.getLogger(OccurrencesSupport.class.getName());
    
    private final Model model;

    public OccurrencesSupport(Model model) {
        this.model = model;
    }
    
    public Occurrence getOccurrence(int offset) {
        Occurrence result;
        long start = System.currentTimeMillis();
        JsObject object = model.getGlobalObject();
        result = findOccurrence(object, offset);
        if (result == null) {
            result = findDeclaration(object, offset);
        }
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Computing getOccurences({0}) took {1}ms. Returns {2}", new Object[]{offset, end - start, result});
        return result;
    }
    
    private Occurrence findOccurrence(JsObject object, int offset) {
        Occurrence result = null;
        JsElement.Kind kind = object.getJSKind();
        for(Occurrence occurrence: object.getOccurrences()) {
                if (occurrence.getOffsetRange().containsInclusive(offset)) {
                    return occurrence;
                }
            }
            if (kind.isFunction() || kind == JsElement.Kind.CATCH_BLOCK) {
                for(JsObject param : ((JsFunction)object).getParameters()) {
                 result = findOccurrence(param, offset);
                    if (result != null) {
                        break;
                    }   
                }
                if (result != null) {
                    return result;
                }
            }
            if (!(object instanceof JsReference && ModelUtils.isDescendant(object, ((JsReference)object).getOriginal()))) {
                for(JsObject property: object.getProperties().values()) {
                    if (!(property instanceof JsReference && !((JsReference)property).getOriginal().isAnonymous())) {
                        result = findOccurrence(property, offset);
                        if (result != null) {
                            break;
                        }
                    } else {
                        for(Occurrence occurrence: property.getOccurrences()) {
                            if (occurrence.getOffsetRange().containsInclusive(offset)) {
                                return occurrence;
                            }
                        }
                    }
                }
            }
        return result;
    }
    
    private Occurrence findDeclaration (JsObject object, int offset) {
        Occurrence result = null;
        JsElement.Kind kind = object.getJSKind();
        if (kind != JsElement.Kind.ANONYMOUS_OBJECT && kind != JsElement.Kind.WITH_OBJECT
                && object.getDeclarationName() != null && object.getDeclarationName().getOffsetRange().containsInclusive(offset)
                && !ModelUtils.isGlobal(object)) {
            if (kind.isPropertyGetterSetter()) {
                // if it's getter or setter in object literal, return it as occurrence of the property
                String propertyName = object.getName();
                propertyName = propertyName.substring(propertyName.lastIndexOf(' ') + 1);
                JsObject property = object.getParent().getProperty(propertyName);
                if (property != null) {
                    return new Occurrence(property.getDeclarationName().getOffsetRange(), property); 
                }
            } 

            result = new Occurrence(object.getDeclarationName().getOffsetRange(), object);
        }
        if (result == null && (kind.isFunction() || kind == JsElement.Kind.CATCH_BLOCK)) {
             for(JsObject param : ((JsFunction)object).getParameters()) {
                 if (param.getDeclarationName().getOffsetRange().containsInclusive(offset)) {
                     result = new Occurrence(param.getDeclarationName().getOffsetRange(), object);
                     return result;
                }
            }
        }
        if (result == null) {
            for(JsObject property: object.getProperties().values()) {
                if (!(property instanceof JsReference)) {
                    result = findDeclaration(property, offset);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }
}
