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
package org.netbeans.modules.javascript2.model.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Petr Pisl, Petr Hejl
 */
public final class Occurrence {

    private final OffsetRange offsetRange;

    private final List<JsObject> declarations;

    public Occurrence(OffsetRange offsetRange, JsObject... declarations) {
        this.offsetRange = offsetRange;
        this.declarations = new ArrayList<>(declarations.length);
        Collections.addAll(this.declarations, declarations);
    }

    public OffsetRange getOffsetRange() {
        return offsetRange;
    }

    public Collection<? extends JsObject> getDeclarations() {
        return Collections.unmodifiableCollection(declarations);
    }

    @Override
    public int hashCode() {
        return offsetRange.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Occurrence other = (Occurrence) obj;
        if (this.offsetRange != other.offsetRange
                && (this.offsetRange == null || !this.offsetRange.equals(other.offsetRange))) {
            return false;
        }
        return true;
    }

}
