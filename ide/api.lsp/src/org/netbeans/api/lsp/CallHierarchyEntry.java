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
package org.netbeans.api.lsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.lsp.CallHierarchyProvider;

/**
 * Represents an entry in a call hierarchy chain. The entry identifies an element in a source which is the origin or target
 * of an invocation/call. 
 * 
 * @since 1.9
 * @author sdedic
 */
public final class CallHierarchyEntry {
    /**
     * The call origin or target element.
     */
    private StructureElement    element;
    
    /**
     * Opaque implementation-specific data.
     */
    private String  customData;

    /**
     * Returns description of the call hierarchy element. See {@link StructureElement} for structure details,
     * @return description of the structural languagein call hierarchy
     */
    @NonNull
    public StructureElement getElement() {
        return element;
    }
    
    /**
     * Returns an opaque, mime type specific data which shall be interpreted by the {@link CallHierarchyProvider}
     * in subsequent calls. For example, method signature can be put here.
     * @return provider-specific data
     */
    @CheckForNull
    public String getCustomData() {
        return customData;
    }

    /**
     * Constructs a new entry object.
     * @param element represents call target or call origin, depending on usage
     * @param customData implementation-specific data
     */
    public CallHierarchyEntry(StructureElement element, String customData) {
        this.element = element;
        this.customData = customData;
    }
    
    
    /**
     * This structure is used for two purposes. For <b>outgoing calls</b> the {@link #getItem}
     * returns the call target, and {@link #getRanges} returns locations in the origin
     * {@link CallHierarchyEntry} where the target is invoked from. For <b>incoming calls</b>
     * the {@link #getItem} identifies the element that makes the call, while {@link #getRanges}
     * locations where the call was made from from that element.
     * 
     */
    public static final class Call {
        private final CallHierarchyEntry item;
        private final List<Range> ranges;

        public Call(@NonNull CallHierarchyEntry item, List<Range> ranges) {
            this.item = item;
            this.ranges = ranges.size() > 1 ? 
                        Collections.unmodifiableList(new ArrayList<>(ranges))
                    : ranges.isEmpty() ? 
                        Collections.emptyList() : Collections.singletonList(ranges.get(0));
        }
        
        /**
         * @return Target or origin element of the call.
         */
        @NonNull
        public CallHierarchyEntry getItem() {
            return item;
        }

        /**
         * @return text locations within the origin element where the target is invoked.
         */
        public List<Range> getRanges() {
            return ranges;
        }
    }
}
