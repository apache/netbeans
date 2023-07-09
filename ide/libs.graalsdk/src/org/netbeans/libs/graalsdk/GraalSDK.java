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
package org.netbeans.libs.graalsdk;

import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;

/**
 * Integration of <a href="@org-netbeans-api-scripting@/overview-summary.html">NetBeans Scripting</a>
 * API and GraalVM; see the {@link org.netbeans.libs.graalsdk tutorial} for more details.
 * <h2>Security</h2>
 * <p>
 * By default all the <a href="https://graalvm.org">GraalVM</a> engines
 * (named <code>GraalVM:something</code>)
 * run in a very restricted, secure sandbox:
 * </p>
 * {@snippet file="org/netbeans/libs/graalsdk/impl/GraalContext.java" region="SANDBOX"}
 * <p>
 * The languages cannot access local files, ports, etc. They can access <b>public</b>
 * fields and <b>public</b> methods of objects passed into their scripts (but not
 * those methods exposed by base {@link Object} class). The scripts can access
 * elements of Java arrays and {@link List} elements. Methods of a 
 * {@linkplain FunctionalInterface functional interfaces} are callable by the 
 * scripts.
 * </p>
 * <p>
 * For some languages such restrictions are too tight. They
 * need to gain wider access. This can be done by setting <code>allowAllAccess</code>
 * attribute to {@code true}:
 * </p>
 * {@snippet file="org/netbeans/libs/graalsdk/ScriptingTutorial.java" region="allowAllAccess"}
 * <p>
 * Once enabled, the {@link HostAccess#ALL} and {@link Builder#allowAllAccess(boolean) allowAllAccess(true)}
 * is then used to construct the engine's environment.
 * </p>
 * <h2>Polyglot API Access</h2>
 * <p>
 * As a consequence of packaging the GraalVM APIs, it is possible to request this module and gain access to
 * {@link org.graalvm.polyglot} package directly. Consult {@link Context} and {@link Engine}
 * classes as a starting points when
 * accessing the Graal SDK directly. Preferably use only if the 
 * <a href="@org-netbeans-api-scripting@/overview-summary.html">NetBeans Scripting</a>
 * API wrapper isn't good enough.
 */
public final class GraalSDK {
    private GraalSDK() {
    }
}
