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

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.codehaus.plexus.util.StringUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.api.FileUtilities;
import org.openide.util.NbPreferences;

/**
 * Utility to maintain sources and Javadoc associated with JARs outside the repo (#205649).
 * More or less deprecated since issue 215971 was implemented but useful for debugging maven plugins feature
 */
public class SourceJavadocByHash {

    private static Preferences node(boolean javadoc) {
        return NbPreferences.forModule(SourceJavadocByHash.class).node(javadoc ? "attachedJavadoc" : "attachedSource");
    }
    
    public static void register(@NonNull URL root, @NonNull File[] result, boolean javadoc) {
        StringBuilder sb = new StringBuilder();
        for (File res : result) {
            sb.append("||").append(res.getAbsolutePath());
        }
        node(javadoc).put(root.toString(), sb.substring(2));
    }    

    public static @CheckForNull File[] find(@NonNull URL root, boolean javadoc) {
        String k = root.toString();
        Preferences n = node(javadoc);
        String v = n.get(k, null);
        if (v == null) {
            return null;
        }
        String[] split = StringUtils.split(v, "||");
        List<File> toRet = new ArrayList<File>();
        for (String vv : split) {
            File f = FileUtilities.convertStringToFile(vv);
            if (f.isFile()) {
                toRet.add(f);
            } else {
                //what do we do when one of the possibly more files is gone?
                //in most cases we are dealing with exactly one file, so keep the
                //previous behaviour of removing it.
                n.remove(k);
            }
        }
        return toRet.toArray(new File[0]);
    }

    private SourceJavadocByHash() {}

}
