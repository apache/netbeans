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

package org.netbeans.modules.javascript2.debug.sources;

import java.io.IOException;
import java.net.URL;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin
 */
public final class SourceFilesCache {
    
    public static final String URL_PROTOCOL = "js-scripts"; // NOI18N
    
    private static final SourceFilesCache DEFAULT = new SourceFilesCache();
    
    private final SourceFS fs;
    
    private SourceFilesCache() {
        fs = new SourceFS();
    }
    
    public static SourceFilesCache getDefault() {
        return DEFAULT;
    }
    
    public URL getSourceFile(String name, int hash, String content) {
        return getSourceFile(name, hash, new StringContent(content));
    }
    
    public URL getSourceFile(String name, int hash, SourceContent content) {
        String path = Integer.toHexString(hash) + '/' + name;
        FileObject fo = fs.findResource(path);
        if (fo == null) {
            fo = fs.createFile(path, content);
        }
        return fo.toURL();
    }
    
    static final class StringContent implements SourceContent {
        
        private final String content;
        
        public StringContent(String content) {
            this.content = content;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public long getLength() {
            return content.length();
        }
    }

}
