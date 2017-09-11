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
    
    public void testWithoutAdditonal() throws CommandException {
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
