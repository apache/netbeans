/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Search for a class matching the given binary filter.
 * 
 * @author martin
 */
class ClassScanner extends TreePathScanner<TypeElement, Void> {
    
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
