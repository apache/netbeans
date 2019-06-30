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
package org.netbeans.modules.payara.common.nodes;

import org.netbeans.modules.payara.tooling.admin.CommandUndeploy;
import org.netbeans.modules.payara.tooling.admin.CommandDisable;
import org.netbeans.modules.payara.tooling.admin.CommandDeploy;
import org.netbeans.modules.payara.tooling.admin.CommandSetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.CommandEnable;
import org.netbeans.modules.payara.tooling.admin.CommandDeleteResource;
import org.netbeans.modules.payara.tooling.admin.ServerAdmin;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.netbeans.modules.payara.common.CommonServerSupport;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.PartialCompletionException;
import org.netbeans.modules.payara.common.nodes.actions.DeployDirectoryCookie;
import org.netbeans.modules.payara.common.nodes.actions.DisableModulesCookie;
import org.netbeans.modules.payara.common.nodes.actions.EditDetailsCookie;
import org.netbeans.modules.payara.common.nodes.actions.DisableCDIProbeModeCookie;
import org.netbeans.modules.payara.common.nodes.actions.EnableCDIProbeModeCookie;
import org.netbeans.modules.payara.common.nodes.actions.EnableModulesCookie;
import org.netbeans.modules.payara.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.payara.common.nodes.actions.UndeployModuleCookie;
import org.netbeans.modules.payara.common.nodes.actions.UnregisterResourceCookie;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.common.ui.BasePanel;
import org.netbeans.modules.payara.common.nodes.actions.ConnectionPoolAdvancedAttributesCookie;
import org.netbeans.modules.payara.common.ui.ConnectionPoolAdvancedAttributesCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 * HK2 nodes cookies.
 * <p/>
 * @author Tomas Kraus
 * @author Gaurav Gupta
 */
public class Hk2Cookie {
    
    private static final String CDI_PROBE_MODE_PROP = "applications.application.%s.property.cdiDevModeEnabled";
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Common node cookie.
     */
    protected static abstract class Cookie {

        /** Task status. */
        protected volatile WeakReference<Future<ResultString>> status;

        /** Payara server instance. */
        protected final PayaraInstance instance;

        /** Resource name. */
        protected final String name;

        /**
         * Creates an instance of cookie.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name   Name of resource to be enabled.
         */
        protected Cookie(final Lookup lookup, final String name) {
            this.instance = getPayaraInstance(lookup);
            this.name = name;
        }

        /**
         * Returns <code>true</code> if this task is still running.
         * <p/>
         * @return Value of <code>true</code> if this task is still running
         *         or <code>false</code> otherwise.
         */
        public boolean isRunning() {
            WeakReference<Future<ResultString>> localref = status;
            if (localref == null) {
                return false;
            }
            Future<ResultString> future = localref.get();
            return future != null && !future.isDone();
        }
    }

    /**
     * Enable node cookie.
     */
    static class Enable
            extends Cookie implements EnableModulesCookie {

        /**
         * Creates an instance of cookie for enabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name Name of resource to be enabled.
         */
        Enable(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Enable module on Payara server.
         * <p/>
         * @return Result of enable task execution.
         */
        @Override
        public Future<ResultString> enableModule() {
            if (instance != null) {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandEnable(name, Util.computeTarget(
                        instance.getProperties())));
                status = new WeakReference<Future<ResultString>>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Disable node cookie.
     */
    static class Disable
            extends Cookie implements DisableModulesCookie {

        /**
         * Creates an instance of cookie for disabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name Name of resource to be disabled.
         */
        Disable(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Disable module on Payara server.
         * <p/>
         * @return Result of disable task execution.
         */
        @Override
        public Future<ResultString> disableModule() {
            if (instance != null) {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandDisable(name, Util.computeTarget(
                        instance.getProperties())));
                status = new WeakReference<Future<ResultString>>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Enable CDI probe mode cookie.
     */
    static class EnableCDIProbeMode
            extends Cookie implements EnableCDIProbeModeCookie {

        
        /**
         * Creates an instance of cookie for enabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name Name of resource to be enabled in probe mode.
         */
        EnableCDIProbeMode(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Enable application CDI probe mode on Payara server.
         * <p/>
         * @return Result of enable task execution.
         */
        @Override
        public Future<ResultString> enableCDIProbeMode() {
            if (instance != null) {
                String key = String.format(CDI_PROBE_MODE_PROP, name);
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandSetProperty(key, Boolean.TRUE.toString()));
                status = new WeakReference<>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Disable node cookie.
     */
    static class DisableCDIProbeMode
            extends Cookie implements DisableCDIProbeModeCookie {

        /**
         * Creates an instance of cookie for disabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name Name of resource to be disabled in probe mode.
         */
        DisableCDIProbeMode(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Disable application CDI probe mode on Payara server.
         * <p/>
         * @return Result of disable task execution.
         */
        @Override
        public Future<ResultString> disableCDIProbeMode() {
            if (instance != null) {
                String key = String.format(CDI_PROBE_MODE_PROP, name);
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandSetProperty(key, Boolean.FALSE.toString()));
                status = new WeakReference<>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Connection Pool advanced attributes cookie.
     */
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

    /**
     * Undeploy node cookie.
     */
    static class Undeploy
            extends Cookie implements UndeployModuleCookie {

        /**
         * Creates an instance of cookie for disabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name   Name of resource to undeploy.
         */
        Undeploy(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Undeploy module on Payara server.
         * <p/>
         * @return Result of undeploy task execution.
         */
        @Override
        public Future<ResultString> undeploy() {
            if (instance != null) {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandUndeploy(name, Util.computeTarget(
                        instance.getProperties())));
                status = new WeakReference<Future<ResultString>>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Deploy node cookie.
     */
    static class Deploy
            extends Cookie implements DeployDirectoryCookie {

        /**
         * Creates an instance of cookie for disabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         */
        Deploy(final Lookup lookup) {
            super(lookup, null);
        }

        /**
         * Deploy module from directory on Payara server.
         * <p/>
         * @return Result of undeploy task execution.
         */
        @Override
        public Future<ResultString> deployDirectory() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getMessage(Hk2ItemNode.class,
                    "LBL_ChooseButton"));
            chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            int returnValue = chooser.showDialog(WindowManager.getDefault()
                    .getMainWindow(), NbBundle.getMessage(
                    Hk2ItemNode.class, "LBL_ChooseButton"));
            if (instance != null
                    || returnValue != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            final File dir
                    = new File(chooser.getSelectedFile().getAbsolutePath());

            Future<ResultString> future = ServerAdmin.<ResultString>exec(
                    instance, new CommandDeploy(dir.getParentFile().getName(),
                    Util.computeTarget(instance.getProperties()),
                    dir, null, null, null));
            status = new WeakReference<Future<ResultString>>(future);
            return future;
        }
    }

    /**
     * Unregister node cookie.
     */
    static class Unregister
            extends Cookie implements UnregisterResourceCookie {

        /** Command suffix. */
        final String cmdSuffix;

        /** Name of query property which contains resource name. */
        final String cmdPropertyName;

        /** Delete also dependent resources when <code>true</code>. */
        final boolean cascadeDelete;

        /**
         * Creates an instance of cookie for unregistering module.
         * <p/>
         * @param lookup          Lookup containing {@see CommonServerSupport}.
         * @param name            Name of resource to unregister.
         * @param cmdSuffix       Resource related command suffix.
         * @param cmdPropertyName Name of query property which contains
         *                        resource name.
         * @param cascadeDelete   Delete also dependent resources
         *                        when <code>true</code>.
         */
        Unregister(final Lookup lookup, final String name,
                final String cmdSuffix, final String cmdPropertyName,
                final boolean cascadeDelete) {
            super(lookup, name);
            this.cmdSuffix = cmdSuffix;
            this.cmdPropertyName = cmdPropertyName;
            this.cascadeDelete = cascadeDelete;
        }

        @Override
        public Future<ResultString> unregister() {
            if (instance != null) {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandDeleteResource(
                        name, cmdSuffix, cmdPropertyName, cascadeDelete));
                status = new WeakReference<Future<ResultString>>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Refresh node cookie.
     */
    static class Refresh implements RefreshModulesCookie {

        /** Child nodes to be refreshed. */
        private final Children children;

        /**
         * Creates an instance of cookie for refreshing nodes.
         * <p/>
         * @param children Child nodes to be refreshed.
         */
        Refresh(Children children) {
            this.children = children;
        }

        /**
         * Refresh child nodes.
         */
        @Override
        public RequestProcessor.Task refresh() {
            return refresh(null, null);
        }

        /**
         * Refresh child nodes.
         * <p/>
         * @param expected   Expected node display name.
         * @param unexpected Unexpected node display name.
         */
        @Override
        public RequestProcessor.Task refresh(String expected, String unexpected) {
            if (children instanceof Refreshable) {
                ((Refreshable) children).updateKeys();
                boolean foundExpected = expected == null;
                boolean foundUnexpected = false;
                for (Node node : children.getNodes()) {
                    if (!foundExpected
                            && node.getDisplayName().equals(expected)) {
                        foundExpected = true;
                    }
                    if (!foundUnexpected
                            && node.getDisplayName().equals(unexpected)) {
                        foundUnexpected = true;
                    }
                }
                if (!foundExpected) {
                    Logger.getLogger("payara").log(Level.WARNING, null,
                            new IllegalStateException(
                            "did not find a child node, named " + expected));
                }
                if (foundUnexpected) {
                    Logger.getLogger("payara").log(Level.WARNING, null,
                            new IllegalStateException(
                            "found unexpected child node, named "
                            + unexpected));
                }
            }
            return null;
        }
    }

    static class EditDetails
            extends Cookie implements EditDetailsCookie {

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
        EditDetails(final Lookup lookup, final String name,
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
            final BasePanel retVal = getBasePanel();
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
                    NbBundle.getMessage(this.getClass(), "TITLE_RESOURCE_EDIT", name),
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

        private BasePanel getBasePanel() {
            BasePanel temp;
            try {
                temp = (BasePanel) customizer.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                temp = new BasePanel.Error();
                Exceptions.printStackTrace(ex);
            }
            return temp;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve Payara instance from {@see Lookup} object.
     * <p/>
     * @param lookup Lookup containing {@see CommonServerSupport}.
     * @return Payara instance retrieved from lookup object.
     */
    private static PayaraInstance getPayaraInstance(final Lookup lookup) {
        CommonServerSupport commonModule = lookup.lookup(
                CommonServerSupport.class);
        return commonModule != null ? commonModule.getInstance() : null;
    }

}
