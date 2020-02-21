/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.spi.configurations;

import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CompileOptionsProvider {
    private static final CompileOptionsProvider DEFAULT = new Default();

    public abstract void onRename(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration, String newName);
    public abstract void onRemove(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration);
    public abstract AllOptionsProvider getOptions(Item item);
    
    protected CompileOptionsProvider() {
    }

    /**
     * Static method to obtain the PkgConfig implementation.
     * @return the PkgConfig
     */
    public static synchronized CompileOptionsProvider getDefault() {
        return DEFAULT;
    }
    
    /**
     * Implementation of the default CompileOptionsProvider
     */
    private static final class Default extends CompileOptionsProvider {
        private final Lookup.Result<CompileOptionsProvider> res;
        private static final boolean FIX_SERVICE = true;
        private CompileOptionsProvider fixedSelector;
        Default() {
            res = Lookup.getDefault().lookupResult(CompileOptionsProvider.class);
        }

        private CompileOptionsProvider getService(){
            CompileOptionsProvider service = fixedSelector;
            if (service == null) {
                for (CompileOptionsProvider selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    fixedSelector = service;
                }
            }
            return service;
        }

        @Override
        public AllOptionsProvider getOptions(Item item) {
            CompileOptionsProvider service = getService();
            if (service != null) {
                return service.getOptions(item);
            }
            return null;
        }

        @Override
        public void onRename(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration, String newName) {
            CompileOptionsProvider service = getService();
            if (service != null) {
                service.onRename(cd, makeConfiguration, newName);
            }
        }

        @Override
        public void onRemove(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration) {
            CompileOptionsProvider service = getService();
            if (service != null) {
                service.onRemove(cd, makeConfiguration);
            }
        }
    }
}
