/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;

/**
 * Connects the editor with persistent snippets store. The store may reside 
 * in the project, or in the IDE's configuration, or both.
 * <p/>
 * An instance of PersistentSnippets can be obtained from 
 * @author sdedic
 */
public interface PersistentSnippets {
    /**
     * Returns a collection of FileObjects which represent snippets saved
     * in a form of a java class source file. The class name will give name of the 
     * snippet. The javadoc description will be presented as a short description.
     * 
     * @return FileObjects which store sources for saved stuff
     */
    public Collection<FileObject>   getSavedClasses(String folderName);
    
    /**
     * Creates a folder to store the class(es). If name exists, the existing name
     * will be returned. Pass {@code null} for the root folder
     * 
     * @param name 
     * @return folder to save classes into.
     */
    public FileObject               savedClassFolder(String name);
    
    /**
     * Saves the class into the storage under the given name
     * @param name
     * @param description
     * @param contents
     * @return 
     */
    public FileObject               saveClass(
                                        String name, 
                                        String description, 
                                        InputStream contents) throws IOException;
    
    public String                   getDescription(FileObject saved);
    public void                     setDescription(FileObject saved, String desc);
    
    public void addChangeListener(ChangeListener l);
    public void removeChangeListener(ChangeListener l);
    
    public boolean                  isValid();
    
    public Collection<FileObject>   startupSnippets(String runAction);
}
