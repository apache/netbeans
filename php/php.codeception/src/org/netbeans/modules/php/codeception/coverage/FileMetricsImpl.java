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

import org.netbeans.modules.php.spi.testing.coverage.FileMetrics;

class FileMetricsImpl extends ClassMetricsImpl implements FileMetrics {

    private final int loc;
    private final int ncloc;
    private final int classes;


    public FileMetricsImpl(int loc, int ncloc, int classes, int methods, int coveredMethods, int conditionals,
            int coveredConditionals, int statements, int coveredStatements, int elements, int coveredElements) {
        super(methods, coveredMethods, conditionals, coveredConditionals, statements, coveredStatements, elements, coveredElements);
        this.loc = loc;
        this.ncloc = ncloc;
        this.classes = classes;
    }

    @Override
    public int getLineCount() {
        return loc;
    }

    public int getNcloc() {
        return ncloc;
    }

    public int getClasses() {
        return classes;
    }

    @Override
    public String toString() {
        return String.format("FileMetricsImpl{loc: %d, ncloc: %d, classes: %d, methods: %d, coveredMethods: %d, " // NOI18N
                + "statements: %d, coveredStatements: %d, elements: %d, coveredElements: %d}", loc, ncloc, classes, getMethods(), getCoveredMethods(), // NOI18N
                getStatements(), getCoveredStatements(), getElements(), getCoveredElements()); // NOI18N
    }

}
