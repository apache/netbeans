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

package org.netbeans.modules.php.project.connections.transfer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Information about remote file transfer.
 * <p>
 * This class is not thread-safe.
 */
public final class TransferInfo {

    private final Set<TransferFile> transfered = new HashSet<>();
    // file, reason
    private final Map<TransferFile, String> failed = new HashMap<>();
    // file, reason
    private final Map<TransferFile, String> partiallyFailed = new HashMap<>();
    // file, reason
    private final Map<TransferFile, String> ignored = new HashMap<>();

    private long runtime;


    public Set<TransferFile> getTransfered() {
        return transfered;
    }

    public Map<TransferFile, String> getFailed() {
        return failed;
    }

    public Map<TransferFile, String> getPartiallyFailed() {
        return partiallyFailed;
    }

    public Map<TransferFile, String> getIgnored() {
        return ignored;
    }

    public long getRuntime() {
        return runtime;
    }

    public boolean isTransfered(TransferFile transferFile) {
        return transfered.contains(transferFile);
    }

    public boolean isFailed(TransferFile transferFile) {
        return failed.containsKey(transferFile);
    }

    public boolean isPartiallyFailed(TransferFile transferFile) {
        return partiallyFailed.containsKey(transferFile);
    }

    public boolean isIgnored(TransferFile transferFile) {
        return ignored.containsKey(transferFile);
    }

    public boolean hasAnyTransfered() {
        return !transfered.isEmpty();
    }

    public boolean hasAnyFailed() {
        return !failed.isEmpty();
    }

    public boolean hasAnyPartiallyFailed() {
        return !partiallyFailed.isEmpty();
    }

    public boolean hasAnyIgnored() {
        return !ignored.isEmpty();
    }

    public void addTransfered(TransferFile transferFile) {
        assertNotContains(failed.keySet(), transferFile, "failed", "transfered"); // NOI18N
        assertNotContains(ignored.keySet(), transferFile, "ignored", "transfered"); // NOI18N
        transfered.add(transferFile);
    }

    public void addFailed(TransferFile transferFile, String reason) {
        assertNotContains(transfered, transferFile, "transfered", "failed"); // NOI18N
        assertNotContains(ignored.keySet(), transferFile, "ignored", "failed"); // NOI18N
        assertNotContains(partiallyFailed.keySet(), transferFile, "partially failed", "failed"); // NOI18N
        failed.put(transferFile, reason);
    }

    public void addPartiallyFailed(TransferFile transferFile, String reason) {
        // can be in transfered
        assertNotContains(failed.keySet(), transferFile, "failed", "partially failed"); // NOI18N
        assertNotContains(ignored.keySet(), transferFile, "ignored", "partially failed"); // NOI18N
        partiallyFailed.put(transferFile, reason);
    }

    public void addIgnored(TransferFile transferFile, String reason) {
        assertNotContains(transfered, transferFile, "transfered", "ignored"); // NOI18N
        assertNotContains(failed.keySet(), transferFile, "failed", "ignored"); // NOI18N
        assertNotContains(partiallyFailed.keySet(), transferFile, "partially failed", "ignored"); // NOI18N
        ignored.put(transferFile, reason);
    }

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [transfered: "); // NOI18N
        sb.append(transfered);
        sb.append(", failed: "); // NOI18N
        sb.append(failed);
        sb.append(", partially failed: "); // NOI18N
        sb.append(partiallyFailed);
        sb.append(", ignored: "); // NOI18N
        sb.append(ignored);
        sb.append(", runtime: "); // NOI18N
        sb.append(runtime);
        sb.append(" ms]"); // NOI18N
        return sb.toString();
    }

    private void assertNotContains(Collection<TransferFile> collection, TransferFile transferFile, String collectionType, String fileType) {
        assert !collection.contains(transferFile) : collectionType + " files should not contain " + fileType + " file"; // NOI18N
    }

}
