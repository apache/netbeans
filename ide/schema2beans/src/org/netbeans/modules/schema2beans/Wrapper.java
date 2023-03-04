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

package org.netbeans.modules.schema2beans;

/**
 *  The Wrapper interface is one of the two ways a user can initialize a
 *  Wrapper object.
 *
 *  By default, when schema2beans generates the java classes, a #PCDATA DTD type
 *  is mapped to a java String. Sometime, the user might want to map an
 *  element to either a scalar or a specialized type (user defined class).
 *
 *  For example, a price DTD element specified as #PCDATA could be mapped
 *  to a float or integer data type. Or a date DTD element also specified
 *  as #PCDATA could be mapped to a specialized Date object, that the user might
 *  provide. schema2beans calls these specialized object 'wrappers'.
 *
 *  If the user specifies a wrapper object in the mdd file (see user
 *  documentation for mdd explanations), schema2beans uses the wrapper class
 *  instead of the String type. In this case, schema2beans needs to initialize
 *  the wrapper object using the String value from the XML document, and
 *  also needs to be able to get the String value from the wrapper 
 *  object (in order to write back the XML document).
 *
 *  This what this Wrapper interface provides. A wrapper class has
 *  has either to have a String constructor and toString() method,
 *  or implements the Wrapper interface. This is how schema2beans can set/get
 *  the String values of user wrapper/customized object.
 *
 */
public interface Wrapper {
    /**
     *	Method called by the schema2beans runtime to get the String value of the
     *	wrapper object. This String value is the value that has to appear 
     *	in the XML document.
     */
    public String 	getWrapperValue();
    
    /**
     *	Method called by the schema2beans runtime to set the value of the
     *	wrapper object. The String value is the value read in the XML document.
     */
    public void 	setWrapperValue(String value);
}
