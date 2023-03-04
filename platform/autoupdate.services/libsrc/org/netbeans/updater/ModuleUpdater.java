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

package org.netbeans.updater;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/** Class used by autoupdate module for the work with module files and
 * for installing / uninstalling modules
 *
 * @author  Petr Hrebejk, Ales Kemr, Jiri Rechtacek
 * @version
 */
public final class ModuleUpdater extends Thread {
    /** Relative name of update directory */
    private static final String DOWNLOAD_DIR_NAME = "download"; // NOI18N

    /** Relative name of directory where the .NBM files are downloaded */
    static final String DOWNLOAD_DIR = UpdaterDispatcher.UPDATE_DIR + UpdateTracking.FILE_SEPARATOR + DOWNLOAD_DIR_NAME; // NOI18N

    /** Relative name of backup directory */
    private static final String BACKUP_DIR = UpdaterDispatcher.UPDATE_DIR + UpdateTracking.FILE_SEPARATOR + "backup"; // NOI18N

    /** The name of zip entry containing netbeans files */
    public static final String UPDATE_NETBEANS_DIR = "netbeans"; // NOI18N

    /** The name of zip entry containing java_extension files */
    public static final String UPDATE_JAVA_EXT_DIR = "java_ext"; // NOI18N

    /** The name of zip entry containing files for external installer */
    public static final String UPDATE_MAIN_DIR = "main"; // NOI18N
        
    /** Name of external installer parameters file*/
    private static final String JVM_PARAMS_FILE = "main.properties"; // NOI18N

    /** Extension of the distribution files */
    public static final String NBM_EXTENSION = "nbm"; // NOI18N

    /** Extension of the OSGi distribution files */
    public static final String JAR_EXTENSION = "jar"; // NOI18N

    /** The name of the log file */
    public static final String LOG_FILE_NAME = "update.log"; // NOI18N

    /** The name of the install_later file */
    public static final String LATER_FILE_NAME = "install_later.xml"; // NOI18N
    
    public static final char SPACE = ' ';
    public static final char QUOTE = '\"';
    
    private static final String TEMP_FILE_NAME = "temporary";
    
    public static final String UPDATER_JAR = "updater.jar"; // NOI18N
    public static final String AUTOUPDATE_UPDATER_JAR_PATH = "netbeans/modules/ext/" + UPDATER_JAR; // NOI18N
    public static final String AUTOUPDATE_UPDATER_JAR_LOCALE_PATTERN = "netbeans/modules/ext/locale/updater(_[a-zA-Z0-9]+)+"; // NOI18N

    public static final String EXECUTABLE_FILES_ENTRY = "Info/executables.list";
    
    /** files that are supposed to be installed (when running inside the ide) */
    private Map<File, Collection<File>> files2clustersForInstall;

    /** Should the thread stop */
    private volatile boolean stop = false;

    /** Total length of unpacked files */
    private long totalLength;
    
    private final UpdatingContext context;

    ModuleUpdater(UpdatingContext context) {
        super("Module Updater");
        this.context = context;
    }
    
    

    /** Creates new ModuleUpdater */
    @Override
    public void run() {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ";
        try {

            checkStop();

            if (getClustersForInstall ().isEmpty ()) {
                endRun();
            }

            checkStop();

            totalLength();

            checkStop();

            unpack();
            
            for (File cluster: UpdateTracking.clusters (true)) {
                deleteAdditionalInfo (cluster);
            }

        } catch (Exception x) {
            XMLUtil.LOG.log(Level.SEVERE, "Error while upgrading", x);
        } finally {
            context.runningFinished();
        }
    }
    
    private void deleteInstallLater (File cluster) {
        File later = new File (cluster, UpdateTracking.FILE_SEPARATOR + DOWNLOAD_DIR + UpdateTracking.FILE_SEPARATOR + LATER_FILE_NAME);
        if ( later.exists() ) {
            later.delete();
            XMLUtil.LOG.info("File " + later + " deleted.");
        }
        File f = later.getParentFile ();
        while (f != null && f.delete ()) { // remove empty dirs too
            f = f.getParentFile ();
        }
    }

    private void deleteAdditionalInfo (File cluster) {
        File additional = new File (cluster, UpdateTracking.FILE_SEPARATOR + DOWNLOAD_DIR + UpdateTracking.FILE_SEPARATOR + UpdateTracking.ADDITIONAL_INFO_FILE_NAME);
        if (additional != null && additional.exists ()) {
            additional.delete ();
            XMLUtil.LOG.info("File " + additional + " deleted.");
        }
        File f = additional == null ? null : additional.getParentFile ();
        while (f != null && f.delete ()) { // remove empty dirs too
            f = f.getParentFile ();
        }
    }

    /** ends the run of update */
    void endRun() {
        stop = true;
    }

    /** checks wheter ends the run of update */
    private void checkStop() {
        if ( stop ) {
            if (context.isFromIDE ()) {
                context.unpackingFinished();
            } else {
                System.exit( 0 );
            }
        }
    }

    private void processFilesForInstall () {
        // if installOnly are null then generate all NBMs around all clusters
        if (context.forInstall() == null) {
            files2clustersForInstall = new HashMap<File, Collection<File>> ();
            for (File cluster : UpdateTracking.clusters (true)) {
                Collection<File> tmp = getModulesToInstall (cluster);
                files2clustersForInstall.put (cluster, tmp);
                // if ModuleUpdater runs 'offline' then delete install_later files
                if (!context.isFromIDE()) {
                    deleteInstallLater (cluster);
                }
            }
        } else {
            files2clustersForInstall = new HashMap<File, Collection<File>> ();
            for (File nbm : context.forInstall()) {
                if (nbm.exists()) {
                    File cluster = getCluster (nbm);
                    if (files2clustersForInstall.get (cluster) == null) {
                        files2clustersForInstall.put (cluster, new HashSet<File> ());
                    }
                    files2clustersForInstall.get (cluster).add (nbm);
                }
            }
        }
    }
    
    private static File getCluster (File nbm) {
        File cluster = null;
        try {
            // nbms are in <cluster>/update/download dir
            // but try to check it
            assert nbm.exists () : nbm + " for install exists.";
            assert nbm.getParentFile () != null : nbm + " has parent.";
            assert DOWNLOAD_DIR_NAME.equalsIgnoreCase (nbm.getParentFile ().getName ()) : nbm + " is in directory " + DOWNLOAD_DIR_NAME;
            assert nbm.getParentFile ().getParentFile () != null : nbm.getParentFile () + " has parent.";
            assert UpdaterDispatcher.UPDATE_DIR.equalsIgnoreCase (nbm.getParentFile ().getParentFile ().getName ()) :
                nbm + " is in directory " + UpdaterDispatcher.UPDATE_DIR;
            assert nbm.getParentFile ().getParentFile ().getParentFile () != null : nbm.getParentFile ().getParentFile () + " has parent.";
            
            cluster = nbm.getParentFile ().getParentFile ().getParentFile ();
        } catch (NullPointerException npe) {
            XMLUtil.LOG.log(Level.SEVERE, "getCluster (" + nbm + ") throws an exception", npe);
        }
        return cluster;
    }
    
    private Collection<File> getFilesForInstallInCluster (File cluster) {
        if (files2clustersForInstall == null) {
            processFilesForInstall ();
        }
        return files2clustersForInstall == null ? null : files2clustersForInstall.get(cluster);
    }

    private Collection<File> getClustersForInstall () {
        if (files2clustersForInstall == null) {
            processFilesForInstall ();
        }
        return files2clustersForInstall == null ? null : files2clustersForInstall.keySet();
    }

    /** Determines size of unpacked modules */
    private void totalLength () {
        totalLength = 0L;

        context.setLabel (Localization.getBrandedString ("CTL_PreparingUnpack"));
        Collection<File> allFiles = new HashSet<File> ();
        for (File c : getClustersForInstall ()) {
            allFiles.addAll (getFilesForInstallInCluster (c));
        }
        context.setProgressRange (0, allFiles.size ());

        int i = 0;
        for (File f : allFiles) {

            JarFile jarFile = null;

            try {
                if(f.getName().endsWith(".jar")) {
                    //OSGi bundle
                    totalLength += f.length();
                } else {
                jarFile = new JarFile (f);
                Enumeration<JarEntry> entries = jarFile.entries ();
                while (entries.hasMoreElements ()) {
                    JarEntry entry = entries.nextElement ();

                    checkStop ();

                    if ((entry.getName ().startsWith (UPDATE_NETBEANS_DIR) || entry.getName ().startsWith (ModuleUpdater.UPDATE_JAVA_EXT_DIR) || entry.getName ().startsWith (UPDATE_MAIN_DIR)) && ! entry.isDirectory ()) {
                        totalLength += entry.getSize ();
                    }
                }
                }
                context.setProgressValue (i ++);
            } catch (java.io.IOException e) {
                XMLUtil.LOG.log(Level.WARNING, "Cannot count size of entries in " + f, e);
            } finally {
                try {
                    if (jarFile != null) {
                        jarFile.close ();
                    }
                } catch (java.io.IOException e) {
                    // We can't close the file do nothing
                    XMLUtil.LOG.log(Level.WARNING, "While closing " + jarFile + " input stream", e); // NOI18N
                }
            }
        }
    }



    /** Unpack the distribution files into update directory */

    private void unpack ()  {
        long bytesRead = 0L;
        boolean hasMainClass;

        context.setLabel( "" ); // NOI18N
        context.setProgressRange( 0, totalLength );
        
        ArrayList<UpdateTracking> allTrackings = new ArrayList<UpdateTracking> ();
        Map<ModuleUpdate, UpdateTracking.Version> l10ns = 
                new HashMap<ModuleUpdate, UpdateTracking.Version>();
        
        for (File cluster : getClustersForInstall ()) {
            UpdateTracking tracking = UpdateTracking.getTracking (cluster, true, context);
            if (tracking == null) {
                throw new RuntimeException ("No update_tracking file in cluster " + cluster);
            }
            allTrackings.add (tracking);

            int installedNBMs = 0;
            for (File nbm : getFilesForInstallInCluster (cluster)) {
                installedNBMs++;
                
                UpdateTracking.Version version;
                UpdateTracking.Module modtrack;
                
                context.setLabel( Localization.getBrandedString("CTL_UnpackingFile") + "  " + nbm.getName() ); //NOI18N
                XMLUtil.LOG.info("780: " + Localization.getBrandedString("CTL_UnpackingFile") + " " + nbm.getName()); //NOI18N
                context.unpackingIsRunning ();
                
                ModuleUpdate mu;
                try {
                    mu = new ModuleUpdate (nbm);
                } catch (RuntimeException re) {
                    if (nbm.exists ()) {
                        XMLUtil.LOG.info("Deleteing file: " + nbm);
                        if (! nbm.delete ()) {
                            XMLUtil.LOG.log(Level.WARNING, "File " + nbm + " cannot be deleted. Propably file lock on the file."); // NOI18N
                            assert false : "Error: File " + nbm + " cannot be deleted. Propably file lock on the file.";
                            nbm.deleteOnExit ();
                        } else {
                            XMLUtil.LOG.info("File " + nbm + " deleted.");
                        }
                    }
                    continue;
                }
                assert mu != null : "Module update is not null for file: " + nbm; // NOI18N
                if ( mu.isL10n() ) {
                    modtrack = null;
                    version = tracking.createVersion( "0" ); // NOI18N
                    l10ns.put( mu, version );
                } else {
                    modtrack = tracking.readModuleTracking (mu.getCodenamebase (), true);
                    // find origin for file
                    UpdateTracking.AdditionalInfo info = UpdateTracking.getAdditionalInformation (cluster, context);
                    String origin = info != null && info.getSource (nbm.getName ()) != null ?
                        info.getSource (nbm.getName ()) : UpdateTracking.UPDATER_ORIGIN;
                    version = modtrack.addNewVersion (mu.getSpecification_version (), origin);
                }
                // input streams should be released, but following is needed
                //System.gc();

                hasMainClass = false;
                context.setProgressValue( bytesRead );
                JarFile jarFile = null;

                try {
                    jarFile = new JarFile (nbm);
                    Enumeration<JarEntry> entries = jarFile.entries();
                    final Manifest manifest = jarFile.getManifest();
                    String symbolicName = manifest != null ? ModuleUpdate.extractCodeName(manifest.getMainAttributes()) : null;
                    if (symbolicName != null) {
                        //OSGi bundle
                        File osgiJar = nbm;
                        
                        File destFile = new File(cluster, "modules/" + osgiJar.getName());
                        if (destFile.exists()) {
                            File bckFile = new File(getBackupDirectory(cluster), osgiJar.getName());
                            bckFile.getParentFile().mkdirs();
                            copyStreams(new FileInputStream(destFile), context.createOS(bckFile), -1);
                            XMLUtil.LOG.info("Backup file " + destFile + " to " + bckFile);
                            if (!destFile.delete() && isWindows()) {
                                trickyDeleteOnWindows(destFile);
                            } else {
                                XMLUtil.LOG.info("File " + destFile + " deleted.");
                            }
                        } else {
                            destFile.getParentFile().mkdirs();
                        }

                        bytesRead = copyStreams(new FileInputStream(osgiJar), context.createOS(destFile), bytesRead);
                        XMLUtil.LOG.info("Copied file " + osgiJar + " to " + destFile);
                        long crc = UpdateTracking.getFileCRC(destFile);
                        version.addFileWithCrc("modules/" + osgiJar.getName(), Long.toString(crc));
                        //create config/Modules/cnb.xml
                        File configDir = new File (new File (cluster, ModuleDeactivator.CONFIG), ModuleDeactivator.MODULES); // NOI18N
                        String configFileName = symbolicName.replace ('.', '-') + ".xml";
                        File configFile = new File(configDir, configFileName);
                        if (configFile.exists()) {
                            long configFileCRC = UpdateTracking.getFileCRC(configFile);
                            version.addFileWithCrc("config/Modules/" + configFileName, Long.toString(configFileCRC));
                        }

                        context.setProgressValue(bytesRead);
                        modtrack.setOSGi(true);
                    } else {
                        //NBM
                        List <String> executableFiles = readExecutableFilesList(jarFile);
                        List <File> filesToChmod = new ArrayList <File> ();
                        while( entries.hasMoreElements() ) {
                            JarEntry entry = entries.nextElement();
                            checkStop();
                            if ( entry.getName().startsWith( UPDATE_NETBEANS_DIR ) ) {
                                if (! entry.isDirectory ()) {
                                    String pathTo = entry.getName().substring(UPDATE_NETBEANS_DIR.length() + 1);
                                    File destFile = new File (cluster, pathTo);
                                    if (AUTOUPDATE_UPDATER_JAR_PATH.equals (entry.getName ()) ||
                                            entry.toString().matches(AUTOUPDATE_UPDATER_JAR_LOCALE_PATTERN)) {
                                        
                                        // #220807 - NoClassDefFoundError: updater/XMLUtil
                                        version.addFileWithCrc(pathTo, Long.toString(destFile.exists() ? UpdateTracking.getFileCRC(destFile) : 0));
                                        
                                        if (destFile.exists()) {
                                            // skip updater.jar
                                            continue;
                                        }
                                    }
                                    // path without netbeans prefix
                                    if ( destFile.exists() ) {
                                        File bckFile = new File( getBackupDirectory (cluster), entry.getName() );
                                        bckFile.getParentFile ().mkdirs ();
                                        copyStreams( new FileInputStream( destFile ), context.createOS( bckFile ), -1 );
                                        XMLUtil.LOG.info("Backup file " + destFile + " to " + bckFile);
                                        if (!destFile.delete() && isWindows()) {
                                            trickyDeleteOnWindows(destFile);
                                        } else {
                                            XMLUtil.LOG.info("File " + destFile + " deleted.");
                                        }
                                    } else {
                                        destFile.getParentFile ().mkdirs ();
                                    }

                                    long crc;
                                    if (pathTo.endsWith(".external")) {
                                        File downloaded = new File(destFile.getParentFile(), destFile.getName().substring(0, destFile.getName().lastIndexOf(".external")));
                                        final InputStream spec = jarFile.getInputStream(entry);
                                        pathTo = pathTo.substring(0, pathTo.length() - ".external".length());
                                        long expectedCRC = externalDownload(spec, nbm);
                                        File external = new File(nbm + "." + Long.toHexString(expectedCRC));
                                        InputStream is = new FileInputStream(external);
                                        try {
                                            spec.close();
                                            OutputStream os = context.createOS(downloaded);
                                            try {
                                                XMLUtil.LOG.info("810: " + Localization.getBrandedString("CTL_DownloadingFile") + " " + downloaded); //NOI18N
                                                bytesRead = copyStreams(is, os, -1);
                                                XMLUtil.LOG.info("Copied external file " + external + " to " + downloaded);
                                            } finally {
                                                os.close();
                                            }
                                        } finally {
                                            external.delete();
                                            XMLUtil.LOG.info("File " + external + " deleted.");
                                            is.close();
                                        }
                                        crc = UpdateTracking.getFileCRC(downloaded);
                                        if (crc != expectedCRC) {
                                            downloaded.delete();
                                            XMLUtil.LOG.info("File " + downloaded + " deleted.");
                                            throw new IOException("Wrong CRC for " + downloaded);
                                        }
                                    } else {
                                        bytesRead = copyStreams( jarFile.getInputStream( entry ), context.createOS( destFile ), bytesRead );
                                        XMLUtil.LOG.info("Copied file " + jarFile.getName() + ":" + entry + " to " + destFile);
                                        crc = entry.getCrc();
                                    }
                                    if(executableFiles.contains(pathTo)) {
                                        filesToChmod.add(destFile);
                                    }
                                    if(pathTo.endsWith(".jar.pack.gz") &&
                                            jarFile.getEntry(entry.getName().substring(0, entry.getName().lastIndexOf(".pack.gz")))==null) {
                                         //check if file.jar.pack.gz does not exit for file.jar - then unpack current .pack.gz file
                                        File unpacked = new File(destFile.getParentFile(), destFile.getName().substring(0, destFile.getName().lastIndexOf(".pack.gz")));
                                        unpack200(destFile, unpacked);
                                        destFile.delete();
                                        XMLUtil.LOG.info("File " + destFile + " deleted.");
                                        pathTo = pathTo.substring(0, pathTo.length() - ".pack.gz".length());
                                        crc = UpdateTracking.getFileCRC(unpacked);
                                    }
                                    if ( mu.isL10n() ) {
                                        version.addL10NFileWithCrc( pathTo, Long.toString(crc), mu.getSpecification_version());
                                    } else {
                                        version.addFileWithCrc( pathTo, Long.toString(crc));
                                    }

                                    context.setProgressValue( bytesRead );
                                }
                            } else if ( entry.getName().startsWith( UPDATE_MAIN_DIR )&&
                                      !entry.isDirectory() ) {
                                // run main
                                String pathTo = entry.getName().substring(UPDATE_MAIN_DIR.length() + 1);
                                File destFile = new File (getMainDirectory (cluster), pathTo);
                                if(executableFiles.contains(pathTo)) {
                                    filesToChmod.add(destFile);
                                }
                                destFile.getParentFile ().mkdirs ();
                                hasMainClass = true;
                                bytesRead = copyStreams( jarFile.getInputStream( entry ), context.createOS( destFile ), bytesRead );
                                XMLUtil.LOG.info("Copied file " + jarFile.getName() + ":" + entry + " to " + destFile);
                                context.setProgressValue( bytesRead );
                            }
                        }
                        chmod(filesToChmod);
                        if ( hasMainClass ) {                    
                            MainConfig mconfig = new MainConfig (getMainDirString (cluster) + UpdateTracking.FILE_SEPARATOR + JVM_PARAMS_FILE, cluster);
                            if (mconfig.isValid()) {
                                String java_path = System.getProperty ("java.home") + UpdateTracking.FILE_SEPARATOR
                                    + "bin"  + UpdateTracking.FILE_SEPARATOR + "java";                              // NOI18N
                                java_path = quoteString( java_path );
                                String torun = java_path + " -cp " + quoteString (getMainDirString (cluster) + mconfig.getClasspath() ) + mconfig.getCommand();  // NOI18N
                                startCommand(torun);

                                deleteDir( getMainDirectory (cluster) );
                            }
                        }
                    }
                }
                catch ( java.io.IOException e ) {
                    // Ignore non readable files
                    XMLUtil.LOG.log(Level.INFO, "Ignore non-readable files ", e);
                }
                finally {
                    try {
                        if ( jarFile != null ) {
                            jarFile.close();
                        }
                    }
                    catch ( java.io.IOException e ) {
                        // We can't close the file do nothing
                        // XMLUtil.LOG.info("Can't close : " + e ); // NOI18N
                    }
                    //XMLUtil.LOG.info("Dleting :" + nbmFiles[i].getName() + ":" + nbmFiles[i].delete() ); // NOI18N

                    if (! nbm.delete ()) {
                        XMLUtil.LOG.log(Level.WARNING, "Error: Cannot delete {0}", nbm); // NOI18N
                        nbm.deleteOnExit ();
                    } else {
                        XMLUtil.LOG.info("File " + nbm + " deleted.");
                    }
                }
                if (! mu.isL10n ()) {
                    modtrack.write ();
                    modtrack.writeConfigModuleXMLIfMissing ();
                }
            }
            
            if (installedNBMs > 0) {
                UpdaterDispatcher.touchLastModified (cluster);
            }
        }
        
        for (UpdateTracking t: allTrackings) {
            // update_tracking of l10n's
            for (Map.Entry<ModuleUpdate, UpdateTracking.Version> entry: l10ns.entrySet()) {
                ModuleUpdate mod = entry.getKey();
                UpdateTracking.Version version = entry.getValue();
                UpdateTracking.Module modtrack = t.readModuleTracking( 
                    mod.getCodenamebase(), 
                    true 
                );
                modtrack.addL10NVersion( version );
                modtrack.write();
            }
            t.deleteUnusedFiles ();            
        }
    }
    
    private boolean unpack200(File src, File dest) {
        String unpack200 = "unpack200" + (isWindows() ? ".exe" : "");
        File unpack200Executable = findUnpack200Executable(unpack200);
        ProcessBuilder pb = new ProcessBuilder(unpack200Executable.getAbsolutePath(), src.getAbsolutePath(), dest.getAbsolutePath());
        pb.directory(src.getParentFile());
        int result = 1;
        try {
            //maybe reuse start() method here?
            Process process = pb.start();
            //TODO: Need to think of unpack200/lvprcsrv.exe issues
            //https://netbeans.org/bugzilla/show_bug.cgi?id=117334
            //https://netbeans.org/bugzilla/show_bug.cgi?id=119861
            result = process.waitFor();
            process.destroy();
            XMLUtil.LOG.info("Unpack " + src + " to " + dest);
        } catch (IOException e) {
            XMLUtil.LOG.log(Level.WARNING, null, e);
        } catch (InterruptedException e) {
            XMLUtil.LOG.log(Level.WARNING, null, e);
        }
        return result == 0;
    }

    private File findUnpack200Executable(String unpack200) {
        File unpack200Executable = new File(new File(System.getProperty("java.home"), "bin"), unpack200);
        if (!unpack200Executable.canExecute()) {
            for (File clusterRoot : UpdateTracking.clusters(true)) {
                File uiConfig = new File(new File(new File(new File(new File(new File(new File(
                        clusterRoot, "config"), "Preferences"), "org"), "netbeans"), "modules"), // NOI18N
                        "autoupdate"), "services.properties"); // NOI18N
                if (uiConfig.canRead()) {
                    Properties p = new Properties();
                    try (FileInputStream is = new FileInputStream(uiConfig)) {
                        p.load(is);
                    } catch (IOException ex) {
                        // go on
                    }
                    String unpackKey = p.getProperty("unpack200"); // NOI18N
                    if (unpackKey != null) {
                        File unpackKeyFile = new File(unpackKey);
                        if (unpackKeyFile.canExecute()) {
                            unpack200Executable = unpackKeyFile;
                            break;
                        }
                    }
                }
            }
        }
        return unpack200Executable;
    }

    private List<String> readExecutableFilesList(JarFile jarFile) {
        List<String> list = new ArrayList<String>();
        JarEntry executableFilesEntry = jarFile.getJarEntry(EXECUTABLE_FILES_ENTRY);
        if (executableFilesEntry != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(executableFilesEntry), StandardCharsets.UTF_8));
                String s;
                while ((s = reader.readLine()) != null) {
                    list.add(s);
                }
                reader.close();
            } catch (Exception e) {
                XMLUtil.LOG.log(Level.WARNING, null, e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return list;
    }

    private void chmod(List<File> executableFiles) {
        if (isWindows() || executableFiles.isEmpty()) {
            return;
        }

        for (File executableFile : executableFiles) {
            executableFile.setExecutable(true, false);
         }
    }
        
    public static boolean trickyDeleteOnWindows(File destFile) {
        assert isWindows() : "Call it only on Windows but system is " + System.getProperty("os.name");
        File f = new File(destFile.getParentFile(), destFile.getName());
        assert f.exists() : "The file " + f + " must exists.";        
        try {
            File tmpFile = File.createTempFile(TEMP_FILE_NAME, null, f.getParentFile());
            if (tmpFile.delete()) {
                f.renameTo(tmpFile);
                XMLUtil.LOG.info("File " + f + " renamed to " + tmpFile);
                tmpFile.deleteOnExit ();
                XMLUtil.LOG.info("Locked file " + tmpFile + " will be deleted on exit.");
            } else {
                XMLUtil.LOG.info("File " + tmpFile + " was deleted.");
            }
        } catch (IOException ex) {
            //no special handling needed
        }
        return !f.exists();
    }
    
    public static boolean isWindows() {
        String os = System.getProperty("os.name"); // NOI18N
        return (os != null && os.toLowerCase().startsWith("windows"));//NOI18N
    }
    
    private void startCommand(String torun) {
        Runtime runtime=Runtime.getRuntime();
        Process proces;            
        try {
            proces=runtime.exec(parseParameters( torun ));
            final Process proc2 = proces;
            new Thread() {
                @Override
                public void run() {
                    try {
                        InputStreamReader stream= new InputStreamReader (proc2.getErrorStream());
                        BufferedReader reader= new BufferedReader(stream);
                        String vystup;
                        do {
                            vystup = reader.readLine();
                            if (vystup!=null) {
                                XMLUtil.LOG.info(vystup);
                            }
                        } while (vystup != null);
                    } catch (Exception e) {
                        XMLUtil.LOG.log(Level.INFO, null, e);
                  }
                }
            }.start();
            int x=proces.waitFor();
        }
        catch (Exception e){
            XMLUtil.LOG.log(Level.INFO, null, e);
        }
    }
    
    /** The directory where to backup old versions of modules */
    public File getBackupDirectory (File activeCluster) {
        // #72960: Backup file created in wrong cluster
        File backupDirectory = new File (activeCluster, BACKUP_DIR);
        if (! backupDirectory.isDirectory ()) {
            backupDirectory.mkdirs();
        }

        return backupDirectory;
    }

    /** Gets the netbeans directory */
    private static File getMainDirectory (File activeCluster) {
        // #72918: Post-install cannot write into platform cluster
        File mainDirectory = new File (activeCluster, UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.UPDATE_DIR + UpdateTracking.FILE_SEPARATOR + UPDATE_MAIN_DIR);
        if (! mainDirectory.isDirectory ()) {
            mainDirectory.mkdirs();
        }

        return mainDirectory;
    }
    
    private static String getMainDirString (File activeCluster) {
        return getMainDirectory (activeCluster).getPath ();
    }
    
     /** Quotes string correctly, eg. removes all quotes from the string and adds 
      * just one at the start and
      * second one at the end.
      * @param s string to be quoted
      * @return correctly quoted string
      */
     public static String quoteString(String s) {
         if ( s.indexOf( SPACE ) > -1 ) {
             StringBuilder sb = new StringBuilder(s);
             int i = 0;
             while ( i < sb.length() ) {
                 if ( sb.charAt(i) == QUOTE ) {
                     sb.deleteCharAt( i );
                 } else {
                     i++;
                 }
             }
             sb.insert( 0, QUOTE );
             sb.append( QUOTE );
             return sb.toString();
         }
         return s;
     }    

    /**
     * It takes the current progress value so it can update progress
     * properly, and also return the new progress value after the
     * copy is done.
     *
     * @param progressVal The current progress bar value.  If this is
     *          negative, we don't want to update the progress bar.
     */
    private long copyStreams( InputStream src, OutputStream dest,
                               long progressVal ) throws java.io.IOException {

        BufferedInputStream bsrc = new BufferedInputStream( src );
        BufferedOutputStream bdest = new BufferedOutputStream( dest );

        int count = 0;

        int c;
        byte [] bytes = new byte [8192];

        try {
            while( ( c = bsrc.read(bytes) ) != -1 ) {
                bdest.write(bytes, 0, c);
                count+=c;
                if ( count > 8500 ) {
                    if (progressVal >= 0) {
                        progressVal += count;
                        context.setProgressValue( progressVal );
                    }

                    count = 0;
                    checkStop();
                }
            }
            // Just update the value, no need to update the
            // GUI yet.   Caller can do that.
            if (progressVal >= 0) {
                progressVal += count;
            }
        }
        finally {
            bsrc.close();
            bdest.close();
            src.close();
            dest.close();
        }
        return progressVal;

    }

    private void deleteDir(File dir) {
        File[] files=dir.listFiles();
        for( int j = 0; j < files.length; j++ ) {
            if ( files[j].isDirectory() ) {
                deleteDir( files[j] );
            }
            if (! files[j].delete()) {
                    XMLUtil.LOG.log(Level.WARNING, "Cannot delete {0}", files [j]); //NOI18N
                    assert false : "Cannot delete " + files [j];
            } else {
                XMLUtil.LOG.info("File " + files[j] + " deleted.");
            }
        }
    }

    // [Copied from org.openide.util.Utilities]
    /**
     * Parses parameters from a given string in shell-like manner.
     * Users of the Bourne shell (e.g. on Unix) will already be familiar
     * with the behavior.
     * For example, when using {@link org.openide.execution.NbProcessDescriptor}
     * you should be able to:
     * <ul>
     * <li>Include command names with embedded spaces, such as 
     * <code>c:\Program Files\jdk\bin\javac</code>.
     * <li>Include extra command arguments, such as <code>-Dname=value</code>.
     * <li>Do anything else which might require unusual characters or
     *     processing. For example:
     * <p><code><pre>
     * "c:\program files\jdk\bin\java" -Dmessage="Hello /\\/\\ there!" -Xmx128m
     * </pre></code>
     * <p>This example would create the following executable name and arguments: 
     * <ol>
     * <li> <code>c:\program files\jdk\bin\java</code>
     * <li> <code>-Dmessage=Hello /\/\ there!</code>
     * <li> <code>-Xmx128m</code>
     * </ol>
     * Note that the command string does not escape its backslashes--under the assumption
     * that Windows users will not think to do this, meaningless escapes are just left
     * as backslashes plus following character.
     * </ul>
     * <em>Caveat</em>: even after parsing, Windows programs (such as
     * the Java launcher)
     * may not fully honor certain
     * characters, such as quotes, in command names or arguments. This is because programs
     * under Windows frequently perform their own parsing and unescaping (since the shell
     * cannot be relied on to do this). On Unix, this problem should not occur.
     * @param s a string to parse
     * @return an array of parameters
     */
     private static String[] parseParameters(String s) {
         int NULL = 0x0;  // STICK + whitespace or NULL + non_"
         int INPARAM = 0x1; // NULL + " or STICK + " or INPARAMPENDING + "\ // NOI18N
         int INPARAMPENDING = 0x2; // INPARAM + \
         int STICK = 0x4; // INPARAM + " or STICK + non_" // NOI18N
         int STICKPENDING = 0x8; // STICK + \
        @SuppressWarnings("UseOfObsoleteCollectionType")
         Vector<String> params = new Vector<String>(5,5);
         char c;
 
         int state = NULL;
         StringBuilder buff = new StringBuilder(20);
         int slength = s.length();
         for (int i = 0; i < slength; i++) {
             c = s.charAt(i);
             if (Character.isWhitespace(c)) {
                 if (state == NULL) {
                     if (buff.length () > 0) {
                         params.addElement(buff.toString());
                         buff.setLength(0);
                     }
                 } else if (state == STICK) {
                     params.addElement(buff.toString());
                     buff.setLength(0);
                     state = NULL;
                 } else if (state == STICKPENDING) {
                     buff.append('\\');
                     params.addElement(buff.toString());
                     buff.setLength(0);
                     state = NULL;
                 } else if (state == INPARAMPENDING) {
                     state = INPARAM;
                     buff.append('\\');
                     buff.append(c);
                 } else {    // INPARAM
                     buff.append(c);
                 }
                 continue;
             }
 
             if (c == '\\') {
                 if (state == NULL) {
                     ++i;
                     if (i < slength) {
                         char cc = s.charAt(i);
                         if (cc == '"' || cc == '\\') {
                             buff.append(cc);
                         } else if (Character.isWhitespace(cc)) {
                             buff.append(c);
                             --i;
                         } else {
                             buff.append(c);
                             buff.append(cc);
                         }
                     } else {
                         buff.append('\\');
                         break;
                     }
                     continue;
                 } else if (state == INPARAM) {
                     state = INPARAMPENDING;
                 } else if (state == INPARAMPENDING) {
                     buff.append('\\');
                     state = INPARAM;
                 } else if (state == STICK) {
                     state = STICKPENDING;
                 } else if (state == STICKPENDING) {
                     buff.append('\\');
                     state = STICK;
                 }
                 continue;
             }
 
             if (c == '"') {
                 if (state == NULL) {
                     state = INPARAM;
                 } else if (state == INPARAM) {
                     state = STICK;
                 } else if (state == STICK) {
                     state = INPARAM;
                 } else if (state == STICKPENDING) {
                     buff.append('"');
                     state = STICK;
                 } else { // INPARAMPENDING
                     buff.append('"');
                     state = INPARAM;
                 }
                 continue;
             }
 
             if (state == INPARAMPENDING) {
                 buff.append('\\');
                 state = INPARAM;
             } else if (state == STICKPENDING) {
                 buff.append('\\');
                 state = STICK;
             }
             buff.append(c);
         }
         // collect
         if (state == INPARAM) {
             params.addElement(buff.toString());
         } else if ((state & (INPARAMPENDING | STICKPENDING)) != 0) {
             buff.append('\\');
             params.addElement(buff.toString());
         } else { // NULL or STICK
             if (buff.length() != 0) {
                 params.addElement(buff.toString());
             }
         }
         String[] ret = new String[params.size()];
         params.copyInto(ret);
         return ret;
     }

    private long externalDownload(InputStream spec, File nbm) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(spec));
        for (;;) {
            String line = br.readLine();
            if (line == null) {
                throw new IOException("No CRC in the .external file!");
            }
            if (line.startsWith("CRC:")) {
                return Long.parseLong(line.substring(4).trim());
            }
        }
    }


    
    /** read jvm parameters from jvm parameters file */
    static class MainConfig extends Object {
        
        /** The names of properties from jvm parameters file */
        private final String PAR_MAIN = "mainClass";               // NOI18N
        private final String PAR_RELCP = "relativeClassPath";      // NOI18N
        private final String PAR_JVMPAR = "jvm.parameters";        // NOI18N
        private final String PAR_MAINARGS = "mainClass.arguments"; // NOI18N
        
        /** The names of variables allow to use in jvm parameters file */
        private final String VAR_IDE_HOME = "%IDE_HOME%";          // NOI18N
        private final String VAR_IDE_USER = "%IDE_USER%";          // NOI18N
        private final String VAR_FILE_SEPARATOR = "%FS%";          // NOI18N        
        private final String VAR_PATH_SEPARATOR = "%PS%";          // NOI18N        
        private final String VAR_JAVA_HOME = "%JAVA_HOME%";        // NOI18N        
    
        /** joined all parameters of jvm java command */
        private String parameters = ""; // NOI18N
        private String classpath = ""; // NOI18N
        
        /** is jvm parameters file in valid stucture */
        private boolean valid = false;
        private final File activeCluster;
        
        public MainConfig (String spath, File activeCluster) {
            valid = readParms(spath);
            this.activeCluster = activeCluster;
        }
        
        /** returns all parameters needed by jvm java command */
        public String getCommand() {
            return parameters;
        }
        
        /** returns all parameters needed by jvm java command */
        public String getClasspath() {
            return classpath;
        }
        
        /** is jvm parameters file in valid stucture */
        public boolean isValid() {
            return valid;
        }
        
        /** read jvm parameters from jvm parameters file */
        @SuppressWarnings("empty-statement")
        private boolean readParms(String spath) {
            Properties details = new Properties();
            FileInputStream fis = null;
            try {
                details.load(fis = new FileInputStream(spath)); // NOI18N
            } catch (IOException e) {            
                return false;
            } finally {
                if (fis != null) {
                    try { fis.close(); } catch (IOException e) { /* ignore */ }
                };
            }
            
            String mainclass;
            String relpath;
            String jvmparms;
            String mainargs;        
        
            relpath = details.getProperty(PAR_RELCP,null);
            if (relpath != null) {
                relpath = replaceVars( relpath );
                StringTokenizer token = new StringTokenizer( relpath, UpdateTracking.PATH_SEPARATOR, false );
                while ( token.hasMoreTokens() ) {
                    classpath = classpath + UpdateTracking.PATH_SEPARATOR + changeRelative( token.nextToken() );
                }
            }
        
            parameters = "";
            jvmparms = details.getProperty(PAR_JVMPAR,null);
            if (jvmparms != null) {
                parameters = parameters + " " + jvmparms;  // NOI18N
            }
            
            mainclass = details.getProperty(PAR_MAIN,null);
            if (mainclass == null) {
                return false;
            } else {
                parameters = parameters + " " + mainclass;
            }  // NOI18N
            
            mainargs = details.getProperty(PAR_MAINARGS,null);
            if (mainargs != null) {
                parameters = parameters + " " + mainargs;  // NOI18N
            }
            
            parameters = replaceVars( parameters );
            return true;            
        }
        
        private String replaceVars(String original) {
            original = replaceAll(original, VAR_IDE_HOME,
                UpdateTracking.getPlatformDir () == null ? "" : UpdateTracking.getPlatformDir ().getPath());
            original = replaceAll(original, VAR_IDE_USER,
                UpdateTracking.getUserDir () == null ? "" : UpdateTracking.getUserDir ().getPath());
            original = replaceAll(original, VAR_FILE_SEPARATOR,
                UpdateTracking.FILE_SEPARATOR);            
            original = replaceAll(original, VAR_PATH_SEPARATOR,
                UpdateTracking.PATH_SEPARATOR);            
            original = replaceAll(original, VAR_JAVA_HOME,
                System.getProperty ("java.home")); // NOI18N
            return original;
        }
        
        private String changeRelative(String path) {
            if ( new File( path ).isAbsolute() ) {
                return path;
            } else {
                return getMainDirString (this.activeCluster) + UpdateTracking.FILE_SEPARATOR + path;
            }
        }
        
        
        /** replace all occurrences of String what by String repl in the String sin */
        private String replaceAll(String sin, String what, String repl) {
            StringBuilder sb = new StringBuilder(sin);
            int i = sb.toString().indexOf(what);
            int len = what.length();
            while ( i > -1 ) {
                sb.replace(i,i + len,repl);
                i = sb.toString().indexOf(what,i+1);                
            }

            return sb.toString();
        }
    }
    
    /** Compute the list of modules that should be installed into this 
     * cluster.
     * @param File root of cluster
     * @return List<File> of nbm files
     */
    public static Set<File> getModulesToInstall (File cluster) {
        
        class NbmFilter implements java.io.FilenameFilter {
            @Override
            public boolean accept (File dir, String name) {
                return name.endsWith (ModuleUpdater.NBM_EXTENSION) || name.endsWith (ModuleUpdater.JAR_EXTENSION);
            }
        }
        
        File idir = new File (cluster, ModuleUpdater.DOWNLOAD_DIR);
        File[] arr = idir.listFiles (new NbmFilter ());
        
        if (arr == null) {
            return Collections.emptySet ();
        } else {
            return new HashSet<File> (Arrays.asList (arr));
        }
    }

}
