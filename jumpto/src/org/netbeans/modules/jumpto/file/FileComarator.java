/**
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

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.common.StateFullComparator;
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
public final class FileComarator extends EntityComparator<FileDescriptor> implements StateFullComparator<FileDescriptor>{

    private final ChangeSupport support;
    private final boolean caseSensitive;
    private boolean usePreferred;

    public FileComarator(
            final boolean usePreferred,
            final boolean caseSensitive ) {
        this.caseSensitive = caseSensitive;
        this.usePreferred = usePreferred;
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
        // Containig project
        String e1projectName = e1.getProjectName();
        String e2projectName = e2.getProjectName();
        int result = compareProjects(e1projectName, e2projectName);
        if(result != 0) {
            return result; // e1projectName NOT equals to e2projectName
        }
        // here: e1projectName equals to e2projectName
        // File name
        int r = compare(e1.getFileName(), e2.getFileName(), caseSensitive);
        if ( r != 0 ) {
            return r;
        }
        // Project name
        r = compare(e1.getProjectName(), e2.getProjectName(), caseSensitive);
        if ( r != 0 ) {
            return r;
        }
        // Relative location
        r = compare( e1.getOwnerPath(), e2.getOwnerPath(), caseSensitive);
        return r;
    }

    @Override
    public void addChangeListener(@NonNull final ChangeListener listener) {
        support.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(@NonNull final ChangeListener listener) {
        support.removeChangeListener(listener);
    }

}

