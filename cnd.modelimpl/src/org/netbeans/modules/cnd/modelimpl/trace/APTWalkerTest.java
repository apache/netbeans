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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.openide.filesystems.FileObject;

/**
 * simple test implementation of walker
 */
public class APTWalkerTest extends APTAbstractWalker {

    public APTWalkerTest(APTFile apt, PreprocHandler ppHandler) {
        super(apt, ppHandler, null);
    }

    private long resolvingTime = 0;
    private long lastTime = 0;
    public long getIncludeResolvingTime() {
        return resolvingTime;
    }

    @Override
    protected void onInclude(APT apt) {
        lastTime = System.currentTimeMillis();
        super.onInclude(apt);
    }

    @Override
    protected void onIncludeNext(APT apt) {
        lastTime = System.currentTimeMillis();
        super.onIncludeNext(apt);
    }

    @Override
    protected boolean needPPTokens() {
        return TraceFlags.PARSE_HEADERS_WITH_SOURCES;
    }
    
    @Override
    protected boolean include(ResolvedPath resolvedPath, IncludeState inclState, APTInclude aptInclude, PostIncludeData postIncludeState) {
        resolvingTime += System.currentTimeMillis() - lastTime;
        if (inclState == IncludeState.Success) {
            try {
                FileObject resolvedFO = resolvedPath.getFileObject();
                if (resolvedFO != null && resolvedFO.isValid()) {
                    APTFile.Kind aptKind = APTHandlersSupport.getAPTFileKind(getPreprocHandler());
                    if (isTokenProducer() && TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                        APTFile apt = APTDriver.findAPT(ModelSupport.createFileBuffer(resolvedFO), aptKind);
                        APTWalkerTest walker = new APTWalkerTest(apt, getPreprocHandler());
                        includeStream(apt, walker);
                        resolvingTime += walker.resolvingTime;
                    } else {
                        APTFile apt = APTDriver.findAPTLight(ModelSupport.createFileBuffer(resolvedFO), aptKind);
                        APTWalkerTest walker = new APTWalkerTest(apt, getPreprocHandler());
                        walker.visit();
                        resolvingTime += walker.resolvingTime;
                    }
                }
            } catch (IOException ex) {
                DiagnosticExceptoins.register(ex);
                APTUtils.LOG.log(Level.SEVERE, "error on include " + resolvedPath, ex);// NOI18N
            }
            return (postIncludeState == null) || !postIncludeState.hasPostIncludeMacroState();
        } else {
            return false;
        }
    }

    @Override
    protected boolean hasIncludeActionSideEffects() {
        return true;
    }
}
