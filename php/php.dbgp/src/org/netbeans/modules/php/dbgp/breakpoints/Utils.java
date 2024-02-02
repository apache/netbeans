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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.FunctionBreakpoint.Type;
import org.netbeans.modules.php.dbgp.packets.BrkpntCommandBuilder;
import org.netbeans.modules.php.dbgp.packets.BrkpntRemoveCommand;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand.State;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

/**
 * @author ads
 *
 */
public final class Utils {
    //keep synchronized with PHPOptionsCategory.PATH_IN_LAYER
    public static final String PATH_IN_LAYER = "org-netbeans-modules-php-project-ui-options-PHPOptionsCategory/Debugger"; //NOI18N
    static final String MIME_TYPE = "text/x-php5"; //NOI18N
    private static LineFactory lineFactory = new LineFactory();

    private Utils() {
    }

    public static void setLineFactory(LineFactory lineFactory) {
        Utils.lineFactory = lineFactory;
    }

    /**
     * Get the current line.
     *
     * @return the current line if the file is php, otherwise {@code null}.
     */
    @CheckForNull
    public static Line getCurrentLine() {
        FileObject fileObject = EditorContextDispatcher.getDefault().getCurrentFile();

        if (!isPhpFile(fileObject)) {
            return null;
        }

        return EditorContextDispatcher.getDefault().getCurrentLine();
    }

    /**
     * Get the Line from FileObject.
     *
     * @param file the FileObject
     * @param lineNumber the line number
     * @return the line if it is found with the file and the line number,
     * otherwise {@code null}
     */
    @CheckForNull
    public static Line getLine(FileObject file, int lineNumber) {
        if (file == null || lineNumber < 0) {
            return null;
        }

        DataObject dataObject;
        try {
            dataObject = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) {
            return null;
        }
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) {
            return null;
        }
        Line.Set ls = lineCookie.getLineSet();
        if (ls == null) {
            return null;
        }
        try {
            return ls.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static BrkpntSetCommand getCommand(DebugSession session, SessionId id, AbstractBreakpoint breakpoint) {
        if (!breakpoint.isSessionRelated(session)) {
            return null;
        }
        BrkpntSetCommand command = null;
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
            command = BrkpntCommandBuilder.buildLineBreakpoint(id, session.getTransactionId(), lineBreakpoint);
        } else if (breakpoint instanceof FunctionBreakpoint) {
            FunctionBreakpoint functionBreakpoint = (FunctionBreakpoint) breakpoint;
            Type type = functionBreakpoint.getType();
            if (type == Type.CALL) {
                command = BrkpntCommandBuilder.buildCallBreakpoint(session.getTransactionId(), functionBreakpoint);
            } else if (type == Type.RETURN) {
                command = BrkpntCommandBuilder.buildReturnBreakpoint(session.getTransactionId(), functionBreakpoint);
            } else {
                assert false;
            }
        } else if (breakpoint instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint exceptionBreakpoint = (ExceptionBreakpoint) breakpoint;
            command = BrkpntCommandBuilder.buildExceptionBreakpoint(session.getTransactionId(), exceptionBreakpoint);
        }
        if (command == null) {
            breakpoint.setInvalid();    // No command, can not be valid
        }
        if (!breakpoint.isEnabled()) {
            command.setState(State.DISABLED);
        }
        return command;
    }

    public static AbstractBreakpoint getBreakpoint(String id) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof AbstractBreakpoint)) {
                continue;
            }
            AbstractBreakpoint bkpnt = (AbstractBreakpoint) breakpoint;
            String bkpntId = bkpnt.getBreakpointId();
            if (id.equals(bkpntId)) {
                return bkpnt;
            }
        }
        return null;
    }

    public static void cleanBreakpoint(DebugSession session, String breakpointId) {
        BrkpntRemoveCommand removeCommand = new BrkpntRemoveCommand(session.getTransactionId(), breakpointId);
        session.sendCommandLater(removeCommand);
    }

    public static boolean isPhpFile(FileObject fileObject) {
        if (fileObject == null) {
            return false;
        } else {
            String mimeType = fileObject.getMIMEType();
            return MIME_TYPE.equals(mimeType);
        }
    }

    /**
     * NB :
     * <code>line</code> is 1-based debugger DBGP line. It differs from editor
     * line !
     *
     * @param line 1-based line in file
     * @param remoteFileName remote file name
     * @param id current debugger session id
     * @return
     */
    public static Line getLine(int line, String remoteFileName, SessionId id) {
        return lineFactory.getLine(line, remoteFileName, id);
    }

    public static class LineFactory {
        public Line getLine(int line, String remoteFileName, SessionId id) {
            DataObject dataObject = Utils.getDataObjectByRemote(id, remoteFileName);
            if (dataObject == null) {
                return null;
            }
            LineCookie lineCookie = (LineCookie) dataObject.getLookup().lookup(LineCookie.class);
            if (lineCookie == null) {
                return null;
            }
            Line.Set set = lineCookie.getLineSet();
            if (set == null) {
                return null;
            }
            try {
                return set.getCurrent(line - 1);
            } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                Logger.getLogger(Utils.class.getName()).log(Level.FINE, e.getMessage(), e);
            }
            return null;
        }

    }

    public static void openPhpOptionsDialog() {
        OptionsDisplayer.getDefault().open(PATH_IN_LAYER);
    }

    public static DataObject getDataObjectByRemote(SessionId id, String uri) {
        try {
            FileObject fileObject = id.toSourceFile(uri);
            if (fileObject == null) {
                return null;
            }
            return DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * Test whether the line is in PHP source.
     * @param line The line to test
     * @return <code>true</code> when the line is in PHP source, <code>false</code> otherwise.
     */
    public static boolean isInPhpScript(Line line) {
        FileObject fo = line.getLookup().lookup(FileObject.class);
        if (!isPhpFile(fo)) {
            return false;
        }
        Set<String> mimeTypesOnLine = EditorContextDispatcher.getDefault().getMIMETypesOnLine(line);
        return mimeTypesOnLine.contains(MIME_TYPE);
    }

    public static boolean isValid(Breakpoint breakpoint) {
        VALIDITY validity = breakpoint.getValidity();
        return validity == VALIDITY.VALID || validity == VALIDITY.UNKNOWN;
    }
}
