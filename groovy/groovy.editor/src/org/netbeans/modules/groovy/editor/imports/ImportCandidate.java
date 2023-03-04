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

package org.netbeans.modules.groovy.editor.imports;

import java.util.Objects;
import javax.swing.Icon;

/**
 *
 * @author Martin Janicek
 */
public class ImportCandidate {

    private String name;
    private String fqnName;
    private Icon icon;
    private int importantsLevel;

    public ImportCandidate(String name, String fqnName, Icon icon, int importantsLevel) {
        this.name = name;
        this.fqnName = fqnName;
        this.icon = icon;
        this.importantsLevel = importantsLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String Name) {
        this.name = Name;
    }

    public String getFqnName() {
        return fqnName;
    }

    public void setFqnName(String fqnName) {
        this.fqnName = fqnName;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public int getImportantsLevel() {
        return importantsLevel;
    }

    public void setImportantsLevel(int importantsLevel) {
        this.importantsLevel = importantsLevel;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.fqnName);
        hash = 79 * hash + this.importantsLevel;
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
        final ImportCandidate other = (ImportCandidate) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.fqnName, other.fqnName)) {
            return false;
        }
        if (this.importantsLevel != other.importantsLevel) {
            return false;
        }
        return true;
    }
}