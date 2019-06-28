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

package org.netbeans.modules.payara.spi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.modules.derby.spi.support.DerbySupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Nitya Doraisamy
 */
public class RegisterDatabase {

    private static volatile RegisterDatabase reg = null;
    
    public static RegisterDatabase getDefault(){
        if (reg == null) {
            reg = new RegisterDatabase();
        }
        return reg;
    }

    public void setupDerby(String serverinstall) {
        String location = DerbySupport.getLocation();
        if (null != location && location.trim().length() > 0) {
            return;
        }
        File dbloc = new File(serverinstall, "javadb"); //NOI18N
        if (dbloc.exists() && dbloc.isDirectory() && dbloc.canRead()) {
            DerbySupport.setLocation(dbloc.getAbsolutePath());
            location = DerbySupport.getSystemHome();
            if (null != location && location.trim().length() > 0) {
                return;
            }else{
                File dbdir = new File(DerbySupport.getDefaultSystemHome());
                if (dbdir.exists() == false) {
                    dbdir.mkdirs();
                }
            }
            DerbySupport.setSystemHome(DerbySupport.getDefaultSystemHome());
        }
        configureDatabase();
    }

    public void configureDatabase(){
        String location = DerbySupport.getLocation();
        File dbInstall = new File(location);
        if (dbInstall != null && dbInstall.exists()){
            registerDerbyLibrary(dbInstall);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        DerbyDatabases.createSampleDatabase();
                    } catch (DatabaseException | IOException ex) {
                        Logger.getLogger("payara-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex); //NOI18N
                    }
                    //NOI18N
                    
                }
            });
        }
    }

    private void registerDerbyLibrary(final File location) { 
        final FileObject libsFolder = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries"); //NOI18N
        if (libsFolder!=null){
            try {
                libsFolder.getFileSystem().runAtomicAction(
                        new DerbyLibraryRegistrar(location, libsFolder));
            } catch (FileStateInvalidException ex) {
                Logger.getLogger("payara-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex); //NOI18N
            } catch (IOException ex) {
                Logger.getLogger("payara-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex); //NOI18N
            }
        }
    }

    static class DerbyLibraryRegistrar implements FileSystem.AtomicAction {

        private File location;

        private FileObject libsFolder;

        DerbyLibraryRegistrar(File location, FileObject libsFolder) {
            this.location = location;
            this.libsFolder = libsFolder;
        }

        @Override
        public void run() throws IOException {
            FileLock ld = null;
            java.io.OutputStream outStreamd = null;
            Writer outd = null;
            OutputStreamWriter osw = null;
            try {
                //  the derby lib driver:
                FileObject derbyLib =null;
                derbyLib = libsFolder.getFileObject("JavaDB" ,"xml");//NOI18N
                if (null == derbyLib) {
                    derbyLib = libsFolder.createData("JavaDB" ,"xml");//NOI18N
                    ld = derbyLib.lock();
                    outStreamd = derbyLib.getOutputStream(ld);
                    osw = new OutputStreamWriter(outStreamd);
                    outd = new BufferedWriter(osw);
                    outd.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">\n");//NOI18N
                    outd.write("<library version=\"1.0\">\n<name>JAVADB_DRIVER_LABEL</name>\n");//NOI18N
                    outd.write("<type>j2se</type>\n");//NOI18N
                    outd.write("<localizing-bundle>org.netbeans.modules.payara.eecommon.api.Bundle</localizing-bundle>\n");//NOI18N
                    outd.write("<volume>\n<type>classpath</type>\n"); //NOI18N
                    outd.write("<resource>jar:"+new File(location.getAbsolutePath()+"/lib/derby.jar").toURI().toURL()+"!/</resource>\n"); //NOI18N
                    outd.write("<resource>jar:"+new File(location.getAbsolutePath()+"/lib/derbyclient.jar").toURI().toURL()+"!/</resource>\n"); //NOI18N
                    outd.write("<resource>jar:"+new File(location.getAbsolutePath()+"/lib/derbynet.jar").toURI().toURL()+"!/</resource>\n"); //NOI18N
                    outd.write("</volume>\n<volume>\n<type>src</type>\n</volume>\n"); //NOI18N
                    outd.write("<volume>\n<type>javadoc</type>\n");  //NOI18N
                    outd.write("</volume>\n</library>"); //NOI18N
                }
            } finally {
                if (null != outd) {
                    try {
                        outd.close();
                    } catch (IOException ioe) {
                        Logger.getLogger("payara-eecommon").log(Level.INFO, ioe.getLocalizedMessage(), ioe); //NOI18N
                    }
                }
                if (null != outStreamd) {
                    try {
                        outStreamd.close();
                    } catch (IOException ioe) {
                        Logger.getLogger("payara-eecommon").log(Level.INFO, ioe.getLocalizedMessage(), ioe); //NOI18N
                    }
                }
                if (null != ld) {
                    ld.releaseLock();
                }
            }
        } //run
    } //DerbyLibraryRegistrar
}
