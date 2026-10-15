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

package org.openide.filesystems.spi;

/**
 * Creates instance of the desired class based on the file.
 * Module providers may register factories to control creation of instances
 * from files that use only file name or {@code instanceClass} attribute.
 * <p>
 * Registered factories are called in the registration order, until one provides
 * a non-null result, which become the file's created instance. If all factories
 * return {@code null}, the FileSystems API will create the instance using the default
 * constructor.
 * 
 * <p>
 * Module implementors are encouraged to use the <code>instanceCreate</code> attribute
 * instead.
 * 
 * @author sdedic
 * @since 9.0
 */
public interface CustomInstanceFactory {
    /**
     * Creates an instance of the class.
     * 
     * @param <T> the desired type
     * @param clazz the desired type
     * @return an instance of the `clazz' or <code>null</code>
     */
    public <T> T createInstance(Class<T> clazz);
}
