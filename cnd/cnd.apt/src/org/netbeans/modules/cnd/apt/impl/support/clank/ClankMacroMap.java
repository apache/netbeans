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
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.apt.impl.support.SupportAPIAccessor;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public class ClankMacroMap implements PPMacroMap {

    private long startCRC;
    private List<String> macros;

//    /** Creates a new instance of ClankSystemMacroMap */
    protected ClankMacroMap(long crc) {
        startCRC = crc;
        this.macros = Collections.emptyList();
    }

    public ClankMacroMap(List<String> sysMacros) {
        startCRC = calculateCRC(sysMacros);
        this.macros = Collections.unmodifiableList(sysMacros);
    }

    //@Override
    public long getCompilationUnitCRC() {
        return startCRC;
    }

    protected Collection<String> getMacros() {
        return this.macros;
    }

    protected static long calculateCRC(List<String> sysMacros) {
        Checksum checksum = new Adler32();
        for (String s : sysMacros) {
            checksum.update(s.getBytes(SupportAPIAccessor.INTERNAL_CHARSET), 0, s.length());
        }
        return checksum.getValue();
    }

    @Override
    public State getState() {
        return new StateImpl(this);
    }

    @Override
    public void setState(State state) {
        ((StateImpl)state).restoreTo(this);
    }

    public static class StateImpl implements State {

        private final List<String> macros;
        private long startCRC;
        protected final boolean cleaned;

        protected StateImpl(ClankMacroMap macroMap) {
            this.macros = macroMap.macros;
            this.startCRC = macroMap.startCRC;
            this.cleaned = false;
        }

        protected StateImpl(StateImpl other, boolean cleaned) {
            this.macros = other.macros;
            this.startCRC = other.startCRC;
            this.cleaned = cleaned;
        }

        ////////////////////////////////////////////////////////////////////////
        // persistence support
        public void write(RepositoryDataOutput output) throws IOException {
            // TODO
            output.writeLong(this.startCRC);
        }

        protected StateImpl(RepositoryDataInput input) throws IOException {
            // TODO
            this.startCRC = input.readLong();
            this.macros = Collections.emptyList();
            this.cleaned = true;
        }

        protected void restoreTo(ClankMacroMap macroMap) {
            if (!cleaned) {
                macroMap.macros = this.macros;
            }
            macroMap.startCRC = this.startCRC;
        }

        public State copyCleaned() {
            return cleaned ? this : new StateImpl(this, true);
        }

        @Override
        public String toString() {
            return APTUtils.macros2String(macros);
        }
    }

    @Override
    public String toString() {
        return APTUtils.macros2String(macros);
    }
}
