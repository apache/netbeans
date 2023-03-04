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
        final List<URL> localResourcesURLList = new ArrayList<>();

        //
        final HashMap<String, String> props = new HashMap<>();
        final List<String> initialProblems = new ArrayList<>();
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
                Mutex.EVENT.readAccess( (Mutex.Action<DatabaseConnection>) () -> {
                    ConnectionManager.getDefault().showConnectionDialog(dbconn);
                    return dbconn;
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
