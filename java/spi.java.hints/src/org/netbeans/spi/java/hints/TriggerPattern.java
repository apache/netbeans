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

package org.netbeans.spi.java.hints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**Find parts of the source code that satisfy the given pattern, and invoke the method
 * that is annotated with this annotation.
 *
 * The method must be {@code public static}, the return type must either be assignable to
 * {@link ErrorDescription} or to {@link Iterable}{@code <? extends }{@link ErrorDescription}{@code >}.
 * Its sole parameter must be {@link HintContext}.
 *
 * @author lahvac
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface TriggerPattern {

    /**
     * Pattern to match on.
     * The pattern consists of:
     * <ul>
     *     <li>a single Java expression</li>
     *     <li>a single Java statement</li>
     *     <li>multiple Java statements</li>
     *     <li>a Java field, method or class</li>
     * </ul>
     *
     * Variables (identifiers starting with {@code $}) can be used to replace part of the pattern.
     * During matching, the actual part of the AST that corresponds to the variable in the pattern
     * will be "bound" to the variable. Variables whose names that do not end with a {@code $} ("single" variables)
     * will be bound to exactly one AST node, whereas variables whose names end with a {@code $} ("multi" variables)
     * will be bound to any number of consecutive AST nodes (with the same AST node as a parent).
     *
     * The actual AST nodes that were bound to single variables are available through {@link HintContext#getVariables() },
     * nodes bound to multi variables are available through {@link HintContext#getMultiVariables() }.
     *
     * For variables that represent an expression, a type constraint can be specified using the
     * {@link #constraints() } attribute.
     *
     * All classes should be referred to using FQNs.
     */
    public String value();
    /**Expected types for variables from the {@link #value() pattern}.
     */
    public ConstraintVariableType[] constraints() default {};

}
