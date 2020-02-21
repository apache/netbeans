/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
