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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** The basic test to check semantics of always option.
 *
 * @author Jaroslav Tulach
 */
public class AlwaysOptionTest extends TestCase {
    private CommandLine l;
    private NoArgProc proc = new NoArgProc();
    private Option help;
    private NoArgProc okProc = new NoArgProc();
    private Option ok;
    
    private Always always = new Always();
    private Option all;
    
    public AlwaysOptionTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        
        help = Option.withoutArgument('h', "help");
        Provider.clearAll();
        Provider.add(proc, help);
        ok = Option.withoutArgument('o', "ok");
        Provider.add(okProc, ok);

        all = Option.always();
        Provider.add(always, all);
        
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-h" });
        assertEquals("Processor found", help, proc.option);
        assertEquals("Always", all, always.option);
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--help" });
        assertEquals("Processor found for long name", help, proc.option);
        assertEquals("Always", all, always.option);
    }

    public void testTwoOptionsRecognized() throws Exception {
        l.process(new String[] { "-ho" });
        assertEquals("Processor for help", help, proc.option);
        assertEquals("Processor for ok", ok, okProc.option);
        assertEquals("Always", all, always.option);
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name", help, proc.option);
        assertEquals("Always", all, always.option);
        
        proc.option = null;
        always.option = null;
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name again", help, proc.option);
        assertEquals("Always", all, always.option);
    }
    

    public void testIncorrectOptionIdentified() throws Exception {
        try {
            l.process(new String[] { "--hell" });
            fail("This option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
        assertEquals("Always not called in case of failure", null, always.option);
    }

    public void testNoProcessorCalledWhenOneOptionIsNotKnown() throws Exception {
        try {
            l.process(new String[] { "-h", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
        assertEquals("Always not called in case of failure", null, always.option);
    }
    
    static final class NoArgProc implements Processor {
        Option option;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            assertEquals(1, values.size());
            option = values.keySet().iterator().next();
            assertNotNull("An option is provided", option);

            String[] args = values.get(option);
            assertNotNull("Values is always [0]", args);
            assertEquals("Values is always [0]", 0, args.length);
        }
    }
    static final class Always implements Processor {
        Option option;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            assertEquals(1, values.size());
            option = values.keySet().iterator().next();
            assertNotNull("An option is provided", option);

            String[] args = values.get(option);
            assertNotNull("Values is always [0]", args);
            assertEquals("Values is always [0]", 0, args.length);
        }
    }
}
