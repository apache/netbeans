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
package org.netbeans.modules.subversion.remote.api;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * 
 */
public class SVNUrl {

    public static final String SVN_PROTOCOL = "svn"; //NOI18N
    public static final String SVNSSH_PROTOCOL = "svn+ssh"; //NOI18N
    public static final String HTTP_PROTOCOL = "http"; //NOI18N
    public static final String HTTPS_PROTOCOL = "https"; //NOI18N
    public static final String FILE_PROTOCOL = "file"; //NOI18N
    
    private final String protocol;
    private final String host;
    private final int port;
    private final String path[];
    
    public SVNUrl(String url) throws MalformedURLException {
        if (url == null) {
            throw new MalformedURLException("SVN URL cannot be NULL."); //NOI18N
        }
        String tmp = url.trim();
        int i = tmp.indexOf("://"); //NOI18N
        if (i < 0) {
            throw new MalformedURLException("Invalid SVN URL: "+url); //NOI18N
        }
        protocol = tmp.substring(0,i).toLowerCase(Locale.ENGLISH);
        if (!(SVN_PROTOCOL.equals(protocol) || SVNSSH_PROTOCOL.equals(protocol) || HTTP_PROTOCOL.equals(protocol) ||
            HTTPS_PROTOCOL.equals(protocol) || FILE_PROTOCOL.equals(protocol))) {
            throw new MalformedURLException("Unsupported protocol of SVN URL: "+url); //NOI18N
        }
        tmp = tmp.substring(i+3);
        if (tmp.isEmpty()) {
            throw new MalformedURLException("Invalid path of SVN URL: "+url); //NOI18N
        }
        i = tmp.indexOf('/'); //NOI18N
        if (i < 0) {
            i = tmp.length();
        }
        if (FILE_PROTOCOL.equals(protocol)) {
            port = -1;
            if(i == 0) {
                host = ""; //NOI18N
            } else {
                host = tmp.substring(0, i);
            }
        } else {
            //http://llvm.org:80/svn/llvm-project/llvm/branches/release_34
            String hostAndPort = tmp.substring(0, i).toLowerCase(Locale.ENGLISH);
            String[] split = hostAndPort.split(":"); //NOI18N
            if (split.length == 1) {
                host = split[0];
                port = getDefaultPort(protocol);
            } else if (split.length == 2) {
                host = split[0];
                try {
                    port = Integer.parseInt(split[1]);
                } catch(NumberFormatException e) {
                    throw new MalformedURLException("Invalid port of SVN URL: "+url); //NOI18N
                }
            } else {
                throw new MalformedURLException("Invalid SVN URL: "+url); //NOI18N
            }
        }
        if(i < tmp.length()) {
            tmp = tmp.substring(i + 1);
            path = tmp.split("/"); //NOI18N
        } else {
            path = new String[0];
        }
    }

    private int getDefaultPort(String protocol) {
        switch(protocol) {
            case HTTPS_PROTOCOL:
                return 443;
            case HTTP_PROTOCOL:
                return 80;
            case SVNSSH_PROTOCOL:
                return 22;
            case SVN_PROTOCOL:
                return 3690;
        }
        return -1;
    }
    
    private SVNUrl(String protocol, String host, int port, String[] path) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public SVNUrl appendPath(String append) {
        List<String> res = new ArrayList<>(Arrays.asList(path));
        for(String s : append.split("/")) { //NOI18N
            if (!s.isEmpty()) {
                res.add(s);
            }
        }
        return new SVNUrl(protocol, host, port, res.toArray(new String[res.size()]));
    }

    public String[] getPathSegments() {
        return path;
    }

    public String getLastPathSegment() {
        if (path.length == 0) {
            return ""; //NOI18N
        }
        return path[path.length - 1];
    }

    public SVNUrl getParent() {
        if (path.length < 2 || host.isEmpty()) {
            return null;
        }
        String parent[] = new String[path.length-1];
        System.arraycopy(path, 0, parent, 0, path.length - 1);
        return new SVNUrl(protocol, host, port, parent);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(protocol).append("://").append(host); //NOI18N
        if (port != getDefaultPort(protocol)) {
            buf.append(':').append(port); //NOI18N
        }
        for(String s : path) {
            buf.append('/').append(s); //NOI18N
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.protocol);
        hash = 89 * hash + Objects.hashCode(this.host);
        hash = 89 * hash + this.port;
        hash = 89 * hash + Arrays.deepHashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SVNUrl other = (SVNUrl) obj;
        if (!Objects.equals(this.protocol, other.protocol)) {
            return false;
        }
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (!Arrays.deepEquals(this.path, other.path)) {
            return false;
        }
        return true;
    }

}
