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

package org.openide.cookies;

import java.io.IOException;
import org.openide.nodes.Node;

/**
 * Cookie that should be provided by all nodes that are able
 * to create or return an "instance".
 * Generally this is used to register objects declaratively in XML layers.
 * The most commonly used implementation seems to be
 * <a href="@org-openide-loaders@/org/openide/loaders/InstanceDataObject.html">InstanceDataObject</a>.
 *
 * @author Jaroslav Tulach
 */
public interface InstanceCookie/*<T>*/ extends Node.Cookie {

    /**
     * The name of {@link #instanceClass}.
     * Should be the same as <code>instanceClass().getName()</code>
     * but may be able to avoid actually loading the class.
     * <p><strong>Generally this method should not be used.</strong>
     * @return the instance class name
     */
    public String instanceName();

    /**
     * The type that the instance is expected to be assignable to.
     * Can be used to test whether the instance is of an appropriate
     * class without actually creating it.
     * <p><strong>Generally this method should not be used.</strong>
     * To test whether the instance will be assignable to some type,
     * use {@link InstanceCookie.Of#instanceOf} instead.
     * To actually load instances, use {@link #instanceCreate}; if your
     * objects are not naturally singletons (e.g. public no-argument constructor),
     * the instances should rather be of some kind of <em>factory</em> you define.
     *
     * @return the type (or perhaps some interesting supertype) of the instance
     * @exception IOException if metadata about the instance could not be read, etc.
     * @exception ClassNotFoundException if the instance type could not be loaded
     */
    public Class<?/*T*/> instanceClass() throws IOException, ClassNotFoundException;

    /**
     * Create or obtain an instance. For example 
     * <a href="@org-openide-loaders@/org/openide/loaders/InstanceDataObject.html#instanceCreate()">InstanceDataObject</a>
     * (one of the most often used implementations of {@link InstanceCookie}) caches
     * previously returned instances.
     * 
     * @return an object assignable to {@link #instanceClass}
     * @throws IOException for the same reasons as {@link #instanceClass}, or an object could not be deserialized, etc.
     * @throws ClassNotFoundException for the same reasons as {@link #instanceClass}
    */
    public Object/*T*/ instanceCreate() throws IOException, ClassNotFoundException;

    /**
     * Enhanced cookie that can answer queries about the type of the
     * instance it creates. It does not add any additional ability except to
     * improve performance, because it is not necessary to load
     * the actual class of the object into memory.
     *
     * @since 1.4
     */
    public interface Of extends InstanceCookie {
        /**
         * Checks if the object created by this cookie is an
         * instance of the given type. The same as
         * <code>type.isAssignableFrom(instanceClass())</code>
         * But this can prevent the actual class from being
         * loaded into the Java VM.
         *
         * @param type the class type we want to check
         * @return true if this cookie will produce an instance of the given type
        */
        public boolean instanceOf(Class<?> type);
    }

}
