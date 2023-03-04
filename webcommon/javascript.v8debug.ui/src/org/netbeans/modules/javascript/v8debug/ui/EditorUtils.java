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
