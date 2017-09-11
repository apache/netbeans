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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.persistence.wizard.dbscript;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.modules.j2ee.persistence.jpqleditor.Utils;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 */
public final class DBScriptWizard implements WizardDescriptor.ProgressInstantiatingIterator {

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private WizardDescriptor.Panel ejbPanel;
    private WizardDescriptor wiz;
    private static final String EXTENSION = "sql";//NOI18N
    private static final Logger LOGGER = Logger.getLogger(DBScriptWizard.class.getName());

    public static DBScriptWizard create() {
        return new DBScriptWizard();
    }

    @Override
    public String name() {
        return NbBundle.getMessage(DBScriptWizard.class, "LBL_CreateDBScriptWizardTitle");
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
    }

    @Override
    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;

        ejbPanel = new DBScriptPanel.WizardPanel();

        panels = new WizardDescriptor.Panel[]{ejbPanel};


        Wizards.mergeSteps(wiz, panels, new String[]{name()});
    }

    @Override
    public Set instantiate() throws java.io.IOException {
        assert true : "should never be called, instantiate(ProgressHandle) should be called instead";
        return null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        Project project = Templates.getProject(wiz);
        FileObject tFolder = Templates.getTargetFolder(wiz);
        try {
            handle.start(100);
            handle.progress(NbBundle.getMessage(DBScriptWizard.class, "MSG_CreateFile"),5);
            FileObject sqlFile = tFolder.createData(Templates.getTargetName(wiz), EXTENSION);//NOI18N
            PersistenceEnvironment pe = project.getLookup().lookup(PersistenceEnvironment.class);
            if (sqlFile != null) {
                //execution
                run(project, sqlFile, pe, handle, false);
            }
            return Collections.singleton(sqlFile);
        } finally {
            handle.finish();
        }
    }

    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }

    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    static List<String> run(final Project project, final FileObject sFile, final PersistenceEnvironment pe, final ProgressHandle handle, final boolean validateOnly) {
        final List<URL> localResourcesURLList = new ArrayList<URL>();

        //
        final HashMap<String, String> props = new HashMap<String, String>();
        final List<String> initialProblems = new ArrayList<String>();
        PersistenceUnit[] pus = null;
        if(handle!=null) {
            handle.progress(NbBundle.getMessage(DBScriptWizard.class, "MSG_CollectConfig"),10);
        }
        try {
            pus = Util.getPersistenceUnits(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (pus == null || pus.length == 0) {
            initialProblems.add(NbBundle.getMessage(DBScriptWizard.class, "ERR_NoPU"));

            return initialProblems;
        }
        final PersistenceUnit pu = pus[0];
        //connection open
        final DatabaseConnection dbconn = JPAEditorUtil.findDatabaseConnection(pu, pe.getProject());
        if (dbconn != null) {
            if (dbconn.getJDBCConnection() == null) {
                Mutex.EVENT.readAccess(new Mutex.Action<DatabaseConnection>() {
                    @Override
                    public DatabaseConnection run() {
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        return dbconn;
                    }
                });
            }
        }
        //
        final boolean containerManaged = Util.isSupportedJavaEEVersion(pe.getProject());
        final Provider provider = ProviderUtil.getProvider(pu.getProvider(), pe.getProject());
        if (containerManaged && provider != null) {
            Utils.substitutePersistenceProperties(pe, pu, dbconn, props);
        }
        final ClassLoader defClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Construct custom classpath here.
            initialProblems.addAll(Utils.collectClassPathURLs(pe, pu, dbconn, localResourcesURLList));

            ClassLoader customClassLoader = pe.getProjectClassLoader(
                    localResourcesURLList.toArray(new URL[]{}));
            Thread.currentThread().setContextClassLoader(customClassLoader);
            Thread t = new Thread() {
                @Override
                public void run() {
                    if (initialProblems.isEmpty()) {
                        new GenerateScriptExecutor().execute(project, sFile, pe, pu, props, initialProblems, handle, validateOnly);
                    }
                    if (!initialProblems.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (String txt : initialProblems) {
                            sb.append(txt).append("\n");
                        }
                        LOGGER.info(sb.toString());
                    }
                    Thread.currentThread().setContextClassLoader(defClassLoader);
                }
            };
            t.setContextClassLoader(customClassLoader);
            t.start();
            t.join(30000);//I don't want to block forever even if in some cases empty file will be opened
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(defClassLoader);
        }
        return initialProblems;
    }
}
