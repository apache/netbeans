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
package org.netbeans.nbbuild;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.io.LineNumberReader;

/**
 *
 * @author skygo
 */
public class ParseGitBranch extends Task {

    /**
     * data to parse
     */
    private String data;

    public void setData(String data) {
        this.data = data;
    }

    /**
     * properties to set
     */
    private String propertyName;

    public void setProperty(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public void execute() throws BuildException {
        super.execute();
        Reader dataReader = new StringReader(data);
        String firstLine = null;
        String secondLine = null;
        try (LineNumberReader r = new LineNumberReader(dataReader);) {
            firstLine = r.readLine();
            secondLine = r.readLine();
        } catch (IOException ex) {
            throw new BuildException("Problem reading information for detached head");
        }
        //if (secondLine != null) {
        //    throw new BuildException("Problem parsing git information for detached head : too many line");
        //}
        if (secondLine != null || firstLine == null || firstLine.trim().isEmpty()) {
            // Assume master if PR or detached HEAD on Travis, etc.
            getProject().setProperty(propertyName, "master");
        } else {
            String[] splited = firstLine.trim().split(" ");
            long count = splited[0].chars().filter(ch -> ch == '/').count();
            if (count != 1) {
                throw new BuildException("Problem parsing git information" + count);
            } else {
                getProject().setProperty(propertyName, splited[0].split("/")[1]);
            }
        }
    }
}
