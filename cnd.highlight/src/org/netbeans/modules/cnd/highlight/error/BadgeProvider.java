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

package org.netbeans.modules.cnd.highlight.error;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.project.NativeProject;

/**
 *
 */
public class BadgeProvider {
    private static final BadgeProvider myInstance = new BadgeProvider();

    private final Storage storage = new Storage();
    private final Object listLock = new Object();

    private BadgeProvider() {
    }

    public static BadgeProvider getInstance(){
        return myInstance;
    }

    public void invalidateProject(CsmProject project){
        boolean badgeStateChanged = false;
        synchronized (listLock){
            boolean oldState = storage.contains(project);
            CsmFileInfoQuery fiq = CsmFileInfoQuery.getDefault();
            ProjectFiles: for( CsmFile file : project.getAllFiles() ) {
                if (CsmFileInfoQuery.getDefault().isStandardHeadersIndexer(file)) {
                    continue;
                }
                if (!file.getErrors().isEmpty()) {
                    if (storage.add(file)) {
                        badgeStateChanged = true;
                    }
                    continue ProjectFiles;
                }
                for (CsmInclude incl : fiq.getBrokenIncludes(file)) {
                    if (incl.getIncludeFile() == null) {
                        if (storage.add(file)) {
                            badgeStateChanged = true;
                        }
                        continue ProjectFiles;
                    }
                }
                if (storage.remove(file)) {
                    badgeStateChanged = true;
                }
            }
            boolean newState = storage.contains(project);
            // if state not changed no need to fire till file badging is not provided
            if (oldState == newState) {
                badgeStateChanged = false;
            }
        }
        if (badgeStateChanged) {
            fireBadgeChanged(project);
        }
    }

    public void invalidateFile(CsmFile file){
        if (CsmFileInfoQuery.getDefault().isStandardHeadersIndexer(file)) {
            return;
        }
        boolean badgeStateChanged = false;
        CsmProject project = file.getProject();
        synchronized (listLock){
            boolean oldState = storage.contains(project);
            boolean badFile = false;
            if (CsmFileInfoQuery.getDefault().hasBrokenIncludes(file) || !file.getErrors().isEmpty()) {
                badFile = true;
                if (storage.add(file)) {
                    badgeStateChanged = true;
                }
            }
            if (!badFile && storage.remove(file)){
                badgeStateChanged = true;
            }
            boolean newState = storage.contains(project);
            // if state not changed no need to fire till file badging is not provided
            if (oldState == newState) {
                badgeStateChanged = false;
            }
        }
        if (badgeStateChanged) {
            fireBadgeChanged(project);
        }
    }

    private void fireBadgeChanged(CsmProject csmProject){
        BrokenProjectService.fireChanges(null);
    }

    private void fireBadgeChanged(){
        BrokenProjectService.fireChanges(null);
    }

    public void onFileRemoved(CsmFile file) {
        boolean badgeStateChanged = false;
        CsmProject project = file.getProject();
        synchronized (listLock){
            boolean oldState = storage.contains(project);
            if (storage.remove(file)){
                badgeStateChanged = true;
            }
            boolean newState = storage.contains(project);
            // if state not changed no need to fire till file badging is not provided
            if (oldState == newState) {
                badgeStateChanged = false;
            }
        }
        if (badgeStateChanged) {
            fireBadgeChanged(project);
        }
    }

    public void removeAllProjects(){
        boolean badgeStateChanged = false;
        synchronized (listLock){
            badgeStateChanged = !storage.isEmpty();
            storage.clear();
        }
        if (badgeStateChanged) {
            fireBadgeChanged();
        }
    }

    public void removeProject(CsmProject project){
        boolean badgeStateChanged = false;
        synchronized (listLock){
            badgeStateChanged = storage.contains(project);
            storage.remove(project);
        }
        if (badgeStateChanged) {
            fireBadgeChanged(project);
        }
    }

    boolean isBroken(NativeProject project) {
        synchronized (listLock){
            return storage.contains(project);
        }
    }

    public Set<CsmUID<CsmFile>> getFailedFiles(NativeProject nativeProject) {
        synchronized (listLock) {
            return new HashSet<>(storage.getFiles(nativeProject));
        }
    }

    public Set<CsmUID<CsmFile>> getFailedFiles(CsmProject csmProject) {
        synchronized (listLock) {
            return new HashSet<>(storage.getFiles(csmProject));
        }
    }

    public boolean hasFailedFiles(NativeProject nativeProject) {
        synchronized (listLock){
            return storage.contains(nativeProject);
        }
    }

    public boolean hasFailedFiles(CsmProject csmProject) {
        synchronized (listLock){
            return storage.contains(csmProject);
        }
    }

    private static class Storage {

        private final Map<CsmProject,Set<CsmUID<CsmFile>>> wrongFiles = new HashMap<>();
        private final Map<CsmProject,NativeProject> nativeProjects = new HashMap<>();

        public Set<CsmUID<CsmFile>> getFiles(CsmProject project){
            return wrongFiles.get(project);
        }

        public Set<CsmUID<CsmFile>> getFiles(NativeProject project) {
            for(Map.Entry<CsmProject,NativeProject> entry : nativeProjects.entrySet()){
                if (project == entry.getValue()){
                    return getFiles(entry.getKey());
                }
            }
            return Collections.<CsmUID<CsmFile>>emptySet();
        }

        public boolean isEmpty(){
            return nativeProjects.isEmpty();
        }

        public void clear(){
            wrongFiles.clear();
            nativeProjects.clear();
        }

        public void remove(CsmProject project){
            wrongFiles.remove(project);
            nativeProjects.remove(project);
        }

        public boolean remove(CsmFile file) {
            CsmProject project = file.getProject();
            if (project != null) {
                Set<CsmUID<CsmFile>> set = getFiles(project);
                if (set != null && set.size()>0){
                    return set.remove(UIDs.get(file));
                }
            }
            return false;
        }

        public boolean add(CsmFile file) {
            CsmProject project = file.getProject();
            if (project != null) {
                Set<CsmUID<CsmFile>> set = getFiles(project);
                if (set == null) {
                    Object id = project.getPlatformProject();
                    if (id instanceof NativeProject) {
                        nativeProjects.put(project, (NativeProject) id);
                    }
                    set = new HashSet<>();
                    wrongFiles.put(project,set);
                }
                return set.add(UIDs.get(file));
            }
            return false;
        }

        public boolean contains(CsmProject project){
            Set<CsmUID<CsmFile>> set = getFiles(project);
            return set != null && set.size() > 0;
        }

        public boolean contains(NativeProject project){
            Set<CsmUID<CsmFile>> set = getFiles(project);
            return set != null && set.size() > 0;
        }
    }
}
