/**
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

package org.netbeans.installer.utils.applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 
 */
public class WebLogicUtils {
   // private static Map<File, Version> knownVersions = new HashMap<File, Version>();
    
    private WebLogicUtils() {
        // does nothing
    }
    
    public static String configureEnvironment(File wlLocation, File jdkLocation) throws IOException {
        String jdkPath = jdkLocation.getAbsolutePath();
        String wlPath = wlLocation.getAbsolutePath();
        if (SystemUtils.isWindows()) {
            // must convert to short paths, the result is elimination of all spaces in path (wl installer cannot handle them)
            jdkPath = convertPathNamesToShort(jdkPath);
            wlPath = convertPathNamesToShort(wlPath);
            wlLocation = new File(wlPath);
        }
        SystemUtils.getEnvironment().put(MW_HOME, wlPath);
        SystemUtils.getEnvironment().put(JAVA_HOME, jdkPath);
        //Setting an environment for WebLogic domain creation
        final ExecutionResults configureResults = SystemUtils.executeCommand(wlLocation, getConfigureCommand(wlLocation));       
        if(configureResults.getErrorCode() > 0) {
            throw new DomainCreationException(ERROR_CONFIGURE_ERROR_STRING, configureResults.getErrorCode());
        }
        if(configureResults.getStdOut().length() == 0) {
            throw new DomainCreationException(ERROR_CONFIGURE_ENV_STRING);
        } 
        return configureResults.getStdOut();
     }
    
    
    public static void createDomain(File wlLocation, File jdkLocation, 
            File domainLocation, String domainName, String username, String adminPassword) throws IOException {
        
        LogManager.log(ErrorLevel.MESSAGE, "Creating domain " + domainName + "...");
        String configOutput = configureEnvironment(wlLocation, jdkLocation); 
        
        ProcessBuilder domainBuilder = new ProcessBuilder();
        BufferedReader reader = new BufferedReader(new StringReader(configOutput));
        //parse CLASSPATH and PATH from the output. TODO: try to find another way to grab this vars
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {                    
            if(line.startsWith(CLASSPATH) || line.startsWith(PATH)) {
                String[] keyvalue = line.split("=");
                LogManager.log(ErrorLevel.MESSAGE, 
                        "Setting environment variable " + keyvalue[0] + " to " + keyvalue[1]);
                domainBuilder.environment().put(keyvalue[0], keyvalue[1]);
            }
        }
        if(!domainBuilder.environment().containsKey(PATH) || 
                !domainBuilder.environment().containsKey(CLASSPATH)) {
            throw new DomainCreationException(ERROR_CONFIGURE_ENV_STRING);
        }                          
        List <String> commands = new ArrayList <String> ();               
        commands.add(getJavaExecutableCommand(jdkLocation));
        commands.add("-Xmx1024m");
        commands.add("-XX:MaxPermSize=128m");               
        commands.add("-Dweblogic.Domain=" + domainName);
        commands.add("-Dweblogic.management.GenerateDefaultConfig=true");
        commands.add("-Dweblogic.management.username=" + username);
        commands.add("-Dweblogic.management.password=" + adminPassword);                
        commands.add("-Djava.endorsed.dirs=" + new File(wlLocation, "oracle_common" + File.separator + "modules" + File.separator + "endorsed").getAbsolutePath());
        commands.add("weblogic.Server");
//        %JAVA_HOME%\bin\java.exe 
//        -Xmx1024m -XX:MaxPermSize=128m -classpath=$CLASSPATH -Dweblogic.Domain=domainname -Dweblogic.management.GenerateDefaultConfig=true
//        -Dweblogic.management.username=username -Dweblogic.management.password=password  weblogic.Server                               
        File domainDir = new File(domainLocation, domainName);
        if(!domainDir.exists()) {
            domainDir.mkdirs();
        }
        domainBuilder.command(commands).directory(domainDir);   
        final ExecutionResults domainResults = 
                executeCommandWithDestroyTag(domainBuilder, SERVER_RUNNING_TAG, adminPassword);
        if (domainResults.getErrorCode() == 0) {
            throw new DomainCreationException(domainResults.getErrorCode());
        }

    }    
    
    public static void stopDomains(File wlLocation) throws IOException{
        //figure out if we need to stop all the domains or just the default one to remove WL.
        
        LogManager.log(ErrorLevel.MESSAGE, "Stopping domains for WebLogic: " + wlLocation);
        Document doc = null;    
        try {
            doc = XMLUtils.loadXMLDocument(new File(wlLocation, DOMAIN_REGISTRY_XML));
        } catch (XMLException e) {
            LogManager.log("Cannot load " + DOMAIN_REGISTRY_XML + " file", e);
        }
        if (doc != null) {
            for (Element element : XMLUtils.getChildren(doc.getDocumentElement(), "domain")) {
                File domainLocation = new File(element.getAttribute("location"));                
                stopDomain(wlLocation, domainLocation);
            }                              
        }
    }
    
    public static boolean stopDomain(File wlLocation, File domainLocation) throws IOException{
        File stopWebLogicFile = getStopWebLogicFile(domainLocation);
                
        if(stopWebLogicFile.exists()) {
            LogManager.log(ErrorLevel.MESSAGE, "Stopping domain at " + domainLocation);            
            SystemUtils.getEnvironment().put(MW_HOME, wlLocation.getAbsolutePath());
//            SystemUtils.getEnvironment().put(JAVA_HOME, 
//                    new File(System.getProperty("java.home")).getAbsolutePath());
            
            ExecutionResults results = SystemUtils.executeCommand(
                    wlLocation, 
                    getStopWebLogicCommand(domainLocation));
            
            return  results.getErrorCode() != ExecutionResults.TIMEOUT_ERRORCODE;       
        } 
        LogManager.log(ErrorLevel.MESSAGE, "stopWebLogicFile doesn't exist for the domain: " + domainLocation);   
        return false;
    }    

    public static void unpackServerFiles (final File wlLocation, File javaHome, File installFile) throws IOException {
        String wlPath = wlLocation.getAbsolutePath();
        if (SystemUtils.isWindows()) {
            // must convert to short paths, the result is elimination of all spaces in path (wl installer cannot handle them)
            wlPath = convertPathNamesToShort(wlPath);
        }
        //Setting an environment for WebLogic domain creation
        List <String> commands = new ArrayList <String> ();               
        commands.add(getJavaExecutableCommand(javaHome));
        commands.add("-jar");
        commands.add(installFile.getAbsolutePath());
        commands.add("-novalidation");
        commands.add("ORACLE_HOME=" + wlPath);
        final ExecutionResults configureResults = SystemUtils.executeCommand(wlLocation, commands.toArray(new String[commands.size()]));
        if(configureResults.getErrorCode() > 0) {
            throw new IOException(StringUtils.format(ERROR_UNPACKING_ERROR_STRING, configureResults.getErrorCode()));
        }
    }
        
    private static String[] getConfigureCommand(File wlLocation) {
        String subPath = "wlserver/server/bin/".replace("/", File.separator); //NOI18N
        String configure = new File(wlLocation, subPath + 
                (SystemUtils.isWindows() ? "setWLSEnv.cmd" : "setWLSEnv.sh")).getAbsolutePath();
        return SystemUtils.isWindows() ? new String[] {configure} :
                                         new String[] {"/bin/bash", configure};
    }    
    
    private static String[] getStopWebLogicCommand(File domainLocation) {
        String stopWebLogic = getStopWebLogicFile(domainLocation).getAbsolutePath();
        return SystemUtils.isWindows() ? new String[] {stopWebLogic} :
                                         new String[] {"/bin/bash", stopWebLogic};
    }
    private static File getStopWebLogicFile(File domainLocation) {
        return new File(domainLocation, "bin/" +  
                (SystemUtils.isWindows() ? "stopWebLogic.cmd" : "stopWebLogic.sh"));
    } 
    
    private static String getJavaExecutableCommand(File jdkLocation) {
        return !SystemUtils.isWindows() ?  jdkLocation.getAbsolutePath() + "/bin/java" :
                        convertPathNamesToShort(jdkLocation.getAbsolutePath()) + "\\bin\\java.exe";
    }
    
    private static String convertPathNamesToShort(String path){
        File pathConverter = new File(SystemUtils.getTempDirectory(), "pathConverter.cmd");
        String result = path;
        List <String> commands = new ArrayList <String> (); 
        commands.add("@echo off");
        commands.add("set JPATH=" + path);
        commands.add("for %%i in (\"%JPATH%\") do set JPATH=%%~fsi");
        commands.add("echo %JPATH%");
        try{
            FileUtils.writeStringList(pathConverter, commands);
            ExecutionResults res=SystemUtils.executeCommand(pathConverter.getAbsolutePath());        
            FileUtils.deleteFile(pathConverter);
            result = res.getStdOut().trim();
        } catch(IOException ioe) {
            LogManager.log(ErrorLevel.WARNING, 
                    "Failed to convert " + path + " to a path with short names only." +
                     "\n Exception is thrown " + ioe);
        }
        return result;
    }    
    
    private static ExecutionResults executeCommandWithDestroyTag(ProcessBuilder builder, String processDestroyTag, String passwordToHide) throws IOException {               
        String commandString = StringUtils.asString(builder.command(), StringUtils.SPACE).replace(passwordToHide, "*****"); //NOI18N
        LogManager.log(ErrorLevel.MESSAGE,
                "executing command: " + commandString +
                ", in directory: " + builder.directory());
        LogManager.indent();                  
        
        StringBuilder processStdOut = new StringBuilder();
        StringBuilder processStdErr = new StringBuilder();
        int           errorLevel = ExecutionResults.TIMEOUT_ERRORCODE;
                                
        Process process = builder.start();              
        
        long startTime = System.currentTimeMillis();
        long endTime   = startTime + MAX_EXECUTION_TIME;
        boolean doRun = true;
        boolean destroyTagReached = false;
        long delay = INITIAL_DELAY;
        while (doRun && (System.currentTimeMillis() < endTime)) {
            try {
                Thread.sleep(delay);
                if(delay < MAX_DELAY) {
                    delay += DELTA_DELAY;
                }
            }  catch (InterruptedException e) {
                ErrorManager.notifyDebug("Interrupted", e);
            }
            try {
                errorLevel = process.exitValue();
                doRun = false;
            } catch (IllegalThreadStateException e) {
                ; // do nothing - the process is still running
            }
            String string;
            
            string = StringUtils.readStream(process.getInputStream());
            if (string.length() > 0) {
                BufferedReader reader = new BufferedReader(new StringReader(string));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    LogManager.log(ErrorLevel.MESSAGE, "[stdout]: " + line);
                    if(line.contains(processDestroyTag)) {
                        try {
                            Thread.sleep(MAX_DELAY);
                        } catch(InterruptedException e) {
                            ErrorManager.notifyDebug("Interrupted", e);
                        }
                        LogManager.log(ErrorLevel.MESSAGE, 
                                "processDestroyTag is reached, destroying the process");                        
                        destroyTagReached = true;
                        break;
                    }
                }                
                processStdOut.append(string);
            }
            
            //temp solution - terminate the process when domain created
            if(destroyTagReached) {
                process.destroy();
                doRun = false;
                errorLevel = 0;
                break;
            }
            
            string = StringUtils.readStream(process.getErrorStream());
            if (string.length() > 0) {
                BufferedReader reader = new BufferedReader(new StringReader(string));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    LogManager.log(ErrorLevel.MESSAGE, "[stderr]: " + line);
                }
                
                processStdErr.append(string);
            }
        }
        
        LogManager.log(ErrorLevel.MESSAGE, 
                (doRun) ? 
                    "[return]: killed by timeout" : 
                    "[return]: " + errorLevel);
        process.destroy();        
        LogManager.unindent();
        LogManager.log(ErrorLevel.MESSAGE, "... command execution finished");
        
        return new ExecutionResults(errorLevel, processStdOut.toString(), processStdErr.toString());
    }
    
  

    // Inner Classes
    public static class DomainCreationException extends IOException {
        private String message;
        
        public DomainCreationException() {
            super();                   
        }
                
        public DomainCreationException(int errorCode) {
            super();
            
            if (errorCode != ExecutionResults.TIMEOUT_ERRORCODE) {
                setMessage(StringUtils.format(ERROR_CREATE_DOMAIN_EXIT_CODE_STRING, errorCode));                
            } else {
                setMessage(ERROR_CREATE_DOMAIN_TIMEOUT_STRING);                
            }
        }
        public DomainCreationException(String msg, int errorCode) {
            super();
            setMessage(StringUtils.format(msg, errorCode));                
        }
        public DomainCreationException(String msg) {
            super();
            setMessage(msg);                
        }        

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
    
    // Constants
    public static final long MAX_EXECUTION_TIME = 600000;    
    public static final int MAX_DELAY = 50; // NOMAGI
    public static final int INITIAL_DELAY = 5; // NOMAGI
    public static final int DELTA_DELAY = 5; // NOMAGI    
    
    private static final String SERVER_RUNNING_TAG = 
            "The server started in RUNNING mode"; // NOI18N
    private static final String MW_HOME = 
            "MW_HOME"; // NOI18N
    private static final String JAVA_HOME = 
            "JAVA_HOME"; // NOI18N
    private static final String CLASSPATH = 
            "CLASSPATH"; // NOI18N
    private static final String PATH = 
            "PATH"; // NOI18N
    private static final String DOMAIN_REGISTRY_XML = 
            "domain-registry.xml"; // NOI18N  
                   
    public static final String ERROR_CONFIGURE_ENV_STRING =
            ResourceUtils.getString(WebLogicUtils.class,
            "WU.error.configure.env");       
    public static final String ERROR_CONFIGURE_ERROR_STRING =
            ResourceUtils.getString(WebLogicUtils.class,
            "WU.error.configure.errno");    
    public static final String ERROR_UNPACKING_ERROR_STRING =
            ResourceUtils.getString(WebLogicUtils.class,
            "WU.error.unpacking.errno"); //NOI18N
    public static final String ERROR_CREATE_DOMAIN_ERROR_STRING =
            ResourceUtils.getString(WebLogicUtils.class,
            "WU.error.create.domain.errno");    
    public static final String ERROR_CREATE_DOMAIN_EXIT_CODE_STRING =
            ResourceUtils.getString(WebLogicUtils.class,
            "WU.error.create.domain.exitcode");
    public static final String ERROR_CREATE_DOMAIN_TIMEOUT_STRING =
            ResourceUtils.getString(WebLogicUtils.class,
            "WU.error.create.domain.timeout");
 
    
}
