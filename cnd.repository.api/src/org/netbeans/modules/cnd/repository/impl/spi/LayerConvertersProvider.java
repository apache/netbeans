/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.impl.spi;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;

/**
 * Represents container to get UnitsConverter
 *
 */
public final class LayerConvertersProvider {

    private final LayerConvertersProviderInstance layerProviderInstance;

    private LayerConvertersProvider(LayerConvertersProviderInstance layerProviderInstance) {
        this.layerProviderInstance = layerProviderInstance;
    }

    public static LayerConvertersProvider getReadInstance(UnitsConverter readUnitsConverter,
            FSConverter readFSConverter, FilePathConverter readFilePathConverter) {
        return new LayerConvertersProvider(new LayerConvertersProviderInstanceImpl1(readUnitsConverter, null,
                readFSConverter, null, readFilePathConverter, null));
    }

    public static LayerConvertersProvider getWriteInstance(UnitsConverter writeUnitsConverter,
            FSConverter writeFSConverter, FilePathConverter writeFilePathConverter) {
        return new LayerConvertersProvider(new LayerConvertersProviderInstanceImpl1(null, writeUnitsConverter,
                null, writeFSConverter, null, writeFilePathConverter));
    }

    public static LayerConvertersProvider getInstance(LayeringSupport layeringSupport, LayerDescriptor layerDescriptor) {
        return new LayerConvertersProvider(new LayerConvertersProviderInstanceImpl2(layeringSupport, layerDescriptor));
    }

    public UnitsConverter getReadUnitsConverter() {
        return layerProviderInstance.getReadUnitsConverter();
    }

    public UnitsConverter getWriteUnitsConverter() {
        return layerProviderInstance.getWriteUnitsConverter();
    }

    public FSConverter getReadFSConverter() {
        return layerProviderInstance.getReadFSConverter();
    }

    public FSConverter getWriteFSConverter() {
        return layerProviderInstance.getWriteFSConverter();
    }

    public FilePathConverter getReadFilePathConverter() {
        return layerProviderInstance.getReadFilePathConverter();
    }

    public FilePathConverter getWriteFilePathConverter() {
        return layerProviderInstance.getWriteFilePathConverter();
    }

    private interface LayerConvertersProviderInstance {

        // 100002 <-> 5

        public UnitsConverter getReadUnitsConverter();

        public UnitsConverter getWriteUnitsConverter();

        public FSConverter getReadFSConverter();

        public FSConverter getWriteFSConverter();

        public FilePathConverter getReadFilePathConverter();

        public FilePathConverter getWriteFilePathConverter();
    }

    private static class LayerConvertersProviderInstanceImpl1 implements LayerConvertersProviderInstance {

        private final UnitsConverter readUnitsConverter;
        private final UnitsConverter writeUnitsConverter;
        private final FSConverter readFSConverter;
        private final FSConverter writeFSConverter;
        private final FilePathConverter readFilePathConverter;
        private final FilePathConverter writeFilePathConverter;

        public LayerConvertersProviderInstanceImpl1(UnitsConverter readUnitsConverter, UnitsConverter writeUnitsConverter, 
                FSConverter readFSConverter, FSConverter writeFSConverter,
                FilePathConverter readFilePathConverter, FilePathConverter writeFilePathConverter) {
            this.readUnitsConverter = readUnitsConverter == null ? new NoopUnitIDConverter() : readUnitsConverter;
            this.writeUnitsConverter = writeUnitsConverter == null ? new NoopUnitIDConverter() : writeUnitsConverter;
            this.readFSConverter = readFSConverter == null ? new NoopFSConverter() : readFSConverter;
            this.writeFSConverter = writeFSConverter == null ? new NoopFSConverter() : writeFSConverter;
            this.readFilePathConverter = readFilePathConverter == null ? new UnsupportedFilePathConverter(): readFilePathConverter;
            this.writeFilePathConverter = writeFilePathConverter == null ? new UnsupportedFilePathConverter() : writeFilePathConverter;
        }

        @Override
        public UnitsConverter getReadUnitsConverter() {
            return readUnitsConverter;
        }

        @Override
        public UnitsConverter getWriteUnitsConverter() {
            return writeUnitsConverter;
        }

        @Override
        public FSConverter getReadFSConverter() {
            return readFSConverter;
        }

        @Override
        public FSConverter getWriteFSConverter() {
            return writeFSConverter;
        }

        @Override
        public FilePathConverter getReadFilePathConverter() {
            return readFilePathConverter;
        }

        @Override
        public FilePathConverter getWriteFilePathConverter() {
            return writeFilePathConverter;
        }

    }

    private static class LayerConvertersProviderInstanceImpl2 implements LayerConvertersProviderInstance {

        private final LayeringSupport layeringSupport;
        private final LayerDescriptor layerDescriptor;

        public LayerConvertersProviderInstanceImpl2(LayeringSupport layeringSupport, LayerDescriptor layerDescriptor) {
            this.layeringSupport = layeringSupport;
            this.layerDescriptor = layerDescriptor;
        }

        @Override
        public UnitsConverter getReadUnitsConverter() {
            return layeringSupport.getReadUnitsConverter(layerDescriptor);
        }

        @Override
        public UnitsConverter getWriteUnitsConverter() {
            return layeringSupport.getWriteUnitsConverter(layerDescriptor);
        }

        @Override
        public FSConverter getReadFSConverter() {
            return layeringSupport.getReadFSConverter(layerDescriptor);
        }

        @Override
        public FSConverter getWriteFSConverter() {
            return layeringSupport.getWriteFSConverter(layerDescriptor);
        }

        @Override
        public FilePathConverter getReadFilePathConverter() {
            return new UnsupportedFilePathConverter();
        }

        @Override
        public FilePathConverter getWriteFilePathConverter() {
            return new UnsupportedFilePathConverter();
        }

    }

    private static final class NoopUnitIDConverter implements UnitsConverter {

        @Override
        public int clientToLayer(int clientUnitID) {
            return clientUnitID;
        }

        @Override
        public int layerToClient(int unitIDInLayer) {
            return unitIDInLayer;
        }
    }

    private static final class NoopFSConverter implements FSConverter {

        @Override
        public FileSystem layerToClient(int fsIdx) {
            return new LocalFileSystem();
        }

        @Override
        public int clientToLayer(FileSystem fileSystem) {
            return 0;
        }
    }

    private static final class UnsupportedFilePathConverter implements FilePathConverter {

        @Override
        public CharSequence layerToClient(int fileIdx) {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public int clientToLayer(CharSequence filePath) {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }
    }

}
