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

package org.netbeans.api.core.ide;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

/** Annotation applicable to any class that extends {@link Node} or
 * static method that returns {@link Node}. Its presence means that
 * the node shall appear in the <em>Services</em> tab.
 * @since org.netbeans.core.ide/1 1.15
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ServicesTabNodeRegistration {
    /** @return programatic name of the node */
    String name();
    /** Human readable name. Use <code>#KEY</code> to reference a key in
     * <code>Bundle.properties</code> file next to the node class
     * or factory method.
     *
     * @return human readable name or reference to bundle
     */
    String displayName();

    /** Description of the node. Use <code>#KEY</code> to reference a key in
     * <code>Bundle.properties</code> file next to the node class
     * or factory method.
     *
     * @return human readable short description or readable to bundle
     */
    String shortDescription() default "";

    /** Icon to use for the node. The icon can have additional variants
     * see {@link AbstractNode#setIconBaseWithExtension(java.lang.String)}
     * for more info.
     *
     */
    String iconResource();

    /** Ordering location of the {@link Node}.
     */
    int position() default Integer.MAX_VALUE;
}
