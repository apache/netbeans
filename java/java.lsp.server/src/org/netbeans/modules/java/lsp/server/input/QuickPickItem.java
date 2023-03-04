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
package org.netbeans.modules.java.lsp.server.input;

import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Represents an item that can be selected from a list of items.
 *
 * @author Dusan Balek
 */
@SuppressWarnings("all")
public class QuickPickItem {

    /**
     * A human-readable string which is rendered prominent.
     */
    @NonNull
    private String label;

    /**
     * A human-readable string which is rendered less prominent in the same line.
     */
    private String description;

    /**
     * A human-readable string which is rendered less prominent in a separate line.
     */
    private String detail;

    /**
     * Optional flag indicating if this item is picked initially.
     */
    private boolean picked;

    /**
     * Optional user data.
     */
    private Object userData;

    public QuickPickItem() {
    }

    public QuickPickItem(@NonNull final String label) {
        this.label = Preconditions.checkNotNull(label, "label");
    }

    public QuickPickItem(@NonNull final String label, final String description, final String detail, final boolean picked, final Object userData) {
        this(label);
        this.description = description;
        this.detail = detail;
        this.picked = picked;
        this.userData = userData;
    }

    /**
     * A human-readable string which is rendered prominent.
     */
    @Pure
    @NonNull
    public String getLabel() {
        return label;
    }

    /**
     * A human-readable string which is rendered prominent.
     */
    public void setLabel(@NonNull final String label) {
        this.label = Preconditions.checkNotNull(label, "label");
    }

    /**
     * A human-readable string which is rendered less prominent in the same line.
     */
    @Pure
    public String getDescription() {
        return description;
    }

    /**
     * A human-readable string which is rendered less prominent in the same line.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * A human-readable string which is rendered less prominent in a separate line.
     */
    @Pure
    public String getDetail() {
        return detail;
    }

    /**
     * A human-readable string which is rendered less prominent in a separate line.
     */
    public void setDetail(final String detail) {
        this.detail = detail;
    }

    /**
     * Optional flag indicating if this item is picked initially.
     */
    @Pure
    public boolean isPicked() {
        return picked;
    }

    /**
     * Optional flag indicating if this item is picked initially.
     */
    public void setPicked(boolean picked) {
        this.picked = picked;
    }

    /**
     * Optional user data.
     */
    @Pure
    public Object getUserData() {
        return userData;
    }

    /**
     * Optional user data.
     */
    public void setUserData(final Object userData) {
        this.userData = userData;
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("label", label);
        b.add("description", description);
        b.add("detail", detail);
        b.add("picked", picked);
        b.add("userData", userData);
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.label);
        hash = 83 * hash + Objects.hashCode(this.description);
        hash = 83 * hash + Objects.hashCode(this.detail);
        hash = 83 * hash + Objects.hashCode(this.userData);
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
        final QuickPickItem other = (QuickPickItem) obj;
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.detail, other.detail)) {
            return false;
        }
        if (!Objects.equals(this.userData, other.userData)) {
            return false;
        }
        return true;
    }
}
