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

import org.netbeans.modules.cnd.apt.impl.support.clank.ClankIncludeHandlerImpl;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * composition of include handler and macro map for parsing file phase
 */
public class APTPreprocHandlerImpl implements APTPreprocHandler {
    private boolean compileContext;
    private boolean isValid = true;
    private CharSequence lang;
    private CharSequence flavor;
    private long cuCRC;
    private PPMacroMap macroMap;
    private PPIncludeHandler inclHandler;
    
    /**
     * @param compileContext determine whether state created for real parse-valid
     * context, i.e. source file has always correct state, but header itself has
     * not correct state until it was included into any source file (may be recursively)
     */
    public APTPreprocHandlerImpl(PPMacroMap macroMap, PPIncludeHandler inclHandler, boolean compileContext, CharSequence lang, CharSequence flavor) {
        this.macroMap = macroMap;
        this.inclHandler = inclHandler;
        this.compileContext = compileContext;
        assert lang != null;
        this.lang = lang;
        assert flavor != null;
        this.flavor = flavor;
        this.cuCRC = countCompilationUnitCRC(inclHandler.getStartEntry().getStartFileProject().getUnitId());
    }

    @Override
    public PPMacroMap getMacroMap() {
        return macroMap;
    }

    @Override
    public PPIncludeHandler getIncludeHandler() {
        return inclHandler;
    }

    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)

    @Override
    public State getState() {
        return createStateImpl();
    }

    @Override
    public void setState(State state) {
        if (state instanceof StateImpl) {
            ((StateImpl)state).restoreTo(this);
        }
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public boolean isCompileContext() {
        return compileContext;
    }

    protected StateImpl createStateImpl() {
        return new StateImpl(this);
    }

    private void setCompileContext(boolean state) {
        this.compileContext = state;
    }

    @Override
    public CharSequence getLanguage() {
        return lang;
    }

    @Override
    public CharSequence getLanguageFlavor() {
        return flavor;
    }

    long getCompilationUnitCRC() {
        return cuCRC;
    }

    private long countCompilationUnitCRC(int unitId) {
	Checksum checksum = new Adler32();
	updateCrc(checksum, lang.toString());
	updateCrc(checksum, flavor.toString());
        if (inclHandler instanceof ClankIncludeHandlerImpl) {
            assert APTTraceFlags.USE_CLANK;
            updateCrcByFSPaths(checksum, ((ClankIncludeHandlerImpl)inclHandler).getSystemIncludePaths(), unitId);
            updateCrcByFSPaths(checksum, ((ClankIncludeHandlerImpl)inclHandler).getUserIncludePaths(), unitId);
            updateCrcByFSPaths(checksum, ((ClankIncludeHandlerImpl)inclHandler).getUserIncludeFilePaths(), unitId);
        } else {
            assert inclHandler instanceof APTIncludeHandlerImpl : "unexpected class " + inclHandler.getClass();
            updateCrcByFSPaths(checksum, ((APTIncludeHandlerImpl)inclHandler).getSystemIncludePaths(), unitId);
            updateCrcByFSPaths(checksum, ((APTIncludeHandlerImpl)inclHandler).getUserIncludePaths(), unitId);
            updateCrcByFSPaths(checksum, ((APTIncludeHandlerImpl)inclHandler).getUserIncludeFilePaths(), unitId);
        }
        long value = checksum.getValue();
        value += APTHandlersSupportImpl.getCompilationUnitCRC(macroMap);
	return value;

    }

    private void updateCrc(Checksum checksum, String s) {
	checksum.update(s.getBytes(SupportAPIAccessor.INTERNAL_CHARSET), 0, s.length());
    }

    private void updateCrcByFSPaths(Checksum checksum, List<IncludeDirEntry> paths, int unitId) {
	for( IncludeDirEntry path : paths ) {
            int id = Repository.getFileIdByName(unitId, path.getAsSharedCharSequence());
	    checksum.update(id);
            //updateCrc(checksum, path.getPath());
	}
    }

    public final static class StateImpl implements State {
        /*package*/ final CharSequence lang;
        /*package*/ final CharSequence flavor;
        /*package*/ final PPMacroMap.State macroState;
        /*package*/ final PPIncludeHandler.State inclState;
        private final byte attributes;
        private final long cuCRC;

        private final static byte COMPILE_CONTEXT_FLAG = 1 << 0;
        private final static byte CLEANED_FLAG = 1 << 1;
        private final static byte VALID_FLAG = 1 << 2;
        private final static byte ALREADY_TRIED_CACHE_PREPARATION_FLAG = 1 << 3;

        private static byte createAttributes(boolean compileContext, boolean cleaned, boolean valid, boolean alreadyTriedCachePreparation) {
            byte out = 0;
            if (compileContext) {
                out |= COMPILE_CONTEXT_FLAG;
            } else {
                out &= ~COMPILE_CONTEXT_FLAG;
            }
            if (cleaned) {
                out |= CLEANED_FLAG;
            } else {
                out &= ~CLEANED_FLAG;
            }
            if (valid) {
                out |= VALID_FLAG;
            } else {
                out &= ~VALID_FLAG;
            }
            if (alreadyTriedCachePreparation) {
                out |= ALREADY_TRIED_CACHE_PREPARATION_FLAG;
            } else {
                out &= ~ALREADY_TRIED_CACHE_PREPARATION_FLAG;
            }
            return out;
        }

        protected StateImpl(APTPreprocHandlerImpl handler) {
            if (handler.getMacroMap() != null) {
                this.macroState = handler.getMacroMap().getState();
            } else {
                this.macroState = null;
            }
            if (handler.getIncludeHandler() != null) {
                this.inclState = handler.getIncludeHandler().getState();
            } else {
                this.inclState = null;
            }
            this.attributes = createAttributes(handler.isCompileContext(), false, handler.isValid(), false);
            this.lang = handler.lang;
            this.flavor = handler.flavor;
            this.cuCRC = handler.cuCRC;
        }

        private StateImpl(StateImpl other, boolean cleanState, boolean compileContext, boolean valid, boolean prepareCacheIfPossible) {
            boolean cleaned;
            boolean alreadyTriedCachePreparation = false;
            PPIncludeHandler.State newIncludeState;
             PPMacroMap.State newMacroState;
            if (cleanState && !other.isCleaned()) {
                // first time cleaning
                // own copy of include information and macro state
                newIncludeState = APTHandlersSupportImpl.copyCleanIncludeState(other.inclState);
                newMacroState = APTHandlersSupportImpl.createCleanMacroState(other.macroState);
                cleaned = true;
            } else {
                // share states
                newMacroState = other.macroState;
                cleaned = other.isCleaned();
                newIncludeState = other.inclState;
            }
            if (cleaned) {
              alreadyTriedCachePreparation = true;
            } else if (prepareCacheIfPossible) {
              alreadyTriedCachePreparation = true;
              newIncludeState = APTHandlersSupportImpl.prepareIncludeStateCachesIfPossible(newIncludeState);
            }
            this.inclState = newIncludeState;
            this.macroState = newMacroState;
            this.attributes = createAttributes(compileContext, cleaned, valid, alreadyTriedCachePreparation);
            this.lang = other.lang;
            this.flavor = other.flavor;
            this.cuCRC = other.cuCRC;
        }

        private void restoreTo(APTPreprocHandlerImpl handler) {
            if (handler.getMacroMap() != null) {
                handler.getMacroMap().setState(this.macroState);
            }
            if (handler.getIncludeHandler() != null) {
                handler.getIncludeHandler().setState(this.inclState);
            }
            handler.setCompileContext(this.isCompileContext());
            handler.lang = this.lang;
            handler.flavor = this.flavor;
            handler.cuCRC = this.cuCRC;
            handler.setValid(this.isValid());
            if (!isValid()) {
                APTUtils.LOG.log(Level.SEVERE, "setting invalid state {0}", new Object[] { this } ); // NOI18N
            }
        }

        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("Lang=").append(lang).append("; Flavor=").append(flavor).append("; CRC=").append(cuCRC);// NOI18N
            retValue.append(isCleaned() ? "\nCleaned State;" : "\nNot Cleaned State;"); // NOI18N
            retValue.append(isCompileContext() ? "Compile Context;" : "Default/Null State;"); // NOI18N
            retValue.append(isValid() ? "Valid State;" : "Invalid State;"); // NOI18N
            retValue.append("\nInclude state Info:\n"); // NOI18N
            retValue.append(inclState);
            retValue.append("\nMACROS state info:\n"); // NOI18N
            retValue.append(this.macroState);
            return retValue.toString();
        }

        boolean equalsIgnoreInvalidFlag(State obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl) obj;
            // we do not compare macroStates because in case of
            // parsing from the same include sequence they are equal
            if (this.isCompileContext() != other.isCompileContext()) {
                return false;
            }
            if (this.inclState != other.inclState && (this.inclState == null || !this.inclState.equals(other.inclState))) {
                return false;
            }
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            // we do not compare macroStates because in case of
            // parsing from the same include sequence they are equal
            if (this.isCompileContext() != other.isCompileContext()) {
                return false;
            }
            if (this.isValid() != other.isValid()) {
                return false;
            }
            if (this.inclState != other.inclState && (this.inclState == null || !this.inclState.equals(other.inclState))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + (this.isCompileContext() ? 1 : 0);
            hash = 83 * hash + (this.isValid() ? 1 : 0);
            hash = 83 * hash + (this.inclState != null ? this.inclState.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean isCompileContext() {
            return (this.attributes & COMPILE_CONTEXT_FLAG) == COMPILE_CONTEXT_FLAG;
        }

        private boolean isAlreadyTriedCachePreparation() {
            return (this.attributes & ALREADY_TRIED_CACHE_PREPARATION_FLAG) == ALREADY_TRIED_CACHE_PREPARATION_FLAG;
        }

        @Override
        public boolean isCleaned() {
            return (this.attributes & CLEANED_FLAG) == CLEANED_FLAG;
        }

        @Override
        public boolean isValid() {
            return (this.attributes & VALID_FLAG) == VALID_FLAG;
        }

        /*package*/ APTPreprocHandler.State copy() {
            return new StateImpl(this, this.isCleaned(), this.isCompileContext(), this.isValid(), false);
        }

        /*package*/ APTPreprocHandler.State prepareCachesIfPossible() {
            if (this.isCleaned()) {
              // can not prepare from clean
              return this;
            } else if (this.isAlreadyTriedCachePreparation()) {
              // no need to make it twice
              return this;
            }
            return new StateImpl(this, this.isCleaned(), this.isCompileContext(), this.isValid(), true);
        }

        /*package*/ APTPreprocHandler.State copyCleaned() {
            return new StateImpl(this, true, this.isCompileContext(), this.isValid(), false);
        }

        /*package*/ APTPreprocHandler.State copyInvalid() {
            return new StateImpl(this, this.isCleaned(), this.isCompileContext(), false, false);
        }

        ////////////////////////////////////////////////////////////////////////
        // persistence support

        public void write(RepositoryDataOutput output) throws IOException {
            output.writeByte(this.attributes);
            APTSerializeUtils.writeIncludeState(this.inclState, output);
            APTSerializeUtils.writeMacroMapState(this.macroState, output);
            output.writeCharSequenceUTF(lang);
            output.writeCharSequenceUTF(flavor);
            output.writeLong(cuCRC);
        }

        public StateImpl(RepositoryDataInput input) throws IOException {
            this.attributes = input.readByte();
            this.inclState = APTSerializeUtils.readIncludeState(input);
            this.macroState = APTSerializeUtils.readMacroMapState(input);
            this.lang = input.readCharSequenceUTF();
            this.flavor = input.readCharSequenceUTF();
            this.cuCRC = input.readLong();
        }

        @Override
        public CharSequence getLanguage() {
            return lang;
        }

        @Override
        public CharSequence getLanguageFlavor() {
            return flavor;
        }

        public long getCRC() {
            return cuCRC;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("Lang=").append(lang).append("; Flavor=").append(flavor).append("; CRC=").append(cuCRC);// NOI18N
        retValue.append(this.isCompileContext() ? "\nCompile Context" : "\nDefault/Null State"); // NOI18N
        retValue.append("\nInclude Info:\n"); // NOI18N
        retValue.append(this.inclHandler);
        retValue.append("\nMACROS info:\n"); // NOI18N
        retValue.append(this.macroMap);
        return retValue.toString();
    }
}
