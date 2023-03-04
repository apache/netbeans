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

package org.netbeans.spi.editor.guards.support;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.modules.editor.guards.GuardedSectionsImpl;
import org.netbeans.modules.editor.guards.GuardsSupportAccessor;
import org.netbeans.modules.editor.guards.PositionBounds;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;

/**
 * The helper class that simplifies writing particular {@link GuardedSectionsProvider}
 * implementations. Subclasses have to implement just {@link #readSections}
 * and {@link #writeSections} methods.
 * 
 * @author Jan Pokorsky
 */
public abstract class AbstractGuardedSectionsProvider implements GuardedSectionsProvider {

    private final GuardedSectionsImpl impl;
    private final boolean useReadersWritersOnSet;
    
    /**
     * Creates an AbstractGuardedSectionsProvider.
     * @param editor an editor abstraction
     */
    protected AbstractGuardedSectionsProvider(GuardedEditorSupport editor) {
        this(editor, false);
    }
    
    /**
     * Creates an AbstractGuardedSectionsProvider.
     * @param editor an editor abstraction
     * @param useReadersWritersOnSet if readers and writers should be used when the content of the guarded section's text is set
     * @since 1.20
     */
    protected AbstractGuardedSectionsProvider(GuardedEditorSupport editor, boolean useReadersWritersOnSet) {
        this.impl = new GuardedSectionsImpl(editor);
        this.useReadersWritersOnSet = useReadersWritersOnSet;
    }

    public final Reader createGuardedReader(InputStream stream, Charset charset) {
        return impl.createGuardedReader(this, stream, charset);
    }

    public Writer createGuardedWriter(OutputStream stream, Charset charset) {
        return impl.createGuardedWriter(this, stream, charset);
    }
    
    /**
     * This should be implemented to persist a list of guarded sections inside
     * the passed content.
     * @param sections guarded sections to persist
     * @param content content
     * @return content including guarded sections
     */
    public abstract char[] writeSections(List<GuardedSection> sections, char[] content);
    
    /**
     * This should be implemented to extract guarded sections out of the passed
     * content.
     * @param content content including guarded sections
     * @return the content that will be presented to users and the list of guarded sections
     */
    public abstract Result readSections(char[] content);
    
    /**
     * Creates a simple section object to represent section read by
     * the {@link #readSections readSections}.
     * @param name the section name 
     * @param begin the start offset
     * @param end the end offset
     * @return the simple section instance
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     */
    public final SimpleSection createSimpleSection(String name, int begin, int end) throws BadLocationException {
        return impl.createSimpleSectionObject(name, PositionBounds.createUnresolved(begin, end, impl));
    }
    
    /**
     * Creates an interior section object to represent section read by
     * the {@link #readSections readSections}.
     * @param name the section name
     * @param headerBegin begin the start offset of the first guarded part
     * @param headerEnd end the end offset of the first guarded part
     * @param footerBegin begin the start offset of the second guarded part
     * @param footerEnd end the end offset of the second guarded part
     * @return the interior section object
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     */
    public final InteriorSection createInteriorSection(String name, int headerBegin, int headerEnd, int footerBegin, int footerEnd) throws BadLocationException {
        return impl.createInteriorSectionObject(
                name,
                PositionBounds.createUnresolved(headerBegin, headerEnd, impl),
                PositionBounds.createBodyUnresolved(headerEnd + 1, footerBegin - 1, impl),
                PositionBounds.createUnresolved(footerBegin, footerEnd, impl)
                );
    }
    
    public final class Result {

        private final char[] content;

        private final List<GuardedSection> sections;
        
        public Result (char[] content, List<GuardedSection> sections) {
            this.content = content;
            this.sections = sections;
        }
        
        public char[] getContent() {
            return this.content;
        }
        
        public List<GuardedSection> getGuardedSections() {
            return this.sections;
        }
    }
    
    static {
        GuardsSupportAccessor.DEFAULT = new GuardsSupportAccessor() {
            @Override public boolean isUseReadersWritersOnSet(AbstractGuardedSectionsProvider impl) {
                return impl.useReadersWritersOnSet;
            }
        };
    }
}
