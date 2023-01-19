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

package org.netbeans.installer.infra.utils.newlinecorrecter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final File file = new File("D:/temp/nbi-build/build.sh");
        final String newline = "\n";
        
        final List<String> lines = new LinkedList<String>();

        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {
            String line2read;
            while ((line2read = reader.readLine()) != null) {
                lines.add(line2read);
            }
        }

        try (FileWriter fileWriter = new FileWriter(file);
             PrintWriter writer = new PrintWriter(fileWriter)) {
            for (String line2write : lines) {
                writer.write(line2write + newline);
            }
        }
    }
}
