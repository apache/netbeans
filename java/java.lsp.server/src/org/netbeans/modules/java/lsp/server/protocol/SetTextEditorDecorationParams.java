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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * VSCode's DecorationRenderOptions.
 *
 * @author martin
 */
public final class SetTextEditorDecorationParams {

    /**
     * The text editor decoration key.
     */
    @NonNull
    private String key;

    /**
     * The text editor uri.
     */
    @NonNull
    private String uri;

    /**
     * The decoration ranges.
     */
    @NonNull
    private Range[] ranges;

    public SetTextEditorDecorationParams() {
        this("", "", new Range[]{});
    }

    public SetTextEditorDecorationParams(@NonNull String key, @NonNull String uri, @NonNull Range[] ranges) {
        this.key = key;
        this.uri = uri;
        this.ranges = ranges;
    }

    @Pure
    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    @Pure
    @NonNull
    public String getUri() {
        return uri;
    }

    public void setUri(@NonNull String uri) {
        this.uri = uri;
    }

    @Pure
    @NonNull
    public Range[] getRanges() {
        return ranges;
    }

    public void setRanges(@NonNull Range[] ranges) {
        this.ranges = ranges;
    }

    @Pure
    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("key", key);
        b.add("uri", uri);
        b.add("ranges", Arrays.toString(ranges));
        return b.toString();
    }

    @Pure
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.key);
        hash = 23 * hash + Objects.hashCode(this.uri);
        hash = 23 * hash + Arrays.deepHashCode(this.ranges);
        return hash;
    }

    @Pure
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
        final SetTextEditorDecorationParams other = (SetTextEditorDecorationParams) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.uri, other.uri)) {
            return false;
        }
        if (!Arrays.deepEquals(this.ranges, other.ranges)) {
            return false;
        }
        return true;
    }

}
