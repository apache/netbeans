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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.db.mysql.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides utility for rerouting process output to an output window
 * 
 * @author  ludo, David Van Couvering
 */

import org.netbeans.modules.db.mysql.impl.MySQLDatabaseServer;
import org.openide.windows.InputOutput;

public class ExecSupport {
    private static final Logger LOGGER = Logger.getLogger(ExecSupport.class.getName());

    private OutputCopier[] copyMakers;

    /** Creates a new instance of ExecSupport */
    public ExecSupport() {
    }
    
    /**
     * Redirect the standard output and error streams of the child
     * process to an output window.
     */
    public InputOutput displayProcessOutputs(final Process child)
    throws IOException, InterruptedException {
        // Get a tab on the output window.  If this client has been
        // executed before, the same tab will be returned.
        InputOutput io = MySQLDatabaseServer.getOutput();
        try {
            io.getOut().reset();
        } catch (IOException e) {
            // not a critical error, continue
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
        }

        copyMakers = new OutputCopier[3];
        
        (copyMakers[0] = new OutputCopier(
                new InputStreamReader(child.getInputStream()), 
                io.getOut(), true)).start();
        
        (copyMakers[1] = new OutputCopier(
                new InputStreamReader(child.getErrorStream()), 
                io.getErr(), true)).start();
        
        (copyMakers[2] = new OutputCopier(io.getIn(), 
                new OutputStreamWriter(child.getOutputStream()), 
                true)).start();
        
        // Start a thread that listens to see when the process ends,
        // and then notifies the copyMakers that they are done
        new Thread() {
            @Override
            public void run() {
                try {
                    child.waitFor();
                    Thread.sleep(2000);  // time for copymakers
                } catch (InterruptedException e) {
                } finally {
                    try {
                        copyMakers[0].interrupt();
                        copyMakers[1].interrupt();
                        copyMakers[2].interrupt();
                    } catch (Exception e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                }
            }
        }.start();
        
        return io;
    }
       
    
    
    /** This thread simply reads from given Reader and writes read chars to given Writer. */
    static public  class OutputCopier extends Thread {
        final Writer os;
        final Reader is;
        /** while set to false at streams that writes to the OutputWindow it must be
         * true for a stream that reads from the window.
         */
        final boolean autoflush;
        private boolean done = false;
        
        
        public OutputCopier(Reader is, Writer os, boolean b) {
            this.os = os;
            this.is = is;
            autoflush = b;
        }
                
        /* Makes copy. */
        @Override
        public void run() {
            int read;
            char[] buff = new char [256];
            try {
                while ((read = read(is, buff, 0, 256)) > 0x0) {
                    if (os != null) {
                        os.write(buff,0,read);
                        
                        if (autoflush) {
                            os.flush();
                        }
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, null, e);
            }
        }
        
        @Override
        public void interrupt() {
            super.interrupt();
            done = true;
        }
        
        private int read(Reader is, char[] buff, int start, int count) 
                throws InterruptedException, IOException {
            
            while (!is.ready() && !done) {
                sleep(100);
            }
            
            return is.read(buff, start, count);
        }

    }       
}
