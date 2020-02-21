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
package org.netbeans.modules.cnd.loaders;

import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.spi.CndCookieProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.loaders.MultiDataObject;
import org.openide.util.lookup.InstanceContent.Convertor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Source data objects need binary support to compile and run source with main method.
 *
 */
@ServiceProvider(service = CndCookieProvider.class)
public final class CndCompileSupportProvider extends CndCookieProvider {
    static final CndCompileSupportFactory staticFactory = new CndCompileSupportFactory();

    @Override
    public void addLookup(InstanceContentOwner icOwner) {
        MultiDataObject mdao = (MultiDataObject) icOwner;
        if (MIMENames.isCppOrCOrFortran(mdao.getPrimaryFile().getMIMEType())){
            icOwner.getInstanceContent().add(mdao, staticFactory);
        }
    }

    private static class CndCompileSupportFactory implements Convertor<MultiDataObject, CompileExecSupport> {

        public CndCompileSupportFactory() {
        }

        @Override
        public CompileExecSupport convert(MultiDataObject obj) {
            return new CompileExecSupport(obj.getPrimaryEntry());
        }

        @Override
        public Class<? extends CompileExecSupport> type(MultiDataObject obj) {
            return CompileExecSupport.class;
        }

        @Override
        public String id(MultiDataObject obj) {
            return CompileExecSupport.class.getName()+obj.getPrimaryFile().getPath();
        }

        @Override
        public String displayName(MultiDataObject obj) {
            return id(obj);
        }
    }
}
