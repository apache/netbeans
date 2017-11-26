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

import java.util.Date;
import termtester.Context;
import termtester.Test;
import termtester.Util;

/**
 *
 * @author ivan
 */
public class Test_key extends Test {

    public Test_key(Context context) {
        super("key", context, 0, 2, false, Util.FillPattern.NONE);
    }

    public void runBasic(String[] args) {
        context.send("\\ESC[!p");               // soft reset

        if (args.length >= 1) {
            if (args[0].equals("ckm")) {
                if (args.length < 2)
                    context.interp.error("Need 'on' or 'off' after %s", args[0]);
                if (args[1].equals("on")) {
                    context.send("\\ESC[?1h");
                } else {
                    context.send("\\ESC[?1l");
                }

            } else if (args[0].equals("pam")) {
                if (args.length < 2)
                    context.interp.error("Need 'on' or 'off' after %s", args[0]);
                if (args[1].equals("on")) {
                    context.send("\\ESC=");
                } else {
                    context.send("\\ESC>");
                }

            } else if (args[0].equals("pnm")) {
                if (args.length < 2)
                    context.interp.error("Need 'on' or 'off' after %s", args[0]);
                if (args[1].equals("on")) {
                    context.send("\\ESC>");
                } else {
                    context.send("\\ESC=");
                }

            } else if (args[0].equals("mouse")) {
                if (args.length < 2)
                    context.interp.error("Need one of 9, 1000, 1001, 1002, 1003 or 1004 after %s", args[0]);
                if (args.length < 3)
                    context.interp.error("Need 'on' or 'off' after %s", args[1]);
                String code = args[1];
                if (args[2].equals("on")) {
                    context.send("\\ESC[?%sh", code);
                } else {
                    context.send("\\ESC[?%sl", code);
                }

            } else if (args[0].equals("seq")) {
                if (args.length < 2)
                    context.interp.error("Need a sequence after %s", args[0]);
                context.send("%s", args[1]);
            }
        }
        /*
        for (int ax = 0; ax < args.length; ax++)
            context.interp.printf("argv[%d] = %s\n", ax, args[ax]);
        */

        char c;
        Date last = null;
        do {
            c = context.receive();
            if (false) {
                context.interp.printf("received %c %08x %s\n", c, (int) c, Util.charToMnemonic(c));
            } else {
                Date now = new Date();
                if (last == null) {
                    context.interp.printf("received %s", Util.charToMnemonic(c));
                    last = now;
                } else if (now.getTime() - last.getTime() < 100) {
                    context.interp.printf("%s", Util.charToMnemonic(c));
                    last = now;
                } else {
                    context.interp.printf("\nreceived %s", Util.charToMnemonic(c));
                    last = now;
                }
            }

        } while(c != '\r');
        /*
        context.send("\\ESC7"); // save
        context.send("\\ESC[10;10H");
        sleep(1000);
        context.send("\\ESC8"); // restore
        */
    }
    
}
