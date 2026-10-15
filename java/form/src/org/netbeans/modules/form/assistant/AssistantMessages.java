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
package org.netbeans.modules.form.assistant;

import java.util.*;

import org.openide.util.NbBundle;

/**
 * Repository of assistant messages.
 *
 * @author Jan Stola
 */
public class AssistantMessages {
    private static AssistantMessages defaultInstance = new AssistantMessages();
    private boolean initialized = false;
    private Map<String, String[]> contextToMessages;

    private AssistantMessages() {
    }

    public static AssistantMessages getDefault() {
        return defaultInstance;
    }

    public String[] getMessages(String context) {
        if (!initialized) {
            initialize();
        }
        String[] messages = contextToMessages.get(context);
        return messages;
    }

    public void setMessages(String context, String... messages) {
        if (!initialized) {
            initialize();
        }
        contextToMessages.put(context, messages);
    }

    private void initialize() {
        Map<String,Set<String>> contextToSet = new HashMap<String,Set<String>>();
        ResourceBundle bundle = NbBundle.getBundle(AssistantMessages.class);
        Enumeration enumeration = bundle.getKeys();
        while (enumeration.hasMoreElements()) {
            String bundleKey = (String)enumeration.nextElement();
            String message = bundle.getString(bundleKey);
            if (message == null || message.trim().length() == 0) {
                continue; // some messages can be filtered out via branding
            }
            String context = getContext(bundleKey);
            Set<String> messages = contextToSet.get(context);
            if (messages == null) {
                messages = new HashSet<String>();
                contextToSet.put(context, messages);
            }
            messages.add(message);
        }

        // Transform sets into arrays
        contextToMessages = new HashMap<String, String[]>();
        Iterator<Map.Entry<String,Set<String>>> iter = contextToSet.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Set<String>> entry = iter.next();
            String key = entry.getKey();
            Set<String> value = entry.getValue();
            String[] messages = value.toArray(new String[0]);
            contextToMessages.put(key, messages);
        }
        
        initialized = true;
    }

    private String getContext(String bundleKey) {
        int index = bundleKey.indexOf('_');
        if (index == -1) {
            return bundleKey;
        } else {
            return bundleKey.substring(0, index);
        }
    }

}
