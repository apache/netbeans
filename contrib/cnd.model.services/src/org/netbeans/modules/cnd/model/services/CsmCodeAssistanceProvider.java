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

package org.netbeans.modules.cnd.model.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.project.CodeAssistance;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.project.CodeAssistance.class)
public class CsmCodeAssistanceProvider implements CodeAssistance, CsmProgressListener {
    private static final WeakHashMap<ChangeListener,Boolean> listeners = new WeakHashMap<ChangeListener,Boolean>();
    private final static Object lock = new Object();
    
    public CsmCodeAssistanceProvider() {
        CsmListeners.getDefault().addProgressListener(this);
    }

    @Override
    public boolean hasCodeAssistance(NativeFileItem item) {
        CndUtils.assertNonUiThread();
        CsmFile csmFile = CsmUtilities.getCsmFile(item, false, false);
        return csmFile != null;
    }

    @Override
    public CodeAssistance.State getCodeAssistanceState(NativeFileItem item) {
        CsmFile csmFile = CsmUtilities.getCsmFile(item, false, false);
        if (csmFile != null) {
            if (csmFile.isHeaderFile()) {
                if (CsmIncludeHierarchyResolver.getDefault().getFiles(csmFile).isEmpty()) {
                    return State.ParsedOrphanHeader;
                }
                return State.ParsedIncludedHeader;
            } else {
                return State.ParsedSource;
            }
        }
        return State.NotParsed;
    }

    @Override
    public Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> getHeaderLanguageFlavour(FileObject item) {
        CsmFile csmFile = CsmUtilities.getCsmFile(item, false, false);
        if (csmFile != null) {
            Collection<CsmCompilationUnit> compilationUnits = CsmFileInfoQuery.getDefault().getCompilationUnits(csmFile, 0);
            if (!compilationUnits.isEmpty()) {
                final Iterator<CsmCompilationUnit> iterator = compilationUnits.iterator();
                Set<NativeFileItem.Language> langs = new HashSet<NativeFileItem.Language>();
                Set<NativeFileItem.LanguageFlavor> flavors = new HashSet<NativeFileItem.LanguageFlavor>();
                while(iterator.hasNext()) {
                    CsmCompilationUnit cu = iterator.next();
                    CsmFile startFile = cu.getStartFile();
                    if (startFile != null) {
                        Object platformProject = startFile.getProject().getPlatformProject();
                        if (platformProject instanceof NativeProject) {
                            NativeProject np = (NativeProject) platformProject;
                            NativeFileItem ni = np.findFileItem(startFile.getFileObject());
                            if (ni != null && startFile != csmFile && startFile.isSourceFile()) {
                                langs.add(ni.getLanguage());
                                flavors.add(ni.getLanguageFlavor());
                            }
                        }
                    }
                }
                if (!flavors.isEmpty()) {
                    NativeFileItem.Language prefLang = NativeFileItem.Language.C_HEADER;
                    if (!csmFile.isHeaderFile())  {
                        if (langs.contains(NativeFileItem.Language.C)) {
                            prefLang = NativeFileItem.Language.C;
                        }
                        if (langs.contains(NativeFileItem.Language.CPP)) {
                            prefLang = NativeFileItem.Language.CPP;
                        }
                    }
                    NativeFileItem.LanguageFlavor prefFlavor = maxFlavor(flavors);
                    return Pair.of(prefLang, prefFlavor);
                }
            }
            if (csmFile.isHeaderFile()) {
                return Pair.of(NativeFileItem.Language.C_HEADER, NativeFileItem.LanguageFlavor.UNKNOWN);
            } else if (csmFile.isSourceFile()) {
                return Pair.of(NativeFileItem.Language.CPP, NativeFileItem.LanguageFlavor.UNKNOWN);
            } 
        }
        return Pair.of(NativeFileItem.Language.OTHER, NativeFileItem.LanguageFlavor.UNKNOWN);
    }

    @Override
    public Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> getStartFileLanguageFlavour(NativeFileItem item) {
        CsmFile csmFile = CsmUtilities.getCsmFile(item, false, false);
        if (csmFile != null) {
            Collection<CsmCompilationUnit> compilationUnits = CsmFileInfoQuery.getDefault().getCompilationUnits(csmFile, 0);
            if (!compilationUnits.isEmpty()) {
                final Iterator<CsmCompilationUnit> iterator = compilationUnits.iterator();
                Set<NativeFileItem.Language> langs = new HashSet<NativeFileItem.Language>();
                Set<NativeFileItem.LanguageFlavor> flavors = new HashSet<NativeFileItem.LanguageFlavor>();
                while(iterator.hasNext()) {
                    CsmCompilationUnit cu = iterator.next();
                    CsmFile startFile = cu.getStartFile();
                    if (startFile != null) {
                        Object platformProject = startFile.getProject().getPlatformProject();
                        if (platformProject instanceof NativeProject) {
                            NativeProject np = (NativeProject) platformProject;
                            NativeFileItem ni = np.findFileItem(startFile.getFileObject());
                            if (ni != null && ni != item && startFile.isSourceFile()) {
                                langs.add(ni.getLanguage());
                                flavors.add(ni.getLanguageFlavor());
                            }
                        }
                    }
                }
                if (!flavors.isEmpty()) {
                    NativeFileItem.Language prefLang = NativeFileItem.Language.C_HEADER;
                    if (langs.contains(NativeFileItem.Language.C)) {
                        prefLang = NativeFileItem.Language.C;
                    }
                    if (langs.contains(NativeFileItem.Language.CPP)) {
                        prefLang = NativeFileItem.Language.CPP;
                    }
                    NativeFileItem.LanguageFlavor prefFlavor = maxFlavor(flavors);
                    return Pair.of(prefLang, prefFlavor);
                }
            }
            if (csmFile.isHeaderFile()) {
                return Pair.of(NativeFileItem.Language.C_HEADER, NativeFileItem.LanguageFlavor.UNKNOWN);
            } else if (csmFile.isSourceFile()) {
                return Pair.of(NativeFileItem.Language.CPP, NativeFileItem.LanguageFlavor.UNKNOWN);
            } 
        }
        return Pair.of(NativeFileItem.Language.OTHER, NativeFileItem.LanguageFlavor.UNKNOWN);
    }

    @Override
    public List<NativeFileItem> findHeaderCompilationUnit(NativeFileItem item) {
        List<NativeFileItem> res = new ArrayList<NativeFileItem>();
        CsmFile csmFile = CsmUtilities.getCsmFile(item, false, false);
        if (csmFile != null) {
            CsmFile topParentFile = CsmIncludeResolver.getDefault().getCloseTopParentFile(csmFile);
            if (topParentFile != null) {
                Object platformProject = topParentFile.getProject().getPlatformProject();
                if (platformProject instanceof NativeProject) {
                    NativeProject np = (NativeProject) platformProject;
                    NativeFileItem ni = np.findFileItem(topParentFile.getFileObject());
                    if (ni != null && ni != item && topParentFile.isSourceFile()) {
                        res.add(ni);
                    }
                }
            }
            Collection<CsmCompilationUnit> compilationUnits = CsmFileInfoQuery.getDefault().getCompilationUnits(csmFile, 0);
            if (!compilationUnits.isEmpty()) {
                final Iterator<CsmCompilationUnit> iterator = compilationUnits.iterator();
                while(iterator.hasNext()) {
                    CsmCompilationUnit cu = iterator.next();
                    CsmFile startFile = cu.getStartFile();
                    if (startFile != null) {
                        Object platformProject = startFile.getProject().getPlatformProject();
                        if (platformProject instanceof NativeProject) {
                            NativeProject np = (NativeProject) platformProject;
                            NativeFileItem ni = np.findFileItem(startFile.getFileObject());
                            if (ni != null && ni != item && startFile.isSourceFile()) {
                                if (!res.contains(ni)) {
                                    res.add(ni);
                                }
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    private NativeFileItem.LanguageFlavor maxFlavor(Set<NativeFileItem.LanguageFlavor> flavors) {
        NativeFileItem.LanguageFlavor prefFlavor = NativeFileItem.LanguageFlavor.UNKNOWN;
        if (flavors.contains(NativeFileItem.LanguageFlavor.C)) {
            prefFlavor = NativeFileItem.LanguageFlavor.C;
        }
        if (flavors.contains(NativeFileItem.LanguageFlavor.C89)) {
            prefFlavor = NativeFileItem.LanguageFlavor.C89;
        }
        if (flavors.contains(NativeFileItem.LanguageFlavor.C99)) {
            prefFlavor = NativeFileItem.LanguageFlavor.C99;
        }
        if (flavors.contains(NativeFileItem.LanguageFlavor.C11)) {
            prefFlavor = NativeFileItem.LanguageFlavor.C11;
        }
        if (flavors.contains(NativeFileItem.LanguageFlavor.CPP98)) {
            prefFlavor = NativeFileItem.LanguageFlavor.CPP98;
        }
        if (flavors.contains(NativeFileItem.LanguageFlavor.CPP11)) {
            prefFlavor = NativeFileItem.LanguageFlavor.CPP11;
        }
        if (flavors.contains(NativeFileItem.LanguageFlavor.CPP14)) {
            prefFlavor = NativeFileItem.LanguageFlavor.CPP14;
        }
        if (flavors.contains(NativeFileItem.LanguageFlavor.CPP17)) {
            prefFlavor = NativeFileItem.LanguageFlavor.CPP17;
        }
        return prefFlavor;
    }
    
    @Override
    public void addChangeListener(ChangeListener listener){
        synchronized(lock) {
            listeners.put(listener,Boolean.TRUE);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener listener){
        synchronized(lock) {
            listeners.remove(listener);
        }
    }
    
    @Override
    public void projectParsingStarted(CsmProject project) {
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
        //fireChanges(project);
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void projectLoaded(CsmProject project) {
        fireChanges(project);
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
        fireChanges(file);
    }

    @Override
    public void fileRemoved(CsmFile file) {
        fireChanges(file);
    }

    private void fireChanges(CsmFile file) {
        if (file == null) {
            return;
        }
        FileObject fileObject = file.getFileObject();
        if (fileObject == null) {
            return;
        }
        ChangeEvent changeEvent = new ChangeEvent(fileObject);
        List<ChangeListener> list;
        synchronized (lock) {
            list = new ArrayList<ChangeListener>(listeners.keySet());
        }
        for (ChangeListener listener : list) {
            listener.stateChanged(changeEvent);
        }
    }

    private void fireChanges(CsmProject project) {
        Object platformProject = project.getPlatformProject();
        if (platformProject instanceof NativeProject) {
            ChangeEvent changeEvent = new ChangeEvent(platformProject);
            List<ChangeListener> list;
            synchronized (lock) {
                list = new ArrayList<ChangeListener>(listeners.keySet());
            }
            for (ChangeListener listener : list) {
                listener.stateChanged(changeEvent);
            }
        }
    }

    @Override
    public void parserIdle() {
    }
}
