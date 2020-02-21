/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.remote.sync;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Stores information about controlled files
 *
 * NB: the class is NOT thread safe
 *
 */
public final class FileData {

    private final Properties data;
    private final FileObject privProjectStorageDir;
    private final String dataFileName;
    private long dataFileTimeStamp;

    // upgrade to 1.3 is caused not by this file itself, but by change in remote host mirror
    private static final String VERSION = "1.3"; // NOI18N
    private static final String VERSION_KEY = "VERSION"; // NOI18N

    //
    //  Public stuff
    //

    public static final class FileStateInfo {
        public final long timestamp;
        public final FileState state;

        public FileStateInfo(FileState mode, long timestamp) {
            this.state = mode;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return state.toString() + timestamp;
        }

    }

    private static final Map<String, WeakReference<FileData>> instances = new HashMap<>();

    @Deprecated
    public static FileData get(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) throws IOException {
        return get(FileUtil.toFileObject(FileUtil.normalizeFile(privProjectStorageDir)), executionEnvironment);
    }

    public static FileData get(FileObject privProjectStorageDir, ExecutionEnvironment executionEnvironment) throws IOException {
        String key;
        try {
            key = FileSystemProvider.getCanonicalPath(privProjectStorageDir);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            key = privProjectStorageDir.getPath();
        }
        key += ExecutionEnvironmentFactory.toUniqueID(executionEnvironment);
        WeakReference<FileData> ref = instances.get(key);
        FileData instance = null;
        if (ref != null) {
            instance = ref.get();
            if (instance != null && ! instance.isValid()) {
                instance = null;
            }
        }
        if (instance == null) {
            instance = new FileData(privProjectStorageDir, executionEnvironment);
            instances.put(key, new WeakReference<>(instance));
        }
        return instance;
    }

    private FileData(FileObject privProjectStorageDir, ExecutionEnvironment executionEnvironment) throws IOException {
        data = new Properties();
        this.privProjectStorageDir = privProjectStorageDir;
        this.dataFileTimeStamp = -1;
        this.dataFileName = "timestamps-" + RemoteUtil.hostNameToLocalFileName(executionEnvironment.getHost()) + //NOI18N
                '-' + RemoteUtil.hostNameToLocalFileName(executionEnvironment.getUser()) +
                '-' + executionEnvironment.getSSHPort(); //NOI18N
        if (!Boolean.getBoolean("cnd.remote.timestamps.clear")) {
            try {
                load();
                if (!VERSION.equals(data.get(VERSION_KEY))) {
                    data.clear();
                }
            } catch (IOException ex) {
                data.clear();
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public FileObject getDataFile() throws IOException {
        return FileUtil.createData(privProjectStorageDir, dataFileName);
    }

//    /**
//     * Initial filling
//     * NB: file should be absolute and NORMALIZED!
//     */
//    public void addFile(File file) {
//        CndUtils.assertNormalized(file);
//        String key = getFileKey(file);
//        FileStateInfo info = getFileInfo(key);
//        if (info == null) {
//            setFileInfo(file, FileState.INITIAL);
//        } else {
//            switch (info.state) {
//                case COPIED: // fall through
//                case TOUCHED:
//                    if (file.lastModified() != info.timestamp) {
//                        setFileInfo(file, FileState.INITIAL);
//                    }
//                    break;
//                case INITIAL: // fall through
//                case UNCONTROLLED:
//                    // nothing
//                    break;
//                default:
//                    CndUtils.assertTrue(false, "Unexpected state: " + info.state); //NOI18N
//                    setFileInfo(file, FileState.INITIAL);
//            }
//        }
//    }

    public FileState getState(File file) {
        FileStateInfo info = getFileInfo(file);
        return (info == null) ? FileState.UNCONTROLLED : info.state;
    }

    public void setState(File file, FileState state) {
        setFileInfo(file, state);
    }

    public FileStateInfo getFileInfo(File file) {
        return getFileInfo(getFileKey(file));
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void store()  {
        FileObject dataFile = null;
        OutputStream os = null;
        try {
            dataFile = getDataFile();
            os = new BufferedOutputStream(dataFile.getOutputStream());
            data.setProperty(VERSION_KEY, VERSION);
            data.store(os, null);
            os.close();
            os = null;
            dataFileTimeStamp = dataFile.lastModified().getTime();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            try {
                if (dataFile != null) { // NB hint that sais it's unnecessary check lies
                    dataFile.delete();
                }
            } catch (IOException ex1) {
                System.err.printf("Error deleting file %s%n", dataFile.getPath());
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private boolean isValid() {
        try {
            return dataFileTimeStamp == getDataFile().lastModified().getTime(); // getDataFile() is never null
        } catch (IOException ex) {
            return false;
        }
    }

    public void clear() {
        data.clear();
    }

    //
    //  Private stuff
    //

    private long load() throws IOException {
        FileObject dataFile = getDataFile();
        if (dataFile.isValid()) {
            long time = System.currentTimeMillis();
            final InputStream is = dataFile.getInputStream();
            BufferedInputStream bs = new BufferedInputStream(is);
            try {
                data.load(bs);
                dataFileTimeStamp = dataFile.lastModified().getTime();
            } finally {
                bs.close();
            }
            if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) {
                time = System.currentTimeMillis() - time;
                System.out.printf("reading %d timestamps from %s took %d ms%n", data.size(), dataFile.getPath(), time); // NOI18N
            }
        }
        return dataFile.lastModified().getTime();
    }

    private FileStateInfo getFileInfo(String fileKey) {
        String strValue = data.getProperty(fileKey, null);
        if (strValue != null && strValue.length() > 0) {
            FileState state;
            char prefix = strValue.charAt(0);
            state = FileState.fromId(prefix);
            strValue = strValue.substring(1);
            try {
                long timeStamp = Long.parseLong(strValue);
                return new FileStateInfo(state, timeStamp);
            } catch (NumberFormatException nfe) {
                RemoteUtil.LOGGER.warning(String.format("Incorrect status/timestamp format \"%s\" for %s", strValue, fileKey)); //NOI18N
            }
        }
        return null;
    }

    private void setFileInfo(File file, FileState state) {
        String key = getFileKey(file);
        char prefix = state.id;
        data.put(key, String.format("%c%d", prefix, file.lastModified())); // NOI18N
    }

    private String getFileKey(File file) {
        String key = file.getAbsolutePath();
        if (!CndFileUtils.isSystemCaseSensitive()) {
            key = key.toLowerCase(Locale.getDefault());
        }
        return key;
    }
}
