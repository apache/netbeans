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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
