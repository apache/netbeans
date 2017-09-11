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
public class Test_attr extends Test {

    public Test_attr(Context context) {
        super("attr", context, 0, 1, false, Util.FillPattern.NONE);
        info("\\ESC[%sm\tAttributes & Colors");
    }

    @Override
    public void runBasic(String[] args) {

	context.interp.printf("normal mode\n");
	run();
	context.pause();

	context.interp.printf("using reverse mode\n");
	context.sendQuiet("\\ESCc");            // full reset
	context.send("\\ESC[?5h");
	run();
    }

    private void run() {
        context.send("\\ESC[1m");
        context.send("BOLD ");
        context.send("\\ESC[22m");
        context.send("PLAIN ");

        context.send("\\ESC[2m");
        context.send("FAINT ");
        context.send("\\ESC[22m");
        context.send("PLAIN ");

        context.send("\\ESC[3m");
        context.send("ITALIC ");
        context.send("\\ESC[23m");
        context.send("PLAIN ");

        context.send("\\ESC[4m");
        context.send("ULINE ");
        context.send("\\ESC[24m");
        context.send("PLAIN ");

        context.send("\\CR\\LF");

        context.send("\\ESC[5m");
        context.send("BLINK-S ");
        context.send("\\ESC[25m");
        context.send("PLAIN ");

        context.send("\\ESC[6m");
        context.send("BLINK-F ");
        context.send("\\ESC[25m");
        context.send("PLAIN ");

        context.send("\\ESC[7m");
        context.send("INV ");
        context.send("\\ESC[27m");
        context.send("PLAIN ");

        context.send("\\ESC[8m");
        context.send("HIDDEN ");
        context.send("\\ESC[28m");
        context.send("PLAIN ");

        // LATER, I"m using 9 for "active text"
        /*
        context.send("\\ESC[9m");
        context.send("STRIKE ");
        context.send("\\ESC[29m");
        context.send("PLAIN ");
        */

        context.send("\\CR\\LF");

        context.send("\\ESC[1;4;7m");
        context.send("BOLD-ULINE-INV ");
        context.send("\\ESC[22m");
        context.send("ULINE-INV ");
        context.send("\\ESC[24;27m");
        context.send("PLAIN ");

        context.send("\\CR\\LF");

	// primary fg colors
        context.send("\\ESC[30m");
        context.send("BLACK ");
        context.send("\\ESC[31m");
        context.send("RED ");
        context.send("\\ESC[32m");
        context.send("GREEN ");
        context.send("\\ESC[33m");
        context.send("YELLOW ");
        context.send("\\ESC[34m");
        context.send("BLUE ");
        context.send("\\ESC[35m");
        context.send("MAGENTA ");
        context.send("\\ESC[36m");
        context.send("CYAN ");
        context.send("\\ESC[37m");
        context.send("WHITE ");

        context.send("\\ESC[39m");
        context.send("DEFAULT ");
        context.send("\\CR\\LF");

	// primary bg colors
        context.send("\\ESC[40m");
        context.send("BLACK ");
        context.send("\\ESC[41m");
        context.send("RED ");
        context.send("\\ESC[42m");
        context.send("GREEN ");
        context.send("\\ESC[43m");
        context.send("YELLOW ");
        context.send("\\ESC[44m");
        context.send("BLUE ");
        context.send("\\ESC[45m");
        context.send("MAGENTA ");
        context.send("\\ESC[46m");
        context.send("CYAN ");
        context.send("\\ESC[47m");
        context.send("WHITE ");

        context.send("\\ESC[49m");
        context.send("DEFAULT ");
        context.send("\\CR\\LF");

	// bright fg colors
        context.send("\\ESC[90m");
        context.send("black ");
        context.send("\\ESC[91m");
        context.send("red ");
        context.send("\\ESC[92m");
        context.send("green ");
        context.send("\\ESC[93m");
        context.send("yellow ");
        context.send("\\ESC[94m");
        context.send("blue ");
        context.send("\\ESC[95m");
        context.send("magenta ");
        context.send("\\ESC[96m");
        context.send("cyan ");
        context.send("\\ESC[97m");
        context.send("white ");

        context.send("\\ESC[39m");
        context.send("default ");
        context.send("\\CR\\LF");

	// bright bg colors
        context.send("\\ESC[100m");
        context.send("black ");
        context.send("\\ESC[101m");
        context.send("red ");
        context.send("\\ESC[102m");
        context.send("green ");
        context.send("\\ESC[103m");
        context.send("yellow ");
        context.send("\\ESC[104m");
        context.send("blue ");
        context.send("\\ESC[105m");
        context.send("magenta ");
        context.send("\\ESC[106m");
        context.send("cyan ");
        context.send("\\ESC[107m");
        context.send("white ");

        context.send("\\ESC[49m");
        context.send("default ");
        context.send("\\CR\\LF");
    }
    
}
