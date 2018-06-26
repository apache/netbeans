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
 * Represents the try statement
 * <pre>e.g.<pre>
 * try {
 *   statements...
 * } catch (Exception $e) {
 *   statements...
 * } catch (AnotherException $ae) {
 *   statements...
 * }
 */
public class TryStatement extends Statement {

    private Block tryStatement;
    private ArrayList<CatchClause> catchClauses = new ArrayList<>();
    private final FinallyClause finallyClause;

    private TryStatement(int start, int end, Block tryStatement, CatchClause[] catchClauses, FinallyClause finallyClause) {
        super(start, end);
        if (tryStatement == null) {
            throw new IllegalArgumentException();
        }
        this.tryStatement = tryStatement;
        if (catchClauses != null) {
            this.catchClauses.addAll(Arrays.asList(catchClauses));
        }
        this.finallyClause = finallyClause;
    }

    public TryStatement(int start, int end, Block tryStatement, List<CatchClause> catchClauses, FinallyClause finallyClause) {
        this(start, end, tryStatement, catchClauses == null ? null : (CatchClause[]) catchClauses.toArray(new CatchClause[catchClauses.size()]), finallyClause);
    }

    /**
     * Returns the body of this try statement.
     *
     * @return the try body
     */
    public Block getBody() {
        return this.tryStatement;
    }

    /**
     * Returns the live ordered list of catch clauses for this try statement.
     *
     * @return the live list of catch clauses
     *    (element type: <code>CatchClause</code>)
     */
    public List<CatchClause> getCatchClauses() {
        return this.catchClauses;
    }

    public FinallyClause getFinallyClause() {
        return finallyClause;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CatchClause catchClause : getCatchClauses()) {
            sb.append(catchClause);
        }
        return "try" + getBody() + sb.toString(); //NOI18N
    }

}
