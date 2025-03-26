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
package org.netbeans.modules.php.dbgp.packets;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.dbgp.packets.DbgpStream.StreamType;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
final class MessageBuilder {
    private static final Logger LOGGER = Logger.getLogger(MessageBuilder.class.getName());
    private static final String TYPE = "type"; // NOI18N

    private MessageBuilder() {
    }

    static DbgpMessage createStream(Node node) {
        Node attr = node.getAttributes().getNamedItem(TYPE);
        assert attr != null;
        String type = attr.getNodeValue();
        if (StreamType.STDOUT.toString().equals(type)) {
            return new DbgpStream(node, StreamType.STDOUT);
        } else if (StreamType.STDERR.toString().equals(type)) {
            return new DbgpStream(node, StreamType.STDERR);
        } else {
            assert false;
            return null;
        }
    }

    static DbgpMessage createResponse(Node node) {
        String command = DbgpMessage.getAttribute(node, DbgpResponse.COMMAND);
        if (StatusCommand.STATUS.equals(command)
                || StepOutCommand.STEP_OUT.equals(command)
                || StepOverCommand.STEP_OVER.equals(command)
                || StepIntoCommand.STEP_INTO.equals(command)
                || StopCommand.COMMAND.equals(command)) {
            return new StatusResponse(node);
        } else if (RunCommand.RUN.equals(command)) {
            return new RunResponse(node);
        } else if (BrkpntSetCommand.BREAKPOINT_SET.equals(command)) {
            return new BrkpntSetResponse(node);
        } else if (BrkpntUpdateCommand.UPDATE.equals(command)) {
            return new BrkpntUpdateResponse(node);
        } else if (BrkpntRemoveCommand.REMOVE.equals(command)) {
            return new BrkpntRemoveResponse(node);
        } else if (ContextNamesCommand.CONTEXT_NAMES.equals(command)) {
            return new ContextNamesResponse(node);
        } else if (ContextGetCommand.CONTEXT_GET.equals(command)) {
            return new ContextGetResponse(node);
        } else if (StackDepthCommand.STACK_DEPTH.equals(command)) {
            return new StackDepthResponse(node);
        } else if (StackGetCommand.STACK_GET.equals(command)) {
            return new StackGetResponse(node);
        } else if (TypeMapGetCommand.TYPEMAP_GET.equals(command)) {
            return new TypeMapGetResponse(node);
        } else if (PropertySetCommand.PROPERTY_SET.equals(command)) {
            return new PropertySetResponse(node);
        } else if (PropertyGetCommand.PROPERTY_GET.equals(command)) {
            return new PropertyGetResponse(node);
        } else if (PropertyValueCommand.PROPERTY_VALUE.equals(command)) {
            return new PropertyValueResponse(node);
        } else if (SourceCommand.SOURCE.equals(command)) {
            return new SourceResponse(node);
        } else if (StreamType.STDERR.toString().equals(command)
                || StreamType.STDOUT.toString().equals(command)) {
            return new StreamResponse(node);
        } else if (FeatureGetCommand.FEATURE_GET.equals(command)) {
            return new FeatureGetResponse(node);
        } else if (FeatureSetCommand.FEATURE_SET.equals(command)) {
            return new FeatureSetResponse(node);
        } else if (BreakCommand.BREAK.equals(command)) {
            return new BreakResponse(node);
        } else if (EvalCommand.EVAL.equals(command)) {
            String transactionId = DbgpMessage.getAttribute(node, DbgpResponse.TRANSACTION_ID);
            if (transactionId.equals(RequestedUrlEvalCommand.getLastUsedTransactionId())) {
                return new RequestedUrlEvalResponse(node);
            }
            return new EvalResponse(node);
        } else if (ExprCommand.EXPR.equals(command)) {
            return new ExprResponse(node);
        } else if (ExecCommand.EXEC.equals(command)) {
            return new ExecResponse(node);
        }
        LOGGER.log(Level.INFO, "Command not matched: {0} NODE: {1}", new Object[]{command, node});
        return new DbgpMessage.NoneDbgpMessage(node);
    }

}
