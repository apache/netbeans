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
package org.netbeans.test.git.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tester
 */
public class gitExistsChecker {

    public static boolean check(boolean printStackTraceIfHgNotFound) {
        Runtime rt = Runtime.getRuntime();
        Process proc = null;

        BufferedReader input;

        List<String> list = new ArrayList<String>();

        try {
            proc = rt.exec("git --help");
            String line;

            input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while ((line = input.readLine()) != null) {
                list.add(line);
            }
            input.close();
            
            input = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = input.readLine()) != null) {
                list.add(line);
            }
            input.close();

            int value = proc.waitFor();
            if (value == 255) {
                return false;
            }
            for (String output : list) {
                if (output.indexOf("git help -a") > -1)
                    return true;
            }
            return false;
        } catch (Throwable e) {
            if (printStackTraceIfHgNotFound) {
                e.printStackTrace();
            }
            return false;
        } finally {
            //wait for 5 secs
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            //then destroy process
            if (proc != null) {
                try {
                    proc.getInputStream().close();
                    proc.getOutputStream().close();
                    proc.getErrorStream().close();
                    proc.destroy();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
