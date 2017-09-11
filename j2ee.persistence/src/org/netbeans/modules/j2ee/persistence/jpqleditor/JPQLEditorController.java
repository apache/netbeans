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
 * specific language governing permissions and limitations under the1
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.jpqleditor;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.modules.j2ee.persistence.jpqleditor.ui.JPQLEditorTopComponent;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * JPQL Editor controller. Controls overall JPQL query execution.
 */
public class JPQLEditorController {

    private static final Logger logger = Logger.getLogger(JPQLEditorController.class.getName());
    private JPQLEditorTopComponent editorTopComponent = null;

    private enum AnnotationAccessType {

        FIELD_TYPE,
        METHOD_TYPE;
    };

    public void executeJPQLQuery(final String jpql,
            final PersistenceUnit pu,
            final PersistenceEnvironment pe,
            final int maxRowCount,
            final ProgressHandle ph) {
        final List<URL> localResourcesURLList = new ArrayList<URL>();

        //
        final HashMap<String, String> props = new HashMap<String, String>();
        final List<String> initialProblems = new ArrayList<String>();
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
        if (containerManaged && provider!=null) {
            Utils.substitutePersistenceProperties(pe, pu, dbconn, props);
        }
        final ClassLoader defClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ph.progress(10);
            ph.setDisplayName(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryExecutionPrepare"));
            // Construct custom classpath here.
            initialProblems.addAll(Utils.collectClassPathURLs(pe, pu, dbconn, localResourcesURLList));

            ClassLoader customClassLoader = pe.getProjectClassLoader(
                    localResourcesURLList.toArray(new URL[]{}));
            Thread.currentThread().setContextClassLoader(customClassLoader);
            Thread t = new Thread() {
                @Override
                public void run() {
                    ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
                    JPQLResult jpqlResult = new JPQLResult();
                    if (initialProblems.isEmpty()) {
                        JPQLExecutor queryExecutor = new JPQLExecutor();
                        try {
                            // Parse POJOs from JPQL
                            // Check and if required compile POJO files mentioned in JPQL

                            ph.progress(50);
                            ph.setDisplayName(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryExecutionPassControlToProvider"));
                            jpqlResult = queryExecutor.execute(jpql, pu, pe, props, provider, maxRowCount, ph, true);
                            ph.progress(80);
                            ph.setDisplayName(NbBundle.getMessage(JPQLEditorTopComponent.class, "queryExecutionProcessResults"));

                        } catch (Exception e) {
                            logger.log(Level.INFO, "Problem in executing JPQL", e);
                            jpqlResult.getExceptions().add(e);
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (String txt : initialProblems) {
                            sb.append(txt).append("\n");
                        }
                        jpqlResult.setQueryProblems(sb.toString());
                        jpqlResult.getExceptions().add(new Exception(sb.toString()));
                    }
                    final JPQLResult jpqlResult0 = jpqlResult;
                    final ClassLoader customClassLoader0 = customClassLoader;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            editorTopComponent.setResult(jpqlResult0, customClassLoader0);
                        }
                    });

                    Thread.currentThread().setContextClassLoader(defClassLoader);
                }
            };
            t.setContextClassLoader(customClassLoader);
            t.start();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(defClassLoader);
        }
    }

    public void init(Node[] activatedNodes) {
        editorTopComponent = new JPQLEditorTopComponent(this);
        editorTopComponent.open();
        editorTopComponent.requestActive();
        editorTopComponent.setFocusToEditor();

        editorTopComponent.fillPersistenceConfigurations(activatedNodes);
    }
}
