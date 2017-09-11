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

package org.netbeans.modules.quicksearch;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import javax.swing.text.Utilities;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

/**
 * Allows tests to install own layers for testing.
 * Copied from org.netbeans.api.project.TestUtil.
 *
 * @author Dafe Simonek
 */
public class UnitTestUtils extends ProxyLookup {

    public static UnitTestUtils DEFAULT_LOOKUP = null;

    /** Creates a new instance of UnitTestUtils */
    public UnitTestUtils() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    /** Makes global layer from given string resource info */
    public static void prepareTest(String[] stringLayers) 
                throws IOException, SAXException, PropertyVetoException {
        prepareTest(stringLayers, null);
    }
    
    public static void prepareTest (String[] stringLayers, Lookup lkp) 
                throws IOException, SAXException, PropertyVetoException {
        URL[] layers = new URL[stringLayers.length];
        
        for (int cntr = 0; cntr < layers.length; cntr++) {
            layers[cntr] = Utilities.class.getResource(stringLayers[cntr]);
        }
        
        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(layers);
        
        Repository repository = new Repository(system);
        
        if (lkp == null) {
            UnitTestUtils.setLookup(new Object[] { repository }, UnitTestUtils.class.getClassLoader());
        } else {
            UnitTestUtils.setLookup(new Object[] { repository }, lkp, UnitTestUtils.class.getClassLoader());
        }
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    private static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    private static void setLookup(Object[] instances, Lookup lkp, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            lkp,        
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    
    static {
        UnitTestUtils.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", UnitTestUtils.class.getName());
        Assert.assertEquals(UnitTestUtils.class, Lookup.getDefault().getClass());
    }
    
    public static void initLookup() {
        //currently nothing.
    }

}
