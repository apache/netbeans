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
package org.netbeans.agent;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.agent.hooks.TrackingHooks;

/**
 *
 * @author lahvac
 */
public class TestExec {
    public static void main(String... args) throws IOException {
        String java = Paths.get(System.getProperty("java.home"))
                           .resolve("bin")
                           .resolve("java")
                           .toAbsolutePath()
                           .toString();
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkExec(List<String> command) {
                System.err.println("check exec: " + command.stream()
                                                           .map(c -> java.equals(c) ? "JAVA" : c)
                                                           .collect(Collectors.joining(", ", "[", "]")));
            }
        }, 0, TrackingHooks.HOOK_EXEC);
        System.err.println("Runtime.exec(String):");
        Runtime.getRuntime().exec(java);
        System.err.println("Runtime.exec(String, String[]):");
        Runtime.getRuntime().exec(java, new String[0]);
        System.err.println("Runtime.exec(String, String[], File):");
        Runtime.getRuntime().exec(java, new String[0], null);
        System.err.println("Runtime.exec(String[]):");
        Runtime.getRuntime().exec(new String[] {java});
        System.err.println("Runtime.exec(String[], String[]):");
        Runtime.getRuntime().exec(new String[] {java}, new String[0]);
        System.err.println("Runtime.exec(String[], String[], File):");
        Runtime.getRuntime().exec(new String[] {java}, new String[0], null);
        System.err.println("ProcessBuilder.start():");
        new ProcessBuilder(java).start();
    }
}
