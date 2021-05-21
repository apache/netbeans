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

package org.netbeans.modules.debugger.jpda.truffle.testapps;

import java.io.ByteArrayOutputStream;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SLApp {
    public static void main(String... args) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Source src = Source.newBuilder("sl",
            "function main() {\n" +
            "  x = 42;\n" +
            "  println(x);\n" +
            "  return x;\n" +
            "}\n"+
            "function init() {\n"+
            "  obj = new();\n"+
            "  obj.fourtyTwo = main;\n"+
            "  return obj;\n"+
            "}\n",
            "Meaning of world.sl").build();
        
        Context context = Context.newBuilder().allowAllAccess(true).out(os).build();
        Value result = context.eval(src);                           // LBREAKPOINT

        assertEquals("Expected result", 42L, result.asLong());
        assertEquals("Expected output", "42\n", os.toString("UTF-8"));
        
        // dynamic generated interface
        Value init = context.getBindings("sl").getMember("init");
        assertNotNull("init method found", init);
        Compute c = init.execute().as(Compute.class);                           // LBREAKPOINT
        Object result42 = c.fourtyTwo();                                        // LBREAKPOINT
        assertEquals("Expected result", 42L, result42);
        assertEquals("Expected output", "42\n42\n", os.toString("UTF-8"));
    }
    
    public static interface Compute {
        public Number fourtyTwo();
    }
}
