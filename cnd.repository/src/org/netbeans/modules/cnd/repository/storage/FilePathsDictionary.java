/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.repository.util.IntToValueList;

/**
 * A list of all client FilePaths per Unit.
 *
 * filePathID (idx of the array) -- clientFilePaths
 *
 */
/* package */ final class FilePathsDictionary implements Persistent, SelfPersistent{
    private static final Logger LOG = Logger.getLogger("repository.support.filecreate.logger"); //NOI18N

    static final String WRONG_PATH = "<WRONG FILE>"; // NOI18N
    private final List<CharSequence> paths;
    private final Map<CharSequence, Integer> map = new HashMap<CharSequence, Integer>();
    private final Object lock = new Object();

    FilePathsDictionary(final List<CharSequence> initData) {
        if (initData != null) {
            paths = new ArrayList<CharSequence>(initData);
            int idx = 0;
            for (CharSequence path : initData) {
                map.put(path, idx++);
            }
        } else {
            paths = new ArrayList<CharSequence>();
        }
    }

    CharSequence getFilePath(final int fileIdx) {
        synchronized (lock) {
            if (fileIdx >= paths.size()) {
                return WRONG_PATH;
            } else {
                return paths.get(fileIdx);
            }
        }
    }    
    
    int size() {
        synchronized (lock) {
            return paths.size();
        }
    }

    int getFileID(final CharSequence filePath, int clientShortUnitID) {
        synchronized (lock) {
            Integer idx = map.get(filePath);
            if (idx == null) {
                int new_idx = paths.size();
                paths.add(filePath);
                map.put(filePath, new_idx);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Create index {0}/{1}={2}", new Object[]{new_idx, clientShortUnitID, filePath}); //NOI18N
                }
                return new_idx;
            } else {
                return idx.intValue();
            }
        }
    }
        

    List<CharSequence> toList() {
        synchronized (lock) {
            return Collections.unmodifiableList(new ArrayList<CharSequence>(paths));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n[clientFilePathID <-> clientFilePaths]\n"); // NOI18N
        int idx = 0;
        for (CharSequence path : toList()) {
            sb.append(idx++).append(" => ").append(path.toString()).append("\n"); // NOI18N
        }
        return sb.toString();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        IntToValueList<CharSequence> list = IntToValueList.<CharSequence>createEmpty("traceName");//NOI18N
        int i = 0;
        final List<CharSequence> toList = toList();
        for (CharSequence file : toList) {
            list.set(i++, file);
        }
        list.write(output);
    }
}
