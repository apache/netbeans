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
package org.netbeans.modules.javaee.wildfly.ide.commands;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyModule {

    private final String archiveName;
    private String url;
    private boolean running;

    public WildflyModule(String archiveName) {
        this.archiveName = archiveName;
    }

    public WildflyModule(String archiveName, boolean running) {
        this.archiveName = archiveName;
        this.running = running;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final WildflyModule other = (WildflyModule) obj;
        if ((this.archiveName == null) ? (other.archiveName != null) : !this.archiveName.equals(other.archiveName)) {
            return false;
        }
        return true;
    }

    /**
     * Get the value of url
     *
     * @return the value of url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the value of url
     *
     * @param url new value of url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the value of running
     *
     * @return the value of running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get the value of archiveName
     *
     * @return the value of archiveName
     */
    public String getArchiveName() {
        return archiveName;
    }

}
