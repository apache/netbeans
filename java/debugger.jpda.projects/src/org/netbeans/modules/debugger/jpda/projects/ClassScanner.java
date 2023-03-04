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

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.ClassTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Search for a class matching the given binary filter.
 * 
 * @author martin
 */
class ClassScanner extends ErrorAwareTreePathScanner<TypeElement, Void> {
    
    private Trees trees;
    private Elements elements;
    private String binaryClassName;
    private String[] classExcludeNames;

    public ClassScanner(Trees trees, Elements elements, String binaryClassName, String[] classExcludeNames) {
        this.trees = trees;
        this.elements = elements;
        this.binaryClassName = binaryClassName;
        this.classExcludeNames = classExcludeNames;
    }

    @Override
    public TypeElement reduce(TypeElement arg0, TypeElement arg1) {
        if (arg0 != null) {
            return arg0;
        } else {
            return arg1;
        }
    }

    @Override
    public TypeElement visitClass(ClassTree arg0, Void arg1) {
        TypeElement typeElement = (TypeElement) trees.getElement(getCurrentPath());
        if (typeElement == null) {
            return super.visitClass(arg0, arg1);
        }
        String binaryName = elements.getBinaryName(typeElement).toString();
        if (match(binaryName)) {
            return typeElement;
        } else {
            return super.visitClass(arg0, arg1);
        }
    }


    private boolean match(String binaryName) {
        if (match(binaryName, binaryClassName)) {
            if (classExcludeNames != null) {
                for (String classExcludeName : classExcludeNames) {
                    if (match(binaryName, classExcludeName)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private static boolean match (String name, String pattern) {
        if (pattern.startsWith ("*")) {
            return name.endsWith (pattern.substring (1));
        } else if (pattern.endsWith ("*")) {
            return name.startsWith (
                pattern.substring (0, pattern.length () - 1)
            );
        }
        return name.equals (pattern);
    }
    
}
