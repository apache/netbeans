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

package org.netbeans.core.startup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;

/**
 * @author Jesse Glick
 */
public class ConsistencyVerifierTest extends NbTestCase {

    public ConsistencyVerifierTest(String n) {
        super(n);
    }

    private Set<Manifest> modules(String... descs) {
        Set<Manifest> modules = new HashSet<Manifest>();
        for (String desc : descs) {
            Manifest m = new Manifest();
            for (String piece : desc.split("; ")) {
                String[] lhsRhs = piece.split("=");
                assert lhsRhs.length == 2 : "'" + piece + "' in '" + desc + "'";
                m.getMainAttributes().putValue(
                        lhsRhs[0].matches("autoload|eager") ? lhsRhs[0] : lhsRhs[0].length() == 0 ? "OpenIDE-Module" : "OpenIDE-Module-" + lhsRhs[0],
                        lhsRhs[1]);
            }
            modules.add(m);
        }
        return modules;
    }

    private void assertProblems(String problems, String... descs) {
        assertEquals("for " + Arrays.toString(descs),
                problems, ConsistencyVerifier.findInconsistencies(modules(descs), Collections.singleton("placeholder"), false).toString());
    }

    public void testBasicFunctionality() throws Exception {
        assertProblems("{}",
                "=foo",
                "=bar; Module-Dependencies=foo");
        assertProblems("{foo=[module bar]}",
                "=foo; Module-Dependencies=bar");
        assertProblems("{foo=[requires svc]}",
                "=foo; Requires=svc");
        assertProblems("{}",
                "=foo; Requires=svc",
                "=bar; Provides=svc");
        assertProblems("{bar=[requires svc], foo=[requires svc]}",
                "=foo; Requires=svc",
                "=bar; Requires=svc");
        assertProblems("{foo=[module bar > 2.0, requires svc]}",
                "=foo; Requires=svc; Module-Dependencies=bar > 2.0",
                "=bar; Specification-Version=1.5");
    }

    public void testStandardProvides() throws Exception {
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.ModuleFormat1");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.ModuleFormat2");
        assertProblems("{foo=[requires org.openide.modules.ModuleFormat99]}",
                "=foo; Requires=org.openide.modules.ModuleFormat99");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Unix");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.PlainUnix");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Windows");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.MacOSX");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.OS2");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Linux");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Solaris");
        assertProblems("{foo=[requires org.openide.modules.os.Windoze]}",
                "=foo; Requires=org.openide.modules.os.Windoze");
    }

    public void testIAE() throws Exception {
        try {
            ConsistencyVerifier.findInconsistencies(modules("=foo; Whatever=1", "=foo; Whatever=2"), null, false);
            fail();
        } catch (IllegalArgumentException x) {}
        try {
            ConsistencyVerifier.findInconsistencies(modules("Whatever=17"), null, false);
            fail();
        } catch (IllegalArgumentException x) {}
        try {
            ConsistencyVerifier.findInconsistencies(modules("=11"), null, false);
            fail();
        } catch (IllegalArgumentException x) {}
    }

    public void testCheckAutoloadsEnabled() throws Exception {
        assertProblems("{}", "=foo; Module-Dependencies=bar", "=bar; autoload=true");
        assertProblems("{baz=[module is autoload but would not be enabled]}", "=foo; Module-Dependencies=bar", "=bar; autoload=true", "=baz; autoload=true");
        assertProblems("{}", "=compat; autoload=true; Deprecated=true");
        assertProblems("{}", "=placeholder; autoload=true");
    }

}
