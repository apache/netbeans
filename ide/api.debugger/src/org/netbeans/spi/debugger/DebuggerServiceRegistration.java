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

package org.netbeans.spi.debugger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative registration of debugger service provider implementing a set
 * of interfaces.
 * By marking an implementation class with this annotation,
 * you automatically register that implementation for use by debugger.
 * The class must be public and have a public constructor which takes
 * no arguments or takes {@link ContextProvider} as an argument.
 * @since 1.16
 *
 * @author Martin Entlicher
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface DebuggerServiceRegistration {

    /**
     * An optional path to register this implementation in.
     * Usually the session ID, view name, etc.
     */
    String path() default "";

    /**
     * The list of interfaces that this class implements and wish to register for.
     */
    Class[] types();

    /**
     * Position of this service within its path.
     * @since 1.28
     */
    int position() default Integer.MAX_VALUE;
}
