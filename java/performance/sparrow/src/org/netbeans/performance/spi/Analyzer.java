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

