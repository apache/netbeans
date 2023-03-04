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

// XXX rewrite to NbModuleSuite

package org.netbeans.core.validation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.netbeans.junit.NbTestCase;

/** Tests for resources contained in modules.
 *
 * @author radim
 */
public class ResourcesTest extends NbTestCase {

    // TODO the idea is to check for duplicates
    //      for patterns that are not effective in NB environment
    private Logger LOG;

    public ResourcesTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        super.setUp();
    }

    /** If package contains only one resource it is not too frendly to our classloaders.
     * Generally not a big problem OTOH we want to avoid this if this is simple.
     */
    public void testOneInPackage() throws Exception {
        SortedSet<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            // check JAR files only
            if (!f.getName().endsWith(".jar"))
                continue;
            
            // ignore branding
            if (f.getName().endsWith("_nb.jar"))
                continue;
            
            // a lot of alarms for 3rd party JARs
            if (f.getName().contains("modules/ext/"))
                continue;
            
            SortedMap<String, Integer> resourcesPerPackage = new TreeMap<String, Integer>();
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            int entryCount = 0;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                
                String name = entry.getName();
                String prefix = (name.lastIndexOf('/') >= 0)?
                    name.substring(0, name.lastIndexOf('/')): "";
                if (prefix.startsWith("META-INF")
                || prefix.startsWith("1.0/")
                || prefix.startsWith("com/")
                || prefix.startsWith("javax/")
                || prefix.startsWith("freemarker/")
                || prefix.startsWith("org/apache/tomcat")
                || prefix.startsWith("org/apache/lucene")
                || prefix.startsWith("org/w3c/")
//                || prefix.startsWith("")
                || prefix.startsWith("org/netbeans/modules/openide/actions")
                || prefix.startsWith("org/netbeans/modules/openide/awt")
                || prefix.startsWith("org/netbeans/modules/openide/windows")
                || prefix.startsWith("org/openide/explorer/propertysheet") // in deprecated core/settings
                || prefix.startsWith("org/openide/io")
                || prefix.startsWith("org/netbeans/api")
                || prefix.startsWith("org/netbeans/spi")
                || prefix.startsWith("org/netbeans/core/execution/beaninfo")
                || prefix.startsWith("org/netbeans/modules/web/monitor")
                || prefix.matches("org/netbeans/.*/[as]pi.*")
                        ) {
                    continue;
                }
                
                entryCount++;
                Integer count = resourcesPerPackage.get(prefix);
                if (count != null) {
                    resourcesPerPackage.put(prefix, count+1);
                }
                else {
                    resourcesPerPackage.put(prefix, 1);
                }
            }
            if (entryCount > 1) { // filter library wrappes (they have only Bundle.properties)
                for (Map.Entry<String, Integer> pkgInfo: resourcesPerPackage.entrySet()) {
                    if (pkgInfo.getValue().equals(1)) {
                        violations.add(new Violation(pkgInfo.getKey(), jar.getName(), " has package with just one resource"));
                    }
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some JARs in IDE contains sparsely populated packages ("+violations.size()+"):\n");
            for (Violation viol: violations) {
                msg.append(viol).append('\n');
            }
            fail(msg.toString());
        }
        //                    assertTrue (entry.toString()+" should have line number table", v.foundLineNumberTable());
    }
    
    /** Historically we had problems with some images.
     */
    public void testImageCanBeRead() throws Exception {
        ImageIO.setUseCache(false);
        ImageReader PNG_READER = ImageIO.getImageReadersByMIMEType("image/png").next();
        ImageReader GIF_READER = ImageIO.getImageReadersByMIMEType("image/gif").next();
        
        SortedSet<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            // check JAR files only
            if (!f.getName().endsWith(".jar"))
                continue;
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            BufferedImage img;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                
                String name = entry.getName();
                if (!name.endsWith(".gif")
                        && !name.endsWith(".png")) {
                    continue;
                }
                try {
                    img = ImageIO.read(jar.getInputStream(entry));
                }
                catch (IOException ioe) {
                    violations.add(new Violation(name, jar.getName(), " cannot be read"));
                    continue;
                }
                catch (IndexOutOfBoundsException ioobe) {
                    violations.add(new Violation(name, jar.getName(), " cannot be read"));
                    continue;
                }
                // more aggressive way - use reader matching to file extension
                if (name.endsWith(".png")) {
                    ImageInputStream stream = ImageIO.createImageInputStream(jar.getInputStream(entry));
                    ImageReadParam param = PNG_READER.getDefaultReadParam();
                    try {
                        PNG_READER.setInput(stream, true, true);
                        img = PNG_READER.read(0, param);
                    }
                    catch (IOException ioe1) {
                        violations.add(new Violation(name, jar.getName(), "Not a PNG image"));
                        continue;
                    }
                    stream.close();
                }
                else if (name.endsWith(".gif")) {
                    ImageInputStream stream = ImageIO.createImageInputStream(jar.getInputStream(entry));
                    ImageReadParam param = GIF_READER.getDefaultReadParam();
                    try {
                        GIF_READER.setInput(stream, true, true);
                        img = GIF_READER.read(0, param);
                    }
                    catch (IOException ioe1) {
                        violations.add(new Violation(name, jar.getName(), "Not a GIF image"));
                        continue;
                    }
                    stream.close();
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some images in IDE have problems ("+violations.size()+"):\n");
            for (Violation viol: violations) {
                msg.append(viol).append('\n');
            }
            fail(msg.toString());
        }
        //                    assertTrue (entry.toString()+" should have line number table", v.foundLineNumberTable());
    }
    
    private static class Violation implements Comparable<Violation> {
        String entry;
        String jarFile;
        String comment;
        Violation(String entry, String jarFile, String comment) {
            this.entry = entry;
            this.jarFile = jarFile;
            this.comment = comment;
        }
    
        public int compareTo(Violation v2) {
            String second = v2.entry + v2.jarFile;
            return (entry +jarFile).compareTo(second);
        }
        
        @Override
        public String toString() {
            return comment + ": " + entry+" in "+jarFile;
        }
    }

    /** Too large or too small (empty) files are suspicious.
     *  There should be just couple of them: splash image
     */
    public void testUnusualFileSize() throws Exception {
        SortedSet<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            // check JAR files only
            if (!f.getName().endsWith(".jar"))
                continue;
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            BufferedImage img;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                
                long len = entry.getSize();
                if (len >= 0 && len < 10) {
                    violations.add(new Violation(entry.getName(), jar.getName(), " is too small ("+len+" bytes)"));
                }
                if (len >= 200 * 1024) {
                    violations.add(new Violation(entry.getName(), jar.getName(), " is too large ("+len+" bytes)"));
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some files have extreme size ("+violations.size()+"):\n");
            for (Violation viol: violations) {
                msg.append(viol).append('\n');
            }
            fail(msg.toString());
        }
    }
    
    /** Scan for accidentally commited files that get into product
     */
    public void testInappropraiteEntries() throws Exception {
        SortedSet<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            // check JAR files only
            if (!f.getName().endsWith(".jar"))
                continue;
            
            if (!f.getName().endsWith("cssparser-0-9-4-fs.jar")) // #108644
                continue;
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            BufferedImage img;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                
                if (entry.getName().endsWith("Thumbs.db")) {
                    violations.add(new Violation(entry.getName(), jar.getName(), " should not be in module JAR"));
                }
                if (entry.getName().contains("nbproject/private")) {
                    violations.add(new Violation(entry.getName(), jar.getName(), " should not be in module JAR"));
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some files does not belong to module JARs ("+violations.size()+"):\n");
            for (Violation viol: violations) {
                msg.append(viol).append('\n');
            }
            fail(msg.toString());
        }
    }
}
