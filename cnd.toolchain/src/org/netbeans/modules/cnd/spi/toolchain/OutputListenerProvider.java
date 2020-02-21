/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
