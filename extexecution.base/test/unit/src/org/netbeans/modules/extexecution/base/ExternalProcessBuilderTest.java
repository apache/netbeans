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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.extexecution.base;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ExternalProcessBuilderTest extends NbTestCase {

    public ExternalProcessBuilderTest(String name) {
        super(name);
    }

    public void testEnvironment() {
        ExternalProcessBuilder creator = new ExternalProcessBuilder("command");
        creator = creator.addEnvironmentVariable("test1", "value1");
        creator = creator.addEnvironmentVariable("test2", "value2");

        Map<String, String> env = new HashMap<String, String>(
                creator.buildEnvironment(Collections.<String, String>emptyMap()));
        assertEquals("value1", env.remove("test1"));
        assertEquals("value2", env.remove("test2"));
        assertTrue(env.isEmpty());
    }

    public void testPath() {
        ExternalProcessBuilder creator = new ExternalProcessBuilder("command");
        Map<String, String> original = new HashMap<String, String>();
        original.put("PATH", "original");

        // original path
        Map<String, String> env = new HashMap<String, String>(
                creator.buildEnvironment(original));
        assertEquals("original", env.remove("PATH"));
        assertTrue(env.isEmpty());

        // some added path
        File addedPath = new File("addedPath");
        creator = creator.prependPath(addedPath);
        env = new HashMap<String, String>(creator.buildEnvironment(original));
        assertEquals(addedPath.getAbsolutePath().replace(" ", "\\ ") + File.pathSeparator + "original", env.remove("PATH"));
        assertTrue(env.isEmpty());

        // yet another path
        File nextPath = new File("nextPath");
        creator = creator.prependPath(nextPath);
        env = new HashMap<String, String>(creator.buildEnvironment(original));
        assertEquals(
                nextPath.getAbsolutePath().replace(" ", "\\ ") + File.pathSeparator
                + addedPath.getAbsolutePath().replace(" ", "\\ ") + File.pathSeparator
                + "original", env.remove("PATH"));
        assertTrue(env.isEmpty());
    }

    public void testImmutability() throws IOException {
        ExternalProcessBuilder builder = new ExternalProcessBuilder("ls");

        assertNotSame(builder, builder.addArgument("test"));
        assertNotSame(builder, builder.addEnvironmentVariable("test", "test"));
        assertNotSame(builder, builder.prependPath(getWorkDir()));
        assertNotSame(builder, builder.redirectErrorStream(true));
        assertNotSame(builder, builder.workingDirectory(getWorkDir()));
    }
}
