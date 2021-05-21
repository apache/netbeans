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

package org.netbeans.modules.payara.jakartaee.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
//import org.netbeans.junit.ide.ProjectSupport;
//import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
//import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
//import org.netbeans.modules.j2ee.sun.api.ServerInterface;
//import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
//import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.BaseResourceNode;
//import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;
//import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
//import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Michal Mocnak
 */
public class Util {
    
    // TEST PROPERTIES
    public static final String EJB_PROJECT_NAME = "SjsasTestEjb";
    public static final String WEB_PROJECT_NAME = "SjsasTestWeb";
    public static final String JSF_PROJECT_NAME = "SjsasJSFTest";
    public static final String MDB_PROJECT_NAME = "SimpleMessage";
    public static final String WEBSERVICE_PROJECT_NAME = "CalculatorWSApplication";
    public static final String CUSTOMER_APPLICATION_PROJECT_NAME = "customer-cmp-ear";
    public static final String CUSTOMER_CLIENT_PROJECT_NAME = "customer-cmp-ear-app-client";
    public static final String CUSTOMER_WEB_PROJECT_NAME = "customer-cmp-ear-war";
    public static final String STATEFUL_PROJECT_NAME = "duke-stateful";
    public static final String STATEFUL_CLIENT_PROJECT_NAME = "duke-stateful-app-client";
    public static final String EJB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + EJB_PROJECT_NAME;
    public static final String WEB_PROJECT_PATH = //System.getProperty("xtest.tmpdir") + File.separator + WEB_PROJECT_NAME;
            "j2ee.sun.appsrv81" + File.separator + "build" + File.separator + "test" +
            File.separator + "qa-functional" + File.separator + "data" + File.separator +
            WEB_PROJECT_NAME;
    public static final String JSF_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + JSF_PROJECT_NAME;
    public static final String MDB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + MDB_PROJECT_NAME;
    public static final String STATEFUL_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + "duke_stateful";
    public static final String WEBSERVICE_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + WEBSERVICE_PROJECT_NAME;
    public static final String CUSTOMER_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + "customer-cmp-ear";
    public static final int SLEEP = 15000;
    
    // SERVER PROPERTIES FOR TESTS
    public static final String _SEP = System.getProperty("file.separator");
    public static final String _DISPLAY_NAME = "Sun Java System Application Server";
    public static final String _PRELUDE_LOCATION = getInstallDirName(System.getProperty("prelude.server.path"));
    public static final String _V3_LOCATION = getInstallDirName(System.getProperty("v3.server.path"));
    public static final String _DOMAIN = "domain1";
    public static final String _HOST = "localhost";
    //public static final String _PORT = getPort(new File(_INSTALL_LOCATION+_SEP+_DOMAIN+_SEP+"config"+_SEP+"domain.xml"));
    public static final String _USER_NAME = "admin";
    public static final String _PASSWORD = System.getProperty("v3.server.password","adminadmin");
    //public static final String _URL = "["+_PLATFORM_LOCATION+"]deployer:Sun:AppServer::"+_HOST+":"+_PORT;
    
    // SERVER PROPERTIES FOR APP SERVER REGISTRATION
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String PRELUDE_LOCATION = "platform_location";
    public static final String INSTALL_LOCATION = "install_location";
    public static final String DOMAIN = "domain";
    public static final String TYPE = "type";
    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName";
    public static final String TEMP_FILE_PREFIX = "DELETEME";

    public static String getInstallDirName(String s) {
        try {
            if (null == s) {
                // the user did not pass in a value
                s = getDirName();
            } else if (s.trim().length() < 1) {
                // the file name is totally bogus
                s = getDirName();
            }
            File f = new File(s);
            if (f.isFile()) {
                // need a directory... not a file
                s = getDirName();
                f = new File(s);
            } else if (!f.canWrite()) {
                // must be able to write into the directory
                s = getDirName();
                f = new File(s);
            }
        } catch (IOException ioe) {
        }
        return s;
    }


    static String getDirName() throws IOException {
        File f = File.createTempFile(TEMP_FILE_PREFIX, "IAMJUNK");
        f.delete();
        return f.getAbsolutePath() + File.separator + "glassfish";
    }
    /**
     * It returns admin port number if the server.
     */
    public static String getPort(File domainXml){
        String adminPort = null;
        String buffer = null;
        
        try {
            FileReader reader = new FileReader(domainXml);
            BufferedReader br = new BufferedReader(reader);
            
            while((buffer = br.readLine()) != null) {
                if(buffer.indexOf("admin-listener") > -1) {
                    int x = buffer.indexOf(34, buffer.indexOf("port"));
                    int y = buffer.indexOf(34, ++x);
                    adminPort = buffer.substring(x, y);
                    break;
                }
            }
            
            br.close();
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return adminPort;
    }
    
    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch(Exception e) {
            // Nothing to do
        }
    }
    
//    public static TargetModuleID deployModule(final ModuleType moduleType, final String modulePath, final String moduleName) throws Exception {
//        Project project = (Project)openProject(new File(modulePath));
//        TargetModuleID retVal = deployModule(moduleType, project, moduleName);
//        Util.closeProject(moduleName);
//        return   retVal;
//    }
    
//    public static TargetModuleID deployModule(final ModuleType moduleType, final Project project, final String moduleName) throws Exception {
//        ActionProvider ap = (ActionProvider)project.getLookup().lookup(ActionProvider.class);
//        J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
//        final ServerInstance si = ServerRegistry.getInstance().getServerInstance(_URL);
//        System.out.println("start deployModule "+new java.util.Date());
//
//        Runnable startCondition = new Runnable() {
//            public void run() {
//                System.out.println("startCond deployModule "+new java.util.Date());
//                while(!si.isConnected()) {
//                    try {
//                        System.out.print("S");
//                        Thread.sleep(1000);
//                    } catch(Exception e) {}
//                }
//                System.out.println("");
//            }
//        };
//
//        Runnable deployCondition = new Runnable() {
//            public void run() {
//                System.out.println("deployCond deployModule "+new java.util.Date());
//                while(getModuleID(moduleType, moduleName, si, true) == null) {
//                    try {
//                        System.out.print("D");
//                        Thread.sleep(500);
//                    } catch(Exception e) {}
//                }
//                System.out.println("");
//            }
//        };
//
//        Task t = RequestProcessor.getDefault().create(startCondition);
//        // TODO : get this to work
//        //        if (moduleType == ModuleType.WAR) {
//        //            ap.invokeAction(ap.COMMAND_RUN,project.getLookup());
//        //        } else {
//        ap.invokeAction(EjbProjectConstants.COMMAND_REDEPLOY, project.getLookup());
//        //        }
//
//        t.run();
//        if(!t.waitFinished(300000))
//            throw new Exception("Server start timeout");
//
//
//        t = RequestProcessor.getDefault().create(deployCondition);
//        t.run();
//        if(!t.waitFinished(300000))
//            throw new Exception("Deploy has timeout");
//
//        System.out.println("finish deployModule "+new java.util.Date());
//
//        TargetModuleID tmid = getModuleID(moduleType, moduleName, si, true);
//
//        if (null != tmid) {
//            si.getDeploymentManager().stop(new TargetModuleID[] {tmid});
//        } else {
//            throw new Exception("the module should be runnable");
//        }
//
//        return null;
//    }
    
    public static void undeployModule(final ModuleType moduleType, final String modulePath, final String moduleName, final TargetModuleID moduleID) throws Exception {
        final ServerInstance si = null; // ServerRegistry.getInstance().getServerInstance(_URL);
        si.getDeploymentManager().undeploy(new TargetModuleID[] {moduleID});
        
        Runnable undeployCondition = new Runnable() {
            public void run() {
                    try {
                        Thread.sleep(250);
                    } catch(Exception e) {}
                System.out.println("undeployCond deployModule "+new java.util.Date());
                while(getModuleID(moduleType, moduleName, si, false) != null) {
                    try {
                        System.out.print("U");
                        Thread.sleep(500);
                    } catch(Exception e) {}
                }
                System.out.println("");
            }
        };
        //sleep(SLEEP);
        
        Task t = RequestProcessor.getDefault().create(undeployCondition);
        t.run();
        if(!t.waitFinished(300000))
            throw new Exception("Undeploy has timeout");
        if(getModuleID(moduleType, moduleName, si, false) != null)
            throw new Exception("Undeploy failed");
    }
    
    public static TargetModuleID getModuleID(ModuleType moduleType, String moduleName, ServerInstance si, boolean running) {
        try {
            si.refresh();
            Target target = si.getTargets()[0].getTarget();
            TargetModuleID[] modules = null;
            if (running) {
                modules = si.getDeploymentManager().getRunningModules(moduleType, new Target[] {target});
            } else {
                modules = si.getDeploymentManager().getAvailableModules(moduleType, new Target[] {target});
            }
            
            for(int i=0;i<modules.length;i++) {
                if(modules[i].getModuleID().equals(moduleName))
                    return modules[i];
            }
            
            return null;
        } catch(Exception e) {
            return null;
        }
        
    }
    //return http error code if module is deployed and -1 if module is not deployed
    public static int executeWebModule(ModuleType moduleType, String moduleName) throws Exception{
        ServerInstance si = null; //ServerRegistry.getInstance().getServerInstance(_URL);
        si.refresh();
        Target target = si.getTargets()[0].getTarget();
        TargetModuleID[] modules = si.getDeploymentManager().getRunningModules(moduleType, new Target[] {target});
        for(int i=0;i<modules.length;i++){
            if(modules[i].getModuleID().equals(moduleName)) {
                URL url=new URL(modules[i].getWebURL());
                HttpURLConnection httpConn = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
                return httpConn.getResponseCode();
            }
        }
        throw new Exception("The module is not deployed");
    }
    
//    public static Object openProject(File projectDir) {
//        return ProjectSupport.openProject(FileUtil.normalizeFile(projectDir));
//    }
//
//    public static void closeProject(String projectName) {
//        ProjectSupport.closeProject(projectName);
//    }
//
//    public static String[] getResourcesNames(String query, String keyProperty, ServerInterface mejb) throws Exception {
//        String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config";
//        ObjectName objName = new ObjectName(MAP_RESOURCES);
//        ObjectName[] beans = (ObjectName[])mejb.invoke(objName, query, null, null);
//        String[] resNames = new String[beans.length];
//        for(int i=0; i<beans.length; i++){
//            String resName = ((ObjectName)beans[i]).getKeyProperty(keyProperty);
//            resNames[i] = resName;
//        }
//
//        return resNames;
//    }
//
//    public static Resources getResourcesObject(SunResourceDataObject resourceObj) {
//        BaseResourceNode resNode = (BaseResourceNode)resourceObj.getNodeDelegate();
//        return resNode.getBeanGraph();
//    }
    
    public static Process runAsadmin(String[] command) throws Exception {
        String[] cmd = new String[command.length+1];
        cmd[0] = new File(Util._PRELUDE_LOCATION, "bin").getAbsolutePath();
        
        if(System.getProperty("os.name").startsWith("Windows"))
            cmd[0] = new File(cmd[0], "asadmin.bat").getAbsolutePath();
        else
            cmd[0] = new File(cmd[0], "asadmin").getAbsolutePath();
        
        for(int i=1;i<cmd.length;i++) {
            cmd[i] = command[i-1];
        }
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        return pb.start();
    }
    /** Read a file (< 100Kb) into a String and return the contents.
     */
    public static String readFile(File target) throws IOException {
        char [] buffer = new char[100000];
        int filelength = 0;
        Reader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(target));
            filelength = reader.read(buffer, 0, buffer.length);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException ex) {
                    System.out.println("IOException on closing file: " + target.getName());
                }
            }
        }
        
        return new String(buffer, 0, filelength);
    }
    /* Quick & dirty implementation that reads two files an compares for exact match, regardless
     * of white space.  Files must be < 100K each.
     */
    public static boolean compareFile(File beforeFile, File afterFile) throws IOException {
        String before = readFile(beforeFile);
        String after = readFile(afterFile);
        
        return after.equals(before);
    }
    
    public static void deleteJunk(File d) {
        if (!d.exists()) return;
        if (d.isFile()) {
            d.delete();
        } else { // directory
            for (File cf : d.listFiles()) {
                deleteJunk(cf);
            }
            d.delete();
        }
    }
}

