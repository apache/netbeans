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

import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 *
 * @author Dusan Balek
 */
@SuppressWarnings("all")
public class ShowMutliStepInputParams {

    /**
     * ID of the input.
     */
    @NonNull
    private String id;

    /**
     * An optional title.
     */
    private String title;

    public ShowMutliStepInputParams() {
        this("", null);
    }

    public ShowMutliStepInputParams(@NonNull final String id, final String title) {
        this.id = Preconditions.checkNotNull(id, "id");
        this.title = title;
    }

    /**
     * ID of the input.
     */
    @Pure
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * ID of the input.
     */
    public void setId(@NonNull final String id) {
        this.id = Preconditions.checkNotNull(id, "id");
    }

    /**
     * An optional title.
     */
    @Pure
    public String getTitle() {
        return title;
    }

    /**
     * An optional title.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("id", id);
        b.add("title", title);
        return b.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.id);
        hash = 43 * hash + Objects.hashCode(this.title);
        return hash;
    }

    @Override
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
        final ShowMutliStepInputParams other = (ShowMutliStepInputParams) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        return true;
    }

}
