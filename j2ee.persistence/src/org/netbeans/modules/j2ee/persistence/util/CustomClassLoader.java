/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;

/**
 * Custom classloader for Persistence plugin. It subclasses URLClassLoader
 * and extends the current classpath by including the given URLs.
 * TODO: have some security issues with jdbc drivers unloading, need to resolve
 * 
 * @author leon (leon@hts.dev.java.net)
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 * @author Sergey Perov
 */
public class CustomClassLoader extends URLClassLoader {

    private Map<String, File> package2File = new HashMap<String, File>();
    private static final Logger logger = Logger.getLogger(CustomClassLoader.class.getName());

    public CustomClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Initializing Custom Classloader with classpath : ");
            for(URL url : urls) {
                logger.info(url.toExternalForm());
            }
        }
    }

    @Override
    protected Class loadClass(String name, boolean b) throws ClassNotFoundException {
        if(name == null) {
            throw new IllegalArgumentException("class name cannot be null");
        }
        Class clazz = findLoadedClass(name);
        if (clazz != null) {
            return clazz;
        }
        String packageName = null;
        int lastDotIndex = name.lastIndexOf(".");
        if (lastDotIndex != -1) {
            packageName = name.substring(0, lastDotIndex);
        }
        if (packageName != null && (packageName.startsWith("java") ||
                packageName.startsWith("javax") ||
                packageName.startsWith("com.sun") ||
                packageName.startsWith("org.hibernate") ||
                packageName.startsWith("org.dom4j") ||
                packageName.startsWith("net.sf.cglib") ||
                packageName.startsWith("org.w3c") ||
                packageName.startsWith("antlr") ||
                packageName.startsWith("org.objectweb.asm") ||
                packageName.startsWith("org.apache.commons.collections") ||
                packageName.startsWith("net.sf.ehcache") ||
                packageName.startsWith("org.netbeans"))) {
                clazz = super.loadClass(name, b);
        }
        if (clazz != null) {
            return clazz;
        }
        if (packageName != null && packageName.startsWith("org.apache.log4j")) {
            // Throw CNFE because we use java.util.logging hander in
            // the logging output
            throw new ClassNotFoundException("Log4J is forbidden");
        }
        int dotIndex = name.indexOf(".");
        String fileName;
        String separator = File.separator;
        if (separator.equals("\\")) {
            separator = "\\\\";
        }
        if (dotIndex != -1) {
            fileName = name.replaceAll("\\.", separator) + ".class";
        } else {
            fileName = name + ".class";
        }
        Class loadedClass = null;
        InputStream is = getLocalResourceAsStream(packageName, fileName);
        if (is != null) {
            try {
                loadedClass = loadClass(name, is);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    // Ignore it
                }
            }
        }
        if (loadedClass != null) {
            return loadedClass;
        }
       // logger.info("loading " + name + " from super class loader " + Thread.currentThread().getContextClassLoader().getParent().getClass().getName());
        return super.loadClass(name, b);
    }

    private Class loadClass(String className, InputStream is) {
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024 * 5];
            int readBytes;
            while ((readBytes = bis.read(bytes)) != -1) {
                os.write(bytes, 0, readBytes);
            }
            byte[] b = os.toByteArray();
            return defineClass(className, b, 0, b.length);
        } catch (ClassFormatError ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    @Override
    protected PermissionCollection getPermissions(CodeSource codeSource) {
        Permissions perms = new Permissions();
        perms.add(new AllPermission());
        perms.setReadOnly();
        return perms;
    }

    private URL getLocalResource(String packageName, String name) {
        File preferred = null;
        if (packageName != null) {
            preferred = package2File.get(packageName);
        }
        List<File> files = new ArrayList<File>();
        if (preferred != null) {
            files.add(preferred);
        }

        for (File entry : files) {
            if (entry.isDirectory() && entry.exists()) {
                File f = new File(entry, name);
                if (f.exists()) {
                    try {
                        package2File.put(packageName, entry);
                        return f.toURL();
                    } catch (MalformedURLException ex) {
                        continue;
                    }
                }
            } else {
                if (entry.isFile() && entry.exists()) {
                    ZipFile zf = null;
                    try {
                        zf = new ZipFile(entry);
                        // Zip entries are delimited by /
                        name = name.replaceAll("\\\\", "/");
                        ZipEntry zipEntry = zf.getEntry(name);
                        if (zipEntry != null) {
                            // URL's can contain only /
                            String url = entry.getAbsolutePath().replaceAll("\\\\", "/");
                            if (!url.startsWith("/")) {
                                url = "/" + url;
                            }
                            URL r = new URL("jar:file://" + url + "!/" + name);
                            package2File.put(packageName, entry);
                            return r;
                        }
                    } catch (ZipException ex) {
                        // continue
                    } catch (IOException ex) {
                        // continue
                    } finally {
                        try {
                            if(zf!=null){
                                zf.close();
                            }
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }
        return null;
    }

    private InputStream getLocalResourceAsStream(String packageName, String name) {
        URL res = getLocalResource(packageName, name);
        if (res == null) {
            return null;
        }
        try {
            return res.openStream();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream is = getLocalResourceAsStream(null, name);
        return is != null ? is : super.getResourceAsStream(name);
    }
}
