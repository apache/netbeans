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

package org.netbeans.modules.web.jspcompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;


/**
 * This class reads SMAP information from files.
 * @author  mg116726
 */
public class SmapFileReader implements SmapReader {

    private File file;

    public SmapFileReader(java.io.File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        if (file != null) return file.toString();
        return null;
    }

    public String readSmap() {
        if (file != null) {
            try {
                FileReader fr = new FileReader(file);
                LineNumberReader lnr = new LineNumberReader(fr);
                try {
                    String line = "";
                    String out = "";
                    while ((line = lnr.readLine()) != null) {
                        out = out.concat(line);
                        out = out.concat("\n");
                    }
                    return out;
                } finally {
                    lnr.close();
                }
            } catch (FileNotFoundException fne) {
                return null;
            } catch (IOException ioe) {
                return null;
            }
        }
        return null;
    }

}
