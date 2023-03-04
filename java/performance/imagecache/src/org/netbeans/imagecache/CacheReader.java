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
 * CacheReader.java
 *
 * Created on February 16, 2004, 8:11 PM
 */

package org.netbeans.imagecache;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author  Tim Boudreau
 */
public class CacheReader {
    static final String filename = CacheWriter.filename;
    static final int METAENTRY_LENGTH = CacheWriter.METAENTRY_LENGTH;
    static final int ID_OFFSET = CacheWriter.ID_OFFSET;
    private File cachefile;
    private File metafile;
    private ByteBuffer cachebuffer = null;
    private ByteBuffer metabuffer = null;

    
    /** Creates a new instance of CacheReader */
    public CacheReader(File cacheDir) throws IOException {
        this (new File(cacheDir + File.separator + filename + ".cache"), 
            new File(cacheDir + File.separator + filename + ".metadata"));
    }
    
    public CacheReader(File cachefile, File metafile) throws IOException {
        if (!cachefile.exists() || !metafile.exists()) {
            throw new IOException ("Cache does not exist: " + cachefile + "," + metafile);
        }
        this.cachefile = cachefile;
        this.metafile = metafile;
    }
    
    public String[] getIDs() throws IOException {
        ArrayList a = new ArrayList();
        ByteBuffer b = getMetaBuffer().asReadOnlyBuffer();
        int max = b.limit() / METAENTRY_LENGTH;
        for (int i=0; i < max; i++) {
            int len = b.asIntBuffer().get();
            b.mark();
            b.position(b.position() + ID_OFFSET);
            byte[] bytes = new byte[len];
            b.get(bytes);
            a.add(new String(bytes));
            b.reset();
            if (i != max-1) {
                b.position(b.position() + METAENTRY_LENGTH);
            }
        }
        String[] result = new String[a.size()];
        result = (String[]) a.toArray(result);
        return result;
    }
    
    public Image find (String id) throws IOException {
        ByteBuffer buf = findMetadataFor (toByteArray(id));
        if (buf == null) {
            return null;
        }
        
        IntBuffer ibuf = buf.asIntBuffer();
        
        int[] intEntries = new int[3];
        ibuf.get(intEntries);
        
        assert id.length() == intEntries[0];
        
        int width = intEntries[1];
        int height = intEntries[2];
        
        long[] longEntries = new long[2];

        buf.position(buf.position() + 12);
        
        LongBuffer lbuf = buf.asLongBuffer();
        lbuf.get(longEntries);
        
        //The cast to int is safe, the writer will never write a cache file
        //bigger than Integer.MAX_VALUE
        int start = (int) longEntries[0];
        int end = (int) longEntries[1];
        
        ByteBuffer cache = getBuffer().asReadOnlyBuffer();
        cache.position(start);
        
        System.err.println("Cache position is " + cache.position());
        IntBuffer databuffer = cache.asIntBuffer();
        System.err.println("IntBuffer position is " + databuffer.position());
        return createImageFrom (databuffer, width, height);
    }
    
    /*
    private Image createImageFrom (IntBuffer data, int width, int height) {
        //Cheap'n'cheesy for now - should really create a custom DataSource which
        //reads the buffer for its pixel data
        
        BufferedImage bi = new BufferedImage (width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x=0; x < width; x++) {
            for (int y=0; y < height; y++) {
                try {
                    bi.setRGB (x, y, data.get());
                } catch (Exception e) {
                    System.err.println("Exception getting " + x + "," + y + " pos:" + data.position());
                }
            }
        }
        return bi;
    }
     */
    
    private Image createImageFrom (IntBuffer data, int width, int height) {
        CRaster raster = new CRaster (data, width, height);
        return new CImage (ColorModel.getRGBdefault(), raster, false, null);
    }
    
    /** Locates metadata related to the passed id (converted into a byte
     * array).  Returns either a bytebuffer with the position set to the
     * start of the metadata or null.  */
    private ByteBuffer findMetadataFor (byte[] id) throws IOException {
        System.err.println("FindMetadataFor " + new String(id));
        
        long result = -1;
        //Get a buffer clone so we don't have threading problems - never
        //use the master buffer
        ByteBuffer buf = getMetaBuffer().asReadOnlyBuffer();
        
        IntBuffer ibuf = buf.asIntBuffer();
        
        do {
           //First, see if the ID (image filename) length matches the ID
           //we received - if it doesn't, no need to examine the record
           int thisIdLength = ibuf.get();
           System.err.println("pos:" + ibuf.position() + " idLen: " + thisIdLength + " looking for len: " + id.length);
           
           if (thisIdLength == id.length) {
               //Mark the start of this metadata record and position to
               //the start of the ID entry
               System.err.println("Length match. Putting mark at " + (buf.position()) + " and moving to " + (buf.position() + ID_OFFSET) + " to check data");
               buf.mark().position (buf.position() + ID_OFFSET);
               
               byte[] chars = new byte[id.length];
               
               //Fetch the ID into the array, and reset the buffer position
               //for either returning or skipping to the next record
               buf.get(chars).reset();
               System.err.println(" id from metadata: " + new String(chars));
               
               //Compare it with the id we were passed
               if (Arrays.equals(chars, id)) {
                   System.err.println("  MATCHED - position: " + buf.position());
                   return buf;
               }
           }
           //Skip ahead to the next record
           buf.position(buf.position() + METAENTRY_LENGTH);
           ibuf.position(buf.position() / 4);
           System.err.println("Buffer pos: " + buf.position() + " ibuf: " + ibuf.position());
        } while (buf.position() <= buf.limit() - METAENTRY_LENGTH);
        
        return null;
    }
    
    private static byte[] toByteArray(String s) {
        char[] c = s.toCharArray();
        byte[] result = new byte[c.length];
        for (int i=0; i < c.length; i++) {
            result[i] = (byte) c[i];
        }
        return result;
    }
    
    protected ByteBuffer createBuffer(File file) throws IOException {
        return new FileInputStream (file).getChannel().map(
            FileChannel.MapMode.READ_ONLY, 0, file.length());
    }
    
    private ByteBuffer getBuffer() throws IOException {
        if (cachebuffer == null) {
            cachebuffer = createBuffer(cachefile);
        }
        cachebuffer.position(0);
        return cachebuffer;
    }
    
    private ByteBuffer getMetaBuffer() throws IOException {
        if (metabuffer == null) {
            metabuffer = createBuffer(metafile);
        }
        return metabuffer;
    }
}
