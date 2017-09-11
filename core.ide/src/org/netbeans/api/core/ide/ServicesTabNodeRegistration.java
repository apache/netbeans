/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
