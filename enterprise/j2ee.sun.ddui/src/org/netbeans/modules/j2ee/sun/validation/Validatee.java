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

package org.netbeans.modules.j2ee.sun.validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


/**
 * Validatee  is an <code>Interface</code> implemented by an <code>Object</code>
 * that needs to be validated.
 * <p>
 * In order to validate an object, framework expects either of the two things;
 * Object being validated is a Validatee itself(it implements Validatee
 * interface) or a separate Validatee Implementation for this Object is provided.
 * <p>
 * If a separate Validatee Implementation is provided then an entry needs to be
 * added to an Implementation File. Framework uses information in Implementation
 * File to figure out which Validatee Implementation to use for a given object.
 * Implementation File is a <code>.properties<code> file with name-value pair
 * enteries in it. Entry in an Implementation File is of the form :
 *    [fully qualified Object Name]=[Validatee Implementation for this Object]
 * Implementation File to be used is specified through system property 
 * <code>impl.file</code>
 * <p>
 * Validation framework has the Validatee Implementation for Schema2beans 
 * objects(<code>BaseBean</code>). You can override any Validatee Implementation
 * that comes with framework, by providing your own.
 * <p>
 * Validations are performed, recursively on the given Object.
 * Two types of Validations are perfomed, Structural validations and Specified
 * validations. Structural validations are expressed through 
 * <code>{@link CardinalConstraint}</code>. <code>CardinalConstraint</code> is 
 * an implicit Constraint. Its always applied to each of the element; you dont
 * need to specify it explicitly. Where as , other Constaints need to be
 * explicitly specified for each element you wanted to apply it to.
 * Constraints to be applied are specified through xml. This xml file is called
 * Validation File.  Validation file is xml file based on  
 * <code>validation.dtd</code>. You can also define, your own custom
 * <code>Constraint</code>s and apply them to any elements.
 *
 * @see Constraint
 * @see CardinalConstraint
 * @see ValidationManager
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public interface Validatee {
    /*
    Every Validatee has a corresponding Validator object and this object
    knows how to validate its Validatee. Validator maintains a list of
    Constraints that needs to be applied for each of the element of its
    Validatee. Constraint objects are built using files, ConstraintsFile
    and Validation File.
    ValidationManager is an object that constructs Validator objects for all
    the Validatees. ValidationManager maintains a map of xpaths to Validators.
    It constructs this by reading Validation File. Validation File specifies
    Constraints to be applied to the elements.
    */

    /**
     * Returns  the list of names, of all the leaf elements
     * of this <code>Object</code>.
     * 
     * @return the list of names, of all the leaf elements
     * of this <code>Object</code>.
     */
    public ArrayList getElementNames();


    /**
     * Returns  the list of dtd names, of all the leaf elements
     * of this <code>Object</code>.
     * 
     * @return the list of dtd names, of all the leaf elements
     * of this <code>Object</code>.
     */
    public ArrayList getElementDtdNames();


    /**
     * Tells if the given element is an index property(array).
     * 
     * @return true if the given element is an index property(array)
     */
    public boolean isIndexed(String elementName);


    /**
     * Gets a Cardinal for the given element of this <code>Object</code>.
     * Cardinal could be one of the following four :
     * MANDATORY, OPTIONAL(?), MANDATORY ARRAY(+) or OPTIONAL ARRAY(*)
     * 
     * @return a Cardinal for the given element of this <code>Object</code>
     */
    public int getElementCardinal(String elementName);


    /**
     * Gets a Cardinal of this <code>Object</code>
     * Cardinal could be one of the following four :
     * MANDATORY, OPTIONAL(?), MANDATORY ARRAY(+) or OPTIONAL ARRAY(*)
     * 
     * @return a Cardinal of this <code>Object</code>.
     */
    public int getCardinal();


    /**
     * Determines whether the given element is a bean or 
     * a property(leaf element).
     * 
     * @return <code>true</code> if the given element is a bean.
     */
    public boolean isBeanElement(String elementName);


    /**
     * Gets xpath of this <code>Object</code>.
     * xpath uniquely identifies an element in xml
     * 
     * @return the xpath of this <code>Object</code>
     */
    public String getXPath();


    /**
     * Gets an indexed-xpath of this <code>Object</code>.
     * indexed-xpath uniquely identifies an element in xml in case of + , * 
     * Cardinalities.
     * 
     * @return an indexed xpath of this <code>Object</code>
     */
    public String getIndexedXPath();


    /**
     * Returns  an element of this Object with the given name.
     * 
     * @return an element of this Object with the given name.
     */
    public Object getElement(String elementName);


    /**
     * Returns an element of this Object with the given name and 
     * at a given index. This is the case when the given name represents
     * an array element
     * 
     * @return an element of this Object with the given name and index.
     */
    public Object  getElement(String elementName, int index);


    /**
     * Returns an array element of this Object with the given name.
     * 
     * @return an array element of this Object with the given name.
     */
    public Object [] getElements(String elementName);


    /**
     * Returns  the <code>Method</code> object, for the given method of this
     * Object. 
     * 
     * @return the <code>Method</code> object, for the given method of this
     * Object.
     */
    public Method getMethod(String methodName);


    /**
     * Invokes given method on this Object
     * 
     * @return an Object returned by the invoked method.
     */
    public Object invoke(Method method);
}
