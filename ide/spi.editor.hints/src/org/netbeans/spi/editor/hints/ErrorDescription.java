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
package org.netbeans.spi.editor.hints;

import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 * Represents one error with a description text, span in the document, list
 * of fixes and a severity. Please see the static methods in the class
 * {@link ErrorDescriptionFactory} if you want to create instances of this class.
 *
 * @author Jan Lahoda
 */
public final class ErrorDescription {

    private final String id;
    private final String description;
    private final CharSequence details;
    private final Severity severity;
    private final String customType;
    private final LazyFixList fixes;
    private final PositionBounds span;
    private final ArrayList<PositionBounds> spanTail = new ArrayList<>();
    private final FileObject file;

    /**
     * The constructor is intentionally not public. Use 
     * {@link ErrorDescriptionFactory} when you need an instance of this class.
     */
    ErrorDescription(FileObject file, String id, String description, CharSequence details, Severity severity, LazyFixList fixes, PositionBounds span) {
        this.id = id;
        this.description = description;
        this.details = details;
        this.severity    = severity;
        this.customType = null;
        this.fixes       = fixes;
        this.span        = span;
        this.file        = file;
    }
    
    
    ErrorDescription(FileObject file, String id, String description, CharSequence details, Severity severity, String customType, LazyFixList fixes, 
            PositionBounds span, ArrayList<PositionBounds> spanTail) {
        this.id = id;
        this.description = description;
        this.details = details;
        this.severity    = severity;
        this.customType = customType;
        this.fixes       = fixes;
        this.span        = span;
        this.spanTail.clear();
        this.spanTail.addAll(spanTail);
        this.file        = file;
    }
    
    /**
     * The constructor is intentionally not public. Use 
     * {@link ErrorDescriptionFactory} when you need an instance of this class.
     */
    ErrorDescription(FileObject file, String id, String description, CharSequence details, Severity severity, String customType, LazyFixList fixes, PositionBounds span) {
        this.id = id;
        this.description = description;
        this.details = details;
        this.severity    = severity;
        this.customType = customType;
        this.fixes       = fixes;
        this.span        = span;
        this.file        = file;
    }

    /**
     * @return the id specified when constructing this {@link ErrorDescription},
     *         or null if none was specified
     * @since 1.22
     */
    public @CheckForNull String getId() {
        return id;
    }

    /**
     * @return description of the error that is displayed to the user.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the details specified when constructing this {@link ErrorDescription},
     *         or null if none was specified
     * @since 1.22
     */
    public @CheckForNull CharSequence getDetails() {
        return details;
    }

    /**
     * The severity determines how the hint will be rendered.
     * @return {@link Severity} of the error
     */
    public Severity getSeverity() {
        return severity;
    }
    
    /**
     * The custom type is an annotation type provided for particular error
     * @return custom annotation type
     */
    public String getCustomType() {
        return customType;
    }

    /**
     * The list of fixes that will be associated with the error.
     * @return {@link LazyFixList} containing the fixes
     */
    public LazyFixList getFixes() {
        return fixes;
    }

    /**
     * @return where the error will be marked in the document or <code>null</code> if no place to mark
     */
    public PositionBounds getRange() {
        return span;
    }
    
    /**
     *  Return range tail: to support multiple ranges for error/warning
     * @return 
     * @since 1.42
     */
    public ArrayList<PositionBounds> getRangeTail() {
        return spanTail;
    }

    /**
     * @return associated file or <code>null</code> if there is none
     */
    public FileObject getFile() {
        return file;
    }
    
    @Override
    public String toString() {
        try {
            return (span != null ? span.getBegin().getLine() + ":" + span.getBegin().getColumn() + "-" + span.getEnd().getLine() + ":" + span.getEnd().getColumn() : "<no-span>") + ":" + severity.getDisplayName() + ":" + description;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ErrorDescription other = (ErrorDescription) obj;
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if (this.severity != other.severity && (this.severity == null || !this.severity.equals(other.severity))) {
            return false;
        }
        if (this.span != null && other.span != null) {
            if (this.span.getBegin().getOffset() != other.span.getBegin().getOffset()) {
                return false;
            }
            if (this.span.getEnd().getOffset() != other.span.getEnd().getOffset()) {
                return false;
            }
        } else if (this.span != other.span) {
            return false;
        }
        if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 17 * hash + (this.severity != null ? this.severity.hashCode() : 0);
        hash = 17 * hash + (this.span != null ? this.span.getBegin().getOffset() : 0);
        hash = 17 * hash + (this.span != null ? this.span.getEnd().getOffset() : 0);
        hash = 17 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }




}
