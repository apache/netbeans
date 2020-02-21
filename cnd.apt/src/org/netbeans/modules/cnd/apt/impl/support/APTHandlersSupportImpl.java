/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.apt.impl.support.clank.ClankIncludeHandlerImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTBaseMacroMap.StateImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap.FileStateImpl;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankSystemMacroMap;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler.State;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.util.CharSequences;

/**
 * utilities for working with APT states (macro-state, include-state, preproc-state)
 */
public class APTHandlersSupportImpl {

    /** Prevents creation of an instance of APTHandlersSupportImpl */
    private APTHandlersSupportImpl() {
    }

    public static APTPreprocHandler createPreprocHandler(PPMacroMap macroMap, PPIncludeHandler inclHandler, boolean compileContext, CharSequence lang, CharSequence flavor) {
        return new APTPreprocHandlerImpl(macroMap, inclHandler, compileContext, lang, flavor);
    }

    public static APTPreprocHandler createEmptyPreprocHandler(StartEntry file) {
        return new APTPreprocHandlerImpl(
                APTTraceFlags.USE_CLANK ? new ClankFileMacroMap() : new APTFileMacroMap(), 
                APTTraceFlags.USE_CLANK ? new ClankIncludeHandlerImpl(file) : new APTIncludeHandlerImpl(file), 
                false, CharSequences.empty(), CharSequences.empty());
    }

    public static void invalidatePreprocHandler(PreprocHandler preprocHandler) {
        ((APTPreprocHandlerImpl)preprocHandler).setValid(false);
    }

    public static PPIncludeHandler createIncludeHandler(StartEntry startFile, List<IncludeDirEntry> sysIncludePaths, List<IncludeDirEntry> userIncludePaths, List<FSPath> includeFileEntries, APTFileSearch fileSearch) {
        // for now prepare IncludeDirEntry for "-include file" elements
        List<IncludeDirEntry> fileEntries = new ArrayList<IncludeDirEntry>(0);
        for (FSPath file : includeFileEntries) {
            fileEntries.add(IncludeDirEntry.get(file, false, true));
        }
        if (APTTraceFlags.USE_CLANK) {
            return new ClankIncludeHandlerImpl(startFile, sysIncludePaths, userIncludePaths, fileEntries, fileSearch);
        } else {
            return new APTIncludeHandlerImpl(startFile, sysIncludePaths, userIncludePaths, fileEntries, fileSearch);
        }
    }
    
    public static long getCompilationUnitCRC(PreprocHandler preprocHandler){
        if (preprocHandler instanceof APTPreprocHandlerImpl) {
            return ((APTPreprocHandlerImpl)preprocHandler).getCompilationUnitCRC();
        }
        return 0;
    }

    public static PPMacroMap createMacroMap(PPMacroMap sysMap, List<String> userMacros) {
        PPMacroMap fileMap;
        if (APTTraceFlags.USE_CLANK) {
            fileMap = new ClankFileMacroMap((ClankSystemMacroMap)sysMap, userMacros);
        } else {
            fileMap = new APTFileMacroMap((APTMacroMap)sysMap, userMacros);
        }
        return fileMap;
    }

    public static APTPreprocHandler.State preparePreprocStateCachesIfPossible(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).prepareCachesIfPossible();
    }

    public static APTPreprocHandler.State createCleanPreprocState(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).copyCleaned();
    }
    
    public static APTPreprocHandler.State createInvalidPreprocState(APTPreprocHandler.State orig) {
        return ((APTPreprocHandlerImpl.StateImpl)orig).copyInvalid();
    }

    public static boolean equalsIgnoreInvalid(State state1, State state2) {
        if (state1 instanceof APTPreprocHandlerImpl.StateImpl) {
            return ((APTPreprocHandlerImpl.StateImpl) state1).equalsIgnoreInvalidFlag(state2);
        } else if (state2 instanceof APTPreprocHandlerImpl.StateImpl) {
            return ((APTPreprocHandlerImpl.StateImpl) state2).equalsIgnoreInvalidFlag(state1);
        } else {
            return state1.equals(state2);
        }
    }

    public static boolean isFirstLevel(PPIncludeHandler includeHandler) {
        if (includeHandler == null) {
          return false;
        }
        if (includeHandler instanceof ClankIncludeHandlerImpl) {
            assert APTTraceFlags.USE_CLANK;
            return ((ClankIncludeHandlerImpl) includeHandler).isFirstLevel();
        } else {
            assert !APTTraceFlags.USE_CLANK;
            return ((APTIncludeHandlerImpl) includeHandler).isFirstLevel();
        }
    }

    public static Collection<IncludeDirEntry> extractIncludeFileEntries(PPIncludeHandler includeHandler) {
        assert !APTTraceFlags.USE_CLANK;
        Collection<IncludeDirEntry> out = new ArrayList<IncludeDirEntry>(0);
        if (includeHandler != null) {
            return ((APTIncludeHandlerImpl) includeHandler).getUserIncludeFilePaths();
        }
        return out;
    }

    public static APTBaseMacroMap.State extractMacroMapState(APTPreprocHandler.State state){
        assert state != null;
        return (StateImpl) ((APTPreprocHandlerImpl.StateImpl)state).macroState;
    }

    public static long getCompilationUnitCRC(PPMacroMap map){
        assert map != null;
        if (map instanceof APTFileMacroMap) {
            return ((APTFileMacroMap)map).getCompilationUnitCRC();
        } else if (map instanceof ClankFileMacroMap) {
            return ((ClankFileMacroMap)map).getCompilationUnitCRC();
        }
        return 0;
    }

    public static PPIncludeHandler.State extractIncludeState(APTPreprocHandler.State state) {
        assert state != null;
        return ((APTPreprocHandlerImpl.StateImpl) state).inclState;
    }
    
    public static StateKeyImpl getStateKey(APTPreprocHandler.State state){
        assert state != null;
        APTFileMacroMap.FileStateImpl macro = (FileStateImpl) ((APTPreprocHandlerImpl.StateImpl)state).macroState;
        StartEntry extractStartEntry = extractStartEntry(((APTPreprocHandlerImpl.StateImpl)state).inclState);
        return macro.getStateKey(extractStartEntry == null ? null : extractStartEntry.getStartFileProject());
    }

    public static int getIncludeStackDepth(APTPreprocHandler.State state) {
        assert state != null;
        PPIncludeHandler.State inclState = ((APTPreprocHandlerImpl.StateImpl) state).inclState;
        if (inclState == null) {
            return 0;
        }
        if (inclState instanceof APTIncludeHandlerImpl.StateImpl) {
            APTIncludeHandlerImpl.StateImpl incl = (APTIncludeHandlerImpl.StateImpl) inclState;
            return incl.getIncludeStackDepth();
        } else {
            ClankIncludeHandlerImpl.StateImpl incl = (ClankIncludeHandlerImpl.StateImpl) inclState;
            return incl.getIncludeStackDepth();
        }
    }

    public static LinkedList<PPIncludeHandler.IncludeInfo> extractIncludeStack(PreprocHandler.State state) {
        assert state != null;
        Collection<PPIncludeHandler.IncludeInfo> inclStack = getIncludeStack(((APTPreprocHandlerImpl.StateImpl)state).inclState);
        // return copy to prevent modification of frozen state objects
        return inclStack == null ? new LinkedList<PPIncludeHandler.IncludeInfo>() : new LinkedList<PPIncludeHandler.IncludeInfo>(inclStack);
    }

    public static StartEntry extractStartEntry(PreprocHandler.State state) {
        return (state == null) ? null : extractStartEntry(((APTPreprocHandlerImpl.StateImpl)state).inclState);
    }
    
//    public static APTPreprocHandler.State copyPreprocState(APTPreprocHandler.State orig) {
//        return ((APTPreprocHandlerImpl.StateImpl)orig).copy();
//    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    private static StartEntry extractStartEntry(PPIncludeHandler.State state) {
        if (state == null) {
            return null;
        }
        if (state instanceof ClankIncludeHandlerImpl.StateImpl) {
            return ((ClankIncludeHandlerImpl.StateImpl) state).getStartEntry();
        } else {
            return ((APTIncludeHandlerImpl.StateImpl) state).getStartEntry();
        }
    }
    
    private static Collection<PPIncludeHandler.IncludeInfo> getIncludeStack(PPIncludeHandler.State inclState) {
        if (inclState == null) {
            return null;
        }
        if (inclState instanceof ClankIncludeHandlerImpl.StateImpl) {
            return ((ClankIncludeHandlerImpl.StateImpl)inclState).getIncludeStack();
        } else {
            return ((APTIncludeHandlerImpl.StateImpl)inclState).getIncludeStack();
        }
    }
    
    /*package*/ static PPIncludeHandler.State copyCleanIncludeState(PPIncludeHandler.State inclState) {
        if (inclState == null) {
          return null;
        }      
        if (inclState instanceof ClankIncludeHandlerImpl.StateImpl) {
            return ((ClankIncludeHandlerImpl.StateImpl)inclState).copyCleaned();
        } else {
            return ((APTIncludeHandlerImpl.StateImpl)inclState).copyCleaned();
        }
    }

    /*package*/ static PPIncludeHandler.State prepareIncludeStateCachesIfPossible(PPIncludeHandler.State inclState) {
        if (inclState == null) {
          return null;
        }
        if (inclState instanceof ClankIncludeHandlerImpl.StateImpl) {
            return ((ClankIncludeHandlerImpl.StateImpl)inclState).prepareCachesIfPossible();
        } else {
            return ((APTIncludeHandlerImpl.StateImpl)inclState).prepareCachesIfPossible();
        }
    }

    /*package*/ static APTMacroMap.State createCleanMacroState(APTMacroMap.State macroState) {
        APTMacroMap.State out = null;
        if (macroState != null) {
            if (macroState instanceof ClankMacroMap.StateImpl) {
                out = ((ClankMacroMap.StateImpl)macroState).copyCleaned();
            } else {
                out = ((APTBaseMacroMap.StateImpl)macroState).copyCleaned();
            }
        }
        return out;
    }

    public static final class StateKeyImpl implements APTPreprocHandler.StateKey {

        private final int crc1, crc2;
        private final Key startProjectKey;
        private final int hashCode;

        public StateKeyImpl(int crc1, int crc2, long crcSys, Key startProjectKey) {
            this.crc1 = crc1 ^ (int)(crcSys & 0x0000FFFFL);
            this.crc2 = crc2 ^ (int)(crcSys>>32);
            this.startProjectKey = startProjectKey;
            int hash = startProjectKey == null ? -1 : startProjectKey.hashCode();
            this.hashCode = crc1 ^ crc2 ^ hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StateKeyImpl)) {
                return false;
            }
            final StateKeyImpl other = (StateKeyImpl) obj;
            if (this.hashCode != other.hashCode) {
                return false;
            }
            if (this.crc1 != other.crc1) {
                return false;
            }
            if (this.crc2 != other.crc2) {
                return false;
            }
            if (this.startProjectKey != other.startProjectKey && (this.startProjectKey == null || !this.startProjectKey.equals(other.startProjectKey))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "<" + crc1 + "," + crc2 + ">" + "from " + startProjectKey; // NOI18N
        }
    }

}
