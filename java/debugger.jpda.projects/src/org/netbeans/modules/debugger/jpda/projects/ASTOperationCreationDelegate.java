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

package org.netbeans.modules.debugger.jpda.projects;

import org.netbeans.spi.debugger.jpda.EditorContext;

/**
 *
 * @author Martin Entlicher
 */
public interface ASTOperationCreationDelegate {
    
    EditorContext.Operation createMethodOperation(
            EditorContext.Position startPosition,
            EditorContext.Position endPosition,
            EditorContext.Position methodStartPosition,
            EditorContext.Position methodEndPosition,
            String methodName, String methodClassType,
            int bytecodeIndex, boolean isNative);

    EditorContext.Position createPosition(int offset, int line, int column);

    void addNextOperationTo(EditorContext.Operation operation,
                            EditorContext.Operation next);

}
