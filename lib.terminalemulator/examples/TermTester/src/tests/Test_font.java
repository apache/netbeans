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
public class Test_font extends Test {

    public Test_font(Context context) {
        super("font", context, 0, 0, false, Util.FillPattern.NONE);
    }

    private void put(String font, char cset) {
        context.send("%s:\n\r", font);
        context.send("\\ESC(%c", cset);
        int max = 255;
        max = 128;
        for (char c = 0; c < max; c++) {
            if (!Character.isISOControl(c))
                if (c == '\\')
                    context.send("\\\\");
                else
                    context.send("%c", c);
        }
        context.send("\n\r");
    }
    public void runBasic(String[] args) {
        put("acs", '0');
        put("UK", 'A');
        put("US", 'B');
        put("Dutch", '4');
        put("Finnish", '5');
        put("Finnish", 'C');
        put("French", 'R');
        put("Quebecois", 'Q');
        put("German", 'K');
        put("Italian", 'Y');
        put("Norsk", 'E');
        put("Norsk", '6');
        put("Spanish", 'Z');
        put("Swedish", 'H');
        put("Swedish", '7');
        put("Swiss", '=');
    }
    
}
