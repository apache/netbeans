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

