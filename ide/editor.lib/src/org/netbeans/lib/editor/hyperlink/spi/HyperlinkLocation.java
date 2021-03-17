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
package org.netbeans.lib.editor.hyperlink.spi;

import java.util.Objects;
import org.openide.filesystems.FileObject;

/**
 * Represents the target location of a hyperlink. Location is a range inside
 * a file object, such as a line inside a text file.
 *
 * @author Dusan Balek
 * @since 4.20
 */
public final class HyperlinkLocation {

    private final FileObject fileObject;
    private final int startOffset;
    private final int endOffset;

    public HyperlinkLocation(FileObject fileObject, int startOffset, int endOffset) {
        this.fileObject = fileObject;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Target file object of this hyperlink.
     *
     * @return file object
     */
    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * The start offset of a hyperlink's target range.
     *
     * @return offset
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * The end offset of a hyperlink's target range.
     *
     * @return offset
     */
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.fileObject);
        hash = 29 * hash + this.startOffset;
        hash = 29 * hash + this.endOffset;
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
        final HyperlinkLocation other = (HyperlinkLocation) obj;
        if (this.startOffset != other.startOffset) {
            return false;
        }
        if (this.endOffset != other.endOffset) {
            return false;
        }
        if (!Objects.equals(this.fileObject, other.fileObject)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HyperlinkLocation{" + "fileObject=" + fileObject + ", startOffset=" + startOffset + ", endOffset=" + endOffset + '}';
    }
}
