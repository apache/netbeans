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
package org.netbeans.modules.profiler.impl;

import org.netbeans.modules.profiler.spi.ProfilerDialogsProvider;
import org.netbeans.modules.profiler.ui.NBHTMLLabel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jiri Sedlacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.ProfilerDialogsProvider.class)
public final class ProfilerDialogsProviderImpl extends ProfilerDialogsProvider {

    @Override
    public void displayInfo(String message, String caption, String details) {
        displayMessage(message, caption, details, NotifyDescriptor.INFORMATION_MESSAGE);
    }

    @Override
    public void displayInfoDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault) {
        displayDNSAMessage(message, caption, dnsaMessage, key, dnsaDefault, NotifyDescriptor.INFORMATION_MESSAGE);
    }

    @Override
    public void displayWarning(String message, String caption, String details) {
        displayMessage(message, caption, details, NotifyDescriptor.WARNING_MESSAGE);
    }

    @Override
    public void displayWarningDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault) {
        displayDNSAMessage(message, caption, dnsaMessage, key, dnsaDefault, NotifyDescriptor.WARNING_MESSAGE);
    }

    @Override
    public void displayError(String message, String caption, String details) {
        displayMessage(message, caption, details, NotifyDescriptor.ERROR_MESSAGE);
    }

    @Override
    public Boolean displayConfirmation(String message, String caption, boolean cancellable) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                cancellable ? NotifyDescriptor.YES_NO_CANCEL_OPTION : NotifyDescriptor.YES_NO_OPTION);
        if (caption != null) nd.setTitle(caption);
        Object ret = DialogDisplayer.getDefault().notify(nd);
        if (ret == NotifyDescriptor.YES_OPTION) return Boolean.TRUE;
        if (ret == NotifyDescriptor.NO_OPTION) return Boolean.FALSE;
        return null;
    }

    @Override
    public Boolean displayConfirmationDNSA(String message, String caption, String dnsaMessage, boolean cancellable, String key, boolean dnsaDefault) {
        ProfilerDialogs.DNSAConfirmation dnsa = new ProfilerDialogs.DNSAConfirmation(
                key, message, cancellable ? NotifyDescriptor.YES_NO_CANCEL_OPTION : NotifyDescriptor.YES_NO_OPTION);
        if (caption != null) dnsa.setTitle(caption);
        if (dnsaMessage != null) dnsa.setDNSAMessage(dnsaMessage);
        dnsa.setDNSADefault(dnsaDefault);
        Object ret = ProfilerDialogs.notify(dnsa);
        if (ret == NotifyDescriptor.YES_OPTION) return Boolean.TRUE;
        if (ret == NotifyDescriptor.NO_OPTION) return Boolean.FALSE;
        return null;
    }
    
    private void displayMessage(String message, String caption, String details, int type) {
        Object msg = message;
        Object det = details;
        if (isHtmlString(message)) msg = new NBHTMLLabel(message);
        if (isHtmlString(details)) det = new NBHTMLLabel(message);
        NotifyDescriptor nd = det == null ? new NotifyDescriptor.Message(msg, type) :
                        new ProfilerDialogs.MessageWithDetails(msg, det, type, false);
        if (caption != null) nd.setTitle(caption);
        ProfilerDialogs.notify(nd);
    }
    
    private void displayDNSAMessage(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault, int type) {
        ProfilerDialogs.DNSAMessage dnsa = new ProfilerDialogs.DNSAMessage(key, message, type);
        if (caption != null) dnsa.setTitle(caption);
        if (dnsaMessage != null) dnsa.setDNSAMessage(dnsaMessage);
        dnsa.setDNSADefault(dnsaDefault);
        ProfilerDialogs.notify(dnsa);
    }
    
    private static boolean isHtmlString(String string) {
        if (string == null) return false;
        // Simple heuristics, seems to work fine
        return string.contains("<") && string.contains(">"); // NOI18N
    }
    
}
