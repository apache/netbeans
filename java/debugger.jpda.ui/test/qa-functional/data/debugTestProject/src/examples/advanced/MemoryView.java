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

package examples.advanced;

import java.text.MessageFormat;
import java.util.*;

import javax.swing.Timer;

/** Frame to display amount of free memory in the running application.
* <P>
* Handy for use with the IDE's internal execution. Then the statistic
* of free memory in the whole environment is displayed.
*/
public class MemoryView extends Helper {
    /** bundle to use */
    private static ResourceBundle bundle;
    /** message of free memory */
    private static MessageFormat msgMemory;

    /** default update time */
    private static int UPDATE_TIME = 1000;
    /** timer to invoke updating */
    private Timer timer;

    {
        bundle = ResourceBundle.getBundle ("examples.advanced.MemoryViewLocale");
        msgMemory = new MessageFormat (bundle.getString ("TXT_STATUS"));
        timer = null;
    }

    /** Initializes the Form */
    public MemoryView() {
        Class clazz=java.lang.Runtime.class;
        String string="Hi!";
        int n=50;
        List llist=new LinkedList();
        List alist=new ArrayList();
        List vec=new Vector();
        Map hmap=new HashMap();
        Map htab=new Hashtable();
        Map tmap=new TreeMap();
        Set hset=new HashSet();
        Set tset=new TreeSet();
        for (int i=0;i < n; i++) {
            String s=i+". item";
            llist.add(s);
            alist.add(s);
            vec.add(s);
            hset.add(s);
            tset.add(s);
            hmap.put(""+i,s);
            htab.put(""+i,s);
            tmap.put(""+i,s);            
        }
        int[] policko=new int[]{1,2,3,4,5};
        int[] pole=new int[n];
        int[][] d2=new int[10][20];
    }

    public void updateConsumption() {
        for (int i = 0; i < 180; i++) {
            updateStatus ();            
            try {
                Thread.sleep(UPDATE_TIME++);
            }
            catch (java.lang.InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Updates the status of memory consumption */
    private int updateStatus () {
        Runtime r = Runtime.getRuntime ();
        long free = r.freeMemory ();
        long total = r.totalMemory ();

        // when bigger than integer then divide by two
        while (total > Integer.MAX_VALUE) {
            total = total >> 1;
            free = free >> 1;
        }

        int taken = (int) (total - free);

        System.out.println((msgMemory.format (new Object[] {
                                            new Long (total),
                                            new Long (free),
                                            new Integer (taken)
                                        })));
        return taken;
    }


    public static void main(String[] args) {
        MemoryView mv = new MemoryView ();
        mv.inner();
        mv.test();
        mv.updateConsumption();
    }

    public void inner() {
        Thread t=new Thread(new Runnable() {
            public void run() {
                System.out.println("I\'m in anonymous inner class.");
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
        }        
    }

    public String Vpublic = "Public Variable";
    protected String Vprotected = "Protected Variable";
    private String Vprivate = "Private Variable";
    String VpackagePrivate = "Package-private Variable";   
    public static String Spublic = "Public Variable";
    protected static String Sprotected = "Protected Variable";
    private static String Sprivate = "Private Variable";
    static String SpackagePrivate = "Package-private Variable";    
}

class Helper {
    public String inheritedVpublic = "Inherited Public Variable";
    protected String inheritedVprotected = "Inherited Protected Variable";
    private String inheritedVprivate = "Inherited Private Variable";
    String inheritedVpackagePrivate = "Inherited Package-private Variable";   
    public static String inheritedSpublic = "Inherited Public Variable";
    protected static String inheritedSprotected = "Inherited Protected Variable";
    private static String inheritedSprivate = "Inherited Private Variable";
    static String inheritedSpackagePrivate = "Inherited Package-private Variable";   
    
    public void test() {
        System.out.println("I\'m in secondary class");
    }
}
