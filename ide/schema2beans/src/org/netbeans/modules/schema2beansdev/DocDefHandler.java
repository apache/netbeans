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

package org.netbeans.modules.schema2beansdev;

import java.util.*;
import java.io.*;

import org.netbeans.modules.schema2beans.*;

/**
 * The DTD parser handler
 */
public interface DocDefHandler {
    /**
     *	Called once, when the DTD is started to be parsed.
     *
     *	@param root root elemement name of the document (as the DOCTYPE
     *	specifies in the XML document)
     */
    public void startDocument(String root);
    public void endDocument();

    /**
     *	Called each time a DTD <!element definition is read.
     *
     *	@param name the name of the element
     *  @param typeName is the name to use for the attribute
     *	@param type the type (as a constant) of the element (for example
     *	TYPE_ELEMENT or TYPE_ATTLIST)
     */
    public void startElement(String uniqueName,
                             String typeName, int type);
    public void endElement();
    
    /**
     * Does this element exist at this point?  Either thru a startElement() creation,
     * or thru an element() reference.
     */
    public boolean doesElementExist(String typeName);
    
    /**
     *	These methods are called to signal the beginning of the ( and )
     *	in an ELEMENT declaration. startGroupElements is called when
     *	an open parenthese is found and endGroupElements is called
     *	when the closed parenthese is found. The closing parenthese
     *	might be followed by the character *, + or ?. The instance
     *	value of the method call reflects this character value.
     */
    public void startGroupElements();
    public void endGroupElements(int instance);
    
    /**
     *	Called each time a character , ( ) or | is found.
     */
    public void character(char c);
    
    /**
     *	Called for each name element found within the scope of an element
     *	(<!element (element1, element2, ...)>. The first element name doesn't
     *	generate a call to this method (@see startElement).
     *
     *	@param name the name of the element defined within the <!element ...>
     *	declaration.
     *	@param instance has one of the three values: INSTANCE_0_1,
     *	INSTANCE_1, INSTANCE_0_N, INSTANCE_1_N
     *
     */
    public void element(String uniqueName, String typeName,
                        String attrName, String attrNamespace,
                        int instance, boolean externalType, String defaultValue);
    public void element(String uniqueName, String name, int instance);

    public void setUnion(String uniqueName, String typeName, boolean value) throws Schema2BeansException;

    public void addExtraDataNode(String uniqueName, String typeName, Object data) throws Schema2BeansException;
    public void addExtraDataCurLink(Object data);
    public void setExtension(String uniqueName, String typeName, String extendsName) throws Schema2BeansException;

    public void nillable(boolean value);
    
    /**
     * Called to request that the current graph node be of a certain
     * Java class.
     * @param javaType is the name of a Java class (eg, "java.lang.Integer", or "int").
     */
    public void javaType(String uniqueName, String name, String javaType);

    public void setAbstract(String uniqueName, String name, boolean value);
    /**
     * Set the namespace that will be used by default in the documents.
     */
    public void setDefaultNamespace(String ns);

    /**
     * set a special property to some value.
     */
    public void setExtendedProperty(String uniqueName, String typeName, String propertyName,
                                    Object value) throws Schema2BeansException;
    
    /**
     * Establish a prefix guesser
     */
    public void setPrefixGuesser(PrefixGuesser guesser);
}
