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

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;
import org.netbeans.installer.infra.build.ant.utils.FileEntry;

/**
 * This class is an ant task which is capable of properly packaging a directory into
 * an archive. In addition to simply jarring the directory, it pack200-packages the
 * jar files present in the source directory and composes a files list which
 * contains some useful metadata about the files in the archive, such as the
 * checksums, sizes, etc.
 *
 * @author Kirill Sorokin
 */
public class Package extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The target archive file.
     */
    private File file;
    
    /**
     * The sources directory.
     */
    private File directory;
    
    /**
     * List of {@link FileEntry} objects, which represent the metadata for the files
     * which should be included into the archive.
     */
    private List<FileEntry> entries;
    
    private long directoriesCount;
    private long filesCount;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * Constructs a new instance of the {@link Package} task. It simply sets the
     * default values for the attributes.
     */
    public Package() {
        entries = new LinkedList<FileEntry>();
    }
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'file' property.
     *
     * @param path New value for the 'file' property.
     */
    public void setFile(final String path) {
        file = new File(path);
        if (!file.equals(file.getAbsoluteFile())) {
            file = new File(getProject().getBaseDir(), path);
        }
    }
    
    /**
     * Setter for the 'directory' property.
     *
     * @param path New value for the 'directory' property.
     */
    public void setDirectory(final String path) {
        directory = new File(path);
        if (!directory.equals(directory.getAbsoluteFile())) {
            directory = new File(getProject().getBaseDir(), path);
        }
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The source directory is recursively browsed, its files are
     * examined, packaged and added to the archive; some additional metadata is
     * calculated and then added to the files list.
     *
     * @throws org.apache.tools.ant.BuildException if an I/O error occurs.
     */
    public void execute() throws BuildException {
        Utils.setProject(getProject());
        
        JarOutputStream output = null;
        try {
            output = new JarOutputStream(new FileOutputStream(file));
            output.setLevel(9);
            
            log("browsing, packing, archiving directory " + directory.getCanonicalPath()); // NOI18N
            browse(directory.getCanonicalFile(),
                    output,
                    directory.getCanonicalPath().length());
            
            log("adding manifest and files list"); // NOI18N
            output.putNextEntry(new JarEntry(METAINF_ENTRY));
            
            output.putNextEntry(new JarEntry(MANIFEST_ENTRY));
            output.write("Manifest-Version: 1.0\n\n".getBytes("UTF-8")); // NOI18N
            
            output.putNextEntry(new JarEntry(FILES_LIST_ENTRY));
            OutputStreamWriter writer =
                    new OutputStreamWriter(output, "UTF-8"); // NOI18N
            
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // NOI18N
            writer.write("<files-list>\n"); // NOI18N
            for (FileEntry entry: entries) {
                writer.write("    <entry "); // NOI18N
                if (entry.isDirectory()) {
                    writer.write("type=\"directory\" "); // NOI18N
                    writer.write("empty=\"" + // NOI18N
                            entry.isEmpty() + "\" "); // NOI18N
                    writer.write("modified=\"" + // NOI18N
                            entry.getLastModified() + "\" "); // NOI18N
                    writer.write("permissions=\"" + // NOI18N
                            entry.getPermissions() + "\""); // NOI18N
                } else {
                    writer.write("type=\"file\" "); // NOI18N
                    writer.write("size=\"" + // NOI18N
                            entry.getSize() + "\" "); // NOI18N
                    writer.write("md5=\"" + // NOI18N
                            entry.getMd5() + "\" "); // NOI18N
                    writer.write("jar=\"" + // NOI18N
                            entry.isJarFile() + "\" "); // NOI18N
                    writer.write("packed=\"" + // NOI18N
                            entry.isPackedJarFile() + "\" "); // NOI18N
                    writer.write("signed=\"" + // NOI18N
                            entry.isSignedJarFile() + "\" "); // NOI18N
                    writer.write("modified=\"" + // NOI18N
                            entry.getLastModified() + "\" "); // NOI18N
                    writer.write("permissions=\"" + // NOI18N
                            entry.getPermissions() + "\""); // NOI18N
                }
                
                writer.write(">" + entry. // NOI18N
                        getName().
                        replace("&", "&amp;"). // NOI18N
                        replace("\'","&apos;"). //NOI18N
                        replace("\"","&quot;"). //NOI18N
                        replace("<", "&lt;"). // NOI18N
                        replace(">", "&gt;") + "</entry>\n"); // NOI18N
            }
            writer.write("</files-list>\n"); // NOI18N
            
            writer.flush();
            writer.close();
            
            output.flush();
            output.close();
            
            log("archived " + directoriesCount + // NOI18N
                    " directories and " + filesCount + " files"); // NOI18N
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    private String getShortPath(File file, final int offset) throws IOException {
        return file.getAbsolutePath().substring(offset + 1);
    }
    // private //////////////////////////////////////////////////////////////////////
    private void browse(
            final File parent,
            final JarOutputStream output,
            final int offset) throws IOException {
        FileInputStream fis = null;
        final List<File> toSkip = new LinkedList<File>();
        
        for (File child: parent.listFiles()) {
            if (toSkip.contains(child)) {
                log("    skipping " + getShortPath(child,offset)); // NOI18N
                continue;
            }
            
            log("    visiting " + getShortPath(child,offset)); // NOI18N
            
            final String path = child.getAbsolutePath();
            String name = path.substring(offset + 1).replace('\\', '/');    // NOMAGI
            
            FileEntry entry;
            JarEntry jarEntry;
            
            if (child.isDirectory()) {
                log("        archiving directory: " + name); // NOI18N
                
                name  = name + "/"; // NOI18N
                entry = new FileEntry(child, name);
                
                output.putNextEntry(new JarEntry(name));
                
                directoriesCount++;
                
                browse(child, output, offset);
            } else {
                entry = new FileEntry(child, name);
                log("        archiving file: " + name); // NOI18N
                jarEntry = new JarEntry(name);
                jarEntry.setTime(entry.getLastModified());
                jarEntry.setSize(entry.getSize());
                output.putNextEntry(jarEntry);
                
                fis = new FileInputStream(child);
                Utils.copy(fis, output);
                fis.close();
                
                filesCount++;
            }
            
            entries.add(entry);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String METAINF_ENTRY =
            "META-INF/"; // NOI18N
    private static final String FILES_LIST_ENTRY =
            "META-INF/files.list"; // NOI18N
    private static final String MANIFEST_ENTRY =
            "META-INF/manifest.mf"; // NOI18N
}
