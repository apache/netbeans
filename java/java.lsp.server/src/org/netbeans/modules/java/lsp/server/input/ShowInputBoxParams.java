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
public class ShowInputBoxParams {

    /**
     * The text to display underneath the input box.
     */
    @NonNull
    private String prompt;

    /**
     * The value to prefill in the input box.
     */
    @NonNull
    private String value;

    /**
     * An optional title of the input box.
     */
    private String title;
    
    /**
     * Controls if a password input is shown. Password input hides the typed text.
     */
    private boolean password = false;

    public ShowInputBoxParams() {
        this("", "");
    }

    public ShowInputBoxParams(@NonNull final String prompt, @NonNull final String value) {
        this.prompt = Preconditions.checkNotNull(prompt, "prompt");
        this.value = Preconditions.checkNotNull(value, "value");
    }

    public ShowInputBoxParams(final String title, @NonNull final String prompt, @NonNull final String value, final boolean password) {
        this(prompt, value);
        this.title = title;
        this.password = password;
    }

    /**
     * An optional title of the input box.
     */
    @Pure
    public String getTitle() {
        return title;
    }

    /**
     * An optional title of the input box.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * The text to display underneath the input box.
     */
    @Pure
    @NonNull
    public String getPrompt() {
        return prompt;
    }

    /**
     * The text to display underneath the input box.
     */
    public void setPrompt(@NonNull final String prompt) {
        this.prompt = Preconditions.checkNotNull(prompt, "prompt");
    }

    /**
     * The value to prefill in the input box.
     */
    @Pure
    @NonNull
    public String getValue() {
        return value;
    }

    /**
     * The value to prefill in the input box.
     */
    public void setValue(@NonNull final String value) {
        this.value = Preconditions.checkNotNull(value, "value");
    }

    /**
     * Controls if a password input is shown. Password input hides the typed text.
     */
    @Pure
    @NonNull
    public boolean isPassword() {
        return password;
    }

    /**
     * Controls if a password input is shown. Password input hides the typed text.
     */
    public void setPassword(boolean password) {
        this.password = password;
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("title", title);
        b.add("prompt", prompt);
        b.add("value", value);
        b.add("password" , password);
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.title);
        hash = 59 * hash + Objects.hashCode(this.prompt);
        hash = 59 * hash + Objects.hashCode(this.value);
        hash = 59 * hash + (this.password ? 1 : 0);
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
        final ShowInputBoxParams other = (ShowInputBoxParams) obj;
        if (this.password != other.password) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.prompt, other.prompt)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }
}
