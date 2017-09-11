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
package org.netbeans.jellytools;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.NbBundle;

/**
 * Base class for testing resurce bundle keys used in a
 * particular jellytools cluster.
 * Descendants need to implement the getPropertiesName() and getDescendantClass()
 * methods and also add the suite() and main() methods.
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author <a href="mailto:vojtech.sigler@sun.com">Vojtech Sigler</a>
 */
public abstract class TestBundleKeys extends NbTestCase {

    private static Properties props = new Properties();

    /**
     * Descendants implement this method to return a proper name.
     * e.g. org/netbeans/jellytools/BundleKeysTest.properties
     *
     * @return name of the descendants-specific properties file
     */
    protected abstract String getPropertiesName();

    /**
     * Descendants need to implement this method to return their classloader.
     * It is needed to load their specific properties file.
     *
     * @return descendant class
     */
    protected abstract ClassLoader getDescendantClassLoader();


    public TestBundleKeys(String isBundleName) {
        super(isBundleName);
    }
    
    protected static Properties getProperties(ClassLoader irClassLoader, String isPropertiesName)
            throws IOException
    {
        if(props.isEmpty()){
            props.load(irClassLoader.getResourceAsStream(isPropertiesName));
        }

        return props;
    }

    /**
     * Performs the test itself.
     *
     * @throws java.lang.Throwable
     */
    protected void runTest() throws Throwable {

        String keys = getProperties(getDescendantClassLoader(), getPropertiesName()).getProperty(getName());

        ResourceBundle lrBundle=NbBundle.getBundle(getName());

        String[] lrTokens = keys.split(",");        
        int lnNumMissing = 0;
        StringBuffer lrBufMissing = new StringBuffer();
        for (String lsKey : lrTokens)
        try {
            lrBundle.getObject(lsKey);
        } catch (MissingResourceException mre) {
            lrBufMissing.append(lsKey).append(" ");
            lnNumMissing++;
        }
        if (lnNumMissing > 0)
            throw new AssertionFailedError("Missing "+String.valueOf(lnNumMissing)+" key(s): "+ lrBufMissing.toString());

    }

    /**
     * Does common things needed in the suite() method that descendants need to
     * create.
     *
     * @param irClass descendant class
     * @param isPropertiesName  name of the descendants-specific properties file
     * @return prepared test
     */
    protected static Test prepareSuite(Class irClass, String isPropertiesName)
    {
        NbModuleSuite.Configuration lrConf = NbModuleSuite.createConfiguration(irClass);
        try {
            Set bundles = getProperties(irClass.getClassLoader(), isPropertiesName).keySet();
            for(Object bundle : bundles) {
                lrConf = lrConf.addTest((String) bundle);
            }
        } catch (Exception e) {}
        return NbModuleSuite.create(lrConf.clusters(".*").enableModules(".*"));
    }


}
