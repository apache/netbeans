/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2017 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a list expression. The list contains key => value list, variables
 * and/or other lists.<br>
 * <b>Note:</b>Can be contained ArrayCreation instead of ListVariable when list
 * is nested with new syntax. Because it is the same syntax pattern as short
 * array syntax.
 *
 * <pre>e.g.
 * list($a,$b) = array (1,2),
 * list($a, list($b, $c)),
 * list("id" => $id, "name" => $name) = $data[0]; // PHP7.1,
 * [$a, $b, $c] = [1, 2, 3]; // PHP7.1,
 * ["a" => $a, "b" => $b, "c" => $c] = ["a" => 1, "b" => 2, "c" => 3]; // PHP7.1
 * </pre>
 */
public class ListVariable extends VariableBase {

    public enum SyntaxType {
        OLD {
            @Override
            String toString(String innerElements) {
                return "list(" + innerElements + ")"; // NOI18N
            }
        },
        NEW {
            @Override
            String toString(String innerElements) {
                return "[" + innerElements + "]"; // NOI18N
            }
        };

        abstract String toString(String innerElements);
    }

    private final List<ArrayElement> elements = new ArrayList<>();
    private final SyntaxType syntaxType;

    private ListVariable(int start, int end, ArrayElement[] elements, SyntaxType syntaxType) {
        super(start, end);

        if (elements == null) {
            throw new IllegalArgumentException();
        }
        this.elements.addAll(Arrays.asList(elements));
        this.syntaxType = syntaxType;
    }

    public ListVariable(int start, int end, List<ArrayElement> elements, SyntaxType syntaxType) {
        this(start, end, elements == null ? null : (ArrayElement[]) elements.toArray(new ArrayElement[elements.size()]), syntaxType);
    }

    /**
     * @return the list of elements
     */
    public List<ArrayElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public SyntaxType getSyntaxType() {
        return syntaxType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        getElements().forEach((element) -> {
            sb.append(element).append(","); //NOI18N
        });
        return syntaxType.toString(sb.toString());
    }

}
