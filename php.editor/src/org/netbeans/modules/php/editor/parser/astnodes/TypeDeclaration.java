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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents base class for class declaration and interface declaration
 */
public abstract class TypeDeclaration extends Statement {

    private Identifier name;
    private ArrayList<Expression> interfaces = new ArrayList<>();
    private Block body;

    public TypeDeclaration(int start, int end, final Identifier name, final Expression[] interfaces, final Block body) {
        super(start, end);

        if (name == null || body == null) {
            throw new IllegalArgumentException();
        }

        this.name = name;
        this.body = body;

        if (interfaces != null) {
            this.interfaces.addAll(Arrays.asList(interfaces));
        }
    }

    /**
     * The body component of this type declaration node
     * @return body component of this type declaration node
     */
    public Block getBody() {
        return body;
    }

    /**
     * The name of the type declaration node
     * @return name of the type declaration node
     */
    public Identifier getName() {
        return this.name;
    }

    /**
     * List of interfaces that this type implements / extends
     */
    public List<Expression> getInterfaes() {
        return this.interfaces;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getInterfaes()) {
            sb.append(expression).append(","); //NOI18N
        }
        return getName() + (sb.length() > 0 ? " " + sb.toString() : " ") + getBody(); //NOI18N
    }

}
