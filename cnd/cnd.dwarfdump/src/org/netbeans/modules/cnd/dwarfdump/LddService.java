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
package org.netbeans.modules.cnd.dwarfdump;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;

/**
 *
 */
public class LddService {

    private LddService() {
    }

    public static void main(String[] args){
        if (args.length < 1) {
            System.err.println("Not enough parameters."); // NOI18N
            System.err.println("Usage:"); // NOI18N
            System.err.println("java -cp org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.LddService binaryFileName"); // NOI18N
            return;
        }
        try {
            dump(args[0],System.out);
        } catch (Throwable ex) {
            Dwarf.LOG.log(Level.INFO, "File "+args[0], ex);
        }
    }
    
    private static void dump(String objFileName, PrintStream out) throws IOException, Exception {
        SharedLibraries res = getPubNames(objFileName);
        if (res != null) {
            for(String dll: res.getDlls()) {
                out.println("Name:"+dll); //NOI18N
            }
            for(String path: res.getPaths()) {
                out.println("Path:"+path); //NOI18N
            }
        }
    }
    public static SharedLibraries getPubNames(BufferedReader out) throws IOException {
        SharedLibraries res = new SharedLibraries();
        String line;
        while ((line=out.readLine())!= null){
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("Name:")) { //NOI18N
                res.addDll(line.substring(5).trim());
            } else if (line.startsWith("Path:")) { //NOI18N
                res.addPath(line.substring(5).trim());
            }
        }
        return res;
    }
    
    public static SharedLibraries getPubNames(String objFileName) {
        SharedLibraries pubNames = null;
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            pubNames = dump.readPubNames();
        } catch (FileNotFoundException ex) {
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                Dwarf.LOG.log(Level.FINE, "File not found {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                Dwarf.LOG.log(Level.FINE, "Unsuported format of file {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } catch (Throwable ex) {
            Dwarf.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return pubNames;
    }

}
