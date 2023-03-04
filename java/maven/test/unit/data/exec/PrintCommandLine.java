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
package test;

public class PrintCommandLine {
    public static void main(String[] args) {
        System.err.println("::PrintCommandLineStart");
        System.err.print("argCount="); System.err.println(args.length);
        
        int index = 1;
        for (String s : args) {
            System.err.print("appArg." + index + "="); System.err.println(s);
            index++;
        }
        
        for (String k : System.getProperties().stringPropertyNames()) {
            if (k.startsWith("test.")) {
                System.err.print(k + "="); System.err.println(System.getProperty(k));
            }
        }
        System.err.println("::PrintCommandLineEnd");
    }
}
