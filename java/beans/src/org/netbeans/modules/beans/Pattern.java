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

package org.netbeans.modules.beans;

import java.awt.Image;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.openide.util.ImageUtilities;


/** Base class for patterns object. These objects hold information
 * about progarammatic patterns i.e. Properties and Events in the source code
 * @author Petr Hrebejk
 */
public abstract class Pattern {

    public static final Comparator<Pattern> NAME_COMPARATOR = new NameComparator();
    public static final String TYPE_COLOR = "#707070";
    
    
    private static final String ICON_BASE = "org/netbeans/modules/beans/resources/";
    private static final String GIF_EXTENSION = ".gif";
   
    protected static final Image PATTERNS = ImageUtilities.loadImage(ICON_BASE + "patternGroup" + GIF_EXTENSION); // NOI18N
    
    protected static final Image PROPERTY_READ = ImageUtilities.loadImage(ICON_BASE + "propertyRO" + GIF_EXTENSION); // NOI18N
    protected static final Image PROPERTY_WRITE = ImageUtilities.loadImage(ICON_BASE + "propertyWO" + GIF_EXTENSION); // NOI18N
    protected static final Image PROPERTY_READ_WRITE = ImageUtilities.loadImage(ICON_BASE + "propertyRW" + GIF_EXTENSION); // NOI18N
    
    protected static final Image IDX_PROPERTY_READ = ImageUtilities.loadImage(ICON_BASE + "propertyIndexedRO" + GIF_EXTENSION); // NOI18N
    protected static final Image IDX_PROPERTY_WRITE = ImageUtilities.loadImage(ICON_BASE + "propertyIndexedWO" + GIF_EXTENSION); // NOI18N
    protected static final Image IDX_PROPERTY_READ_WRITE = ImageUtilities.loadImage(ICON_BASE + "propertyIndexedRW" + GIF_EXTENSION); // NOI18N
    
    protected static final Image EVENT_SET_UNICAST = ImageUtilities.loadImage(ICON_BASE + "eventSetUnicast" + GIF_EXTENSION); // NOI18N
    protected static final Image EVENT_SET_MULTICAST = ImageUtilities.loadImage(ICON_BASE + "eventSetMulticast" + GIF_EXTENSION); // NOI18N
   
    protected static final Image CLASS = ImageUtilities.icon2Image(ElementIcons.getElementIcon(ElementKind.CLASS, Collections.<Modifier>emptySet())); // NOI18N
    protected static final Image INTERFACE = ImageUtilities.icon2Image(ElementIcons.getElementIcon(ElementKind.INTERFACE, Collections.<Modifier>emptySet())); // NOI18N
   
    protected final TypeMirrorHandle<TypeMirror> type;
    protected final String name;
    protected final Kind kind;

    private PatternAnalyser patternAnalyser;
   
    /** Constructor of Pattern. The patternAnalyser is the only connetion
     * to class which created this pattern.
     * @param patternAnalyser The patern analayser which created this pattern.
     */
    public Pattern( PatternAnalyser patternAnalyser, Kind kind, 
                    String name, TypeMirrorHandle<TypeMirror> type ) {
        this.patternAnalyser = patternAnalyser;
        this.kind = kind;
        this.name = name;
        this.type = type;
    }

    public PatternAnalyser getPatternAnalyser() {
        return patternAnalyser;
    }

    /** Gets the name of pattern.
     * @return Name of the pattern.
     */
    public String getName() {
        return name;
    }

    /** Sets the name of the pattern
     * @param name New name of the pattern.
     */
    public abstract void setName( String name );
    
    public abstract Image getIcon();
    
    public String getHtmlDisplayName() {
        return null;
    }
    
    /** Gets the class which declares this Pattern.
     * @return Class in which this pattern is defined.
     */
    public ElementHandle<TypeElement> getDeclaringClass() {
        return patternAnalyser.getClassElementHandle();
    }

    /** Gets the type of property */
    public TypeMirrorHandle<TypeMirror> getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public List<Pattern> getPatterns() {
        if ( kind == Kind.CLASS ) {
            return patternAnalyser.getPatterns();
        }
        else {
            return Collections.<Pattern>emptyList();
        }
    }

    /** Default behavior for destroying pattern is to do nothing
     */
    public void destroy() {
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pattern other = (Pattern) obj;
//        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
//            return false;
//        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.kind != other.kind && (this.kind == null || this.kind != other.kind)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 3;
//        hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        return hash;
    }
    
    public enum Kind {
        
        CLASS,          // Good for inerclasses
        PROPERTY,
        EVENT_SOURCE;

    }

    private static class NameComparator implements Comparator<Pattern> {

        public int compare(Pattern p1, Pattern p2) {
            return p1.name.compareToIgnoreCase(p2.name);
        }
        
    }
    
}
