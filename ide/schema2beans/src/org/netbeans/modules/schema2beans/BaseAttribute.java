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

import java.util.*;
import java.io.*;


/**
 *  The BaseBean introspection methods return BaseProperty and BaseAttribute
 *  objects. This interface is the attribute equivalent to the BaseProperty
 *  interface.
 *
 *  This interface is the public access to the schema2beans internal attribute
 *  information (see AttrProp class for the implementation of this interface).
 */
public interface BaseAttribute {
    /**
     *	Values returned by getType()
     **/
    public static final int TYPE_CDATA 		= AttrProp.CDATA;
    public static final int TYPE_ENUM 		= AttrProp.ENUM;
    public static final int TYPE_NMTOKEN 	= AttrProp.NMTOKEN;
    public static final int TYPE_ID 		= AttrProp.ID;
    public static final int TYPE_IDREF 		= AttrProp.IDREF;
    public static final int TYPE_IDREFS 	= AttrProp.IDREFS;
    public static final int TYPE_ENTITY 	= AttrProp.ENTITY;
    public static final int TYPE_ENTITIES 	= AttrProp.ENTITIES;
    public static final int TYPE_NOTATION 	= AttrProp.NOTATION;
    
    /**
     *	Values returned by getOption()
     */
    public static final int OPTION_REQUIRED 	= AttrProp.REQUIRED;
    public static final int OPTION_IMPLIED 	= AttrProp.IMPLIED;
    public static final int OPTION_FIXED 	= AttrProp.FIXED;
    
    
    /**
     *	Return the name of the attribute as it is used in the bean class.
     */
    public String 	getName();
    
    /**
     *	Return the dtd name of the attribute, as it appears in the DTD file.
     */
    public String 	getDtdName();
    
    /**
     *	Return true if the name is either equals to getName() or getDtdName()
     */
    public boolean	hasName(String name);
    
    /**
     *	If the attribute is Enum, returns the list of possible values
     */
    public String[] getValues();
    
    /**
     *	Default value used when creating this attribute
     */
    public String getDefaultValue();
    
    /**
     *	True if the attribute is Enum
     */
    public boolean isEnum();
    
    /**
     *	True if the attribute has a fixed value
     */
    public boolean isFixed();
    
    /**
     *	Returns one of the following constants:
     *
     *		OPTION_REQUIRED
     *		OPTION_IMPLIED
     *		OPTION_FIXED
     */
    public int getOption();
    
    /**
     *	Returns one of the following constants:
     *
     *		TYPE_CDATA
     *		TYPE_ENUM
     *		TYPE_NMTOKEN
     *		TYPE_ID
     *		TYPE_IDREF
     *		TYPE_IDREFS
     *		TYPE_ENTITY
     *		TYPE_ENTITIES
     *		TYPE_NOTATION
     */
    public int getType();
    
    /**
     *	In general the attributes are defined in the DTD file,
     *	like the properties, and are therefore part of the bean structure,
     *	because part of the generated beans. This method returns false
     *	for such attributes.
     *	If an attribute is not declared in the DTD file but used in the XML
     *	document, the schema2beans consider such attributes as transient and
     *	add them, on the fly, into the bean structures. This method returns
     *	true for such attributes.
     */
    public boolean isTransient();
}
