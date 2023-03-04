/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.javascript.debugger.eval;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.debug.NamesTranslator;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;

/**
 *
 * @author martin
 */
public final class VarNamesTranslatorFactory {
    
    private static final Map<CallFrame, VarNamesTranslatorFactory> TRANSLATORS = new WeakHashMap<>();
    
    private final NamesTranslator namesTranslator;
    
    private VarNamesTranslatorFactory(Debugger debugger, Project project, String scriptName, String functionName, int lineNumber, int columnNumber) {
        this.namesTranslator = MiscEditorUtil.createNamesTranslator(debugger, project, scriptName, lineNumber, columnNumber);
    }
    
    public static VarNamesTranslatorFactory get(CallFrame frame, Debugger debugger, Project project) {
        VarNamesTranslatorFactory vmt;
        synchronized (TRANSLATORS) {
            vmt = TRANSLATORS.get(frame);
            if (vmt == null) {
                vmt = new VarNamesTranslatorFactory(debugger, project, frame.getScript().getURL(), frame.getFunctionName(), frame.getLineNumber(), frame.getColumnNumber());
                TRANSLATORS.put(frame, vmt);
            }
        }
        return vmt;
    }
    
    public NamesTranslator getNamesTranslator() {
        return namesTranslator;
    }
}
