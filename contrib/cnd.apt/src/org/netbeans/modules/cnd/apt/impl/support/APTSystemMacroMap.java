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

import java.util.List;
import java.util.logging.Level;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
public class APTSystemMacroMap extends APTBaseMacroMap {

    private APTMacroMap preMacroMap;
    private final long startCRC;

//    /** Creates a new instance of APTSystemMacroMap */
    protected APTSystemMacroMap(long crc) {
        preMacroMap = new APTPredefinedMacroMap();
        startCRC = crc;
    }

    public APTSystemMacroMap(List<String> sysMacros) {
        this(calculateCRC(sysMacros));
        fill(sysMacros, true);
    }

    @Override
    protected APTMacro createMacro(CharSequence file, APTDefine define, Kind macroType) {
        return new APTMacroImpl(file, define, macroType);
    }

    @Override
    public boolean pushPPDefined() {
        APTUtils.LOG.log(Level.SEVERE, "pushPPDefined is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    @Override
    public boolean popPPDefined() {
        APTUtils.LOG.log(Level.SEVERE, "popPPDefined is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    @Override
    public boolean pushExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "pushExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }

    @Override
    public void popExpanding() {
        APTUtils.LOG.log(Level.SEVERE, "popExpanding is not supported", new IllegalAccessException());// NOI18N
    }

    @Override
    public boolean isExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "isExpanding is not supported", new IllegalAccessException());// NOI18N
        return false;
    }

    @Override
    public APTMacro getMacro(APTToken token) {
        APTMacro res = super.getMacro(token);

        if(res == null) {
            res = preMacroMap.getMacro(token);
        }
        // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }

    @Override
    protected APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent) {
        assert parent == null : "parent must be null";
        return new APTMacroMapSnapshot((APTMacroMapSnapshot)null);
    }

    @Override
    public void define(APTFile file, APTDefine define, Kind macroType) {
        throw new UnsupportedOperationException("Can not modify immutable System macro map"); // NOI18N
    }

    @Override
    public void undef(APTFile file, APTToken name) {
        throw new UnsupportedOperationException("Can not modify immutable System macro map"); // NOI18N
    }

    //@Override
    public long getCompilationUnitCRC() {
        return startCRC;
    }

    private static long calculateCRC(List<String> sysMacros) {
	Checksum checksum = new Adler32();
	for( String s : sysMacros ) {
             checksum.update(s.getBytes(SupportAPIAccessor.INTERNAL_CHARSET), 0, s.length());
	}
	return checksum.getValue();
    }
}
