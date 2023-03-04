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
