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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.spring.java;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.spring.beans.completion.OptionCodeCompletionSettings;

/**
 * Finds all simple bean properties starting with a specified prefix on the specified type
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PropertyFinder {
    
    private final ElementUtilities eu;
    private final TypeMirror type;
    private final String matchText;
    private final Map<String, Property> name2Prop = new HashMap<String, Property>();
    private final MatchType matchType;

    public PropertyFinder(TypeMirror type, String matchText, ElementUtilities eu, MatchType matchType) {
        this.type = type;
        this.matchText = matchText;
        this.eu = eu;
        this.matchType = matchType;
    }

    public Property[] findProperties() {
        name2Prop.clear();
        eu.getMembers(type, new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror type) {

                // only accept methods
                if (e.getKind() != ElementKind.METHOD) {
                    return false;
                }

                ExecutableElement ee = (ExecutableElement) e;
                String methodName = ee.getSimpleName().toString();
                
                // only accept getXXX and isXXX (for boolean)
                if (JavaUtils.isGetter(ee)) {
                    String propName = JavaUtils.getPropertyName(methodName);
                    if(!match(propName)) {
                        return false;
                    }
                    
                    addPropertyGetter(propName, ee);
                    return true;
                }
                
                // only accept setXXX
                if (JavaUtils.isSetter(ee)) {
                    String propName = JavaUtils.getPropertyName(methodName);
                    if(!match(propName)) {
                        return false;
                    }
                    
                    addPropertySetter(propName, ee);
                    return true;
                }

                return false;
            }
        });
        
        return name2Prop.values().toArray(new Property[0]);
    }
    
    private void addPropertySetter(String propName, ExecutableElement setter) {
        Property prop = getProperty(propName);
        prop.setSetter(setter);
    }
    
    private void addPropertyGetter(String propName, ExecutableElement getter) {
        Property prop = getProperty(propName);
        prop.setGetter(getter);
    }
    
    private Property getProperty(String propName) {
        Property prop = name2Prop.get(propName);
        if(prop == null) {
            prop = new Property(propName);
            name2Prop.put(propName, prop);
        }
        
        return prop;
    }
    
    private boolean match(String propName) {
        int length = matchType == MatchType.PREFIX ? matchText.length() : Math.max(propName.length(), matchText.length());
        return propName.regionMatches(!OptionCodeCompletionSettings.isCaseSensitive(), 0, matchText, 0, length);
    }
}
