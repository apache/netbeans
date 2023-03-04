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
import java.util.concurrent.Callable;
import org.openide.util.lookup.NamedServiceDefinition;

/** Annotation that can be applied to {@link Runnable} or 
 * {@link Callable}{@code <Boolean>} subclasses with default constructor
 * which will be invoked during shutdown sequence or when the module
 * is being shutdown.
 * <p>
 * First of all call callables are consulted to allow or deny proceeding
 * with the shutdown:
 * <pre>
 * {@code @OnStop}
 * <b>public class</b> AskTheUser <b>implements</b> Callable<Boolean> {
 *   <b>public</b> Boolean call() {
 *     <b>return</b> isItOKToShutdown() ? Boolean.TRUE : Boolean.FALSE;
 *   }
 * }
 * </pre>
 * If the shutdown is approved, all runnables registered are acknowledged and
 * can perform the shutdown cleanup. The runnables are invoked in parallel. 
 * It is guaranteed their execution is finished before the shutdown sequence
 * is over:
 * <pre>
 * {@code @OnStop}
 * <b>public class</b> Cleanup <b>implements</b> Runnable {
 *   <b>public void</b> run() {
 *     <em>// do some cleanup</em>
 *   }
 * }
 * </pre>
 *
 * @since 7.29
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@NamedServiceDefinition(
    path="Modules/Stop", serviceType={ Runnable.class, Callable.class }
)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface OnStop {
}
