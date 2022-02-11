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
package org.netbeans.spi.lsp;

import java.util.ArrayList;
import java.util.HashSet;
import org.netbeans.api.lsp.StructureElement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.lsp.StructureElementAccessor;

/**
 * Interface for building structure of symbols at a given document.
 * Implementations of the interface should be registered in MimeLookup.
 * <pre>
 *
 *  {@codesnippet @MimeRegistration(mimeType = "text/foo", service = StructureProvider.class)
 *   public class FooStructureProvider implements StructureProvider {
 *     ...
 *   }
 *  }
 * </pre>
 *
 * @author Petr Pisl
 * @since 1.8
 */
public interface StructureProvider {

    /**
     * Resolves a structure tree of symbols at the given document.
     *
     * @param doc document on which to operate.
     * @return a list of top level structure elements
     *
     * @since 1.8
     */
    @NonNull
    CompletableFuture<List<StructureElement>> getStructure(@NonNull Document doc);

    /**
     * Create builder for {@link StructureElement} instances.
     *
     * @param name the name of the structure element
     * @param kind the kind of structure element
     * @return newly created builder
     *
     * @since 1.8
     */
    public static Builder newBuilder(@NonNull String name, @NonNull StructureElement.Kind kind) {
        return new Builder(name, kind);
    }

    /**
     * Builder for {@link StructureElement} instances.
     *
     * @since 1.8
     */
    public static final class Builder {

        private String name;
        private String detail;
        private int selectionStartOffset;
        private int selectionEndOffset;
        private int expandedStartOffset;
        private int expandedEndOffset;
        private StructureElement.Kind kind;
        private Set<StructureElement.Tag> tags;
        private List<StructureElement> children;

        private Builder(@NonNull String name, @NonNull StructureElement.Kind kind) {
            this.name = name;
            this.kind = kind;
        }

        /**
         * The name of {@link StructureElement} is displayed in the structure
         * tree and other ui.
         *
         * @since 1.8
         */
        @NonNull
        public Builder name(@NonNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Kind of this structure element.
         *
         * @since 1.8
         */
        @NonNull
        public Builder kind(@NonNull StructureElement.Kind name) {
            this.kind = kind;
            return this;
        }

        /**
         * The detail of {@link StructureElement}, e.g the signature of a
         * function. If not provided only the name is used.
         *
         * @since 1.8
         */
        @NonNull
        public Builder detail(@NonNull String detail) {
            this.detail = detail;
            return this;
        }

        /**
         * The start of offset range where this element should be selected and
         * revealed in document when selected in outline view.
         *
         * @since 1.8
         */
        @NonNull
        public Builder selectionStartOffset(int selectionStartOffset) {
            this.selectionStartOffset = selectionStartOffset;
            return this;
        }

        /**
         * The end of offset range where this element should be selected and
         * revealed in document when selected in outline view.
         *
         * @since 1.8
         */
        @NonNull
        public Builder selectionEndOffset(int selectionEndOffset) {
            this.selectionEndOffset = selectionEndOffset;
            return this;
        }

        /**
         * The expanded range is offset range that is typically used to
         * determine if the cursors inside the element to reveal in the element
         * in the UI.
         *
         * @since 1.8
         */
        @NonNull
        public Builder expandedStartOffset(int expandedStartOffset) {
            this.expandedStartOffset = expandedStartOffset;
            return this;
        }

        /**
         * The expanded range is offset range that is typically used to
         * determine if the cursors inside the element to reveal in the element
         * in the UI.
         *
         * @since 1.8
         */
        @NonNull
        public Builder expandedEndOffset(int expandedEndOffset) {
            this.expandedEndOffset = expandedEndOffset;
            return this;
        }

        /**
         * Adds tag for this structure element.
         *
         * @since 1.8
         */
        @NonNull
        public Builder addTag(@NonNull StructureElement.Tag tag) {
            if (this.tags == null) {
                this.tags = new HashSet<>();
            }
            this.tags.add(tag);
            return this;
        }
        
        /**
         * Tags for this structure element.
         *
         * @since 1.8
         */
        @NonNull
        public Builder tags(@NonNull Set<StructureElement.Tag> tags) {
            this.tags = tags;
            return this;
        }

        /**
         * Adds sub element for this structure element.
         *
         * @since 1.8
         */
        @NonNull
        public Builder children(@NonNull StructureElement ... children) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            for (StructureElement structureElement : children) {
                this.children.add(structureElement);
            }
            return this;
        }
        
        /**
         * Sub elements of this structure element. 
         * 
         * @since 1.8
         */
        @NonNull
        public Builder children(@NonNull List<StructureElement> children) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            this.children.addAll(children);
            return this;
            
        }
        
        /**
         * Builds structure element.
         *
         * @since 1.8
         */
        @NonNull
        public StructureElement build() {
            return StructureElementAccessor.getDefault()
                    .createStructureElement(name, detail, selectionStartOffset, 
                            selectionEndOffset, expandedStartOffset, expandedEndOffset, 
                            kind, tags, children);
        }
        
    }
}
