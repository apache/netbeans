/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.masterfs.filebasedfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;

/**
 * @author  rm111737
 */
public class FileBasedFileSystemWithExtensionsTest extends FileBasedFileSystemTest {
    /** Creates new MasterFileSystemTest */
    public FileBasedFileSystemWithExtensionsTest(Test test) {
        super(test);
        ProvidedExtensionsTest.ProvidedExtensionsImpl.setImplsCopyRetVal(true);
        ProvidedExtensionsTest.ProvidedExtensionsImpl.setImplsMoveRetVal(true);
        ProvidedExtensionsTest.ProvidedExtensionsImpl.setImplsRenameRetVal(true);
        ProvidedExtensionsTest.ProvidedExtensionsImpl.setImplsDeleteRetVal(true);
    }

    @Override
    protected void setServices(Class<?>... services) {
        List<Class<?>> arr = new ArrayList<Class<?>>();
        arr.addAll(Arrays.asList(services));
        arr.add(FileBasedURLMapper.class);
        arr.add(ProvidedExtensionsTest.ProvidedExtensionsImpl.class);
        arr.add(ProvidedExtensionsTest.AnnotationProviderImpl.class);
        MockServices.setServices(arr.toArray(new Class<?>[0]));
    }

    public static Test suite() {
        return new FileBasedFileSystemWithExtensionsTest(
            FileBasedFileSystemTest.suite(true)
        );
    }
}
