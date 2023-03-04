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

        private static EmptyImpl theInstance = null;
    }
}
