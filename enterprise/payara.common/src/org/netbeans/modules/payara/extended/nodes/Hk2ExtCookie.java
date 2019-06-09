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
package org.netbeans.modules.payara.extended.nodes;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.common.PartialCompletionException;
import org.netbeans.modules.payara.common.nodes.Hk2Cookie;
import org.netbeans.modules.payara.common.ui.BasePanel;
import org.netbeans.modules.payara.extended.ui.ConnectionPoolAdvancedAttributesCustomizer;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.CommandSetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.openide.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.extended.nodes.actions.ConnectionPoolAdvancedAttributesCookie;
/**
 *
 * @author Gaurav Gupta
 */
public class Hk2ExtCookie extends Hk2Cookie {
    
    static class ConnectionPoolAdvancedAttributes
            extends Hk2Cookie.Cookie implements ConnectionPoolAdvancedAttributesCookie {

        /** Resources properties query prefix */
        private static final String QUERY_PREFIX = "resources.*";

        /** Properties query common item element. */
        private static final String QUERY_ITEM = ".*";

        /** Properties query common item separator. */
        private static final String QUERY_SEPARATOR = ".";

        /** Resource properties query. */
        private final String query;

        /** Properties customizer. */
        final Class customizer;

        /**
         * Creates an instance of cookie for editing details of module.
         * <p/>
         * @param lookup    Lookup containing {@see CommonServerSupport}.
         * @param name      Name of resource to undeploy.
         * @param cmdSuffix Resource related command suffix.
         */
        ConnectionPoolAdvancedAttributes(final Lookup lookup, final String name,
                final String cmdSuffix, final Class customizer) {
            super(lookup, name);
            final int nameLen = name != null ? name.length() : 0;
            StringBuilder sb = new StringBuilder(QUERY_PREFIX.length()
                    + QUERY_SEPARATOR.length() + nameLen + QUERY_ITEM.length());
            sb.append(QUERY_PREFIX);
            if (nameLen > 0
                    && !PayaraModule.JDBC_RESOURCE.equals(cmdSuffix)) {
                sb.append(QUERY_SEPARATOR);
                sb.append(name);
                sb.append(QUERY_ITEM);
            }
            query = sb.toString();
            this.customizer = customizer;
        }

        @Override
        public void openCustomizer() {
            final BasePanel retVal = new ConnectionPoolAdvancedAttributesCustomizer();
            retVal.initializeUI();
            RequestProcessor.getDefault().post(new Runnable() {

                // fetch the data for the BasePanel
                @Override
                public void run() {
                    if (instance != null) {
                        Future<ResultMap> future = ServerAdmin.<ResultMap>exec(
                                instance, new CommandGetProperty(query));
                        Map<String, String> value;
                        try {
                            ResultMap result = future.get();
                            value = result.getValue();
                        } catch (InterruptedException | ExecutionException ie) {
                            Logger.getLogger("payara")
                                    .log(Level.INFO, ie.getMessage(), ie);
                            value = new HashMap<String, String>();
                        }
                        retVal.initializeData(name, value);
                    }
                }
            });
            DialogDescriptor dd = new DialogDescriptor(retVal,
                    NbBundle.getMessage(this.getClass(), "TITLE_CONNECTION_POOL_ADVANCED_ATTRIBUTES", name),
                    false,
                    new ActionListener() {

                        private void appendErrorReport(StringBuilder sb, final String key, final String value) {
                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            sb.append(key);
                            sb.append("=");
                            sb.append(value != null ? value : "<null>");
                        }

                        @Override
                        public void actionPerformed(ActionEvent event) {
                            if (event.getSource().equals(NotifyDescriptor.OK_OPTION)) {
                                if (instance != null) {
                                    Map<String, String> properties = retVal.getData();
                                    Set<String> keys = properties.keySet();
                                    StringBuilder sb = new StringBuilder();
                                    for (String key : keys) {
                                        String value = properties.get(key);
                                        Future<ResultString> future = ServerAdmin.<ResultString>exec(
                                                instance, new CommandSetProperty(key, value));
                                        try {
                                            ResultString result = future.get();
                                        } catch (InterruptedException | ExecutionException ie) {
                                            appendErrorReport(sb, key, value);
                                        }
                                    }
                                    if (sb.length() > 0) {
                                        Exceptions.printStackTrace(new PartialCompletionException(sb.toString()));
                                    }
                                }
                            }
                        }
                    });
            Dialog d = DialogDisplayer.getDefault().createDialog(dd);
            d.setVisible(true);
        }

    }

}
