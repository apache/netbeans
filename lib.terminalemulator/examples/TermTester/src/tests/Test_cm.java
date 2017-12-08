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
public class Test_cm extends Test {

    public Test_cm(Context context) {
        super("cm", context, 0, 2, true, Util.FillPattern.ROWCOL);
    }

    public void runBasic(String[] args) {
        if (args.length == 0) {
            context.send("\\ESC[H");
        } else if (args.length == 2) {
            context.send("\\ESC[%s;%sH", args[0], args[1]);
        }
    }
    
}
