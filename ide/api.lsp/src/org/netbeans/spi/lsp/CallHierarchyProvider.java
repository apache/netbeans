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
package org.netbeans.spi.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lsp.CallHierarchyEntry;

/**
 * Provides call hierarchy for the given location in a document. May provide list of
 * calls originating from an element (i.e. method, field initializer), or callers to
 * the element at the passed document's offset.
 * <p>
 * First {@link #findCallOrigin} should be called, producing a {@link CallHierarchyEntry} 
 * that describes the element at the given position. In subsequent calls, inbound or
 * outbound calls may be inspected for the passed Entry using {@link #findIncomingCalls(CallHierarchyEntry)}
 * or {@link #findOutgoingCalls(CallHierarchyEntry)}. 
 * <p>
 * Note that the returned CompletableFutures may become completed cancelled if the implementation (for example)
 * displays user-facing UI like progress, which allows the user to interrupt the operation. If the reported
 * exception is a {@link java.util.concurrent.CompletionException}, its cause should be also checked.
 * 
 * @since 1.9
 * @author sdedic
 */
public interface CallHierarchyProvider {
    /**
     * Returns a {@link CallHierarchyEntry} that corresponds to the given document's location. 
     * If the location does not represent a valid starting point, the returned Future will complete
     * with {@code null} result. Any errors, i.e. parsing or I/O will be reported through the
     * returned Future.
     * <p>
     * 
     * @param doc the document
     * @param offset location in the document
     * @return location entry, or {@code null}
     */
    @CheckForNull
    public CompletableFuture<List<CallHierarchyEntry>> findCallOrigin(@NonNull Document doc, int offset);

    /**
     * Returns a list of locations that call into the passed call target. The method may return {@code null}, if the
     * call target location is not supported or is invalid. The {@link CallHierarchyEntry.Call#getRanges() Call.getRanges()} 
     * describes ranges int he caller {@link org.netbeans.api.lsp.StructureElement} where the target is invoked.
     * @param callTarget the call target location
     * @return list of callers, or {@code null}
     */
    @CheckForNull
    public CompletableFuture<List<CallHierarchyEntry.Call>> findIncomingCalls(@NonNull CallHierarchyEntry callTarget);

    /**
     * Returns a list of locations that are called from the passed source. The method may return {@code null}, if the
     * call target location is not supported or is invalid. The {@link CallHierarchyEntry.Call#getRanges() Call.getRanges()} 
     * describe ranges in the call source, where the specific {@link org.netbeans.api.lsp.StructureElement} target is called from.
     * @param callSource the location to inspect
     * @return list of callees, or {@code null}
     */
    @CheckForNull
    public CompletableFuture<List<CallHierarchyEntry.Call>> findOutgoingCalls(@NonNull CallHierarchyEntry callSource);
}
