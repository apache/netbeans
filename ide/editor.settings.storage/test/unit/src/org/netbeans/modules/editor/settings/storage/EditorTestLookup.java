/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.settings.storage;


import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import org.junit.Assert;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.DefaultAttributes;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.StatusDecorator;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 * Inspired by org.netbeans.api.project.TestUtil and FolderLookupTest
 *
 * @author Martin Roskanin
 */
public class EditorTestLookup extends ProxyLookup {
    
    public static EditorTestLookup DEFAULT_LOOKUP = null;
    
    static {
        EditorTestLookup.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", EditorTestLookup.class.getName());
        Assert.assertEquals(EditorTestLookup.class, Lookup.getDefault().getClass());
    }
    
    public EditorTestLookup() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    public static void setLookup(Object[] instances, ClassLoader cl, FileObject servicesFolder, Class [] exclude) {
        Lookup metaInfServices = Lookups.metaInfServices(cl);
        if (exclude != null && exclude.length > 0) {
            metaInfServices = Lookups.exclude(metaInfServices, exclude);
        }
        
        DEFAULT_LOOKUP.setLookups(new Executor() {
                @Override
                public void execute(Runnable command) {
                    // Ignore the first set of events
                }
            }, new Lookup[] {
            Lookups.fixed(instances),
            metaInfServices,
            Lookups.singleton(cl),
        });
        
        if (servicesFolder != null) {
            // DataSystems need default repository, which is read from the default lookup.
            // That's why the lookup is set first without the services lookup and then again
            // here with the FolderLookup over the Services folder.
//            Lookup services = new FolderLookup(DataFolder.findFolder(servicesFolder)).getLookup();
            Lookup services = Lookups.forPath(servicesFolder.getPath());
            if (exclude != null && exclude.length > 0) {
                services = Lookups.exclude(services, exclude);
            }
            
            DEFAULT_LOOKUP.setLookups(new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        // Ignore the first set of events
                    }
                }, new Lookup[] {
                Lookups.fixed(instances),
                metaInfServices,
                Lookups.singleton(cl),
                services
            });
        }
    }
    
    public static void setLookup(String[] files, File workDir, Object[] instances, ClassLoader cl)
    throws IOException, PropertyVetoException {
        setLookup(files, workDir, instances, cl, null);
    }
    
    public static void setLookup(String[] files, File workDir, Object[] instances, ClassLoader cl, Class [] exclude)
    throws IOException, PropertyVetoException {
        FileSystem fs = createLocalFileSystem(workDir, files);
        setLookup(new FileSystem [] { fs }, instances, cl, exclude);
    }
    
    public static void setLookup(URL[] layers, File workDir, Object[] instances, ClassLoader cl)
    throws IOException, PropertyVetoException {
        setLookup(layers, workDir, instances, cl, null);
    }
    
    public static void setLookup(URL[] layers, File workDir, Object[] instances, ClassLoader cl, Class [] exclude)
    throws IOException, PropertyVetoException {
        ArrayList<FileSystem> fs = new ArrayList<FileSystem>();
        fs.add(createLocalFileSystem(workDir, new String[0]));

        ArrayList<URL> xmlLayers = new ArrayList<URL>();
        for(URL layer : layers) {
            if (layer.getPath().endsWith(".xml")) {
                xmlLayers.add(layer);
            } else if (layer.getPath().endsWith(".zip")) {
                if (!xmlLayers.isEmpty()) {
                    XMLFileSystem layersFs = new XMLFileSystem();
                    layersFs.setXmlUrls(xmlLayers.toArray(new URL [0]));
                    fs.add(layersFs);
                    xmlLayers.clear();
                }
                
                fs.add(new ZipFileSystem(layer));
            } else {
                throw new IOException("Expecting .xml or .zip layers, but not '" + layer.getPath() + "'");
            }
        }

        //for MIMEResolver to work:
        for (URL url : NbCollections.iterable(EditorTestLookup.class.getClassLoader().getResources("META-INF/generated-layer.xml"))) {
            xmlLayers.add(url);
        }
        
        if (!xmlLayers.isEmpty()) {
            XMLFileSystem layersFs = new XMLFileSystem();
            layersFs.setXmlUrls(xmlLayers.toArray(new URL [0]));
            fs.add(layersFs);
        }
        
        setLookup(fs.toArray(new FileSystem [0]), instances, cl, exclude);
    }
    
    @SuppressWarnings("deprecation")
    private static void setLookup(FileSystem [] fs, Object[] instances, ClassLoader cl, Class [] exclude)
    throws IOException, PropertyVetoException {

        // Remember the tests run in the same VM and repository is singleton.
        // Once it is created for the first time it will stick around forever.
        Repository repository = (Repository) Lookup.getDefault().lookup(Repository.class);
        if (repository == null) {
            repository = new Repository(new SystemFileSystem(fs));
        } else {
            ((SystemFileSystem) repository.getDefaultFileSystem()).setOrig(fs);
        }
        
        Object[] lookupContent = new Object[instances.length + 1];
        lookupContent[0] = repository;
        System.arraycopy(instances, 0, lookupContent, 1, instances.length);

        // Create the Services folder (if needed}
        FileObject services = repository.getDefaultFileSystem().findResource("Services");
        if (services == null) {
            services = repository.getDefaultFileSystem().getRoot().createFolder("Services");
        }
        
        EditorTestLookup.setLookup(lookupContent, cl, services, exclude);
    }

    private static FileSystem createLocalFileSystem(File mountPoint, String[] resources) throws IOException {
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {
            createFileOnPath(mountPoint, resources[i]);
        }
        
        LocalFileSystem lfs = new LocalFileSystem();
        try {
            lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    private static void createFileOnPath(File mountPoint, String path) throws IOException{
        mountPoint.mkdir();
        
        File f = new File (mountPoint, path);
        if (f.isDirectory() || path.endsWith("/")) {
            f.mkdirs();
        }
        else {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException iex) {
                throw new IOException ("While creating " + path + " in " + mountPoint.getAbsolutePath() + ": " + iex.toString() + ": " + f.getAbsolutePath());
            }
        }
    }
    
    private static class SystemFileSystem extends MultiFileSystem implements StatusDecorator {
        public SystemFileSystem(FileSystem [] orig) {
            super(orig);
        }
        
        public void setOrig(FileSystem [] orig) {
            StorageImpl.ignoreFilesystemEvents(true);
            try {
                setDelegates(orig);
            } finally {
                StorageImpl.ignoreFilesystemEvents(false);
            }
        }

        public @Override StatusDecorator getDecorator() {
            return this;
        }
        
        public String annotateNameHtml (String name, java.util.Set files) {
            return null;
        }
        
        public String annotateName (String name, java.util.Set files) {
            for(Object o : files) {
                FileObject fo = (FileObject) o;
                String bundleName = (String)fo.getAttribute ("SystemFileSystem.localizingBundle"); // NOI18N
                if (bundleName != null) {
                    bundleName = org.openide.util.Utilities.translate(bundleName);
//                    System.out.println("~~~ looking up annotateName for '" + fo.getPath() + "', localizingBundle='" + bundleName + "'");
                    
                    try {
                        ResourceBundle b = NbBundle.getBundle(bundleName);
                        return b.getString (fo.getPath());
                    } catch (MissingResourceException ex) {
                        // ignore--normal

//                        System.out.println("~~~ No annotateName for '" + fo.getPath() + "', localizingBundle='" + bundleName + "'");
//                        ex.printStackTrace();
//
//                        ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);
//                        if (c == null) {
//                            c = ClassLoader.getSystemClassLoader();
//                        }
//                        try {
//                            String s = bundleName.replace('.', '/') + ".properties";
//                            URL r = c.getResource(s);
//                            System.out.println("~~~ '" + s + "' -> " + r);
//
//                            Enumeration<URL> e = c.getResources(s);
//                            while (e.hasMoreElements()) {
//                                URL url = e.nextElement();
//                                System.out.println("  -> " + url);
//                            }
//                        } catch (IOException ioe) {
//                            ioe.printStackTrace();
//                        }
                    }
                }
            }

            return name;
        }
    } // End of SystemFileSystem class
    
    private static final class ZipFileSystem extends AbstractFileSystem {

        private final String zipPath;
        
        public ZipFileSystem(URL zipURL) throws IOException {
            this.zipPath = zipURL.toString();
            
            File zipFile = Files.createTempFile("ZipFileSystem", ".zip").toFile();
            zipFile.deleteOnExit();
            
            OutputStream os = new FileOutputStream(zipFile);
            try {
                InputStream is = zipURL.openStream();
                try {
                    byte [] buffer = new byte [1024];
                    int size;
                    while(0 < (size = is.read(buffer, 0, buffer.length))) {
                        os.write(buffer, 0, size);
                    }
                } finally {
                    is.close();
                }
            } finally {
                os.close();
            }
            
            JarFileSystem jfs = new JarFileSystem();
            try {
                jfs.setJarFile(zipFile);
            } catch (PropertyVetoException pve) {
                IOException ioe = new IOException();
                ioe.initCause(pve);
                throw ioe;
            }
            
            JarFileSystem.Impl jfsImpl = new JarFileSystem.Impl(jfs);
            DefaultAttributes attribs = new DefaultAttributes(jfsImpl, jfsImpl, jfsImpl);
            
            this.info = jfsImpl;
            this.change = jfsImpl;
            this.list = attribs;
            this.attr = attribs;
        }
        
        public String getDisplayName() {
            return "ZipFileSystem[" + zipPath;
        }

        public boolean isReadOnly() {
            return true;
        }
    } // End of ZipFileSystem class
}
