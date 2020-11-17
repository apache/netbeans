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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleText;
import javax.swing.Icon;
import javax.swing.JButton;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Adapts a {@link NotifyDescriptor} to a {@link ShowMessageRequestParams} call.
 * @author sdedic
 */
class NotifyDescriptorAdapter {
    private static final Logger LOG = Logger.getLogger(NotifyDescriptorAdapter.class.getName());
    
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
        String res = 
                original.replaceAll("<p/>|</p>|<br>", "\n"). // NOI18N
                    replaceAll( "<[^>]*>", "" ). // NOI18N 
                    replaceAll( "&nbsp;", " " ); // NOI18N 
        res = res.trim();
        return res;
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
    
    public ShowMessageRequestParams createShowMessageRequest() {
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
                addMessageItem(new MessageActionItem(text), o);
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
        ShowMessageRequestParams params = createShowMessageRequest();
        if (params == null) {
            CompletableFuture<Object> x = new CompletableFuture<>();
            x.complete(NotifyDescriptor.CLOSED_OPTION);
            return x;
        }
        CompletableFuture<MessageActionItem> resultItem =  client.showMessageRequest(request);
        return resultItem /*.exceptionally(this::handleClientException) */.thenApply(this::processActivatedOption);
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
}
