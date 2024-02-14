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
package org.netbeans.modules.subversion.client.cli;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;

/**
 * Encapsulates svn shell process. 
 * 
 * @author Tomas Stupka
 */
class Commandline {

    private Process           cli;
    private BufferedReader    ctOutput;
    private BufferedReader    ctError;

    private String executable;
    private boolean canceled = false;
    
    /**
     * Creates a new cleartool shell process.
     */
    Commandline() {
        executable = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if(executable == null || executable.trim().equals("")) {
            executable = "svn";                                                 // NOI18N
        } else {
            File f = new File(executable);
            if(f.isDirectory()) {
                executable = executable + "/svn";                               // NOI18N
            } 
        }                      
    }

    /**
     * Forcibly closes the cleartool console, just like using Ctrl-C.
     */
    public void interrupt() {
        try {
            destroy();
        } catch (IOException e) {
            // swallow, we are not interested
        }
    }
   
    private void destroy() throws IOException {
        canceled = true;
        if(cli != null) {
            cli.destroy();
        }        
        Subversion.LOG.fine("cli: Process destroyed");                          // NOI18N
    }

    // why synchronized? with 1.7, all commands go through this method, even those parallelizable ones
    // so it may (and it does) happen that two commands run simultaneously and eventually a wrong output is read (because cli is an instance field)
    // this is a hotfix, cli should probably be turned into a local var, but how would we interrupt the command in that case???
    synchronized void exec(SvnCommand command) throws IOException {
        canceled = false;
        command.prepareCommand();        
        
        String cmd = executable + " " + command.getStringCommand();
        Subversion.LOG.log(Level.FINE, "cli: Executing \"{0}\"", cmd);          // NOI18N
        
        Subversion.LOG.fine("cli: Creating process...");                        // NOI18N
        command.commandStarted();
        try {
            cli = Runtime.getRuntime().exec(command.getCliArguments(executable), getEnvVar());
            if(canceled) return;
            ctError = new BufferedReader(new InputStreamReader(cli.getErrorStream()));

            Subversion.LOG.fine("cli: process created");                        // NOI18N

            String line = null;                
            if(command.hasBinaryOutput()) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                int i = -1;
                if(canceled) return;
                Subversion.LOG.fine("cli: ready for binary OUTPUT \"");         // NOI18N          
                while(!canceled && (i = cli.getInputStream().read()) != -1) {
                    b.write(i);
                }
                if(Subversion.LOG.isLoggable(Level.FINER)) Subversion.LOG.log(Level.FINER, "cli: BIN OUTPUT \"{0}\"", (new String(b.toByteArray()))); // NOI18N
                command.output(b.toByteArray());
            } else {             
                if(canceled) return;
                Subversion.LOG.fine("cli: ready for OUTPUT \"");                // NOI18N     
                ctOutput = new BufferedReader(new InputStreamReader(cli.getInputStream()));
                while (!canceled && (line = ctOutput.readLine()) != null) {                                        
                    Subversion.LOG.log(Level.FINE, "cli: OUTPUT \"{0}\"", line);// NOI18N
                    command.outputText(line);
                }    
            }
            
            while (!canceled && (line = ctError.readLine()) != null) {                                    
                if (!line.isEmpty()) {
                    Subversion.LOG.log(Level.INFO, "cli: ERROR \"{0}\"", line); //NOI18N
                    command.errorText(line);
                }
            }     
            if(canceled) return;
            cli.waitFor();
            command.commandCompleted(cli.exitValue());
        } catch (InterruptedException ie) {
            Subversion.LOG.log(Level.INFO, " command interrupted"); //NOI18N
            Subversion.LOG.log(Level.FINE, " command interrupted: [" + command.getStringCommand() + "]", ie); // should be logged with a lower level, password is printed, too // NOI18N
        } catch (InterruptedIOException ie) {
            Subversion.LOG.log(Level.INFO, " command interrupted"); //NOI18N
            Subversion.LOG.log(Level.FINE, " command interrupted: [" + command.getStringCommand() + "]", ie); // should be logged with a lower level, password is printed, too // NOI18N 
        } catch (Throwable t) {
            if(canceled) {
                Subversion.LOG.fine(t.getMessage());
            } else {
                if(t instanceof IOException) {
                    throw (IOException) t;
                } else {
                    throw new IOException(t);
                }
            }
        } finally {
            if(cli != null) {
                try { cli.getErrorStream().close(); } catch (IOException iOException) { }
                try { cli.getInputStream().close(); } catch (IOException iOException) { }
                try { cli.getOutputStream().close(); } catch (IOException iOException) { }
            }            
            ctError = null;
            ctOutput = null;
            Subversion.LOG.fine("cli: process finnished");                      // NOI18N
            command.commandFinished();
        }        
    }    

    private String[] getEnvVar() {
        Map vars = System.getenv();            
        List<String> ret = new ArrayList<String>(vars.keySet().size());
        for (Iterator it = vars.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();                
            if(key.equals("LC_ALL")) {                                          // NOI18N
                ret.add("LC_ALL=");                                             // NOI18N    
            } else if(key.equals("LC_MESSAGES")) {                              // NOI18N    
                ret.add("LC_MESSAGES=C");                                       // NOI18N
            } else if(key.equals("LC_TIME")) {                                  // NOI18N
                ret.add("LC_TIME=C");                                           // NOI18N    
            } else {
                ret.add(key + "=" + vars.get(key));                             // NOI18N    
            }		                
        }                       
        if(!vars.containsKey("LC_ALL"))      ret.add("LC_ALL=");                // NOI18N    
        if(!vars.containsKey("LC_MESSAGES")) ret.add("LC_MESSAGES=C");          // NOI18N
        if(!vars.containsKey("LC_TIME"))     ret.add("LC_TIME=C");              // NOI18N
        return (String[]) ret.toArray(new String[0]);
    }	    
    
}
