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
package org.netbeans.libs.git;

import java.net.URISyntaxException;
import org.eclipse.jgit.transport.URIish;

/**
 * Representation of a supported Git URI, that denotes a remote repository and
 * is used mainly in Git commands connecting to remote repositories.
 * An instance of this class is immutable meaning any setter method constructs 
 * a new instance but does not modify the original instance.
 * @author Tomas Stupka
 */
public final class GitURI {
    private URIish uri;
    
    private GitURI() {}
    
    // a private constructor with URIish uri causes:
    // cannot access org.eclipse.jgit.transport.URIish 
    // class file for org.eclipse.jgit.transport.URIish not found
    private GitURI create(URIish uri) {
        GitURI u = new GitURI();
        u.uri = uri;
        return u;
    }
    
    /**
     * Constructor creating an instance of a Git URI. In case the given uri string
     * has an unsupported format a URISyntaxException is thrown.
     * @param uriString string representation of a Git URI.
     * @throws URISyntaxException if the given string has unsupported format
     */
    public GitURI(String uriString) throws URISyntaxException {
        this.uri = new URIish(uriString);

        // WORKAROUND:
        // new URIish("https://foo.bar").getHost() returns null
        // new URIish("https://foo.bar").getPath() returns foo.bar
        // it should work instead like
        // new URIish("https://foo.bar/").getHost() returns foo.bar
        // new URIish("https://foo.bar/").getPath() returns null
        String scheme = uri.getScheme();
        if(scheme != null && 
           !scheme.startsWith("file:") && 
           uri.getHost() == null && 
           !uriString.endsWith("/")) 
        {
            uri = new URIish(uriString + "/");
        }
    }

    /**
     * Returns string representation that contains also username and password as plaintext.
     * @return full form (along with user credentials) of the Git URI
     */
    public String toPrivateString() {
        return uri.toPrivateString();
    }

    /**
     * Constructs a new instance with the given user name.
     * @param user user name
     * @return new instance that contains user as part of the credential area.
     */
    public GitURI setUser (String user) {
        return create(uri.setUser(user));
    }

    /**
     * Constructs a new instance with the given user name.
     * @param scheme new scheme
     * @return a duplicate instance of this with modified scheme part
     */
    public GitURI setScheme (String scheme) {
        return create(uri.setScheme(scheme));
    }

    /**
     * Constructs a new instance with the given port number.
     * @param port port number
     * @return a duplicate instance of this with modified port number.
     */
    public GitURI setPort (int port) {
        return create(uri.setPort(port));
    }

    /**
     * Constructs a new instance with the given path.
     * @param path path to a resource
     * @return a duplicate instance of this with modified path to a resource.
     */
    public GitURI setPath (String path) {
        return create(uri.setPath(path));
    }

    /**
     * Creates a new instance with the given new password.
     * @param password new password
     * @return a duplicate instance of this with a new password
     */
    public GitURI setPass (String password) {
        return create(uri.setPass(password));
    }

    /**
     * Creates a new instance with the given new host name.
     * @param host new host name
     * @return a duplicate instance of this with a new host name
     */
    public GitURI setHost (String host) {
        return create(uri.setHost(host));
    }

    /**
     * Returns <code>true</code> if this URI references a repository on another system.
     * @return true if this URI references a repository on another system.
     */
    public boolean isRemote() {
        return uri.isRemote();
    }

    /**
     * Returns the username part of the URI's credentials part.
     * @return username part of the URI's credentials part
     */
    public String getUser() {
        return uri.getUser();
    }

    /**
     * Returns the URI's scheme as a string.
     * @return URI scheme as a string
     */
    public String getScheme() {
        return uri.getScheme();
    }

    /**
     * Returns the port number specified by the URI.
     * @return the port number from the URI
     */
    public int getPort() {
        return uri.getPort();
    }

    /**
     * Returns the path on the host to the resource denoted by the URI.
     * @return the path from URI
     */
    public String getPath() {
        return uri.getPath();
    }

    /**
     * Returns the password part of the URI's credentials part.
     * @return the password part of the URI's credentials part.
     */
    public String getPass() {
        return uri.getPass();
    }

    /**
     * Returns the URI's host.
     * @return the host from the URI
     */
    public String getHost() {
        return uri.getHost();
    }

    /**
     * Returns string representation if the URI without the credentials part
     * @return readable representation of the URI with empty credentials.
     */
    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GitURI) {
            return uri.equals(((GitURI) o).uri);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
    
}
