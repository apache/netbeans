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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.extexecution.destroy.ProcessDestroyPerformer;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Hejl
 */
public class ExternalProcessSupportTest extends NbTestCase {

    public ExternalProcessSupportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setInstances(new TestProcessDestroyPerformer());
    }

    public void testDestroy() {
        TestProcess process = new TestProcess();
        Map<String, String> env = new HashMap<String, String>();
        env.put("test1", "value1");
        env.put("test2", "value2");

        ExternalProcessSupport.destroy(process, env);

        assertFalse(process.destroyCalled());

        ProcessDestroyPerformer performer = Lookup.getDefault().lookup(ProcessDestroyPerformer.class);
        assertNotNull(performer);

        TestProcessDestroyPerformer testPerformer = (TestProcessDestroyPerformer) performer;
        assertEquals(process, testPerformer.getProcess());

        Map<String, String> perfEnv = testPerformer.getEnv();
        assertEquals(2, perfEnv.size());

        assertEquals(env.get("test1"), perfEnv.get("test1"));
        assertEquals(env.get("test2"), perfEnv.get("test2"));
    }

    private static class TestProcess extends Process {

        private boolean destroyed;

        public boolean destroyCalled() {
            return destroyed;
        }

        @Override
        public void destroy() {
            this.destroyed = true;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public InputStream getErrorStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getInputStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

    }

    private static class TestProcessDestroyPerformer implements ProcessDestroyPerformer {

        private Process process;

        private Map<String, String> env;

        public void destroy(Process process, Map<String, String> env) {
            this.process = process;
            this.env = env;
        }

        public Process getProcess() {
            return process;
        }

        public Map<String, String> getEnv() {
            return env;
        }
    }
}
