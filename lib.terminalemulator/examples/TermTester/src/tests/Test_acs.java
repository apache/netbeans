/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package tests;

import termtester.Context;
import termtester.Test;
import termtester.Util;

/**
 *
 * @author ivan
 */
public class Test_acs extends Test {

    private final String graphics_xterm = "+,-.0`afgijklmnopqrstuvwxyz{|}~";
    // "\020\021\030\031\333\004\261\370\361\260\331\277\332\300\305~\304\304\304_\303\264\301\302\263\363\362\343\330\234\376"
    private final String graphics_ansi = "\u0010\u0011\u0018\u0019\u00db\u0004\u00b1\u00f8\u00f1\u00b0\u00d9\u00bf\u00da\u00c0\u00c5~\u00c4\u00c4\u00c4_\u00c3\u00b4\u00c1\u00c2\u00b3\u00f3\u00f2\u00e3\u00d8\u009c\u00fe";

    public Test_acs(Context context) {
        super("acs", context, 0, 0, false, Util.FillPattern.NONE);
	info("Alternate Character Set");
    }

    public void runBasic(String[] args) {
        context.send("\\ESC(B");                // ascii -> G0
        context.send("\\ESC)0");                // graphics -> G1
        context.send("\\ESC*B");                // ascii -> G2
        context.send("\\ESC+0");                // graphics -> G3

        context.send("\\SI");                   // G0 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\0e");                   // \SO: G1 -> GL (default)
        context.send("GRAPH: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESCn");                 // G2 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESCo");                 // G3 -> GL (default)
        context.send("GRAPH: " + graphics_xterm);
        context.send("\n\r");

        //
        context.send("\n\r");

        context.send("\\ESC[!p");               // soft reset

        context.send("\\SI");                   // G0 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\0e");                   // \SO: G1 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESCn");                 // G2 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESCo");                 // G3 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        // 
        context.send("\n\r");

        context.send("\\SI");                   // G0 -> GL (default)
        context.send("\\ESC(B");                // ascii -> G0
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\0e");                   // \SO: G1 -> GL (default)
        context.send("\\ESC)0");                // graphics -> G1
        context.send("GRAPH: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESCn");                 // G2 -> GL (default)
        context.send("\\ESC*B");                // ascii -> G2
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESCo");                 // G3 -> GL (default)
        context.send("\\ESC+0");                // graphics -> G3
        context.send("GRAPH: " + graphics_xterm);
        context.send("\n\r");

        //
        context.send("\n\r");

        context.send("\\ESC[!p");               // soft reset

        context.send("\\ESC(B");                // ascii -> G0
        context.send("\\SI");                   // G0 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESC)0");                // graphics -> G1
        context.send("\\0e");                   // \SO: G1 -> GL (default)
        context.send("GRAPH: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESC*B");                // ascii -> G2
        context.send("\\ESCn");                 // G2 -> GL (default)
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");

        context.send("\\ESC+0");                // graphics -> G3
        context.send("\\ESCo");                 // G3 -> GL (default)
        context.send("GRAPH: " + graphics_xterm);
        context.send("\n\r");

        //
        // \ESC[10m and 11m are only accepted by $TERM="ansi".
        // Furthermore the ACS characters are different, so we use
        // graphics_ansi instead of graphics_xterm.
        // But graphics_ansi doesn't work because it uses 8 bits values
        // which don't travel well through the TermTester -> TermDriver -> Term
        // pipeline yet
        context.send("\n\r");

        context.send("\\ESC[!p");               // soft reset
        context.send("ANSI ONLY:\n\r");

        context.send("\\ESC[11m");              // ~= graphics -> GL
        context.send("!GRAPH:" + graphics_xterm);
        context.send("\n\r");
        context.send("!GRAPH:" + graphics_ansi);
        context.send("\n\r");

        context.send("\\ESC[10m");             // ~= ascii -> GL
        context.send("ASCII: " + graphics_xterm);
        context.send("\n\r");
    }
}
