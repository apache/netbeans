/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.updatecenters.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;


/** Modifies the etc/netbeans.conf if necessary.
 * 
 * @author  Jaroslav Tulach
 */
@ServiceProvider(service=AutoupdateClusterCreator.class)
public final class NetBeansClusterCreator extends AutoupdateClusterCreator {
    protected @Override File findCluster(String clusterName) {
        AtomicReference<File> parent = new AtomicReference<>();
        File conf = findConf(parent, new ArrayList<>());
        return conf != null && conf.isFile() && canWrite (conf) ? new File(parent.get(), clusterName) : null;
    }
    
    private static File findConf(AtomicReference<File> parent, List<? super File> clusters) {
        String nbdirs = System.getProperty("netbeans.dirs");
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator); // NOI18N
            while (tok.hasMoreElements()) {
                File cluster = new File(tok.nextToken());
                clusters.add(cluster);
                if (!cluster.exists()) {
                    continue;
                }
                if (parent.get() == null) {
                    parent.set(cluster.getParentFile());
                }
                if (!parent.get().equals(cluster.getParentFile())) {
                    // we can handle only case when all clusters are in
                    // the same directory these days
                    return null;
                }
            }
        }
        
        return new File(new File(parent.get(), "etc"), "netbeans.clusters");
    }
    
    protected @Override File[] registerCluster(String clusterName, File cluster) throws IOException {
        AtomicReference<File> parent = new AtomicReference<>();
        List<File> clusters = new ArrayList<>();
        File conf = findConf(parent, clusters);
        assert conf != null;
        clusters.add(cluster);
        Properties p = new Properties();
        try(InputStream is = new FileInputStream(conf)) {
            p.load(is);
        }
        if (!p.containsKey(clusterName)) {
            try (OutputStream os = new FileOutputStream(conf, true)) {
                os.write('\n');
                os.write(clusterName.getBytes());
                os.write('\n');
            }
        }
        return clusters.toArray(new File[0]);
    }
    
    public static boolean canWrite (File f) {
        // workaround the bug: https://bugs.openjdk.org/browse/JDK-4420020
        if (Utilities.isWindows ()) {
            try (FileWriter fw = new FileWriter(f, true)) {
                Logger.getLogger(NetBeansClusterCreator.class.getName()).log(Level.FINE, "{0} has write permission", f);
                return true;
            } catch (IOException ioe) {
                Logger.getLogger (NetBeansClusterCreator.class.getName ()).log (Level.FINE, f + " has no write permission", ioe);
                return false;
            }
        } else {
            return f.canWrite ();
        }
    }
    
}
