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
package org.netbeans.modules.subversion.remote.client.cli;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNBaseDir;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.CommandlineClient.NotificationHandler;
import org.netbeans.modules.subversion.remote.client.cli.Parser.Line;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 * Encapsulates a command given to a svn client. 
 * 
 * 
 */
public abstract class SvnCommand implements CommandNotificationListener {
    
    private static final boolean EXPAND_TARGETS_OPTION = true;
               
    private final List<String> cmdError = new ArrayList<>(10);
       
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
    private final Arguments arguments;
    private CommandlineClient.NotificationHandler notificationHandler;
    private VCSFileProxy configDir;
    private String username;
    private String password;
    private VCSFileProxy tmpFolder;
    private final FileSystem fileSystem;

    protected SvnCommand(FileSystem fs) {
        this.fileSystem = fs;
        arguments = new Arguments();        
    }

    public void setConfigDir(VCSFileProxy configDir) {
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

    protected abstract ISVNNotifyListener.Command getCommand();  

    public void setCommandWorkingDirectory(VCSFileProxy... files) {
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
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.fine("outputText [" + lineString + "]");
        }
        if(!notifyOutput()) {
            return;
        }
        Line line = Parser.getInstance().parse(lineString);
        if(line != null) {
            if(notificationHandler != null && line.getPath() != null) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.fine("outputText [" + line.getPath() + "]");
                }
                String path = getAbsolutePath(line.getPath());
                if (!path.startsWith("/")) { //NOI18N
                    path = "/"+path; //NOI18N
                }
                notificationHandler.notifyListenersOfChange(VCSFileProxySupport.getResource(fileSystem, path));
            }
            notify(line);
            notificationHandler.logMessage(lineString);
        }
    }
    
    public String getAbsolutePath(String path) {
        return path;
    }
    
    protected final  String getAbsolutePath(String path, VCSFileProxy... files) {
        if (!path.startsWith("/")) { //NOI18N
            for(VCSFileProxy f : files){
                if (f.getPath().endsWith("/"+path)) { //NOI18N
                    return f.getPath();
                }
            }
        }
        return path;
    }
    
    public void output(byte[] bytes) {
        
    }

    @Override
    public void errorText(String line) {
        if (line.toLowerCase(Locale.ENGLISH).contains("killed by signal")) { //NOI18N
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
        try {
            VCSFileProxy f = getTempCommandFolder(false);
            if (f != null) {
                SvnUtils.deleteRecursively(f);
            }
            notificationHandler.logCompleted("");
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
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

    String[] getCliArguments() {
        List<String> l = new ArrayList<>(arguments.size());
        for (String arg : arguments.toArray()) {
            l.add(arg);
        }
        return l.toArray(new String[l.size()]);
    }        
    
    private static StringBuilder toString(Arguments args, boolean scramble) {
        StringBuilder cmd = new StringBuilder(100);
        boolean psswd = false;
        for (String arg : args) {            
            cmd.append(psswd && scramble ? "******" : arg); //NOI18N
            cmd.append(' ');
            if(scramble) psswd = arg.equals("--password"); //NOI18N
        }
        cmd.delete(cmd.length() - 1, cmd.length());
        return cmd;
    }        
        
    protected String createTempCommandFile(String value) throws IOException {
        return createTempCommandFile(new String[] {value});
    }   
    
    protected String createTempCommandFile(VCSFileProxy[] files) throws IOException {
        String[] lines = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            lines[i] = files[i].getPath();            
            if (files[i].getPath().indexOf('@') != -1) {
                lines[i] += '@';
            }
        }
        return createTempCommandFile(lines);
    }
    
    protected String createTempCommandFile(String[] lines) throws IOException {
        VCSFileProxy targetFile = VCSFileProxySupport.createTempFile(getTempCommandFolder(true), "svn_", "", true); //NOI18N

        PrintWriter writer = null; 
        try {
            writer = new PrintWriter(new OutputStreamWriter(VCSFileProxySupport.getOutputStream(targetFile), "UTF-8"));
            for (int i = 0; i < lines.length; i++) {
                writer.print(i < lines.length -1 ? lines[i] + "\n" : lines[i]); //NOI18N
            }
        } finally {
            if(writer!=null) {
                writer.flush();
                writer.close();    
            }
        }
        return targetFile.getPath();
    }

    protected void config(VCSFileProxy configDir, String username, String password, Arguments arguments) {
        arguments.addConfigDir(configDir);
        arguments.add("--non-interactive"); //NOI18N
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

    private VCSFileProxy getTempCommandFolder (boolean forceCreation) throws IOException {
        if (tmpFolder == null && forceCreation) {
            tmpFolder = VCSFileProxySupport.getTempFolder(VCSFileProxy.createFileProxy(fileSystem.getRoot()), true);
        }
        return tmpFolder;
    }
        
    public final class Arguments implements Iterable<String> {

        private final List<String> args = new ArrayList<>(5);

        public Arguments() {
        }
        
        public void add(String argument) {
            args.add(argument);
        }

        public void add(VCSFileProxy ... files) {
            for (VCSFileProxy file : files) {
                add(file);
            }            
        }
        
        public void add(VCSFileProxy file) {
            String absolutePath = file.getPath();
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
            add("-r"); //NOI18N
            add( (rev1 == null || rev1.toString().trim().equals("") ? "HEAD" : rev1.toString() ) + //NOI18N
                 ":" + //NOI18N
                 (rev2 == null || rev2.toString().trim().equals("") ? "HEAD" : rev2.toString() ) ); //NOI18N
        }
        
        public void add(SVNUrl url, SVNRevision pegging) {
            if(url != null) {
                add(makeCliUrlString(url, pegging));
            }            
        }
        
        public void add(SVNRevision revision) {
            add("-r"); //NOI18N
            add(revision == null || revision.toString().trim().equals("") ? "HEAD" : revision.toString()); //NOI18N
        }                    

        public void addPathArguments(String... paths) throws IOException {        
            if (EXPAND_TARGETS_OPTION && paths.length > 0 && paths.length < 500) {
                for(String path : paths) {
                    add("'"+path+"'"); //NOI18N
                }
            } else {
                add("--targets"); //NOI18N
                add(createTempCommandFile(paths));
            }
        }
        
        public void addFileArguments(VCSFileProxy... files) throws IOException {        
            if (EXPAND_TARGETS_OPTION && files.length > 0 && files.length < 500) {
                for(VCSFileProxy file : files) {
                    String path = file.getPath();            
                    if (path.indexOf('@') != -1) { //NOI18N
                        path += '@'; //NOI18N
                    }
                    add("'"+path+"'"); //NOI18N
                }
            } else {
                add("--targets"); //NOI18N
                add(createTempCommandFile(files));
            }
        }

        public void addUrlArguments(SVNUrl... urls) throws IOException {        
            String[] paths = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                paths[i] = makeCliUrlString(urls[i], true);
            }
            if (EXPAND_TARGETS_OPTION && paths.length > 0 && paths.length < 500) {
                for(String path : paths) {
                    add("'"+path+"'"); //NOI18N
                }
            } else {
                add("--targets"); //NOI18N
                add(createTempCommandFile(paths));
            }
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
            return encodeUrl(url).toString() + '@' + (pegRev == null ? "HEAD" : pegRev); //NOI18N
        }
        
        public void addMessage(String message) throws IOException {
            if(message == null) {
                return;
            }
            add("--force-log"); //NOI18N
            add("-F"); //NOI18N             
            String msgFile = createTempCommandFile(message);
            add(msgFile);                               		
        }
        
        public void addConfigDir(VCSFileProxy configDir) {            
            if (configDir != null) {
                arguments.add("--config-dir"); //NOI18N
                arguments.add(configDir);
            }
        }         
    
        public void addCredentials(String user, String psswd) {
            if(user == null || user.trim().equals("")) { //NOI18N
                return;
            }            
            add("--username"); //NOI18N                      		
            add(user);
            if(psswd == null) {
                psswd = ""; //NOI18N
            }
            add("--password"); //NOI18N                      		
            add(psswd);                      		
        }
    
        @Override
        public Iterator<String> iterator() {
            return args.iterator();
        }
        
        String[] toArray() {
            return args.toArray(new String[args.size()]);
        }
        
        int size() {
            return args.size();
        }
    }       
                    
}
