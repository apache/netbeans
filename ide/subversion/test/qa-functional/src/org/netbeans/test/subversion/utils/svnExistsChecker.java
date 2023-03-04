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
package org.netbeans.test.subversion.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author tester
 */
public class svnExistsChecker {

    /**
     * Checks if SVN is installed and if its PATH is properly configured
     * @param printStackTraceIfSVNNotFound - if SVN cannot be executed and this param is true, the exception stack trace is printed
     * @return returns true is SVN is installed and PATH is properly configured (SVN can be run using "svn" from anywhere). Otherwise returns false
     */
    public static boolean check(boolean printStackTraceIfSVNNotFound) {
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        BufferedReader br;
        boolean svnExists = false;

        try {
            proc = rt.exec("svn");
            br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().indexOf("type 'svn help' for usage.") > -1) {
                    svnExists = true;
                    break;
                }
            }

            if (br != null) {
                br.close();
            }

            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().indexOf("type 'svn help' for usage.") > -1) {
                    svnExists = true;
                    break;
                }
            }

            if (br != null) {
                br.close();
            }
            proc.waitFor();
        } catch (Exception e) {
            if (printStackTraceIfSVNNotFound) {
                e.printStackTrace();
            }
            svnExists = false;
        } finally {
            if (proc != null) {
                proc.destroy();
            }
        }
        return svnExists;
    }
}
