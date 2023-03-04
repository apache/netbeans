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
package org.netbeans.api.lsp;

import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.lsp.StructureElementAccessor;
import org.openide.filesystems.FileObject;

/**
 * StructureElement is a tree item that shows the structure of the source code.
 *
 * @author Petr Pisl
 * @since 1.8
 */
public final class StructureElement {

    static {
        StructureElementAccessor.setDefault(new StructureElementAccessor() {
            @Override
            public StructureElement createStructureElement(FileObject file, String name, String detail, int selectionStartOffset, int selectionEndOffset, int expandedStartOffset, int expandedEndOffset, Kind kind, Set<Tag> tags, List<StructureElement> children) {
                return new StructureElement(file, name, detail, selectionStartOffset, selectionEndOffset, expandedStartOffset, expandedEndOffset, kind, tags, children);
            }
        });
    }

    private final FileObject file;
    private final String name;
    private final String detail;
    private final Range selectionRange;
    private final Range expandedRange;
    private final Kind kind;
    private final Set<Tag> tags;
    private final List<StructureElement> children;

    /**
     * Kind of the structure element.
     */
    public static enum Kind {
        File,
        Module,
        Namespace,
        Package,
        Class,
        Method,
        Property,
        Field,
        Constructor,
        Enum,
        Interface,
        Function,
        Variable,
        Constant,
        String,
        Number,
        Boolean,
        Array,
        Object,
        Key,
        Null,
        EnumMember,
        Struct,
        Event,
        Operator,
        TypeParameter
    }

    /**
     * Tags are extra annotations that tweak the rendering of a symbol.
     */
    public static enum Tag {
        Deprecated;
    }

    private StructureElement(FileObject file, @NonNull String name, String detail, int selectionStartOffset, int selectionEndOffset, int expandedStartOffset, int expandedEndOffset, @NonNull Kind kind, Set<Tag> tags, List<StructureElement> children) {
        this.file = file;
        this.name = name;
        this.detail = detail;
        this.selectionRange = new Range(selectionStartOffset, selectionEndOffset);
        this.expandedRange = new Range(expandedStartOffset, expandedEndOffset);
        this.kind = kind;
        this.tags = tags;
        this.children = children;
    }

    /**
     * The name is displayed in the structure tree and other ui.
     *
     * @return The name of this element.
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * The start of offset range where this element should be selected and
     * revealed in document when selected in outline view.
     *
     * @return start offset of the selection
     */
    public int getSelectionStartOffset() {
        return selectionRange.getStartOffset();
    }

    /**
     * The end of offset range where this element should be selected and
     * revealed in document when selected.
     *
     * @return end offset of the selection
     */
    public int getSelectionEndOffset() {
        return selectionRange.getEndOffset();
    }

    /**
     * The expanded range is offset range that is typically used to determine if
     * the cursors inside the element to reveal in the element in the UI.
     *
     * @return start of the enclosed range
     */
    public int getExpandedStartOffset() {
        return expandedRange.getStartOffset();
    }

    /**
     * The expanded range is offset range that is typically used to determine if
     * the cursors inside the element to reveal in the element in the UI.
     *
     * @return end of the enclosed range
     */
    public int getExpandedEndOffset() {
        return expandedRange.getEndOffset();
    }
    
    /**
     * The selection range marks part of document that should be revealed
     * and selected for an element.
     *
     * @return start offset of the selection
     * @since 1.9
     */
    public Range getSelectionRange() {
        return selectionRange;
    }
    
    /**
     * The expanded range is offset range that is typically used to determine if
     * the cursors inside the element to reveal in the element in the UI.
     *
     * @return expanded range.
     * @since 1.9
     */
    public Range getExpandedRange() {
        return expandedRange;
    }

    /**
     * Kind of this structure element.
     *
     * @return Kind of this symbol
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Tags for this element.
     *
     * @return list of tags
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * More detail for this symbol, e.g the signature of a function. If not
     * provided only the name is used.
     *
     * @return the detail text for this element
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Children of this element, e.g. method and fields of a class.
     *
     * @return list of sub elements.
     */
    public List<StructureElement> getChildren() {
        return children;
    }

    /**
     * File which contains this element. If {@code null}, the file may
     * be inaccessible; in that case, offsets will be also set to -1.
     * @return owning file, or {@code null}
     * @since 1.9
     */
    @CheckForNull
    public FileObject getFile() {
        return file;
    }
}
