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
package org.netbeans.modules.refactoring.spi;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;

/** Factory for an object handling refactoring in a guarded block. This should be
 * implemented by modules providing guarded sections in Java documents. If
 * a change proposed by a refactoring affects a guarded section, the refactoring object
 * asks the registered GuardedBlockHandlers to handle that change.
 *
 * @author Martin Matula
 */
public interface GuardedBlockHandlerFactory {
    /** Creates and returns a new instance of the guarded block refactoring handler or
     * null if the handler is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the handler should be plugged in.
     * @return Instance of GuardedBlockHandler or null if the handler is not applicable to
     * the passed refactoring.
     */
    GuardedBlockHandler createInstance(AbstractRefactoring refactoring);
}
