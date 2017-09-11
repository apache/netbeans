/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.form.codestructure;

import java.util.Collection;

/**
 * @author Tomas Pavek
 */

public interface CodeVariable {

    // variable type constants

    // variable scope type (bits 12, 13)
    public static final int NO_VARIABLE = 0x0000; // means just name reserved
    public static final int LOCAL = 0x1000;
    public static final int FIELD = 0x2000;

    // access modifiers - conforms to Modifier class (bits 0, 1, 2)
    public static final int PUBLIC = 0x0001;
    public static final int PRIVATE = 0x0002;
    public static final int PROTECTED = 0x0004;
    public static final int PACKAGE_PRIVATE = 0x0000;

    // other modifiers  - conforms to Modifier class (bits 3, 4, 6, 7)
    public static final int STATIC = 0x0008;
    public static final int FINAL = 0x0010;
    public static final int VOLATILE = 0x0040;
    public static final int TRANSIENT = 0x0080;

    public static final int NO_MODIFIER = 0x0000;

    // explicit local variable declaration in code (bit 14)
    public static final int EXPLICIT_DECLARATION = 0x4000;

    // variable management according to number of expressions attached (bit 15)
    public static final int EXPLICIT_RELEASE = 0x8000;

    // masks
    public static final int SCOPE_MASK = 0x3000;
    public static final int ACCESS_MODIF_MASK = 0x0007;
    public static final int OTHER_MODIF_MASK = 0x00D8;
    public static final int ALL_MODIF_MASK = 0x00DF;
    public static final int DECLARATION_MASK = 0x4000;
    public static final int RELEASE_MASK = 0x8000;
    public static final int ALL_MASK = 0xF0DF;

    static final int DEFAULT_TYPE = SCOPE_MASK | ALL_MODIF_MASK; // 0x30DF;

    // ------

    public int getType();

    public Class getDeclaredType();
    
    public String getDeclaredTypeParameters();

    public String getName();

    public Collection getAttachedExpressions();

    public CodeStatement getDeclaration();

    public CodeStatement getAssignment(CodeExpression expression);
}
