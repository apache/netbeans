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
public class Test_txtprm extends Test {

    public Test_txtprm(Context context) {
        super("txtprm", context, 0, 1, false, Util.FillPattern.ROWCOL);
        info("\\ESC]%d:%s\\BEL\tSet Text Parameters\n\t\\ESC]%d:%s\\ESC\\");
    }

    public void runBasic(String[] args) {
        // dtterm & xterm
        context.interp.printf("dtterm & xterm\n");
        context.send("\\ESC]0;iconname+windowtitle\\BEL");
        context.pause();
        context.send("\\ESC]1;iconname\\BEL");
        context.pause();
        context.send("\\ESC]2;windowtitle\\BEL");
        context.pause();
        context.send("\\ESC]3;cwd\\BEL");
        // xterm only
        context.interp.printf("xterm only\n");
        context.send("\\ESC]0;ICONNAME+WINDOWTITLE\\ESC\\\\");
        context.pause();
        context.send("\\ESC]1;ICONNAME\\ESC\\\\");
        context.pause();
        context.send("\\ESC]2;WINDOWTITLE\\ESC\\\\");
        // dtterm only
        context.interp.printf("dtterm only\n");
        context.send("\\ESC]lWINDOWTITLE\\ESC\\\\");
        /* not yet implemented
        context.pause();
        context.send("\\ESC]IICONIMAGEFILE\\ESC\\\\");
         */
        context.pause();
        context.send("\\ESC]LICONNAME\\ESC\\\\");
    }
    
}
