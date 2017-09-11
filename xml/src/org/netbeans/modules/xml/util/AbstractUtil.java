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
package org.netbeans.modules.xml.util;

import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.*;
import java.util.StringTokenizer;
import java.util.MissingResourceException;

/**
 * This provides package scope utilities for debugging and code
 * internationalization. It is designed to have a subclass,
 * <code>ErrorManager</code> instance and bundle in each client's package.
 * <p>
 * It ensures localized strings will be loaded from <code>Bundle.properties</code>
 * (branded) in same package as subclass belongs.
 * <p>
 * Debugging methods use {@link org.openide.ErrorManager} to log messages.
 * <code>ErrorManager</code> instance also belongs to sub-class's package.
 *
 * @author     Libor Kramolis
 */
public abstract class AbstractUtil {
    /** Instance package ErrorManager. */
    private ErrorManager packageErrorManager;
    /** Default debug severity used with ErrorManager. */
    private static final int DEBUG_SEVERITY = ErrorManager.INFORMATIONAL;
    
    //
    // String localizing purposes
    //
    

    /** 
     * Get localized string from package bundle.
     * @param key Key identifing localized value.
     * @return localized value.
     */
    public final String getString (Class clazz, String key) {
        if (key == null) throw new NullPointerException();
	return NbBundle.getMessage (clazz, key);
    }
    
    /** 
     * Get localized string from package bundle.
     * @param key Key identifing localized value (<code>MessageFormat</code>).
     * @param param An argument <code>{0}</code> used for message parametrization.
     * @return localized value.
     */
    public final String getString (Class clazz, String key, Object param) {
        if (key == null) throw new NullPointerException();        
	return NbBundle.getMessage (clazz, key, param);
    }
    
    /**
     * Get localized string from package bundle.
     * @param key Key identifing localized value (<code>MessageFormat</code>).
     * @param param1 An argument <code>{0}</code> used for message parametrization.
     * @param param2 An argument <code>{1}</code> used for message parametrization.
     * @return Localized value.
     */
    public final String getString (Class clazz, String key, Object param1, Object param2) {
        if (key == null) throw new NullPointerException();        
	return NbBundle.getMessage (clazz, key, param1, param2);
    }
    
    /** 
     * Get localized character from package bundle. Usually used on mnemonic.
     * @param key Key identifing localized value.
     * @return localized value.
     */
    public final char getChar (Class clazz, String key) {
        if (key == null) throw new NullPointerException();        
	return NbBundle.getMessage (clazz, key).charAt (0);
    }


    /**
     * Loads branded color. Parses syntax given by to Color(int,int,int) constructor:
     * e.g.: <tt>0,255,128</tt>.
     * @return color or <code>null</null> if bundle contains <tt>null</tt> literal
     * (masking) or of entry miss at all.
     * @throws MissingResourceException on invalid color syntax
     */
    public final Color getColor(String key) {
        String raw = null;
        try {
            raw = getString(AbstractUtil.class, key);
        } catch (MissingResourceException e) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(raw, ", \t"); // NOI18N
        if (tokenizer.countTokens() < 3) {
            if (tokenizer.countTokens() == 1) {
                if ("null".equals(tokenizer.nextToken())) return null;  // NOI18N
            }
            throw new MissingResourceException("Invalid color format: " + raw, getClass().getName(), key);  // NOI18N
        }

        String red = tokenizer.nextToken();
        String green = tokenizer.nextToken();
        String blue = tokenizer.nextToken();
        int r = Integer.parseInt(red);
        if (r<0 || r>255) throw new MissingResourceException("Invalid color format: " + raw, getClass().getName(), key);  // NOI18N
        int g = Integer.parseInt(green);
        if (g<0 || g>255) throw new MissingResourceException("Invalid color format: " + raw, getClass().getName(), key);  // NOI18N
        int b = Integer.parseInt(blue);
        if (b<0 || b>255) throw new MissingResourceException("Invalid color format: " + raw, getClass().getName(), key);  // NOI18N
        // ignore remainig tokens, possibly alpha in future

        return new Color(r, g, b);
    }

    //
    // Debugging purposes
    //
    
    /**
     * Check whether running at loggable level.
     * @return true if <code>debug (...)</code> will log something.
     */
    public final boolean isLoggable () {
        return getErrorManager().isLoggable (DEBUG_SEVERITY);
    }

    /**
     * Log a message if package log level passes.
     * @param message Message to log down. <code>null</code> is allowed
     *        but is not logged.
     */
    public final void debug (String message) {
        if (message == null) return;
        getErrorManager().log (DEBUG_SEVERITY, message);
    }

    /**
     * Always log a exception.
     * @param ex Exception to log down. <code>null</code> is allowed
     *           but is not logged.
     */
    public final void debug (Throwable ex) {
        if (ex == null) return;
        getErrorManager().notify (DEBUG_SEVERITY, ex);
    }

    /**
     * Always log an annotated exception.
     * @param message Message used for exception annotation or <code>null</code>.
     * @param ex Exception to log down. <code>null</code> is allowed
     *        but is not logged.
     */
    public final void debug (String message, Throwable ex) {
        if (ex == null) return;
        if (message != null) {
            ex = getErrorManager().annotate(ex, DEBUG_SEVERITY,  message, null, null, null);
        }
        debug (ex);
    }

    /**
     * Provide an <code>ErrorManager</code> instance named per subclass package.
     * @return ErrorManager which is default for package where is class
     * declared .
     */
    public final synchronized ErrorManager getErrorManager () {
        if ( packageErrorManager == null ) {
            String pack = "org.netbeans.modules.xml.core.lib"; // NOI18N
            packageErrorManager = ErrorManager.getDefault().getInstance(pack);
        }
        return packageErrorManager;
    }

}
