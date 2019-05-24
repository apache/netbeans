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
package org.netbeans.modules.java.openjdk.jtreg;

import java.io.File;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.java.openjdk.project.JDKProject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

/**
 *
 * @author Sarvesh Kesharwani
 */
@ServiceProvider(service = ActionProvider.class)
public class SingleJavaSourceDebugActionProvider implements ActionProvider {
    
    private FileObject fileObject;
    
    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_DEBUG_SINGLE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        InputOutput io = IOProvider.getDefault().getIO("Opening Debugger Port", false);
        JPDAStart start = new JPDAStart(io, "debug.single");
        try {
            FileObject fileObject = getJavaFileWithoutProjectFromLookup(context);
            File classFile = new File(fileObject.getParent().getPath() + File.separator + fileObject.getName() + ".class");
            if (classFile.exists()) {
                classFile.delete();
            }
            File javacPath = new File(new File(new File(System.getProperty("java.home")), "bin"), "javac");
            Runtime.getRuntime().exec("\"" + javacPath.getAbsolutePath() + "\" " + "\"" + fileObject.getPath() + "\"");
            long startTime = System.currentTimeMillis();
            long fintime = startTime + 6000;
            while (!classFile.exists() && fintime < System.currentTimeMillis()) {
                //
            }
            if (classFile.exists()) {
                this.fileObject = fileObject;
                start.execute(new JDKProject(fileObject.getParent(), null, null));
            }
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return command.equalsIgnoreCase(ActionProvider.COMMAND_DEBUG_SINGLE);
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }
    
    private FileObject getJavaFileWithoutProjectFromLookup(Lookup lookup) {
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if (p == null && fObj.getExt().equalsIgnoreCase("java")) {
                return fObj;
            }
        }
        return null;
    }

}
