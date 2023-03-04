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

package org.netbeans.installer.infra.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Kirill Sorokin
 */
public class Main {
    public static void main(String[] args) {
        for (String arg: args) {
            if (arg.matches("[0-9]+")) {
                occupy(Integer.parseInt(arg));
            }
            
            if (arg.matches("[0-9]+-[0-9]+")) {
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
