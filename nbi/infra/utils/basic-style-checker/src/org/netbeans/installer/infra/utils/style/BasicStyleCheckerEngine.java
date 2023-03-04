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

package org.netbeans.installer.infra.utils.style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.infra.utils.style.checkers.Checker;
import org.netbeans.installer.infra.utils.style.checkers.LineLengthChecker;
import org.netbeans.installer.infra.utils.style.checkers.UnescapedStringChecker;

/**
 *
 * @author ks152834
 */
public class BasicStyleCheckerEngine {
    public static void main(String[] args) throws IOException {
        BasicStyleCheckerEngine engine = new BasicStyleCheckerEngine();
        
        for (String arg: args) {
            engine.check(new File(arg));
        }
    }
    
    private List<Checker> checkers;
    
    public BasicStyleCheckerEngine() {
        checkers = new LinkedList<Checker>();
        
        checkers.add(new LineLengthChecker());
        checkers.add(new UnescapedStringChecker());
    }
    
    public void check(
            final File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String line = null;
        for (int i = 1; (line = reader.readLine()) != null; i++) {
            String error = "";
            
            for (Checker checker: checkers) {
                if (checker.accept(file)) {
                    final String message = checker.check(line);
                    
                    if (message != null) {
                        error += "        " + message + "\n";
                    }
                }
            }
            
            if (!error.equals("")) {
                System.out.println("    Line " + i + ":");
                System.out.println(error);
            }
        }
        
        reader.close();
    }
}
