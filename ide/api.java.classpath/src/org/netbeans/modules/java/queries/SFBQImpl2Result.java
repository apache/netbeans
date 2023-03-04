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

package org.netbeans.modules.java.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class SFBQImpl2Result implements SourceForBinaryQueryImplementation2.Result {
    
    private final SourceForBinaryQuery.Result delegate;
    
    public SFBQImpl2Result (final SourceForBinaryQuery.Result result) {
        assert result != null;
        this.delegate = result;
    }

    public boolean preferSources() {
        //Preserve the old behavior from 4.0 to 6.1, ignore sources inside archives
        final FileObject[] roots = this.delegate.getRoots();
        for (FileObject root : roots) {
            if (root == null) {
                //Issue #139894: SQBQ.Result.getRoots() contains null
                throw new NullPointerException("SFBQ.Result: "+delegate.getClass().getName() +" returned null in roots.");
            }
            if (FileUtil.getArchiveFile(root) != null) {
                return false;
            }
        }
        return true;
    }

    public FileObject[] getRoots () {
        return this.delegate.getRoots();
    }

    public void addChangeListener(ChangeListener l) {
        this.delegate.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        this.delegate.removeChangeListener(l);
    }
}
