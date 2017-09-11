/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;

/**
 *
 * @author vita
 */
public final class DefaultPathRecognizer extends PathRecognizer {

    // ------------------------------------------------------------------------
    // PathRecognizer implementation
    // ------------------------------------------------------------------------

    @Override
    public Set<String> getSourcePathIds() {
        return sourcePathIds;
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        return binaryLibraryPathIds;
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return libraryPathIds;
    }

    @Override
    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    // ------------------------------------------------------------------------
    // Public implementation
    // ------------------------------------------------------------------------

    public static PathRecognizer createInstance(Map fileAttributes) {
        
        // path ids
        Set<String> sourcePathIds = readIdsAttribute(fileAttributes, "sourcePathIds"); //NOI18N
        Set<String> libraryPathIds = readIdsAttribute(fileAttributes, "libraryPathIds"); //NOI18N
        Set<String> binaryLibraryPathIds = readIdsAttribute(fileAttributes, "binaryLibraryPathIds"); //NOI18N

        // mime types
        Set<String> mimeTypes = new HashSet<String>();
        Object mts = fileAttributes.get("mimeTypes"); //NOI18N
        if (mts instanceof String) {
            String [] arr = ((String) mts).split(","); //NOI18N
            for(String mt : arr) {
                mt = mt.trim();
                if (mt.length() > 0 && MimePath.validate(mt)) {
                    mimeTypes.add(mt);
                } else {
                    LOG.log(Level.WARNING, "Invalid mimetype {0}, ignoring.", mt); //NOI18N
                }
            }
        }

        return new DefaultPathRecognizer(sourcePathIds, libraryPathIds, binaryLibraryPathIds, Collections.unmodifiableSet(mimeTypes));
    }

    @Override
    public String toString() {
        return super.toString()
                + "[sourcePathIds=" + sourcePathIds //NOI18N
                + ", libraryPathIds=" + libraryPathIds //NOI18N
                + ", binaryLibraryPathIds=" + binaryLibraryPathIds //NOI18N
                + ", mimeTypes=" + mimeTypes; //NOI18N
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultPathRecognizer.class.getName());

    private final Set<String> sourcePathIds;
    private final Set<String> libraryPathIds;
    private final Set<String> binaryLibraryPathIds;
    private final Set<String> mimeTypes;

    private DefaultPathRecognizer(Set<String> sourcePathIds, Set<String> libraryPathIds, Set<String> binaryLibraryPathIds, Set<String> mimeTypes) {
        this.sourcePathIds = sourcePathIds;
        this.libraryPathIds = libraryPathIds;
        this.binaryLibraryPathIds = binaryLibraryPathIds;
        this.mimeTypes = mimeTypes;
    }

    private static Set<String> readIdsAttribute(Map fileAttributes, String attributeName) {
        Set<String> ids = new HashSet<String>();
        
        Object attributeValue = fileAttributes.get(attributeName); //NOI18N
        if (attributeValue instanceof String) {
            String [] varr = ((String) attributeValue).split(","); //NOI18N
            for(String v : varr) {
                v = v.trim();
                if (v.equals("ANY")) { //NOI18N
                    ids = null;
                    break;
                } else if (v.length() > 0) {
                    ids.add(v);
                } else {
                    LOG.log(Level.WARNING, "Empty IDs are not alowed in {0} attribute, ignoring.", attributeName); //NOI18N
                }
            }
        } else {
            if (attributeValue != null) {
                LOG.log(Level.WARNING, "Invalid {0} attribute value, expecting java.lang.String, but got {1}, {2}", //NOI18N
                        new Object [] { attributeName, attributeValue, attributeValue == null ? null : attributeValue.getClass()});
            }
            
            ids = Collections.<String>emptySet();
        }

        return ids == null ? null : Collections.unmodifiableSet(ids);
    }
}
