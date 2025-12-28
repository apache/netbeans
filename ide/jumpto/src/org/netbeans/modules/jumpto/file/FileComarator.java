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
    protected boolean preferOpPrjs;
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

    void setUsePreferred(boolean usePreferred) {
        if (this.usePreferred != usePreferred) {
            this.usePreferred = usePreferred;
            support.fireChange();
        }
    }

    void setPrefereOpenProjects(boolean prefereOpenProjects) {
        if (this.preferOpPrjs != prefereOpenProjects) {
            this.preferOpPrjs = prefereOpenProjects;
            support.fireChange();
        }
    }

    abstract void setText(@NonNull final String text);

    @Override
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

        @Override
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

        @Override
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
        return switch (kind) {
            case LEXICOGRAPHIC -> new Alphabet(usePreferred, caseSensitive, preferOpPrjs);
            case LEVENSHTEIN -> new Levenshtein(text, usePreferred, caseSensitive, preferOpPrjs);
        };
    }
}

