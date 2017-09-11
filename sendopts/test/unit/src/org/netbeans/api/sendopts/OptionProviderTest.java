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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/** The basic test to check semantics of getopts behaviour.
 *
 * @author Jaroslav Tulach
 */
public class OptionProviderTest extends TestCase {
    private CommandLine l;
    private static Option help;
    private static Option ok;
    
    static {
        help = Option.withoutArgument('h', "help");
        ok = Option.withoutArgument('o', "ok");

        OP.arr = new Option[] { ok, help };
        MockServices.setServices(OP.class);
    }
    
    public OptionProviderTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        
        OP.values = null;
        
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-h" });
        assertEquals("Processor found", true, OP.values.containsKey(help));
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--help" });
        assertEquals("Processor found for long name", true, OP.values.containsKey(help));
    }

    public void testTwoOptionsRecognized() throws Exception {
        l.process(new String[] { "-ho" });
        assertEquals("Processor for help", true, OP.values.containsKey(help));
        assertEquals("Processor for ok", true, OP.values.containsKey(ok));
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name", true, OP.values.containsKey(help));
    }
    

    public void testIncorrectOptionIdentified() throws Exception {
        try {
            l.process(new String[] { "--hell" });
            fail("This option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", OP.values);
    }

    public void testNoProcessorCalledWhenOneOptionIsNotKnown() throws Exception {
        try {
            l.process(new String[] { "-h", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", OP.values);
    }
    
    public static final class OP extends OptionProcessor {
        static Option[] arr;
        static Map<Option, String[]> values;
        
        protected Set<Option> getOptions() {
            return new HashSet<Option>(Arrays.asList(arr));
        }

        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            values = optionValues;
        }
        
    }
}
