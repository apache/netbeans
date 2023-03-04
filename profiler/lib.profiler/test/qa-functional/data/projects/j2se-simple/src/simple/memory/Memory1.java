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
 * Memory1.java
 *
 * Created on July 25, 2005, 4:16 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package simple.memory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 *
 * @author ehucka
 */
public class Memory1 {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static ArrayList storage2 = new ArrayList();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    ArrayList storage;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of Memory1 */
    public Memory1() {
        storage = new ArrayList();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void add() {
        storage.add(new Bean());
        storage2.add(new Bean());
    }

    public void clear() {
        storage.clear();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(">>app: start: " + System.currentTimeMillis());

        //wait for profiler
        /*try {
           BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
           br.readLine();
           } catch (Exception ex) {}
           //wait for the first measuring
           try {
               Thread.sleep(4000);
           } catch (Exception e) {}*/
        int[] cnts = new int[] { 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1 };
        Memory1 memory = new Memory1();

        for (int i = 0; i < cnts.length; i++) {
            /*try {
               Thread.sleep(200);
               } catch (Exception e) {}*/
            for (int b = 0; b < cnts[i]; b++) {
                memory.add();
            }

            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }

            //memory.clear();
            System.gc();
        }

        System.out.println(">>app: end: " + System.currentTimeMillis());
    }
}
