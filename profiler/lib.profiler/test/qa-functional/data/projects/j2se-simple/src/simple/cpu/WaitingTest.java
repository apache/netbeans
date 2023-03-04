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

package simple.cpu;

public class WaitingTest {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final Object mutex = new Object();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    long run = 0;
    long sleep = 0;
    long wait = 0;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public WaitingTest() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("Start app: " + System.currentTimeMillis());

        WaitingTest test = new WaitingTest();

        for (int i = 0; i < 2; i++) {
            test.method1000();
            System.out.println("sleep: " + test.sleep);
            System.out.println("wait: " + test.wait);
            System.out.println("run: " + test.run);
        }

        System.out.println("Finish app: " + System.currentTimeMillis());
    }

    public void method1000() {
        sleep = 0;
        run = 0;
        wait = 0;

        long time = System.currentTimeMillis();

        while ((System.currentTimeMillis() - time) < 1000) {
            ;
        }

        run += (System.currentTimeMillis() - time);
        time = System.currentTimeMillis();

        synchronized (mutex) {
            try {
                mutex.wait(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        wait += (System.currentTimeMillis() - time);
        time = System.currentTimeMillis();

        while ((System.currentTimeMillis() - time) < 1000) {
            ;
        }

        run += (System.currentTimeMillis() - time);
        time = System.currentTimeMillis();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }

        sleep += (System.currentTimeMillis() - time);
    }
}
