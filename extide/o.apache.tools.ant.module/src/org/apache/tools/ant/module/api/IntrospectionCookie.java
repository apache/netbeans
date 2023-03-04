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

package org.apache.tools.ant.module.api;

import org.openide.nodes.Node;

/** Represents an object with an associated introspectable class.
 * Used for Ant elements which are matched to some Java class
 * (e.g. for a task or for a subelement).
 * Similar in concept to InstanceCookie; however InstanceCookie
 * requires the cookie to be able to load the actual class and
 * instantiate it (which is not always possible from these elements)
 * and also does not provide a way to get the class name <em>without</em>
 * loading the class (which is useful from these elements).
 * IntrospectedInfo can be used to look up introspection results from
 * the resulting class name.
 * @since 2.3
 * @see IntrospectedInfo
 * @deprecated No longer useful in new UI.
 */
@Deprecated
public interface IntrospectionCookie extends Node.Cookie {
    
    /** Get the name of the class this object is associated with.
     * Objects <em>not</em> associated with a class, or not associated
     * with a known particular class, should not have this cookie.
     * @return the fully-qualified dot-separated class name
     */
    String getClassName ();
    
}
