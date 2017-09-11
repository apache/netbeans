/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.implspi;

import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Allows to control Source state within the Parsing subsystem based on
 * external events.
 * The {@link SourceEnvironment} gets this instance to forward interesting
 * events to the parser. It is possible to invalidate the source, region of source.
 * @author sdedic
 * @since 9.2
 */
public final class SourceControl {

    private static final RequestProcessor RP = new RequestProcessor (
        "parsing-event-collector",         //NOI18N
        1,
        false,
        false);

    private final RequestProcessor.Task resetTask;

    private final Source source;

    /**
     * Creates a new control object for the source.
     * 
     * @param source the source which is controlled
     */
    public SourceControl(@NonNull final Source source) {
        Parameters.notNull("source", source);   //NOI18N
        this.source = source;
        this.resetTask = RP.create(new Runnable() {
            @Override
            public void run() {
                if (SourceAccessor.getINSTANCE().getEnv(source).isReparseBlocked()) {
                    return;
                }
                TaskProcessor.resetStateImpl(source);
            }
        });
    }


    /**
     * Provides reference to the Source. The reference is provided for convenience
     * to help proper garbage collection of the Source object. If the client keeps
     * a Source instance, it should use WeakReference to store it.
     * @return Source instance or {@code null}
     */
    @NonNull
    public Source getSource() {
        return source;
    }

    /**
     * Informs that the source was changed in an unspecified way, and possibly
     * its mime was changed.
     * The source will be reparsed and if {@code mimeChanged} is true, the MIME
     * type will be re-read and the appropriate parser will be used for parsing.
     * <p/>
     * The {@code mimeChanged} flag is used for optimization; set it aggressively,
     * so proper parser is looked up.
     * 
     * @param mimeChanged true, if mime type might have changed.
     */
    public void sourceChanged(final boolean mimeChanged) {
        final SourceAccessor sa = SourceAccessor.getINSTANCE();
        final Set<SourceFlags> flags = EnumSet.of(
            SourceFlags.CHANGE_EXPECTED,
            SourceFlags.INVALID,
            SourceFlags.RESCHEDULE_FINISHED_TASKS);
        if (mimeChanged) {
            sa.mimeTypeMayChanged(source);
        }
        sa.setSourceModification(source, true, -1, -1);
        sa.setFlags(source, flags);
        TaskProcessor.resetState(source, true, true);
    }

    /**
     * Informs that part of the source was edited. The parser implementation
     * may reparse just a portion of the text or a whole (depends on the 
     * implementation). Setting {@code startOffset} or {@code endOffset}
     * to -1 will execute an equivalent of {@code sourceChanged(false)}.
     * 
     * @param startOffset start of the change
     * @param endOffset end of the change
     */
    public void regionChanged(int startOffset, int endOffset) {
        final SourceAccessor sa = SourceAccessor.getINSTANCE();
        final Set<SourceFlags> flags = EnumSet.of(
            SourceFlags.CHANGE_EXPECTED,
            SourceFlags.INVALID,
            SourceFlags.RESCHEDULE_FINISHED_TASKS);
        sa.setSourceModification(source, true, startOffset, endOffset);
        sa.setFlags(source, flags);
        TaskProcessor.resetState(source, true, true);
    }

    /**
     * Informs about a non-text change in the Source, such as caret movement
     * or focus. Does not invalidate the parsing result, but may re-execute certain
     * tasks.
     */
    public void stateChanged() {
        final SourceAccessor sa = SourceAccessor.getINSTANCE();
        final Set<SourceFlags> flags = EnumSet.of(SourceFlags.CHANGE_EXPECTED);
        sa.setSourceModification(source, false, -1, -1);
        sa.setFlags(source, flags);
        TaskProcessor.resetState(source, false, true);
    }

    /**
     * Marks the source for reparsing after the specified delay.
     *
     * @param delay time in milliseconds.
     */
    public void revalidate(int delay) {
        resetTask.schedule(delay);
    }
}
