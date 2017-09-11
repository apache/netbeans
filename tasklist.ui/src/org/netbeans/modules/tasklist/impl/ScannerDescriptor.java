/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
