/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
