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
