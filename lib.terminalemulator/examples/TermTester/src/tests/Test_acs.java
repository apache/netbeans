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
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
