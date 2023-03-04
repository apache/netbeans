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

package org.netbeans.modules.beans.beaninfo;

/** Resource string constants for pattern node icons.
*
* @author Petr Hrebejk
*/
interface IconBases {

    // Properties for Bean Info Features. There should be added S for selected features
    // and N for non selected features at the end of the string.

    public static final String BIF_DESCRIPTOR =
        "org/netbeans/modules/beans/resources/bifDescriptor"; // NOI18N !!! MUST BE CHANGED, BAD ICON

    public static final String BIF_PROPERTY_RW =
        "org/netbeans/modules/beans/resources/bifPropertyRW_"; // NOI18N

    public static final String BIF_PROPERTY_RO =
        "org/netbeans/modules/beans/resources/bifPropertyRO_"; // NOI18N

    public static final String BIF_PROPERTY_WO =
        "org/netbeans/modules/beans/resources/bifPropertyWO_"; // NOI18N

    public static final String BIF_IDXPROPERTY_RW =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyRW_"; // NOI18N

    public static final String BIF_IDXPROPERTY_RO =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyRO_"; // NOI18N

    public static final String BIF_IDXPROPERTY_WO =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyWO_"; // NOI18N

    public static final String BIF_EVENTSET_MULTICAST =
        "org/netbeans/modules/beans/resources/bifEventSetMC_"; // NOI18N

    public static final String BIF_EVENTSET_UNICAST =
        "org/netbeans/modules/beans/resources/bifEventSetUC_"; // NOI18N

    public static final String BIF_METHOD =
        "org/netbeans/modules/beans/resources/bifMethod_"; // NOI18N

}
