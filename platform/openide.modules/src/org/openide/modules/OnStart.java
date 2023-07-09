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
package org.openide.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openide.util.lookup.NamedServiceDefinition;

/** An annotation to place on a {@link Runnable} with public default constructor
 * that you wish to be instantiated and invoked on start of your module.
 * Runnables defined by various modules are invoked in parallel and as soon
 * as possible. It is guaranteed that execution of all runnables is finished
 * before the startup sequence is claimed over.
 * <p>
 * Typical initialization code follows:
 * <pre>
 * {@code @OnStart}
 * <b>public class</b> InitializeMe <b>implements</b> Runnable {
 *     <b>public void</b> run() {
 *       <em>// invoke initialization code</em>
 *     }
 * }
 * </pre>
 * Some modules may want to invoke code when the UI is initialized. In such
 * case consider using <a href="@org-openide-windows@/org/openide/windows/OnShowing.html">
 * {@code @OnShowing}</a> annotation.
 * 
 * @since 7.29
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
@NamedServiceDefinition(
    path="Modules/Start", // NOI18N
    serviceType=Runnable.class
)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface OnStart {
}
