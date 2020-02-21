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

import org.netbeans.modules.cnd.execution.BinaryExecSupport;
import org.netbeans.modules.cnd.spi.CndCookieProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.loaders.MultiDataObject;
import org.openide.util.lookup.InstanceContent.Convertor;

/**
 * TODO: Is it needed class? All binaries data objects create binary support in constructors.
 * Source data objects do not need binary support.
 * Remove class or migrate binaries data objects from CookieSet to Lookup.
 *
 */
public final class CndBinaryExecSupportProvider extends CndCookieProvider {
    static final BinaryExecSupportFactory staticFactory = new BinaryExecSupportFactory();

    @Override
    public void addLookup(InstanceContentOwner icOwner) {
        MultiDataObject mdao = (MultiDataObject) icOwner;
        if (!MIMENames.isFortranOrHeaderOrCppOrC(mdao.getPrimaryFile().getMIMEType())){
            icOwner.getInstanceContent().add(mdao, staticFactory);
        }
    }

    private static class BinaryExecSupportFactory implements Convertor<MultiDataObject, BinaryExecSupport> {

        public BinaryExecSupportFactory() {
        }

        @Override
        public BinaryExecSupport convert(MultiDataObject obj) {
            return new BinaryExecSupport(obj.getPrimaryEntry());
        }

        @Override
        public Class<? extends BinaryExecSupport> type(MultiDataObject obj) {
            return BinaryExecSupport.class;
        }

        @Override
        public String id(MultiDataObject obj) {
            return BinaryExecSupport.class.getName()+obj.getPrimaryFile().getPath();
        }

        @Override
        public String displayName(MultiDataObject obj) {
            return id(obj);
        }
    }
}
