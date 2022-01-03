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

package org.netbeans.modules.cnd.apt.utils;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTBaseMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroMapSnapshot;
import org.netbeans.modules.cnd.apt.impl.support.APTPreprocHandlerImpl;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankIncludeHandlerImpl;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;

/**
 * utilities for APT serialization
 */
public class APTSerializeUtils {

    private APTSerializeUtils() {
    }

    static public void writeAPT(ObjectOutputStream out, APT apt) throws IOException {
        out.writeObject(apt);
        // the tree structure has a lot of siblings =>
        // StackOverflow exceptions during serialization of "next" field
        // we try to prevent it by using own procedure of writing 
        // tree structure
        if (apt != null) {
            writeTree(out, apt);
        }
    }
    
    // symmetric to writeObject
    static public APT readAPT(ObjectInputStream in) throws IOException, ClassNotFoundException {
        APT apt = (APT)in.readObject();
        if (apt != null) {
            // read tree structure into this node
            readTree(in, apt);
        }
        return apt;
    }

    ////////////////////////////////////////////////////////////////////////////
    // we have StackOverflow when serialize APT due to it's tree structure:
    // to many recurse calls to writeObject on writing "next" field
    // let's try to reduce depth of recursion by depth of tree
    
    private static final short CHILD = 1;
    private static final short SIBLING = 2;
    private static final short END_APT = 3;
    
    static private void writeTree(ObjectOutputStream out, APT root) throws IOException {
        assert (root != null) : "there must be something to write"; // NOI18N
        APT node = root;
        do {
            APT child = node.getFirstChild();
            if (child != null) {
                // due to not huge depth of the tree                
                // write child without optimization
                out.writeShort(CHILD);
                writeAPT(out, child);
            }
            node = node.getNextSibling();            
            if (node != null) {
                // we don't want to use recursion on writing sibling
                // to prevent StackOverflow, 
                // we use while loop for writing siblings
                out.writeShort(SIBLING);
                // write node data
                out.writeObject(node);                 
            }
        } while (node != null);
        out.writeShort(END_APT);
    }

    static private void readTree(ObjectInputStream in, APT root) throws IOException, ClassNotFoundException {
        assert (root != null) : "there must be something to read"; // NOI18N
        APT node = root;
        do {
            short kind = in.readShort();
            switch (kind) {
                case END_APT:
                    return;
                case CHILD:
                    node.setFirstChild(readAPT(in));
                    break;
                case SIBLING:
                    APT sibling = (APT) in.readObject();
                    node.setNextSibling(sibling);
                    node = sibling;
                    break;
                default:
                    assert(false);
            }            
        } while (node != null);
    }
    
    private static int fileIndex = 0;
    private static final boolean TRACE = true;
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    static public APT testAPTSerialization(APTFileBuffer buffer, APT apt) {
        FileObject file = CndFileUtils.toFileObject(buffer.getFileSystem(), buffer.getAbsolutePath());
        APT aptRead = null;
        // testing caching ast
        String prefix = "cnd_apt_"+(fileIndex++); // NOI18N
        String suffix = file.getNameExt();
        try {
            File out = File.createTempFile(prefix, suffix); // File - sic!
            if (TRACE) { System.out.println("...saving APT of file " + file.getPath() + " into tmp file " + out); } // NOI18N
            long astTime = System.currentTimeMillis();
            // write
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(out), APTTraceFlags.BUF_SIZE));
            try {
                writeAPT(oos, apt);
            } finally {
                oos.close();
            }
            long writeTime = System.currentTimeMillis() - astTime;
            if (TRACE) { System.out.println("saved APT of file " + file.getPath() + " withing " + writeTime + "ms"); } // NOI18N
            astTime = System.currentTimeMillis();
            // read
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(out), APTTraceFlags.BUF_SIZE));
            try {
                aptRead = readAPT(ois);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace(System.err);
            } finally {
                ois.close();                
            }
            long readTime = System.currentTimeMillis() - astTime;
            if (TRACE) { System.out.println("read APT of file " + file.getPath() + " withing " + readTime + "ms"); } // NOI18N
            out.delete();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return aptRead;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // persistence support
    
    public static void writeSystemMacroMap(APTMacroMap macroMap, RepositoryDataOutput output) throws IOException {
        //throw new UnsupportedOperationException("Not yet supported"); // NOI18N
    }
    
    public static APTMacroMap readSystemMacroMap(RepositoryDataInput input) throws IOException {
        //throw new UnsupportedOperationException("Not yet supported"); // NOI18N
        return null;
    }
    
    public static void writeMacroMapState(PPMacroMap.State state, RepositoryDataOutput output) throws IOException {
        assert state != null;
        if (state instanceof APTFileMacroMap.FileStateImpl) {
            output.writeShort(MACRO_MAP_FILE_STATE_IMPL);
            ((APTFileMacroMap.FileStateImpl)state).write(output);
        } else if (state instanceof ClankFileMacroMap.FileStateImpl) {
            output.writeShort(CLANK_MACRO_MAP_FILE_STATE_IMPL);
            ((ClankFileMacroMap.FileStateImpl)state).write(output);
        } else {
            assert !APTTraceFlags.USE_CLANK;
            assert state instanceof APTBaseMacroMap.StateImpl;
            output.writeShort(MACRO_MAP_STATE_IMPL);
            ((APTBaseMacroMap.StateImpl)state).write(output);
        }
    }
    
    public static PPMacroMap.State readMacroMapState(RepositoryDataInput input) throws IOException {
        short handler = input.readShort();
        PPMacroMap.State state;
        if (handler == MACRO_MAP_FILE_STATE_IMPL) {
            state = new APTFileMacroMap.FileStateImpl(input);
        } else if (handler == CLANK_MACRO_MAP_FILE_STATE_IMPL) {
            state = new ClankFileMacroMap.FileStateImpl(input);
        } else {
            assert handler == MACRO_MAP_STATE_IMPL;
            state = new APTBaseMacroMap.StateImpl(input);
        }
        return state;
    }

    public static void writeIncludeState(PPIncludeHandler.State state, RepositoryDataOutput output) throws IOException {
        assert state != null;
        if (APTTraceFlags.USE_CLANK) {
            assert state instanceof ClankIncludeHandlerImpl.StateImpl;
            ((ClankIncludeHandlerImpl.StateImpl)state).write(output);
        } else {
            assert state instanceof APTIncludeHandlerImpl.StateImpl;
            ((APTIncludeHandlerImpl.StateImpl)state).write(output);
        }
    }
    
    public static PPIncludeHandler.State readIncludeState(RepositoryDataInput input) throws IOException {
        PPIncludeHandler.State state;
        if (APTTraceFlags.USE_CLANK) {
            state = new ClankIncludeHandlerImpl.StateImpl(input);
        } else {
            state = new APTIncludeHandlerImpl.StateImpl(input);
        }
        return state;
    }    

    public static void writePreprocState(APTPreprocHandler.State state, RepositoryDataOutput output) throws IOException {
        assert state != null;
        if (state instanceof APTPreprocHandlerImpl.StateImpl) {
            output.writeShort(PREPROC_STATE_STATE_IMPL);
            ((APTPreprocHandlerImpl.StateImpl)state).write(output);
        } else {
            throw new IllegalArgumentException("unknown preprocessor state" + state);  //NOI18N
        }        
    }
    
    public static APTPreprocHandler.State readPreprocState(RepositoryDataInput input) throws IOException {
        int handler = input.readShort();
        APTPreprocHandler.State out;
        switch (handler) {
            case PREPROC_STATE_STATE_IMPL:
                out = new APTPreprocHandlerImpl.StateImpl(input);
                break;
            default:
                throw new IllegalArgumentException("unknown preprocessor state handler" + handler);  //NOI18N
        }
        return out;
    } 
    
    ////////////////////////////////////////////////////////////////////////////
    // persist snapshots
    
    public static void writeSnapshot(APTMacroMapSnapshot snap, RepositoryDataOutput output) throws IOException {
        // FIXUP: we do not support yet writing snapshots!
//        if (snap == null) {
            output.writeShort(NULL_POINTER);
//        } else {
//            output.writeInt(MACRO_MAP_SNAPSHOT);
//            snap.write(output);
//        }
    }

    public static APTMacroMapSnapshot readSnapshot(RepositoryDataInput input) throws IOException {
        int handler = input.readShort();
        APTMacroMapSnapshot snap = null;
        if (handler != NULL_POINTER) {
            assert handler == MACRO_MAP_SNAPSHOT;
            snap = new APTMacroMapSnapshot(input);
        }
        return snap;
    }
    
    public static void writeMacro(APTMacro macro, RepositoryDataOutput output) throws IOException {
        assert macro != null;
        if (macro == APTMacroMapSnapshot.UNDEFINED_MACRO) {
            output.writeShort(UNDEFINED_MACRO);
        } else if (macro instanceof APTMacroImpl) {
            output.writeShort(MACRO_IMPL);
            ((APTMacroImpl)macro).write(output);
        }
    }
    
    public static APTMacro readMacro(RepositoryDataInput input) throws IOException {
        int handler = input.readShort();
        APTMacro macro;
        if (handler == UNDEFINED_MACRO) {
            macro = APTMacroMapSnapshot.UNDEFINED_MACRO;
        } else {
            assert handler == MACRO_IMPL;
            macro = new APTMacroImpl(input);
        }
        return macro;
    }
    
    private static final short NULL_POINTER               = -1;
    private static final short MACRO_MAP_STATE_IMPL       = 1;
    private static final short CLANK_MACRO_MAP_FILE_STATE_IMPL  = MACRO_MAP_STATE_IMPL + 1;
    private static final short MACRO_MAP_FILE_STATE_IMPL  = CLANK_MACRO_MAP_FILE_STATE_IMPL + 1;
    private static final short PREPROC_STATE_STATE_IMPL   = MACRO_MAP_FILE_STATE_IMPL + 1;
    private static final short MACRO_MAP_SNAPSHOT         = PREPROC_STATE_STATE_IMPL + 1;
    private static final short UNDEFINED_MACRO            = MACRO_MAP_SNAPSHOT + 1;
    private static final short MACRO_IMPL                 = UNDEFINED_MACRO + 1;
    
}
