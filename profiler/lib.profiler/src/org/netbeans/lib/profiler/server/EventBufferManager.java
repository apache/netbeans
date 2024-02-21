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

package org.netbeans.lib.profiler.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.Platform;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;


/**
 * Target VM-side management of the shared-memory event buffer file, through which rough profiling data
 * is transmitted to the client.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 */
public class EventBufferManager implements CommonConstants {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final boolean DEBUG = System.getProperty("org.netbeans.lib.profiler.server.EventBufferManager") != null; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private File bufFile;
    private FileChannel bufFileChannel;
    private MappedByteBuffer mapByteBuf;
    private ProfilerServer profilerServer;
    private RandomAccessFile raFile;
    private String bufFileName = "";
    private boolean bufFileOk;
    private boolean bufFileSent;
    private boolean remoteProfiling;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public EventBufferManager(ProfilerServer server) {
        profilerServer = server;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getBufferFileName() {
        if (remoteProfiling) {
            return ""; // NOI18N
        } else {
            return bufFileName;
        }
    }

    public void eventBufferDumpHook(byte[] eventBuffer, int startPos, int curPtrPos) {
        int length = curPtrPos - startPos;

        if (!remoteProfiling) {
            if (!bufFileOk) {
                return;
            }

            if (DEBUG) {
                System.err.println("EventBufferManager.DEBUG: Dumping to file: startPos:" + startPos + ", length:" + length); // NOI18N
            }

            mapByteBuf.reset();
            mapByteBuf.put(eventBuffer, startPos, length);
            bufFileOk = profilerServer.sendEventBufferDumpedCommand(length, bufFileSent ? "": getBufferFileName());
            bufFileSent = true;
        } else {
            if (DEBUG) {
                System.err.println("EventBufferManager.DEBUG: Dumping to compressed wire: startPos:" + startPos + ", length:" + length); // NOI18N
            }
            profilerServer.sendEventBufferDumpedCommand(length, eventBuffer, startPos);
        }
    }

    public void freeBufferFile() {
        if (remoteProfiling) {
            return;
        }

        try {
            if (bufFileChannel != null) {
                mapByteBuf = null;
                bufFileChannel.close();
                raFile.close();
                System.gc(); // GCing mapBuf is the only way to free the buffer file.
                bufFileOk = false;
            }
        } catch (IOException ex) {
            System.err.println("Profiler Agent Error: internal error when closing temporary memory-mapped communication file"); // NOI18N
        }
    }

    public void openBufferFile(int sizeInBytes) throws IOException {
        remoteProfiling = profilerServer.getProfilingSessionStatus().remoteProfiling;
        if (remoteProfiling) {
            return;
        }

        if (bufFileOk) {
            return;
        }

        try {
            bufFileSent = false;
            bufFile = Files.createTempFile("jfluidbuf", null).toFile(); // NOI18N
            bufFileName = bufFile.getCanonicalPath();

            // Bugfix: http://profiler.netbeans.org/issues/show_bug.cgi?id=59166
            // Summary: Temporary communication file should be accessible for all users
            // Bugfix details: As it does not seem to be possible to set the file permissions using Java code
            //                 we explicitely invoke chmod on the newly created buffer file if we are on UNIX
            if (Platform.isUnix()) {
                try {
                    Runtime.getRuntime().exec(new String[] { "chmod", "666", bufFileName }); // NOI18N
                } catch (Exception e) {
                    System.err.println("*** JFluid Warning: Failed to set access permissions on temporary buffer file, you may not be able to attach as a different user: " + e.getMessage()); // NOI18N
                }
            }

            raFile = new RandomAccessFile(bufFile, "rw"); // NOI18N
            bufFileChannel = raFile.getChannel();
            mapByteBuf = bufFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, sizeInBytes);
            mapByteBuf.rewind();
            mapByteBuf.mark();
            bufFileOk = true;
        } catch (FileNotFoundException ex1) {
            System.err.println("Profiler Agent Error: FileNotFoundException in EventBufferManager.openBufferFile - should not happen!"); // NOI18N

            return;
        } catch (IOException ex2) {
            System.err.println("Profiler Agent Error: Could not create temporary buffer file in the default temporary directory: "
                               + ex2.getMessage() + ": " + System.getProperty("java.io.tmpdir")); // NOI18N
            throw new IOException("Could not create temporary buffer file in the default temporary directory: "
                                  + ex2.getMessage() + ": " + System.getProperty("java.io.tmpdir")); // NOI18N
        }
    }
}
