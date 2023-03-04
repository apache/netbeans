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
package org.netbeans.modules.java.hints.onsave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.spiimpl.batch.Scopes;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class RemoveUnusedAfterSave implements OnSaveTask {

    public static final String KEY_SETTINGS_PREFIX = "on-save.";
    
    private final Context c;
    private final JavaSource javaSource;
    private final Set<String> toRun;
    private final AtomicBoolean cancel = new AtomicBoolean();

    public RemoveUnusedAfterSave(Context c, JavaSource javaSource, Set<String> toRun) {
        this.c = c;
        this.javaSource = javaSource;
        this.toRun = toRun;
    }
    
    @Override
    public void performTask() {
        try {
            List<HintDescription> hints = new ArrayList<HintDescription>();
            for (Entry<HintMetadata, ? extends Collection<? extends HintDescription>> e : RulesManager.getInstance().readHints(null, null, cancel).entrySet()) {
                if (toRun.contains(e.getKey().id)) {
                    hints.addAll(e.getValue());
                }
            }
            FileObject file = javaSource.getFileObjects().iterator().next();
            BatchResult batchResult = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(file)));
            for (ModificationResult mr : BatchUtilities.applyFixes(batchResult, new ProgressHandleWrapper(1), cancel, new ArrayList<RefactoringElementImplementation>(), null, true, new ArrayList<MessageImpl>())) {
                mr.commit();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void runLocked(final Runnable run) {
        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override public void run(CompilationController parameter) throws Exception {
                    run.run();
                }
            }, true);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }
    
    @MimeRegistration(mimeType="text/x-java", service=Factory.class, position=250)
    public static class TaskFactory implements Factory {
        @Override public OnSaveTask createTask(Context context) {
            JavaSource javaSource = JavaSource.forDocument(context.getDocument());
            
            if (javaSource == null) return null;
            
            Set<String> toRun = new HashSet<String>();
            Preferences settings = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(Preferences.class);

            for (Entry<String, Boolean> e : id2DefaultState.entrySet()) {
                if (settings.getBoolean(KEY_SETTINGS_PREFIX + e.getKey(), e.getValue())) toRun.add(e.getKey());
            }
            
            if (toRun.isEmpty()) return null;

            return new RemoveUnusedAfterSave(context, javaSource, toRun);
        }
    }

    private static final Map<String, Boolean> id2DefaultState = new HashMap<String, Boolean>();
    private static final Map<String, Boolean> id2SavedState = new HashMap<String, Boolean>();
    
    static {
        id2DefaultState.put("Imports_UNUSED", false);
        id2DefaultState.put("org.netbeans.modules.java.hints.OrganizeImports", false);
    }
    
    static boolean getValue(Preferences settings, String id) {
        String saved = settings.get(KEY_SETTINGS_PREFIX + id, null);
        if(saved == null) {
            id2SavedState.put(KEY_SETTINGS_PREFIX + id, id2DefaultState.get(id));
            return id2DefaultState.get(id);
        }
        id2SavedState.put(KEY_SETTINGS_PREFIX + id, Boolean.parseBoolean(saved));
        return Boolean.parseBoolean(saved);
    }
    
    static boolean getSavedValue(String id) {
        return id2SavedState.get(id);
    }
    
}
