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
package org.netbeans.modules.cnd.spi.toolchain;

import java.util.Collection;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.windows.IOPosition;
import org.openide.windows.OutputEvent;

/**
 *
 */
public abstract class OutputListenerProvider {

    private static final OutputListenerProvider DEFAULT = new OutputListenerProviderDefault();

    public static OutputListenerProvider getInstance() {
        if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
            return DEFAULT;
        }
        Collection<? extends OutputListenerProvider> notifiers = Lookup.getDefault().lookupAll(OutputListenerProvider.class);
        if (notifiers.isEmpty()) {
            return DEFAULT;
        }
        return notifiers.iterator().next();
    }

    abstract public OutputListenerExt get(ErrorParserProvider.OutputListenerRegistry registry, FileObject file, int line, boolean isError, String description, IOPosition.Position ioPos);

    abstract public void attach(ErrorParserProvider.OutputListenerRegistry registry);

    private static class OutputListenerProviderDefault extends OutputListenerProvider {

        private static final OutputListenerExt impl = new OutputListenerExt() {
            @Override
            public void outputLineSelected(OutputEvent ev) {
                //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void outputLineAction(OutputEvent ev) {
                //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void outputLineCleared(OutputEvent ev) {
                //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isError() {
                return false;
            }

            @Override
            public FileObject getFile() {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public int getLine() {
                return -1000;
            }
        };

        public OutputListenerProviderDefault() {
        }

        @Override
        public OutputListenerExt get(ErrorParserProvider.OutputListenerRegistry registry, FileObject file, int line, boolean isError, String description, IOPosition.Position ioPos) {
            return impl;
        }

        @Override
        public void attach(ErrorParserProvider.OutputListenerRegistry registry) {
            //do nothing?
        }
    }

}
