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
package org.netbeans.modules.cnd.toolchain.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.toolchain.compilers.CompilerDefinitionAccessor;

public final class CompilerDefinition extends ArrayList<String> {
    
    static {
        CompilerDefinitionAccessor.register(new CompilerDefinitionAccessorImpl());
    }

    private List<Integer> userAddedDefinitions = new ArrayList<Integer>(0);

    public CompilerDefinition() {
        super();
    }

    public CompilerDefinition(int size) {
        super(size);
    }

    public CompilerDefinition(Collection<String> c) {
        super(c);
    }

    public boolean isUserAdded(int i) {
        return userAddedDefinitions.contains(i);
    }

    public void setUserAdded(boolean isUserAddes, int i) {
        if (isUserAddes) {
            if (!userAddedDefinitions.contains(i)) {
                userAddedDefinitions.add(i);
            }
        } else if (userAddedDefinitions.contains(i)) {
            userAddedDefinitions.remove(Integer.valueOf(i));
        }
    }

    public void sort() {
        Set<String> set = new HashSet<String>();
        for (Integer i : userAddedDefinitions) {
            if (i < size()) {
                set.add(get(i));
            }
        }
        Collections.sort(this, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        userAddedDefinitions.clear();
        for (String s : set) {
            userAddedDefinitions.add(indexOf(s));
        }
    }
    
    private static class CompilerDefinitionAccessorImpl extends CompilerDefinitionAccessor {

        @Override
        public List<Integer> getUserAddedDefinitions(CompilerDefinition cdf) {
            return cdf.userAddedDefinitions;
        }
    
}
}
