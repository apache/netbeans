/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.javascript.cdtdebug.ui.eval;

import org.netbeans.modules.javascript.cdtdebug.vars.Variable;

final class VarOrError {

    private final Variable var;
    private final String err;

    public VarOrError(Variable var) {
        this.var = var;
        this.err = null;
    }

    public VarOrError(String err) {
        this.var = null;
        this.err = err;
    }

    public boolean hasVar() {
        return var != null;
    }

    public Variable getVar() {
        return var;
    }

    public String getError() {
        return err;
    }
}
