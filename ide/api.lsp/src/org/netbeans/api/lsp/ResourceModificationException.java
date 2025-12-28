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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Records a failure from applying {@link WorkspaceEdit}s to resources. Callers may
 * inspect exception details to recover from the failure, revert the applied operations etc.
 * 
 * @author sdedic
 * @since 1.27
 */
public final class ResourceModificationException extends Exception {
    private final List<WorkspaceEdit> appliedEdits;
    private final WorkspaceEdit failedEdit;
    private final int failedOperationIndex;
    private final List<String> savedResources;
    private final int failedEditIndex;
    
    /**
     * Index that indicates a failure before the first edit in {@link #getFailedEdit()}.
     */
    public static final int BEFORE_FIRST_EDIT = -1;
    
    /**
     * Index that indicates that the exact failing edit is unspecified.
     */
    public static final int UNSPECIFIED_EDIT = -2;

    /**
     * Index that indicates that the exact failing WorkspaceEdit operation is not known.
     */
    public static final int UNSPECIFIED_OPERATIION = -2;

    /**
     * Initializes the exception for a failure from applying the workspace edits.
     * @param message error message.
     * @param appliedEdits Edits that have been fully applied to the resource, prior to the failure.
     * @param failedOperationIndex index of operation within failedEdit that caused the failure
     * @param savedResources resources that have been already saved.
     * @param failedEdit the WorkspaeEdit that failed to apply.
     * @param cause more specific exception that causes the failure, if any.
     */
    public ResourceModificationException(List<WorkspaceEdit> appliedEdits, WorkspaceEdit failedEdit, 
            int failedOperationIndex, int failedEditIndex, Collection<String> savedResources, String message, Throwable cause) {
        super(message, cause);
        Parameters.notNull("appliedEdits", appliedEdits);
        this.appliedEdits = appliedEdits;
        this.failedEdit = failedEdit;
        this.savedResources = savedResources == null ? Collections.emptyList() : new ArrayList<>(savedResources);
        this.failedOperationIndex = failedOperationIndex;
        this.failedEditIndex = failedEditIndex;
    }

    /**
     * Initializes the exception for a failure from saving the resources. All edits of the operation have been applied.
     * @param appliedEdits list of edits applied during the operation.
     * @param savedResources resources that were successfully saved
     * @param message failure message
     */
    public ResourceModificationException(List<WorkspaceEdit> appliedEdits, List<String> savedResources, String message) {
        super(message);
        Parameters.notNull("appliedEdits", appliedEdits);
        Parameters.notNull("savedResources", savedResources);
        this.appliedEdits = appliedEdits;
        this.failedEdit = null;
        this.failedOperationIndex = UNSPECIFIED_OPERATIION;
        this.failedEditIndex = UNSPECIFIED_EDIT;
        this.savedResources = savedResources;
    }
    
    /**
     * The index of the failed edit, from the failed operation (see {@link #getFailedOperationIndex()}).
     * Will be set to {@link #UNSPECIFIED_EDIT}, if the exact failed edit is not known, or if the failure
     * is the resource operation that does not use edits.
     * @return index of the failed edit operation.
     */
    public int getFailedEditIndex() {
        return failedEditIndex;
    }
    
    /**
     * Returns true, if the edit that caused the failure is specified.
     * @return true, if the failing edit is known, false otherwise.
     */
    public boolean isUnspecifiedEdit() {
        return failedOperationIndex >= 0 && failedEditIndex != UNSPECIFIED_EDIT;
    }
    

    /**
     * Edits that have been fully applied.
     * @return applied edits
     */
    public @NonNull List<WorkspaceEdit> getAppliedEdits() {
        return appliedEdits;
    }

    /**
     * Workspace edit that failed to apply. Possibly {@code null} (and then
     * {@link #getFailedOperationIndex()} returns {@link #UNSPECIFIED_OPERATIION}). If the request fails
     * during save, there's no failed edit.
     * @return failed edit.
     */
    public @CheckForNull WorkspaceEdit getFailedEdit() {
        return failedEdit;
    }

    /**
     * Index of the failed edit operation. If no failed operation is reported, 
     * the index is set to {@link #UNSPECIFIED_OPERATIION}.
     * @return index of the failed operation, within {@link #getFailedEdit()}.
     */
    public int getFailedOperationIndex() {
        return failedOperationIndex;
    }

    /**
     * List of modified resources saved to the disk prior to the failure. The method
     * returns uris
     * @return resource uris.
     */
    public @NonNull List<String> getSavedResources() {
        return savedResources;
    }
}
