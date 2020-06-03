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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 */
public class MacroSupport {
    // utility class
    private MacroSupport() {
    }
    
    public static String expandMacro(NativeDebugger debugger, String expr) {
        Frame currentFrame = debugger.getCurrentFrame();
        if (currentFrame != null) {
            StyledDocument doc = getDocument(debugger, currentFrame);
            if (doc != null) {
                int offset = getOffset(currentFrame, doc);
                if (offset >= 0) {
                    String expand = CsmMacroExpansion.expand(doc, offset, expr);
                    if (expand != null) {
                        return expand;
                    }
                }
            }
        }
        return expr;
    }
    
    private static StyledDocument getDocument(NativeDebugger debugger, Frame frame) {
        String fullPath = frame.getFullPath();
        if (fullPath == null) {
            return null;
        }
        FileObject fo = EditorBridge.findFileObject(fullPath, debugger);
        if (fo != null /*paranoia*/ && fo.isValid()) {
            return (StyledDocument) CsmUtilities.getDocument(fo);
        }
        return null;
    }

    private static int getOffset(Frame frame, StyledDocument doc) {
        try {
            int lineNumber = Integer.parseInt(frame.getLineNo());
            if (lineNumber >= 0 && doc != null) {
                return NbDocument.findLineOffset(doc, lineNumber-1);
            }
        } catch(NumberFormatException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IndexOutOfBoundsException ex) {
            return doc.getLength();
        }
        return -1;
    }
}
