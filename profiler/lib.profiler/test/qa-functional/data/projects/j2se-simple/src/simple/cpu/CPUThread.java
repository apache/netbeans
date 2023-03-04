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
 * CPUThread.java
 *
 * Created on July 21, 2005, 1:19 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package simple.cpu;


/**
 *
 * @author ehucka
 */
public class CPUThread extends Thread {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of CPUThread */
    public CPUThread() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void run() {
        run512();
        run512();
    }

    public void run512() {
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 512) {
            ;
        }
    }
}
