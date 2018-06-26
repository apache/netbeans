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
 * Represents a declare statement
 * <pre>e.g.<pre> declare(ticks=1) { }
 * declare(ticks=2) { for ($x = 1; $x < 50; ++$x) {  }  }
 */
public class DeclareStatement extends Statement {

    private final ArrayList<Identifier> directiveNames = new ArrayList<>();
    private final ArrayList<Expression> directiveValues = new ArrayList<>();
    private Statement body;

    private DeclareStatement(int start, int end, Identifier[] directiveNames, Expression[] directiveValues, Statement action) {
        super(start, end);

        if (directiveNames == null || directiveValues == null || directiveNames.length != directiveValues.length) {
            throw new IllegalArgumentException();
        }
        this.directiveNames.addAll(Arrays.asList(directiveNames));
        this.directiveValues.addAll(Arrays.asList(directiveValues));
        this.body = action;
    }

    public DeclareStatement(int start, int end, List<Identifier> directiveNames, List<Expression> directiveValues, Statement action) {
        this(start, end,
                directiveNames == null ? null : (Identifier[]) directiveNames.toArray(new Identifier[directiveNames.size()]),
                directiveValues == null ? null : (Expression[]) directiveValues.toArray(new Expression[directiveValues.size()]),
                action);
    }

    /**
     * The list of directive names
     *
     * @return List of directive names
     */
    public List<Identifier> getDirectiveNames() {
        return directiveNames;
    }

    /**
     * The list of directive values
     *
     * @return List of directive values
     */
    public List<Expression> getDirectiveValues() {
        return directiveValues;
    }

    /**
     * The body of this declare statement
     *
     * @return body of this this declare statement
     */
    public Statement getBody() {
        return this.body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
