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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.apt.impl.support.APTHandlersSupportImpl;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.utils.FSPath;

/**
 * utilities for working with APT states (macro-state, include-state, preproc-state)
 */
public class APTHandlersSupport {

    private APTHandlersSupport() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // factory methods for handlers
    public static PreprocHandler createPreprocHandler(PPMacroMap macroMap, PPIncludeHandler inclHandler, boolean compileContext, CharSequence lang, CharSequence flavor) {
        return APTHandlersSupportImpl.createPreprocHandler(macroMap, inclHandler, compileContext, lang, flavor);
    }
    
    public static PreprocHandler createEmptyPreprocHandler(StartEntry file) {
        return APTHandlersSupportImpl.createEmptyPreprocHandler(file);
    }

    public static void invalidatePreprocHandler(PreprocHandler preprocHandler) {
        APTHandlersSupportImpl.invalidatePreprocHandler(preprocHandler);
    }
 
    public static PPIncludeHandler createIncludeHandler(StartEntry startFile, List<IncludeDirEntry> sysIncludePaths, List<IncludeDirEntry> userIncludePaths, List<FSPath> includeFileEntries, APTFileSearch fileSearch) {
        return APTHandlersSupportImpl.createIncludeHandler(startFile, sysIncludePaths, userIncludePaths, includeFileEntries, fileSearch);
    }

    public static long getCompilationUnitCRC(PreprocHandler preprocHandler){
        return APTHandlersSupportImpl.getCompilationUnitCRC(preprocHandler);
    }

    public static PPMacroMap createMacroMap(PPMacroMap sysMap, List<String> userMacros) {
        return APTHandlersSupportImpl.createMacroMap(sysMap, userMacros);
    }
    
    public static PPMacroMap.State extractMacroMapState(PreprocHandler.State state){
        return APTHandlersSupportImpl.extractMacroMapState(state);
    }
    
    public static PreprocHandler.StateKey getStateKey(PreprocHandler.State state){
        return APTHandlersSupportImpl.getStateKey(state);
    }

    public static int getIncludeStackDepth(PreprocHandler.State state) {
        return APTHandlersSupportImpl.getIncludeStackDepth(state);
    }
    
    public static APTFile.Kind getAPTFileKind(PreprocHandler.State state) {
        String language = (state == null) ? "" : state.getLanguage().toString();
        String languageFlavor = (state == null) ? "" : state.getLanguageFlavor().toString();     
        return APTDriver.langFlavorToAPTFileKind(language, languageFlavor);
    }
    
    public static APTFile.Kind getAPTFileKind(PreprocHandler handler) {
        String language = (handler == null) ? "" : handler.getLanguage().toString();
        String languageFlavor = (handler == null) ? "" : handler.getLanguageFlavor().toString();     
        return APTDriver.langFlavorToAPTFileKind(language, languageFlavor);
    }
    ////////////////////////////////////////////////////////////////////////////
    // help methods for preprocessor states
//    public static PreprocHandler.State copyPreprocState(PreprocHandler.State orig) {
//        return APTHandlersSupportImpl.copyPreprocState(orig);
//    }

    public static PreprocHandler.State preparePreprocStateCachesIfPossible(PreprocHandler.State orig) {
        return APTHandlersSupportImpl.preparePreprocStateCachesIfPossible(orig);
    }

    public static PreprocHandler.State createCleanPreprocState(PreprocHandler.State orig) {
        return APTHandlersSupportImpl.createCleanPreprocState(orig);
    }
    
    public static LinkedList<PPIncludeHandler.IncludeInfo> extractIncludeStack(PreprocHandler.State state) {
        return APTHandlersSupportImpl.extractIncludeStack(state);
    }
    
    public static StartEntry extractStartEntry(PreprocHandler.State state) {
	return APTHandlersSupportImpl.extractStartEntry(state);
    }
    
    public static PreprocHandler.State createInvalidPreprocState(PreprocHandler.State orig) {
        return APTHandlersSupportImpl.createInvalidPreprocState(orig);
    }

    public static boolean equalsIgnoreInvalid(PreprocHandler.State state1, PreprocHandler.State state2) {
        return APTHandlersSupportImpl.equalsIgnoreInvalid(state1, state2);
    }
}
