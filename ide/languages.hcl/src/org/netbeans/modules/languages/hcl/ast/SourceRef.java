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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Laszlo Kishalmi
 */
public class SourceRef {
    public final Snapshot source;
    private Map<HCLElement, OffsetRange> elementOffsets = new HashMap<>();
    private final Comparator<HCLElement> sourceOrder = Comparator.comparing((HCLElement e) -> elementOffsets.get(e));

    public SourceRef(Snapshot source) {
        this.source = source;
    }

    void add(HCLElement e, OffsetRange r) {
        elementOffsets.put(e, r);
    }

    void add(HCLElement e, int startOffset, int endOffset) {
        add(e, new OffsetRange(startOffset, endOffset));
    }

    public FileObject getFileObject() {
        return source.getSource().getFileObject();
    }
    public Optional<OffsetRange> getOffsetRange(HCLElement e) {
        return Optional.ofNullable(elementOffsets.get(e));
    }
    
    <E extends HCLElement> List<E> sortBySource(List<? extends E> elements) {
        List<E> ret = new ArrayList<>(elements);
        Collections.sort(ret, sourceOrder);
        return ret;
    }
}
