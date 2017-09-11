/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
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
