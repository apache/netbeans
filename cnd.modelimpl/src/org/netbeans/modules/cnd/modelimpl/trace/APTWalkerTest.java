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
