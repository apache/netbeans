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

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.glassfish.spi.ExecSupport;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.InputOutput;

public class CreateDomain extends Thread {

    static final String PORTBASE = "portbase"; // NOI18N
    private final String uname;
    private final String pword;
    private final File platformLocation;
    private final Map<String, String> map;
    private final Map<String, String> ip;
    private GlassfishInstanceProvider gip;
    private boolean register;
    private final String installRootKey;

    public CreateDomain(String uname, String pword, File platformLocation, 
            Map<String, String> ip, GlassfishInstanceProvider gip, boolean register,
            boolean useDefaultPorts, String installRootKey) {
        this.uname = uname;
        this.pword = pword;
        this.platformLocation = platformLocation;
        this.ip = ip;
        this.map = new HashMap<String,String>();
        this.gip = gip;
        map.putAll(ip);
        this.register = register;
        this.installRootKey = installRootKey;
        computePorts(ip,map, useDefaultPorts);
    }

    private static void computePorts(Map<String, String> ip, Map<String, String> createProps, boolean useDefaultPorts) {
        int portBase = 8900;
        int kicker = ((new Date()).toString() + ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR)+ip.get(GlassfishModule.DOMAIN_NAME_ATTR)).hashCode() % 40000;
        kicker = kicker < 0 ? -kicker : kicker;
        
        int httpPort = portBase + kicker + 80;
        int adminPort = portBase + kicker + 48;
        if (useDefaultPorts) {
            httpPort = 8080;
            adminPort = 4848;
        }
        ip.put(GlassfishModule.HTTPPORT_ATTR, Integer.toString(httpPort));
        ip.put(GlassfishModule.ADMINPORT_ATTR, Integer.toString(adminPort));
        createProps.put(GlassfishModule.HTTPPORT_ATTR, Integer.toString(httpPort));
        createProps.put(GlassfishModule.ADMINPORT_ATTR, Integer.toString(adminPort));
        if (!useDefaultPorts) {
            createProps.put(CreateDomain.PORTBASE, Integer.toString(portBase+kicker));
        }
    }

    @Override
    public void run() {
        Process process = null;
        // attempt to do the domian/instance create HERE
        File irf = platformLocation;
        int retVal = 0;
        if (null != irf && irf.exists()) {
            PDCancel pdcan;
            String startScript = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java"; // NOI18N
            if ("\\".equals(File.separator)) { // NOI18N
                startScript += ".exe"; // NOI18N
            }
            File jar = new File(irf, "modules" + File.separator + "admin-cli.jar"); // NOI18N
            String jarLocation = jar.getAbsolutePath();
            String domain = map.get(GlassfishModule.DOMAIN_NAME_ATTR);
            String domainDir = map.get(GlassfishModule.DOMAINS_FOLDER_ATTR);
            File passWordFile = createTempPasswordFile(pword, "changeit"); //NOI18N

            if (passWordFile == null) {
                return;
            }
            String arrnd[];
            
            if ("".equals(pword)) { // NOI18N
                arrnd = gip.getNoPasswordCreatDomainCommand(startScript, jarLocation,domainDir,map.get(PORTBASE),uname,domain);
            } else {
                if (null == map.get(PORTBASE)) {
                    arrnd = new String[] {startScript,
                        "-client",  // NOI18N
                        "-jar",  // NOI18N
                        jarLocation,
                        "create-domain", //NOI18N
                        "--domaindir", //NOI18N
                        domainDir,
                        "--portbase", //NOI18N
                        map.get(PORTBASE),
                        "--user", //NOI18N
                        uname,
                        "--passwordfile", //NOI18N
                        passWordFile.getAbsolutePath(),
                        domain
                    };
                } else {
                    arrnd = new String[] {startScript,
                        "-client",  // NOI18N
                        "-jar",  // NOI18N
                        jarLocation,
                        "create-domain", //NOI18N
                        "--domaindir", //NOI18N
                        domainDir,
                        "--user", //NOI18N
                        uname,
                        "--passwordfile", //NOI18N
                        passWordFile.getAbsolutePath(),
                        domain
                    };
                }
            }

            ProgressHandle ph = null;
            try {
                ExecSupport ee = new ExecSupport();
                process = Runtime.getRuntime().exec(arrnd, null, irf);
                pdcan = new PDCancel(process, domainDir + File.separator + domain);
                ph = ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(this.getClass(), "LBL_Creating_personal_domain"), // NOI18N
                        pdcan);
                ph.start();

                ee.displayProcessOutputs(process,
                        NbBundle.getMessage(this.getClass(), "LBL_outputtab"),//NOI18N
                        NbBundle.getMessage(this.getClass(), "LBL_RunningCreateDomainCommand")//NOI18N
                        );
            } catch (MissingResourceException ex) {
                showInformation(ex.getLocalizedMessage());
            } catch (IOException ex) {
                showInformation(ex.getLocalizedMessage());
            } catch (InterruptedException ex) {
                showInformation(ex.getLocalizedMessage());
            } catch (RuntimeException ex) {
                showInformation(ex.getLocalizedMessage());
                // this is more interesting
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        ex);
            }
            if (null != process) {
                try {
                    retVal = process.waitFor();
                    if (!passWordFile.delete()) {
                        showInformation(NbBundle.getMessage(this.getClass(), "MSG_delete_password_failed", passWordFile.getAbsolutePath())); // NOI18N
                    }
                } catch (InterruptedException ie) {
                    retVal = -1;
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            ie);
                }
            } else {
                retVal = -1;
            }
            if (null != ph) {
                ph.finish();
            }
            if (0 == retVal) {
                // The create was successful... create the instance and register it.
                if (register) {
                    GlassfishInstance gi = GlassfishInstance.create(ip,gip);
                }
            } else {
                if (register) {
                    NbPreferences.forModule(this.getClass()).put(ServerUtilities.PROP_FIRST_RUN+installRootKey,
                            "false");
                }
            }

            String mess = 0 == retVal ?
                NbBundle.getMessage(this.getClass(), "MSG_see_successful_results") :
                NbBundle.getMessage(this.getClass(), "MSG_see_failure_results");
            Object ret = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(mess, NotifyDescriptor.YES_NO_OPTION));
            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                InputOutput io = org.openide.windows.IOProvider.getDefault().getIO(NbBundle.getMessage(this.getClass(), "LBL_outputtab"), false);
                io.select();
            }
        }
    }

    public int getHttpPort() {
        return Integer.parseInt(ip.get(GlassfishModule.HTTPPORT_ATTR));
    }

    public int getAdminPort() {
        return Integer.parseInt(ip.get(GlassfishModule.ADMINPORT_ATTR));
    }

    static class PDCancel implements Cancellable {

        private final Process p;
        private final String dirname;
        private boolean notFired = true;

        PDCancel(Process p, String newDirName) {
            this.p = p;
            this.dirname = newDirName;
        }

        public synchronized boolean isNotFired() {
            return notFired;
        }

        @Override
        public synchronized boolean cancel() {
            notFired = false;
            p.destroy();
            File domainDir = new File(dirname);
            if (domainDir.exists()) {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(domainDir));
                try {
                    fo.delete();
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINER,"",ex); // NOI18N
                    showError(NbBundle.getMessage(GlassfishInstanceProvider.class, "ERR_Failed_cleanup", dirname));  // NOI18N
                }
            }
            return true;
        }
    }

    /*
     * This create a temporary file, deleted at exit, that contains
     * the necessary password infos for starting or creating a domain
     * bot admu and master password are there.
     * @returns the temporary file
     * or null if for some reason, this file cannot be created.
     */
    private static File createTempPasswordFile(String password, String masterPassword) {
        OutputStream output;
        PrintWriter p = null;
        File retVal = null;
        try {
            retVal = Files.createTempFile("admin", null).toFile();//NOI18N

            retVal.deleteOnExit();
            output = new FileOutputStream(retVal);
            p = new PrintWriter(output);
            p.println("AS_ADMIN_ADMINPASSWORD=" + password);//NOI18N for create domains

            p.println("AS_ADMIN_PASSWORD=" + password);//NOI18N for start domains

            p.println("AS_ADMIN_MASTERPASSWORD=" + masterPassword);//NOI18N

        } catch (IOException e) {
            // this should not happen... If it does we should at least log it
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (p != null) {
                p.close();
            }
        }
        return retVal;
    }
    
    private static void showInformation(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });       
    }
    
    private static void showError(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });        
    }
}
