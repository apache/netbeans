/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.sendopts;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Various types of processor and option closures tested.
 *
 * @author Jaroslav Tulach
 */
public class DefineRefineIgnoreTest extends TestCase {
    private OneArgProc proc = new OneArgProc();
    private Option define;
    private Option refine;
    private Option ignore;
    private Option files;
    
    public DefineRefineIgnoreTest(String s) {
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
    }
    
    public void testDefineRefinePair() throws CommandException {
        Option pair = OptionGroups.allOf(define, refine);
        Provider.add(proc, pair);
            
        CommandLine l = CommandLine.getDefault();
        l.process(new String[] { "--define=1", "--refine", "2" });
        
        assertEquals("V1", "1", proc.clone.get(define)[0]);
        assertEquals("V2", "2", proc.clone.get(refine)[0]);
    }
    
    public void testWithoutAdditional() throws CommandException {
        Option pair = OptionGroups.allOf(ignore, files);
        Provider.add(proc, pair);
            
        CommandLine l = CommandLine.getDefault();
        l.process(new String[] { "--ignore", "--files", "30" });
        
        assertTrue("V1", proc.clone.containsKey(ignore));
        assertEquals("V2", "30", proc.clone.get(files)[0]);
    }
    
    static final class OneArgProc implements Processor {
        Map<Option, String[]> clone;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("No clone yet", clone);
            clone = new HashMap<Option, String[]>(values);
        }
    }
}
