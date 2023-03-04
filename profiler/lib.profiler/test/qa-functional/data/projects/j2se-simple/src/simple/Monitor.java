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

/*
 * Monitor.java
 *
 * Created on June 14, 2005, 12:20 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package simple;


/**
 *
 * @author ehucka
 */
public class Monitor {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of Monitor
     */
    public Monitor() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Data data = new Data();
        Consumer c1 = new Consumer(data, 1);
        Producer p1 = new Producer(data, 1);
        p1.start();
        c1.start();

        try {
            Thread.sleep(5000);

            Consumer c2 = new Consumer(data, 2);
            Producer p2 = new Producer(data, 2);
            p2.start();
            c2.start();
            p1.join();
            p2.join();
        } catch (InterruptedException e) {
        }
    }
}
