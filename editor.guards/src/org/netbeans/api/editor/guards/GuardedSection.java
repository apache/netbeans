/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
