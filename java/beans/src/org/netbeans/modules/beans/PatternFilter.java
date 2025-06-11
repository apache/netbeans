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

package org.netbeans.modules.beans;

/** Orders and filters members in a pattern node.
*
* @author Petr Hrebejk
*/
public final class PatternFilter {

    /** Specifies a child representing a property. */
    public static final int     PROPERTY = 256;
    /** Specifies a child representing a indexed property */
    public static final int     IDXPROPERTY = 512;
    /** Specifies a child representing a event listener. */
    public static final int     EVENT_SET = 1024;

    /** Does not specify a child type. */
    public static final int     ALL = PROPERTY | IDXPROPERTY | EVENT_SET;

    /** Default order and filtering.
    * Places all fields, constructors, methods, and inner classes (interfaces) together
    * in one block.
    */
    public static final int[]   DEFAULT_ORDER = {PROPERTY | IDXPROPERTY | EVENT_SET};

}
