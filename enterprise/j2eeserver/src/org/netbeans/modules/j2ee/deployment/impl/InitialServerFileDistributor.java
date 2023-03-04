/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.j2ee.deployment.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  nn136682
 */
public class InitialServerFileDistributor extends ServerProgress {

    private static final Logger LOGGER = Logger.getLogger(InitialServerFileDistributor.class.getName());

    private final ServerString serverString;
    private final DeploymentTarget dtarget;
    private final IncrementalDeployment incDeployment;
    private final Target target;
    boolean inPlace = false;


    public InitialServerFileDistributor(DeploymentTarget dtarget, Target target) {
        super(dtarget.getServer().getServerInstance());
        this.serverString = dtarget.getServer();
        this.dtarget = dtarget;
        this.target = target;
        incDeployment = serverString.getServerInstance().getIncrementalDeployment();
    }

    public File distribute() {
        ModuleConfigurationProvider deployment = dtarget.getModuleConfigurationProvider();
        J2eeModule source = dtarget.getModule();
        String name = dtarget.getDeploymentName();
        File dir = incDeployment.getDirectoryForNewApplication(name, target, deployment.getModuleConfiguration());
        try {
            if (dir == null) {
                inPlace = true;
                if (dtarget.getModule().getContentDirectory() != null) {
                    dir = FileUtil.toFile(dtarget.getModule().getContentDirectory());
                }
                if (dir == null) {
                    String msg = NbBundle.getMessage(InitialServerFileDistributor.class, "MSG_InPlaceNoSupport");
                    setStatusDistributeFailed(msg);
                    return null;
                } else {
                    setStatusDistributeCompleted(NbBundle.getMessage(InitialServerFileDistributor.class, "MSG_InPlaceDeployment", dir)); //NOI18N
                    return dir;
                }
            }

            setStatusDistributeRunning(NbBundle.getMessage(
                InitialServerFileDistributor.class, "MSG_RunningInitialDeploy", dtarget.getDeploymentName(), dir));

            _distribute(source.getArchiveContents(), dir, collectChildModuleNames(source));

            if (source instanceof J2eeApplication) {
                J2eeModule[] childModules = ((J2eeApplication) source).getModules();
                for (int i = 0; i < childModules.length; i++) {
                    String uri = childModules[i].getUrl();
                    J2eeModule childModule = deployment.getJ2eeModule(uri);
                    File subdir = incDeployment.getDirectoryForNewModule(dir, uri, childModule, deployment.getModuleConfiguration());
                    _distribute(childModules[i].getArchiveContents(), subdir, null);
                }
            }

            setStatusDistributeCompleted(NbBundle.getMessage(
                InitialServerFileDistributor.class, "MSG_DoneInitialDistribute", dtarget.getDeploymentName()));

            return dir;

        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
            setStatusDistributeFailed(e.getMessage());
            if (!inPlace && !cleanup(dir)) {
                setStatusDistributeFailed("Failed to cleanup the data after unsucesful distribution");
            }
        }
        return null;
    }

    // We are collecting module names to be able to skip .jar and .war files under
    // the application root with the same name as one of the deployed modules. Those
    // are typically jars coresponding to already existing exploded directory and we
    // don't want to deploy them  -->  see also #199096 and #222924 for more details
    private Set<String> collectChildModuleNames(J2eeModule source) {
        final Set<String> childModuleNames = new HashSet<String>();
        if (source instanceof J2eeApplication) {
            for (J2eeModule module : ((J2eeApplication) source).getModules()) {

                // We have to use getUrl() --> it's the only method that take the
                // maven ear plugin fileNameMapping attribute into account
                String moduleURL = module.getUrl();
                if (moduleURL != null) {
                    moduleURL = moduleURL.substring(moduleURL.lastIndexOf("/") + 1); // NOI18N
                    childModuleNames.add(moduleURL);
                }
            }
        }
        return childModuleNames;
    }

    private boolean cleanup(File f) {
        String [] chNames = f.list();
        boolean deleted = true;
        if (chNames != null) {
            for (int i = 0; i < chNames.length; i++) {
                File ch = new File(f.getAbsolutePath(), chNames[i]);
                if (ch.isDirectory()) {
                    deleted = deleted && cleanup(ch);
                } else {
                    deleted = deleted && ch.delete();
                }
            }
        }
        deleted = deleted && f.delete();
        return deleted;
    }

    private void _distribute(Iterator<J2eeModule.RootedEntry> rootedEntries, File dir, Set<String> childModuleNames) {
        FileLock lock = null;

        try {
            // this is just safeguard - should not happen anymore
            // used to happen in EAR when folder had a same name as jar
            // and jar was copied to exploded dir
            if (dir.exists() && dir.isFile()) {
                dir.delete();
            }

            // mkdirs()/toFileObject is not not tolerated any more.
            FileObject destRoot = FileUtil.createFolder(dir);

            FileObject[] garbages = destRoot.getChildren();
            for (int i = 0; i < garbages.length; i++) {
                try {
                    garbages[i].delete();
                } catch (java.io.IOException ioe) {
                    LOGGER.log(Level.FINER, null, ioe);
                    if (Utilities.isWindows()) {
                        String ext = garbages[i].getExt().toLowerCase(Locale.ENGLISH);
                        if ("jar".equals(ext) || "zip".equals(ext)) {
                            zeroOutArchive(garbages[i]);
                        } else {
                            throw ioe;
                        }
                    } else {
                        throw ioe;
                    }
                }
            }

            while (rootedEntries.hasNext()) {
                J2eeModule.RootedEntry entry = rootedEntries.next();
                String relativePath = entry.getRelativePath();
                FileObject sourceFO = entry.getFileObject();

                if (childModuleNames != null && childModuleNames.contains(relativePath) && sourceFO.isData()) {
                    continue;
                }

                FileObject dest = ServerFileDistributor.findOrCreateParentFolder(destRoot, relativePath);
                if (sourceFO.isData()) {
                    copyFile(sourceFO, dir, relativePath);
                } else if (dest != null && sourceFO.isFolder()) {
                    FileUtil.createFolder(dest, new File(relativePath).getName());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINER, null, e);
            String msg = NbBundle.getMessage(InitialServerFileDistributor.class, "MSG_IncrementalDeployFailed", e);
            setStatusDistributeFailed(msg);
            throw new RuntimeException(e);
        } finally {
            if (lock != null) {
                try {
                    lock.releaseLock();
                } catch (Exception ex) {
                }
            }
        }
    }

    //ServerProgress methods
    private void setStatusDistributeRunning(String message) {
        notify(createRunningProgressEvent(CommandType.DISTRIBUTE, message));
    }
    private void setStatusDistributeFailed(String message) {
        notify(createFailedProgressEvent(CommandType.DISTRIBUTE, message));
    }
    private void setStatusDistributeCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.DISTRIBUTE, message));
    }

    // Make this method speedie quick... since folks can have large
    // projects, but expect the IDE to be as fast or faster that zip or jar
    //
    private void copyFile(FileObject sourceObject, File directory, String relativePath) throws IOException {
        String ext = sourceObject.getExt();
        if (sourceObject.getSize() == 0 && ("zip".equals(ext) || "jar".equals(ext))) { // NOI18N
            // a zero length jar or zip file is NEVER ok...
            return;
        }
        File destFile = new File(directory, relativePath);
        FileOutputStream os = new FileOutputStream(destFile);
        FileInputStream fis = null;
        InputStream is = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            File sourceFile = FileUtil.toFile(sourceObject);
            if (null != sourceFile && sourceFile.canRead()) {
                // we are coming from a readable file
                fis = new FileInputStream(sourceFile);
                in = fis.getChannel();
                out = os.getChannel();

                long fileSize = sourceFile.length();
                long bufSize = Math.min(65536, fileSize);
                long offset = 0;

                do {
                    offset += in.transferTo(offset, bufSize, out);
                } while (offset < fileSize);
            } else {
                is = sourceObject.getInputStream();
                FileUtil.copy(is, os);
            }
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, null, ioe);
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, null, ioe);
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, null, ioe);
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, null, ioe);
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, null, ioe);
                }
            }
        }
    }

    private void zeroOutArchive(FileObject garbage) throws IOException {
        OutputStream fileToOverwrite = garbage.getOutputStream();
        try {
            JarOutputStream jos = new JarOutputStream(fileToOverwrite);
            try {
                jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF")); // NOI18N
                // UTF-8 guaranteed on any platform
                jos.write("Manifest-Version: 1.0\n".getBytes(StandardCharsets.UTF_8)); // NOI18N
            } finally {
                jos.close();
            }
        } finally {
            fileToOverwrite.close();
        }
    }
}
