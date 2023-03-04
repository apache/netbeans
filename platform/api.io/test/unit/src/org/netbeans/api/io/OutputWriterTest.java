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
