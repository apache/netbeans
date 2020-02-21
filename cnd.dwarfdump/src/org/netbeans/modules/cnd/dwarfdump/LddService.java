/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
