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
package org.netbeans.api.xml.parsers;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author Libor Kramolis
 */
class Util  {

    /** Default and only one instance of this class. */
    public static final Util THIS = new Util();

    /** Nobody can create instance of it, just me. */
    private Util () {
    }

    /** Instance package ErrorManager. */
    private static final Logger LOG = Logger.getLogger(Util.class.getName());
    /** Default debug severity used with ErrorManager. */
    private static final Level DEBUG_SEVERITY = Level.INFO;

    //
    // String localizing purposes
    //


    /** 
     * Get localized string from package bundle.
     * @param key Key identifing localized value.
     * @return localized value.
     */
    public final String getString (String key) {
        if (key == null) throw new NullPointerException();
	return NbBundle.getMessage (this.getClass(), key);
    }
    
    /** 
     * Get localized string from package bundle.
     * @param key Key identifing localized value (<code>MessageFormat</code>).
     * @param param An argument <code>{0}</code> used for message parametrization.
     * @return localized value.
     */
    public final String getString (String key, Object param) {
        if (key == null) throw new NullPointerException();        
	return NbBundle.getMessage (this.getClass(), key, param);
    }
    
    /**
     * Get localized string from package bundle.
     * @param key Key identifing localized value (<code>MessageFormat</code>).
     * @param param1 An argument <code>{0}</code> used for message parametrization.
     * @param param2 An argument <code>{1}</code> used for message parametrization.
     * @return Localized value.
     */
    public final String getString (String key, Object param1, Object param2) {
        if (key == null) throw new NullPointerException();        
	return NbBundle.getMessage (this.getClass(), key, param1, param2);
    }
    
    /** 
     * Get localized character from package bundle. Usually used on mnemonic.
     * @param key Key identifing localized value.
     * @return localized value.
     */
    public final char getChar (String key) {
        if (key == null) throw new NullPointerException();        
	return NbBundle.getMessage (this.getClass(), key).charAt (0);
    }
    
    
    //
    // Debugging purposes
    //
    
    /**
     * Check whether running at loggable level.
     * @return true if <code>debug (...)</code> will log something.
     */
    public final boolean isLoggable () {
        return isLoggable (DEBUG_SEVERITY);
    }

    /**
     * Check whether running at loggable level.
     * @param level the log level
     * @return true if <code>debug (...)</code> will log something.
     */
    public final boolean isLoggable (Level level) {
        return LOG.isLoggable (level);
    }

    /**
     * Log a message if package log level passes.
     * @param message Message to log down. <code>null</code> is allowed
     *        but is not logged.
     */
    public final void debug (String message) {
        debug(DEBUG_SEVERITY, message);
    }

    /**
     * Log a message if package log level passes.
     * @param level the log level
     * @param message Message to log down. <code>null</code> is allowed
     *        but is not logged.
     */
    public final void debug (Level level, String message) {
        if (message == null) return;
        LOG.log (level, message);
    }

    /**
     * Always log a exception.
     * @param ex Exception to log down. <code>null</code> is allowed
     *           but is not logged.
     */
    public final void debug (Throwable ex) {
        if (ex == null) return;
        LOG.log (DEBUG_SEVERITY, null, ex);
    }

    /**
     * Always log an annotated exception.
     * @param message Message used for exception annotation or <code>null</code>.
     * @param ex Exception to log down. <code>null</code> is allowed
     *        but is not logged.
     */
    public final void debug (String message, Throwable ex) {
        if (ex == null) return;
        LOG.log (DEBUG_SEVERITY, message, ex);
    }
}
