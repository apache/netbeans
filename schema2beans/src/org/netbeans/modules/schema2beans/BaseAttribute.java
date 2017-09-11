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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    static public final int TYPE_CDATA 		= AttrProp.CDATA;
    static public final int TYPE_ENUM 		= AttrProp.ENUM;
    static public final int TYPE_NMTOKEN 	= AttrProp.NMTOKEN;
    static public final int TYPE_ID 		= AttrProp.ID;
    static public final int TYPE_IDREF 		= AttrProp.IDREF;
    static public final int TYPE_IDREFS 	= AttrProp.IDREFS;
    static public final int TYPE_ENTITY 	= AttrProp.ENTITY;
    static public final int TYPE_ENTITIES 	= AttrProp.ENTITIES;
    static public final int TYPE_NOTATION 	= AttrProp.NOTATION;
    
    /**
     *	Values returned by getOption()
     */
    static public final int OPTION_REQUIRED 	= AttrProp.REQUIRED;
    static public final int OPTION_IMPLIED 	= AttrProp.IMPLIED;
    static public final int OPTION_FIXED 	= AttrProp.FIXED;
    
    
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
