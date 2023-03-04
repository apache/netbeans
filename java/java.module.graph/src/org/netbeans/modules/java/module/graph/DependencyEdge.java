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
package org.netbeans.modules.java.module.graph;

import java.util.Objects;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class DependencyEdge {
    private final ModuleNode source;
    private final ModuleNode target;
    private final boolean pub;
    private final boolean trans;

    DependencyEdge(
            @NonNull final ModuleNode source,
            @NonNull final ModuleNode target,
            final boolean pubReq,
            final boolean trans) {
        Parameters.notNull("source", source);   //NOI18N
        Parameters.notNull("target", target);   //NOI18N
        this.source = source;
        this.target = target;
        this.pub = pubReq;
        this.trans = trans;
    }

    @NonNull
    ModuleNode getSource() {
        return source;
    }

    @NonNull
    ModuleNode getTarget() {
        return target;
    }

    boolean isPublic() {
        return pub;
    }

    boolean isTrasitive() {
        return trans;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DependencyEdge other = (DependencyEdge) obj;
        if (this.pub != other.pub) {
            return false;
        }
        if (this.trans != other.trans) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return true;
    }


}
