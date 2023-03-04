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

package org.netbeans.installer.infra.server.ejb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.ejb.Stateless;
import org.netbeans.installer.Installer;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.Progress;
import org.w3c.dom.Document;

/**
 *
 * @author Kirill Sorokin
 */
@Stateless
public class ManagerBean implements Manager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static ReentrantLock bundlesLock = new ReentrantLock();
    
    private static Map<String, File> registries = new HashMap<String, File>();
    private static Map<String, File> bundles    = new HashMap<String, File>();
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ManagerBean() {
        try {
            ROOT.mkdirs();
            
            TEMP.mkdirs();
            REGISTRIES.mkdirs();
            UPLOADS.mkdirs();
            BUNDLES.mkdirs();
            NBI.mkdirs();
            
            if (!REGISTRIES_LIST.exists()) {
                REGISTRIES_LIST.createNewFile();
            }
            
            loadRegistriesList();

            //Issue #183611
            //Locale.setDefault(new Locale("en", "US"));
            
            DownloadManager.getInstance().setLocalDirectory(NBI);
            DownloadManager.getInstance().setFinishHandler(new DummyFinishHandler());
            
            System.setProperty(
                    Installer.LOCAL_DIRECTORY_PATH_PROPERTY, NBI.getAbsolutePath());
            System.setProperty(
                    Installer.IGNORE_LOCK_FILE_PROPERTY, "true");
            System.setProperty(
                    LogManager.LOG_TO_CONSOLE_PROPERTY, "false");
            System.setProperty(
                    Registry.LAZY_LOAD_ICONS_PROPERTY, "true");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ManagerException e) {
            e.printStackTrace();
        }
    }
    
    // registry operations //////////////////////////////////////////////////////////
    public void addRegistry(String registryName) throws ManagerException {
        if (registries.get(registryName) == null) {
            registries.put(registryName, initializeRegistry(registryName));
        }
        
        saveRegistriesList();
    }
    
    public void removeRegistry(String registryName) throws ManagerException {
        try {
            if (registries.get(registryName) != null) {
                FileUtils.deleteFile(registries.get(registryName), true);
                registries.remove(registryName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
        
        saveRegistriesList();
    }
    
    public String getRegistry(String registryName) throws ManagerException {
        if (registries.get(registryName) == null) {
            addRegistry(registryName);
        }
        
        final File registryDir = registries.get(registryName);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        try {
            return FileUtils.readFile(registryXml);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    public List<String> getRegistries() throws ManagerException {
        return new ArrayList<String>(registries.keySet());
    }
    
    // engine operations ////////////////////////////////////////////////////////////
    public File getEngine() throws ManagerException {
        return ENGINE;
    }
    
    public void updateEngine(File engine) throws ManagerException {
        deleteBundles();
        
        ENGINE.delete();
        
        try {
            FileUtils.moveFile(engine, ENGINE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    // component operations /////////////////////////////////////////////////////////
    public void addPackage(String registryName, File archive, String parentUid, String parentVersion, String parentPlatforms, String uriPrefix) throws ManagerException {
        deleteBundles();
        
        if (registries.get(registryName) == null) {
            addRegistry(registryName);
        }
        
        try {
            final File localRegistryDir =
                    registries.get(registryName);
            final File localRegistryXml =
                    new File(localRegistryDir, REGISTRY_XML);
            final File packageRegistryDir =
                    FileUtils.createTempFile(TEMP, false);
            final File packageRegistryXml =
                    new File(packageRegistryDir, "registry.xml");
            
            FileUtils.unjar(archive, packageRegistryDir);
            FileUtils.modifyFile(packageRegistryXml,
                    "(\\>)resource:(.*?\\<\\/)",
                    "$1" + uriPrefix.replace("&", "&amp;") + "$2", true, StringUtils.ENCODING_UTF8);
            
            final Registry localRegistry = new Registry();
            localRegistry.setLocalDirectory(NBI);
            localRegistry.setFinishHandler(new DummyFinishHandler());
            localRegistry.loadProductRegistry(localRegistryXml);
            
            final Registry packageRegistry = new Registry();
            packageRegistry.setLocalDirectory(NBI);
            packageRegistry.setFinishHandler(new DummyFinishHandler());
            packageRegistry.loadProductRegistry(packageRegistryXml);
            
            final Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
            
            for (Product product: packageRegistry.getProducts()) {
                final List<Product> existingProducts = localRegistry.getProducts(
                        product.getUid(),
                        product.getVersion(),
                        product.getPlatforms());
                
                if (existingProducts.size() > 0) {
                    for (Product existingProduct: existingProducts) {
                        nodes.offer(existingProduct);
                    }
                }
            }
            
            for (Group group: packageRegistry.getGroups()) {
                if (!group.equals(packageRegistry.getRegistryRoot())) {
                    final Group existingGroup = localRegistry.getGroup(
                            group.getUid());
                    
                    if (existingGroup != null) {
                        nodes.offer(existingGroup);
                    }
                }
            }
            
            if (nodes.size() > 0) {
                while (nodes.peek() != null) {
                    final RegistryNode node = nodes.poll();
                    
                    node.getParent().removeChild(node);
                    
                    if (node instanceof Product) {
                        final Product temp = (Product) node;
                        final String path = PRODUCTS + "/" +
                                temp.getUid() + "/" +
                                temp.getVersion() + "/" +
                                StringUtils.asString(temp.getPlatforms(), " ");
                        
                        FileUtils.deleteFile(new File(localRegistryDir, path), true);
                    }
                    
                    if (node instanceof Group) {
                        final Group temp = (Group) node;
                        final String path = GROUPS + "/" +
                                temp.getUid();
                        
                        FileUtils.deleteFile(new File(localRegistryDir, path), true);
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            FileUtils.copyFile(
                    new File(packageRegistryDir, PRODUCTS),
                    new File(localRegistryDir, PRODUCTS),
                    true);
            FileUtils.copyFile(
                    new File(packageRegistryDir, GROUPS),
                    new File(localRegistryDir, GROUPS),
                    true);
            
            RegistryNode parent;
            
            List<Product> parents = null;
            if ((parentVersion != null) &&
                    !parentVersion.equals("null") &&
                    (parentPlatforms != null) &&
                    !parentPlatforms.equals("null")) {
                parents = localRegistry.getProducts(
                        parentUid,
                        Version.getVersion(parentVersion),
                        StringUtils.parsePlatforms(parentPlatforms));
            }
            if ((parents == null) || (parents.size() == 0)) {
                parent = localRegistry.getGroup(parentUid);
                if (parent == null) {
                    parent = localRegistry.getRegistryRoot();
                }
            } else {
                parent = parents.get(0);
            }
            
            parent.attachRegistry(packageRegistry);
            
            localRegistry.saveProductRegistry(
                    localRegistryXml,
                    TrueFilter.INSTANCE,
                    true,
                    true,
                    true);
            
            FileUtils.deleteFile(archive);
            FileUtils.deleteFile(packageRegistryDir, true);
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (XMLException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    public void removeProduct(String registryName, String uid, String version, String platforms) throws ManagerException {
        deleteBundles();
        
        if (registries.get(registryName) == null) {
            addRegistry(registryName);
        }
        
        final File registryDir   = registries.get(registryName);
        final File productsDir = new File(registryDir, PRODUCTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml   = new File(registryDir, REGISTRY_XML);
        
        try {
            final Registry registry = new Registry();
            
            registry.setLocalDirectory(NBI);
            registry.setFinishHandler(new DummyFinishHandler());
            registry.loadProductRegistry(registryXml);
            
            final List<Product> existing = registry.getProducts(
                    uid,
                    Version.getVersion(version),
                    StringUtils.parsePlatforms(platforms));
            
            if (existing != null) {
                existing.get(0).getParent().removeChild(existing.get(0));
                
                Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                nodes.offer(existing.get(0));
                
                while(nodes.peek() != null) {
                    RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        Product temp = (Product) node;
                        FileUtils.deleteFile(new File(productsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof Group) {
                        Group temp = (Group) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(registryXml, TrueFilter.INSTANCE, true, true, true);
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ManagerException("Could not update component", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    public void removeGroup(String registryName, String uid) throws ManagerException {
        deleteBundles();
        
        if (registries.get(registryName) == null) {
            addRegistry(registryName);
        }
        
        final File registryDir = registries.get(registryName);
        final File productsDir = new File(registryDir, PRODUCTS);
        final File groupsDir     = new File(registryDir, GROUPS);
        final File registryXml = new File(registryDir, REGISTRY_XML);
        
        try {
            final Registry registry = new Registry();
            
            registry.setLocalDirectory(NBI);
            registry.setFinishHandler(new DummyFinishHandler());
            registry.loadProductRegistry(registryXml);
            
            final Group existing = registry.getGroup(uid);
            
            if (existing != null) {
                existing.getParent().removeChild(existing);
                
                Queue<RegistryNode> nodes = new LinkedList<RegistryNode>();
                nodes.offer(existing);
                
                while(nodes.peek() != null) {
                    RegistryNode node = nodes.poll();
                    
                    if (node instanceof Product) {
                        Product temp = (Product) node;
                        FileUtils.deleteFile(new File(productsDir, temp.getUid() + "/" + temp.getVersion()));
                    }
                    
                    if (node instanceof Group) {
                        Group temp = (Group) node;
                        FileUtils.deleteFile(new File(groupsDir, temp.getUid()));
                    }
                    
                    for (RegistryNode child: node.getChildren()) {
                        nodes.offer(child);
                    }
                }
            }
            
            registry.saveProductRegistry(registryXml, TrueFilter.INSTANCE, true, true, true);
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (FinalizationException e) {
            e.printStackTrace();
            throw new ManagerException("Could not remove component", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public File exportRegistries(String[] registryNames, String codebase) throws ManagerException {
        try {
            final File userDir = FileUtils.createTempFile(TEMP, false);
            final File registryFile = FileUtils.createTempFile(TEMP, false);
            
            final File target = new File(
                    EXPORTED,
                    StringUtils.asString(registryNames, ", ") + ".jar");
            
            FileUtils.mkdirs(target.getParentFile());
            
            final Registry registry = new Registry();
            
            registry.setLocalDirectory(userDir);
            registry.setFinishHandler(new DummyFinishHandler());
            
            for (String registryName: registryNames) {
                registry.loadProductRegistry(
                        new File(registries.get(registryName), REGISTRY_XML));
            }
            
            registry.saveProductRegistry(
                    registryFile,
                    TrueFilter.INSTANCE,
                    false,
                    true,
                    true);
            
            final JarOutputStream out =
                    new JarOutputStream(new FileOutputStream(target));
            
            for (String registryName: registryNames) {
                final List<File> excludes = new LinkedList<File>();
                
                excludes.add(new File(registries.get(registryName), REGISTRY_XML));
                
                FileUtils.zip(
                        registries.get(registryName),
                        out,
                        registries.get(registryName).getParentFile(),
                        excludes);
                
            }
            
            FileUtils.modifyFile(
                    registryFile,
                    ">.*?registry=(.+?)&amp;file=",
                    ">" + codebase + "/$1/",
                    true, StringUtils.ENCODING_UTF8);
            FileUtils.modifyFile(
                    registryFile,
                    "%2F",
                    "/", StringUtils.ENCODING_UTF8);
            FileUtils.modifyFile(
                    registryFile,
                    "+",
                    "%20", StringUtils.ENCODING_UTF8);
            
            out.putNextEntry(new ZipEntry("registry.xml"));
            StreamUtils.transferFile(registryFile, out);
            
            out.putNextEntry(new ZipEntry("nbi-engine.jar"));
            StreamUtils.transferFile(getEngine(), out);
            
            out.putNextEntry(new ZipEntry("nbi.jnlp"));
            StreamUtils.writeChars(out, StringUtils.format(
                    JNLP_STUB,
                    codebase,
                    "nbi.jnlp",
                    codebase + "/nbi-engine.jar",
                    codebase + "/registry.xml"));
            
            FileUtils.deleteFile(userDir);
            FileUtils.deleteFile(registryFile);
            
            out.close();
            
            return target;
        } catch (IOException e) {
            throw new ManagerException("Cannot export", e);
        } catch (InitializationException e) {
            throw new ManagerException("Cannot export", e);
        } catch (FinalizationException e) {
            throw new ManagerException("Cannot export", e);
        }
    }
    
    public String getJnlp(String[] registryNames, String codebase) throws ManagerException {
        try {
            String jnlp = "install?true=true";
            for (String registryName: registryNames) {
                jnlp += "&registry=" + URLEncoder.encode(registryName, "UTF-8");
            }
            
            String engine = codebase + "/nbi-engine.jar";
            
            String registry = "";
            for (String registryName: registryNames) {
                registry +=
                        codebase +
                        "/get-registry?registry=" +
                        URLEncoder.encode(registryName, "UTF-8") +
                        "\n";
            }
            registry = registry.trim();
            
            return StringUtils.format(JNLP_STUB, codebase, jnlp, engine, registry);
        } catch (UnsupportedEncodingException e) {
            throw new ManagerException("Whoah..", e);
        }
    }
    
    public File getFile(String registryName, String file) throws ManagerException {
        if (registries.get(registryName) == null) {
            addRegistry(registryName);
        }
        
        final File registryDir = registries.get(registryName);
        
        return new File(registryDir, file);
    }
    
    public Registry loadRegistry(String... registryNames) throws ManagerException {
        if (registryNames.length > 0) {
            List<File> files = new LinkedList<File>();
            
            for (String name: registryNames) {
                if (registries.get(name) == null) {
                    addRegistry(name);
                }
                
                files.add(new File(registries.get(name), REGISTRY_XML));
            }
            
            try {
                final Registry registry = new Registry();
                
                registry.setLocalDirectory(NBI);
                registry.setFinishHandler(new DummyFinishHandler());
                for (File file: files) {
                    registry.loadProductRegistry(file);
                }
                
                return registry;
            } catch (InitializationException e) {
                e.printStackTrace();
                throw new ManagerException("Could not load registry", e);
            }
        }
        
        return null;
    }
    
    public List<Product> getProducts(String... registryNames) throws ManagerException {
        List<Product> components = new LinkedList<Product>();
        
        if (registryNames.length > 0) {
            final List<File> files = new LinkedList<File>();
            for (String registryName: registryNames) {
                if (registries.get(registryName) == null) {
                    addRegistry(registryName);
                }
                
                files.add(new File(registries.get(registryName), REGISTRY_XML));
            }
            
            try {
                final Registry registry = new Registry();
                registry.setLocalDirectory(NBI);
                registry.setFinishHandler(new DummyFinishHandler());
                
                for (File file: files) {
                    registry.loadProductRegistry(file);
                }
                
                components.addAll(registry.getProducts());
            } catch (InitializationException e) {
                e.printStackTrace();
                throw new ManagerException("Could not load registry", e);
            }
        }
        
        return components;
    }
    
    public File createBundle(Platform platform, String[] registryNames, String[] components) throws ManagerException {
        bundlesLock.lock();
        try {
            final String key =
                    StringUtils.asString(registryNames) +
                    StringUtils.asString(components) +
                    platform;
            
            if ((bundles.get(key) != null) && bundles.get(key).exists()) {
                return bundles.get(key);
            }
            
            if (bundles.get(key) != null) {
                bundles.remove(key);
            }
            
            try {
                File statefile = FileUtils.createTempFile(TEMP, false);
                File userDir = FileUtils.createTempFile(TEMP, false);
                File bundle = new File(
                        FileUtils.createTempFile(BUNDLES, false), 
                        "bundle.jar");
                
                File javaHome  = new File(System.getProperty("java.home"));
                
                String     remote = "";
                List<File> files  = new LinkedList<File>();
                for (String name: registryNames) {
                    if (registries.get(name) == null) {
                        addRegistry(name);
                    }
                    File xml = new File(registries.get(name), REGISTRY_XML);
                    
                    files.add(xml);
                    remote += xml.toURI().toString() + "\n";
                }
                remote = remote.trim();
                
                final Registry registry = new Registry();
                
                registry.setLocalDirectory(NBI);
                registry.setFinishHandler(new DummyFinishHandler());
                registry.setTargetPlatform(platform);
                for (File file: files) {
                    registry.loadProductRegistry(file);
                }
                
                for (String string: components) {
                    String[] parts = string.split(",");
                    
                    registry.getProduct(
                            parts[0],
                            Version.getVersion(parts[1])).setStatus(Status.INSTALLED);
                }
                registry.saveStateFile(statefile, new Progress());
                
                bundle.getParentFile().mkdirs();
                
                ExecutionResults results = SystemUtils.executeCommand(
                        JavaUtils.getExecutable(javaHome).getAbsolutePath(),
                        "-Dnbi.product.remote.registries=" + remote,
                        "-jar",
                        ENGINE.getAbsolutePath(),
                        "--silent",
                        "--state",
                        statefile.getAbsolutePath(),
                        "--create-bundle",
                        bundle.getAbsolutePath(),
                        "--ignore-lock",
                        "--platform",
                        platform.toString(),
                        "--userdir",
                        userDir.getAbsolutePath());
                
                System.out.println(results.getErrorCode());
                System.out.println(results.getStdOut());
                System.out.println(results.getStdErr());
                
                if (results.getErrorCode() != 0) {
                    throw new ManagerException("Could not create bundle - error in running the engine");
                }
                
                FileUtils.deleteFile(statefile);
                FileUtils.deleteFile(userDir, true);
                
                if (platform == Platform.WINDOWS) {
                    bundle = new File(
                            bundle.getAbsolutePath().replaceFirst("\\.jar$", ".exe"));
                } else if (platform.isCompatibleWith(Platform.MACOSX)) {
                    bundle = new File(
                            bundle.getAbsolutePath().replaceFirst("\\.jar$", ".zip"));
                } else {
                    bundle = new File(
                            bundle.getAbsolutePath().replaceFirst("\\.jar$", ".sh"));                
                }
                
                bundles.put(key, bundle);
                
                return bundle;
            } catch (InitializationException e) {
                e.printStackTrace();
                throw new ManagerException("Could not load registry", e);
            } catch (FinalizationException e) {
                e.printStackTrace();
                throw new ManagerException("Could not load registry", e);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ManagerException("Could not load registry", e);
            }
        } finally {
            bundlesLock.unlock();
        }
    }
    
    public void generateBundles(String[] registryNames) throws ManagerException {
        try {
            final List<File> files = new ArrayList<File>();
            for (String name: registryNames) {
                if (registries.get(name) == null) {
                    addRegistry(name);
                }
                
                files.add(new File(registries.get(name), REGISTRY_XML));
            }
            
            for (Platform platform: Platform.values()) {
                final Registry registry = new Registry();
                
                registry.setLocalDirectory(NBI);
                registry.setFinishHandler(new DummyFinishHandler());
                for (File file: files) {
                    registry.loadProductRegistry(file);
                }
                
                final List<Product> products = registry.getProducts(platform);
                
                for (int i = 1; i <= products.size(); i++) {
                    Product[] combination = new Product[i];
                    
                    iterate(platform, registryNames, registry, combination, 0, products, 0);
                }
            }
        } catch (InitializationException e) {
            throw new ManagerException("Cannot generate bundles", e);
        }
    }
    
    public void deleteBundles() throws ManagerException {
        bundlesLock.lock();
        try {
            for (File file: bundles.values()) {
                FileUtils.deleteFile(file);
            }
            
            bundles.clear();
        } catch (IOException e) {
            throw new ManagerException("Cannot clear bundles", e);
        } finally {
            bundlesLock.unlock();
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void loadRegistriesList() throws ManagerException {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(REGISTRIES_LIST)));
            
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                registries.put(line, initializeRegistry(line));
            }
            
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    private void saveRegistriesList() throws ManagerException {
        try {
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(REGISTRIES_LIST)));
            
            try {
                registries.keySet().forEach(writer::println);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Could not load registry", e);
        }
    }
    
    private File initializeRegistry(String name) throws ManagerException {
        File directory   = new File(REGISTRIES, name);
        File registryxml = new File(directory, REGISTRY_XML);
        
        directory.mkdirs();
        
        if (!registryxml.exists()) {
            try {
                Document document = Registry.
                        getInstance().getEmptyRegistryDocument();
                XMLUtils.saveXMLDocument(document, registryxml);
            } catch (XMLException e) {
                e.printStackTrace();
                throw new ManagerException("Cannot initialize registry", e);
            }
        }
        
        return directory;
    }
    
    private void iterate(Platform platform, String[] registryNames, Registry registry, Product[] combination, int index, List<Product> products, int start) throws ManagerException {
        for (int i = start; i < products.size(); i++) {
            combination[index] = products.get(i);
            
            if (index == combination.length - 1) {
                for (Product product: products) {
                    product.setStatus(Status.NOT_INSTALLED);
                }
                for (Product product: combination) {
                    product.setStatus(Status.TO_BE_INSTALLED);
                }
                
                if (registry.getProductsToInstall().size() == combination.length) {
                    String[] components = new String[combination.length];
                    
                    for (int j = 0; j < combination.length; j++) {
                        components[j] = combination[j].getUid() + "," +
                                combination[j].getVersion().toString();
                    }
                    
                    createBundle(platform, registryNames, components);
                }
            } else {
                iterate(platform, registryNames, registry, combination, index + 1, products, i + 1);
            }
        }
    }
}
