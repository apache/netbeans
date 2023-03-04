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

package org.netbeans.modules.tasklist.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;

/**
 *
 * @author S. Aubrecht
 */
public abstract class ScannerDescriptor implements Comparable<ScannerDescriptor> {

    public abstract String getType();
    
    public abstract String getDisplayName();
    
    public abstract String getDescription();
    
    public abstract String getOptionsPath();
    
    
    public int compareTo( ScannerDescriptor sd ) {
        return getDisplayName().compareTo( sd.getDisplayName() );
    }
    
    public static String getType( FileTaskScanner scanner ) {
        return scanner.getClass().getName();
    }
    
    public static String getType( PushTaskScanner scanner ) {
        return scanner.getClass().getName();
    }
    
    public static List<? extends ScannerDescriptor> getDescriptors() {
        List<? extends FileTaskScanner> fileScanners = ScannerList.getFileScannerList().getScanners();
        List<? extends PushTaskScanner> simpleScanners = ScannerList.getPushScannerList().getScanners();
        
        ArrayList<ScannerDescriptor> res = new ArrayList<ScannerDescriptor>( fileScanners.size() + simpleScanners.size() );
        for( FileTaskScanner s : fileScanners ) {
            res.add( new FileDescriptor( s ) );
        }
        for( PushTaskScanner s : simpleScanners ) {
            res.add( new PushDescriptor( s ) );
        }
        Collections.sort( res );
        return res;
    }
    
    private static class FileDescriptor extends ScannerDescriptor {
        private FileTaskScanner scanner;
        public FileDescriptor( FileTaskScanner scanner ) {
            assert null != scanner;
            this.scanner = scanner;
        }
    
        public String getDisplayName() {
            return Accessor.getDisplayName( scanner );
        }

        public String getDescription() {
            return Accessor.getDescription( scanner );
        }

        public String getOptionsPath() {
            return Accessor.getOptionsPath( scanner );
        }
    
        public String getType() {
            return getType( scanner );
        }
    }
    
    private static class PushDescriptor extends ScannerDescriptor {
        private PushTaskScanner scanner;
        public PushDescriptor( PushTaskScanner scanner ) {
            assert null != scanner;
            this.scanner = scanner;
        }
    
        public String getDisplayName() {
            return Accessor.getDisplayName( scanner );
        }

        public String getDescription() {
            return Accessor.getDescription( scanner );
        }

        public String getOptionsPath() {
            return Accessor.getOptionsPath( scanner );
        }
    
        public String getType() {
            return getType( scanner );
        }
    }
}
