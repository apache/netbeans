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
package org.netbeans.modules.php.editor.api;

import java.util.List;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;

public enum QualifiedNameKind {
    UNQUALIFIED, QUALIFIED, FULLYQUALIFIED;
    public boolean isUnqualified() {
        return UNQUALIFIED.equals(this);
    }
    public boolean isQualified() {
        return QUALIFIED.equals(this);
    }
    public boolean isFullyQualified() {
        return FULLYQUALIFIED.equals(this);
    }
    public static QualifiedNameKind resolveKind(NamespaceName namespaceName) {
        if (namespaceName.isGlobal()) {
            return FULLYQUALIFIED;
        } else if (namespaceName.getSegments().size() > 1) {
            return QUALIFIED;
        }
        return UNQUALIFIED;
    }
    public static QualifiedNameKind resolveKind(List<String> segments) {
        if (segments.size() > 1) {
            return QUALIFIED;
        }
        return UNQUALIFIED;
    }
    public static QualifiedNameKind resolveKind(Identifier identifier) {
        return UNQUALIFIED;
    }
    public static QualifiedNameKind resolveKind(String name) {
        if (name.startsWith(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR)) {
            return FULLYQUALIFIED;
        } else if (name.indexOf(NamespaceDeclarationInfo.NAMESPACE_SEPARATOR) != -1) {
            return QUALIFIED;
        }
        return UNQUALIFIED;
    }

}
