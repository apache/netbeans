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

package org.netbeans.core.startup.layers;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;
/** Test layer cache managers generally.
 * @author Jesse Glick
 */
public abstract class CacheManagerTestBaseHid extends NbTestCase implements ImageObserver {
    private static Long initTime;
    
    /**
     * Called from layer, do not rename!
     */
    public static Object method(FileObject fo, String attr) {
        //System.err.println("CMTBH.m: fo=" + fo.getClass().getName() + "<" + fo.getPath() + ">; attr=" + attr + "; x=" + fo.getAttribute("x"));
        return String.valueOf(fo.getAttribute("x")) + "/" + attr;
    }
    
    public static Object map1(Map map) {
        return String.valueOf(map.get("x"));
    }
    public static Object map2(Map map, String attr) {
        return String.valueOf(map.get("x")) + "/" + attr;
    }
    public static Object mapImage(Map map) {
        return map.get("image");
    }
    public static Object mapDisplayName(Map map) {
        return map.get("displayName");
    }
    
    protected CacheManagerTestBaseHid(String name) {
        super(name);
    }
    
    protected static interface ManagerFactory {
        LayerCacheManager createManager() throws Exception;
        boolean supportsTimestamps ();
    }

    @Override
    protected void setUp() throws Exception {
        if (initTime == null) {
            File f = new File(getWorkDir(), "test");
            f.createNewFile();
            TestFileUtils.touch(f, null);
            initTime = f.lastModified();
        }
        clearWorkDir();
    }
    
    protected URL loadResource(String name) throws IOException {
        return new URL(new URL(Utilities.toURI(getDataDir()).toURL(), "layers/"), name);
    }
    
    public void testCacheManager() throws Exception {
        ManagerFactory mf = (ManagerFactory)this;
        
        clearWorkDir();
        LayerCacheManager m = mf.createManager();
        // layer2.xml should override layer1.xml where necessary:
        List<URL> urls = Arrays.asList(
            loadResource("data/layer2.xml"),
            loadResource("data/layer1.xml"));
        FileSystem f = BinaryCacheManagerTest.store(m, urls);
        // Initial run.
        checkStruct(f);
        if (mf.supportsTimestamps ()) {
            checkLastModified (f, "foo/test2", "data/test2a");
            checkLastModified (f, "bar/test5", "data/layer2.xml");
            checkLastModified (f, "baz/thingy", "data/layer1.xml");
            checkLastModified (f, "foo/test1", "data/layer1.xml");
            checkLastModified (f, "bug39210/inline.txt", "data/layer1.xml");
        }
    }
    
    public void testSameFolderAndFileName() throws Exception {
        ManagerFactory mf = (ManagerFactory) this;

        clearWorkDir();
        LayerCacheManager m = mf.createManager();
        // layer2.xml should override layer1.xml where necessary:
        List<URL> urls = Arrays.asList(
            loadResource("data/folder2.xml"),
            loadResource("data/folder1.xml"));
        
        FileSystem f = BinaryCacheManagerTest.store(m, urls);
        final FileObject fld = f.findResource("folder");
        
        assertTrue("Is folder", fld.isFolder());
        assertEquals("No content", 0, fld.getSize());
    }
    
    private void checkLastModified (FileSystem f, String file, String resource) throws Exception {
        FileObject obj = f.findResource (file);
        assertNotNull (file + " found", obj);
        
        long time = obj.lastModified ().getTime ();
        URL url = loadResource(resource);
        long resourceTime = url.openConnection ().getLastModified ();
        
        if (initTime < resourceTime) {
            fail ("The time of the resource " + file + " (" + resourceTime + ") is likely older than iniciation of this class (" + initTime + ")");
        }

        assertEquals ("Time of " + file + " is the same as URL time of " + resource, resourceTime, time);
        
        
    }
    
    private void checkStruct(FileSystem f) throws Exception {
        assertEquals("Root has 5 children", 5, f.getRoot().getChildren().length);
        assertEquals(1, f.findResource("bar").getChildren().length);
        assertEquals("", slurp(f, "bar/test5"));
        assertEquals(5, f.findResource("foo").getChildren().length);
        // XXX not clear if this is in fact supposed to be empty instead:
        //assertEquals("lala", slurp(f, "foo/test1"));
        assertEquals("two", attr(f, "foo/test1", "y"));
        assertEquals("rara!", slurp(f, "foo/test2"));
        assertEquals("hi!", slurp(f, "foo/test3"));
        assertEquals("three", attr(f, "foo/test3", "x"));
        assertEquals("one too", attr(f, "foo/test3", "y"));
        assertEquals("", slurp(f, "foo/test4"));
        // #29356: methodvalue should pass in MultiFileObject, not the original FileObject:
        FileSystem ffs = FileUtil.createMemoryFileSystem();
        FileUtil.createData(ffs.getRoot(), "foo/29356").setAttribute("x", "val");
        MultiFileSystem mfs = new MultiFileSystem(f, ffs);
        assertEquals("val", attr(ffs, "foo/29356", "x"));
        assertEquals("val", attr(mfs, "foo/29356", "x"));
        assertEquals("val/a", attr(mfs, "foo/29356", "a"));
        assertEquals("val", attr(mfs, "foo/29356", "map1"));
        assertEquals("val/map2", attr(mfs, "foo/29356", "map2"));
        assertEquals("Ahoj", attr(mfs, "foo/29356", "mapDisplayName"));

        StatusDecorator s = FileUtil.getConfigRoot().getFileSystem().getDecorator();
        ImageDecorator id = FileUIUtils.getImageDecorator(FileUtil.getConfigRoot().getFileSystem());
        FileObject annot = f.findResource("foo/29356");
        String annotName = s.annotateName(null, Collections.singleton(annot));
        assertEquals("Ahoj", annotName);

        Image img = id.annotateIcon(null, BeanInfo.ICON_COLOR_16x16, Collections.singleton(annot));
        assertNotNull("Icon provided", img);
        assertEquals("height", 16, img.getHeight(this));
        assertEquals("width", 16, img.getHeight(this));
        Image img32 = id.annotateIcon(null, BeanInfo.ICON_COLOR_32x32, Collections.singleton(annot));
        assertNotNull("Icon 32 provided", img32);
        assertEquals("height", 32, img32.getHeight(this));
        assertEquals("width", 32, img32.getHeight(this));
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return true;
    }
    
    private static String slurp(FileSystem f, String path) throws IOException {
        FileObject fo = f.findResource(path);
        if (fo == null) return null;
        InputStream is = fo.getInputStream();
        StringBuilder text = new StringBuilder((int)fo.getSize());
        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf)) != -1) {
            text.append(new String(buf, 0, read, StandardCharsets.US_ASCII));
        }
        return text.toString();
    }
    
    private static Object attr(FileSystem f, String path, String a) throws IOException {
        FileObject fo = f.findResource(path);
        if (fo == null) return null;
        return fo.getAttribute(a);
    }
    
}
