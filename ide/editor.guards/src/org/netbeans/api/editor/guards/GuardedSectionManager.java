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

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.guards.GuardedSectionImpl;
import org.netbeans.modules.editor.guards.GuardedSectionsImpl;
import org.netbeans.modules.editor.guards.GuardsAccessor;
import org.netbeans.modules.editor.guards.InteriorSectionImpl;
import org.netbeans.modules.editor.guards.SimpleSectionImpl;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;

/**
 * This is the entry point for clients to manipulate guarded sections
 * of the given document.
 *
 * @author Jan Pokorsky
 */
public final class GuardedSectionManager {

    /**
     * Gets the manager instance.
     * @param doc a document containing guarded sections
     * @return the manager instance or <code>null</code>.
     */
    public static GuardedSectionManager getInstance(StyledDocument doc) {
        return (GuardedSectionManager) doc.getProperty(GuardedSectionManager.class);
    }

    /**
     * Tries to find the simple section of the given name.
     * @param name the name of the requested section
     * @return the found guarded section or <code>null</code> if there is no section
     *         of the given name
     */
    public SimpleSection findSimpleSection(String name) {
        GuardedSection s = impl.findSection(name);
        return (s instanceof SimpleSection) ? (SimpleSection) s : null;
    }

    /**
     * Tries to find the interior section of the given name.
     * @param name the name of the looked-for section
     * @return the found guarded section or <code>null</code> if there is no section
     *         of the given name
     */
    public InteriorSection findInteriorSection(String name) {
        GuardedSection s = impl.findSection(name);
        return (s instanceof InteriorSection) ? (InteriorSection) s : null;
    }
    
    /**
     * Creates an empty simple section at the given position.
     * The position must not be within any existing guarded section
     * and the passed name must not be registered for other
     * already existing section. The created section will initially contain
     * one space and a newline.
     * @return SimpleSection instance that can be used for generating text into
     * the protected region
     * @throws IllegalArgumentException if either the name has been already used, or
     * the position is inside another section or Java Element.
     * @throws BadLocationException if pos is outside of document's scope, or
     * the document does not permit creating the guarded section.
     */
    public SimpleSection createSimpleSection(Position pos, String name)
            throws IllegalArgumentException, BadLocationException {
        return impl.createSimpleSection(pos, name);
    }
    
    /**
     * Creates a simple section from a region of code. The code region will become
     * guarded as in the case of `initComponents' method in forms. The section name
     * can be used to find the section and if the section is persisted, will be written
     * into the output file. Unline {@link #createSimpleSection}, this method is
     * intended for marking existing text protected/guarded.
     * <p/>
     * Use {@link GuardedSection#removeSection()} to remove the protection.
     * 
     * @param start start of the protected text
     * @param end  end of text, exclusive.
     * @param name name for the guarded section
     * @return SimpleSection instance, to allow further manipulation with the 
     * protected content.
     * @throws IllegalArgumentException
     * @throws BadLocationException 
     * @since 1.33
     */
    public SimpleSection protectSimpleRegion(Position start, Position end, String name) 
            throws IllegalArgumentException, BadLocationException {
        return impl.createSimpleSection(start, end, name);
    }
    
    /**
     * Creates an empty interior section at the given position.
     * The position must not be within any existing guarded section
     * and the passed name must not be registered to other
     * already existing section. The created section will initially contain
     * one space and a newline in all its parts (header, body and footer).
     * @return InteriorSection instance that can be used for generating text into
     * the protected region
     * @throws IllegalArgumentException if either the name has been already used, or
     * the position is inside another section or Java Element.
     * @throws BadLocationException if pos is outside of document's scope, or
     * the document does not permit creating the guarded section.
     */
    public InteriorSection createInteriorSection(Position pos, String name)
            throws IllegalArgumentException, BadLocationException {
        return impl.createInteriorSection(pos, name);
    }
    
    /** Gets all sections.
     * @return an iterable over {@link GuardedSection}s
     */
    public Iterable<GuardedSection> getGuardedSections() {
        return impl.getGuardedSections();
    }
    
    // package

    // private
    
    static {
        GuardsAccessor.DEFAULT = new GuardsAccessor() {
            public GuardedSectionManager createGuardedSections(GuardedSectionsImpl impl) {
                return new GuardedSectionManager(impl);
            }

            public SimpleSection createSimpleSection(SimpleSectionImpl impl) {
                return new SimpleSection(impl);
            }

            public InteriorSection createInteriorSection(InteriorSectionImpl impl) {
                return new InteriorSection(impl);
            }
            
            public GuardedSectionImpl getImpl(GuardedSection gs) {
                return gs.getImpl();
            }

            @Override
            public GuardedSection clone(GuardedSection gs, int offset) {
                return gs.clone(offset);
            }
            
        };
    }
    
    /** Creates a new instance of GuardedDocument */
    private GuardedSectionManager(GuardedSectionsImpl impl) {
        this.impl = impl;
    }

    private final GuardedSectionsImpl impl;

}
