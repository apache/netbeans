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

package org.netbeans.api.xml.cookies;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Extensible and immutable <code>CookieObserver</code> message.
 * Look at particular cookie what detail subclasses it supports.
 *
 * @author  Petr Kuzel
 * @since   0.8
 */
public final class CookieMessage {

    // details
    private final Lookup details;

    // localized message
    private final String message;

    // message level
    private final int level;

    /**
     * Receive a localized message not tied with the processing problems.
     */
    public static final int INFORMATIONAL_LEVEL = 0;

    /**
     * Receive notification of a warning.
     */            
    public static final int WARNING_LEVEL = 1;

    /**
     * Receive notification of a recoverable error.
     */        
    public static final int ERROR_LEVEL = 2;

    /**
     * Receive notification of a non-recoverable error.
     */            
    public static final int FATAL_ERROR_LEVEL = 3;

    /**
     * Create new informational level message.
     * @param message Localized message.
     */
    public CookieMessage(String message) {
        this( message, INFORMATIONAL_LEVEL, null);
    }

    /**
     * Create new message.
     * @param message Localized message.
     * @param level Message level.
     */        
    public CookieMessage(String message, int level) {
        this( message, level, null);
    }

    /**
     * Create new informational level message with structured detail.
     * @param message Localized message.
     * @param detail Structured detail attached to the message.
     */
    public CookieMessage(String message, Object detail) {
        this( message, INFORMATIONAL_LEVEL, detail);
    }
    
    /**
     * Create new message with structured detail.
     * @param message Localized message.
     * @param level Message level.
     * @param detail Structured detail attached to the message.
     */        
    public CookieMessage(String message, int level, Object detail) {        
        this(message, level, Lookups.singleton(detail));
    }

    /**
     * Create new message with structured detail.
     * @param message Localized message.
     * @param level Message level.
     * @param details Lookup holding structured details.
     */        
    public CookieMessage(String message, int level, Lookup details) {
        if (message == null) throw new NullPointerException();
        if (level < INFORMATIONAL_LEVEL || level > FATAL_ERROR_LEVEL)
            throw new IllegalArgumentException();

        this.message = message;
        this.level = level;
        this.details = details == null ? Lookup.EMPTY : details;
    }
    
    
    /**
     * @return Localized message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return Message level.
     */        
    public final int getLevel() {
        return level;
    }

    /**
     * Query for structured detail attached to the message.
     * @param  klass Requested detail subclass.
     * @return Instance of requested structured detail or <code>null</code>.
     */
    public <T> T getDetail(Class<T> klass) {
        return details.lookup(klass);
    }

    /**
     * Query for structured details attached to the message.
     * @return Lookup of attached structured details.
     */    
    public Lookup getDetails() {
        return details;
    }
}
