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
package org.netbeans.api.sendopts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Is the environment correctly passed to processors?
 *
 * @author Jaroslav Tulach
 */
public class EnvTest extends TestCase {
    private CommandLine l;
    private OneArgProc proc = new OneArgProc();
    private Option define;
    private Option refine;
    private Option ignore;
    private Option files;
    
    public EnvTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Provider.clearAll();
        
        define = Option.optionalArgument('D', "define");
        refine = Option.requiredArgument('R', "refine");
        ignore = Option.withoutArgument('I', "ignore");
        files = Option.additionalArguments('F', "files");
        
        Provider.add(proc, define, refine, ignore, files);
        
        l = CommandLine.getDefault();
    }
    
    public void testDefaultEnv() throws Exception {
        proc.expIs = System.in;
        proc.expOs = System.out;
        proc.expErr = System.err;
        proc.expDir = new File(System.getProperty("user.dir"));
        
        l.process(new String[] { "-Dx", "-Ry", "-I", "-F", "somefile" });
        assertEquals("one checks", 1, proc.cnt);
        assertEquals("but on four options", 4, proc.values.size());
    }
        
    public void testOwnEnv() throws Exception {
        proc.expIs = new ByteArrayInputStream(new byte[0]);
        proc.expOs = new PrintStream(new ByteArrayOutputStream());
        proc.expErr = new PrintStream(new ByteArrayOutputStream());
        proc.expDir = new File("c:/");
        
        l.process(new String[] { "-Dx", "-Ry", "-I", "-F", "somefile" }, proc.expIs, proc.expOs, proc.expErr, proc.expDir);
        
        assertEquals("one check", 1, proc.cnt);
        assertEquals("but on four options", 4, proc.values.size());
    }
    
    static final class OneArgProc implements Processor {
        InputStream expIs;
        OutputStream expOs;
        OutputStream expErr;
        File expDir;
        int cnt;
        Map<Option,String[]> values;
        
        private void assertEnv(Env env) {
            assertEquals(expIs, env.getInputStream());
            assertEquals(expOs, env.getOutputStream());
            assertEquals(expErr, env.getErrorStream());
            assertEquals(expDir, env.getCurrentDirectory());
            cnt++;
        }

        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertEnv(env);
            this.values = new HashMap<Option,String[]>(values);
        }
    }
}
