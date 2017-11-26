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

package org.netbeans.modules.java.hints.declarative;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.hints.declarative.Condition.Instanceof;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {

    public static String readFile(FileObject file) {
        StringBuilder sb = new StringBuilder();

        Reader r = null;

        try {
            r = new InputStreamReader(file.getInputStream(), "UTF-8");

            int read;

            while ((read = r.read()) != (-1)) {
                sb.append((char) read);
            }

            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, ex);
            return null;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }
    }
    
    public static Map<String, String> conditions2Constraints(List<Condition> conditions) {
        Map<String, String> constraints = new HashMap<String, String>();

        for (Condition c : conditions) {
            if (!(c instanceof Instanceof) || c.not)
                continue;

            Instanceof i = (Instanceof) c;

            constraints.put(i.variable, i.constraint.trim()); //TODO: may i.constraint contain comments? if so, they need to be removed
        }
        
        return constraints;
    }

}
