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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 *
 * @author Dusan Balek
 */
public class InputCallbackParams {

    /**
     * ID of the input.
     */
    @NonNull
    private String inputId;

    /**
     * Step number.
     */
    private int step;

    /**
     * Collected input values and selected items.
     */
    @NonNull
    private Map<String, Either<List<QuickPickItem>, String>> data;

    public InputCallbackParams() {
        this("", 0, new HashMap<>());
    }

    public InputCallbackParams(@NonNull final String inputId, final int step, @NonNull final Map<String, Either<List<QuickPickItem>, String>> data) {
        this.inputId = Preconditions.checkNotNull(inputId, "inputId");
        this.step = step;
        this.data = Preconditions.checkNotNull(data, "data");
    }

    /**
     * ID of the input.
     */
    @Pure
    @NonNull
    public String getInputId() {
        return inputId;
    }

    /**
     * ID of the input.
     */
    public void setInputId(@NonNull final String inputId) {
        this.inputId = Preconditions.checkNotNull(inputId, "inputId");
    }

    /**
     * Step number.
     */
    @Pure
    public int getStep() {
        return step;
    }

    /**
     * Step number.
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Collected input values and selected items.
     */
    @Pure
    @NonNull
    public Map<String, Either<List<QuickPickItem>, String>> getData() {
        return data;
    }

    /**
     * Collected input values and selected items.
     */
    public void setData(@NonNull final Map<String, Either<List<QuickPickItem>, String>> data) {
        this.data = Preconditions.checkNotNull(data, "data");
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("inputId", inputId);
        b.add("step", step);
        b.add("data", data);
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.inputId);
        hash = 79 * hash + this.step;
        hash = 79 * hash + Objects.hashCode(this.data);
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
        final InputCallbackParams other = (InputCallbackParams) obj;
        if (this.step != other.step) {
            return false;
        }
        if (!Objects.equals(this.inputId, other.inputId)) {
            return false;
        }
        return Objects.equals(this.data, other.data);
    }
}
