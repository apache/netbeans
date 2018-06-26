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
        if (RunCommand.RUN.equals(command)
                || StatusCommand.STATUS.equals(command)
                || StepOutCommand.STEP_OUT.equals(command)
                || StepOverCommand.STEP_OVER.equals(command)
                || StepIntoCommand.STEP_INTO.equals(command)
                || StopCommand.COMMAND.equals(command)) {
            return new StatusResponse(node);
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
