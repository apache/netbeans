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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This interface represents a common interface shared by all schema elements.
 * @author Chris Webster
 */
public interface SchemaComponent extends DocumentComponent<SchemaComponent> {

    // TODO Should there be a resolve capability and expose uri for references
    public static final String ANNOTATION_PROPERTY = "annotation";
    public static final String ID_PROPERTY = "id";
    
    /**
     * @return the schema model this component belongs to.
     */
    SchemaModel getModel();
    
    /**
     * @return schema component 'id' attribute if presents, null otherwise.
     */
    String getId();
    
    /**
     * Set the schema component 'id' attribute value.
     */
    void setId(String id);
    
    /**
     * Returns value of an attribute defined in a certain namespace.
     */
    String getAnyAttribute(QName attributeName);
    
    /**
     * Sets value of an attribute defined in a certain namespace.
     * Propery change event will be fired with property name using attribute local name.
     */
    void setAnyAttribute(QName attributeName, String value);
    
    /**
     **/
    public Annotation getAnnotation();
    
    /**
     **/
    public void setAnnotation(Annotation annotation);
    
    /**
     * Visitor providing
     */
    void accept(SchemaVisitor visitor);
    
    /**
     * @return true if the elements are from the same schema model.
     */
    boolean fromSameModel(SchemaComponent other);
    
    /**
     * Returns the type of the component in terms of the schema model interfaces
     *
     */
    Class<? extends SchemaComponent> getComponentType();
	
    /**
     * Creates a global reference to the given target Schema component.
     * @param referenced the schema component being referenced.
     * @param type actual type of the target
     * @return the reference.
     */
    <T extends ReferenceableSchemaComponent> NamedComponentReference<T> createReferenceTo(T referenced, Class<T> type);
}
