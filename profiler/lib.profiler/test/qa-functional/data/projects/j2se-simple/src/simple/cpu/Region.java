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
 * Region.java
 *
 * Created on November 1, 2005, 4:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package simple.cpu;


/**
 *
 * @author ehucka
 */
public class Region {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of Region */
    public Region() {
        run100();
        run100();
        run100();
        run1000();
        run2000();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Start application: " + System.currentTimeMillis());

        AnotherThread tt = new AnotherThread();
        tt.start();

        Region r = new Region();

        try {
            tt.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        System.out.println("Finish application: " + System.currentTimeMillis());
    }

    public void run100() {
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 100) {
            ;
        }
    }

    public void run1000() {
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < 1000) {
            ;
        }
    }

    public void run2000() {
        for (int i = 0; i < 20; i++) {
            run100();
        }
    }
}
