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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.glassfish.javaee.ide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;


/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans
 *
 * @author  Michal Mocnak
 */
public class Hk2Logger {
    
    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int delay = 1000;
    
    /**
     * Singleton model pattern
     */
    private static Map<String, Hk2Logger> instances = new HashMap<String, Hk2Logger>();
    
    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;
    
    /**
     * Creates and starts a new instance of Hk2Logger
     * 
     * @param uri the uri of the server
     */
    private Hk2Logger(String uri) {
        io = UISupport.getServerIO(uri);
        
        if (io == null) {
            return; // finish, it looks like this server instance has been unregistered
        }
        
        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ioe) {
            // no op
        }
        
        io.select();
    }
    
    /**
     * Returns uri specific instance of Hk2Logger
     * 
     * @param uri the uri of the server
     * @return uri specific instamce of OCHk2Logger
     */
    public static Hk2Logger getInstance(String uri) {
        if (!instances.containsKey(uri))
            instances.put(uri, new Hk2Logger(uri));
        
        return instances.get(uri);
    }
    
    /**
     * Reads a newly included InputSreams
     *
     * @param inputStreams InputStreams to read
     */
    public void readInputStreams(InputStream[] inputStreams) {
        for(InputStream inputStream : inputStreams)
            RequestProcessor.getDefault().post(new LoggerRunnable(inputStream));
    }
    
    /**     
     * Reads a newly included Files
     * 
     * @param files Files to read
     */
    public void readFiles(File[] files) {
        for(InputStream inputStream : getInputStreamsFromFiles(files))
            RequestProcessor.getDefault().post(new LoggerRunnable(inputStream));
    }
    
    /**
     * Writes a message into output
     * 
     * @param s message to write
     */
    public synchronized void write(String s) {
        io.getOut().print(s);
    }
    
    /**
     * Selects output panel
     */
    public synchronized void selectIO() {
        io.select();
    }
    
    private static InputStream[] getInputStreamsFromFiles(File[] files) {
        InputStream[] inputStreams = new InputStream[files.length];
        int i = 0;
        try {
            for(i=0 ; i<files.length ; i++)
                inputStreams[i] = new FileInputStream(files[i]);
        } catch(FileNotFoundException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, files[i].getAbsolutePath(), ex); // NOI18N
            return new InputStream[] {};
        }
        
        return inputStreams;
    }
    
    private class LoggerRunnable implements Runnable {
        
        private InputStream inputStream;
        
        public LoggerRunnable(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        public void run() {
            try {
                // create a reader from the input stream
                InputStreamReader reader = new InputStreamReader(inputStream);
                
                // read from the input stream and put all the changes to the
                // I/O window
                char[] chars = new char[1024];
                while (true) {
                    // while there is something in the stream to be read - read that
                    while (reader.ready()) {
                        write(new String(chars, 0, reader.read(chars)));
                        selectIO();
                    }
                    
                    // when the stream is empty - sleep for a while
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            } finally {
                // close the opened stream
                try {
                    inputStream.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                }
            }
        }
    }
}
