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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.validation.Constants;


/**
 * BundleReader  is a Class  to read properties from the bundle.
 * <code>getValue()</code> method can be used to read the properties
 * from the bundle file(Bundle.properties). Default bundle file used
 * is <code>{ @link Constants }.BUNDLE_FILE</code>. Bundle file to use
 * can be set by using <code>setBundle()</code> method.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class BundleReader {

    /**
     * A resource bundle of this reader.
     */
    // !PW FIXME Get rid of this, probably requires eliminating this entire class
    //     and upgrading callers to use some NbBundle variant for bundle management.
    //     See IZ 96422
    private static ResourceBundle resourceBundle;

    
    /** Creates a new instance of BundleReader */
    public BundleReader() {
    }

    /**
     * Gets the value of the the given <code>key</code> from the bundle
     * 
     * @param key the key of which, the value needs to be fetched from
     * the bundle.
     */
    public static String getValue(String key) {
        if(resourceBundle == null)
            return key;
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException missingResourceException) {
            return key;
        }
    }


    /**
     * sets the given bundle file as the file to use by this object.
     */
    public static void setBundle(String bundleFile){
        try {
            resourceBundle = ResourceBundle.getBundle(bundleFile);
        } catch (Exception ex) { }
    }
    

    static {
        try {
            resourceBundle = ResourceBundle.getBundle(Constants.BUNDLE_FILE);
        } catch (Exception ex) { }
    }
}
