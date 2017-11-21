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
