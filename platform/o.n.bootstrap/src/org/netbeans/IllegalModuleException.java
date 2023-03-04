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
package org.netbeans;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.openide.modules.Dependency;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Union2;

/** Special subclass of {@link IllegalModuleException} with
 * better localized message.
 */
final class IllegalModuleException extends IllegalArgumentException {
    @Messages({
        "# {0} - names of modules",
        "MSG_ENABLE_MISSING=Cannot enable {0}"
    })
    static enum Reason {
        // from delete
        DELETE_FIXED_MODULE("fixed module: {0}"),
        DELETE_ENABLED_MODULE("enabled module: {0}"),

        // from reload
        RELOAD_FIXED_MODULE("reload fixed module: {0}"),
        RELOAD_ENABLED_MODULE("reload enabled module: {0}"),

        // from enable
        ENABLE_MISSING("Not all requested modules can be enabled: {0}", "MSG_ENABLE_MISSING"),
        ENABLE_TESTING("Would also need to enable: {0}"),

        // from disable
        DISABLE_TOO("Would also need to disable: {0}"),

        // from simulateEnable
        SIMULATE_ENABLE_AUTOLOAD("Cannot simulate enabling an autoload: {0}", "MSG_ENABLE_MISSING"),
        SIMULATE_ENABLE_EAGER("Cannot simulate enabling an eager module: {0}", "MSG_ENABLE_MISSING"),
        SIMULATE_ENABLE_ALREADY("Already enabled: {0}"),
        SIMULATE_ENABLE_INVALID("Not managed by me: {0}"),

        // from simulateDisable
        SIMULATE_DISABLE_AUTOLOAD("Cannot disable autoload: {0}"),
        SIMULATE_DISABLE_EAGER("Cannot disable eager module: {0}"),
        SIMULATE_DISABLE_FIXED("Cannot disable fixed module: {0}"),
        SIMULATE_DISABLE_ALREADY("Already disabled: {0}");


        final String msg;
        final String l10n;

        Reason(String msg) {
            this(msg, null);
        }

        Reason(String msg, String l10n) {
            this.msg = msg;
            this.l10n = l10n;
        }
    }
    private final Reason reason;
    private final Map<Module, Set<Union2<Dependency,InvalidException>>> bogus;

    IllegalModuleException(Reason reason, Module bogus) {
        this(reason, Collections.singletonMap(bogus, Collections.emptySet()));
    }

    IllegalModuleException(Reason reason, Map<Module, Set<Union2<Dependency,InvalidException>>> bogus) {
        this.reason = reason;
        this.bogus = bogus;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format(reason.msg, bogus);
    }

    @Override
    public String getLocalizedMessage() {
        if (reason.l10n != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Module, Set<Union2<Dependency, InvalidException>>> entry : bogus.entrySet()) {
                Module m = entry.getKey();
                sb.append(m.getDisplayName()).append("[").append(m.getCodeNameBase()).append("]"); // NOI18N
                for (Union2<Dependency, InvalidException> u : entry.getValue()) {
                    if (u.hasFirst()) {
                        sb.append("\n - ").append(u.first().toString()); // NOI18N
                    }
                }
                sb.append("\n");
            }
            return NbBundle.getMessage(Reason.class, reason.l10n, sb.toString());
        } else {
            return getMessage();
        }
    }
}
