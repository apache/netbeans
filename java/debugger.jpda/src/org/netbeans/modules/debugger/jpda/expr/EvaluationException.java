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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Value;
import com.sun.jdi.InvocationException;
import com.sun.source.tree.Tree;

import java.util.*;
import java.text.MessageFormat;

import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.openide.util.NbBundle;

/**
 * This class is a runtime exception because it integrates better with the generated code and
 * it also prevents unnecessary code bloat.
 *
 * @author Maros Sandor
 */
public class EvaluationException extends RuntimeException {

    private Tree      node;
    private String    reason;
    private Object[]  params;

    private String    message;

    public EvaluationException(Tree node, String reason, Object[] params) {
        this.node = node;
        this.reason = reason;
        this.params = params;
    }

    public String getMessage() {
        try {
            return getMessageImpl();
        } catch (Exception e) {
            return message = formatMessage("CTL_EvalError_unknownInternalError", new String[] {e.getMessage()});
        }
    }

    public String getMessageImpl() {
        if (message != null) return message;

        String [] msgParams = null;

        try {
        if (reason.equals("internalError"))
            msgParams = new String [] { null };
        else if (reason.equals("invalidArrayInitializer"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("arraySizeBadType"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("notArrayType"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("arrayCreateError"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("instantiateInterface"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("castToBooleanRequired"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("castFromBooleanRequired"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("castError"))
            msgParams = new String [] { String.valueOf(params[0]), String.valueOf(params[1]) };
        else if (reason.equals("badOperandForPostfixOperator"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("postfixOperatorEvaluationError"))
            msgParams = new String [] { String.valueOf(params[1]) };
        else if (reason.equals("badOperandForPrefixOperator"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("prefixOperatorEvaluationError"))
            msgParams = new String [] { String.valueOf(params[1]) };
        else if (reason.equals("badOperandForUnaryOperator"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("unaryOperatorEvaluationError"))
            msgParams = new String [] { String.valueOf(params[1]) };
        else if (reason.equals("unknownType"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("internalErrorResolvingType"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("instanceOfLeftOperandNotAReference"))
            msgParams = new String [] { TypeWrapper.name(ValueWrapper.type((Value) params[0])) };
        else if (reason.equals("conditionalOrAndBooleanOperandRequired"))
            msgParams = new String [] { TypeWrapper.name(ValueWrapper.type((Value) params[0])) };
        else if (reason.equals("conditionalQuestionMarkBooleanOperandRequired"))
            msgParams = new String [] { TypeWrapper.name(ValueWrapper.type((Value) params[0])) };
        else if (reason.equals("thisObjectUnavailable"))
            msgParams = null;
        else if (reason.equals("objectReferenceRequiredOnDereference"))
            msgParams = new String [] { TypeWrapper.name(ValueWrapper.type((Value) params[0])) };
        else if (reason.equals("badArgument"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("argumentsBadSyntax"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("ambigousMethod"))
            msgParams = new String [] { String.valueOf(params[0]),  String.valueOf(params[1]) };
        else if (reason.equals("noSuchMethod"))
            msgParams = new String [] { (String) params[0], (String) params[1] };
        else if (reason.equals("noSuchMethodWithArgs"))
            msgParams = new String [] { (String) params[0], (String) params[1], (String) params[2] };
        else if (reason.equals("noSuchConstructorWithArgs"))
            msgParams = new String [] { (String) params[0], (String) params[1] };
        else if (reason.equals("callException"))
            msgParams = new String [] { String.valueOf(params[1]), String.valueOf(params[0]) };
        else if (reason.equals("calleeException"))
            msgParams = new String [] { null, null, ((Exception) params[0]).getLocalizedMessage() };
        else if (reason.equals("identifierNotAReference"))
            msgParams = new String [] { TypeWrapper.name(ValueWrapper.type((Value) params[0])) };
        else if (reason.equals("notarray"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("arrayIndexNAN"))
            msgParams = new String [] { String.valueOf(params[1]) };
        else if (reason.equals("arrayIndexOutOfBounds"))
            msgParams = new String [] { String.valueOf(params[1]), Integer.toString(ArrayReferenceWrapper.length0((ArrayReference) params[0]) - 1) };
        else if (reason.equals("unknownVariable"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("unknownVarNoDebugInfo"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("integerLiteralTooBig"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("badFormatOfIntegerLiteral"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("unknownLiteralType"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("evaluateError"))
//            return Assert.error(node, "evaluateError", value, ((Token) operators[i-1]).image, next);
            msgParams = new String [] { String.valueOf(params[1]), String.valueOf(params[0]), String.valueOf(params[2]) };
        else if (reason.equals("evaluateErrorUnary"))
            msgParams = new String [] { String.valueOf(params[0]), String.valueOf(params[1]) };
        else if (reason.equals("notEnclosingType"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("accessInstanceVariableFromStaticContext"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("invokeInstanceMethodAsStatic"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("methodCallOnNull"))
            msgParams = new String[] { String.valueOf(params[0]) };
        else if (reason.equals("fieldOnNull"))
            msgParams = new String[] { String.valueOf(params[0]) };
        else if (reason.equals("cannotApplyOperator"))
            msgParams = new String[] { String.valueOf(params[0]) };
        else if (reason.equals("invalidMemberReference"))
            msgParams = new String[] { String.valueOf(params[0]) };
        else if (reason.equals("arrayIsNull"))
            msgParams = new String[] { String.valueOf(params[0]) };
        else if (reason.equals("unsupported"))
            msgParams = new String[] { node.toString() };
        else if (reason.equals("errorneous"))
            msgParams = new String[] { node.toString() };
        else if (reason.equals("unknownField"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("unknownOuterClass"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("notExpression"))
            msgParams = new String [] {  };
        else if (reason.equals("methOnArray"))
            msgParams = new String [] {  };
        else if (reason.equals("methOnInterface"))
            msgParams = new String [] {  };
        else if (reason.equals("forEachNotApplicable"))
            msgParams = new String [] {  };
        else if (reason.equals("localVariableAlreadyDefined"))
            msgParams = new String [] { String.valueOf(params[0]) };
        else if (reason.equals("unsupportedStringCreation")) {
            msgParams = new String [] {  };
        } else if (reason.equals("canNotInvokeMethods")) {
            msgParams = new String [] {  };
        } else if (reason.equals("notABoolean")) {
            msgParams = new String [] { String.valueOf(params[0]), String.valueOf(params[1]), String.valueOf(params[2]) };
        } else if (reason.equals("noNewClassWithBody")) {
            msgParams = new String [] { };
        } else if (reason.equals("noModules")) {
            msgParams = new String [] { };
        }
        else {
            msgParams = new String [] { reason };
            reason = "unknownInternalError";
        }
        } catch (InternalExceptionWrapper e) {
            msgParams = new String [] { e.getLocalizedMessage() };
            reason = "unknownInternalError";
        } catch (VMDisconnectedExceptionWrapper e) {
            msgParams = new String [] { e.getLocalizedMessage() };
        } catch (ObjectCollectedExceptionWrapper e) {
            msgParams = new String [] { e.getLocalizedMessage() };
        }

        message = formatMessage("CTL_EvalError_" + reason, msgParams);
        //message = formatMessage("CTL_EvalErrorExpr", new String[] { node.toString(), message });

        return message;
    }

    private String formatMessage(String msg, String [] params) {
        ResourceBundle bundle = NbBundle.getBundle (EvaluationException.class);
        msg = bundle.getString(msg);
        return MessageFormat.format(msg, (Object[]) params);
    }
}
