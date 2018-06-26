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
package org.netbeans.modules.websvc.design.javamodel;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

/**
 *
 * @author Jan Lahoda
 */
public final class SourceUtilsTestUtil extends ProxyLookup {
    
    private static SourceUtilsTestUtil DEFAULT_LOOKUP = null;
    
    public SourceUtilsTestUtil() {
        System.out.println("calling constructor "+DEFAULT_LOOKUP);
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    private static Object[] extraLookupContent = null;
    
    public static void prepareTest(String[] additionalLayers, Object[] additionalLookupContent) throws IOException, SAXException, PropertyVetoException {
        URL[] layers = new URL[additionalLayers.length];
        
        for (int cntr = 0; cntr < additionalLayers.length; cntr++) {
            layers[cntr] = Thread.currentThread().getContextClassLoader().getResource(additionalLayers[cntr]);
        }
        
        XMLFileSystem xmlFS = new XMLFileSystem();
        xmlFS.setXmlUrls(layers);
        
        FileSystem system = new MultiFileSystem(new FileSystem[] {FileUtil.createMemoryFileSystem(), xmlFS});
        
        Repository repository = new Repository(system);
        extraLookupContent = new Object[additionalLookupContent.length + 1];
        
        System.arraycopy(additionalLookupContent, 0, extraLookupContent, 1, additionalLookupContent.length);
        
        extraLookupContent[0] = repository;
        
        setLookup(extraLookupContent, SourceUtilsTestUtil.class.getClassLoader());
    }
    
    static {
        SourceUtilsTestUtil.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", SourceUtilsTestUtil.class.getName());
        Assert.assertEquals(SourceUtilsTestUtil.class, Lookup.getDefault().getClass());
    }
    
    public static void compileRecursively(FileObject sourceRoot) throws Exception {
        List<FileObject> queue = new LinkedList<FileObject>();
        
        queue.add(sourceRoot);
        
        while (!queue.isEmpty()) {
            FileObject file = queue.remove(0);
            
            if (file.isData()) {
//                CountDownLatch l = RepositoryUpdater.getDefault().scheduleCompilationAndWait(file, sourceRoot);
//                l.await(60, TimeUnit.SECONDS);
                IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), Collections.singleton(file.getURL()));
            } else {
                queue.addAll(Arrays.asList(file.getChildren()));
            }
        }
    } 
    
}
