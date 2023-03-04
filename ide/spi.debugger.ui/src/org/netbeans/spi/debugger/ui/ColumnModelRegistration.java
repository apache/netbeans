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

package org.netbeans.spi.debugger.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Declarative registration of a ColumnModel implementation.
 * By marking the implementation class with this annotation,
 * you automatically register that implementation for use by debugger.
 * The class must be public and have a public constructor which takes
 * no arguments or takes {@link ContextProvider} as an argument.
 * 
 * @author Martin Entlicher
 * @since 2.16
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ColumnModelRegistration {

    /**
     * An optional path to register this implementation in.
     * Usually the session ID, view name, etc.
     */
    String path();

    /**
     * An optional position in which to register this service relative to others.
     * Lower-numbered services are returned in the lookup result first.
     * Services with no specified position are returned last.
     */
    int position() default Integer.MAX_VALUE;
}
