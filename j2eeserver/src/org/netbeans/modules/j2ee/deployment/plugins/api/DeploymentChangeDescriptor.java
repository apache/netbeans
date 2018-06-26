/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.impl.ChangeDescriptorAccessor;
import org.netbeans.modules.j2ee.deployment.impl.ServerFileDistributor;

/**
 * This class allows the plugin to ask for changes which happened
 * in the application. This is compatible replacement for {@link AppChangeDescriptor}.
 *
 * @author Petr Hejl
 * @since 1.47
 */
public final class DeploymentChangeDescriptor implements AppChangeDescriptor {

    private final ServerFileDistributor.AppChanges desc;

    private final boolean serverResourcesChanged;
    
    static {
        ChangeDescriptorAccessor.setDefault(new ChangeDescriptorAccessor() {
            @Override
            public DeploymentChangeDescriptor newDescriptor(ServerFileDistributor.AppChanges desc) {
                return new DeploymentChangeDescriptor(desc, false);
            }

            @Override
            public DeploymentChangeDescriptor withChangedServerResources(DeploymentChangeDescriptor desc) {
                return new DeploymentChangeDescriptor(desc.desc, true);
            }
        });
    }

    private DeploymentChangeDescriptor(ServerFileDistributor.AppChanges desc,
            boolean serverResourcesChanged) {
        this.desc = desc;
        this.serverResourcesChanged = serverResourcesChanged;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getChangedEjbs() {
        return desc.getChangedEjbs();
    }

    /**
     * {@inheritDoc}
     */
    public boolean ejbsChanged() {
        return desc.ejbsChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean serverDescriptorChanged() {
        return desc.serverDescriptorChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean manifestChanged() {
        return desc.manifestChanged();
    }

    /**
     * {@inheritDoc}<p>
     * 
     * Returns all changed files (locations where they are deployed).
     */
    public File[] getChangedFiles() {
        return desc.getChangedFiles();
    }

    /**
     * {@inheritDoc}
     */
    public boolean descriptorChanged() {
        return desc.descriptorChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean classesChanged() {
        return desc.classesChanged();
    }

    /**
     * Returns <code>true</code> if the resources intended to be delivered to
     * server (such as connection pools) were changed.
     *
     * @return <code>true</code> if server side reources were changed
     * @since 1.63
     */
    public boolean serverResourcesChanged() {
        return serverResourcesChanged;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("classesChanged: ").append(classesChanged());
        builder.append(", ");
        builder.append("descriptorChanged: ").append(descriptorChanged());
        builder.append(", ");
        builder.append("ejbsChanged: ").append(ejbsChanged());
        builder.append(", ");
        builder.append("manifestChanged: ").append(manifestChanged());
        builder.append(", ");
        builder.append("serverDescriptorChanged: ").append(serverDescriptorChanged());
        builder.append(", ");
        builder.append("serverResourcesChanged: ").append(serverResourcesChanged());
        return builder.toString();
    }

}
