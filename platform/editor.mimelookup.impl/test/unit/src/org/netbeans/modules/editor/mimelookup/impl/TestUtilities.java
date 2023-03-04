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

package org.netbeans.modules.editor.mimelookup.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Roskanin
 */
public class TestUtilities {

    private TestUtilities(){
    }

    /** Method will wait max. <code> maxMiliSeconds </code> miliseconds for the <code> requiredValue </code>
     *  gathered by <code> resolver </code>.
     *
     *  @param maxMiliSeconds maximum time to wait for requiredValue
     *  @param resolver resolver, which is gathering an actual value
     *  @param requiredValue if resolver value equals requiredValue the wait cycle is finished
     *
     *  @return false if the given maxMiliSeconds time elapsed and the requiredValue wasn't obtained
     */
    public static boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue){
        int time = maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
        }
        return false;
    }
    
    /** Interface for value resolver needed for i.e. waitMaxMilisForValue method.  
     *  For more details, please look at {@link #waitMaxMilisForValue()}.
     */
    public static interface ValueResolver{
        /** Returns checked value */
        Object getValue();
    }
    
    public static void deleteFile(final File workDir, final String path) throws IOException {
        File f = new File(workDir, path);
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        fo.delete();
    }

    public static void createFile(final File workDir, final String path) throws IOException {
        FileObject fo = FileUtil.toFileObject(workDir);
        if (fo == null) {
            throw new IOException("Can't map '" + workDir.getAbsolutePath() + "' to the filesystem repository.");
        }
        if (path.endsWith("/")) {
            FileUtil.createFolder(fo, path.substring(0, path.length() - 1));
        } else {
            FileUtil.createData(fo, path);
        }
    }

    public static void sleepForWhile() {
        try {
            Thread.sleep(321);
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public static void consumeAllMemory() {
        List<byte[]> list = new ArrayList<byte[]>();
        long size = 0;
        try {
            for(int i = 0; i < 1000000; i++) {
                byte [] padding = new byte[100000];
                list.add(padding);
                size += padding.length;
            }
            throw new IllegalStateException("Can't run out of memory! The VM's heap size is too big.");
        } catch (OutOfMemoryError e) {
            // ok the VM's just run out of memory
            // release everything we've allocated
            list = null;
            System.out.println("OutOfMemory after allocating " + size + " bytes.");
        }
    }
    
    public static void gc() {
        for (int i = 0; i < 10; i++) {
            System.gc();
            try {
                Thread.sleep(123);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }
}
