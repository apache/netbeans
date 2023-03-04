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

package org.netbeans.insane.impl;

import org.netbeans.insane.live.Path;

/**
 *
 * @author nenik
 */
public class Utils {
    
    static long getUsedMemory() {
//        for (int i=0; i<5; i++) {
//            System.gc();
//            System.runFinalization();
//            try {Thread.sleep(1000); } catch (InterruptedException ie) {}
//            try {byte[] a = new byte[1000000000];} catch (OutOfMemoryError oe) {}
//        }
        System.gc();
        
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    
    static Path createPath(Object item, Path next) {
        return PATH_FACTORY.createPath(item, next);
    }
    
    public static PathFactory PATH_FACTORY;
    
    public interface PathFactory {
        public Path createPath(Object item, Path next);
    }
    
    static {
        try {
            Class.forName("org.netbeans.insane.live.Path", true, Utils.class.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
