/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.embedder.impl;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.eclipse.sisu.plexus.Roles;

/**
 * this module is meant to be used by the project embedder only
 * @author mkleint
 */
public class ExtensionModule implements Module {

    public ExtensionModule() {
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(PluginDependenciesResolver.class).to(NbPluginDependenciesResolver.class);
        binder.bind(Roles.componentKey(RepositoryConnectorFactory.class, "offline")).to(OfflineConnector.class);
        //#212214 the EnhancedLocalRepositoryManager will claim artifact is not locally present even if file is there but some metadata is missing
        //we just replace it with the simple variant that relies on file's presence only. 
        //I'm a bit afraid to remove the binding altogether, that's why we map simple to enhanced.
        binder.bind(Roles.componentKey(LocalRepositoryManagerFactory.class, "enhanced")).to(SimpleLocalRepositoryManagerFactory.class);
        
        //exxperimental only.
//        binder.bind(InheritanceAssembler.class).to(NbInheritanceAssembler.class);
        binder.bind(ModelBuilder.class).to(NBModelBuilder.class);
    }
    
}
