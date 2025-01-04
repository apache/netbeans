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

package org.netbeans.modules.lsp.client.debugger;

import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;

public final class DAPUtils {

    private static final Logger logger = Logger.getLogger(DAPUtils.class.getName());

    private static Object currentLine;

    private DAPUtils() {
    }

    static synchronized void markCurrent (final Object line) {
        unmarkCurrent ();

        Annotatable[] annotatables = (Annotatable[]) line;
        int i = 0, k = annotatables.length;

        // first line with icon in gutter
        DebuggerAnnotation[] annotations = new DebuggerAnnotation [k];
        if (annotatables [i] instanceof Line.Part) {
            annotations [i] = new DebuggerAnnotation (
                DebuggerAnnotation.CURRENT_LINE_PART_ANNOTATION_TYPE,
                annotatables [i]
            );
        } else {
            annotations [i] = new DebuggerAnnotation (
                DebuggerAnnotation.CURRENT_LINE_ANNOTATION_TYPE,
                annotatables [i]
            );
        }

        // other lines
        for (i = 1; i < k; i++)
            if (annotatables [i] instanceof Line.Part)
                annotations [i] = new DebuggerAnnotation (
                    DebuggerAnnotation.CALL_STACK_FRAME_ANNOTATION_TYPE,
                    annotatables [i]
                );
            else
                annotations [i] = new DebuggerAnnotation (
                    DebuggerAnnotation.CALL_STACK_FRAME_ANNOTATION_TYPE,
                    annotatables [i]
                );
        currentLine = annotations;

        showLine (line);
    }

    static synchronized void unmarkCurrent () {
        if (currentLine != null) {

//            ((DebuggerAnnotation) currentLine).detach ();
            int i, k = ((DebuggerAnnotation[]) currentLine).length;
            for (i = 0; i < k; i++) {
                ((DebuggerAnnotation[]) currentLine) [i].detach ();
            }
            currentLine = null;
        }
    }

    public static void showLine (final Object line) {
        final Annotatable[] a = (Annotatable[]) line;
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                if (a[0] instanceof Line) {
                    ((Line) a[0]).show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                } else if (a[0] instanceof Line.Part) {
                    ((Line.Part) a[0]).getLine ().show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                } else {
                    throw new InternalError(a[0].toString());
                }
            }
        });
    }

    public static boolean contains (Object currentLine, Line line) {
        if (currentLine == null) return false;
        final Annotatable[] a = (Annotatable[]) currentLine;
        int i, k = a.length;
        for (i = 0; i < k; i++) {
            if (a [i].equals (line)) return true;
            if ( a [i] instanceof Line.Part &&
                 ((Line.Part) a [i]).getLine ().equals (line)
            ) return true;
        }
        return false;
    }

}
