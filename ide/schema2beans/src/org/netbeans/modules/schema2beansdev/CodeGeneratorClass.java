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

import java.io.*;
import java.util.*;

import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.schema2beansdev.metadd.*;

public interface CodeGeneratorClass {
    /**
     *	This method is called by the BeanBuilder to register a new property
     *	on this bean class.
     */
    AbstractCodeGeneratorClass.Property addProperty(String name,
                     String dtdName, String namespace,
                     GraphNode node, GraphLink l, String classType, int nestedLevel,
                     int eltInstance, int groupInstance,
                     int type, boolean ored, AttrProp[] propAttributes,
                     String constName, String defaultValue,
                     boolean directChild, List extraData, boolean isUnion);
    List/*<AbstractCodeGeneratorClass.Property>*/ getPropertyList();

    void setPackageName(String n);

    /**
     * The string that is used for indentation.
     */
    void setIndent(String indent);

    /**
     *	Generate the java code in the out stream, using the optional
     *	metaDD bean graph.
     */
    void generate(String filename, MetaDD mdd) throws IOException;

    /**
     *	Generate the java code in the out stream, using the optional
     *	metaDD bean graph.
     */
    void generate(OutputStream out, MetaDD mdd) throws IOException;

    public void generateDelegator(OutputStream out, MetaDD mdd,
                                  String delegatorClassName,
                                  String delegatorPackageName) throws IOException;

    /**
     * The generator should put in an entry for every name that is
     * invalid to use as a property name.
     *
     * @param invalidNames is a
     * Map<String, BeanElement> where they key is the invalid name and
     * the value (BeanElement) is most likely null.
     * For instance, anything that inherits from java.lang.Object will
     * not be able to generate a getClass method, so the property
     * named "Class" is invalid.
     * Typically, the invalid method names are due to a parent class
     * already having a method of that name with the same parameters
     * (overloading is okay though).  A property X is invalid if a
     * parent class already has a getX(); and if X might
     * ever be an array, then also getX(int) or sizeX().
     */
    public void setInvalidPropertyNames(Map invalidNames);
    
    public void setRootBeanElement(BeanBuilder.BeanElement element);

    /**
     * Set the namespace that will be used by default in the documents.
     */
    public void setDefaultNamespace(String ns);

    public Collection getGeneratedMethods();	// Collection<JavaWriter.Method>

    /**
     * Print out the bean graph.
     */
    public void dumpBeanTree(java.io.Writer out, String indent, String indentBy) throws java.io.IOException;
    
    public void setPrefixGuesser(PrefixGuesser guesser);
}
