/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jumpto.file;

import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.common.DescriptorAccessor;
import org.netbeans.modules.jumpto.common.StateFullComparator;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.openide.util.ChangeSupport;

/**
 * The {@code FileComarator} establishes the sort order of the files.
 * It is used for ordering a list that will be displayed in:
 * <ul>
 *   <li>the field "Matching Files" of the dialog "Go to File"
 *       (Alt+Shift+O)</li>
 * </ul>
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
//@NotThreadSave //Use from EDT
public abstract class FileComarator extends EntityComparator<FileDescriptor> implements StateFullComparator<FileDescriptor> {

    private final ChangeSupport support;
    protected final boolean caseSensitive;
    protected final boolean preferOpPrjs;
    protected boolean usePreferred;

    private FileComarator(
            final boolean usePreferred,
            final boolean caseSensitive,
            final boolean preferOpPrjs) {
        this.caseSensitive = caseSensitive;
        this.usePreferred = usePreferred;
        this.preferOpPrjs = preferOpPrjs;
        this.support = new ChangeSupport(this);
    }

    boolean isUsePreferred() {
        return usePreferred;
    }

    void setUsePreferred(final boolean usePreferred) {
        final boolean fire = this.usePreferred ^ usePreferred;
        this.usePreferred = usePreferred;
        if (fire) {
            support.fireChange();
        }
    }

    abstract void setText(@NonNull final String text);

    public abstract int compare(FileDescriptor e1, FileDescriptor e2);

    void fireChange() {
        support.fireChange();
    }

    private static final class Alphabet extends FileComarator {

        Alphabet(
                final boolean usePreferred,
                final boolean caseSensitive,
                final boolean preferOpPrjs) {
            super(usePreferred, caseSensitive, preferOpPrjs);
        }

        void setText(@NonNull final String text) {
        }

        @Override
        public int compare(FileDescriptor e1, FileDescriptor e2) {
            // If prefered prefer prefered
            if ( usePreferred ) {
                FileProviderAccessor fpa = FileProviderAccessor.getInstance();
                boolean isE1Curr = fpa.isFromCurrentProject(e1);
                boolean isE2Curr = fpa.isFromCurrentProject(e2);
                if (isE1Curr && !isE2Curr) {
                    return -1;
                }
                if (!isE1Curr && isE2Curr) {
                    return 1;
                }
            }
            int result;
            // Containig project
            if (preferOpPrjs) {
                String e1projectName = e1.getProjectName();
                String e2projectName = e2.getProjectName();
                result = compareProjects(e1projectName, e2projectName);
                if(result != 0) {
                    return result; // e1projectName NOT equals to e2projectName
                }
            }
            // here: e1projectName equals to e2projectName
            // File name
            result = compare(e1.getFileName(), e2.getFileName(), caseSensitive);
            if ( result != 0 ) {
                return result;
            }
            // Project name
            result = compare(e1.getProjectName(), e2.getProjectName(), caseSensitive);
            if ( result != 0 ) {
                return result;
            }
            // Relative location
            result = compare( e1.getOwnerPath(), e2.getOwnerPath(), caseSensitive);
            return result;
        }
    }

    private static final class Levenshtein extends FileComarator {

        private static final String ATTR_PATTERN = "Pattern";       //NOI18N
        private static final String ATTR_LS_DIST = "LevenshteinDistance";   //NOI18N
        private static final String ATTR_LS_TAIL = "LevenshteinTail";   //NOI18N

        private String text;

        Levenshtein(
                final String text,
                final boolean usePreferred,
                final boolean caseSensitive,
                final boolean preferOpPrjs) {
            super(usePreferred, caseSensitive, preferOpPrjs);
            this.text = text;
        }

        void setText(@NonNull final String text) {
            final boolean fire = !Objects.equals(this.text, text);
            this.text = text;
            if (fire) {
                fireChange();
            }
        }

        @Override
        public int compare(FileDescriptor e1, FileDescriptor e2) {
            if (usePreferred) {
                FileProviderAccessor fpa = FileProviderAccessor.getInstance();
                boolean isE1Curr = fpa.isFromCurrentProject(e1);
                boolean isE2Curr = fpa.isFromCurrentProject(e2);
                if (isE1Curr && !isE2Curr) {
                    return -1;
                }
                if (!isE1Curr && isE2Curr) {
                    return 1;
                }
            }
            // Containig project
            int result;
            if (preferOpPrjs) {
                String e1projectName = e1.getProjectName();
                String e2projectName = e2.getProjectName();
                result = compareProjects(e1projectName, e2projectName);
                if(result != 0) {
                    return result; // e1projectName NOT equals to e2projectName
                }
            }
            int l1, l2, t1, t2;
            Object d = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_LS_DIST);
            Object t = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_LS_TAIL);
            Object p = DescriptorAccessor.getInstance().getAttribute(e1, ATTR_PATTERN);
            if (d instanceof Integer && t instanceof Integer && text.equals(p)) {
                l1 = (Integer) d;
                t1 = (Integer) t;
            } else {
                final String fn = e1.getFileName();
                final String prefix = levenshteinPrefix(fn, text, caseSensitive);
                l1 = levenshteinDistance(prefix, text, caseSensitive);
                t1 = fn.length() - prefix.length();
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_LS_DIST, l1);
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_LS_TAIL, t1);
                DescriptorAccessor.getInstance().setAttribute(e1, ATTR_PATTERN, text);
            }
            d = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_LS_DIST);
            t = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_LS_TAIL);
            p = DescriptorAccessor.getInstance().getAttribute(e2, ATTR_PATTERN);
            if (d instanceof Integer && t instanceof Integer && text.equals(p)) {
                l2 = (Integer) d;
                t2 = (Integer) t;
            } else {
                final String fn = e2.getFileName();
                final String prefix = levenshteinPrefix(fn, text, caseSensitive);
                l2 = levenshteinDistance(prefix, text, caseSensitive);
                t2 = fn.length() - prefix.length();
                DescriptorAccessor.getInstance().setAttribute(e2, ATTR_LS_DIST, l2);
                DescriptorAccessor.getInstance().setAttribute(e2, ATTR_LS_TAIL, t2);
                DescriptorAccessor.getInstance().setAttribute(e2, ATTR_PATTERN, text);
            }

            result = l1 - l2;
            if (result != 0) {
                return result;
            }
            result = t1 - t2;
            if (result != 0) {
                return result;
            }

            // here: e1projectName equals to e2projectName
            // File name
            result = compare(e1.getFileName(), e2.getFileName(), caseSensitive);
            if ( result != 0 ) {
                return result;
            }
            // Project name
            result = compare(e1.getProjectName(), e2.getProjectName(), caseSensitive);
            if ( result != 0 ) {
                return result;
            }
            // Relative location
            result = compare( e1.getOwnerPath(), e2.getOwnerPath(), caseSensitive);
            return result;
        }
    }

    @Override
    public void addChangeListener(@NonNull final ChangeListener listener) {
        support.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(@NonNull final ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    @NonNull
    public static FileComarator create(
            @NonNull final GoToSettings.SortingType kind,
            @NonNull final String text,
            final boolean usePreferred,
            final boolean caseSensitive,
            final boolean preferOpPrjs) {
        switch (kind) {
            case LEXICOGRAPHIC:
                return new Alphabet(usePreferred, caseSensitive, preferOpPrjs);
            case LEVENSHTEIN:
                return new Levenshtein(text, usePreferred, caseSensitive, preferOpPrjs);
            default:
                throw new IllegalArgumentException(String.valueOf(kind));
        }
    }
}

