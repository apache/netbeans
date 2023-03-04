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

package org.netbeans.modules.profiler;

import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.results.ResultsSnapshot;
import org.netbeans.lib.profiler.results.coderegion.CodeRegionResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot.NoDataAvailableException;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.openide.util.NbBundle;
import java.io.*;
import java.lang.management.ThreadInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.management.openmbean.CompositeData;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.openide.util.Lookup;

@NbBundle.Messages({
    "LoadedSnapshot_IllegalSnapshotDataMsg=Illegal snapshot data",
    "LoadedSnapshot_InvalidSnapshotFileMsg=Not a valid NetBeans Profiler snapshot file",
    "LoadedSnapshot_UnsupportedSnapshotVersionMsg=Unsupported file version",
    "LoadedSnapshot_WrongSnapshotTypeMsg=Incorrect snapshot type",
    "LoadedSnapshot_CannotReadSnapshotDataMsg=Cannot read snapshot data",
    "LoadedSnapshot_CannotReadSettingsDataMsg=Cannot read settings data",
    "LoadedSnapshot_UnrecognizedSnapshotTypeMsg=Unrecognized snapshot type",
    "LoadedSnapshot_SnapshotDataCorruptedMsg=Snapshot data corrupted",
    "LoadedSnapshot_SnapshotFileShortMsg=File too short",
    "LoadedSnapshot_SnapshotFileCorrupted=Snapshot file corrupted",
    "LoadedSnapshot_SnapshotFileCorruptedReason=Snapshot file is corrupted: {0}",
    "LoadedSnapshot_OutOfMemoryLoadingMsg=Not enough memory to load snapshot.\n\nTo avoid this error, increase the -Xmx value\nin the etc/netbeans.conf file in NetBeans IDE installation and restart the IDE."
})
public class LoadedSnapshot {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(LoadedSnapshot.class.getName());

    //  private static final boolean DEBUG = true; //System.getProperty("org.netbeans.modules.profiler.LoadedSnapshot") != null; // TODO [m7] : change to property
    public static final int SNAPSHOT_TYPE_UNKNOWN = 0;
    public static final int SNAPSHOT_TYPE_CPU = 1;
    public static final int SNAPSHOT_TYPE_CODEFRAGMENT = 2;
    public static final int SNAPSHOT_TYPE_MEMORY_ALLOCATIONS = 4;
    public static final int SNAPSHOT_TYPE_MEMORY_LIVENESS = 8;
    public static final int SNAPSHOT_TYPE_MEMORY_SAMPLED = 16;
    public static final int SNAPSHOT_TYPE_CPU_JDBC = 32;
    public static final int SNAPSHOT_TYPE_MEMORY = SNAPSHOT_TYPE_MEMORY_ALLOCATIONS | SNAPSHOT_TYPE_MEMORY_LIVENESS | SNAPSHOT_TYPE_MEMORY_SAMPLED;
    public static final String PROFILER_FILE_MAGIC_STRING = "nBpRoFiLeR"; // NOI18N
    private static final byte SNAPSHOT_FILE_VERSION_MAJOR = 1;
    private static final byte SNAPSHOT_FILE_VERSION_MINOR = 2;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private File file;
    private ProfilingSettings settings;
    private Lookup.Provider project = null;
    private ResultsSnapshot snapshot;
    private String userComments = ""; // NOI18N
    private boolean saved = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new LoadedSnapshot.
     *
     * @param snapshot The actual snapshot results data
     * @param settings ProfilingSettings used to obtain this snapshot
     * @param file     The FileObject in which this snapshot is saved or null if it is not yet saved (in-memory only)
     */
    public LoadedSnapshot(ResultsSnapshot snapshot, ProfilingSettings settings, File file, Lookup.Provider project) {
        if (snapshot == null) {
            throw new IllegalArgumentException();
        }

        if (settings == null) {
            throw new IllegalArgumentException();
        }

        this.snapshot = snapshot;
        this.settings = settings;
        this.file = file;
        this.project = project;
    }

    private LoadedSnapshot() {
        // for persistence only
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setFile(File file) {
        this.file = file;
        saved = true;
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (SnapshotResultsWindow.hasSnapshotWindow(LoadedSnapshot.this))
                        SnapshotResultsWindow.get(LoadedSnapshot.this).refreshTabName();
                }
            });
    }

    /**
     * @return The File in which this snapshot is saved or null if it is not yet saved (in-memory only)
     */
    public File getFile() {
        return file;
    }

    public Lookup.Provider getProject() {
        return project;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }
    
    public void setUserComments(String userComments) {
        if (!this.userComments.equals(userComments)) {
            this.userComments = userComments;
            setSaved(false);
        }
    }
    
    public String getUserComments() {
        return userComments;
    }

    /**
     * @return ProfilingSettings used to obtain this snapshot
     */
    public ProfilingSettings getSettings() {
        return settings;
    }

    /**
     * @return The actual snapshot results data
     */
    public ResultsSnapshot getSnapshot() {
        return snapshot;
    }

    public int getType() {
        if (snapshot instanceof CPUResultsSnapshot) {
            return SNAPSHOT_TYPE_CPU;
        } else if (snapshot instanceof CodeRegionResultsSnapshot) {
            return SNAPSHOT_TYPE_CODEFRAGMENT;
        } else if (snapshot instanceof LivenessMemoryResultsSnapshot) {
            return SNAPSHOT_TYPE_MEMORY_LIVENESS;
        } else if (snapshot instanceof AllocMemoryResultsSnapshot) {
            return SNAPSHOT_TYPE_MEMORY_ALLOCATIONS;
        } else if (snapshot instanceof SampledMemoryResultsSnapshot) {
            return SNAPSHOT_TYPE_MEMORY_SAMPLED;
        } else if (snapshot instanceof JdbcResultsSnapshot) {
            return SNAPSHOT_TYPE_CPU_JDBC;
        } else {
            throw new IllegalStateException(Bundle.LoadedSnapshot_IllegalSnapshotDataMsg());
        }
    }

    /**
     * Will load a snapshot into memory from the provided stream and return the snapshot representation.
     *
     * @param dis Stream to read from, typically data from file.
     * @return The loaded snapshot or null if failed to load (has already been reported to the user)
     * @throws IOException If unexpected error occurred while loading (should be reported to the user)
     */
    public static LoadedSnapshot loadSnapshot(DataInputStream dis)
                                       throws IOException {
        dis.mark(100);
        try {
            LoadedSnapshot ls = new LoadedSnapshot();

            if (ls.load(dis)) {
                return ls;
            } else {
                return null;
            }
        } catch (IOException ex) {
            if (Bundle.LoadedSnapshot_InvalidSnapshotFileMsg().equals(ex.getMessage())) {
                dis.reset();
                return loadSnapshotFromStackTraces(dis);
            }
            throw ex;
        }
    }

    private static LoadedSnapshot loadSnapshotFromStackTraces(DataInputStream dis) throws IOException {
        SamplesInputStream is = new SamplesInputStream(dis);
        StackTraceSnapshotBuilder builder = new StackTraceSnapshotBuilder();
        ThreadsSample sample = is.readSample();
        // start time in milliseconds
        long startTime = sample.getTime() / 1000000;

        for ( ;sample != null; sample = is.readSample()) {
            builder.addStacktrace(sample.getTinfos(),sample.getTime());
            
        }
        is.close();
        is = null;
        CPUResultsSnapshot snapshot;
        try {
            snapshot = builder.createSnapshot(startTime);
        } catch (NoDataAvailableException ex) {
            throw new IOException(ex);
        }
        return new LoadedSnapshot(snapshot, ProfilingSettingsPresets.createCPUPreset(), null, null);
    }

    public void setProject(Lookup.Provider project) {
        this.project = project;
    }

    static void writeToStream(CPUResultsSnapshot snapshot, DataOutputStream dos) throws IOException {
        LoadedSnapshot loadedSnapshot = new LoadedSnapshot(snapshot,
                ProfilingSettingsPresets.createCPUPreset(), null, null);
        loadedSnapshot.save(dos);
    }

    public void save(DataOutputStream dos) throws IOException, OutOfMemoryError {
        // todo [performance] profile memory use during the save operation
        // there is ~80MB bytes used for byte[], for the length of uncompressed data ~20MB
        Properties props = new Properties();
        settings.store(props);

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("save properties: --------------------------------------------------------------"); // NOI18N
            LOGGER.finest(settings.debug());
            LOGGER.finest("-------------------------------------------------------------------------------"); // NOI18N
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000); // ~1MB pre-allocated
        DataOutputStream snapshotDataStream = new DataOutputStream(baos);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream(10000); // ~10kB pre-allocated
        DataOutputStream settingsDataStream = new DataOutputStream(baos2);

        try {
            snapshot.writeToStream(snapshotDataStream);
            snapshotDataStream.flush();
            props.store(settingsDataStream, ""); //NOI18N
            settingsDataStream.flush();

            byte[] snapshotBytes = baos.toByteArray();
            byte[] compressedBytes = new byte[snapshotBytes.length];

            Deflater d = new Deflater();
            d.setInput(snapshotBytes);
            d.finish();

            int compressedLen = d.deflate(compressedBytes);
            int uncompressedLen = snapshotBytes.length;

            // binary file format:
            // 1. magic number: "nbprofiler"
            // 2. int type
            // 3. int length of snapshot data size
            // 4. snapshot data bytes
            // 5. int length of settings data size
            // 6. settings data bytes (.properties plain text file format)
            // 7. String (UTF) custom comments
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("save version:" + SNAPSHOT_FILE_VERSION_MAJOR //NOI18N
                              + "." + SNAPSHOT_FILE_VERSION_MINOR); // NOI18N
                LOGGER.finest("save type:" + getType()); // NOI18N
                LOGGER.finest("length of uncompressed snapshot data:" + uncompressedLen); // NOI18N
                LOGGER.finest("save length of snapshot data:" + compressedLen); // NOI18N
                LOGGER.finest("length of settings data:" + baos2.size()); // NOI18N
            }

            dos.writeBytes(PROFILER_FILE_MAGIC_STRING); // 1. magic number: "nbprofiler"
            dos.writeByte(SNAPSHOT_FILE_VERSION_MAJOR); // 2. file version
            dos.writeByte(SNAPSHOT_FILE_VERSION_MINOR); // 3. file version
            dos.writeInt(getType()); // 4. int type
            dos.writeInt(compressedLen); // 5. int length of compressed snapshot data size
            dos.writeInt(uncompressedLen); // 5. int length of compressed snapshot data size
            dos.write(compressedBytes, 0, compressedLen); // 6. compressed snapshot data bytes
            dos.writeInt(baos2.size()); // 7. int length of settings data size
            dos.write(baos2.toByteArray()); // 8. settings data bytes (.properties plain text file format)
            dos.writeUTF(userComments);
        } catch (OutOfMemoryError e) {
            baos = null;
            snapshotDataStream = null;
            baos2 = null;
            settingsDataStream = null;

            throw e;
        } finally {
            if (snapshotDataStream != null) {
                snapshotDataStream.close();
            }

            if (settingsDataStream != null) {
                settingsDataStream.close();
            }
        }
    }

    public String toString() {
        String snapshotString = "snapshot = " + snapshot.toString(); // NOI18N
        String fileString = "file = " + ((file == null) ? "null" : file.toString()); // NOI18N
        String projectString = "project = "
                               + ((project == null) ? "null" : ProjectUtilities.getDisplayName(project)); // NOI18N

        return "Loaded Results Snapshot, " + snapshotString + ", " + projectString + ", " + fileString; // NOI18N
    }

    private static String getCorruptedMessage(IOException e) {
        String message = e.getMessage();

        if (message == null) {
            if (e instanceof EOFException) {
                return Bundle.LoadedSnapshot_SnapshotFileCorruptedReason(Bundle.LoadedSnapshot_SnapshotFileShortMsg());
            } else {
                return Bundle.LoadedSnapshot_SnapshotFileCorrupted();
            }
        } else {
            return Bundle.LoadedSnapshot_SnapshotFileCorruptedReason(message);
        }
    }

    private boolean load(DataInputStream dis) throws IOException {
        try {
            Properties props = new Properties();
            settings = new ProfilingSettings();

            // binary file format:
            // 1. magic number: "nbprofiler"
            // 2. byte major, minor version
            // 3. int type
            // 4. int length of snapshot data size
            // 5. snapshot data bytes
            // 6. int length of settings data size
            // 7. settings data bytes (.properties plain text file format)
            // 8. String (UTF) custom comments

            // 1. magic number: "nbprofiler"
            byte[] magicArray = new byte[PROFILER_FILE_MAGIC_STRING.length()];
            int len = dis.read(magicArray);

            if ((len != PROFILER_FILE_MAGIC_STRING.length()) || !PROFILER_FILE_MAGIC_STRING.equals(new String(magicArray))) {
                throw new IOException(Bundle.LoadedSnapshot_InvalidSnapshotFileMsg());
            }

            // 2. byte major, minor version
            byte majorVersion = dis.readByte();
            byte minorVersion = dis.readByte();

            if (majorVersion > SNAPSHOT_FILE_VERSION_MAJOR) {
                throw new IOException(Bundle.LoadedSnapshot_SnapshotFileCorruptedReason(Bundle.LoadedSnapshot_UnsupportedSnapshotVersionMsg()));
            }

            // 3. int type
            int type = dis.readInt();

            if (type == -1) {
                throw new IOException(Bundle.LoadedSnapshot_SnapshotFileCorruptedReason(Bundle.LoadedSnapshot_WrongSnapshotTypeMsg()));
            }

            // 4. int length of snapshot data size
            int compressedDataLen = dis.readInt();
            int uncompressedDataLen = dis.readInt();

            // 5. snapshot data bytes
            InputStream zipStream = new InflaterInputStream(new SubInputStream(dis,compressedDataLen));
            
            switch (type) {
                case SNAPSHOT_TYPE_CPU:
                    snapshot = new CPUResultsSnapshot();

                    break;
                case SNAPSHOT_TYPE_CODEFRAGMENT:
                    snapshot = new CodeRegionResultsSnapshot();

                    break;
                case SNAPSHOT_TYPE_MEMORY_ALLOCATIONS:
                    snapshot = new AllocMemoryResultsSnapshot();

                    break;
                case SNAPSHOT_TYPE_MEMORY_LIVENESS:
                    snapshot = new LivenessMemoryResultsSnapshot();

                    break;
                case SNAPSHOT_TYPE_MEMORY_SAMPLED:
                    snapshot = new SampledMemoryResultsSnapshot();
                    
                    break;
                case SNAPSHOT_TYPE_CPU_JDBC:
                    snapshot = new JdbcResultsSnapshot();
                    
                    break;
                default:
                    throw new IOException(Bundle.LoadedSnapshot_SnapshotFileCorruptedReason(Bundle.LoadedSnapshot_UnrecognizedSnapshotTypeMsg())); // not supported
            }

            BufferedInputStream bufBais = new BufferedInputStream(zipStream);
            DataInputStream dataDis = new DataInputStream(bufBais);

            try {
                snapshot.readFromStream(dataDis);
            } catch (IOException e) {
                throw new IOException(getCorruptedMessage(e));
            }

            // 6. int length of settings data size
            int settingsLen = dis.readInt();
            byte[] settingsBytes = new byte[settingsLen];

            // 7. settings data bytes (.properties plain text file format)
            int readLen2 = dis.read(settingsBytes);

            if (settingsLen != readLen2) {
                throw new IOException(Bundle.LoadedSnapshot_SnapshotFileCorruptedReason(Bundle.LoadedSnapshot_CannotReadSettingsDataMsg()));
            }
            
            // 8. String (UTF) custom comments
            if (minorVersion >= SNAPSHOT_FILE_VERSION_MINOR) {
                userComments = dis.readUTF();
            }

            // Process read data:
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("load version:" + majorVersion + "." + minorVersion); // NOI18N
                LOGGER.finest("load type:" + type); // NOI18N
                LOGGER.finest("load length of snapshot data:" + compressedDataLen); // NOI18N
                LOGGER.finest("uncompressed length of snapshot data:" + uncompressedDataLen); // NOI18N
                LOGGER.finest("load length of settings data:" + settingsLen); // NOI18N
            }

            ByteArrayInputStream bais2 = new ByteArrayInputStream(settingsBytes);
            DataInputStream settingsDis = new DataInputStream(bais2);

            try {
                props.load(settingsDis);
            } catch (IOException e) {
                throw new IOException(getCorruptedMessage(e));
            } finally {
                settingsDis.close();
            }

            settings.load(props);

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("load properties: --------------------------------------------------------------"); // NOI18N
                LOGGER.finest(settings.debug());
                LOGGER.finest("-------------------------------------------------------------------------------"); // NOI18N
            }
        } catch (OutOfMemoryError e) {
            ProfilerDialogs.displayError(Bundle.LoadedSnapshot_OutOfMemoryLoadingMsg());

            return false;
        }

        return true;
    }

    private static class SubInputStream extends FilterInputStream {
        private int limit;
        
        private SubInputStream(InputStream is, int l) {
            super(is);
            limit = l;
        }

        @Override
        public int available() throws IOException {
            int avail = super.available();
            return Math.min(avail, limit);
        }

        @Override
        public int read() throws IOException {
            if (limit == 0) {
                return -1;
            }
            limit--;
            return super.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (limit == 0) {
                return -1;
            }
            int realLen = Math.min(len, limit);
            int readBytes = super.read(b, off, realLen);
            limit -= readBytes;
            return readBytes;
        }

        @Override
        public long skip(long n) throws IOException {
            long skip = Math.min(n, limit);
            long skipped = super.skip(skip);
            limit -= skipped;
            return skipped;
        }

        @Override
        public boolean markSupported() {
            return false;
        }
    }
    
    static class SamplesInputStream {
        static final String ID = "NPSS"; // NetBeans Profiler samples stream, it must match org.netbeans.modules.sampler.SamplesOutputStream.ID
        static final int MAX_SUPPORTED_VERSION = 2;

        int version;
        int samples;
        long lastTimestamp;
        ObjectInputStream in;
        Map<Long,ThreadInfo> threads;
        
        SamplesInputStream(File file) throws IOException {
            this(new FileInputStream(file));
        }

        SamplesInputStream(InputStream is) throws IOException {
            readHeader(is);
            in = new ObjectInputStream(new GZIPInputStream(is));
            if (version > 1) {
                samples = in.readInt();
                lastTimestamp = in.readLong();
            }
            threads = new HashMap(128);
        }

        int getSamples() {
            return samples;
        }

        long getLastTimestamp() {
            return lastTimestamp;
        }
        
        ThreadsSample readSample() throws IOException {
            long time;
            ThreadInfo infos[];
            int sameThreads;
            Map<Long,ThreadInfo> newThreads;
            
            try {
                time = in.readLong();
            } catch (EOFException ex) {
                return null;
            }
            newThreads = new HashMap(threads.size());
            sameThreads = in.readInt();
            for (int i=0;i<sameThreads;i++) {
                Long tid = Long.valueOf(in.readLong());
                ThreadInfo oldThread = threads.get(tid);
                assert oldThread != null;
                newThreads.put(tid,oldThread);
            }
            infos = new ThreadInfo[in.readInt()];
            for (int i = 0 ; i < infos.length; i++) {
                CompositeData infoData;
                ThreadInfo thread;
                
                try {
                    infoData = (CompositeData) in.readObject();
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                thread = ThreadInfo.from(infoData);
                newThreads.put(Long.valueOf(thread.getThreadId()),thread);
            }
            threads = newThreads;
            return new ThreadsSample(time,threads.values());
        }

        void close() throws IOException {
            in.close();
        }

        @NbBundle.Messages({
            "MSG_NotNPSSSnapshot=Not a NPSS snapshot.",
            "MSG_UnsupportedSnapshotVersion=Unsupported snapshot version."
        })
        private void readHeader(InputStream is) throws IOException {
            String id;
            byte[] idarr = new byte[ID.length()];

            is.read(idarr);
            id = new String(idarr);
            if (!ID.equals(id)) {
                throw new IOException("Invalid header "+id) { // NOI18N
                    public String getLocalizedMessage() {
                        return Bundle.MSG_NotNPSSSnapshot();
                    }
                };
            }
            version = is.read();
            if (version > MAX_SUPPORTED_VERSION) {
                throw new IOException("NPSS file version "+version+" is not supported") { // NOI18N
                    public String getLocalizedMessage() {
                        return Bundle.MSG_UnsupportedSnapshotVersion();
                    }
                };
            }
        }
    }

    static final class ThreadsSample {
        private final long time;
        private final ThreadInfo[] tinfos;

        ThreadsSample(long t, Collection<ThreadInfo> tis) {
            time = t;
            tinfos = tis.toArray(new ThreadInfo[0]);
        }

        long getTime() {
            return time;
        }

        ThreadInfo[] getTinfos() {
            return tinfos;
        }
    }
}
/* Code to do persist into a ZIP file
   public void save (ZipOutputStream zos) throws IOException {
     Properties props = new Properties ();
     settings.store(props);
     Properties versionProps = new Properties ();
     versionProps.put("major", ""+SNAPSHOT_FILE_VERSION_MAJOR);
     versionProps.put("minor", ""+SNAPSHOT_FILE_VERSION_MINOR);
     ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000); // ~1MB pre-allocated
     BufferedOutputStream bufBaos = new BufferedOutputStream(baos);
     DataOutputStream snapshotDataStream = new DataOutputStream(bufBaos);
     try {
       snapshot.writeToStream(snapshotDataStream);
       snapshotDataStream.flush();
       // binary file format is ZIP file with the following content:
       // 1. version properties stored in file <type>.properties
       // 2. snapshot data in file "data"
       // 3. settings properties in file "settings.properties"
       if (DEBUG) {
         System.err.println("LoadedSnapshot.DEBUG: save version:" + SNAPSHOT_FILE_VERSION_MAJOR + "." + SNAPSHOT_FILE_VERSION_MINOR);
         System.err.println("LoadedSnapshot.DEBUG: save type:" + getType());
         System.err.println("LoadedSnapshot.DEBUG: save length of snapshot data:" + baos.size());
       }
       // 1. store version data, in the form of properties file named by type of results
       switch (getType ()) {
         case SNAPSHOT_TYPE_CPU: zos.putNextEntry(new ZipEntry("cpu.properties")); break;
         case SNAPSHOT_TYPE_CODEFRAGMENT: zos.putNextEntry(new ZipEntry("fragment.properties")); break;
         case SNAPSHOT_TYPE_MEMORY_ALLOCATIONS: zos.putNextEntry(new ZipEntry("allocations.properties")); break;
         case SNAPSHOT_TYPE_MEMORY_LIVENESS: zos.putNextEntry(new ZipEntry("liveness.properties")); break;
         default: throw new IllegalStateException();
       }
       versionProps.store(zos, "");
       zos.flush();
       zos.closeEntry();
       // 2. store data into file "data"
       zos.putNextEntry(new ZipEntry("data"));
       writeBytes (zos, baos.toByteArray());
       zos.flush();
       zos.closeEntry();
       // 3. store properties as "settings.properties"
       zos.putNextEntry(new ZipEntry("settings.properties"));
       props.store(zos, "");
       zos.flush();
       zos.closeEntry();
     } finally {
       snapshotDataStream.close ();
     }
   }
   public void load (ZipInputStream zis) throws IOException {
     Properties settingsProps = null;
     Properties versionProps = null;
     int type = SNAPSHOT_TYPE_UNKNOWN;
     byte[] dataBytes = null;
     ZipEntry ze = zis.getNextEntry();
     while (ze != null) {
       if (ze.isDirectory()) continue;
       String name = ze.getName();
       if (name.equals ("data")) {
         dataBytes = readBytes (zis);
       } else if (name.equals ("settings.properties")) {
         settingsProps = new Properties ();
         settingsProps.load(zis);
       } else if (type == SNAPSHOT_TYPE_UNKNOWN && name.endsWith(".properties")) {
         if (name.equals ("cpu.properties")) type = SNAPSHOT_TYPE_CPU;
         else if (name.equals ("fragment.properties")) type = SNAPSHOT_TYPE_CODEFRAGMENT;
         else if (name.equals ("allocations.properties")) type = SNAPSHOT_TYPE_MEMORY_ALLOCATIONS;
         else if (name.equals ("liveness.properties")) type = SNAPSHOT_TYPE_MEMORY_LIVENESS;
         if (type != SNAPSHOT_TYPE_UNKNOWN) {
           versionProps = new Properties ();
           versionProps.load(zis);
         }
       }
       ze = zis.getNextEntry();
     }
     if (dataBytes == null) throw new IOException ("The file is not a valid NetBeans Profiler Snapshot file: missing results data");
     if (settingsProps == null) throw new IOException ("The file is not a valid NetBeans Profiler Snapshot file: missing settings data");
     if (versionProps == null) throw new IOException ("The file is not a valid NetBeans Profiler Snapshot file: missing type and version data");
     // binary file format is ZIP file with the following content:
     // 1. version properties stored in file <type>.properties
     // 2. snapshot data in file "data"
     // 3. settings properties in file "settings.properties"
     // 2. int type
   /*    byte majorVersion = zis.readByte();
       byte minorVersion = zis.readByte();
       if (majorVersion > SNAPSHOT_FILE_VERSION_MAJOR) throw new IOException("Snapshot file is corrupted: unsupported file version");
 * /
   /*    // 5. snapshot data bytes
       int readLen1 = zis.read(dataBytes);
       if (lenData1 != readLen1) throw new IOException("Snapshot file is corrupted: cannot read snapshot data");
       // 6. int length of settings data size
       int lenData2 = zis.readInt();
       byte[] settingsBytes = new byte [lenData2];
       // 7. settings data bytes (.properties plain text file format)
       int readLen2 = zis.read(settingsBytes);
       if (lenData2 != readLen2) throw new IOException("Snapshot file is corrupted: cannot read settings data");
 * /
       // Process read data:
   /*
       if (DEBUG) {
         System.err.println("LoadedSnapshot.DEBUG: load version:" + majorVersion + "."+minorVersion);
         System.err.println("LoadedSnapshot.DEBUG: load type:" + type);
         System.err.println("LoadedSnapshot.DEBUG: load length of snapshot data:" + lenData1);
         System.err.println("LoadedSnapshot.DEBUG: load length of settings data:" + lenData2);
       }
 * /
       switch (type) {
         case SNAPSHOT_TYPE_CPU: snapshot = new CPUResultsSnapshot (); break;
         case SNAPSHOT_TYPE_CODEFRAGMENT: snapshot = new CodeRegionResultsSnapshot (); break;
         case SNAPSHOT_TYPE_MEMORY_ALLOCATIONS: snapshot = new AllocMemoryResultsSnapshot(); break;
         case SNAPSHOT_TYPE_MEMORY_LIVENESS: snapshot = new LivenessMemoryResultsSnapshot(); break;
         default: throw new IOException ("Snapshot file is corrupted: unrecognized snapshot type"); // not supported
       }
       ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes);
       BufferedInputStream bufBais = new BufferedInputStream(bais);
       DataInputStream dataDis = new DataInputStream(bufBais);
       try {
         snapshot.readFromStream(dataDis);
       } catch (IOException e) {
         ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
         throw new IOException (getCorruptedMessage(e));
       } finally {
         dataDis.close ();
       }
       settings = new ProfilingSettings();
       settings.load(settingsProps);
     }
     private byte[] readBytes(ZipInputStream zis) throws IOException {
       int ch1 = zis.read();
       int ch2 = zis.read();
       int ch3 = zis.read();
       int ch4 = zis.read();
       if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
       int length = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
       System.err.println("Loading length:"+length);
       if (length < 0) throw new IOException("Wrong data size: "+length);
       // TODO: if length is too large by error, we should not crash by allocating too much memory, but what is too much?
       byte[] bytes = new byte[length];
       zis.read(bytes, 0, length);
       for (int i = 0; i < 10; i++) {
         System.err.println("byte["+i+"]="+bytes[i]);
       }
       for (int i = bytes.length-10; i < bytes.length; i++) {
         System.err.println("byte["+i+"]="+bytes[i]);
       }
       return bytes;
     }
     private void writeBytes(ZipOutputStream zos, byte[] bytes) throws IOException {
       int v = bytes.length;
       System.err.println("Storing length:"+v);
       for (int i = 0; i < 10; i++) {
         System.err.println("byte["+i+"]="+bytes[i]);
       }
       for (int i = bytes.length-10; i < bytes.length; i++) {
         System.err.println("byte["+i+"]="+bytes[i]);
       }
       zos.write((v >>> 24) & 0xFF);
       zos.write((v >>> 16) & 0xFF);
       zos.write((v >>>  8) & 0xFF);
       zos.write((v >>>  0) & 0xFF);
       zos.write(bytes);
     }
     private static String getCorruptedMessage (IOException e) {
       String message = e.getMessage();
       if (message == null) {
         if (e instanceof EOFException) return "Snapshot file is corrupted: file too short";
         else return "Snapshot file is corrupted";
       }
       else return "Snapshot file is corrupted: " + message;
     }
     public static LoadedSnapshot loadSnapshot (ZipInputStream zis) throws IOException {
       LoadedSnapshot ls = new LoadedSnapshot ();
       ls.load (zis);
       return ls;
     }
 */
