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
     */
    public boolean isRemote() {
        return uri.isRemote();
    }

    /**
     * Returns the username part of the URI's credentials part.
     */
    public String getUser() {
        return uri.getUser();
    }

    /**
     * Returns the URI's scheme as a string.
     */
    public String getScheme() {
        return uri.getScheme();
    }

    /**
     * Returns the port number specified by the URI.
     */
    public int getPort() {
        return uri.getPort();
    }

    /**
     * Returns the path on the host to the resource denoted by the URI.
     */
    public String getPath() {
        return uri.getPath();
    }

    /**
     * Returns the password part of the URI's credentials part.
     */
    public String getPass() {
        return uri.getPass();
    }

    /**
     * Returns the URI's host.
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
