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

package org.netbeans.modules.beans;

/** Names of properties of patterns.
*
*
* @author Petr Hrebejk
*/
public interface PatternProperties {
    /** Name of type property for all {@link PropertyPattern}s.
    */
    public static final String PROP_TYPE = "type"; // NOI18N

    public static final String PROP_MODE = "mode"; // NOI18N

    public static final String PROP_NAME = "name"; // NOI18N

    public static final String PROP_GETTER = "getter"; // NOI18N

    public static final String PROP_SETTER = "setter"; // NOI18N

    public static final String PROP_ESTIMATEDFIELD = "estimatedField"; // NOI18N

    public static final String PROP_INDEXEDTYPE = "indexedType"; // NOI18N

    public static final String PROP_INDEXEDGETTER = "indexedGetter"; // NOI18N

    public static final String PROP_INDEXEDSETTER = "indexedSetter"; // NOI18N

    public static final String PROP_ADDLISTENER = "addListener"; // NOI18N

    public static final String PROP_REMOVELISTENER = "removeListener"; // NOI18N

    public static final String PROP_ISUNICAST = "isUnicast"; // NOI18N
}
