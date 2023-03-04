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
 *//*
 * CacheWriter.java
 *
 * Created on February 16, 2004, 8:12 PM
 */

package org.netbeans.imagecache;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.ImageConsumer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Writes an image cache.  It will create two files in the directory set in
 * setDir:  images.cache and images.metadata.  The first is a flat binary
 * cache of image data;  the second is metadata about the image names, offsets
 * into the file, etc.
 * <p>
 * File formats:<br>
 * The image cache is a big blob of rgb pixel data with no markers, format 
 * information or anything else.  The metadata file is a binary file consisting
 * of a series of 255 character records.  Each record is in the format:
 * <ul>
 * <li>4 bytes: integer length of the id (image path in its jar)</li>
 * <li>4 bytes: integer width of the image</li>
 * <li>4 bytes: integer height of the image</li>
 * <li>8 bytes: long offset into the cache file for data start</li>
 * <li>8 bytes: long offset into the cache file for data end</li>
 * </ul>
 * While the width/height of the image could be derived from only one 
 * parameter and the data length, these are included separately to enable
 * future use of data compression (such as RLE to reduce repeated transparent
 * pixel data).
 * <p>
 * As long as the output filename is not user-settable, it should be possible
 * to indicate future format changes by altering the file name.
 * 
 *
 * @author  Tim Boudreau
 */
public class CacheWriter {
    private File file = null;
    private String dir = null;
    private File metafile = null;
    static final String filename = "images"; //NOI18N
    /** Fixed length of a single entry of metadata */
    static final int METAENTRY_LENGTH = 256;
    /** Offset from the beginning of a metadata entry where the id starts */
    static final int ID_OFFSET = 28;
    /** Creates a new instance of CacheWriter */
    public CacheWriter() {
    }
    
    public void setDir (String dir, boolean clean) throws IOException {
        StringBuffer sb = new StringBuffer(dir);
        if (sb.charAt(sb.length()-1) != File.pathSeparatorChar) {
            sb.append (File.separatorChar);
        }
        this.dir = sb.toString();
        File f = new File (dir);
        if (!f.exists()) {
            f.mkdir();
        }
        if (clean) {
            File mf = new File (this.dir + filename + ".metadata");
            File fc = new File (this.dir + filename + ".cache");
            if (mf.exists()) {
                mf.delete();
            }
            if (fc.exists()) {
                fc.delete();
            }
        }
    }
    
    /** Recursively writes all files to the image cache */
    public void writeDir (String dirname, boolean append) throws IOException {
        File f = new File (dirname);
        if (!f.exists()) {
            throw new IOException ("Directory " + dirname + " does not exist");
        }
        if (!f.isDirectory()) {
            throw new IOException ("File " + dirname + " is not a directory");
        }
        //Force existing files to be deleted if !append
        if (!append) {
            getOutfile(append);
            getMetafile(append);
        }
        String[] filenames = findImageFiles (f);
        for (int i=0; i < filenames.length; i++) {
            write (filenames[i], true);
        }
    }
    
    private String[] findImageFiles (File root) {
        String[] formats = ImageIO.getReaderFormatNames();
        Set set = new HashSet();
        findImageFiles (root, set);
        String[] result = new String[set.size()];
        result = (String[]) set.toArray(result);
        return result;
    }
    
    private void findImageFiles (File root, Set set) {
        String[] files = root.list(new FormatFilenameFilter());
        for (int i=0; i < files.length; i++) {
            set.add (root.getPath() + File.separatorChar + files[i]);
        }
        File[] children = root.listFiles(new DirectoryFilter());
        for (int i=0; i < children.length; i++) {
            findImageFiles(children[i], set);
        }
    }
    
    public void write (String filename, boolean append) throws IOException {
        try {
            BufferedImage img;
            //Force the gif decoder to get registered
            Object o = com.sun.imageio.plugins.gif.GIFImageReader.class;
            ImageIO.scanForPlugins();
            
            img = ImageIO.read(new File(filename));
                write (img, append, filename);
                
        } catch (Exception ioe) {
            System.err.println("Could not write " + filename + " - " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    
    public void write (BufferedImage img, boolean append, String id) throws IOException {
        File out = getOutfile(append);
        File meta = getMetafile(append);
        System.err.println("Writing to " + out + " and " + meta);
        
        int width = img.getWidth();
        int height = img.getHeight();
        
        ByteBuffer buf = ByteBuffer.allocate(width * height * 4);
        IntBuffer ibuf = buf.asIntBuffer();
  
        for (int y=0; y < height; y++) {
            for (int x=0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                ibuf.put(pixel);
            }
        }
        FileOutputStream fileOut = new FileOutputStream (out, append);
        FileOutputStream metaOut = new FileOutputStream (meta, append);
        FileChannel fileChannel = fileOut.getChannel();
        
        if (append) {
            fileChannel.position(out.length());
        }
        
        //Check the size of the file we're creating - nio bytebuffers are
        //limited to dealing with files < Integer.MAX_VALUE large
        if (fileChannel.position() + buf.limit() > Integer.MAX_VALUE) {
            //Can handle this and create a second cache file in the unlikely
            //event this comes to pass
            throw new BufferOverflowException();
        }
        
        long start = fileChannel.position();
        
        fileChannel.write(buf);
        
        long end = fileChannel.position();
        
        fileChannel.force(true);
        fileChannel.close();
        
        FileChannel metaChannel = metaOut.getChannel();
        if (append) {
            metaChannel.position(meta.length());
        }
        
        metaChannel.write(getMetadata(img, id, start, end));
        metaChannel.force(true);
        metaChannel.close();
    }
    
    private ByteBuffer getMetadata (BufferedImage img, String id, long start, long end) throws IOException {
        byte[] bytes = new byte[METAENTRY_LENGTH];
        Arrays.fill(bytes, (byte)'-'); //XXX
        
        ByteBuffer result = ByteBuffer.wrap(bytes);
        result.position(0);
        
        int width = img.getWidth();
        int height = img.getHeight();
        
        id = convertPathSeparators (id);
        
        //First write the id length, width and height as ints
        
        IntBuffer ibuf = result.asIntBuffer();
        ibuf.put(id.length()).put(width).put(height);
        
        result.position(result.position() + (ibuf.position() * 4));
        
        //Then write the start and end positions in the cache file as longs
        
        LongBuffer lbuf = result.asLongBuffer();
        lbuf.put(start).put(end);
        
        result.position(result.position() + (lbuf.position() * 8));
        
        //We are intentionally stripping the high eight bits - unless we start
        //having modules with katakana pathnames, 16 bits clean filenames will
        //not be needed
        
        char[] chars = id.toCharArray();
        if (chars.length + result.position() > METAENTRY_LENGTH) {
            throw new IOException ("ID " + id + " too long.  Limit is " + (METAENTRY_LENGTH - 8));
        }
        
        //Now write the id text
        for (int i=0; i < chars.length; i++) {
            result.put((byte) chars[i]);
        }
        
        result.position(METAENTRY_LENGTH);
        result.flip();
        return result;
    }
    
    private String convertPathSeparators (String id) {
        String sep = File.separator;
        if (File.separatorChar == '/') {
            return id;
        } else {
            StringBuffer sb = new StringBuffer (id);
            while (sb.indexOf(sep) != -1) {
                int idx = sb.indexOf(sep);
                sb.replace(idx, idx + sep.length()-1, "/");
            }
            return sb.toString();
        }
    }
    
    private File getOutfile (boolean append) throws IOException {
        if (file == null) {
            String outname = filename + ".cache";
            file = getOrCreateFile (outname, append);
        }
        return file;
    }
    
    private File getMetafile (boolean append) throws IOException {
        if (metafile == null) {
            String metaname = filename + ".metadata";
            metafile = getOrCreateFile (metaname, append);
        }
        return metafile;
    }
    
    private File getOrCreateFile (String filename, boolean append) throws IOException {
        if (dir == null) {
            throw new IOException ("Output directory not set");
        }
        File pdir = new File (dir);
        if (!pdir.exists()) {
            throw new IOException ("Directory " + pdir + " does not exist");
        }
        if (!pdir.isDirectory()) {
            throw new IOException ("File " + pdir + " is not a directory");
        }
        File result = new File (dir + filename);
        if (result.exists() && !append) {
            result.delete();
            result.createNewFile();
        } else {
            result.createNewFile();
        }
        return result;
    }
    
    private static class FormatFilenameFilter implements FilenameFilter {
        private String[] formats;
        public FormatFilenameFilter () {
            formats = ImageIO.getReaderFormatNames();
            String[] s = new String[formats.length + 2];
            System.arraycopy (formats, 0, s, 2, formats.length);
            s[0] = "GIF";
            s[1] = "gif";
            formats = s;
        }
        public boolean accept(File dir, String name) {
            if (name.startsWith(".xvpics")) {
                //Goofy non-GIF .xvpicsNNN.gif files that confuse the decoder
                return false;
            }
            for (int i=0; i < formats.length; i++) {
                if (name.endsWith(formats[i])) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private static class DirectoryFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }
}
