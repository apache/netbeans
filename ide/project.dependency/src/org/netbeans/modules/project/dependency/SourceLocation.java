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
package org.netbeans.modules.project.dependency;

import org.openide.filesystems.FileObject;

/**
 * Represents a location range in a project file occupied by a project model object.
 * In case the range cannot be determined,
 * the start an end position are equal. In the case the position cannot be determined
 * at all, the start (and end) positions are set to -1.
 * <p/>
 * In the case the object itself is not present in the project file, but is implied
 * by another project construction, the {@link #getImpliedBy()} is not null, and provides
 * the model for that construction. For example a dependency may be introduced by an intermediate
 * libraries. In that case, when the API is queried for the dependency declaration source,
 * it will return the direct dependence that introduced the dependency in question from {@link #getImpliedBy}
 * and its location range.
 * 
 * @author sdedic
 */
public final class SourceLocation {
    private final FileObject file;
    private final int startOffset;
    private final int endOffset;
    private final Object impliedBy;

    public SourceLocation(FileObject file, int startOffset, int endOffset, Object impliedBy) {
        this.file = file;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.impliedBy = impliedBy;
    }

    /**
     * @return Returns the file.
     */
    public FileObject getFile() {
        return file;
    }

    /**
     * Returns starting offset of the construct in the project file. May return -1,
     * if the position can not be determined.
     * @return startinf offset or -1
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * Ending offset of the project construct in the project file, exclusive. May return
     * the same value as {@link #getStartOffset} if the range can not be determined. Will return
     * -1 if {@link #getStartOffset()} is -1.
     * @return ending offset, inclusive, or -1.
     */
    public int getEndOffset() {
        return endOffset;
    }

    /**
     * Determines if the construct is directly in the project source, or implied by some
     * other construction. If this method returns {@code null}, the queried project construct
     * is specified in the project file, and {@link #getStartOffset()} / {@link #getEndOffset()}
     * give its location or range. If the return value is non-{@code null}, it represents
     * a project element / metadata that caused the queried project construct into existence.
     * Start and end offset apply to that "owning" construct instead.
     * 
     * @return null if the Location is occupied by the queried construct directly, or
     * the project element that impled the queried construct.
     */
    public Object getImpliedBy() {
        return impliedBy;
    }
    
    /**
     * @return true, if the offset within the file is known
     */
    public boolean hasPosition() {
        return startOffset > -1;
    }
    
    /**
     * @return false, if the Location represents a non-empty range.
     */
    public boolean isEmpty() {
        return startOffset >= endOffset;
    }
}
