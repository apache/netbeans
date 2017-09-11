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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
        AtomicReference<File> parent = new AtomicReference<File>();
        File conf = findConf(parent, new ArrayList<File>());
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
        AtomicReference<File> parent = new AtomicReference<File>();
        List<File> clusters = new ArrayList<File>();
        File conf = findConf(parent, clusters);
        assert conf != null;
        clusters.add(cluster);
        Properties p = new Properties();
        InputStream is = new FileInputStream(conf);
        try{
            p.load(is);
        } finally {
            is.close();
        }
        if (!p.keySet().contains(clusterName)) {         
            OutputStream os = new FileOutputStream(conf, true);
            try {
                os.write('\n');
                os.write(clusterName.getBytes());
                os.write('\n');
            } finally {
                os.close();
            }
        }
        return clusters.toArray(new File[0]);
    }
    
    public static boolean canWrite (File f) {
        // workaround the bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
        if (Utilities.isWindows ()) {
            FileWriter fw = null;
            try {
                fw = new FileWriter (f, true);
                Logger.getLogger(NetBeansClusterCreator.class.getName()).log(Level.FINE, "{0} has write permission", f);
            } catch (IOException ioe) {
                // just check of write permission
                Logger.getLogger (NetBeansClusterCreator.class.getName ()).log (Level.FINE, f + " has no write permission", ioe);
                return false;
            } finally {
                try {
                    if (fw != null) {
                        fw.close ();
                    }
                } catch (IOException ex) {
                    Logger.getLogger (NetBeansClusterCreator.class.getName ()).log (Level.INFO, ex.getLocalizedMessage (), ex);
                }
            }
            return true;
        } else {
            return f.canWrite ();
        }
    }
    
}
