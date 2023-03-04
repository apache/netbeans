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

import java.beans.FeatureDescriptor;


/**
 * An extension to the PropertyModel interface that allows
 * the property to supply information for ExPropertyEditor.
 * @deprecated - Use PropertySupport.Reflection or BeanNode if you need to
 *  expose bean properties
 * @author David Strupl
 */
public @Deprecated interface ExPropertyModel extends PropertyModel {
    /**
     * Returns an array of beans/nodes that this property belongs
     * to.
     */
    public Object[] getBeans();

    /**
     * Returns descriptor describing the property.
     */
    public FeatureDescriptor getFeatureDescriptor();
}
