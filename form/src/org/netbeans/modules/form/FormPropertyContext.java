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

import java.beans.*;

/**
 * An interface representing a context of a FormProperty.
 *
 * @author Tomas Pavek
 */

public interface FormPropertyContext {

    /**
     * Describes whether the FormPropertyEditor can be used for editing properties.
     * This property editor encapsulates multiple property editors which can be used
     * for given property - this feature is not suitable e.g. for event properties,
     * and sometimes not possible beacuase of restrictions in XML storage format
     * (which must stay compatible with previous versions).
     * @return true if multiple property editors can be used (FormPropertyEditor)
     */
    public boolean useMultipleEditors();

    /**
     * Initializes property editor for a property - property editors are usually
     * constructed with no parameters, but often needs some context
     * (e.g. FormAwareEditor needs FormModel and FormProperty).
     * 
     * @param prEd property editor to initialize.
     * @param property property.
     */
    public void initPropertyEditor(PropertyEditor prEd, FormProperty property);

    /**
     * Provides the form the property belongs to. The context is needed for loading
     * classes of property editors (from the right classpath).
     * @return FormModel this property belong to
     */
    public FormModel getFormModel();

    /**
     * Returns the property owner (the object it is a property of). Typically
     * a RADComponent or another property (nested properties).
     * @return Object the owner object of the property
     */
    public Object getOwner();

    /**
     * Implementation of FormPropertyContext for component properties.
     */
    public static class Component implements FormPropertyContext {
        private RADComponent component;

        public Component(RADComponent metacomp) {
            component = metacomp;
        }

        @Override
        public boolean useMultipleEditors() {
            return true;
        }

        @Override
        public void initPropertyEditor(PropertyEditor prEd, FormProperty property) {
            if (prEd instanceof FormAwareEditor)
                ((FormAwareEditor)prEd).setContext(getFormModel(), property);
        }

        @Override
        public FormModel getFormModel() {
            return component.getFormModel();
        }

        @Override
        public RADComponent getOwner() {
            return component;
        }
    }

    /**
     * Implementation of FormPropertyContext for a property that is a
     * "sub-property" of another property (e.g. border support properties).
     */
    public static class SubProperty implements FormPropertyContext {
        private FormProperty parentProperty;

        public SubProperty(FormProperty parentProp) {
            this.parentProperty = parentProp;
        }

        @Override
        public boolean useMultipleEditors() {
            return parentProperty.getPropertyContext().useMultipleEditors();
        }

        @Override
        public void initPropertyEditor(PropertyEditor prEd, FormProperty property) {
            parentProperty.getPropertyContext().initPropertyEditor(prEd, property);
        }

        @Override
        public FormModel getFormModel() {
            return parentProperty.getPropertyContext().getFormModel();
        }

        @Override
        public Object getOwner() {
            return parentProperty;
        }
    }

    /** "Empty" implementation of FormPropertyContext. */
    public static class EmptyImpl implements FormPropertyContext {

        @Override
        public boolean useMultipleEditors() {
            return false;
        }

        @Override
        public void initPropertyEditor(PropertyEditor prEd, FormProperty property) {
            if (prEd instanceof FormAwareEditor) {
                ((FormAwareEditor)prEd).setContext(getFormModel(), property);
            }
        }

        @Override
        public FormModel getFormModel() {
            return null;
        }

        @Override
        public Object getOwner() {
            return null;
        }

        // ------

        public static EmptyImpl getInstance() {
            if (theInstance == null)
                theInstance = new EmptyImpl();
            return theInstance;
        }

        static private EmptyImpl theInstance = null;
    }
}
