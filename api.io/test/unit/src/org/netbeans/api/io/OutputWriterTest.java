/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.api.io;

import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

/**
 *
 * @author jhavlin
 */
public class OutputWriterTest {

    public OutputWriterTest() {
    }

    @Test
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public void testAllMethodsAreDelegatedToSPI() {
        MockServices.setServices(IOProviderTest.MockInputOutputProvider.class);
        try {

            InputOutput io = IOProvider.getDefault().getIO("test1", true);
            OutputWriter ow = io.getOut();
            Lookup lkp = io.getLookup();

            IOProviderTest.CalledMethodList list
                    = lkp.lookup(IOProviderTest.CalledMethodList.class);

            io.show();
            Position p = ow.getCurrentPosition();
            p.scrollTo();
            Fold f = ow.startFold(true);
            f.expand();
            f.collapse();
            ow.endFold(f);

            int order = 0;
            assertEquals("getIO", list.get(order++));
            assertEquals("getOut", list.get(order++));
            assertEquals("getIOLookup", list.get(order++));
            assertEquals("showIO", list.get(order++));
            assertEquals("getCurrentPosition", list.get(order++));
            assertEquals("scrollTo", list.get(order++));
            assertEquals("startFold", list.get(order++));
            assertEquals("setFoldExpanded", list.get(order++));
            assertEquals("setFoldExpanded", list.get(order++));
            assertEquals("endFold", list.get(order++));

            ow.print("Line");
            ow.print(" 1");
            ow.println();

            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                }
            };

            ow.print("Hyperlink ", Hyperlink.from(runnable));
            ow.print(" ");
            ow.print("Color", OutputColor.debug());
            ow.print(" ");
            ow.print("Color link", Hyperlink.from(runnable),
                    OutputColor.debug());
            ow.println();

            ow.println("Line with link", Hyperlink.from(runnable));
            ow.println("Color line", OutputColor.debug());
            ow.println("Color line with link",
                    Hyperlink.from(runnable), OutputColor.debug());

            StringWriter sw = lkp.lookup(StringWriter.class);
            sw.toString();

            String[] lines = sw.toString().split(
                    System.getProperty("line.separator"));

            assertEquals("Line 1", lines[0]);
            assertEquals("<ext link>Hyperlink </ext> <ext color>Color</ext> "
                    + "<ext color link>Color link</ext>", lines[1]);
            assertEquals("<ext link>Line with link</ext>", lines[2]);
            assertEquals("<ext color>Color line</ext>", lines[3]);
            assertEquals("<ext color link>Color line with link</ext>", lines[4]);

        } finally {
            MockServices.setServices();
        }
    }

}
