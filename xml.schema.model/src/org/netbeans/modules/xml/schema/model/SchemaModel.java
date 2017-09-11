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

package org.netbeans.modules.xml.schema.model;
import java.util.Collection;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;

/**
 * This interface represents an instance of a schema model. A schema model is
 * bound to a single file.
 * @author Chris Webster
 */
public interface SchemaModel extends DocumentModel<SchemaComponent>, Referenceable {
	
	/**
	 * @return the schema represented by this model. The returned schema
	 * instance will be valid and well formed, thus attempting to update 
	 * from a document which is not well formed will not result in any changes
	 * to the schema model. 
	 */
	Schema getSchema();
        
	/**
	 * This api returns the effective namespace for a given component. 
	 * If given component has a targetNamespace different than the 
	 * this schema, that namespace is returned. The special case is that if
	 * the targetNamespace of the component is null, there is no target
	 * namespace defined, then the import statements for this file are 
	 * examined to determine if this component is directly or indirectly 
	 * imported. If the component is imported, then null if returned 
	 * otherwise the component is assumed to be included or redefined and
	 * the namespace of this schema is returned. 
	 * @param component The component which namespace to find
	 * @return The effective target namespace
	 */
	String getEffectiveNamespace(SchemaComponent component);
	
	/**
	 * @return common schema element factory valid for this instance
	 */
	SchemaComponentFactory getFactory();
	
        /**
         * Returns all visible schemas matching the given namespace.  
         * Note visibility rule is defined as following:
         * (1) Include or redefine are transitive, i.e., includer can see all schemas 
         *     included or redefined by the included
         * (2) Import is not transitive, i.e., importing schema can only see the 
         *     imported schema, but not those included, redefined or imported by the imported.
         * (3) Imported schemas are not visible to includer or redefiner.
         */
        Collection<Schema> findSchemas(String namespaceURI);

        /**
         * Finds the component in current schema by local name and type.
         * @param localName the local name of the schema component.
         * @param type the exact type of the schema component.
         * @return first encountered of the schema component of specified name and type; 
         * null if not found.
         */
        <T extends NamedReferenceable> T findByNameAndType(String localName, Class<T> type);
        
        /**
         * Resolves the reference to component given namespace and local name.
         * @param namespace the namespace of the referenced component
         * @param localName local name of the refrenced component.
         * @param type type of the component.
         */
        <T extends NamedReferenceable> T resolve(String namespace, String localName, Class<T> type);
        
        /**
         * Returns true for schemas that are embedded inside other artifacts such as WSDLs and BPELs.
         * False in all other cases.
         */
        public boolean isEmbedded();
}
