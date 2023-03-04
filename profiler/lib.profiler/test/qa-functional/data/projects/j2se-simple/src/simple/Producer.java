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
 * Producer.java
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
public class Producer extends Thread {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Data cubbyhole;
    private int number;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public Producer(Data c, int number) {
        cubbyhole = c;
        this.number = number;
        setName("Producer " + number);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void run() {
        while (true) {
            int in = (int) (Math.random() * 500);
            long time = System.currentTimeMillis();

            while ((System.currentTimeMillis() - time) < in) {
                ;
            }

            cubbyhole.put(number, in);

            try {
                sleep(in);
            } catch (InterruptedException e) {
            }
        }
    }
}
