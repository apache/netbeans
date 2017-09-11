/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fakepkg.FakeHandler;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.RequestProcessor;

/** Tests that handler can set netbeans.mainclass property in its constructor.
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1211
public class CLIHowHardIsToGuessKeyTest extends NbTestCase {
    private static Object LOCK = new Object();
    private Logger LOG;
    
    public CLIHowHardIsToGuessKeyTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        
        System.setProperty("netbeans.mainclass", "org.netbeans.CLIHowHardIsToGuessKeyTest");
        System.setProperty("netbeans.user", getWorkDirPath());
//        System.setProperty("org.netbeans.CLIHandler", "-1");
    }
    
    public static void main(String[] args) throws Exception {
        org.netbeans.MainImpl.finishInitialization();
        synchronized (LOCK) {
            LOCK.notifyAll();
            LOCK.wait();
        }
    }

    public void testGuessTheKey() throws Exception {
        class R implements Runnable {
            public int cnt;
            
            public void run() {
                cnt++;
            }
        }
        
        R run = new R();
        FakeHandler.toRun = run;
        
        class Main implements Runnable {
            Exception ex;
            public void run() {
                try {
                    org.netbeans.MainImpl.main(new String[] { });
                } catch (Exception ex) {
                    this.ex = ex;
                }
            }
        }
        Main main = new Main();
        synchronized (LOCK) {
            RequestProcessor.getDefault().post(main);
            LOCK.wait();
        }
        
        assertEquals("One call", 1, run.cnt);
        
        if (main.ex != null) {
            throw main.ex;
        }
        
        File lock = new File(getWorkDir(), "lock");
        assertTrue("Lock is created", lock.canRead());
        LOG.info("lock file exists" + lock);
        for (int i = 0; i < 500; i++) {
            LOG.info(i + ": testing its size: " + lock.length());
            if (lock.length() >= 14) {
                break;
            }
            Thread.sleep(500);
        }
        assertTrue("Lock must contain the key now: " + lock.length(), lock.length() >= 14);//fail("Ok");
        
        final byte[] arr = new byte[10]; // CLIHandler.KEY_LENGTH
        DataInputStream is = new DataInputStream(new FileInputStream(lock));
        final int port = is.readInt();
        int read = is.read(arr);
        assertEquals("All read", arr.length, read);

        FileOutputStream os = new FileOutputStream(lock);
        os.write(arr);
        os.close();
        
        class Connect implements Runnable {
            int times;
            Exception ex;
            
            public void run() {
                
                while(times++ < 100) {
                    // making the key incorrect
                    arr[5]++;
                    try {
                        Socket s = new Socket(localHostAddress(), port);
                        OutputStream os = s.getOutputStream();
                        os.write(arr);
                        os.flush();
                        int reply = s.getInputStream().read();
                        if (reply == 0) { // CLIHandler.REPLY_FAIL
                            continue;
                        }
                        fail("The reply should be fail: " + reply);
                    } catch (Exception ex) {
                        this.ex = ex;
                        return;
                    }
                }
            }
        }
        
        Connect c = new Connect();
        RequestProcessor.getDefault().post(c).waitFinished(5000);
        
        if (c.ex != null) {
            throw c.ex;
        }
        
        if (c.times > 10) {
            fail("Too many allowed connections, the responce has to be slow to prevent secure attacks: " + c.times);
        }
    }
    static InetAddress localHostAddress () throws Exception {
        java.net.NetworkInterface net = java.net.NetworkInterface.getByName ("lo");
        if (net == null || !net.getInetAddresses().hasMoreElements()) {
            return InetAddress.getLocalHost();
        }
        else {
            return net.getInetAddresses().nextElement();
        }
    }
}
