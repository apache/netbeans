/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.lexer.nbbridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vita
 */
@MimeLocation(subfolderName="languagesEmbeddingMap", instanceProviderClass=MimeLookupFolderInfo.class)
public class MimeLookupFolderInfo implements InstanceProvider {
    
    private static final Logger LOG = Logger.getLogger(MimeLookupFolderInfo.class.getName());
    
    public MimeLookupFolderInfo() {
    }

    public Object createInstance(List fileObjectList) {
        HashMap<String, LanguageEmbedding<?>> map
                = new HashMap<String, LanguageEmbedding<?>>();
        
        for(Object o : fileObjectList) {
            assert o instanceof FileObject : "fileObjectList should contain FileObjects and not " + o; //NOI18N
            
            FileObject f = (FileObject) o;
            try {
                Object [] info = parseFile(f);
                String mimeType = (String) info[0];
                int startSkipLength = (Integer) info[1];
                int endSkipLength = (Integer) info[2];
                
                if (isMimeTypeValid(mimeType)) {
                    Language<?> language = LanguageManager.getInstance().findLanguage(mimeType);
                    if (language != null) {
                        map.put(f.getName(), LanguageEmbedding.create(language, startSkipLength, endSkipLength));
                    } else {
                        LOG.warning("Can't find Language for mime type '" + mimeType + "', ignoring."); //NOI18N
                    }
                } else {
                    LOG.log(Level.WARNING, "Ignoring invalid mime type '" + mimeType + "' from: " + f.getPath()); //NOI18N
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Can't read language embedding definition from: " + f.getPath()); //NOI18N
            }
        }
        
        return new LanguagesEmbeddingMap(map);
    }
    
    private boolean isMimeTypeValid(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        int slashIndex = mimeType.indexOf('/'); //NOI18N
        if (slashIndex == -1) { // no slash
            return false;
        }
        if (mimeType.indexOf('/', slashIndex + 1) != -1) { //NOI18N
            return false;
        }
        return true;
    }
    
    private Object [] parseFile(FileObject f) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(f.getInputStream()));
        try {
            String line;
            
            while (null != (line = r.readLine())) {
                line.trim();
                if (line.length() != 0) {
                    String [] parts = line.split(","); //NOI18N
                    return new Object [] { 
                        parts[0], 
                        parts.length > 1 ? toInt(parts[1], "Ignoring invalid start-skip-length ''{0}'' in " + f.getPath()) : 0, //NOI18N
                        parts.length > 2 ? toInt(parts[2], "Ignoring invalid end-skip-length ''{0}'' in " + f.getPath()) : 0 //NOI18N
                    };
                }
            }
            
            return null;
        } finally {
            r.close();
        }
    }
    
    private int toInt(String s, String errorMsg) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, MessageFormat.format(errorMsg, s), e);
            return 0;
        }
    }
    
}
