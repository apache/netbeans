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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * A selection list parameters.
 *
 * @author Dusan Balek
 */
@SuppressWarnings("all")
public class ShowQuickPickParams {

    /**
     * An optional title of the quick pick.
     */
    private String title;

    /**
     * A string to show as placeholder in the input box to guide the user what to pick on.
     */
    @NonNull
    private String placeHolder;

    /**
     * An optional flag to make the picker accept multiple selections.
     */
    private boolean canPickMany;

    /**
     * A list of items.
     */
    @NonNull
    private List<QuickPickItem> items;

    public ShowQuickPickParams() {
        this("", new ArrayList<>());
    }

    public ShowQuickPickParams(@NonNull final String placeHolder, @NonNull final List<QuickPickItem> items) {
        this.placeHolder = Preconditions.checkNotNull(placeHolder, "placeHolder");
        this.items = Preconditions.checkNotNull(items, "items");
    }

    public ShowQuickPickParams(final String title, @NonNull final String placeHolder, final boolean canPickMany, @NonNull final List<QuickPickItem> items) {
        this(placeHolder, items);
        this.title = title;
        this.canPickMany = canPickMany;
    }

    /**
     * An optional title of the quick pick.
     */
    @Pure
    public String getTitle() {
        return title;
    }

    /**
     * An optional title of the quick pick.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * A string to show as placeholder in the input box to guide the user what to pick on.
     */
    @Pure
    @NonNull
    public String getPlaceHolder() {
        return placeHolder;
    }

    /**
     * A string to show as placeholder in the input box to guide the user what to pick on.
     */
    public void setPlaceHolder(@NonNull final String placeHolder) {
        this.placeHolder = Preconditions.checkNotNull(placeHolder, "placeHolder");
    }

    /**
     * An optional flag to make the picker accept multiple selections.
     */
    @Pure
    public boolean getCanPickMany() {
        return canPickMany;
    }

    /**
     * An optional flag to make the picker accept multiple selections.
     */
    public void setCanPickMany(final boolean canPickMany) {
        this.canPickMany = canPickMany;
    }

    /**
     * A list of items.
     */
    @Pure
    @NonNull
    public List<QuickPickItem> getItems() {
        return items;
    }

    /**
     * A list of items.
     */
    public void setItems(@NonNull final List<QuickPickItem> items) {
        this.items = Preconditions.checkNotNull(items, "items");
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("title", title);
        b.add("placeHolder", placeHolder);
        b.add("canPickMany", canPickMany);
        b.add("items", items);
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.title);
        hash = 29 * hash + Objects.hashCode(this.placeHolder);
        hash = 29 * hash + (this.canPickMany ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.items);
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
        final ShowQuickPickParams other = (ShowQuickPickParams) obj;
        if (this.canPickMany != other.canPickMany) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.placeHolder, other.placeHolder)) {
            return false;
        }
        return Objects.equals(this.items, other.items);
    }
}
