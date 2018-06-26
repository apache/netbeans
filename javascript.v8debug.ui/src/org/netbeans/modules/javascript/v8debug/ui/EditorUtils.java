/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.ui;

import java.awt.EventQueue;
import java.util.List;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.modules.javascript.v8debug.ScriptsHandler;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 *
 * @author Martin Entlicher
 */
public final class EditorUtils {
    
    public static final String CURRENT_LINE_ANNOTATION_TYPE =  "CurrentPC"; //NOI18N
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =  "CallSite"; //NOI18N
    
    private EditorUtils() {}
    
    public static Line getLine(V8Debugger dbg, FileObject fo, int line, int column) {
        SourceMapsTranslator smtr = dbg.getScriptsHandler().getSourceMapsTranslator();
        if (smtr != null) {
            if (column < 0) {
                column = 0; // If column not defined, set it to zero
            } else if (line == 0) {
                column -= dbg.getScriptsHandler().getScriptFirstLineColumnShift(fo);
            }
            SourceMapsTranslator.Location loc = new SourceMapsTranslator.Location(fo, line, column);
            loc = smtr.getSourceLocation(loc);
            fo = loc.getFile();
            line = loc.getLine();
        }
        LineCookie lineCookie = fo.getLookup().lookup(LineCookie.class);
        try {
            return lineCookie.getLineSet().getCurrent(line);
        } catch (IndexOutOfBoundsException ioob) {
            List<? extends Line> lines = lineCookie.getLineSet().getLines();
            if (lines.size() > 0) {
                return lines.get(lines.size() - 1);
            } else {
                return null;
            }
        }
    }
    
    public static void showLine(final Line line, final boolean toFront) {
        if (line == null) {
            return;
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                line.show(Line.ShowOpenType.REUSE, 
                          toFront ? Line.ShowVisibilityType.FRONT :
                                    Line.ShowVisibilityType.FOCUS);
            }
        });
    }

    public static void showLine(final Line line) {
        showLine(line, false);
    }
    
    public static void showFrameLine(V8Debugger dbg, CallFrame frame, boolean toFront) {
        V8Frame f = frame.getFrame();
        V8Script script = frame.getScript();
        if (script == null) {
            return ;
        }
        ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
        FileObject fo = scriptsHandler.getFile(script);
        Line line = EditorUtils.getLine(dbg, fo, (int) f.getLine(), (int) f.getColumn());
        if (line != null) {
            EditorUtils.showLine(line, toFront);
        }
    }

}
