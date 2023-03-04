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

package org.openide.explorer.propertysheet;

import java.beans.PropertyEditor;

/**
 * An extension interface for property editors that hides
 * all the necessary communication with the property sheet. ExPropertyEditor
 * is able to accept an instance of PropertyEnv class - this
 * environment passes additional information to the editor.
 * The <code>PropertyEnv</code> instance is typically used
 * to set the valid/invalid state of the property, and to
 * retrieve a reference to the Node.Property or PropertyDescriptor
 * for the property being edited.
 * @author  dstrupl
 */
public interface ExPropertyEditor extends PropertyEditor {
    /**
     * If you want to enable/disable the OK button on the custom
     * property editor panel you can fire a property change event
     * with boolean value. You don't have to implement the ExPropertyEditor
     * interface for this feature to be turned on.
     * When firing property change event PROP_VALUE_VALID is the name
     * and an instance of java.lang.Boolean should be passed as a value.
     */
    public static final String PROP_VALUE_VALID = "propertyValueValid"; // NOI18N

    /**
     * If you want to add custom help ID on the custom property
     * editor panel you can store its value in PROPERTY_HELP_ID property.
     */
    public static final String PROPERTY_HELP_ID = "helpID"; // NOI18N

    /**
     * This method is called by the property sheet to pass
     * the environment to the property editor.  The typical use case is for
     * the ExPropertyEditor to call <code>
     * env.getFeatureDescriptor().getValue (String key)</code> to retrieve
     * any hints the Property object may supply regarding how the property
     * editor should behave (such as providing alternate text representations
     * of &quot;true&quot; and &quot;false&quot; for a Boolean property
     * editor).<P>Property editors that support an invalid state (typically
     * used to disable the OK button in custom editor dialogs) should cache
     * the env object and update the env's state on calls to <code>setValue()</code>.
     * <P><strong>Note:</strong> This method may be called more than once
     * during the lifetime of a property editor.  In particular, custom
     * property editors which want to change the state of the OK button by
     * calling <code>PropertyEnv.setState(PropertyEnv.STATE_VALID)</code>
     * should not assume that the instance of PropertyEnv most recently
     * passed to attachEnv() on the underlying property editor is the same
     * instance that controls the dialog they are displayed in.  Custom
     * editors which wish to control the state of the OK button should cache
     * the last-set PropertyEnv at the time they are instantiated.
     */
    public void attachEnv(PropertyEnv env);
}
