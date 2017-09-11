/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.history;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author John Baker, Jiri Skrivanek
 */
public class SQLHistoryPersistenceManagerTest extends NbTestCase {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public SQLHistoryPersistenceManagerTest(String testName) {
        super(testName);
    }

    /** Called after every test case. */
    @Override
    public void tearDown() throws IOException {
        clearWorkDir();
    }

    /** Test testExecuteStatements passes if no exceptions occur. */
    public void testExecuteStatements() throws Exception {
        SQLHistoryManager testableManager = new SQLHistoryManager() {

            @Override
            protected FileObject getConfigRoot() {
                try {
                    return FileUtil.toFileObject(getWorkDir());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            protected String getRelativeHistoryPath() {
                return "";
            }
            
        };
        // History does not yet exists as file
        assertNull(testableManager.getHistoryRoot(false));
        testableManager.getSQLHistory().add(new SQLHistoryEntry("jdbc:// mysql", "select * from TRAVEL.PERSON", Calendar.getInstance().getTime()));
        // History does not yet exists as file
        testableManager.save();
        assertNull(testableManager.getHistoryRoot(false));
        testableManager.getSQLHistory().add(new SQLHistoryEntry("jdbc:// oracle", "select * from PERSON", Calendar.getInstance().getTime()));
        final Semaphore s = new Semaphore(0);
        PropertyChangeListener releasingListener =
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (SQLHistoryManager.PROP_SAVED.equals(
                                evt.getPropertyName())) {
                            s.release(); //release semaphore when data are saved
                        }
                    }
                };
        testableManager.addPropertyChangeListener(releasingListener);
        testableManager.save();
        // History does not yet exists as file
        assertNull(testableManager.getHistoryRoot(false));
        // Enforce writing of history
        s.tryAcquire(6, TimeUnit.SECONDS);
        testableManager.removePropertyChangeListener(releasingListener);
        // History file need to exist now!
        assertNotNull(testableManager.getHistoryRoot(false));
        assertTrue(testableManager.getHistoryRoot(false).isData());
    }

    /** Tests parsing of date format. */
    public void testDateParsing() throws Exception {
        final URL u = this.getClass().getResource("sql_history.xml");
        final FileObject fo = FileUtil.toFileObject(new File(u.toURI()));
        SQLHistoryManager testableManager = new SQLHistoryManager() {
            @Override
            protected FileObject getHistoryRoot(boolean create) throws IOException {
                return fo;
            }
        };
        
        List<SQLHistoryEntry> sqlHistoryList = new ArrayList<SQLHistoryEntry>(testableManager.getSQLHistory());
        for (SQLHistoryEntry sqlHistory : sqlHistoryList) {
            assertNotNull(sqlHistory.getDate());
        }
    }
}
