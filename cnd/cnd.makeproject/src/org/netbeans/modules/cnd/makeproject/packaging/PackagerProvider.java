/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.makeproject.packaging;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptorProvider;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.makeproject.api.PackagerDescriptorProvider.class)
public class PackagerProvider implements PackagerDescriptorProvider {

    @Override
    public List<PackagerDescriptor> getPackagerDescriptorProviderList() {
        ArrayList<PackagerDescriptor> list = new ArrayList<>();
        list.add(new TarPackager());
        list.add(new ZipPackager());
        list.add(new SVR4Packager());
        list.add(new IPSPackager());
        list.add(new RPMPackager());
        list.add(new DebianPackager());
        return list;
    }
}
