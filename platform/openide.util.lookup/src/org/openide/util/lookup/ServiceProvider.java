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

package org.openide.util.lookup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openide.util.Lookup;

/**
 * Declarative registration of a singleton service provider.
 * By marking an implementation class with this annotation,
 * you automatically register that implementation, normally in {@link Lookup#getDefault}.
 * The class must be public and have a public no-argument constructor.
 * <p>Example of usage:
 * <pre>
 * package my.module;
 * import org.netbeans.spi.whatever.Thing;
 * import org.openide.util.lookup.ServiceProvider;
 * &#64;ServiceProvider(service=Thing.class)
 * public class MyThing implements Thing {...}
 * </pre>
 * <p>would result in a resource file <code>META-INF/services/org.netbeans.spi.whatever.Thing</code>
 * containing the single line of text: <code>my.module.MyThing</code>
 * @see Lookups#metaInfServices(ClassLoader)
 * @since org.openide.util 7.20
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ServiceProvider {

    /**
     * The interface (or abstract class) to register this implementation under.
     * It is an error if the implementation class is not in fact assignable to the interface.
     * <p>If you need to register one class under multiple interfaces, use {@link ServiceProviders}.
     * <p>Requests to look up the specified interface should result in this implementation.
     * Requests for any other types may or may not result in this implementation even if the
     * implementation is assignable to those types.
     * @return type to register this implementation
     */
    Class<?> service();

    /**
     * An optional position in which to register this service relative to others.
     * Lower-numbered services are returned in the lookup result first.
     * Services with no specified position are returned last.
     * @return position to register the service
     */
    int position() default Integer.MAX_VALUE;

    /**
     * An optional list of implementations (given as fully-qualified class names) which this implementation supersedes.
     * If specified, those implementations will not be loaded even if they were registered.
     * Useful on occasion to cancel a generic implementation and replace it with a more advanced one.
     *
     * <p><i>Note:</i> Dollar sign ($) is used in inner class names. For example
     * <code>org.netbeans.modules.openfile.FileChooser$JavaFilesFilter</code>.</p>
     * @return set of implementations this implementation supersedes
     */
    String[] supersedes() default {};

    /**
     * An optional path to register this implementation in.
     * For example, <code>Projects/sometype/Nodes</code> could be used.
     * This style of registration would be recognized by {@link Lookups#forPath}
     * rather than {@link Lookup#getDefault}.
     * <p>
     * The providers of a registration slot are advised to have
     * a look at {@link NamedServiceDefinition} and consider using it since 
     * version 8.14.
     * The {@link NamedServiceDefinition} offers various benefits over 
     * plain {@link #path()} usage including type checking and lower 
     * possibility of typos.
     * @return path to register implementation
     */
    String path() default "";

}
