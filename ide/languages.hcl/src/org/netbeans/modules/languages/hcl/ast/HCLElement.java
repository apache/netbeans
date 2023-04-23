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
package org.netbeans.modules.languages.hcl.ast;

import java.util.Comparator;
import java.util.Optional;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class HCLElement {

    public static final Comparator<HCLElement> SOURCE_ORDER = (HCLElement h1, HCLElement h2) -> {
        if (h1.sourceRef.isPresent() && h2.sourceRef.isPresent()) {
            return h1.sourceRef.get().startOffset - h2.sourceRef.get().startOffset;
        }
        return 0;
    };

    HCLElement parent;
    final Optional<SourceRef> sourceRef;

    public HCLElement() {
        this(null);
    }

    
    public HCLElement(SourceRef sourceRef) {
        this.sourceRef = Optional.ofNullable(sourceRef);
    }

    public Optional<SourceRef> getSourceRef() {
        return sourceRef;
    }

    public abstract String id();
}
