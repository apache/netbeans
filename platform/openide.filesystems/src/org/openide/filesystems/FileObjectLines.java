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
package org.openide.filesystems;

import java.io.IOException;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.ListIterator;

final class FileObjectLines extends AbstractSequentialList<String> {
    private final FileObjectLineIterator ready;
    final FileObject fo;

    public FileObjectLines(String encoding, FileObject fo) throws IOException {
        super();
        this.fo = fo;
        this.ready = new FileObjectLineIterator(this, encoding);
    }

    public synchronized ListIterator<String> listIterator(int position) {
        FileObjectLineIterator ret = ready.cloneIterator();
        while (position-- > 0) {
            ret.next();
        }
        return ret;
    }

    public synchronized int size() {
        int cnt = 0;
        Iterator<String> it = listIterator();
        while (it.hasNext()) {
            it.next();
            cnt++;
        }
        return cnt;
    }
}
