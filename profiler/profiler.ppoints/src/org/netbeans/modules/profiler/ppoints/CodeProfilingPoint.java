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

package org.netbeans.modules.profiler.ppoints;

import java.io.File;
import org.netbeans.lib.profiler.client.RuntimeProfilingPoint;
import org.openide.ErrorManager;
import java.util.Properties;
import org.openide.util.Lookup;


/**
 * Abstract superclass for all Profiling Points defined in source code
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
public abstract class CodeProfilingPoint extends ProfilingPoint {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public abstract static class Paired extends CodeProfilingPoint {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Location endLocation; // End location of the Profiling Point
        private Location startLocation; // Start location of the Profiling Point

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        Paired(String name, Location startLocation, Location endLocation, Lookup.Provider  project, ProfilingPointFactory factory) {
            super(name, project, factory);
            this.startLocation = startLocation;
            this.endLocation = endLocation;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------
        
        public boolean isValid() {
            // TODO: should also check line number!
            if (startLocation == null) return false;
            String filename = startLocation.getFile();
            File file = new File(filename);
            if (!filename.endsWith(".java") || !file.exists() || !file.isFile()) return false; // NOI18N
            
            if (!usesEndLocation()) return true;
            
            // TODO: should also check line number!
            if (endLocation == null) return false;
            filename = endLocation.getFile();
            file = new File(filename);
            return filename.endsWith(".java") && file.exists() && file.isFile(); // NOI18N
        }

        public void setEnabled(boolean enabled) {
            if (isEnabled() != enabled) {
                super.setEnabled(enabled);
                getStartAnnotation().fireTypeChanged();

                if (usesEndLocation()) {
                    getEndAnnotation().fireTypeChanged();
                }
            }
        }

        public void setEndLocation(Location endLocation) {
            if (((this.endLocation == null) && (endLocation == null))
                    || ((this.endLocation != null) && this.endLocation.equals(endLocation))) {
                return;
            }

            Annotation oldAnnotation = getEndAnnotation();
            Location oldLocation = this.endLocation;
            this.endLocation = endLocation;

            Annotation newAnnotation = getEndAnnotation();
            getChangeSupport()
                .firePropertyChange(PROPERTY_ANNOTATION,
                                    (oldAnnotation == null) ? new Annotation[0] : new Annotation[] { oldAnnotation },
                                    (newAnnotation == null) ? new Annotation[0] : new Annotation[] { newAnnotation });
            getChangeSupport()
                .firePropertyChange(PROPERTY_LOCATION,
                                    (oldLocation == null) ? Location.EMPTY : oldLocation,
                                    (endLocation == null) ? Location.EMPTY : endLocation);
        }

        public Location getEndLocation() {
            return endLocation;
        }

        public void setStartLocation(Location startLocation) {
            if (this.startLocation.equals(startLocation)) {
                return;
            }

            Annotation oldAnnotation = getStartAnnotation();
            Location oldLocation = this.startLocation;
            this.startLocation = startLocation;

            Annotation newAnnotation = getStartAnnotation();
            getChangeSupport()
                .firePropertyChange(PROPERTY_ANNOTATION, new Annotation[] { oldAnnotation }, new Annotation[] { newAnnotation });
            getChangeSupport()
                .firePropertyChange(PROPERTY_LOCATION, oldLocation, startLocation);
        }

        public Location getStartLocation() {
            return startLocation;
        }

        public RuntimeProfilingPoint[] createRuntimeProfilingPoints() {
            RuntimeProfilingPoint rpp1 = createRuntimeProfilingPoint(getStartLocation());
            if (rpp1 == null) return new RuntimeProfilingPoint[0]; // Cannot create RPP, wrong location

            if (usesEndLocation()) {
                RuntimeProfilingPoint rpp2 = createRuntimeProfilingPoint(getEndLocation());
                if (rpp2 == null) return new RuntimeProfilingPoint[0]; // Cannot create RPP, wrong location
                return new RuntimeProfilingPoint[] { rpp1, rpp2 };
            } else {
                return new RuntimeProfilingPoint[] { rpp1 };
            }
        }

        protected abstract Annotation getEndAnnotation();

        protected abstract Annotation getStartAnnotation();

        protected abstract boolean usesEndLocation();

        protected void timeAdjust(final int threadId, final long timeDiff0, final long timeDiff1) {
        }

        public CodeProfilingPoint.Annotation[] getAnnotations() {
            return usesEndLocation() ? new CodeProfilingPoint.Annotation[] { getStartAnnotation(), getEndAnnotation() }
                                     : new CodeProfilingPoint.Annotation[] { getStartAnnotation() };
        }
        
        public void setLocation(Annotation annotation, Location location) {
            if (annotation.equals(getStartAnnotation())) {
                setStartLocation(location);
            } else if (annotation.equals(getEndAnnotation())) {
                setEndLocation(location);
            }
        }

        Location getLocation(Annotation annotation) {
            if (annotation.equals(getStartAnnotation())) {
                return getStartLocation();
            } else if (annotation.equals(getEndAnnotation())) {
                return getEndLocation();
            }

            return null;
        }
    }

    public abstract static class Single extends CodeProfilingPoint {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Location location; // Location of the Profiling Point

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        Single(String name, Location location, Lookup.Provider  project, ProfilingPointFactory factory) {
            super(name, project, factory);
            this.location = location;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------
        
        public boolean isValid() {
            // TODO: should also check line number!
            if (location == null) return false;
            String filename = location.getFile();
            File file = new File(filename);
            return filename.endsWith(".java") && file.exists() && file.isFile(); // NOI18N
        }

        public void setEnabled(boolean enabled) {
            if (isEnabled() != enabled) {
                super.setEnabled(enabled);
                getAnnotation().fireTypeChanged();
            }
        }

        public void setLocation(Location location) {
            if (this.location.equals(location)) {
                return;
            }
            
            Annotation oldAnnotation = getAnnotation();
            Location oldLocation = this.location;
            this.location = location;

            Annotation newAnnotation = getAnnotation();
            getChangeSupport()
                .firePropertyChange(PROPERTY_ANNOTATION, new Annotation[] { oldAnnotation }, new Annotation[] { newAnnotation });
            getChangeSupport()
                .firePropertyChange(PROPERTY_LOCATION, oldLocation, location);
        }

        @Override
        public Location getLocation() {
            return location;
        }

        public RuntimeProfilingPoint[] createRuntimeProfilingPoints() {
            RuntimeProfilingPoint rpp = createRuntimeProfilingPoint(getLocation());
            if (rpp == null) return new RuntimeProfilingPoint[0]; // Cannot create RPP, wrong location
            return new RuntimeProfilingPoint[] { rpp };
        }

        protected abstract Annotation getAnnotation();

        public CodeProfilingPoint.Annotation[] getAnnotations() {
            return new CodeProfilingPoint.Annotation[] { getAnnotation() };
        }
        
        public void setLocation(Annotation annotation, Location location) {
            if (annotation.equals(getAnnotation())) setLocation(location);
        }

        Location getLocation(Annotation annotation) {
            if (annotation.equals(getAnnotation())) {
                return getLocation();
            }

            return null;
        }
    }

    public static final class Location {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        public static final String PROPERTY_LOCATION_FILE = "p_location_file"; // NOI18N
        public static final String PROPERTY_LOCATION_LINE = "p_location_line"; // NOI18N
        public static final String PROPERTY_LOCATION_OFFSET = "p_location_offset"; // NOI18N
        public static final int OFFSET_START = Integer.MIN_VALUE;
        public static final int OFFSET_END = Integer.MAX_VALUE;
        public static final Location EMPTY = new Location("", 0, OFFSET_START); // NOI18N

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private String file;
        private int line; // 1-based line
        private int offset;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Location(String file, int line, int offset) {
            this.file = file;
            this.line = line; // 1-based line
            this.offset = offset;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public String getFile() {
            return file;
        }

        public int getLine() {
            return line;
        } // 1-based line

        public boolean isLineEnd() {
            return getOffset() == OFFSET_END;
        }

        public boolean isLineStart() {
            return getOffset() == OFFSET_START;
        }

        public int getOffset() {
            return offset;
        }

        public boolean equals(Object object) {
            if (!(object instanceof Location)) {
                return false;
            }

            Location location = (Location) object;

            return file.equals(location.file) // Should compare resolved FileObjects
                   && (line == location.line) && (offset == location.offset);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 79 * hash + this.line;
            hash = 79 * hash + this.offset;
            return hash;
        }

        public static Location load(Lookup.Provider  project, int index, Properties properties) {
            return load(project, index, null, properties);
        }

        public static Location load(Lookup.Provider  project, int index, String prefix, Properties properties) {
            String absPrefix = (prefix == null) ? (index + "_") : (index + "_" + prefix); // NOI18N
            String relUrl = properties.getProperty(absPrefix + PROPERTY_LOCATION_FILE, null);
            String lineStr = properties.getProperty(absPrefix + PROPERTY_LOCATION_LINE, null);
            String offsetStr = properties.getProperty(absPrefix + PROPERTY_LOCATION_OFFSET, null);

            if ((relUrl == null) || (lineStr == null) || (offsetStr == null)) {
                return null;
            }

            Location location = null;
            String url = Utils.getAbsolutePath(project, relUrl);

            try {
                location = new Location(url, Integer.parseInt(lineStr), Integer.parseInt(offsetStr));
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
            }

            return location;
        }

        public void store(Lookup.Provider  project, int index, Properties properties) {
            store(project, index, null, properties);
        }

        public void store(Lookup.Provider  project, int index, String prefix, Properties properties) {
            String absPrefix = (prefix == null) ? (index + "_") : (index + "_" + prefix); // NOI18N
            properties.put(absPrefix + PROPERTY_LOCATION_FILE, Utils.getRelativePath(project, file));
            properties.put(absPrefix + PROPERTY_LOCATION_LINE, Integer.toString(line));
            properties.put(absPrefix + PROPERTY_LOCATION_OFFSET, Integer.toString(offset));
        }

        public String toString() {
            return "File: " + getFile() + ", line: " + getLine() + ", offset: " + getOffset(); // NOI18N
        }
    }
    
    public abstract static class Annotation extends org.openide.text.Annotation {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public abstract CodeProfilingPoint profilingPoint();

        void fireDescriptionChanged() {
            firePropertyChange(PROP_SHORT_DESCRIPTION, false, true);
        }

        void fireTypeChanged() {
            firePropertyChange(PROP_ANNOTATION_TYPE, false, true);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static final String PROPERTY_LOCATION = "p_location"; // NOI18N
    static final String PROPERTY_ANNOTATION = "p_annotation"; // NOI18N

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    CodeProfilingPoint(String name, Lookup.Provider  project, ProfilingPointFactory factory) {
        super(name, project, factory);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public abstract RuntimeProfilingPoint[] createRuntimeProfilingPoints();

    // returns first defined location
    public Location getLocation() {
        Annotation firstAnnotation = getAnnotations()[0];

        return (firstAnnotation == null) ? null : getLocation(firstAnnotation);
    }

    public abstract Annotation[] getAnnotations();
    
    abstract void setLocation(Annotation annotation, Location location);

    abstract Location getLocation(Annotation annotation);

    String getServerHandlerClassName() {
        return ((CodeProfilingPointFactory) getFactory()).getServerHandlerClassName();
    }

    String getServerInfo() {
        return null;
    }

    RuntimeProfilingPoint createRuntimeProfilingPoint(Location location) {
        ProfilingPointsManager ppManager = ProfilingPointsManager.getDefault();
        String className = Utils.getClassName(location);
        if (className == null) return null; // Classname cannot be resolved, most likely invalid location

        return new RuntimeProfilingPoint(ppManager.createUniqueRuntimeProfilingPointIdentificator(),
                                         className, location.getLine(), location.getOffset(),
                                         getServerHandlerClassName(), getServerInfo());
    }

    abstract void hit(RuntimeProfilingPoint.HitEvent hitEvent, int index);

    // used for updating location when editor line is moved, do not use!!!
    void internalUpdateLocation(Annotation annotation, int line) {
        Location location = getLocation(annotation);

        if (location != null) {
            location.line = line;
        }
    }
}
