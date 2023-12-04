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
package org.netbeans.modules.gradle.api;

import java.io.File;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Describes a problem in a buildscript reported by Gradle daemon and/or
 * our infrastructure.
 *
 * @author sdedic
 * @since 2.27
 */
public final class GradleReport {
    private final String errorClass;
    private final String location;
    private final int line;
    private final String message;
    private final GradleReport causedBy;
    private final Severity severity;
    private final String[] details;
    
    /**
     * Severity of the report.
     * 
     * @since 2.38
     */
    public enum Severity {
        /**
         * Unexpected exception during project read, most likely an error
         * in the project model reading code.
         */
        EXCEPTION,
        
        /**
         * Project could not be read, essential project data is missing.
         */
        ERROR, 
        
        /**
         * Warning that indicates some issue that happened during project model reading, but
         * basic model properties are still read.
         */
        WARNING, 
        
        /**
         * Notices and information messages.
         */
        INFO,
    }
    
    GradleReport(Severity severity, String errorClass, String location, int line, String message, GradleReport causedBy, String... details) {
        this.severity = severity;
        this.errorClass = errorClass;
        this.location = location;
        this.line = line;
        this.message = message == null ? "" : message;
        this.causedBy = causedBy;
        this.details = details != null && details.length > 0 ? details : null;
    }

    GradleReport(String errorClass, String location, int line, String message, GradleReport causedBy) {
        this(Severity.ERROR, errorClass, location, line, message, causedBy);
    }
    
    public @CheckForNull String getLocation() {
        return location;
    }

    public int getLine() {
        return line;
    }

    public @NonNull String getMessage() {
        return message;
    }

    public @CheckForNull GradleReport getCause() {
        return causedBy;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    public String getErrorClass() {
        return errorClass;
    }

    /**
     * @return the report's severity
     * @since 2.38
     */
    public Severity getSeverity() {
        return severity;
    }
    
    public String[] getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GradleReport other = (GradleReport) obj;
        if (!Objects.equals(this.errorClass, other.errorClass)) {
            return false;
        }
        if (this.line != other.line) {
            return false;
        }
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        return Objects.equals(this.location, other.location);
    }

    @Override
    public String toString() {
        return formatReportForHintOrProblem(true, null);
    }

    @NbBundle.Messages({
        "# {0} - previous part",
        "# {1} - appended part",
        "FMT_AppendMessage={0} {1}",
        "# {0} - the error message",
        "# {1} - the file",
        "# {2} - the line",
        "FMT_MessageWithLocation={0} ({1}:{2})",
        "# {0} - the error message",
        "# {1} - the file",
        "FMT_MessageWithLocationNoLine={0} ({1})",
        "# {0} - the error message",
        "# {1} - the stack trace, one line per stack frame",
        "FMT_MessageWithTrace={0}\nError stack trace: {1}"
    })

    /**
     * Formats the report for simple viewing. The function concatenates report messages, starting from
     * outer Report. If `includeLocation` is true, script name + line is printed at the end. 
     */
    public String formatReportForHintOrProblem(boolean includeLocation, FileObject relativeTo) {
        String msg = "";
        String prevMessage = null;
        
        for (GradleReport r2 = this; r2 != null; r2 = r2.getCause()) {
            // do not repeat nested messages, if the outer cause incorporated them
            if (prevMessage != null && prevMessage.contains(r2.getMessage())) {
                break;
            }
            prevMessage = r2.getMessage();
            msg = Bundle.FMT_AppendMessage(msg, r2.getMessage());
        }
        if (!includeLocation || getLocation() == null) {
            // exceptions from gradle contain quite descriptive messages, but runtime exceptions
            // and exceptions from the wrapper do not. 
            if (severity == Severity.EXCEPTION && details != null && details.length >0) {
                return Bundle.FMT_MessageWithTrace(msg, String.join("\n", details));
            } else {
                return msg;
            }
        }
        
        String locString;

        if (relativeTo != null) {
            // try convert the location to a FileObject:
            File dir = FileUtil.toFile(relativeTo);
            try {
                Path scriptPath = Paths.get(getLocation());
                locString = dir.toPath().relativize(scriptPath).toString();
            } catch (FileSystemNotFoundException | IllegalArgumentException ex) {
                // perhaps the location is not a filename
                locString = getLocation();
            }
        } else {
            locString = getLocation();
        }
        if (getLine() >= 1) {
            return Bundle.FMT_MessageWithLocation(msg, locString, getLine());
        } else {
            return Bundle.FMT_MessageWithLocationNoLine(msg, locString);
        }
    }
}
