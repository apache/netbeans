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
package org.netbeans.modules.lsp.client.bindings;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.lsp.client.bindings.Formatter.Factory;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * LspMimeDataProvider allows dynamically registering the lsp based formatter
 * into a MimeLookup if a matching LanguageServerProvider is found on the
 * MimeLookup.
 *
 * <p>The assumption is: if a LanguageServerProvider is present, we want to
 * provide the Formatting from it.</p>
 */
@ServiceProvider(service = MimeDataProvider.class, position = 1000)
public class LspMimeDataProvider implements MimeDataProvider {

    private static final Factory lspFormatter = new Formatter.Factory();
    private static final Lookup lspFormatterLookup = Lookups.fixed(lspFormatter);

    @Override
    public Lookup getLookup(MimePath mimePath) {
        return new MimeLookupMonitoringLookup(mimePath);
    }

    private static class MimeLookupMonitoringLookup extends ProxyLookup implements LookupListener {
        private final AtomicBoolean initialized = new AtomicBoolean();
        private final Result<LanguageServerProvider> lspResult;

        public MimeLookupMonitoringLookup(MimePath mimePath) {
            // Monitor the MimeLookup - if it changes, the lookup we provided
            // needs to be updated on the next query
            lspResult = MimeLookup.getLookup(mimePath)
                .lookupResult(LanguageServerProvider.class);
            lspResult.addLookupListener(this);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            initialized.set(false);
        }

        @Override
        protected void beforeLookup(Lookup.Template<?> template) {
            super.beforeLookup(template);
            final Class<?> clz = template.getType();
            if (ReformatTask.Factory.class.isAssignableFrom(clz)) {
                if (!initialized.getAndSet(true)) {
                    if (lspResult.allInstances().isEmpty()) {
                        setLookups();
                    } else {
                        setLookups(lspFormatterLookup);
                    }
                }
            }
        }
    }
}
