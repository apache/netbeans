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
package org.netbeans.modules.profiler.spi;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilerDialogsProvider {
    
    /**
     * Displays a user-level info message. Can be run from any thread.
     * @param message The info message to display
     * @param caption The caption of the dialog, null for default
     * @param details Optional message with detailed information, null for no details
     */
    public abstract void displayInfo(String message, String caption, String details);
    
    /**
     * Displays a user-level info message with a checkbox. Can be run from any thread.
     * @param message The info message to display
     * @param caption The caption of the dialog, null for default
     * @param dnsaMessage The dnsa checkbox label, null for default
     * @param key dialog ID, must be unique for each DNSA dialog
     * @param dnsaDefault true if the dnsa checkbox should be selected by default, false otherwise
     */
    public abstract void displayInfoDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault);
    
    /** Displays a user-level warning message. Can be run from any thread.
     * @param message The warning message to display
     * @param caption The caption of the dialog, null for default
     * @param details Optional message with detailed information, null for no details
     */
    public abstract void displayWarning(String message, String caption, String details);
    
    /**
     * Displays a user-level warning message with a checkbox. Can be run from any thread.
     * @param message The warning message to display
     * @param caption The caption of the dialog, null for default
     * @param dnsaMessage The dnsa checkbox label, null for default
     * @param key dialog ID, must be unique for each DNSA dialog
     * @param dnsaDefault true if the dnsa checkbox should be selected by default, false otherwise
     */
    public abstract void displayWarningDNSA(String message, String caption, String dnsaMessage, String key, boolean dnsaDefault);
    
    /** Displays a user-level error message. Can be run from any thread.
     * @param message The error message to display
     * @param caption The caption of the dialog, null for default
     * @param details Optional message with detailed information, null for no details
     */
    public abstract void displayError(String message, String caption, String details);
    
    /**
     * Displays a user-level confirmation message. Can be run from any thread.
     * @param message The confirmation message to display
     * @param caption The caption of the dialog, null for default
     * @param cancellable true if the dialog should display Cancel option, false otherwise
     * @return Boolean.TRUE if the user has confirmed the dialog, Boolean.FALSE if the user has rejected the dialog, null if the dialog has been cancelled
     */
    public abstract Boolean displayConfirmation(String message, String caption, boolean cancellable);
    
    /**
     * Displays a user-level confirmation message with a checkbox. Can be run from any thread.
     * @param message The confirmation message to display
     * @param caption The caption of the dialog, null for default
     * @param dnsaMessage The dnsa checkbox label, null for default
     * @param cancellable true if the dialog should display Cancel option, false otherwise
     * @param key dialog ID, must be unique for each DNSA dialog
     * @param dnsaDefault true if the dnsa checkbox should be selected by default, false otherwise
     * @return Boolean.TRUE if the user has confirmed the dialog, Boolean.FALSE if the user has rejected the dialog, null if the dialog has been cancelled
     */
    public abstract Boolean displayConfirmationDNSA(String message, String caption, String dnsaMessage, boolean cancellable, String key, boolean dnsaDefault);
    
}
