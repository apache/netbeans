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

import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** The basic test to check semantics of getopts behaviour wrt. one optional argument.
 *
 * @author Jaroslav Tulach
 */
public class OptionalArgumentOptsTest extends TestCase {
    private CommandLine l;
    private OneArgProc proc = new OneArgProc();
    private Option define;

    public OptionalArgumentOptsTest(String s) {
        super(s);
   }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Provider.clearAll();
        
        define = Option.optionalArgument('D', "define");
        Provider.add(proc, define);
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-Dahoj" });
        assertEquals("Processor found", define, proc.option);
        assertEquals("Value is correct", "ahoj", proc.value);
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--define=ahoj" });
        assertEquals("Processor found for long name", define, proc.option);
        assertEquals("Value is correct", "ahoj", proc.value);
    }
    public void testLongOptionRecognized2() throws Exception {
        try {
            l.process(new String[] { "--define", "ahoj" });
            fail("ahoj is not used as optional argument, according to getopts");
        } catch (CommandException ex) {
            // ok
        }
            
        assertNull("Processor found for long name", proc.option);
        assertNull("Value is unset", proc.value);
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--def=ahoj" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
    }
    
    public void testAbrevatedNameRecognized2() throws Exception {
        try {
            l.process(new String[] { "--def", "ahoj" });
            fail("Optional argument must follow the option with = sign, cannot be left out");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No Processor called", proc.option);
    }

    public void testAbrevatedNameRecognizedWithoutArg() throws Exception {
        l.process(new String[] { "--def" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
        assertNull("Value is null", proc.value);
    }
    public void testShortNameRecognizedWithoutArg() throws Exception {
        l.process(new String[] { "-D" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
        assertNotNull("Value is null", proc.value);
    }


    public void testIncorrectOptionIdentified() throws Exception {
        try {
            l.process(new String[] { "--hell" });
            fail("This option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
    }

    public void testNoProcessorCalledWhenOneOptionIsNotKnown() throws Exception {
        try {
            l.process(new String[] { "-Dx", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
    }
    
    static final class OneArgProc implements Processor {
        Option option;
        String value;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            assertEquals("One option", 1, values.size());
            option = values.keySet().iterator().next();
            String[] arr = values.values().iterator().next();
            assertNotNull("Never null", arr);
            if (arr.length == 0) {
                value = null;
            } else {
                assertEquals("Exactly one argument", 1, arr.length);
                value = arr[0];
            }
        }
    }
}
