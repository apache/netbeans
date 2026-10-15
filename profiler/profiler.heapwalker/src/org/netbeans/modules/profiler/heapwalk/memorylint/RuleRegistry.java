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

package org.netbeans.modules.profiler.heapwalk.memorylint;

import org.openide.util.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author nenik
 */
final class RuleRegistry {

    private RuleRegistry() {}

    public static Collection<Rule> getRegisteredRules() {
        List<Rule> al = instantiateRules();
        al.sort((Rule o1, Rule o2) -> o1.getDisplayName().compareTo(o2.getDisplayName()));
        return al;
    }

    private static List<Rule> instantiateRules() {
        return new ArrayList<>(Lookup.getDefault().lookupAll(Rule.class));
    }
}
