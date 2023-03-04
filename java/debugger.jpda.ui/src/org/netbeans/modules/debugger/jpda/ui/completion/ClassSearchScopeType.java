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
package org.netbeans.modules.debugger.jpda.ui.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.java.source.ClassIndex.SearchScopeType;

/**
 *
 * @author Martin Entlicher
 */
class ClassSearchScopeType implements SearchScopeType {
    
    private String prefix;

    public ClassSearchScopeType() {
        prefix = null;
    }

    public ClassSearchScopeType(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Set<? extends String> getPackages() {
        if (prefix == null) {
            return null;
        }
        int dot = prefix.lastIndexOf('.');
        if (dot > 0) {
            return Collections.singleton(prefix.substring(0, dot));
        } else {
            return new HashSet(Arrays.asList(new String[] { "java.lang", "" }));
        }
    }

    @Override
    public boolean isSources() {
        return true;
    }

    @Override
    public boolean isDependencies() {
        return true;
    }

}
