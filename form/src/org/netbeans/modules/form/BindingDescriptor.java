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

package org.netbeans.modules.form;

import java.lang.reflect.*;
import org.netbeans.modules.form.FormUtils.TypeHelper;

/**
 * Descriptor of binding property/one segment in the binding path.
 *
 * @author Jan Stola, Tomas Pavek.
 */
public class BindingDescriptor {
    /** Generified value type of the binding. */
    private TypeHelper genericValueType;
    /** Value type of the binding. */
    private Class valueType;
    /** Name of the binding property/path segment. */
    private String path;

    /** Display name of this binding. */
    private String propertyDisplayName;
    /** Short description of this binding. */
    private String propertyShortDescription;

    /**
     * Creates new <code>BindingDescriptor</code>.
     *
     * @param path name of the binding property/path segment.
     * @param genericValueType value type of the binding. 
     */
    public BindingDescriptor(String path, Type genericValueType) {
        this(path, new TypeHelper(genericValueType));
    }

    /**
     * Creates new <code>BindingDescriptor</code>.
     *
     * @param path name of the binding property/path segment.
     * @param genericValueType value type of the binding. 
     */    
    public BindingDescriptor(String path, TypeHelper genericValueType) {
        this.path = path;
        this.valueType = FormUtils.typeToClass(genericValueType);
        this.genericValueType = genericValueType;
    }

    /**
     * Returns generified value type of the binding. May return <code>null</code>
     * if the type of the binding depends on the context. In such a case the
     * type should be determined using BindingDesignSupport.determineType() method.
     *
     * @return generified value type of the binding or <code>null</code>.
     */
    public TypeHelper getGenericValueType() {
        return genericValueType;
    }

    /**
     * Returns value type of the binding.
     *
     * @return value type of the binding.
     */
    public Class getValueType() {
        return valueType;
    }

    /**
     * Returns name of the binding property/path segment.
     *
     * @return name of the binding property/path segment.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns display name of this binding.
     *
     * @return display name of this binding.
     */
    public String getDisplayName() {
        return propertyDisplayName;
    }

    /**
     * Sets the display name of the binding.
     *
     * @param displayName display name of the binding.
     */
    public void setDisplayName(String displayName) {
        propertyDisplayName = displayName;
    }

    /**
     * Returns description of the binding.
     *
     * @return description of the binding.
     */
    public String getShortDescription() {
        return propertyShortDescription;
    }

    /**
     * Sets the description of the binding.
     *
     * @param description description of the binding.
     */
    public void setShortDescription(String description) {
        propertyShortDescription = description;
    }

    /**
     * Marks the value type of this binding as relative. Type of such a binding
     * may depend on the context and should be determined using
     * <code>BindingDesignSupport.determineType()</code> method.
     */
    public void markTypeAsRelative() {
        genericValueType = null;
    }

    /**
     * Determines whether the value type of this binding depends on the context
     * and should be determined using <code>BindingDesignSupport.determineType()</code> method.
     * 
     * @return <code>true</code> if the value type is relative,
     * returns <code>false</code> otherwise.
     */
    boolean isValueTypeRelative() {
        return (genericValueType == null);
    }

}
