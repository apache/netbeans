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
