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
package org.netbeans.modules.extexecution.startup;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class ProxyStartupExtender implements StartupExtenderImplementation {

    private final Map<String,?> attributes;

    private final Set<StartMode> startMode;

    private StartupExtenderImplementation delegate;

    public ProxyStartupExtender(Map<String,?> attributes) {
        this.attributes = attributes;

        String startModeValue = (String) attributes.get(
                StartupExtenderRegistrationProcessor.START_MODE_ATTRIBUTE);
        startMode = EnumSet.noneOf(StartMode.class);
        if (startModeValue != null) {
            for (String value : startModeValue.split(",")) {
                startMode.add(StartMode.valueOf(value));
            }
        }
    }

    @Override
    public List<String> getArguments(Lookup context, StartMode mode) {
        if (startMode.contains(mode)) {
            return getDelegate().getArguments(context, mode);
        }
        return Collections.emptyList();
    }

    private StartupExtenderImplementation getDelegate() {
        synchronized (this) {
            if (delegate != null) {
                return delegate;
            }
        }

        StartupExtenderImplementation provider = (StartupExtenderImplementation) attributes.get(
                StartupExtenderRegistrationProcessor.DELEGATE_ATTRIBUTE);
        if (provider == null) {
            throw new IllegalStateException("Delegate must not be null");
        }

        synchronized (this) {
            if (delegate == null) {
                delegate = provider;
            }
            return delegate;
        }
    }
    
    /**
     * Returns false only if the extender registration explicitly says it does not escape arguments.
     */
    public boolean argumentsQuoted() {
        Object o = attributes.get(StartupExtenderRegistrationProcessor.QUOTED_ATTRIBUTE);
        // older binaries do not contain the attribute, so fall back to 'escaped' mode.
        return o == null || o != Boolean.FALSE;
    }

    /**
     * Enhanced version that declares escaping policy from 1.62
     */
    public static class V2 extends ProxyStartupExtender implements StartupExtenderRegistrationOptions {
        public V2(Map<String, ?> attributes) {
            super(attributes);
        }
    }
}
