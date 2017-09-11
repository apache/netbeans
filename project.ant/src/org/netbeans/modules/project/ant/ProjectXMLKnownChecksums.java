/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
