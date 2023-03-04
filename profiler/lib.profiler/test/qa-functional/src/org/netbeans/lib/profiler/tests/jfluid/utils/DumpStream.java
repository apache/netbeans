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

package org.netbeans.lib.profiler.tests.jfluid.utils;

import java.io.*;


public class DumpStream extends Thread {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private BufferedReader reader;
    private PrintStream out;
    private Process process;
    private String prefix;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DumpStream(Process p, InputStream ins, PrintStream out, String prefix) {
        this.process = p;
        this.reader = new BufferedReader(new InputStreamReader(ins));
        this.out = out;
        this.prefix = prefix;

        if (this.prefix == null) {
            this.prefix = "";
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                out.println(prefix + line);

                try {
                    sleep(10);
                } catch (InterruptedException ex) {
                }
            }

            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
