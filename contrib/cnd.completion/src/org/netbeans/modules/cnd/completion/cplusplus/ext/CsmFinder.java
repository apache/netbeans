/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
