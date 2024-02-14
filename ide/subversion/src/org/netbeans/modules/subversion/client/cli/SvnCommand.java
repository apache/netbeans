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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.CommandlineClient.NotificationHandler;
import org.netbeans.modules.subversion.client.cli.Parser.Line;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.tigris.subversion.svnclientadapter.SVNBaseDir;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Encapsulates a command given to a svn client. 
 * 
 * @author Maros Sandor
 */
public abstract class SvnCommand implements CommandNotificationListener {
               
    private final List<String> cmdError = new ArrayList<String>(10);
       
    /**
     * If the command throws an execption, this is it.
     */
    private Exception thrownException;

    /**
     * True if the command produced errors (messages in error stream), false otherwise.
     */
    private boolean hasFailed;

    /**
     * exit code (return code) of the command - only set it the command execution
     * was not interrupted
     */
    private Integer exitCode;

    /**
     * Internal check mechanism to prevent commands reuse.
     */
    private boolean commandExecuted;
    private Arguments arguments;
    private CommandlineClient.NotificationHandler notificationHandler;
    private File configDir;
    private String username;
    private String password;
    private File tmpFolder;

    protected SvnCommand() {
        arguments = new Arguments();        
    }

    public void setConfigDir(File configDir) {
        this.configDir = configDir;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    NotificationHandler getNotificationHandler() {
        return notificationHandler;
    }

    void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
        if(notifyOutput()) {
            notificationHandler.enableLog();
        } else {
            notificationHandler.disableLog();
        }
    }    
    
    void prepareCommand() throws IOException {
        assert notificationHandler != null;
        prepareCommand(arguments);
        config(configDir, username, password, arguments);        
    }
    
    /**
     * Prepare the command: fill list of arguments to cleartool and compute commandWorkingDirectory.
     * 
     * @param arguments 
     * @throws ClearcaseException
     */
    public abstract void prepareCommand(Arguments arguments) throws IOException;

    protected abstract int getCommand();  

    public void setCommandWorkingDirectory(File... files) {
        notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(files));        
    }
        
    protected boolean hasBinaryOutput() {
        return false;
    }       
    
    protected boolean notifyOutput() {
        return true;
    }
    
    @Override
    public void commandStarted() {
        assert !commandExecuted : "Command re-use is not supported";
        commandExecuted = true;
        String cmdString = toString(arguments, true).toString();
        notificationHandler.logCommandLine(cmdString);        
    }

    @Override
    public void outputText(String lineString) {
        Subversion.LOG.fine("outputText [" + lineString + "]");
        if(!notifyOutput()) {
            return;
        }
        Line line = Parser.getInstance().parse(lineString);
        if(line != null) {
            if(notificationHandler != null && line.getPath() != null) {
                Subversion.LOG.fine("outputText [" + line.getPath() + "]");
                notificationHandler.notifyListenersOfChange(line.getPath());
            }
            notify(line);
            notificationHandler.logMessage(lineString);
        }
    }
    
    public void output(byte[] bytes) {
        
    }

    @Override
    public void errorText(String line) {
        if (line.toLowerCase().contains("killed by signal")) {
            // commandline normal output on linux for ssh connections
            return;
        }
        cmdError.add(line);
        if (isErrorMessage(line)) hasFailed = true;
        notificationHandler.logError(line);
    }

    public void commandCompleted(int exitCode) {
        this.exitCode = Integer.valueOf(exitCode);
    }

    /**
     * Returns exit code of the command.
     * @return  integer having the value of the command's exit code
     *          (return code), or {@code null} if the command was cancelled
     *          or otherwise interrupted
     */
    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public final void commandFinished() {
        File f = getTempCommandFolder(false);
        if (f != null) {
            FileUtils.deleteRecursively(f);
        }
        notificationHandler.logCompleted("");        
    }
    
    public boolean hasFailed() {
        return hasFailed;
    }

    public List<String> getCmdError() {
        return cmdError;
    }

    public void setException(Exception e) {
        thrownException = e;
    }

    public Exception getThrownException() {
        return thrownException;
    }

    protected void notify(Line line) {
        
    }
    
    /**
     * Tests if the given message printed to the error stream indicates an actual command error.
     * Commands sometimes print diagnostic messages to error stream which are not errors and should not be reported as such. 
     * 
     * @param s a message printed to the output stream
     * @return true if the message is an error that should be reported, false otherwise
     */
    protected boolean isErrorMessage(String s) {
        return true;
    }   
            
    public String getStringCommand() {         
        return toString(arguments, false).toString();        
    }

    String[] getCliArguments(String executable) {
        List<String> l = new ArrayList<String>(arguments.size() + 1);
        l.add(executable);
        for (String arg : arguments.toArray()) {
            l.add(arg);
        }
        return l.toArray(new String[0]);
    }        
    
    private static StringBuilder toString(Arguments args, boolean scramble) {
        StringBuilder cmd = new StringBuilder(100);
        boolean psswd = false;
        for (String arg : args) {            
            cmd.append(psswd && scramble ? "******" : arg);
            cmd.append(' ');
            if(scramble) psswd = arg.equals("--password");
        }
        cmd.delete(cmd.length() - 1, cmd.length());
        return cmd;
    }        
        
    protected String createTempCommandFile(String value) throws IOException {
        return createTempCommandFile(new String[] {value});
    }   
    
    protected String createTempCommandFile(File[] files) throws IOException {
        String[] lines = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            lines[i] = files[i].getAbsolutePath();            
            if (files[i].getAbsolutePath().indexOf('@') != -1) {
                lines[i] += '@';
            }
        }
        return createTempCommandFile(lines);
    }
    
    protected String createTempCommandFile(String[] lines) throws IOException {
        File targetFile = Files.createTempFile(getTempCommandFolder(true).toPath(), "svn_", "").toFile();
        targetFile.deleteOnExit();

        PrintWriter writer = null; 
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile))); // NOI18N           
            for (int i = 0; i < lines.length; i++) {
                writer.print(i < lines.length -1 ? lines[i] + "\n" : lines[i]);
            }
        } finally {
            if(writer!=null) {
                writer.flush();
                writer.close();    
            }
        }
        return targetFile.getAbsolutePath();
    }

    protected void config(File configDir, String username, String password, Arguments arguments) {
        arguments.addConfigDir(configDir);
        arguments.add("--non-interactive");
        arguments.addCredentials(username, password);
    }

    /**
     * Solves the problem with malformed URLs (e.g. "xxx[]")
     * Decodes and then encodes given URL
     * @param url url to be encoded
     * @return encoded URL
     */
    protected static SVNUrl encodeUrl(SVNUrl url) {
        try {
            url = SvnUtils.decodeAndEncodeUrl(url);
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, "Url: " + url, ex);
        }
        return url;
    }

    private File getTempCommandFolder (boolean forceCreation) {
        if (tmpFolder == null && forceCreation) {
            tmpFolder = Utils.getTempFolder(true);
        }
        return tmpFolder;
    }
        
    public final class Arguments implements Iterable<String> {

        private final List<String> args = new ArrayList<String>(5);

        public Arguments() {
        }
        
        public void add(String argument) {
            args.add(argument);
        }

        public void add(File... files) {
            for (File file : files) {
                add(file);
            }            
        }
        
        public void add(File file) {
            String absolutePath = file.getAbsolutePath();
            if (absolutePath.indexOf('@') == -1) {
                add(absolutePath);
            } else {
                add(absolutePath + '@');
            }
        }

        public void add(SVNUrl url) {
            if(url != null) {
                add(makeCliUrlString(url, true));
            }
        }

        public void addNonExistent (SVNUrl url) {
            if(url != null) {
                add(makeCliUrlString(url, false));
            }
        }

        public void add(SVNRevision rev1, SVNRevision rev2) {
            add("-r");   
            add( (rev1 == null || rev1.toString().trim().equals("") ? "HEAD" : rev1.toString() ) + 
                 ":" +
                 (rev2 == null || rev2.toString().trim().equals("") ? "HEAD" : rev2.toString() ) ); 
        }
        
        public void add(SVNUrl url, SVNRevision pegging) {
            if(url != null) {
                add(makeCliUrlString(url, pegging));
            }            
        }
        
        public void add(SVNRevision revision) {
            add("-r");   
            add(revision == null || revision.toString().trim().equals("") ? "HEAD" : revision.toString());
        }                    

        public void addPathArguments(String... paths) throws IOException {        
            add("--targets");
            add(createTempCommandFile(paths));
        }
        
        public void addFileArguments(File... files) throws IOException {        
            add("--targets");
            add(createTempCommandFile(files));
        }

        public void addUrlArguments(SVNUrl... urls) throws IOException {        
            String[] paths = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                paths[i] = makeCliUrlString(urls[i], true);
            }
            add("--targets");
            add(createTempCommandFile(paths));
        }

        private String makeCliUrlString(SVNUrl url, boolean appendAtSign) {
            String cliUrlString = encodeUrl(url).toString();
            if (appendAtSign) {
                for (String pathSegment : url.getPathSegments()) {
                    if (pathSegment.indexOf('@') != -1) {
                        cliUrlString += '@';
                        break;
                    }
                }
            }
            return cliUrlString;
        }

        private String makeCliUrlString(SVNUrl url, SVNRevision pegRev) {
            return encodeUrl(url).toString() + '@' + (pegRev == null ? "HEAD" : pegRev);
        }
        
        public void addMessage(String message) throws IOException {
            if(message == null) {
                return;
            }
            add("--force-log");
            add("-F");                
            String msgFile = createTempCommandFile((message != null) ? message : "");
            add(msgFile);                               		
        }
        
        public void addConfigDir(File configDir) {            
            if (configDir != null) {
                arguments.add("--config-dir");
                arguments.add(configDir);
            }
        }         
    
        public void addCredentials(String user, String psswd) {
            if(user == null || user.trim().equals("")) {
                return;
            }            
            add("--username");                               		
            add(user);
            if(psswd == null) {
                psswd = "";
            }
            if (org.openide.util.Utilities.isWindows() && psswd.trim().equals("")) {
                psswd = "\"" + psswd + "\"";
            }
            add("--password");                               		
            add(psswd);                      		
        }
    
        @Override
        public Iterator<String> iterator() {
            return args.iterator();
        }
        
        String[] toArray() {
            return args.toArray(new String[0]);
        }
        
        int size() {
            return args.size();
        }
    }       
                    
}
