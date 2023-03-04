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

package org.netbeans.modules.java.hints.spiimpl.refactoring;

import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Scope;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.Lookup;

public class FindDuplicatesRefactoring extends AbstractRefactoring {

    private final boolean query;
    private Iterable<? extends HintDescription> patterns;
    private Scope scope;
    private boolean verify = true;

    public FindDuplicatesRefactoring(boolean query) {
        super(Lookup.EMPTY);
        this.query = query;
    }

    public boolean isQuery() {
        return query;
    }

    public synchronized Iterable<? extends HintDescription> getPattern() {
        return patterns;
    }

    public synchronized void setPattern(Iterable<? extends HintDescription> patterns) {
        this.patterns = patterns;
    }

    public synchronized Scope getScope() {
        return scope;
    }

    public synchronized void setScope(Scope scope) {
        this.scope = scope;
    }

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

}
