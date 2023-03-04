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
package org.netbeans.modules.profiler.api;

import org.netbeans.modules.profiler.spi.ProfilerDialogsProvider;
import org.openide.util.Lookup;

/**
 * Support for displaying dialogs by the profiler.
 *
 * @author Jiri Sedlacek
 */
public final class ProfilerDialogs {
    
    /**
     * Displays a user-level info message. Can be run from any thread.
     * @param message The info message to display
     */
    public static void displayInfo(String message) {
        displayInfo(message, null, null);
    }
    
    /**
     * Displays a user-level info message. Can be run from any thread.
     * @param message The info message to display
     * @param caption The caption of the dialog, null for default
     * @param details Optional message with detailed information, null for no details
     */
    public static void displayInfo(String message, String caption, String details) {
        ProfilerDialogsProvider p = provider();
        if (p != null) p.displayInfo(message, caption, details);
    }
    
    /**
     * Displays a user-level info message with a checkbox. Can be run from any thread.
     * @param message The info message to display
     * @param caption The caption of the dialog, null for default
     * @param dnsaMessage The dnsa checkbox label, null for default
     * @param key dialog ID, must be unique for each DNSA dialog
     * @param dnsaDefault true if the dnsa checkbox should be selected by default, false otherwise
     */
    public static void displayInfoDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault) {
        ProfilerDialogsProvider p = provider();
        if (p != null) p.displayInfoDNSA(message, caption, dnsaMessage, key, dnsaDefault);
    }
    
    /** Displays a user-level warning message. Can be run from any thread.
     * @param message The warning message to display
     */
    public static void displayWarning(String message) {
        displayWarning(message, null, null);
    }
    
    /** Displays a user-level warning message. Can be run from any thread.
     * @param message The warning message to display
     * @param caption The caption of the dialog, null for default
     * @param details Optional message with detailed information, null for no details
     */
    public static void displayWarning(String message, String caption, String details) {
        ProfilerDialogsProvider p = provider();
        if (p != null) p.displayWarning(message, caption, details);
    }
    
    /**
     * Displays a user-level warning message with a checkbox. Can be run from any thread.
     * @param message The warning message to display
     * @param caption The caption of the dialog, null for default
     * @param dnsaMessage The dnsa checkbox label, null for default
     * @param key dialog ID, must be unique for each DNSA dialog
     * @param dnsaDefault true if the dnsa checkbox should be selected by default, false otherwise
     */
    public static void displayWarningDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault) {
        ProfilerDialogsProvider p = provider();
        if (p != null) p.displayWarningDNSA(message, caption, dnsaMessage, key, dnsaDefault);
    }
    
    /** Displays a user-level error message. Can be run from any thread.
     * @param message The error message to display
     */
    public static void displayError(String message) {
        displayError(message, null, null);
    }
    
    /** Displays a user-level error message. Can be run from any thread.
     * @param message The error message to display
     * @param caption The caption of the dialog, null for default
     * @param details Optional message with detailed information, null for no details
     */
    public static void displayError(String message, String caption, String details) {
        ProfilerDialogsProvider p = provider();
        if (p != null) p.displayError(message, caption, details);
    }
    
    /**
     * Displays a user-level confirmation message. Can be run from any thread.
     * @param message The confirmation message to display
     * @return true if the user has confirmed the dialog, false otherwise
     */
    public static boolean displayConfirmation(String message) {
        return displayConfirmation(message, null);
    }
    
    /**
     * Displays a user-level confirmation message. Can be run from any thread.
     * @param message The confirmation message to display
     * @param caption The caption of the dialog, null for default
     * @return true if the user has confirmed the dialog, false otherwise
     */
    public static boolean displayConfirmation(String message, String caption) {
        ProfilerDialogsProvider p = provider();
        if (p != null) return Boolean.TRUE.equals(p.displayConfirmation(message, caption, false));
        else return false;
    }
    
    /**
     * Displays a user-level cancellable confirmation message. Can be run from any thread.
     * @param message The confirmation message to display
     * @param caption The caption of the dialog, null for default
     * @return Boolean.TRUE if the user has confirmed the dialog, Boolean.FALSE if the user has rejected the dialog, null if the dialog has been cancelled
     */
    public static Boolean displayCancellableConfirmation(String message, String caption) {
        ProfilerDialogsProvider p = provider();
        if (p != null) return p.displayConfirmation(message, caption, true);
        else return false;
    }
    
    /**
     * Displays a user-level confirmation message with a checkbox. Can be run from any thread.
     * @param message The confirmation message to display
     * @param caption The caption of the dialog, null for default
     * @param dnsaMessage The dnsa checkbox label, null for default
     * @param key dialog ID, must be unique for each DNSA dialog
     * @param dnsaDefault true if the dnsa checkbox should be selected by default, false otherwise
     * @return true if the user has confirmed the dialog, false otherwise
     */
    public static boolean displayConfirmationDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault) {
        ProfilerDialogsProvider p = provider();
        if (p != null) return Boolean.TRUE.equals(p.displayConfirmationDNSA(message, caption, dnsaMessage, false, key, dnsaDefault));
        else return false;
    }
    
    /**
     * Displays a user-level cancellable confirmation message with a checkbox. Can be run from any thread.
     * @param message The confirmation message to display
     * @param caption The caption of the dialog, null for default
     * @param dnsaMessage The dnsa checkbox label, null for default
     * @param key dialog ID, must be unique for each DNSA dialog
     * @param dnsaDefault true if the dnsa checkbox should be selected by default, false otherwise
     * @return Boolean.TRUE if the user has confirmed the dialog, Boolean.FALSE if the user has rejected the dialog, null if the dialog has been cancelled
     */
    public static Boolean displayCancellableConfirmationDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault) {
        ProfilerDialogsProvider p = provider();
        if (p != null) return p.displayConfirmationDNSA(message, caption, dnsaMessage, true, key, dnsaDefault);
        else return false;
    }
    
    private static ProfilerDialogsProvider provider() {
        return Lookup.getDefault().lookup(ProfilerDialogsProvider.class);
    }
    
}
