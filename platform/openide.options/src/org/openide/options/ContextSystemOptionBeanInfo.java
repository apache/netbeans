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

package org.openide.options;

import java.beans.*;

import org.openide.options.*;
import org.openide.util.Exceptions;

/** Empty bean info.
*
* @author Jesse Glick
*/
public class ContextSystemOptionBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (SystemOption.class) };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }

    /** No properties.
    * @return array of hidden properties
    */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor beanContextProxy = new PropertyDescriptor ("beanContextProxy", ContextSystemOption.class, "getBeanContextProxy", null);
            beanContextProxy.setHidden (true);
            PropertyDescriptor options = new PropertyDescriptor ("options", ContextSystemOption.class, "getOptions", null);
            options.setHidden (true);
            return new PropertyDescriptor[] { beanContextProxy, options };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }
}
