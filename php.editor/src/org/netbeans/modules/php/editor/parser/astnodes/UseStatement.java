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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a 'use' statement
 * <pre>e.g.
 * use MyNamespace;
 * use MyNamespace as MyAlias;
 * use MyProject\Sub\Level as MyAlias;
 * use \MyProject\Sub\Level as MyAlias;
 * use \MyProject\Sub\Level as MyAlias, MyNamespace as OtherAlias, MyOtherNamespace;
 * use some\namespace\{ClassA, sub\ClassB, ClassC as C};
 * </pre>
 */
public class UseStatement extends Statement {
    private final List<UseStatementPart> parts;
    private final Type type;

    public enum Type {
        TYPE("TYPE"), //NOI18N
        CONST("CONST"), //NOI18N
        FUNCTION("FUNCTION"); //NOI18N

        private final String type;

        private Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

    };

    public UseStatement(int start, int end, List parts, Type type) {
        super(start, end);

        if (parts == null || parts.isEmpty() || type == null) {
            throw new IllegalArgumentException();
        }

        this.parts = new ArrayList<>(parts);
        this.type = type;
    }

    public UseStatement(int start, int end, List parts) {
        this(start, end, parts, Type.TYPE);
    }

    public UseStatement(int start, int end, SingleUseStatementPart[] parts, Type type) {
        this(start, end, Arrays.asList(parts), type);
    }

    public UseStatement(int start, int end, SingleUseStatementPart[] parts) {
        this(start, end, parts, Type.TYPE);
    }

    /**
     * Returns the list of parts of this 'use' statement.
     * @return list of this statement parts
     */
    public List<UseStatementPart> getParts() {
        return Collections.unmodifiableList(parts);
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (UseStatementPart useStatementPart : parts) {
            if (sb.length() > 0) {
                sb.append(", "); // NOI18N
            }
            sb.append(useStatementPart);
        }
        return "use " + type.toString() + " " + sb.toString(); //NOI18N
    }

}
