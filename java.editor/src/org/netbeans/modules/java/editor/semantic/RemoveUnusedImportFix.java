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
