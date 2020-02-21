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
package org.netbeans.modules.cnd.repository.storage.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.impl.spi.FSConverter;
import org.netbeans.modules.cnd.repository.impl.spi.FilePathConverter;
import org.netbeans.modules.cnd.repository.impl.spi.LayerConvertersProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class RepositoryDataInputStream extends DataInputStream implements RepositoryDataInput {
    private final LayerConvertersProvider layersConverterProvider;
    private static final UnitsConverter noConversionUnits = new UnitsConverter() {
        @Override
        public int clientToLayer(int unitID) {
            return unitID;
        }

        @Override
        public int layerToClient(int unitID) {
            return unitID;
        }
    };
    private static final FSConverter noConversionsFS = new FSConverter() {
        @Override
        public FileSystem layerToClient(int readInt) {
            throw new InternalError();
        }

        @Override
        public int clientToLayer(FileSystem fileSystem) {
            throw new InternalError();
        }
    };
    private static final FilePathConverter noConversionsFilePath = new FilePathConverter() {

        @Override
        public CharSequence layerToClient(int fileIdx) {
            throw new InternalError();
        }

        @Override
        public int clientToLayer(CharSequence filePath) {
            throw new InternalError();
        }

    };

    public RepositoryDataInputStream(InputStream in, LayerConvertersProvider layersConverterProvider) {
        super(in);
        this.layersConverterProvider = layersConverterProvider;
    }

    /**
     * Creates an instance of RepositoryDataInputStream that does not perform
     * any conversions
     *
     * @param in
     */
    public RepositoryDataInputStream(DataInputStream in) {
        this(in, LayerConvertersProvider.getReadInstance(noConversionUnits, noConversionsFS, noConversionsFilePath));
    }

    @Override
    public CharSequence readCharSequenceUTF() throws IOException {
        return UTF.readCharSequenceUTF(this);
    }

    @Override
    public int readUnitId() throws IOException {
        int rawData = readInt();
        UnitsConverter unitIDConverter = layersConverterProvider.getReadUnitsConverter();
        final int clientUnitID = unitIDConverter == null ? rawData : unitIDConverter.layerToClient(rawData);
        CndUtils.assertTrue(rawData > -1, "Impossible on disk unit id: ", rawData); //NOI18N
        return clientUnitID;
    }

    @Override
    public FileSystem readFileSystem() throws IOException {
        FSConverter fsConverter = layersConverterProvider.getReadFSConverter();
        final int fsIdx = readInt();
        final FileSystem fs = fsConverter.layerToClient(fsIdx);
        if (fs == null) {
            // I don't like this null check very much; but without it,
            // once fileSystem was read or written as null, it will be null forever,
            // code assistance not functional at all and even IDE restart doesn't help
            // see NPEs #248225, #251527, #247726
            // the assertion is moved to Storage.FSReadConverterImpl.layerToClient
            throw new IOException("Can not restore file system from persistence", new NullPointerException()); //NOI18N            
        }
        return fs;
    }

    @Override
    public CharSequence readFilePath() throws IOException {
        FilePathConverter pathConverter = layersConverterProvider.getReadFilePathConverter();
        return FilePathCache.getManager().getString(pathConverter.layerToClient(readInt()));
    }

    @Override
    public CharSequence readFilePathForFileSystem(FileSystem fs) throws IOException {
        // for now we don't distinguish path dictionaries, but could in future
        // i.e. when system library is moved from local to remote fs
        return readFilePath();
    }
}
