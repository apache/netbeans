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
package org.netbeans.modules.php.editor.model.nodes;

import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import static org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind.METHOD;
import static org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind.STATIC_METHOD;

public final class ASTNodeInfoUtils {

    private ASTNodeInfoUtils() {
    }

    /**
     * Check whether the kind is METHOD or STATIC_METHOD.
     *
     * @param kind the kind
     * @return {@code true} if this is METHOD or STATIC_METHOD, {@code false}
     * otherwise
     */
    public static boolean isMethod(Kind kind) {
        return kind == METHOD || kind == STATIC_METHOD;
    }
}
