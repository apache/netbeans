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
package org.netbeans.modules.cnd.repository.storage;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.disk.RepositoryImplUtil;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.repository.util.IntToValueList;

/**
 * The factory to write/read FilePathsDictionary object
 */
public class FilePathsDictionaryPersistentFactory implements PersistentFactory {

    private static final FilePathsDictionaryPersistentFactory instance = new FilePathsDictionaryPersistentFactory();

    private FilePathsDictionaryPersistentFactory() {

    }

    /*package*/ static FilePathsDictionaryPersistentFactory instance() {
        return instance;
    }

    @Override
    public void write(RepositoryDataOutput out, Persistent obj) throws IOException {
        SelfPersistent persistentObj = (SelfPersistent) obj;
        persistentObj.write(out);
    }

    @Override
    public Persistent read(RepositoryDataInput in) throws IOException {
        //read FilePathsDictionary
        IntToValueList<CharSequence> list = IntToValueList.<CharSequence>createFromStream(in, "trace", IntToValueList.CHAR_SEQUENCE_FACTORY); //NOI18N
        FilePathsDictionary obj = new FilePathsDictionary(list.getTable());
        return obj;
    }

    public static String getFilePathsDictionaryKeyFileName () throws IOException {
        FilePathsDictionaryKey key = new FilePathsDictionaryKey(0);
        return RepositoryImplUtil.getKeyFileName(key);
    }
}
