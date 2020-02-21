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

import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemOffilneTestCase;
import org.netbeans.modules.remote.impl.fs.ScheduleRefreshParityTestCase;

/**
 *
 */
public class RemoteApiMultipleTestSuite extends RemoteTestSuiteBase {

    @SuppressWarnings("unchecked")
    public RemoteApiMultipleTestSuite() {
        this("Remote API", getTestClasses());
    }

    @SuppressWarnings("unchecked")
    /*package*/ static Class<? extends NativeExecutionBaseTestCase>[] getTestClasses() {
        Class[] orig = RemoteApiTest.getTestClasses();
        int mul = 4;
        List<Class<? extends NativeExecutionBaseTestCase>> res = new ArrayList<>();
        for (int i = 0; i < mul; i++) {
            for (int j = 0; j < orig.length; j++) {
                if (orig[j] != RemoteFileSystemOffilneTestCase.class && orig[j] != ScheduleRefreshParityTestCase.class) {
                    res.add(orig[j]);
                }
            }
        }
        return res.toArray(new Class[res.size()]);
    }
    
    @SuppressWarnings("unchecked")
    public static RemoteApiMultipleTestSuite createSuite(Class<? extends NativeExecutionBaseTestCase> testClass) {
        return new RemoteApiMultipleTestSuite(testClass.getName(), testClass);
    }

    public RemoteApiMultipleTestSuite(String name, Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(name, "remote.platforms", testClasses);
    }

    public static Test suite() {
        return new RemoteApiMultipleTestSuite();
    }
}
