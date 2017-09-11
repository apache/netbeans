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

/** Options that work with additonal arguments.
 *
 * @author Jaroslav Tulach
 */
public class AdditionalArgumentOptsTest extends TestCase {
    private CommandLine l;
    private AddArgsProc proc = new AddArgsProc();
    private Option open;
    private Option close;
    private AddArgsProc closeProc = new AddArgsProc();

    public AdditionalArgumentOptsTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Provider.clearAll();
        open = Option.additionalArguments((char)-1, "open");
        Provider.add(proc, open);
        close = Option.additionalArguments('C', "close");
        Provider.add(closeProc, close);
        
        l = CommandLine.getDefault();
    }

    
    public void testCanHypenBeAdditionalArg() throws Exception {
        l.process(new String[] { "--open", "-", "+", "*", "/" });

        assertNotNull("Processor called", proc.option);
        assertNotNull("args provided", proc.values);
        assertEquals("Four ", 4, proc.values.length);
        assertEquals("-", proc.values[0]);
        assertEquals("+", proc.values[1]);
        assertEquals("*", proc.values[2]);
        assertEquals("/", proc.values[3]);
    }
    
    public void testOpenCannotHaveAnArgument() throws Exception {
        try {
            l.process(new String[] { "--open=ahoj", "1", "2", "3" });
            fail("Open cannot have an argument");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("Processor not called", proc.option);
        assertNull("No args provided", proc.values);
    }
    
    public void testOptionsPassedToOpen() throws Exception {
        l.process(new String[] { "1", "--open", "2", "3" });
        assertEquals("Processor found for long name", open, proc.option);
        assertEquals("Three files provided", 3, proc.values.length);
        assertEquals("first", "1", proc.values[0]);
        assertEquals("second", "2", proc.values[1]);
        assertEquals("third", "3", proc.values[2]);


        proc.option = null;
        proc.values = null;
        l.process(new String[] { "1", "--open", "2", "3" });
        assertEquals("Processor found for long name", open, proc.option);
        assertEquals("Three files provided", 3, proc.values.length);
        assertEquals("first", "1", proc.values[0]);
        assertEquals("second", "2", proc.values[1]);
        assertEquals("third", "3", proc.values[2]);
    }
    public void testCannotHaveTwoAdditionalOptionUsedAtOnce() throws Exception {
        try {
            l.process(new String[] { "--open", "ahoj", "--close" });
            fail("open & close cannot be used at once");
        } catch (CommandException ex) {
            // ok
        }
            
        assertNull("No processor called1", proc.option);
        assertNull("No processor called2", closeProc.option);
        assertNull("Value is unset", proc.values);
    }
    
    public void testProcessingStopsAtDashDash() throws Exception {
        l.process(new String[] { "--ope", "1", "--", "--close", "2" });
        assertEquals("Processor found for abbrevated name", open, proc.option);
        assertNull("No close called", closeProc.option);
        assertEquals("three options provided", 3, proc.values.length);
        assertEquals("first", "1", proc.values[0]);
        assertEquals("second", "--close", proc.values[1]);
        assertEquals("third", "2", proc.values[2]);
    }
    public void testShortOptionWorksAsWell() throws Exception {
        l.process(new String[] { "-C", "1", "--", "--open", "2" });
        assertEquals("Close Processor found for abbrevated name", close, closeProc.option);
        assertNull("No open called", proc.option);
        assertEquals("three options provided", 3, closeProc.values.length);
        assertEquals("first", "1", closeProc.values[0]);
        assertEquals("second", "--open", closeProc.values[1]);
        assertEquals("third", "2", closeProc.values[2]);
    }
    
    static final class AddArgsProc implements Processor {
        Option option;
        String[] values;

        public void process(Option o, Env env, String[] args) throws CommandException {
        }

        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            
            assertEquals("One value", 1, values.size());
            
            option = values.keySet().iterator().next();
            this.values = values.values().iterator().next();

            assertNotNull("An option is provided", option);
            assertNotNull("An array of args is provided", this.values);
        }
    }
}
