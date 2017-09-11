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
