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
package org.netbeans.modules.debugger.jpda.truffle.node;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.project.Project;

import org.netbeans.modules.javascript.nodejs.spi.DebuggerStartModifier;
import org.netbeans.modules.javascript.nodejs.spi.DebuggerStartModifierFactory;

public class DebuggerStartModifierFactoryImpl implements DebuggerStartModifierFactory {

    @Override
    public DebuggerStartModifier create(Project project, List<String> localPaths, List<String> serverPaths, List<String> localPathsExclusionFilter, AtomicReference<Future<Integer>> taskRef) {
        return new DebuggerStartModifierImpl();
    }

}
