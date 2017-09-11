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

package org.netbeans.modules.j2ee.persistence.spi.support;

import java.io.File;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.EntityMappingsMetadataModelFactory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsMetadataModelHelper {

    private final ClassPath bootPath;
    private final ClassPath moduleBootPath;
    private final ClassPath compilePath;
    private final ClassPath moduleCompilePath;
    private final ClassPath moduleClassPath;
    private final ClassPath sourcePath;
    private final ClassPath moduleSourcePath;
    private final Object modelLock;

    private File persistenceXml;

    private MetadataModel<EntityMappingsMetadata> model;

    public static EntityMappingsMetadataModelHelper create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        return new EntityMappingsMetadataModelHelper(bootPath, ClassPath.EMPTY, compilePath, ClassPath.EMPTY, ClassPath.EMPTY, sourcePath, ClassPath.EMPTY);
    }

    private EntityMappingsMetadataModelHelper(ClassPath bootPath, ClassPath moduleBootPath, ClassPath compilePath, ClassPath moduleCompilePath, ClassPath moduleClassPath, ClassPath sourcePath, ClassPath moduleSourcePath) {
        this.bootPath = bootPath;
        this.moduleBootPath = moduleBootPath;
        this.compilePath = compilePath;
        this.moduleCompilePath = moduleCompilePath;
        this.moduleClassPath = moduleClassPath;
        this.sourcePath = sourcePath;
        this.moduleSourcePath = moduleSourcePath;
        modelLock = new Object();
    }

    public synchronized void changePersistenceXml(File newPersistenceXml) {
        persistenceXml = newPersistenceXml;
    }

    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String puName) {
        File pXml;
        synchronized (this) {
            pXml = this.persistenceXml;
        }
        if (pXml == null || FileUtil.toFileObject(pXml) == null) {
            return null;
        }
        // XXX trivial implementation which is not affected by the contents of
        // the persistence unit (i.e., the list of orm.xml files and the list
        // of entity classes)
        return getDefaultEntityMappingsModel(false);
    }
    
    public MetadataModel<EntityMappingsMetadata> getDefaultEntityMappingsModel(boolean withDeps) {
        synchronized (modelLock) {
            if (model == null) {
                model = EntityMappingsMetadataModelFactory.createMetadataModel(bootPath, moduleBootPath, compilePath, moduleCompilePath, moduleClassPath, sourcePath, moduleSourcePath);
            }
            return model;
        }
    }

    /**
     * Builder for {@link EntityMappingsMetadataModelHelper}.
     * @since 1.36
     */
    public static final class Builder {
        private final ClassPath bootPath;
        private ClassPath moduleBootPath = ClassPath.EMPTY;
        private ClassPath classPath = ClassPath.EMPTY;
        private ClassPath moduleCompilePath = ClassPath.EMPTY;
        private ClassPath moduleClassPath = ClassPath.EMPTY;
        private ClassPath sourcePath = ClassPath.EMPTY;
        private ClassPath moduleSourcePath = ClassPath.EMPTY;

        public Builder(@NonNull final ClassPath bootPath) {
            Parameters.notNull("bootPath", bootPath);   //NOI18N
            this.bootPath = bootPath;
        }

        @NonNull
        public Builder setModuleBootPath(@NullAllowed ClassPath moduleBootPath) {
            if (moduleBootPath == null) {
                moduleBootPath = ClassPath.EMPTY;
            }
            this.moduleBootPath = moduleBootPath;
            return this;
        }

        @NonNull
        public Builder setClassPath(@NullAllowed ClassPath classPath) {
            if (classPath == null) {
                classPath = ClassPath.EMPTY;
            }
            this.classPath = classPath;
            return this;
        }

        @NonNull
        public Builder setModuleCompilePath(@NullAllowed ClassPath moduleCompilePath) {
            if (moduleCompilePath == null) {
                moduleCompilePath = ClassPath.EMPTY;
            }
            this.moduleCompilePath = moduleCompilePath;
            return this;
        }

        @NonNull
        public Builder setModuleClassPath(@NullAllowed ClassPath moduleClassPath) {
            if (moduleClassPath == null) {
                moduleClassPath = ClassPath.EMPTY;
            }
            this.moduleClassPath = moduleClassPath;
            return this;
        }

        @NonNull
        public Builder setSourcePath(@NullAllowed ClassPath sourcePath) {
            if (sourcePath == null) {
                sourcePath = ClassPath.EMPTY;
            }
            this.sourcePath = sourcePath;
            return this;
        }

        @NonNull
        public Builder setModuleSourcePath(@NullAllowed ClassPath moduleSourcePath) {
            if (moduleSourcePath == null) {
                moduleSourcePath = ClassPath.EMPTY;
            }
            this.moduleSourcePath = moduleSourcePath;
            return this;
        }

        /**
         * Creates a new {@link ClasspathInfo}.
         * @return the {@link ClasspathInfo}
         */
        @NonNull
        public EntityMappingsMetadataModelHelper build() {
            return new EntityMappingsMetadataModelHelper(
                    bootPath,
                    moduleBootPath,
                    classPath,
                    moduleCompilePath,
                    moduleClassPath,
                    sourcePath,
                    moduleSourcePath);
        }
    }
}
