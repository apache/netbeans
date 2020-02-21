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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.Location;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/**
 *
 */
public class DisassemblyUtils {
    private DisassemblyUtils() {
    }
    
    private static int getAddressLine(String address) {
        Disassembly dis = Disassembly.getCurrent();
        if (dis == null) {
            return -1;
        }
        return dis.getAddressLine(address);
    }

    public static String getLineAddress(int lineNo) {
        Disassembly dis = Disassembly.getCurrent();
        if (dis == null) {
            return null;
        }
        return dis.getLineAddress(lineNo);
    }

    public static boolean showLine(int line) {
        if (line != -1) {
            FileObject fo = Disassembly.getFileObject();
            if (fo != null) {
                try {
                    Line disLine = Disassembly.getLine(line);
                    EditorBridge.showInEditor(disLine);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
//                try {
//                    return EditorContextBridge.getContext().showSource(DataObject.find(fo), line, null);
//                } catch (DataObjectNotFoundException dex) {
//                    // do nothing
//                }
            }
        } else {
            Disassembly.open();
        }
        return false;
    }

    public static boolean showAddress(String address) {
        return showLine(getAddressLine(address));
    }

    public static void annotatePC(Location location, DebuggerAnnotation pcMarker, boolean andShow) {
        Disassembly dis = Disassembly.getCurrent();
        if (dis == null) {
            return;
        }
        int line = dis.getAddressLine(location.pc());
        if (line != -1) {
            FileObject fo = Disassembly.getFileObject();
            if (fo != null) {
                try {
                    Line disLine = Disassembly.getLine(line);
                    if (andShow) {
                        EditorBridge.showInEditor(disLine);
                    }
                    pcMarker.setLine(disLine, true);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                DisInfoPanel.setLocation(location);
            }
        } else {
            pcMarker.setLine(null, true);
        }
    }
}
