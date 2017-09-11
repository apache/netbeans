/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    public Object getDetail(Class klass) {
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
