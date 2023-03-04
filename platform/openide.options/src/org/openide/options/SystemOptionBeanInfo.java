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

package org.openide.options;

import java.beans.*;

import org.openide.options.SystemOption;
import org.openide.util.Exceptions;

/** Empty bean info
*
* @author Jaroslav Tulach
*/
public class SystemOptionBeanInfo extends SimpleBeanInfo {
    /** No properties.
    * @return array of hidden properties
    */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor name = new PropertyDescriptor ("name", SystemOption.class, "getName", null);
            name.setHidden (true);
            PropertyDescriptor helpCtx = new PropertyDescriptor ("helpCtx", SystemOption.class, "getHelpCtx", null);
            helpCtx.setHidden (true);
            return new PropertyDescriptor[] { name, helpCtx };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }
}
