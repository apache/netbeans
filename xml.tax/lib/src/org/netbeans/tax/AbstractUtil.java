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
package org.netbeans.tax;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 *
 * @author Libor Kramolis
 * @version 0.2
 */
public abstract class AbstractUtil {
    /** Cached isLoggable value. */
    private boolean loggable;
    /** Has loggable already been initialized. */
    private boolean loggableInit = false;
    /** Package resource bundle. */
    private ResourceBundle bundle;


    /**
     * Kind of equals that treat null-ed object as equvivalent.
     * Suitable while testing for property equalence before firing.
     * return true if these are same
     */
    public static boolean equals (Object a, Object b) {
        if ( a != null ) {
            return (a.equals (b));
        } else {
            return (a == b);
        }
    }

    /**
     * Just for debugging purposes.
     */
    public final void debug (String message, Throwable ex) {
        if ( isLoggable() ) {
            System.err.println("[org.netbeans.tax] " + message);
            if ( ex != null ) {
                ex.printStackTrace (System.err);
            }
        }
    }
    
    
    /**
     * Just for debugging purposes.
     */
    public final void debug (String message) {
        debug (message, null);
    }


    /**
     * Just for debugging purposes.
     */
    public final void debug (Throwable ex) {
        debug (ex.getMessage(), ex);
    }


    /** Test if <code>debug (...)</code> will log something.
     */
    public final synchronized boolean isLoggable () {
        if ( loggableInit == false ) {
            loggable = Boolean.getBoolean("org.netbeans.tax"); // NOI18N
            loggableInit = true;
        }
        return loggable;
    }

    /**
     * @return bundle for this instance package
     */
    protected final synchronized ResourceBundle getBundle () {
        return ResourceBundle.getBundle(getClass().getName().replaceFirst("\\.[^.]+$", ".Bundle")); // NOI18N
    }
    
    /** Get localized string.
     * @param key key of localized value.
     * @return localized value.
     */
    public final String getString (String key) {
        return getBundle ().getString (key);
    }
    
    /** Get localized string by passing parameter.
     * @param key key of localized value.
     * @param param argument to use when formating the message
     * @return localized value.
     */
    public final String getString (String key, Object param) {
        return MessageFormat.format (getBundle().getString (key), new Object[] {param});
    }
    
    /** Get localized character. Usually used on mnemonic.
     * @param key key of localized value.
     * @return localized value.
     */
    public final char getChar (String key) {
        return getString (key).charAt (0);
    }

}
