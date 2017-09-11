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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.hints;

import java.lang.annotation.Target;

/**Specifies a type of a variable. During the matching process, only those
 * sections of the source code that have the given type are considered.
 *
 * @author Jan Lahoda
 */
@Target({})
public @interface ConstraintVariableType {

    /**Variable name, must start with the dollar sign (<code>$</code>).
     * Variable<code>$this</code> is automatically bound to the current class.
     */
    public String variable();

    /**The required type of the section of source code. The value must be a type
     * per JLS 4.1, i.e. a primitive type (JLS 4.2), or a reference type (JLS 4.3).
     * All elements of the type must be resolvable when written to any Java file,
     * they may not contain e.g. references to type variables, simple names, etc.
     *
     * The type may include any actual type arguments, including wildcard.
     *
     * While matching, the type of the tree that corresponds to the variable in
     * the actual occurrence candidate is accepted if it is assignable into the
     * variable defined by the attribute.
     */
    public String type();
    
}
