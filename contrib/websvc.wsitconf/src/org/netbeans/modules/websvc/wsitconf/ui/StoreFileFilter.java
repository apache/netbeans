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

package org.netbeans.modules.websvc.wsitconf.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;


public class StoreFileFilter extends FileFilter {
    
    public static final String JKS_EXT = "jks";        //NOI18N
    public static final String PKCS12_EXT = "p12";     //NOI18N

    public StoreFileFilter() { }

    @Override
    public boolean accept(File f) {
        if ((f != null) && f.exists() && (f.getName() != null) && 
                ((f.getName().contains(JKS_EXT)  || f.getName().contains(PKCS12_EXT)) || (f.isDirectory()))) {
            return true;
        }
        return false;
    }
    @Override
    public String getDescription() {
        return NbBundle.getMessage(StoreFileFilter.class, "STORE_FILES");  //NOI18N
    }
}
