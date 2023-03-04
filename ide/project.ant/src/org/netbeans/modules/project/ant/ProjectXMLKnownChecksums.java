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

package org.netbeans.modules.project.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.CRC32;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.openide.util.NbPreferences;

/**
 * Caches CRC-32s of {@code project.xml} files known to be valid, since validation can be slow.
 * See bug #142680.
 */
public class ProjectXMLKnownChecksums {

    private static final String KEY = "knownValidProjectXmlCRC32s";
    private static final Logger LOG = Logger.getLogger(ProjectXMLKnownChecksums.class.getName());

    private final List<Long> knownHashes = new ArrayList<Long>();
    private StringBuilder newKnownHashes;

    /**
     * Loads currently known checksums.
     */
    public ProjectXMLKnownChecksums() {
        String knownHashesS = prefs().get(KEY, null);
        LOG.log(Level.FINE, "knownHashesS={0}", knownHashesS);
        if (knownHashesS != null) {
            for (String knownHash : knownHashesS.split(",")) {
                try {
                    knownHashes.add(Long.valueOf(knownHash, 16));
                } catch (NumberFormatException x) {/* forget it */}
            }
        }
    }

    /**
     * Checks whether a given project file has already been encountered.
     * If not, prepares to add its checksum to the list.
     * @param data content of a {@code project.xml} being read or written
     * @return true if it has already been encountered; false (see {@link #save}) if not
     */
    public boolean check(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        long hash = crc.getValue();
        if (knownHashes.contains(hash)) {
            return true;
        } else {
            newKnownHashes = new StringBuilder(Long.toString(hash, 16));
            for (int i = 0; i < knownHashes.size() && i < 100; i++) {
                newKnownHashes.append(',');
                newKnownHashes.append(Long.toString(knownHashes.get(i), 16));
            }
            return false;
        }
    }

    /**
     * If {@link #check} returned false, save the newly encountered checksum.
     * Uses an LRU strategy.
     */
    public void save() {
        LOG.log(Level.FINE, "knownHashesS:={0}", newKnownHashes);
        prefs().put(KEY, newKnownHashes.toString());
    }

    private Preferences prefs() {
        return NbPreferences.forModule(AntBasedProjectFactorySingleton.class);
    }

}
