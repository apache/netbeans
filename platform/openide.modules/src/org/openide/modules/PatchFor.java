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

package org.openide.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class, which provides binary-compatible implementation for a changed API class.
 * When removing <code>@deprecated</code> code from API, it is still desirable to retain
 * backward binary compatibility. Because of JVM method/field/class resolution
 * mechanism, it is permitted to move the implementation upwards the inheritance
 * hierarchy. However, the superclass that receives the methods must not be
 * visible in the sources (otherwise the change would not serve any good) and
 * specifically, must not be referenced from <i>extends</i> clause of the changed
 * class. 
 * <p/>
 * The compatibility superclass ought to reside in a <b>different module</b>
 * to allow reduce type dependencies and should be only injected into
 * inheritance chain if and only if some of enabled modules depend on the
 * obsolete version of the API module.
 * <p/>
 * <hr/>
 * This annotation is designed to mark code which should be injected to provide 
 * backward compatibility and will provide implementation for removed API methods.
 * The marked class will be used as a <b>superclass</b> 
 * of the API class identified by value of the annotation, so old clients can 
 * still use removed members at run time.
 * <p/>
 * The injected superclass <b>must</b> extend the same type as the original
 * API class, so that API-visible inheritance chain is preserved. It must declare
 * all non-private constructors as the original superclass.
 * <p/>
 * The module that contains {@code PatchFor} classes <b>must be</b> defined as
 * <b>module fragment</b>, since it has to share classloader with the patched
 * module: put
 * <code><pre>
 * OpenIDE-Module-Fragment-Host: codenamebase
 * </pre></code>
 * into the Module's manifest. The <i><code>codenamebase</code></i> must identify the host module
 * which contains the class(es) to be patched.
 * <p/>
 * Note that it is not possible to support <b>removed constructors</b> this way; the
 * constructor has to be physically present on the API class. To provide backward-compatibile
 * code injection for constructors, please use {@link ConstructorDelegate} annotation.
 * <p/>
 * For examples, plese see the <a href="http://wiki.netbeans.org/BackwardCompatibilityPatches">NetBeans Wiki</a>
 * @since 7.44
 * @author sdedic
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface PatchFor {
    /**
     * The manifest header name.
     */
    public static final String MANIFEST_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host";
    
    /**
     * @return Class that should be changed to extend the annotated class
     */
    public Class<?> value();
}
