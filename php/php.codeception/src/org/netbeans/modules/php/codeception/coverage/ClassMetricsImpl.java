/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
