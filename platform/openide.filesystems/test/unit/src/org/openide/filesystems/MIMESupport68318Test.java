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
package org.openide.filesystems;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/** Simulating stackover flow from issue 68318
 *
 * @author Jaroslav Tulach
 */
public class MIMESupport68318Test extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport68318Test$Lkp");
    }

    public MIMESupport68318Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
//        ErrorManager.getDefault().log("Just initialize the ErrorManager");
    }

    public void testQueryMIMEFromInsideTheLookup() throws IOException {
        Lkp l = (Lkp)Lookup.getDefault();
        {
            MIMEResolver[] result = MIMESupport.getResolvers();
            MIMESupportHid.assertNonDeclarativeResolver("c1 is there", Lkp.c1, result);

            assertNotNull("Result computed", l.result);
            assertEquals("But it has to be empty", 0, l.result.length);
        }
        
        l.result = null;
        l.ic.add(Lkp.c2);
        
        {
            MIMEResolver[] result = MIMESupport.getResolvers();
            MIMESupportHid.assertNonDeclarativeResolver("c1 and c2 are there", new MIMEResolver[] { Lkp.c1, Lkp.c2 }, result);

            assertNotNull("Result in lookup computed", l.result);
            MIMESupportHid.assertNonDeclarativeResolver("And it contains the previous result", Lkp.c1, l.result);
        }
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        static MIMEResolver c1 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            
            public String toString() {
                return "C1";
            }
        };
        static MIMEResolver c2 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            
            public String toString() {
                return "C2";
            }
        };
        private MIMEResolver[] result;
        
        
        public InstanceContent ic;
        public Lkp () {
            this (new InstanceContent ());
        }
        
        private Lkp (InstanceContent ic) {
            super (ic);
            this.ic = ic;
            
            ic.add(c1);
        }

        protected void beforeLookup(org.openide.util.Lookup.Template template) {
            if (template.getType() == MIMEResolver.class) {
                assertNull("First invocation to assign result", result);
                result = MIMESupport.getResolvers();
            }
        }

        
    }
    
}
