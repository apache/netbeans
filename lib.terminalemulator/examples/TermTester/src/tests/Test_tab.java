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
public class Test_tab extends Test {

    private boolean fwd;

    public Test_tab(Context context) {
        super("tab", context, 0, 0, false, Util.FillPattern.NONE);
    }

    private void line(int row, int start, int n) {
        Util.go(context, row, 0);
        if (row == 2) {
            context.send("    interp->run();");
            Util.go(context, row, 5);
            Util.attr(context, 43);         // bg -> yellow
            Util.attr(context, 4);          // underline

            context.send("A");          // should overwrite 'i'
            if (fwd) {
                // context.send("\\ESC[%dI", n);
                // context.send("\\ESC[%dI", n);
                context.send("\\HT");
                context.send("\\HT");
            } else {
                // context.send("\\ESC[%dZ", n);
                // context.send("\\ESC[%dZ", n);
                context.send("\\HT");
                context.send("\\HT");
            }
            context.send("Z");
        } else {
            for (int cx = 0; cx <= 8*9; cx++) {
                if (cx % 8 == 0)
                    context.send("|");
                else
                    context.send(".");
            }
            Util.go(context, row, start);

            Util.attr(context, 43);         // bg -> yellow
            Util.attr(context, 4);          // underline

            context.send("A");
            if (fwd)
                context.send("\\ESC[%dI", n);
            else
                context.send("\\ESC[%dZ", n);
            context.send("Z");
        }

        Util.attr(context, 0);          // reset
    }

    public void runBasic(String[] args) {
        if (args.length != 1) {
            context.interp.error("usage: test tab fwd|bwd");
        }

        if (args[0].equals("fwd")) {
            fwd = true;
        } else {
            fwd = false;
        }

        for (int tx = 0; tx <= 9; tx++) {
            context.send(String.format("%d", tx));
            context.send("\\HT");
        }

        int row = 2;
        int n = 1;
        line(row++, 8*1+4, n);
        line(row++, 8*1+1, n);
        line(row++, 8*1, n);
        line(row++, 8*1-1, n);
        line(row++, 8*1-2, n);
        line(row++, 1, n);

        n = 2;
        line(row++, 8*3+4, n);
        line(row++, 8*3, n);
        line(row++, 8*3-1, n);
        line(row++, 8*3-2, n);
        line(row++, 8*2, n);

        n = 3;
        line(row++, 8*5+4, n);
        line(row++, 8*5, n);
        line(row++, 8*5-1, n);
        line(row++, 8*5-2, n);
        line(row++, 8*4, n);

        n = 10;
        line(row++, 8*5+4, n);
        line(row++, 8*5, n);
        line(row++, 8*5-1, n);
        line(row++, 8*5-2, n);
        line(row++, 8*4, n);
    }
}
