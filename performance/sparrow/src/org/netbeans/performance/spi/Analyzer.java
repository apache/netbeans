/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Analyzer.java
 *
 * Created on October 9, 2002, 10:18 PM
 */

package org.netbeans.performance.spi;
import java.io.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 *
 * @author  Tim Boudreau
 */
public abstract class Analyzer extends Task {
    protected String datafile=null;
    protected String outfile=null;
    protected String outdir=null;
    protected String datadir=null;
    /**Deserialize a DataAggregation instance.  */
    protected DataAggregation getData() throws Exception {
        File f = new File (datafile);
        InputStream is = new FileInputStream (f);
        ObjectInputStream ois = new ObjectInputStream (is);
        DataAggregation result = (DataAggregation) ois.readObject();
        return result;
    }

    public void execute() throws BuildException {
        if ((datafile==null && datadir==null) || (outfile==null && outdir==null)) throw new BuildException("Missing datafile or outfile name to build report");
        System.out.println(analyze());
    }    
    
    /**Perform whatever analysis tasks this Analyzer needs to perform
     * (presumably writing reports to files or building charts).  The
     * returned String can be the location it wrote whatever it wrote
     * to, if appropriate.
     */
    public abstract String analyze () throws BuildException;
    
    public void setDataFile (String datafile) {
        this.datafile = datafile;
    }
    
    public void setOutFile (String outfile) {
        this.outfile = outfile;
    }
    
    public void setOutDir (String outdir) {
        this.outdir = outdir;
    }
    
    public void setDataDir (String datadir) {
        this.datadir = datadir;
    }
}

