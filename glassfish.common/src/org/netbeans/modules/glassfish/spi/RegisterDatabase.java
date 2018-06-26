/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.spi;

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
                    } catch (DatabaseException ex) {
                        Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex); //NOI18N
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex); //NOI18N
                    }
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
                Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex); //NOI18N
            } catch (IOException ex) {
                Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex); //NOI18N
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
                    outd.write("<localizing-bundle>org.netbeans.modules.glassfish.eecommon.api.Bundle</localizing-bundle>\n");//NOI18N
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
                        Logger.getLogger("glassfish-eecommon").log(Level.INFO, ioe.getLocalizedMessage(), ioe); //NOI18N
                    }
                }
                if (null != outStreamd) {
                    try {
                        outStreamd.close();
                    } catch (IOException ioe) {
                        Logger.getLogger("glassfish-eecommon").log(Level.INFO, ioe.getLocalizedMessage(), ioe); //NOI18N
                    }
                }
                if (null != ld) {
                    ld.releaseLock();
                }
            }
        } //run
    } //DerbyLibraryRegistrar
}
