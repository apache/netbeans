/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
