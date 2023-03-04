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
package org.netbeans.modules.web.webkit.tooling.console;

import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.console.Console;
import org.netbeans.modules.web.webkit.debugging.spi.BrowserConsoleLoggerFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=BrowserConsoleLoggerFactory.class)
public class BrowserConsoleLoggerFactoryImpl implements BrowserConsoleLoggerFactory {

    @Override
    public Lookup createBrowserConsoleLogger(WebKitDebugging webkit, Lookup projectContext) {
        BrowserConsoleLogger logger = new BrowserConsoleLogger(projectContext);
        Console console = webkit.getConsole();
        console.addListener(logger);
        logger.setInput(console.getInput());
        return Lookups.fixed(webkit, logger);
    }

    @Override
    public void stopBrowserConsoleLogger(Lookup session) {
        WebKitDebugging webkit = session.lookup(WebKitDebugging.class);
        assert webkit != null;
        BrowserConsoleLogger logger = session.lookup(BrowserConsoleLogger.class);
        assert logger != null;
        if (logger == null || webkit == null) {
            return;
        }
        webkit.getConsole().removeListener(logger);
        logger.sessionWasClosed();
        logger.close();
    }

}
