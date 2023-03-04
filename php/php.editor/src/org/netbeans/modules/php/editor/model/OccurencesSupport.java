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

package org.netbeans.modules.php.editor.model;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;

/**
 *
 * @author Radek Matous
 */
public final class OccurencesSupport {
    private ModelVisitor modelVisitor;
    private Occurence occurence;
    private CodeMarker codeMarker;
    int offset;

    OccurencesSupport(ModelVisitor modelVisitor, int offset) {
        this.modelVisitor = modelVisitor;
        this.offset = offset;
    }

    @CheckForNull
    public synchronized Occurence getOccurence() {
        if (occurence == null) {
            occurence = modelVisitor.getOccurence(offset);
        }
        return occurence;
    }

    @CheckForNull
    public synchronized CodeMarker getCodeMarker() {
        if (codeMarker == null) {
            codeMarker = modelVisitor.getCodeMarker(offset);
        }
        return codeMarker;
    }

}
