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
package org.netbeans.modules.css.editor.csl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class CssErrorFactory {

    public static FilterableError createError(String key, String displayName, String description, FileObject file, int start, int end, boolean lineError, Severity severity, Collection<FilterableError.SetFilterAction> enableFilter, FilterableError.SetFilterAction disableFilter) {
        return new CssDefaultError(key, displayName, description, file, start, end, lineError, severity, enableFilter, disableFilter);
    }
    
    public static FilterableError createError(String key, String displayName, String description, FileObject file, int start, int end, boolean lineError, Severity severity) {
        return createError(key, displayName, description, file, start, end, lineError, severity, Collections.<FilterableError.SetFilterAction>emptyList(), null);
    }
    
    private static class CssDefaultError extends DefaultError implements FilterableError, Error.Badging {

        private final Collection<SetFilterAction> enableFilter;
        private final SetFilterAction disableFilter;
        
        private CssDefaultError(String key, String displayName, String description, FileObject file, int start, int end, boolean lineError, Severity severity, Collection<SetFilterAction> enableFilter, SetFilterAction disableFilter) {
            super(key, displayName, description, file, start, end, lineError, severity);
            this.disableFilter = disableFilter;
            this.enableFilter = enableFilter;
        }

         @Override
        public boolean showExplorerBadge() {
            return getSeverity() == Severity.ERROR || getSeverity() == Severity.FATAL;
        }
        
        @Override
        public boolean isFiltered() {
            return disableFilter != null;
        }

        @Override
        public Collection<SetFilterAction> getEnableFilterActions() {
            return enableFilter;
        }

        @Override
        public SetFilterAction getDisableFilterAction() {
            return disableFilter;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FilterableError other = (FilterableError) obj;
            if ((this.getDisplayName() == null) ? (other.getDisplayName() != null) : !this.getDisplayName().equals(other.getDisplayName())) {
                return false;
            }
            if ((this.getDescription() == null) ? (other.getDescription() != null) : !this.getDescription().equals(other.getDescription())) {
                return false;
            }
            if (this.getFile() != other.getFile() && (this.getFile() == null || !this.getFile().equals(other.getFile()))) {
                return false;
            }
            if (this.getStartPosition() != other.getStartPosition()) {
                return false;
            }
            if (this.getEndPosition() != other.getEndPosition()) {
                return false;
            }
            if (this.isLineError() != other.isLineError()) {
                return false;
            }
            if ((this.getKey() == null) ? (other.getKey() != null) : !this.getKey().equals(other.getKey())) {
                return false;
            }
            if (this.getSeverity() != other.getSeverity()) {
                return false;
            }
            if (this.isFiltered()!= other.isFiltered()) {
                return false;
            }
            
            if (!Arrays.deepEquals(this.getParameters(), other.getParameters())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.getDisplayName() != null ? this.getDisplayName().hashCode() : 0);
            hash = 29 * hash + (this.getDescription() != null ? this.getDescription().hashCode() : 0);
            hash = 29 * hash + (this.getFile() != null ? this.getFile().hashCode() : 0);
            hash = 29 * hash + this.getStartPosition();
            hash = 29 * hash + this.getEndPosition();
            hash = 29 * hash + (this.isLineError() ? 1 : 0);
            hash = 29 * hash + (this.getKey() != null ? this.getKey().hashCode() : 0);
            hash = 29 * hash + (this.getSeverity() != null ? this.getSeverity().hashCode() : 0);
            hash = 29 * hash + (this.isFiltered() ? 1 : 0);
            hash = 29 * hash + Arrays.deepHashCode(this.getParameters());
            return hash;
        }

        @Override
        public String toString() {
            return super.toString() + " (file:" + getFile() + ", from:" + getStartPosition() + ", to:" + getEndPosition() + ")";
        }
        
        
    }
}
