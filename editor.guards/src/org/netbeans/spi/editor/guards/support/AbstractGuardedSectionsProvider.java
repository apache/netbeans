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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
