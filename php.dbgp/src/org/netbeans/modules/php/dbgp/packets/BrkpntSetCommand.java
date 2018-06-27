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

import java.util.Locale;
import org.netbeans.modules.php.dbgp.breakpoints.AbstractBreakpoint;

/**
 * @author ads
 *
 */
public class BrkpntSetCommand extends DbgpCommand {
    public static final String BREAKPOINT_SET = "breakpoint_set"; // NOI18N
    private static final String TYPE_ARG = "-t "; // NOI18N
    private static final String FILE_ARG = "-f "; // NOI18N
    private static final String LINE_ARG = "-n "; // NOI18N
    private static final String STATE_ARG = "-s "; // NOI18N
    private static final String TEMP_ARG = "-r "; // NOI18N
    private static final String FUNC_ARG = "-m "; // NOI18N
    private static final String EXCEPTION_ARG = "-x"; // NOI18N
    private String myFunction;
    private Types myType;
    private String myFile;
    private String myException;
    private State myState;
    private int myLineNumber;
    private String myExpression;
    private boolean isTemporary;
    private int myHitCount;
    private int myHitValue;
    private String myHitCondition;
    private AbstractBreakpoint myBrkpnt;

    public enum Types {
        LINE, // at the time of writing ( protocol version 2.0.0 ) this command is supported
        CALL, // at the time of writing ( protocol version 2.0.0 ) this command is supported
        RETURN, // at the time of writing ( protocol version 2.0.0 ) this command is supported
        EXCEPTION, // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported
        CONDITIONAL, // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported
        WATCH;          // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.US);
        }

    }

    public enum State {
        ENABLED,
        DISABLED;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.US);
        }

        public static State forString(String str) {
            State[] states = State.values();
            for (State state : states) {
                if (state.toString().equals(str)) {
                    return state;
                }
            }
            return null;
        }

    }

    BrkpntSetCommand(String transactionId) {
        this(BREAKPOINT_SET, transactionId);
    }

    BrkpntSetCommand(String cmndName, String transactionId) {
        super(cmndName, transactionId);
        myState = State.ENABLED;
        myHitCount = -1;
        myHitValue = -1;
        myLineNumber = -1;
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

    public void setBreakpoint(AbstractBreakpoint breakpoint) {
        myBrkpnt = breakpoint;
    }

    public AbstractBreakpoint getBreakpoint() {
        return myBrkpnt;
    }

    public void setType(Types type) {
        myType = type;
    }

    public void setFile(String file) {
        myFile = file;
    }

    public void setFunction(String function) {
        myFunction = function;
    }

    public void setException(String exception) {
        myException = exception;
    }

    public void setState(State state) {
        myState = state;
    }

    public void setLineNumber(int line) {
        myLineNumber = line;
    }

    public void setExpression(String expression) {
        myExpression = expression;
    }

    public void setTemporary(boolean isTemp) {
        isTemporary = isTemp;
    }

    public void setHitCount(int count) {
        myHitCount = count;
    }

    public void setHitValue(int value) {
        myHitValue = value;
    }

    public void setHitCondition(String condition) {
        myHitCondition = condition;
    }

    @Override
    protected String getData() {
        return myExpression;
    }

    @Override
    protected String getArguments() {
        assert myType != null;
        StringBuilder builder = new StringBuilder();
        setType(builder);
        setState(builder);
        setTemporary(builder);
        switch (myType) {
            case LINE:
                setLineArguments(builder);
                break;
            case CALL:
                setCallArguments(builder);
                break;
            case RETURN:
                setReturnArguments(builder);
                break;
            case EXCEPTION:
                setExceptionArguments(builder);
                break;
            case CONDITIONAL:
                setConditionalArguments(builder);
                break;
            case WATCH:
                // this case need only expression that is returned by getData() automatically
                break;
            default:
                assert false;
        }
        return builder.toString();
    }

    private void setTemporary(StringBuilder builder) {
        if (isTemporary) {
            builder.append(SPACE);
            builder.append(TEMP_ARG);
            builder.append(1);
        }
    }

    private void setState(StringBuilder builder) {
        if (myState != null) {
            builder.append(SPACE);
            builder.append(STATE_ARG);
            builder.append(myState.toString());
        }
    }

    private void setConditionalArguments(StringBuilder builder) {
        builder.append(SPACE);
        builder.append(FILE_ARG);
        builder.append(myFile);
        if (myLineNumber > -1) {
            builder.append(SPACE);
            builder.append(FILE_ARG);
            builder.append(myFile);
        }
    }

    private void setExceptionArguments(StringBuilder builder) {
        builder.append(SPACE);
        builder.append(EXCEPTION_ARG);
        builder.append(myException);
    }

    private void setReturnArguments(StringBuilder builder) {
        setCallArguments(builder);
    }

    private void setCallArguments(StringBuilder builder) {
        builder.append(SPACE);
        builder.append(FUNC_ARG);
        builder.append(myFunction);
    }

    private void setLineArguments(StringBuilder builder) {
        builder.append(SPACE);
        builder.append(FILE_ARG);
        builder.append(myFile);
        builder.append(SPACE);
        builder.append(LINE_ARG);
        // line number is 1-based.
        builder.append((myLineNumber + 1));
    }

    private void setType(StringBuilder builder) {
        builder.append(TYPE_ARG);
        builder.append(myType.toString());
    }

}
