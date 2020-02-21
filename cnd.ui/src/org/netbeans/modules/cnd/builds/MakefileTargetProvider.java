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

package org.netbeans.modules.cnd.builds;

import java.io.IOException;
import java.util.Set;
import org.openide.nodes.Node;

/**
 * Retrieves list of targets from a makefile.
 *
 */
public interface MakefileTargetProvider extends Node.Cookie {

    /**
     * Preferred targets are runnable, but not all runnable targets are preferred.
     * Usually there are too many runnable targets to show them all.
     *
     * @return list of preferred targets, never <code>null</code>
     * @throws IOException if file operation fails
     */
    Set<String> getPreferredTargets() throws IOException;

    /**
     * @return list of all runnable targets, never <code>null</code>
     * @throws IOException if file operation fails
     */
    Set<String> getRunnableTargets() throws IOException;
}
