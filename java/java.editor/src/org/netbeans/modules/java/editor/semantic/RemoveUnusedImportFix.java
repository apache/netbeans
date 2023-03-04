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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class RemoveUnusedImportFix implements Fix  {
    
    public static final String IS_ENABLED_KEY = "Enabled";
    private static final String SEVERITY_KEY = "Severity";
    private static Preferences preferences;
    
    public static RemoveUnusedImportFix create(FileObject file, TreePathHandle importToRemove) {
        return new RemoveUnusedImportFix(file, Collections.singletonList(importToRemove), "FIX_Remove_Unused_Import");
    }
    
    public static RemoveUnusedImportFix create(FileObject file, List<TreePathHandle> importsToRemove) {
        return new RemoveUnusedImportFix(file, importsToRemove, "FIX_All_Remove_Unused_Import");
    }

    private FileObject file;
    private List<TreePathHandle> importsToRemove;
    private String bundleKey;
    
    private RemoveUnusedImportFix(FileObject file, List<TreePathHandle> importsToRemove, String bundleKey) {
        this.file = file;
        this.importsToRemove = importsToRemove;
        this.bundleKey = bundleKey;
    }
    
    public String getText() {
        return NbBundle.getMessage(RemoveUnusedImportFix.class, bundleKey);
    }

    public ChangeInfo implement() {
        JavaSource js = JavaSource.forFileObject(file);

        if (js == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaFixAllImports.class, "MSG_CannotFixImports")); //NOI18N
        } else {
            try {
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy copy) throws Exception {
                        copy.toPhase(Phase.PARSED);

                        CompilationUnitTree nueCUT = copy.getCompilationUnit();

                        for (TreePathHandle handle : importsToRemove) {
                            TreePath tp = handle.resolve(copy);

                            if (tp == null) {
                                //cannot resolve
                                Logger.getLogger(RemoveUnusedImportFix.class.getName()).info("Cannot resolve import to remove."); //NOI18N
                                return ;
                            }

                            nueCUT = copy.getTreeMaker().removeCompUnitImport(nueCUT, (ImportTree) tp.getLeaf());
                        }

                        copy.rewrite(copy.getCompilationUnit(), nueCUT);
                    }
                }).commit();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private static synchronized Preferences getPreferences() {
        if( preferences == null ) {
            preferences = NbPreferences.forModule(RemoveUnusedImportFix.class);
        }
        return preferences;
    }
    
    public static synchronized boolean isEnabled() {
        return getPreferences().getBoolean(IS_ENABLED_KEY, true);
    }
    
    public static void setEnabled( boolean enabled ) {
        getPreferences().putBoolean(IS_ENABLED_KEY, enabled);
    }

    public static Severity getSeverity() {
        int severity = getPreferences().getInt(SEVERITY_KEY, 1);
        for (Entry<Severity, Integer> e : severity2index.entrySet()) {
            if (e.getValue() == severity)
                return e.getKey();
        }
        return Severity.VERIFIER;
    }

    public static void setSeverity(Severity severity) {
        getPreferences().putInt(SEVERITY_KEY, severity2index.get(severity));
    }

    private static Map<Severity,Integer> severity2index;

    static {
        severity2index = new HashMap<Severity, Integer>();
        severity2index.put( Severity.ERROR, 0  );
        severity2index.put( Severity.VERIFIER, 1  );
        severity2index.put( Severity.HINT, 2  );        
    }
}
