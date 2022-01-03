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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface CsmProject extends CsmNamedElement, CsmValidable {

    CsmNamespace getGlobalNamespace();
    
    /*
     * Waits until each file of the project is parsed.
     * If all files file are already parsed, immediately returns.
     */
    void waitParse();
    
    /** Gets an object, which represents correspondent IDE project */
    Object getPlatformProject();
    
    /** Gets this project display name */
    String getDisplayName();

    /** Gets this project display name in HTML form */
    String getHtmlDisplayName();
    
    /** Gets this project file system */
    FileSystem getFileSystem();

    /**
     * Finds namespace by its qualified name
     *
     * TODO: what if different projects contain namespaces with equal FQN?
     * Now we assume that these namespaces are represented via different instances.
     * Probably this is not correct
     */
    CsmNamespace findNamespace( CharSequence qualifiedName );
    
    /**
     * Finds compound classifier (class or enum) by its qualified name
     */
    CsmClassifier findClassifier(CharSequence qualifiedName);

    /**
     * Finds all compound classifier (class, struct, union, enum, typedef, classforward) by its qualified name
     */
    Collection<CsmClassifier> findClassifiers(CharSequence qualifiedName);

    /**
     * Finds all inheritances by its name
     */
    Collection<CsmInheritance> findInheritances(CharSequence name);
    
    /**
     * Finds declaration by its unique name
     */
    CsmDeclaration findDeclaration(CharSequence uniqueName);
    
    /**
     * Finds declarations by its unique name
     */
    Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName);

    /**
     * Finds file by object that can be absolute path or native file item or FSPath
     */
    public abstract CsmFile findFile(Object absolutePathOrNativeFileItem, boolean createIfPossible, boolean snapShot);

    /**
     * Gets the collection of source project files.
     */
    Collection<CsmFile> getSourceFiles();
    
    /**
     * Gets the collection of header project files.
     */
    Collection<CsmFile> getHeaderFiles();
    
    /**
     * Gets the collection of all (source and header) project files.
     */
    Collection<CsmFile> getAllFiles();
    
    /**
     * Gets the collection of libraries of the project.
     * Library can be either other project (which this project depends on)
     * or just a set of system include files
     * (most likely, the latter kind of project would correspond with 
     * one include directory, so there would be as many libraries as include 
     * path components)
     */
    Collection<CsmProject> getLibraries();
    
    /**
     * Returns true if the project is completely parsed
     * @param skipFile if null => all project files are checked;
     * if param is not null => project is stable even if skipFile not parsed     
     */
    boolean isStable(CsmFile skipFile);

    /**
     * return true for auto created projects for included standard headers.
     */
    boolean isArtificial();

}
