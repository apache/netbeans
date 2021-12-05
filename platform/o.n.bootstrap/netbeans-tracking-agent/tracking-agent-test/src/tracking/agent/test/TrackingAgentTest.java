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
package tracking.agent.test;

import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author lahvac
 */
public class TrackingAgentTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Class<?> agent = Class.forName("org.netbeans.agent.TrackingAgent");
        agent.getDeclaredMethod("install").invoke(null);
//        try {
//            System.exit(42);
//        } catch (Throwable t) {
//            t.printStackTrace();
//            System.err.println("caught Throwable");
//        }
//        try {
//            Runtime.getRuntime().halt(42);
//        } catch (Throwable t) {
//            t.printStackTrace();
//            System.err.println("caught Throwable");
//        }
//        try {
//            Runtime.getRuntime().exit(42);
//        } catch (Throwable t) {
//            t.printStackTrace();
//            System.err.println("caught Throwable");
//        }
        new FileOutputStream(new File("/tmp/x2.txt")).close();
        new Window((Window) null);
        System.err.println("finished successfully");
    }
    
}
