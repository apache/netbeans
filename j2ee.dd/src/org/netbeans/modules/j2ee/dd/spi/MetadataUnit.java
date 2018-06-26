/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.dd.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class MetadataUnit {

    // XXX need to listen on the DD file

    public static final String PROP_DEPLOYMENT_DESCRIPTOR = "deploymentDescriptor"; // NOI18N

    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;

    private File deploymentDescriptor;

    public static MetadataUnit create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, File deploymentDescriptor) {
        return new MetadataUnit(bootPath, compilePath, sourcePath, deploymentDescriptor);
    }

    private MetadataUnit(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, File deploymentDescriptor) {
        this.bootPath = bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    public ClassPath getBootPath() {
        return bootPath;
    }

    public ClassPath getCompilePath() {
        return compilePath;
    }

    public ClassPath getSourcePath() {
        return sourcePath;
    }

    public synchronized FileObject getDeploymentDescriptor() {
        return deploymentDescriptor != null ? FileUtil.toFileObject(FileUtil.normalizeFile(deploymentDescriptor)) : null;
    }

    public void changeDeploymentDescriptor(File deploymentDescriptor) {
        synchronized (this) {
            this.deploymentDescriptor = deploymentDescriptor;
        }
        propChangeSupport.firePropertyChange(PROP_DEPLOYMENT_DESCRIPTOR, null, null);
    }
}
