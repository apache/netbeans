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
package org.netbeans.modules.java.lsp.server.input;

import java.util.List;
import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.util.Preconditions;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 *
 * @author Dusan Balek
 */
public final class QuickPickStep extends ShowQuickPickParams {

    /**
     * An optional total step count.
     */
    private int	totalSteps;

    /**
     * Current step id. Used to retrieve step result.
     */
    @NonNull
    private String stepId;

    public QuickPickStep() {
        super();
        this.totalSteps = 0;
        this.stepId = "";
    }

    public QuickPickStep(final int totalSteps, @NonNull final String stepId, @NonNull final String placeHolder, @NonNull final List<QuickPickItem> items) {
        super(placeHolder, items);
        this.totalSteps = totalSteps;
        this.stepId = Preconditions.checkNotNull(stepId, "stepId");
    }

    public QuickPickStep(final int totalSteps, @NonNull final String stepId, final String title, @NonNull final String placeHolder, final boolean canPickMany, @NonNull final List<QuickPickItem> items) {
        super(title, placeHolder, canPickMany, items);
        this.totalSteps = totalSteps;
        this.stepId = Preconditions.checkNotNull(stepId, "stepId");
    }

    /**
     * An optional total step count.
     */
    @Pure
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * An optional total step count.
     */
    public void setTotalSteps(final int totalSteps) {
        this.totalSteps = totalSteps;
    }

    /**
     * Current step id. Used to retrieve step result.
     */
    @Pure
    @NonNull
    public String getStepId() {
        return stepId;
    }

    /**
     * Current step id. Used to retrieve step result.
     */
    public void setStepId(@NonNull final String stepId) {
        this.stepId = Preconditions.checkNotNull(stepId, "stepId");
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("totalSteps", totalSteps);
        b.add("stepId", stepId);
        b.add("title", getTitle());
        b.add("placeHolder", getPlaceHolder());
        b.add("canPickMany", getCanPickMany());
        b.add("items", getItems());
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + this.totalSteps;
        hash = 71 * hash + Objects.hashCode(this.stepId);
        hash = 71 * hash + Objects.hashCode(this.getTitle());
        hash = 71 * hash + Objects.hashCode(this.getPlaceHolder());
        hash = 71 * hash + (this.getCanPickMany() ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.getItems());
        return hash;
    }

    @Override
    @Pure
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
        final QuickPickStep other = (QuickPickStep) obj;
        if (this.totalSteps != other.totalSteps) {
            return false;
        }
        if (this.getCanPickMany() != other.getCanPickMany()) {
            return false;
        }
        if (!Objects.equals(this.stepId, other.stepId)) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
            return false;
        }
        if (!Objects.equals(this.getPlaceHolder(), other.getPlaceHolder())) {
            return false;
        }
        return Objects.equals(this.getItems(), other.getItems());
    }
}
