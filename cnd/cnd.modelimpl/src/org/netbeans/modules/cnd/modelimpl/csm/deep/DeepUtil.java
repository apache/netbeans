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
package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmCondition;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;

/**
 * Misc static methods used by deep impls
 */
public class DeepUtil {    

    public static List<CsmScopeElement> merge(CsmVariable var, List<CsmStatement> statements) {
        if (var == null) {
            @SuppressWarnings("unchecked")
            List<CsmScopeElement> out = (List) statements;
            return out;
        } else {
            List<CsmScopeElement> l = new ArrayList<>();
            l.add(var);
            l.addAll(statements);
            return l;
        }
    }

    public static List<CsmScopeElement> merge(CsmCondition condition, CsmStatement statement) {
        return merge(condition == null ? null : condition.getDeclaration(), statement);
    }

    public static List<CsmScopeElement> merge(CsmCondition condition, CsmStatement statement1, CsmStatement statement2) {
        CsmVariable var = (condition == null) ? (CsmVariable) null : condition.getDeclaration();
        List<CsmScopeElement> l = merge(var, statement1);
        if (statement2 != null) {
            l.add(statement2);
        }
        return l;
    }

    public static List<CsmScopeElement> merge(CsmVariable var, CsmStatement statement) {
        List<CsmScopeElement> l = new ArrayList<>();
        if (var != null) {
            l.add(var);
        }
        if (statement != null) {
            l.add(statement);
        }
        return l;
    }       
}

