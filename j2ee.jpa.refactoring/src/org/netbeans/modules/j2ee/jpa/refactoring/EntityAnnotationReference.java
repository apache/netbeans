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

package org.netbeans.modules.j2ee.jpa.refactoring;

import org.netbeans.api.java.source.TreePathHandle;

/**
 * This class represents an annotation reference to an entity.
 *
 * @author Erno Mononen
 */
public class EntityAnnotationReference {
    
    /**
     * The entity that has the feature with the referencing annotation.
     */
    private final String entity;
    /**
     * The FQN of the referencing annotation.
     */
    private final String annotation;
    /**
     * The referencing annotation attribute.
     */ 
    private final String attribute;
    /**
     * The value for the referencing annotation attribute.
     */ 
    private final String attributeValue;
    /**
     * The handle for the property that has the referencing annotation.
     */ 
    private final TreePathHandle handle;
    /**
     * Creates a new instance of EntityAssociation
     * @param referenced the entity that is referenced.
     * @param referring the entity that has the property with referencing annotation.
     * @param property the property that hat the referencing annotation.
     * @param annotation the referencing annotation
     * @param attributeValue the attribute value of the annotation that references other entity
     */
    public EntityAnnotationReference(String entity, String annotation, 
            String attribute, String attributeValue, TreePathHandle handle) {
        this.entity = entity;
        this.annotation = annotation;
        this.attribute = attribute;
        this.attributeValue = attributeValue;
        this.handle = handle;
    }

    /**
     *@see #entity
     */ 
    public String getEntity() {
        return entity;
    }

    /**
     *@see #annotation
     */ 
    public String getAnnotation() {
        return annotation;
    }

    /**
     *@see #attribute
     */ 
    public String getAttribute() {
        return attribute;
    }

    /**
     *@see #attributeValue
     */ 
    public String getAttributeValue() {
        return attributeValue;
    }

    /**
     *@see #handle
     */ 
    public TreePathHandle getHandle() {
        return handle;
    }
    
}
