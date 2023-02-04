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

package org.netbeans.installer.infra.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.regex.Pattern;

/**
 *
 * @author Kirill Sorokin
 */
public class Main {

    private static final Pattern PATTERN_ARGS_FILTER_1 = Pattern.compile("[0-9]+");
    private static final Pattern PATTERN_ARGS_FILTER_2 = Pattern.compile("[0-9]+-[0-9]+");

    public static void main(String[] args) {
        for (String arg: args) {
            if (PATTERN_ARGS_FILTER_1.matcher(arg).matches()) {
                occupy(Integer.parseInt(arg));
            }
            
            if (PATTERN_ARGS_FILTER_2.matcher(arg).matches()) {
                int start = Integer.parseInt(arg.substring(0, arg.indexOf("-")));
                int end = Integer.parseInt(arg.substring(arg.indexOf("-") + 1));
                
                for (int port = start; port <= end; port++) {
                    occupy(port);
                }
            }
        }
    }
    
    private static void occupy(final int port) {
        new Thread() {
            public void run() {
                try {
                    System.out.println("occupying: " + port);
                    new ServerSocket(port).accept();
                } catch (IOException e) {
                    System.out.println("    failed: " + port + " (" + e.getMessage() + ")");
                }
            }
        }.start();
    }
}
