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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.LineBreakpoint;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand.Types;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 * @author ads
 *
 */
public final class BrkpntCommandBuilder {
    private BrkpntCommandBuilder() {
    }

    public static BrkpntSetCommand buildLineBreakpoint(SessionId id, String transactionId, FileObject localFile, int lineNumber) {
        if (localFile == null) {
            // #251806
            return null;
        }
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        String uri = id.toWebServerURI(localFile);
        if (uri == null) {
            return null;
        }
        command.setType(Types.LINE);
        command.setFile(uri);
        command.setLineNumber(lineNumber);
        return command;
    }

    public static BrkpntSetCommand buildLineBreakpoint(SessionId id, String transactionId, LineBreakpoint breakpoint) {
        Line line = breakpoint.getLine();
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        BrkpntSetCommand command = buildLineBreakpoint(id, transactionId, fileObject, line.getLineNumber());
        if (command != null) {
            command.setBreakpoint(breakpoint);
        }
        return command;
    }

    public static BrkpntSetCommand buildCallBreakpoint(String transactionId, String funcName) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.CALL);
        command.setFunction(funcName);
        return command;
    }

    public static BrkpntSetCommand buildCallBreakpoint(String transactionId, FunctionBreakpoint functionBreakpoint) {
        String func = functionBreakpoint.getFunction();
        BrkpntSetCommand command = buildCallBreakpoint(transactionId, func);
        command.setBreakpoint(functionBreakpoint);
        return command;
    }

    public static BrkpntSetCommand buildReturnBreakpoint(String transactionId, String funcName) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.RETURN);
        command.setFunction(funcName);
        return command;
    }

    public static BrkpntSetCommand buildReturnBreakpoint(String transactionId, FunctionBreakpoint functionBreakpoint) {
        String func = functionBreakpoint.getFunction();
        BrkpntSetCommand command = buildReturnBreakpoint(transactionId, func);
        command.setBreakpoint(functionBreakpoint);
        return command;
    }

    public static BrkpntSetCommand buildExceptionBreakpoint(String transactionId, String excName) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.EXCEPTION);
        command.setException(excName);
        return command;
    }

    public static BrkpntSetCommand buildConditionalBreakpoint(String transactionId, String expression) {
        BrkpntSetCommand command = new BrkpntSetCommand(transactionId);
        command.setType(Types.WATCH);
        command.setExpression(expression);
        return command;
    }

}
