/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.api;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author lukas
 */
public final class TestNGSupport {

    private static Lookup.Result<TestNGSupportImplementation> implementations;
    /** Cache of all available TestNGSupportImplementation instances. */
    private static List<TestNGSupportImplementation> cache;

    public static enum Action {
        CREATE_TEST,
        RUN_FAILED,
        RUN_TESTMETHOD,
        RUN_TESTSUITE,
        DEBUG_TEST,
        DEBUG_TESTMETHOD,
        DEBUG_TESTSUITE
    }

    private TestNGSupport() {
    }

    /**
     * Look for instance of TestNGSupportImplementation supporting given project
     * in the default lookup
     *
     * @param p
     * @return TestNGSupportImplementation instance for given project; null if
     *      there's not any
     */
    public static final TestNGSupportImplementation findTestNGSupport(Project p) {
        for (TestNGSupportImplementation s: getInstances()) {
            for (Action a : Action.values()) {
                if (s.isActionSupported(a, p)) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Check if at least one of TestNGSupportImplementation instances
     * registered in the default lookup supports given project
     *
     * @param p project
     *
     * @return true if at least one instance of TestNGSupportImplementation
     *      supporting given project is found, false otherwise
     */
    public static final boolean isActionSupported(Action action, Project p) {
        for (TestNGSupportImplementation s: getInstances()) {
            if (s.isActionSupported(action, p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if at least one of TestNGSupportImplementation instances
     * registered in the default lookup supports given project
     *
     * @param activatedFOs
     *
     * @return true if at least one instance of TestNGSupportImplementation
     *      supporting given project is found, false otherwise
     */
    public static final boolean isSupportEnabled(FileObject[] activatedFOs) {
        for (TestNGSupportImplementation s: getInstances()) {
            if (s.isSupportEnabled(activatedFOs)) {
                return true;
            }
        }
        return false;
    }

    private static synchronized List<TestNGSupportImplementation> getInstances() {
        if (implementations == null) {
            implementations = Lookup.getDefault().lookup(new Lookup.Template<TestNGSupportImplementation>(TestNGSupportImplementation.class));
            implementations.addLookupListener(new LookupListener() {

                public void resultChanged(LookupEvent ev) {
                    synchronized (TestNGSupport.class) {
                        cache = null;
                    }
                }
            });
        }
        if (cache == null) {
            cache = new ArrayList<TestNGSupportImplementation>(implementations.allInstances());
        }
        return cache;
    }
}
