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
