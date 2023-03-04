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

package org.netbeans.modules.projectimport.eclipse.core;

class Link {
    
    private String name;
    private boolean file; // or folder?
    private String location;

    public Link(String name, boolean file, String location) {
        this.name = name;
        this.file = file;
        this.location = location;
    }
    
    String getName() {
        return name;
    }

    boolean isFile() {
        return file;
    }

    String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return name + " = " + location + " (type type: " + file + ")"; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Link)) return false;
        final Link link = (Link) obj;
        if (file != link.file) return false;
        if (name != null ? !name.equals(link.name) : link.name != null)
            return false;
        if (location != null ? !location.equals(link.location) : link.location != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + System.identityHashCode(name);
        result = 37 * result + System.identityHashCode(location);
        return result;
    }
}
