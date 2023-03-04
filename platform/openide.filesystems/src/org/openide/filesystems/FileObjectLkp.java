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
package org.openide.filesystems;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.implspi.NamedServicesProvider;

/** A lookup associated with a {@link FileObject}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class FileObjectLkp extends ProxyLookup {
    private FileObjectLkp() {
    }
    
    static synchronized FileObjectLkp create(FileObject dest, boolean create) {
        FileObjectLkp lkp = dest.lookup();
        if (lkp == null && create) {
            lkp = new FileObjectLkp();
            dest.assignLookup(lkp);
            lkp.assign(dest);
        }
        return lkp;
    }
    static synchronized void reassign(FileObject from, FileObject to) {
        FileObjectLkp lkp = from.lookup();
        if (lkp != null) {
            to.assignLookup(lkp);
            lkp.assign(to);
        }
    }
    private void assign(FileObject fo) {
        Lookup l = NamedServicesProvider.createLookupFor(fo);
        if (l == null) {
            l = Lookups.singleton(fo);
        }
        this.setLookups(l);
    }
}
