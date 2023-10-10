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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.swing.Icon;
import javax.swing.JButton;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.InputBoxStep;
import org.netbeans.modules.java.lsp.server.input.InputCallbackParams;
import org.netbeans.modules.java.lsp.server.input.InputService;
import org.netbeans.modules.java.lsp.server.input.LspInputServiceImpl;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.QuickPickStep;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.UIContext;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Adapts a {@link NotifyDescriptor} to a {@link ShowMessageRequestParams} call.
 * @author sdedic
 */
class NotifyDescriptorAdapter {
    private static final Logger LOG = Logger.getLogger(NotifyDescriptorAdapter.class.getName());
    
    /**
     * Processor that handles requests to create input steps, which might block
     */
    private static final RequestProcessor RP = new RequestProcessor(NotifyDescriptorAdapter.class.getName(), 20);
    
    private final UIContext client;
    private final NotifyDescriptor  descriptor;
    private final Map<MessageActionItem, Object> item2Option = new LinkedHashMap<>();
    private final Map<String, Object> text2Option = new HashMap<>();
    private final Map<Object, List<ActionListener>> optionListeners = new HashMap<>();
    private final Map<Object, JButton> option2Button = new HashMap<>();
    
    private static final Set<String> warnedClasses = new HashSet<>();
    
    private ShowMessageRequestParams request;

    private static final Object[] YES_NO_CANCEL = new Object[] {
        NotifyDescriptor.YES_OPTION,
        NotifyDescriptor.NO_OPTION,
        NotifyDescriptor.CANCEL_OPTION
    };
    
    private static final Object[] YES_NO = new Object[] {
        NotifyDescriptor.YES_OPTION,
        NotifyDescriptor.NO_OPTION,
    };
    
    private static final Object[] OK_CANCEL = new Object[] {
        NotifyDescriptor.OK_OPTION,
        NotifyDescriptor.CANCEL_OPTION
    };
    
    private static final Object[] JUST_OK = new Object[] {
        NotifyDescriptor.OK_OPTION
    };
    
    public NotifyDescriptorAdapter(NotifyDescriptor descriptor, UIContext client) {
        this.descriptor = descriptor;
        this.client = client;
    }
    
    private MessageType translateMessageType() {
        switch(descriptor.getMessageType()) {
            case NotifyDescriptor.ERROR_MESSAGE:
                return MessageType.Error;
            case NotifyDescriptor.WARNING_MESSAGE:
                return MessageType.Warning;
            
            case NotifyDescriptor.QUESTION_MESSAGE:
            case NotifyDescriptor.PLAIN_MESSAGE:
            case NotifyDescriptor.INFORMATION_MESSAGE:
                return MessageType.Info;
            default:
                return MessageType.Log;
        }
    }
    
    /**
     * Strip HTML from the message; VSCode standard showMessage does not support HTML.
     * @param original
     * @return 
     */
    private String translateText(String original) {
        if (!original.startsWith("<html>")) { // NOI18N
            return original;
        }
        return original.replaceAll("<p/>|</p>|<br>", "\n") // NOI18N
                       .replaceAll("<[^>]*>", "")
                       .replace("&nbsp;", " ") // NOI18N
                       .trim();
    }
    
    public String getAccessibleDescription(Object o) {
        if (!(o instanceof Accessible)) {
            return null;
        }
        AccessibleContext ac = ((Accessible)o).getAccessibleContext();
        String s = ac.getAccessibleDescription();
        if (s != null && !"N/A".equals(s)) {
            return s;
        }
        return ac.getAccessibleName();
    }
    
    private ShowMessageRequestParams createShowMessageRequest() {
        if (this.request != null) {
            return request;
        }
        Object msg = descriptor.getMessage();
        String displayText = null;
        
        if (msg instanceof String) {
            displayText = msg.toString();
        } else {
            displayText = getAccessibleDescription(msg);
        }
        if (displayText == null) {
            return null;
        }
        mapDescriptorOptions();
        ShowMessageRequestParams request = new ShowMessageRequestParams();
        request.setMessage(translateText(displayText));
        request.setActions(getActionItems());
        request.setType(translateMessageType());
        return this.request = request;
    }
    
    public Object actionToOption(MessageActionItem item) {
        return item2Option.get(item);
    }
    
    public List<MessageActionItem>  getActionItems() {
        return new ArrayList<>(item2Option.keySet());
    }
    
    private void addMessageItem(MessageActionItem item, Object option) {
        item2Option.put(item, option);
        text2Option.put(item.getTitle(), option);
    }
    
    @NbBundle.Messages({
        "OPTION_Yes=Yes",
        "OPTION_No=No",
        "OPTION_OK=OK",
        "OPTION_Cancel=Cancel",
    })
    private String mapDescriptorOption(Object option) {
        if (option == NotifyDescriptor.CANCEL_OPTION) {
            return Bundle.OPTION_Cancel();
        } else if (option == NotifyDescriptor.NO_OPTION) {
            return Bundle.OPTION_No();
        } else if (option == NotifyDescriptor.YES_OPTION) {
            return Bundle.OPTION_Yes();
        } else if (option == NotifyDescriptor.OK_OPTION) {
            return Bundle.OPTION_OK();
        }
        if (option instanceof Component) {
            return null;
        }
        if (option != null) {
            return option.toString();
        }
        return null;
    }
    
    private void mapDescriptorOptions() {
        Object[] options = descriptor.getOptions();
        if (options == null) {
            switch (descriptor.getOptionType()) {
                case NotifyDescriptor.DEFAULT_OPTION:
                case NotifyDescriptor.OK_CANCEL_OPTION:
                    options = OK_CANCEL; break;
                case NotifyDescriptor.YES_NO_CANCEL_OPTION:
                    options = YES_NO_CANCEL; break;
                case NotifyDescriptor.YES_NO_OPTION:
                    options = YES_NO; break;
                default:
                    options = JUST_OK; 
                    break;
            }
        }
        Object[] add = descriptor.getAdditionalOptions();
        if (add != null && add.length > 0) {
            Object[] addOpts = Arrays.copyOf(add, add.length + options.length);
            System.arraycopy(options, 0, addOpts, add.length, options.length);
            options = addOpts;
        }
        for (Object o : options) {
            String text;
            
            if (o instanceof JButton) {
                text = addButtonItem((JButton)o);
            } else if (o instanceof Icon) {
                text = addIconItem((Icon)o);
            } else {
                text = mapDescriptorOption(o);
            }
            if (text != null) {
                // Discard ampersands, LSP spec does not support such pattern.
                addMessageItem(new MessageActionItem(Actions.cutAmpersand(text)), o);
            } else {
                reportUnknownOption(o);
            }
        }
    }
    
    private void reportUnknownOption(Object o) {
        Throwable t = new Throwable();
        StackTraceElement[] stack = t.getStackTrace();
        if (stack.length >= 7) {
        String callerClass = stack[6].getClassName();
            synchronized (warnedClasses) {
                if (!warnedClasses.add(callerClass)) {
                    return;
                }
            }
        }
        LOG.log(Level.WARNING, new Throwable(), 
                () -> "Unhandled option " + o + " for descriptor: " + descriptor);
    }
    
    private String addButtonItem(JButton button) {
        if (!button.isVisible()) {
            return null;
        }
        String t = button.getText();
        List<ActionListener> ll = Arrays.asList(button.getActionListeners());
        if (!ll.isEmpty()) {
            optionListeners.put(button, ll);
        }
        return t;
    }
    
    private String addIconItem(Icon icon) {
        if (icon instanceof AccessibleIcon) {
            return ((AccessibleIcon)icon).getAccessibleIconDescription();
        } else {
            return null;
        }
    }
    
    public Object clientNotify() {
        try {
            return clientNotifyLater().get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    public CompletableFuture<Object> clientNotifyLater() {
        LOG.log(Level.FINE, "notifyLater with context: {0}", this.client);
        return clientNotifyCompletion().thenApply(d -> d.getValue()).exceptionally(t -> {
            if (t instanceof CompletionException) {
                t = t.getCause();
            }
            if (!(t instanceof CancellationException)) {
                LOG.log(Level.WARNING, "Exception occurred for {0}", descriptor);
                LOG.log(Level.WARNING, "Exception stacktrace", t);
            }
            return null;
        });
    }
    
    class Ctrl<T> extends CompletableFuture<T> {
        private final CompletableFuture clientFuture;

        public Ctrl(CompletableFuture clientFuture) {
            this.clientFuture = clientFuture;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // this is not exactly well synchronized, but with no synchronization in 
            // NotifyDescriptor the value cannot be set really atomically, but the dependents
            // will be released during super.cancel() call.
            boolean b = !isDone();
            if (b) {
                Object v = descriptor.getValue();
                if (!(v == NotifyDescriptor.CANCEL_OPTION || v == NotifyDescriptor.CLOSED_OPTION)) {
                    descriptor.setValue(NotifyDescriptor.CANCEL_OPTION);
                }
                b &= clientFuture.cancel(mayInterruptIfRunning);
            }
            return super.cancel(mayInterruptIfRunning) || b;
        }
    }
    
    @NbBundle.Messages({
        "MSG_InvalidInput=Invalid input"
    })
    public <T extends NotifyDescriptor> CompletableFuture<T> clientNotifyCompletion() {
        // wrapper that allows to externally control the client's Future
        if (descriptor instanceof NotifyDescriptor.InputLine) {
            NotifyDescriptor.InputLine inp = (NotifyDescriptor.InputLine) descriptor;
            ShowInputBoxParams params = new ShowInputBoxParams();
            params.setPrompt(descriptor.getTitle());
            params.setValue(inp.getInputText());
            params.setPassword(descriptor instanceof NotifyDescriptor.PasswordLine);
            CompletableFuture<String> newText = client.showInputBox(params);
            Ctrl<T> ctrl = new Ctrl<>(newText);
            newText.thenAccept((item) -> {
                if (item == null) {
                    descriptor.setValue(NotifyDescriptor.CLOSED_OPTION);
                    ctrl.completeExceptionally(new CancellationException());
                } else {
                    inp.setInputText(item);
                    descriptor.setValue(NotifyDescriptor.OK_OPTION);
                    ctrl.complete((T)descriptor);
                }
            }).exceptionally(t -> {
                if (t instanceof CompletionException) {
                    t = t.getCause();
                }
                ctrl.completeExceptionally(t);
                return null;
            });
            return ctrl;
        } else if (descriptor instanceof NotifyDescriptor.QuickPick) {
            NotifyDescriptor.QuickPick qp = (NotifyDescriptor.QuickPick) descriptor;
            List<NotifyDescriptor.QuickPick.Item> qpItems = qp.getItems();
            List<QuickPickItem> items = new ArrayList<>(qpItems.size());
            for (int i = 0; i < qpItems.size(); i++) {
                NotifyDescriptor.QuickPick.Item item = qpItems.get(i);
                items.add(new QuickPickItem(item.getLabel(), Utils.html2plain(item.getDescription(), true), null, item.isSelected(), Integer.toString(i)));
            }
            ShowQuickPickParams params = new ShowQuickPickParams(qp.getLabel(), qp.getTitle(), qp.isMultipleSelection(), items);
            CompletableFuture<List<QuickPickItem>> qpF = client.showQuickPick(params);
            Ctrl<T> ctrl = new Ctrl<>(qpF);
            qpF.thenAccept(selected -> {
                if (selected == null) {
                    descriptor.setValue(NotifyDescriptor.CLOSED_OPTION);
                    ctrl.completeExceptionally(new CancellationException());
                } else {
                    List<Object> selectedIds = selected.stream().map(t -> t.getUserData()).collect(Collectors.toList());
                    for (int i = 0; i < qpItems.size(); i++) {
                        NotifyDescriptor.QuickPick.Item item = qpItems.get(i);
                        item.setSelected(selectedIds.contains(Integer.toString(i)));
                    }
                    descriptor.setValue(NotifyDescriptor.OK_OPTION);
                    ctrl.complete((T)descriptor);
                }
            }).exceptionally(t -> {
                if (t instanceof CompletionException) {
                    t = t.getCause();
                }
                ctrl.completeExceptionally(t);
                return null;
            });
            return ctrl;
        } else if (descriptor instanceof NotifyDescriptor.ComposedInput) {
            NotifyDescriptor.ComposedInput ci = (NotifyDescriptor.ComposedInput) descriptor;
            InputService.Registry inputServiceRegistry = Lookup.getDefault().lookup(InputService.Registry.class);
            if (inputServiceRegistry == null) {
                Ctrl<T> ctrl = new Ctrl<>(null);
                ctrl.completeExceptionally(new IllegalStateException("No LSPInputService found"));
                return ctrl;
            }
            Map<String, NotifyDescriptor> data = new HashMap<>();
            String inputId = inputServiceRegistry.registerInput(new InputService.Callback() {
                @Override
                public CompletableFuture<Either<QuickPickStep, InputBoxStep>> step(InputCallbackParams params) {
                    String stepId = "ID:" + params.getStep();
                    updateData(params.getData(), data);
                    
                    CompletableFuture<Either<QuickPickStep, InputBoxStep>> res = new CompletableFuture<>();
                    // the ComposedInput.Callback may block gathering information for e.g. quickpick. Offload to a Requestprocessor, as 
                    // the main LSP thread cannot be blocked by long operations.
                    RP.post(() -> {
                        try {
                            NotifyDescriptor input = ci.createInput(params.getStep());
                            if (input instanceof NotifyDescriptor.InputLine) {
                                data.put(stepId, input);
                                InputBoxStep step = new InputBoxStep(ci.getEstimatedNumberOfInputs(), stepId,
                                        null, input.getTitle(), ((NotifyDescriptor.InputLine) input).getInputText(),
                                        input instanceof NotifyDescriptor.PasswordLine);
                                res.complete(Either.forRight(step));
                            } else if (input instanceof NotifyDescriptor.QuickPick) {
                                data.put(stepId, input);
                                List<NotifyDescriptor.QuickPick.Item> qpItems = ((NotifyDescriptor.QuickPick) input).getItems();
                                List<QuickPickItem> items = new ArrayList<>();
                                for (int i = 0; i < qpItems.size(); i++) {
                                    NotifyDescriptor.QuickPick.Item item = qpItems.get(i);
                                    items.add(new QuickPickItem(item.getLabel(), 
                                            Utils.html2plain(item.getDescription(), true), null, item.isSelected(), Integer.toString(i)));
                                }
                                QuickPickStep step = new QuickPickStep(ci.getEstimatedNumberOfInputs(), stepId,
                                        null, input.getTitle(), ((NotifyDescriptor.QuickPick) input).isMultipleSelection(),
                                        items);
                                res.complete(Either.forLeft(step));
                            } else {
                                res.complete(null);
                            }
                        } catch (ThreadDeath td) {
                            throw td;
                        } catch (Throwable t) {
                            res.completeExceptionally(t);
                        }
                    });
                    return res;
                }

                @Override
                public CompletableFuture<String> validate(InputCallbackParams params) {
                    String stepId = "ID:" + params.getStep();
                    updateData(params.getData(), data);
                    NotifyDescriptor input = data.get(stepId);
                    if (input != null && !input.isValid()) {
                        NotificationLineSupport nls = input.getNotificationLineSupport();
                        String errMsg = nls != null ? nls.getErrorMessage() : null;
                        return CompletableFuture.completedFuture(errMsg != null ? errMsg : Bundle.MSG_InvalidInput());
                    }
                    return CompletableFuture.completedFuture(null);
                }
            });
            ShowMutliStepInputParams params = new ShowMutliStepInputParams(inputId, ci.getTitle());
            CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> msiF = client.showMultiStepInput(params);
            Ctrl<T> ctrl = new Ctrl<>(msiF);
            msiF.thenAccept(result -> {
                if (result == null || result.size() < data.size()) {
                    descriptor.setValue(NotifyDescriptor.CLOSED_OPTION);
                    ctrl.completeExceptionally(new CancellationException());
                } else {
                    updateData(result, data);
                    descriptor.setValue(NotifyDescriptor.OK_OPTION);
                    ctrl.complete((T)descriptor);
                }
            }).exceptionally(t -> {
                if (t instanceof CompletionException) {
                    t = t.getCause();
                }
                ctrl.completeExceptionally(t);
                return null;
            });
            return ctrl;
        } else {
            ShowMessageRequestParams params = createShowMessageRequest();
            if (params == null) {
                descriptor.setValue(NotifyDescriptor.CLOSED_OPTION);
                CompletableFuture<T> x = new CompletableFuture<>();
                x.complete((T)descriptor);
                return x;
            }
            CompletableFuture<MessageActionItem> resultItem =  client.showMessageRequest(request);
            Ctrl<T> ctrl = new Ctrl<>(resultItem);
            resultItem /*.exceptionally(this::handleClientException) */.thenApply(this::processActivatedOption).thenAccept(o -> {
                descriptor.setValue(o);
                if (o == NotifyDescriptor.CLOSED_OPTION || o == NotifyDescriptor.CANCEL_OPTION) {
                    ctrl.completeExceptionally(new CancellationException());
                } else {
                    ctrl.complete((T)descriptor);
                }
            }).exceptionally(t -> {
                // JDK-8233050: this exceptionally is chained after .thenApply, it will receive earlier exceptions wrapped:
                if (t instanceof CompletionException) {
                    t = ((CompletionException)t).getCause();
                }
                ctrl.completeExceptionally(t);
                return null;
            });
            return ctrl;
        }
    }
    
    MessageActionItem handleClientException(Throwable t) {
        // TBD
        return null;
    }
    
    Object processActivatedOption(MessageActionItem item) {
        Object option = selectActivatedOption(item);
        List<ActionListener> ll = optionListeners.get(option);
        if (ll != null) {
            ActionEvent e = new ActionEvent(option, ActionEvent.ACTION_PERFORMED, item.getTitle());
            for (ActionListener l : ll) {
                try {
                    l.actionPerformed(e);
                } catch (RuntimeException ex) {
                    LOG.log(Level.SEVERE, "Error occurred during actionListener dispatch", ex);
                }
            }
        }
        return option;
    }
    
    Object selectActivatedOption(MessageActionItem item) {
        if (item == null) {
            return NotifyDescriptor.CLOSED_OPTION;
        }
        Object option = item2Option.get(item);
        if (option == null) {
            option = text2Option.get(item.getTitle());
        }
        if (option == null) {
            LOG.log(Level.WARNING, "Unknown client response received: {0}, the valid options were: {1}", new Object[] {
                item.getTitle(), 
                item2Option.keySet().stream().map(MessageActionItem::getTitle).collect(Collectors.toList())
            });
            return NotifyDescriptor.CLOSED_OPTION;
        }
        
        return option;
    }

    private static void updateData(Map<String, Either<List<QuickPickItem>, String>> from, Map<String, NotifyDescriptor> to) {
        for (Map.Entry<String, Either<List<QuickPickItem>, String>> entry : from.entrySet()) {
            NotifyDescriptor desc = to.get(entry.getKey());
            if (desc instanceof NotifyDescriptor.InputLine) {
                assert entry.getValue().isRight();
                ((NotifyDescriptor.InputLine) desc).setInputText(entry.getValue().getRight());
            } else if (desc instanceof NotifyDescriptor.QuickPick) {
                assert entry.getValue().isLeft();
                List<Object> selected = entry.getValue().getLeft().stream().map(t -> t.getUserData()).collect(Collectors.toList());
                List<NotifyDescriptor.QuickPick.Item> qpItems = ((NotifyDescriptor.QuickPick) desc).getItems();
                for (int i = 0; i < qpItems.size(); i++) {
                    NotifyDescriptor.QuickPick.Item item = qpItems.get(i);
                    item.setSelected(selected.contains(Integer.toString(i)));
                }
            }
        }
    }
}
