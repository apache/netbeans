/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfukala@netbeans.org
 */
public class URLRetriever {
    
    private static final WeakHashMap<String, String> PAGES_CACHE = new WeakHashMap<>();
    
    public static String getURLContentAndCache(URL url) {
        //strip off the anchor url part
        String path = url.getPath();

        //try to load from cache
        String file_content = PAGES_CACHE.get(path);
        if (file_content == null) {
            try {
                ByteArrayOutputStream baos;
                try (InputStream is = url.openStream()) {
                    byte buffer[] = new byte[8096];
                    baos = new ByteArrayOutputStream();
                    int count = 0;
                    do {
                        count = is.read(buffer);
                        if (count > 0) {
                            baos.write(buffer, 0, count);
                        }
                    } while (count > 0);
                }
                file_content = baos.toString("UTF-8"); //NOI18N
                baos.close();
            } catch (java.io.IOException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Cannot read css help file.", e); //NOI18N
            }

            PAGES_CACHE.put(path, file_content);
        }
        
        return file_content;
    }
    
}
