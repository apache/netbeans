/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.explorer.propertysheet.editors;

import org.openide.explorer.propertysheet.PropertyEnv;

/**
* Enhances standard custom property editor with the possibility to return the customized value.
* I.e. the custom property editor does not need to fire property changes upon
* modifications; the property dialog manager
* instead sets the acquired value after the custom editor is closed.
*
* @author  Ian Formanek
* @deprecated Use {@link PropertyEnv} instead. An example of what needs to be
*    done can be found in the rewrite of
*    <a href="https://github.com/apache/netbeans/tree/master/platform/o.n.core/src/org/netbeans/beaninfo/editors/RectangleCustomEditor.java">RectangleCustomEditor</a>.
*  Another example showing the changes in property editor as well as in its
*  custom component can be found in
*  <a href="https://github.com/apache/netbeans/tree/master/platform/openide.execution/src/org/openide/execution/NbProcessDescriptor.java">NbProcessDescriptor{,Custom}Editor</a>.
*/
public @Deprecated interface EnhancedCustomPropertyEditor {
    /** Get the customized property value.
    * @return the property value
    * @exception IllegalStateException when the custom property editor does not contain a valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue() throws IllegalStateException;
}
