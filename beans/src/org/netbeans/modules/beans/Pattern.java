/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        if (this.kind != other.kind && (this.kind == null || !this.kind.equals(other.kind))) {
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
