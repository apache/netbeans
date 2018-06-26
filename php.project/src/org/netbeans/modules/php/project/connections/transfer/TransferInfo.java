/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
