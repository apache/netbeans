/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * CacheWriterTask.java
 *
 * Created on February 19, 2004, 12:07 AM
 */

package org.netbeans.imagecache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 *
 * @author  tim
 */
public class CacheWriterTask extends Task {
    private File outDir = null;
    private List paths = new ArrayList();
    private boolean clean = true;
    /** Creates a new instance of CacheWriterTask */
    public CacheWriterTask() {
    }

    public void setDir (File dir) {
        paths.add(new Path(getProject(), dir.toString()));
    }

    public void setOutdir (File dir) {
        this.outDir = dir;
    }

    public void addPath(Path fs) {
        paths.add (fs);
    }

    public void setClean (boolean clean) {
        this.clean = clean;
    }

    public void execute() throws BuildException {
        if (paths.isEmpty()) {
            throw new BuildException ("Source dir or fileset required to scan for images");
        }
        if (outDir == null) {
            throw new BuildException ("Output directory for cache file must be specified");
        }
        
        try {
            CacheWriter writer = new CacheWriter();
            writer.setDir(outDir.toString(), clean);

            Iterator it = paths.iterator();
            while (it.hasNext()) {
                Path curr = (Path) it.next();
                String[] dirs = curr.list();
                for (int i=0; i < dirs.length; i++) {
                    System.err.println("WriteDir " + dirs[i]);
                    writer.writeDir(dirs[i], true);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new BuildException (ioe.getMessage());
        }
    }
}
