/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.codeception.coverage;

class ClassMetricsImpl {

    private final int methods;
    private final int coveredMethods;
    private final int conditionals;
    private final int coveredConditionals;
    private final int statements;
    private final int coveredStatements;
    private final int elements;
    private final int coveredElements;


    public ClassMetricsImpl(int methods, int coveredMethods, int conditionals, int coveredConditionals, int statements,
            int coveredStatements, int elements, int coveredElements) {
        this.methods = methods;
        this.coveredMethods = coveredMethods;
        this.conditionals = conditionals;
        this.coveredConditionals = coveredConditionals;
        this.statements = statements;
        this.coveredStatements = coveredStatements;
        this.elements = elements;
        this.coveredElements = coveredElements;
    }

    public int getMethods() {
        return methods;
    }

    public int getCoveredMethods() {
        return coveredMethods;
    }

    public int getConditionals() {
        return conditionals;
    }

    public int getCoveredConditionals() {
        return coveredConditionals;
    }

    public int getStatements() {
        return statements;
    }

    public int getCoveredStatements() {
        return coveredStatements;
    }

    public int getElements() {
        return elements;
    }

    public int getCoveredElements() {
        return coveredElements;
    }

    @Override
    public String toString() {
        return String.format("ClassMetricsImpl{methods: %d, coveredMethods: %d, conditionals: %d, coveredConditionals: %d, statements: %d, " // NOI18N
                + "coveredStatements: %d, elements: %d, coveredElements: %d}", methods, coveredMethods, conditionals, coveredConditionals, // NOI18N
                statements, coveredStatements, elements, coveredElements);
    }

}
