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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public class ClankFileMacroMap extends ClankMacroMap {

    private ClankSystemMacroMap sysMacros;

    public ClankFileMacroMap() {
        super(0);
    }

    public ClankFileMacroMap(ClankSystemMacroMap sysMacroMap, List<String> userMacros) {
        super(userMacros);
        this.sysMacros = sysMacroMap;
    }

    @Override
    public long getCompilationUnitCRC() {
        if (sysMacros == null) {
            return super.getCompilationUnitCRC();
        }
        return super.getCompilationUnitCRC() ^ sysMacros.getCompilationUnitCRC();
    }

    @Override
    public State getState() {
        return new FileStateImpl(this);
    }

    @Override
    public void setState(State state) {
        ((FileStateImpl)state).restoreTo(this);
    }

    Collection<String> getSystemMacroDefinitions() {
        return (sysMacros == null) ? Collections.<String>emptyList() : this.sysMacros.getMacros();
    }

    Collection<String> getUserMacroDefinitions() {
        return this.getMacros();
    }

    public static final class FileStateImpl extends StateImpl {

        private final ClankSystemMacroMap sysMacros;

        private FileStateImpl(ClankFileMacroMap macroMap) {
            super(macroMap);
            this.sysMacros = macroMap.sysMacros;
        }

        private FileStateImpl(FileStateImpl other, boolean cleaned) {
            super(other, cleaned);
            this.sysMacros = other.sysMacros;
        }
        ////////////////////////////////////////////////////////////////////////
        // persistence support

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
        }

        public FileStateImpl(RepositoryDataInput input) throws IOException {
            super(input);
            // TODO
            this.sysMacros = null;
        }

        protected void restoreTo(ClankFileMacroMap macroMap) {
            super.restoreTo(macroMap);
            if (this.sysMacros != null) {
                macroMap.sysMacros = this.sysMacros;
            }
        }

        @Override
        public State copyCleaned() {
            return super.cleaned ? this : new FileStateImpl(this, true);
        }

        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("FileState\n"); // NOI18N
            retValue.append("Parent\n"); // NOI18N
            retValue.append(super.toString());
            retValue.append("\nSystem MacroMap\n"); // NOI18N
            if (System.getProperty("cnd.apt.macro.trace") != null) {
                retValue.append(sysMacros);
            } else if (sysMacros == null) {
                retValue.append("null"); // NOI18N
            } else {
                retValue.append(System.identityHashCode(sysMacros));
            }
            return retValue.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("Own Map:\n"); // NOI18N
        retValue.append(super.toString());
        retValue.append("System Map:\n"); // NOI18N
        retValue.append(sysMacros);
        return retValue.toString();
    }
}
