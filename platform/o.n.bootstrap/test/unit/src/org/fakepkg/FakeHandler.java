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

package org.fakepkg;

import java.io.PrintWriter;
import java.util.Map;
import org.netbeans.CLIHandler;

/**
 *
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.CLIHandler.class)
public class FakeHandler extends CLIHandler {
    public static Runnable toRun;
    public static Map<CLIHandler.Args, Integer> chained;

    /** Creates a new instance of FakeHandler */
    public FakeHandler() {
        super(WHEN_INIT);

        Runnable r = toRun;
        toRun = null;

        if (r != null) {
            r.run();
        }
    }

    protected int cli(CLIHandler.Args args) {
        if (chained != null) {
            Integer i = chained.get(args);
            return i.intValue();
        }
        return 0;
    }

    protected void usage(PrintWriter w) {
    }
    
}
