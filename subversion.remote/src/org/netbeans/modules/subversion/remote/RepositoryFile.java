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
package org.netbeans.modules.subversion.remote;

import java.net.MalformedURLException;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class RepositoryFile {

    public static final String[] ROOT_FOLDER_PATH = new String[0];

    private final SVNUrl repositoryUrl;
    private final SVNRevision revision;
    private SVNUrl fileUrl;
    private String[] pathSegments;
    private String path;    
    private String toString = null;    
    private boolean repositoryRoot;

    private String name;
    private final FileSystem fileSystem;
    
    public RepositoryFile(FileSystem fileSystem, SVNUrl repositoryUrl, SVNRevision revision) {
        assert repositoryUrl != null;        
        assert revision != null;
        
        this.repositoryUrl = repositoryUrl;
        this.revision = revision;
        repositoryRoot = true;
        this.fileSystem = fileSystem;
    }
        
    public RepositoryFile(FileSystem fileSystem, SVNUrl repositoryUrl, SVNUrl fileUrl, SVNRevision revision) {       
        this(fileSystem, repositoryUrl, revision);
        this.fileUrl = fileUrl;        
        repositoryRoot = fileUrl == null;   
        
        if(!repositoryRoot) {            
            String[] fileUrlSegments = fileUrl.getPathSegments();
            int fileSegmentsLength = fileUrlSegments.length;
            int repositorySegmentsLength = repositoryUrl.getPathSegments().length;
            pathSegments = new String[fileSegmentsLength - repositorySegmentsLength];
            StringBuilder sb = new StringBuilder();
            for (int i = repositorySegmentsLength; i < fileSegmentsLength; i++) {
                pathSegments[i-repositorySegmentsLength] = fileUrlSegments[i];
                sb.append(fileUrlSegments[i]);
                if(i-repositorySegmentsLength < pathSegments.length-1) {
                    sb.append("/"); // NOI18N
                }
            }    
            path = sb.toString();
        }                
    }

    public RepositoryFile(FileSystem fileSystem, SVNUrl repositoryUrl, String[] pathSegments, SVNRevision revision) throws MalformedURLException {
        this(fileSystem, repositoryUrl, revision);
        this.pathSegments = pathSegments;    
        repositoryRoot = pathSegments == null;        
        
        if(!repositoryRoot) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pathSegments.length; i++) {
                sb.append(pathSegments[i]);
                if(i<pathSegments.length-1) {
                sb.append("/"); // NOI18N
                }            
            }
            path = sb.toString();        
            fileUrl = repositoryUrl.appendPath(path);        
        }
    }
    
    public RepositoryFile(FileSystem fileSystem, SVNUrl repositoryUrl, String path, SVNRevision revision) throws MalformedURLException {
        this(fileSystem, repositoryUrl, revision);
        this.path = path;
        repositoryRoot = path == null;        
        
        if(!repositoryRoot) {                        
            fileUrl = repositoryUrl.appendPath(path);        
            pathSegments = path.split("/"); // NOI18N
        }
    }
    
    public FileSystem getFileSystem() {
        return fileSystem;
    }
    
    public SVNUrl getRepositoryUrl() {
        return repositoryUrl;
    }

    public SVNRevision getRevision() {
        return revision;
    }

    public SVNUrl getFileUrl() {
        if(isRepositoryRoot()) {
            return getRepositoryUrl();
        }
        return fileUrl;
    }

    public String[] getPathSegments() {
        if(isRepositoryRoot()) {
            return ROOT_FOLDER_PATH;
        }        
        return pathSegments;
    }

    public String getPath() {
        if(isRepositoryRoot()) { 
            return ""; // NOI18N
        }        
        return path;
    }
    
    public boolean isRepositoryRoot() {
        return repositoryRoot;
    }

    public String getName() {
        if(name == null) {
            if(isRepositoryRoot()) {
                String url = getRepositoryUrl().toString();
                int idx = url.indexOf("://"); // NOI18N
                if(idx >= 0) {
                    url = url.substring(idx+3);
                }
                return  url;
            } else {
                return  getFileUrl().getLastPathSegment();
            }    
        }
        return name;
    }
    
    @Override
    public String toString() {
        if(toString == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(fileUrl);
            sb.append("@"); // NOI18N
            sb.append(revision);    
            toString = sb.toString();
        }
        
        return toString;
    }

    public RepositoryFile appendPath(String path) {
        return new RepositoryFile(fileSystem, repositoryUrl, getFileUrl().appendPath(path), revision);
    }       

    public RepositoryFile replaceLastSegment(String segment, int level) {
        assert segment != null && !segment.equals(""); // NOI18N
        assert level > -1 && level < fileUrl.getPathSegments().length;
        assert !isRepositoryRoot(); // can't do this 

        String fileUrlString = fileUrl.toString();
        int fromIdx = fileUrlString.lastIndexOf('/');
        int toIdx = fileUrlString.length() - 1;
        for (int i = 0; i < level; i++) {
            toIdx = fromIdx - 1;
            fromIdx = fileUrlString.lastIndexOf('/', fromIdx - 1);
        }        
        
        assert !(fromIdx < repositoryUrl.toString().length());
        assert toIdx >= fromIdx && toIdx < fileUrlString.length();

        SVNUrl newUrl = null;
        try {
            newUrl = new SVNUrl(fileUrlString.substring(0, fromIdx + 1) + segment + fileUrlString.substring(toIdx + 1));
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, ex.getMessage(), ex);   // should not happen            
        }                        
        return new RepositoryFile(fileSystem, repositoryUrl, newUrl, revision);
    }

    public RepositoryFile  removeLastSegment() {                
        assert !isRepositoryRoot(); // can't do this 
        
        String fileUrlStrint = fileUrl.toString();
        int idx = fileUrlStrint.lastIndexOf('/');

        assert !(idx < repositoryUrl.toString().length());

        SVNUrl newUrl = null;
        try {
            newUrl = new SVNUrl(fileUrlStrint.substring(0, idx));
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, ex.getMessage(), ex);  // should not happen
        }                        
        return new RepositoryFile(fileSystem, repositoryUrl, newUrl, revision);
    }         
    
}
