/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor;

import java.util.Arrays;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;

/**
 * Error marker attached to an Element produced by {@link XMLLexerParser}.
 */
public final class ErrorMark {
    private boolean error = true;
    
    /**
     * Offset of the error mark
     */
    private final int offset;
    /**
     * Length of the offending text
     */
    private final int len;
    
    /**
     * Type / classification of the error
     */
    private final String errorType;
    
    /**
     * Parameter
     */
    private final Object[]    parameters;
    
    /**
     * Custom message, if any
     */
    private final String message;
    
    /**
     * Target of the error, possibly null for low-level errors
     */
    private final FxNode  target;
    
    public ErrorMark(int offset, int len, String errorType, String message, Object... params) {
        this(null, true, offset, len, errorType, message, params);
    }

    public ErrorMark(FxNode target, int offset, int len, String errorType, String message, Object... params) {
        this(null, true, offset, len, errorType, message, params);
    }

    ErrorMark(FxNode target, boolean error, int offset, int len, String errorType, String message, Object... params) {
        this.target = target;
        this.error = error;
        this.offset = offset;
        this.len = len;
        this.errorType = errorType;
        this.message = message;
        this.parameters = params;
    }

    public static ErrorMark makeError(FxNode target, int offset, int len, String errorType, String message, Object... params) {
        return new ErrorMark(target, true,  offset, len, errorType, message, params);
    }

    public static ErrorMark makeError(int offset, int len, String errorType, String message, Object... params) {
        return new ErrorMark(null, true,  offset, len, errorType, message, params);
    }

    public static ErrorMark makeWarning(int offset, int len, String errorType, String message, Object... params) {
        return new ErrorMark(null, false,  offset, len, errorType, message, params);
    }
    
    public boolean isError() {
        return error;
    }

    public int getOffset() {
        return offset;
    }

    public int getLen() {
        return len;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getParameters() {
        return parameters;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "Error{type: %s, pos: %d, len: %d, message: %s",
                errorType, offset, len, message
        ));
        if (parameters != null) {
            sb.append(", ").append(Arrays.asList(parameters));
        }
        sb.append("}");
        return sb.toString();
    }
}
