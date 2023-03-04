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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 *
 * @author ehucka
 */
public class Memory {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    ArrayList list = new ArrayList();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of Memory */
    public Memory() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void getData() {
        for (int i = 0; i < 100; i++) {
            list.add(new Data());
        }
    }

    public void get1000() {
        long[] l = new long[100];
        list.add(l);
    }

    public void get500() {
        int[] d = new int[1000];
        list.add(d);
    }

    public static void main(String[] args) {
        System.out.println(">>app: start: " + System.currentTimeMillis());

        //wait for profiler
        /*try {
           BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
           br.readLine();
           } catch (Exception ex) {}
           //wait for the first measuring
           try {
               Thread.sleep(3000);
           } catch (Exception e) {}*/
        Memory m = new Memory();
        int cycle = 20;

        while (cycle > 0) {
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }

            m.get1000();
            m.get500();
            m.getData();
            cycle--;
        }

        System.out.println(">>app: end: " + System.currentTimeMillis());
    }
}
