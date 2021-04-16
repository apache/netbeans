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
package org.netbeans.modules.java.lsp.server.protocol;

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

    public ShowInputBoxParams() {
        this("", "");
    }

    public ShowInputBoxParams(@NonNull final String prompt, @NonNull final String value) {
        this.prompt = Preconditions.checkNotNull(prompt, "prompt");
        this.value = Preconditions.checkNotNull(value, "value");
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

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("prompt", prompt);
        b.add("value", value);
        return b.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.prompt);
        hash = 59 * hash + Objects.hashCode(this.value);
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
        final ShowInputBoxParams other = (ShowInputBoxParams) obj;
        if (!Objects.equals(this.prompt, other.prompt)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
}
