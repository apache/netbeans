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

package simple;


/**
 *
 * @author
 */
public class CPU {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of CPU
     */
    public CPU() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println("Application (20070815) started: " + System.currentTimeMillis());

            for (int i = 0; i < 5; i++) {
                test1000();
            }

            double[] fibo = new double[100];
            fibo[0] = 0;
            fibo[1] = 1;

            for (int i = 2; i < 100; i++) {
                fibo[i] = fibo[i - 2] + fibo[i - 1];
            }

            System.out.println("Fibonacci:");
            System.out.print("[");

            for (int i = 0; i < 100; i++) {
                System.out.print(fibo[i]);

                if (i < 99) {
                    System.out.print(",");
                }
            }

            System.out.println("]");
            System.out.println("Application finished: " + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void test1000() {
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 1000) {
            ;
        }

        test500();
    }

    private static void test20() {
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 20) {
            ;
        }
    }

    private static void test500() {
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 500) {
            ;
        }

        test20();
    }
}
