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
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.cli.CLIHandler;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public final class EngineUtils {

    /**
     * Cache installer at NBI`s home directory.
     */
    public static File cacheEngine(Progress progress) throws IOException {
        File cachedEngine = getEngineLocation();

        if (!FileUtils.exists(cachedEngine)) {
            cacheEngine(cachedEngine, progress);
        }

        return cachedEngine;
    }
    public static Class getEngineMainClass() {
        return Installer.class;
    }

    public static void cacheEngine(File dest, Progress progress) throws IOException {
        LogManager.logIndent("cache engine data locally to run uninstall in the future");

        String filePrefix = "file:";
        String httpPrefix = "http://";
        String jarSep = "!/";

        String installerResource = ResourceUtils.getResourceClassName(getEngineMainClass());
        URL url = getEngineMainClass().getClassLoader().getResource(installerResource);
        if (url == null) {
            throw new IOException("No main Installer class in the engine");
        }

        LogManager.log(ErrorLevel.DEBUG, "NBI Engine URL for Installer.Class = " + url);
        LogManager.log(ErrorLevel.DEBUG, "URL Path = " + url.getPath());

        boolean needCache = true;
                        
        if ("jar".equals(url.getProtocol())) {
            LogManager.log("... running engine as a .jar file");
            // we run engine from jar, not from .class
            String path = url.getPath();
            String jarLocation;

            if (path.startsWith(filePrefix)) {
                LogManager.log("... classloader says that jar file is on the disk");
                if (path.indexOf(jarSep) != -1) {
                    jarLocation = path.substring(filePrefix.length(),
                            path.indexOf(jarSep + installerResource));
                    jarLocation = URLDecoder.decode(jarLocation, StringUtils.ENCODING_UTF8);
                    File jarfile = new File(jarLocation);
                    LogManager.log("... checking if it runs from cached engine");
                    if (jarfile.getAbsolutePath().equals(
                            dest.getAbsolutePath())) {
                        needCache = false; // we already run cached version
                    }
                    LogManager.log("... " + !needCache);
                } else {
                    throw new IOException("JAR path " + path +
                            " doesn`t contaion jar-separator " + jarSep);
                }
            } else if (path.startsWith(httpPrefix)) {
                LogManager.log("... classloader says that jar file is on remote server");
            }
        } else {
            // a quick hack to allow caching engine when run from the IDE (i.e.
            // as a .class) - probably to be removed later. Or maybe not...
            LogManager.log("... running engine as a .class file");
        }

        if (needCache) {
            cacheEngineJar(dest, progress);
        }

        LogManager.logUnindent("... finished caching engine data");
    }


    public static void checkEngine() {
        Class mainClass = getEngineMainClass();
        String thisClassResource = ResourceUtils.getResourceClassName(mainClass);
        InputStream is = mainClass.getClassLoader().getResourceAsStream(thisClassResource);
        ClassLoader cl = mainClass.getClassLoader();
        boolean exclamationMarkInURL = false;
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = (URLClassLoader) cl;
            for (URL url : ucl.getURLs()) {
                exclamationMarkInURL = exclamationMarkInURL || url.getPath().contains("!");
            }
        }
        if (is == null) {
            if (exclamationMarkInURL) {
                String message;
                if (SystemUtils.getDefaultUserdirRoot().toString().contains("!")) {
                    //Issue #156011
                    //Note: don`t use ResourceUtils for getting string here.
                    message =
                            "Looks like the name of current user profile directory contains an exclamation mark (!):\n" +
                            SystemUtils.getDefaultUserdirRoot() + "\n\n" +
                            "It is not recommended to use such profile names due to JDK bugs:\n" +
                            "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4730642" + "\n" +
                            "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4523159" + "\n" + "\n" +
                            "That bugs affects the installer work as well.\n" +
                            "The workaround is to run installer with different temporary and working installer directory.\n" +
                            "For example, try run installer with the following arguments:\n" +
                            "--tempdir " + new File(System.getenv("SystemDrive"), "Temp") + " " +
                            "--userdir " + new File(System.getenv("SystemDrive"), "NBI") + "\n";

                } else {
                    // some generic message
                    message =
                            "Cannot load the main class " + mainClass.getName() + " from the jar file due to JDK bugs:\n" +
                            "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4730642" + "\n" +
                            "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4523159" + "\n" + "\n" +
                            "Use other directory for the jar file.\n";
                }

                ErrorManager.notifyCritical(message);
            }
        } else {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    private static File getEngineLocation() {
        final String propName = EngineResources.LOCAL_ENGINE_PATH_PROPERTY;

        if (System.getProperty(propName) == null) {
            File cachedEngine = new File(Installer.getInstance().getLocalDirectory(), 
                    DEFAULT_ENGINE_JAR_NAME);
            System.setProperty(propName, cachedEngine.getAbsolutePath());
        } 
        return new File(System.getProperty(propName));       
    }
    
    private static void cacheEngineJar(File dest, Progress progress) throws IOException {
        LogManager.log("... starting copying engine content to the new jar file");
        String[] entries = StringUtils.splitByLines(
                StreamUtils.readStream(
                ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST)));

        JarOutputStream jos = null;
        Set <String> jarEntries = new HashSet <String> ();

        try {
            Manifest mf = new Manifest();
            mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, getEngineMainClass().getName());
            mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, StringUtils.EMPTY_STRING);

            dest.getParentFile().mkdirs();
            jos = new JarOutputStream(new FileOutputStream(dest), mf);
            LogManager.log("... total entries : " + entries.length);
            for (int i = 0; i < entries.length; i++) {
                progress.setPercentage((i * 100) / entries.length);
                String name = entries[i];
                if (name.length() > 0) {
                    String dataDir = EngineResources.DATA_DIRECTORY +
                            StringUtils.FORWARD_SLASH;
                    if (!name.startsWith(dataDir) || // all except "data/""
                            name.equals(dataDir) || // "data/"
                            name.matches(EngineResources.ENGINE_PROPERTIES_PATTERN) || // engine properties
                            name.equals(CLIHandler.OPTIONS_LIST)) { // additional CLI commands list
                        addJarEntry(jos, name, jarEntries);
                        if (!name.endsWith(StringUtils.FORWARD_SLASH)) {
                            StreamUtils.transferData(ResourceUtils.getResource(name), jos);
                        }
                    }
                }
            }
            LogManager.log("... adding content list and some other stuff");
            addJarEntry(jos, EngineResources.DATA_DIRECTORY + StringUtils.FORWARD_SLASH +
                    Registry.DEFAULT_BUNDLED_REGISTRY_FILE_NAME, jarEntries);

            XMLUtils.saveXMLDocument(
                    Registry.getInstance().getEmptyRegistryDocument(),
                    jos);

            addJarEntry(jos, EngineResources.ENGINE_CONTENTS_LIST, jarEntries);
            jos.write(StringUtils.asString(entries, SystemUtils.getLineSeparator()).getBytes());
        } catch (XMLException e) {
            throw new IOException(e);
        } finally {
            if (jos != null) {
                try {
                    jos.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }

            }
        }

        LogManager.log("Installer Engine has been cached to " + dest);
    }

    private static void addJarEntry(JarOutputStream jos, String name, Set <String> entries) throws IOException {
        String parent;
        int index;
        if(!name.endsWith(StringUtils.FORWARD_SLASH)) {
            //file entry
            index = name.lastIndexOf(StringUtils.FORWARD_SLASH);            
        } else {
            index = name.substring(0, name.length() - 1).lastIndexOf(StringUtils.FORWARD_SLASH);
        }
        if(index != -1) {
            parent = name.substring(0, index + 1);
            addJarEntry(jos, parent, entries);
        }
        if(entries.add(name)) {
            jos.putNextEntry(new JarEntry(name));            
        }
    }
    
    public static final String DEFAULT_ENGINE_JAR_NAME = "nbi-engine.jar";//NOI18N    

}
