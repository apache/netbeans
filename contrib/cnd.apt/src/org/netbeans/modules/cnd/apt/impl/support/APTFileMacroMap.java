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

package org.netbeans.modules.cnd.apt.impl.support;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * macro map is created for each translation unit and
 * it has specified system predefined map where it delegates
 * requests about macros if not found in own macro map
 */
public class APTFileMacroMap extends APTBaseMacroMap {
    private static final Map<CharSequence,APTMacro> NO_CACHE = Collections.unmodifiableMap(new HashMap<CharSequence,APTMacro>(0));
    private static final int INITIAL_CACHE_SIZE = 512;
    private APTMacroMap sysMacroMap;
    private Map<CharSequence,APTMacro> macroCache = NO_CACHE;
    private int crc1 = 0;
    private int crc2 = 0;

    public APTFileMacroMap() {
    }

    /**
     * Creates a new instance of APTFileMacroMap
     */
    public APTFileMacroMap(APTMacroMap sysMacroMap, List<String> userMacros) {
        if (sysMacroMap == null) {
            sysMacroMap = APTBaseMacroMap.EMPTY;
        }
        this.sysMacroMap = sysMacroMap;
        this.macroCache = new HashMap<CharSequence,APTMacro>(INITIAL_CACHE_SIZE);
        fill(userMacros, false);
    }

    @Override
    public boolean isDefined(CharSequence token) {
        token = CharSequences.create(token);
        // check own map
        initCache();
        // no need to check in super, because everything is in cache already
        APTMacro res = macroCache.get(token);
        if (res == APTMacroMapSnapshot.UNDEFINED_MACRO) {
            return false;
        } else if (res != null) {
            return true;
        }

        // then check system map
        if (sysMacroMap != null) {
            return sysMacroMap.isDefined(token);
        }
        return false;
    }

    @Override
    public APTMacro getMacro(APTToken token) {
        // check own map
        CharSequence macroText = token.getTextID();
        initCache();
        APTMacro res = macroCache.get(macroText);
        if (res == null) {
            // no need to check in super, because everything is in cache already
            // then check system map
            if (sysMacroMap != null) {
                res = sysMacroMap.getMacro(token);
            }
            if (res == null) {
                res = APTMacroMapSnapshot.UNDEFINED_MACRO;
            }
            if (res.getKind() != APTMacro.Kind.POSITION_PREDEFINED) {
                // do not remember position based macro values
                macroCache.put(macroText, res);
            }
        }
        // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }

    @Override
    protected void putMacro(CharSequence name, APTMacro macro) {
        initCache();
        super.putMacro(name, macro);
        APTMacro old = macroCache.put(name, macro);
        int i1 = name.hashCode();
        int i2;
        if (old != null) {
            i2 = old.hashCode();
            crc1 -= i1 + i2;
            crc2 -= i1 ^ i2;
        }
        if (macro != APTMacroMapSnapshot.UNDEFINED_MACRO) {
            i2 = macro.hashCode();
            crc1 += i1 + i2;
            crc2 += i1 ^ i2;
        }
    }

    @Override
    protected APTMacro createMacro(CharSequence file, APTDefine define, Kind macroType) {
        APTMacro macro = new APTMacroImpl(file, define, macroType);
        if (APTTraceFlags.APT_SHARE_MACROS) {
            macro = cache.getMacro(macro);
        }
        return macro;
    }

    @Override
    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        return new APTMacroMapSnapshot(parent);
    }

    @Override
    public State getState() {
        //Create new snapshot instance in the tree
        changeActiveSnapshotIfNeeded();
        return new FileStateImpl(active.getParent(), sysMacroMap, crc1, crc2);
    }

    public long getCompilationUnitCRC() {
        long out = (((long)crc1) << 32) + crc2;
        if (sysMacroMap instanceof APTSystemMacroMap) {
            out += ((APTSystemMacroMap)sysMacroMap).getCompilationUnitCRC();
        }
        return out;
    }

    @Override
    public void setState(State state) {
        active = makeSnapshot(((StateImpl)state).snap);
        crc1 = 0;
        crc2 = 0;
        if (state instanceof FileStateImpl) {
            FileStateImpl fileState = (FileStateImpl) state;
            sysMacroMap = fileState.sysMacroMap;
            crc1 = fileState.crc1;
            crc2 = fileState.crc2;
        }
        macroCache = NO_CACHE;
    }

    private void initCache() {
        if (macroCache == NO_CACHE) {
            // fill cache to speedup getMacro
            macroCache = active.getAll();
            if (crc1 == 0 && crc2 == 0) {
                for(Map.Entry<CharSequence, APTMacro> entry : macroCache.entrySet()){
                    int i1 = entry.getKey().hashCode();
                    int i2 = entry.getValue().hashCode();
                    crc1 += i1 + i2;
                    crc2 += i1 ^ i2;
                }
            }
        }
    }

//    public StateKey getStateKey() {
//        return new StateKey(crc1, crc2);
//    }

    public static class FileStateImpl extends StateImpl {
        private final APTMacroMap sysMacroMap;
        private final int crc1;
        private final int crc2;

        private FileStateImpl(APTMacroMapSnapshot snap, APTMacroMap sysMacroMap, int crc1, int crc2) {
            super(snap);
            this.sysMacroMap = sysMacroMap;
            this.crc1 = crc1;
            this.crc2 = crc2;
        }

        private FileStateImpl(FileStateImpl state, boolean cleanedState) {
            super(state, cleanedState);
            this.sysMacroMap = state.sysMacroMap;
            this.crc1 = state.crc1;
            this.crc2 = state.crc2;
        }

        APTHandlersSupportImpl.StateKeyImpl getStateKey(Key startFileProject) {
            long sysCrc = 0;
            if (sysMacroMap instanceof APTSystemMacroMap) {
                sysCrc = ((APTSystemMacroMap) sysMacroMap).getCompilationUnitCRC();
            }
            return new APTHandlersSupportImpl.StateKeyImpl(crc1, crc2, sysCrc, startFileProject);
        }

        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("FileState\n"); // NOI18N
            retValue.append("Snapshot\n"); // NOI18N
            retValue.append(super.toString());
            retValue.append("\nSystem MacroMap\n"); // NOI18N
            if (System.getProperty("cnd.apt.macro.trace") != null) {
                retValue.append(sysMacroMap);
            } else {
                retValue.append(System.identityHashCode(sysMacroMap));
            }
            return retValue.toString();
        }

        ////////////////////////////////////////////////////////////////////////
        // persistence support

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            output.writeInt(crc1);
            output.writeInt(crc2);
            APTSerializeUtils.writeSystemMacroMap(this.sysMacroMap, output);
        }

        public FileStateImpl(final RepositoryDataInput input) throws IOException {
            super(input);
            this.crc1 = input.readInt();
            this.crc2 = input.readInt();
            APTMacroMap systemMap = APTSerializeUtils.readSystemMacroMap(input);
            if (systemMap == null) {
                this.sysMacroMap = APTBaseMacroMap.EMPTY;
            } else {
                this.sysMacroMap = systemMap;
            }
        }

        @Override
        public StateImpl copyCleaned() {
            return new FileStateImpl(this, true);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // manage macro expanding stack

    private final LinkedList<CharSequence> expandingMacros = new LinkedList<CharSequence>();
    private final HashSet<CharSequence> expandingMacroIDs = new HashSet<CharSequence>();
    private boolean afterPPDefinedKeyword = false;

    @Override
    public boolean pushPPDefined() {
        if (afterPPDefinedKeyword) {
            return false;
        } else {
            afterPPDefinedKeyword = true;
            return true;
        }
    }

    @Override
    public boolean popPPDefined() {
        if (afterPPDefinedKeyword) {
            afterPPDefinedKeyword = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean pushExpanding(APTToken token) {
        assert (token != null);
        CharSequence id = token.getTextID();
        if (!isExpanding(id)) {
            expandingMacros.addLast(id);
            expandingMacroIDs.add(id);
            return true;
        }
        return false;
    }

    @Override
    public void popExpanding() {
        try {
            CharSequence id = expandingMacros.removeLast();
            expandingMacroIDs.remove(id);
        } catch (ArrayIndexOutOfBoundsException ex) {
            assert (false) : "why pop from empty stack?"; // NOI18N
        }
    }

    @Override
    public boolean isExpanding(APTToken token) {
        return isExpanding(token.getTextID());
    }

    private boolean isExpanding(CharSequence id) {
        return expandingMacroIDs.contains(id);
    }

    //////////////////////////////////////////////////////////////////////////
    // implementation details
    /*public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        boolean retValue = false;
        if (obj instanceof APTFileMacroMap) {
            retValue = super.equals(obj);
            if (retValue) {
                // use '==' as we share system maps
                retValue = (this.sysMacroMap == ((APTFileMacroMap)obj).sysMacroMap);
            }
        }
        return retValue;
    }*/

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("Own Map:\n"); // NOI18N
        retValue.append(super.toString());
        retValue.append("System Map:\n"); // NOI18N
        retValue.append(sysMacroMap);
        return retValue.toString();
    }

    private static final APTMacroCache cache = APTMacroCache.getManager();
}
