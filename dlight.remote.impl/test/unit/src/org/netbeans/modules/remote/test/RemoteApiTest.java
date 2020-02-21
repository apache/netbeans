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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.test;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.remote.impl.fs.*;

/**
 *
 */
public class RemoteApiTest extends NativeExecutionBaseTestSuite {

    public static final boolean TRACE_LISTENERS = Boolean.getBoolean("trace.listener.tests"); //NOI18N

    @SuppressWarnings("unchecked")
    public RemoteApiTest() {
        this("Remote API", getTestClasses());
    }

    @SuppressWarnings("unchecked")
    /*package*/ static Class<? extends NativeExecutionBaseTestCase>[] getTestClasses() {
        return new Class[] {
           // some tests moved to RemoteApiLinksAndListenersTest
           AdeMockupTestCase.class,
           RemoteFileSystemTestCase.class,
           CopyTestCase.class,
           RemotePathTestCase.class,
           RemoteURLTestCase.class,
           RenameTestCase.class,
           EscapeWindowsNameTestCase.class,
           CaseSensivityTestCase.class,
           DirectoryStorageTestCase.class,
           DirectoryReaderTestCase.class,
           RefreshTestCase.class,
           FastRefreshTestCase.class,
           RefreshTestCase_IZ_210125.class,
           MoveTestCase.class,
           RefreshNonInstantiatedTestCase.class,
           RefreshDirSyncCountTestCase.class,
           CanonicalTestCase.class,
           CreateDataAndFolderTestCase.class,
           NormalizationTestCase.class,
           ReadOnlyDirTestCase.class,
           ScheduleRefreshParityTestCase.class,
           WritingQueueTestCase.class,
           RemoteFileSystemParallelReadTestCase.class,
           RemoteFileSystemParallelLsTestCase.class,
           RemoteFileSystemOffilneTestCase.class,
           TempFileRelatedExceptionsIZ_258285_testCase.class,
           DeleteOnExitTestCase.class,
           CyclicLinksTestCase.class
        };
    }
    
    @SuppressWarnings("unchecked")
    public static RemoteApiTest createSuite(Class<? extends NativeExecutionBaseTestCase> testClass) {
        return new RemoteApiTest(testClass.getName(), testClass);
    }

    @SuppressWarnings("unchecked")
    public static RemoteApiTest createSuite(Class<? extends NativeExecutionBaseTestCase> testClass, int timesToRepeat) {
        Class[] classes = new Class[timesToRepeat];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = testClass;            
        }
        return new RemoteApiTest(testClass.getName(), classes);
    }
    
    public RemoteApiTest(String name, Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(name, "remote.platforms", testClasses);
    }

    public static Test suite() {
        return new RemoteApiTest();
    }
}
