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
package org.netbeans.modules.java.lsp.server.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.swing.JPanel;
import static junit.framework.TestCase.fail;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author sdedic
 */
public class AbstractDialogDisplayerTest extends NbTestCase {

    public AbstractDialogDisplayerTest(String name) {
        super(name);
    }
    
    private static class MockUIContext extends UIContext {
        @Override
        protected boolean isValid() {
            return true;
        }

        @Override
        protected void showMessage(MessageParams msg) {
        }

        @Override
        protected CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams msg) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        protected void logMessage(MessageParams msg) {
        }

        @Override
        protected StatusDisplayer.Message showStatusMessage(ShowStatusMessageParams msg) {
            return null;
        }
    }
    
    /**
     * Checks that component-based dialogs will just return CLOSED.
     * @throws Exception 
     */
    public void testUnsupportedDialogWithPanel() throws Exception {
        MockUIContext client = new MockUIContext() {
            @Override
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
                fail();
                return null;
            }

            @Override
            public void showMessage(MessageParams messageParams) {
                fail();
            }
        };
        
        NotifyDescriptor nd = new NotifyDescriptor(new JPanel(), "Unused", 
                NotifyDescriptor.OK_CANCEL_OPTION, 
                NotifyDescriptor.WARNING_MESSAGE,
                null, null);
        
        NotifyDescriptorAdapter adapter = new NotifyDescriptorAdapter(nd, client);
        assertSame(NotifyDescriptor.CLOSED_OPTION, adapter.clientNotify());
    }
    
    private static final Map<Integer, MessageType>  MESSAGE_TYPES = new HashMap<>();
    
    {
        MESSAGE_TYPES.put(NotifyDescriptor.PLAIN_MESSAGE, MessageType.Info);
        MESSAGE_TYPES.put(NotifyDescriptor.QUESTION_MESSAGE, MessageType.Info);
        MESSAGE_TYPES.put(NotifyDescriptor.INFORMATION_MESSAGE, MessageType.Info);
        MESSAGE_TYPES.put(NotifyDescriptor.WARNING_MESSAGE, MessageType.Warning);
        MESSAGE_TYPES.put(NotifyDescriptor.ERROR_MESSAGE, MessageType.Error);
    }
    
    /**
     * Checks that showMessage receives an appropriate message type, for different
     * ND's {@code messageTy[e} value/
     * @throws Exception 
     */
    public void testCheckMessageTypes() throws Exception {
        for (int i : MESSAGE_TYPES.keySet()) {
            MockUIContext cl = new MockUIContext() {
                MessageType check = MESSAGE_TYPES.get(i);
                
                public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
                    assertEquals(check, requestParams.getType());
                    CompletableFuture<MessageActionItem> x = new CompletableFuture<>();
                    x.complete(null);
                    return x;
                }
            };
            NotifyDescriptor nd = new NotifyDescriptor("Hello, LSP client", "Unused", 
                    NotifyDescriptor.OK_CANCEL_OPTION, 
                    i,
                    null, null);
            NotifyDescriptorAdapter adapter = new NotifyDescriptorAdapter(nd, cl);
            adapter.clientNotify();
        }
    }
    
    /**
     * Checks that yes-no-cancel items will be presented at the client
     */
    public void testYesNoCancelItems() {
        MockUIContext cl = new MockUIContext() { 
            public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
                assertEquals(3, requestParams.getActions().size());
                assertEquals("Yes", requestParams.getActions().get(0).getTitle());
                assertEquals("No", requestParams.getActions().get(1).getTitle());
                assertEquals("Cancel", requestParams.getActions().get(2).getTitle());

                CompletableFuture<MessageActionItem> x = new CompletableFuture<>();
                x.complete(requestParams.getActions().get(0));
                return x;
            }
        };
        NotifyDescriptor nd = new NotifyDescriptor("Hello, LSP client", "Unused", 
                NotifyDescriptor.YES_NO_CANCEL_OPTION, 
                NotifyDescriptor.QUESTION_MESSAGE,
                null, null);
        NotifyDescriptorAdapter adapter = new NotifyDescriptorAdapter(nd, cl);
        assertEquals(NotifyDescriptor.YES_OPTION, adapter.clientNotify());
    }
}
