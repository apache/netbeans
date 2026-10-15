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
package org.openide.windows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openide.util.lookup.NamedServiceDefinition;

/** Annotation to place on a {@link Runnable} with default constructor
 * which should be invoked as soon as the window system is shown. The
 * runnables are invoked in AWT event dispatch thread one by one:
 * <pre>
 * {@code @OnShowing}
 * <b>public class</b> ShowSomething <b>implements</b> Runnable {
 *   <b>public void</b> run() {
 *     assert EventQueue.isDispatchThread();
 *     <em>// do something visual</em>
 *   }
 * }
 * </pre>
 * 
 * @since 6.54
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
@NamedServiceDefinition(
    path="Modules/UIReady", // NOI18N
    serviceType=Runnable.class,
    position="position" // NOI18N
)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface OnShowing {
    /** Allows to specify order between the individual runnables
     * registered by different modules
     */
    public int position() default Integer.MAX_VALUE;
}
