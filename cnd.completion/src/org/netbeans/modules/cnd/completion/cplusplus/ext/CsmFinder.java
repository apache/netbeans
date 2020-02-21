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

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import java.util.List;

import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;

/**
* Java completion finder
*
* @version 1.00
* based on JCFinder @version 1.00 - move to CSM model
*/

public interface CsmFinder {

    public CsmFile getCsmFile();
    
    /** Get the namespace from the namespace name */
    public CsmNamespace getExactNamespace(String namespaceName);

    /** Get the class from full name of the class */
    public CsmClassifier getExactClassifier(String classFullName);

    /** Get the list of nested namespaces that start with the given name
    * @param name the start of the requested namespace(s) name
    * @return list of the matching namespaces
    */
    public List<CsmNamespace> findNestedNamespaces(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested);

    /** Find classes by name and possibly in some namespace
    * @param nmsp namespace where the classes should be searched for. It can be null
    * @param begining of the name of the class. The namespace name must be omitted.
    * @param exactMatch whether the given name is the exact requested name
    *   of the class or not.
    * @param searchNested whether elements must be searched in unnamed nested namespaces as well
    * @return list of the matching classes
    */
    public List<CsmClassifier> findClasses(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested);

    /** Find elements (classes, variables, enumerators) by name and possibly in some namespace
    * @param nmsp namespace where the elements should be searched for. It can be null
    * @param begining of the name of the element. The namespace name must be omitted.
    * @param exactMatch whether the given name is the exact requested name
    *   of the element or not.
    * @param searchNested whether elements must be searched in unnamed nested namespaces as well
    * @return list of the matching elements
    */
    public List<CsmObject> findNamespaceElements(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested, boolean searchFirst);

    /**
     * Find elements (functions, variables, enumerators) by name and in some namespace.
     *
     * @param nmsp namespace where the elements should be searched for. Can't be null.
     * @param begining of the name of the element. The namespace name must be omitted.
     * @param exactMatch whether the given name is the exact requested name
     *   of the element or not.
     * @return list of the matching elements
     */
    public List<CsmObject> findStaticNamespaceElements(CsmNamespace nmsp, String name, boolean exactMatch);

    /** Find fields by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the fields.
    * @param name start of the name of the field
    * @param exactMatch whether the given name of the field is exact
    * @param staticOnly whether search for the static fields only
    * @param inspectOuterClasses if the given class is inner class of some
    *   outer class, whether the fields of the outer class should be possibly
    *   added or not. This should be false when searching for 'this.'
    * @return list of the matching fields
    */
    public List<CsmField> findFields(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch,
                           boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort);

    /** Find base classes by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the fields.
    * @param name start of the name of the field
    * @param exactMatch whether the given name of the field is exact
    * @param staticOnly whether search for the static fields only
    * @param inspectOuterClasses if the given class is inner class of some
    *   outer class, whether the fields of the outer class should be possibly
    *   added or not. This should be false when searching for 'this.'
    * @return list of the matching fields
    */
    public List<CsmClass> findBaseClasses(CsmOffsetableDeclaration contextDeclaration, CsmClassifier c, String name, boolean exactMatch, boolean sort);

    /** Find enumerators by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the enumerators.
    * @param name start of the name of the field
    * @param exactMatch whether the given name of the enumerators is exact
    * @param inspectOuterClasses if the given class is inner class of some
    *   outer class, whether the enumerators of the outer class should be possibly
    *   added or not. This should be false when searching for 'this.'
    * @return list of the matching fields
    */    
    public List<CsmEnumerator> findEnumerators(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch,
            boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort);
    
    /** Find methods by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the methods.
    * @param name start of the name of the method
    * @param exactMatch whether the given name of the method is exact
    * @param staticOnly whether search for the static methods only
    * @param inspectOuterClasses if the given class is inner class of some
    *   outer class, whether the methods of the outer class should be possibly
    *   added or not. This should be false when searching for 'this.'
    * @return list of the matching methods
    */
    public List<CsmMethod> findMethods(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch,
                            boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort);    
    
    /** Find nested classifiers by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the nested classifiers.
    * @param name start of the name of the nested classifiers
    * @param exactMatch whether the given name of the nested classifiers is exact
    * @param staticOnly whether search for the static nested classifiers only
    * @param inspectParentClasses if the given class is inner class of some
    *   outer class, whether the classifiers of the outer class should be possibly
    *   added or not
    * @return list of the matching nested classifiers
    */
    public List<CsmClassifier> findNestedClassifiers(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch,
                            boolean inspectParentClasses, boolean sort);      

    public List<CsmLabel> findLabel(CsmOffsetableDeclaration contextDeclaration, String name, boolean exactMatch,  boolean sort);

}
