/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package test.pkg.not.in.junit;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.junit.internal.NbModuleLogHandler;
import org.openide.util.Lookup;

public class NbModuleSuiteException extends TestCase {

    public NbModuleSuiteException(String t) {
        super(t);
    }

    public void testGenerateMsgOrException() throws IOException {
        boolean ok = false;
        for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
            if (h.getClass().equals(NbModuleLogHandler.class)) {
                ok = true;
                break;
            }
        }
        assertTrue("Our loader found", ok);

        if (Boolean.getBoolean("generate.msg")) {
            Logger.getLogger("my.own.logger").warning("msg");
        }
        if (Boolean.getBoolean("generate.exc")) {
            Logger.getLogger("my.own.logger").log(Level.INFO, "msg", new Exception());
        }
    }
}
