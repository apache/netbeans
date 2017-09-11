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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Michal Zlamal
 */
public class GetModuleName extends Task {
    String name = null;
    File root = null;

    // XXX this is a lousy attr name; conventional for such attrs to
    // end in 'property' so you realize they refer to a property name
    public void setName (String name) {
        this.name = name;
    }

    /** Root directory of the whole project - ${nb_all} */
    public void setRoot( File root ) {
        this.root = root;
    }
     
    public void execute() throws BuildException {
        if (name == null) 
            throw new BuildException("You must set the property name, where to store the module name", this.getLocation());
        if (root == null)
            throw new BuildException("You must set the root dir", this.getLocation());
        try {
            File dir = this.getProject ().getBaseDir ();
            if (dir.toString ().endsWith (java.io.File.separatorChar + "test")) {
                // when looking for base dir for tests
                dir = dir.getParentFile ();
            }
            String rootdir = root.getCanonicalPath();
            StringBuffer modulename = new StringBuffer ();
            while (dir != null) {
                if (dir.getCanonicalPath ().equals (rootdir)) {
                    break;
                }
                if (modulename.length () > 0) {
                    modulename.insert (0, '/');
                }
                modulename.insert (0, dir.getName ());
                dir = dir.getParentFile ();
            }
            
            //log("Basedir: " + basedir + " rootdir: " + rootdir);
            if (dir == null) {
                throw new BuildException("This module (" + this.getProject().getBaseDir() + ") is on different path than the root dir", this.getLocation());
            }
            this.getProject().setProperty(name, modulename.toString()); // XXX should be setNewProperty, when that is possible
        }
        catch (IOException ex) {
            throw new BuildException("Root dir or module's base dir wasn't recognized", ex, this.getLocation());
        }
    }
    
}
