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
package org.netbeans.api.editor.guards;

import org.netbeans.modules.editor.guards.OffsetPosition;
import java.beans.PropertyVetoException;
import javax.swing.text.Position;
import org.netbeans.modules.editor.guards.GuardedSectionImpl;

/**
 * Represents one guarded section.
 */
public abstract class GuardedSection {
    
    private final GuardedSectionImpl impl;
    private final GuardedSection delegate;
            final int offset;

    /**
     * Creates new section.
     * @param name Name of the new section.
     */
    GuardedSection(GuardedSectionImpl impl) {
        assert impl != null;
        this.impl = impl;
        impl.attach(this);
        this.delegate = null;
        this.offset = 0;
    }
    
    GuardedSection(GuardedSection delegate, int offset) {
        this.impl = null;
        this.delegate = delegate;
        this.offset = offset;
    }
    
    /**
     * Get the name of the section.
     * @return the name
     */
    public String getName() {
        return impl != null ? impl.getName() : delegate.getName();
    }

    /**
     * Set the name of the section.
     * @param name the new name
     * @exception PropertyVetoException if the new name is already in use
     */
    public void setName(String name) throws PropertyVetoException {
        if (impl == null) throw new IllegalStateException();
        impl.setName(name);
    }

    /**
     * Removes the section and the text of the section from the Document.
     * The section will then be invalid
     * and it will be impossible to use its methods.
     */
    public void deleteSection() {
        if (impl == null) throw new IllegalStateException();
        impl.deleteSection();
    }

    /**
     * Tests if the section is still valid - it is not removed from the
     * source.
     */
    public boolean isValid() {
        return impl != null ? impl.isValid() : delegate.isValid();
    }

    /**
     * Removes the section from the Document, but retains the text contained
     * within. The method should be used to unprotect a region of code
     * instead of calling NbDocument.
     */
    public void removeSection() {
        if (impl == null) throw new IllegalStateException();
        impl.removeSection();
    }
    
    /**
     * Gets the begin of section. To this position is set the caret
     * when section is open in the editor.
     * @return the position to place the caret.
     */
    public Position getCaretPosition() {
        return impl != null ? impl.getCaretPosition() : new OffsetPosition(delegate.getCaretPosition(), offset);
    }
    
    /**
     * Gets the text contained in the section.
     * @return The text contained in the section.
     */
    public String getText() {
        return impl != null ? impl.getText() : delegate.getText();
    }

    /**
     * Assures that a position is not inside the guarded section. Complex guarded sections
     * that contain portions of editable text can return true if the tested position is
     * inside one of such portions provided that permitHoles is true.
     * @param pos position in question
     * @param permitHoles if false, guarded section is taken as a monolithic block
     * without any holes in it regardless of its complexity.
     * @return <code>true</code> if the position is inside section.
     */
    public boolean contains(Position pos, boolean permitHoles) {
        return impl != null ? impl.contains(pos, permitHoles) : delegate.contains(new OffsetPosition(pos, -offset), permitHoles);
    }
    
    /**
     * Returns the end position of the whole guarded section.
     * @return the end position of the guarded section.
     */
    public Position getEndPosition() {
        return impl != null ? impl.getEndPosition() : new OffsetPosition(delegate.getEndPosition(), offset);
    }
    
    /** 
     * Returns the start position of the whole guarded section.
     * @return the start position of the guarded section.
     */
    public Position getStartPosition() {
        return impl != null ? impl.getStartPosition() : new OffsetPosition(delegate.getStartPosition(), offset);
    }
    
    GuardedSectionImpl getImpl() {
        return impl;
    }

    GuardedSection getDelegate() {
        return delegate;
    }

    abstract GuardedSection clone(int offset);
}
