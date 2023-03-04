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

package org.openide.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import org.openide.util.lookup.NamedServiceDefinition;

/**
 * Replacement for {@link URLStreamHandlerFactory} within the NetBeans platform.
 * (The JVM only permits one global factory to be set at a time,
 * whereas various independent modules may wish to register handlers.)
 * May be placed on a {@link URLStreamHandler} implementation to register it.
 * Your handler will be loaded and used if and when a URL of a matching protocol is created.
 * <p>A {@link URLStreamHandlerFactory} which uses these registrations may be found in {@link Lookup#getDefault}.
 * This factory is active whenever the module system is loaded.
 * You may also wish to call {@link URL#setURLStreamHandlerFactory}
 * from a unit test or otherwise without the module system active.
 * @since org.openide.util 7.31
 */
@NamedServiceDefinition(path="URLStreamHandler/@protocol()", serviceType=URLStreamHandler.class)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface URLStreamHandlerRegistration {

    /**
     * URL protocol(s) which are handled.
     * {@link URLStreamHandler#openConnection} will be called with a matching {@link URL#getProtocol}.
     * @return list of protocol which are handled
     */
    String[] protocol();

    /**
     * An optional position in which to register this handler relative to others.
     * The lowest-numbered handler is used in favor of any others, including unnumbered handlers.
     * @return position to register this handler
     */
    int position() default Integer.MAX_VALUE;

}
