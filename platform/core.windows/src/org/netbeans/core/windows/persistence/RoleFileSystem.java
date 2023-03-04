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
package org.netbeans.core.windows.persistence;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;

/**
 *
 * @author stan
 */
public class RoleFileSystem extends MultiFileSystem {
    
    private final String roleName;
    
    public static FileSystem create( String roleName ) throws FileStateInvalidException {
        if( null == roleName )
            return FileUtil.getConfigRoot().getFileSystem();
        
        RoleFileSystem rfs = new RoleFileSystem( roleName );
        return new MultiFileSystem( new FileSystem[] {rfs, FileUtil.getConfigRoot().getFileSystem()} );
    }
    
    private RoleFileSystem( String roleName ) throws FileStateInvalidException {
        super( new FileSystem[] { FileUtil.getConfigRoot().getFileSystem() } );
        this.roleName = roleName;
    }

    @Override
    protected FileObject findResourceOn( FileSystem fs, String res ) {
        return super.findResourceOn( fs, convert(res) );
    }
    
    String convert( String path ) {
        if( path.startsWith( "Windows2/" ) ) {
            return "Windows2/Roles/" + roleName + path.substring( "Windows2".length() );
        } else {
            return path;
        }
    }
}
