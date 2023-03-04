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

package org.netbeans.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.jar.Attributes;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.startup.ManifestSection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Checking the behaviour of entity resolvers.
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // not for me, but apparently on core builder
public class NbLoaderPoolResolverChangeTest extends NbTestCase implements ChangeListener {
    private FileObject fo;
    private Lenka loader;
    private ManifestSection.LoaderSection ls;
    private int poolChange;
    private static ErrorManager err;
    
    public NbLoaderPoolResolverChangeTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        // XXX NbRepository seems to randomly produce different instances of Services/MIMEResolver
        // so when using "real" repo, test can fail randomly
        // XXX cannot use MockLookup for now or MetaInfServicesTest fails
        // (clash between org.foo.Interface in openide.util/test and services-jar-1)
        System.setProperty(Lookup.class.getName(), MyLkp.class.getName());
        FileUtil.createFolder(FileUtil.getConfigRoot(), "Services/MIMEResolver");
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());

        NbLoaderPool.IN_TEST = true; // allow it to fire changes w/o module system
        DataLoaderPool.getDefault().addChangeListener(this);
        
        assertEquals("No change in pool during initialization", 0, poolChange);
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        
        fo = FileUtil.createData(lfs.getRoot(), "X.lenka");
        
        Attributes at = new Attributes();
        at.putValue("OpenIDE-Module-Class", "Loader");
        String name = Lenka.class.getName().replace('.', '/') + ".class";
        ls = (ManifestSection.LoaderSection)ManifestSection.create(name, at, null);
        NbLoaderPool.add(ls);
        
        loader = Lenka.getLoader(Lenka.class);
    }
    public static class MyLkp extends ProxyLookup {
        public MyLkp() {
            this(NbLoaderPoolResolverChangeTest.class.getClassLoader());
        }
        private MyLkp(ClassLoader l) {
            super(Lookups.fixed(new Repository(FileUtil.createMemoryFileSystem())), Lookups.metaInfServices(l), Lookups.singleton(l));
        }
    }

    @Override
    protected void tearDown() throws Exception {
        NbLoaderPool.remove(loader, NbLoaderPool.getNbLoaderPool());
    }

    public void testNewResolverShallInfluenceExistingDataObjects() throws Exception {
        DataObject old = DataObject.find(fo);
        if (old.getLoader() == loader) {
            fail("The should be taken be default loader: " + old);
        }
        if (old.getClass() == MultiDataObject.class) {
            fail("The should be taken be default loader: " + old);
        }
        
        err.log("starting to create the resolver");
        FileObject res = FileUtil.createData(
            FileUtil.getConfigRoot(),
            "Services/MIMEResolver/Lenkaresolver.xml"
        );
        err.log("file created: " + res);
        org.openide.filesystems.FileLock l = res.lock();
        OutputStream os = res.getOutputStream(l);
        err.log("stream opened");
        PrintStream ps = new PrintStream(os);
        
        ps.println("<?xml version='1.0' encoding='UTF-8'?>");
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println("    <file>");
        ps.println("        <ext name='lenka'/>");
        ps.println("        <resolver mime='hodna/lenka'/>");
        ps.println("    </file>");
        ps.println("</MIME-resolver>");

        err.log("Content written");
        os.close();
        err.log("Stream closed");
        l.releaseLock();
        err.log("releaseLock");
        
        err.log("Let's query the resolvers");
        
        err.log("Waiting till finished");
        NbLoaderPool.waitFinished();
        err.log("Waiting done, querying the data object");
        
        err.log("Clear the mime type cache in org.openide.filesystems.MIMESupport: " + fo.getFileSystem().getRoot().getMIMEType());
        
        DataObject now = DataObject.find(fo);
        
        err.log("Object is here: " + now);
        assertEquals("Loader updated to lenka (mimetype: " + fo.getMIMEType() + ")", loader, now.getLoader());
        
        {
            DataObject xml = DataObject.find(res).copy(now.getFolder());
            
            Reference<Object> ref = new WeakReference<Object>(xml);
            xml = null;
            assertGC("And the copied XML object can disapper", ref);
            
        }
        
        {
            Reference<Object> ref = new WeakReference<Object>(now);
            now = null;
            assertGC("And the object can disapper", ref);
        }
    }

    public void stateChanged(ChangeEvent e) {
        poolChange++;
    }
    
    public static final class Lenka extends UniFileLoader {
        public Lenka() {
            super(MultiDataObject.class.getName());
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }

        @Override
        protected void initialize() {
            getExtensions().addMimeType("hodna/lenka");
            super.initialize();
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            err.log("findPrimaryFile: " + fo + " with mime: " + fo.getMIMEType());
            FileObject retValue;
            retValue = super.findPrimaryFile(fo);
            err.log("findPrimaryFile result: " + retValue);
            return retValue;
        }
        
    }
}
