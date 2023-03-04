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

package simple.monitor;


/**
 *
 * @author ehucka
 */
public class CascadeThread extends Thread {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static int MAX_THREADS = 5;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of CascadeThread */
    public CascadeThread() {
        super("Cascade " + MAX_THREADS);
        MAX_THREADS--;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public void run() {
        long time = System.currentTimeMillis();

        while ((System.currentTimeMillis() - time) < 2000) {
            ;
        }

        System.out.println(getName() + " die");

        if (MAX_THREADS > 0) {
            new CascadeThread().start();
        }
    }
}
