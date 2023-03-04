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

package advanced;

public class DetectDeadlockTest {

    static Object mutex1 = new Object();
    static Object mutex2 = new Object();

    public DetectDeadlockTest() {
    }

    public static void main(String[] args) {
        Thread1 t1 = new Thread1();
        Thread2 t2 = new Thread2();
        t1.start();
        t2.start();
    }

    static class Thread1 extends Thread {

        public void run() {
            synchronized (mutex1) {
                System.out.println("mutex 1");
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {}
                synchronized (mutex2) {
                    try {
                        java.lang.System.out.println("mutex 2 - mutex 1");
                        mutex2.wait(100);
                    } catch (InterruptedException ex) {}
                }
            }
        }
    }

    static class Thread2 extends Thread {

        public void run() {
            synchronized (mutex2) {
                System.out.println("mutex 2");
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {}
                synchronized (mutex1) {
                    System.out.println("mutex 2 - mutex 1");
                }
            }
        }
    }
}