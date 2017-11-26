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
public class Test_overstrike extends Test {

    public Test_overstrike(Context context) {
        super("overstrike", context, 0, 0, false, Util.FillPattern.NONE);
        info("\n\t\\ESC[4l\tReplace mode\n" +
           "\t\\ESC[4h\tInsert mode");
    }

    public void runBasic(String[] args) {
        context.send("abcdefghijklmnopqrstuvwxyz\n\r");
        context.send("abcdefghijklmnopqrstuvwxyz\n\r");
        context.pause();

        context.send("\\ESC[1;1H");
        context.send("\\ESC[4l");       // replace
        context.send("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        context.pause();

        context.send("\\ESC[2;1H");
        context.send("\\ESC[4h");       // insert
        context.send("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        context.pause();

        context.interp.printf("CR/LF under replace mode\n");
        context.send("\\ESC[5;1H");
        context.send("\\ESC[4l");       // replace
        context.send("hello\\CR\\LFthere");
        context.pause();

        context.interp.printf("TAB under replace mode\n");
        context.send("\\ESC[7;1H");
        context.send("..........................\r");
        context.send("\\ESC[4l");       // replace
        context.send("hello\\HTthere");
        context.pause();

        context.interp.printf("TAB under insert mode\n");
        context.send("\\ESC[9;1H");
        context.send("..........................\r");
        context.send("\\ESC[4h");       // insert
        context.send("hello\\HTthere");
    }
}
