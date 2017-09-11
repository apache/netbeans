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
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Can we associated short description?
 *
 * @author Jaroslav Tulach
 */
public class ShortDescriptionTest extends TestCase implements Processor {
    private Option help;
    private Option descr;
    
    public ShortDescriptionTest(String s) {
        super(s);
    }

    private void setUpHelp() throws Exception {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        Provider.add(this, help);
    }

    private void setUpShort() {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        descr = Option.shortDescription(help, "org.netbeans.api.sendopts.TestBundle", "HELP");
        assertEquals("Option with description is the same", help, descr);
        assertEquals("Option with description has the same hashCode", help.hashCode(), descr.hashCode());
        Provider.add(this, descr);
    }
    
    public void testPrintedUsage() throws Exception {
        setUpHelp();
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-h.*--help").matcher(w.toString());
        if (!m.find()) {
            fail("-h, --help should be there:\n" + w.toString());
        }

        assertEquals("No help associated", w.toString().indexOf("shorthelp"), -1);
    }
    public void testPrintedUsageEiyhFrdvtipyion() throws Exception {
        setUpShort();
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-h.*--help").matcher(w.toString());
        if (!m.find()) {
            fail("-h, --help should be there:\n" + w.toString());
        }

        if (w.toString().indexOf("shorthelp") == -1) {
            fail("shorthelp associated: " + w.toString());
        }
    }
    public void testProvidedOwnDisplayName() throws Exception {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        Option shor = Option.shortDescription(help, "org.netbeans.api.sendopts.TestBundle", "HELP");
        assertEquals("Option with description is the same", help, shor);
        assertEquals("Option with description has the same hashCode", help.hashCode(), shor.hashCode());
        descr = Option.displayName(shor, "org.netbeans.api.sendopts.TestBundle", "NAMEHELP");
        assertEquals("Option with description is the same", help, descr);
        assertEquals("Option with description has the same hashCode", help.hashCode(), descr.hashCode());
        Provider.add(this, descr);
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-p.*--pomoc").matcher(w.toString());
        if (!m.find()) {
            fail("--pomoc should be there:\n" + w.toString());
        }

        if (w.toString().indexOf("shorthelp") == -1) {
            fail("shorthelp associated: " + w.toString());
        }
    }

    public void process(Env env, Map<Option, String[]> values) throws CommandException {
    }

}

