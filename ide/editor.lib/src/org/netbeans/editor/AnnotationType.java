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

package org.netbeans.editor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.beans.PropertyChangeSupport;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.ImageUtilities;

/** Definition of the annotation type. Annotation type is defined by attributes like
 * highlight color, foreground color, glyph icon, etc. Each annotation added to document
 * has reference to the name of the annotation type which defines how the annotation
 * will be drawn.
 *
 * @author David Konecny
 * @since 07/2001
 */

public class AnnotationType {

    private static final Logger LOG = Logger.getLogger(AnnotationType.class.getName());
    
    /** Property name for Name (String) */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Property name for Description (String) */
    public static final String PROP_DESCRIPTION = "description"; // NOI18N

    /** Property name for Visible (boolean) */
    public static final String PROP_VISIBLE = "visible"; // NOI18N
    
    /** Property name for Glyph (URL) */
    public static final String PROP_GLYPH_URL = "glyph"; // NOI18N

    /** Property name for Highlight (Color) */
    public static final String PROP_HIGHLIGHT_COLOR = "highlight"; // NOI18N

    /** Property name for Foreground (Color) */
    public static final String PROP_FOREGROUND_COLOR = "foreground"; // NOI18N

    /** Property name for WaveUnderline (Color) */
    public static final String PROP_WAVEUNDERLINE_COLOR = "waveunderline"; // NOI18N

    /** Property name for WholeLine (boolean) */
    public static final String PROP_WHOLE_LINE = "wholeline"; // NOI18N
    
    /** Property name for ContentType (String) */
    public static final String PROP_CONTENT_TYPE = "contenttype"; // NOI18N

    /** Property name for Actions (Action[]) */
    public static final String PROP_ACTIONS = "actions"; // NOI18N

    /** Property name for TooltipText (String) */
    public static final String PROP_TOOLTIP_TEXT = "tooltipText"; // NOI18N

    /** Property name for InheritForegroundColor (Boolean) */
    public static final String PROP_INHERIT_FOREGROUND_COLOR = "inheritForegroundColor"; // NOI18N
    
    /** Property name for UseHighlightColor (Boolean) */
    public static final String PROP_USE_HIGHLIGHT_COLOR = "useHighlightColor"; // NOI18N

    /** Property name for UseWaveUnderlineColor (Boolean) */
    public static final String PROP_USE_WAVEUNDERLINE_COLOR = "useWaveUnderlineColor"; // NOI18N

    public static final String PROP_USE_CUSTOM_SIDEBAR_COLOR = "useCustomSidebarColor"; // NOI18N
    
    public static final String PROP_CUSTOM_SIDEBAR_COLOR = "customSidebarColor"; // NOI18N
    
    public static final String PROP_SEVERITY = "severity"; // NOI18N
    
    public static final String PROP_BROWSEABLE = "browseable"; // NOI18N
    
    public static final String PROP_PRIORITY = "priority"; // NOI18N

    /** Property name for Combinations (AnnotationType.CombinationMember[]). 
     * If some annotation type has set this property, it means that editor
     * must check if line contains all types which are defined in this array.
     * If it contains, then all this annotation types become hidden and this 
     * type is shown instead of them. */
    public static final String PROP_COMBINATIONS = "combinations"; // NOI18N

    public static final String PROP_COMBINATION_ORDER = "combinationOrder"; // NOI18N

    public static final String PROP_COMBINATION_MINIMUM_OPTIONALS = "combinationMinimumOptionals"; // NOI18N

    /** Property holding the object which represent the source of this annotation type.
     * This property is used during the saving of the changes in annotation type. */
    public static final String PROP_FILE = "file"; // NOI18N

    public static final String PROP_LOCALIZING_BUNDLE = "bundle"; // NOI18N
    
    public static final String PROP_DESCRIPTION_KEY = "desciptionKey"; // NOI18N
    
    public static final String PROP_ACTIONS_FOLDER = "actionsFolder"; // NOI18N
    
    public static final String PROP_COMBINATION_TOOLTIP_TEXT_KEY = "tooltipTextKey"; // NOI18N
    
    /** Storage of all annotation type properties. */
    private Map properties;

    /** Support for property change listeners*/
    private PropertyChangeSupport support;
    
    /** Glyph icon loaded from URL into Image */
    private Image img = null;
    
    /** Coloring composed from foreground and highlight color*/
    private Coloring col;
    
    public AnnotationType() {
        properties = new HashMap(15*4/3);
        support = new PropertyChangeSupport(this);
    }

    /** Getter for Glyph property
     * @return  URL of the glyph icon */    
    public java.net.URL getGlyph() {
        URL u = (java.net.URL)getProp(PROP_GLYPH_URL);
        if (u == null)
            u = AnnotationTypes.getDefaultGlyphURL();
        return u;
    }

    /** Setter for the Glyph property
     * @param glyph URL to gpylh icon */    
    public void setGlyph(java.net.URL glyph) {
        putProp(PROP_GLYPH_URL, glyph);
    }

    /** Gets Image which represent the glyph. This method is called 
     * only from AWT thead and so it is not necessary to synchronize it.
     */
    public Image getGlyphImage() {
        if (img == null) {
            try {
                img = ImageUtilities.loadImage(getGlyph().toURI());
            } catch (URISyntaxException e) {
                LOG.log(Level.WARNING, "getGlyph() returned invalid URI", e);
                return null;
            }
            final boolean waiting[] = new boolean [1];
            waiting[0] = true;
            if (!Toolkit.getDefaultToolkit().prepareImage(img, -1, -1, new ImageObserver() {
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    if ((infoflags & ImageObserver.ALLBITS) == ImageObserver.ALLBITS) {
                        waiting[0] = false;
                        return false;
                    } else {
                        return true;
                    }
                }
            }))
            {
                long tm = System.currentTimeMillis();
                while(waiting[0] && System.currentTimeMillis() - tm < 1000) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        }
        return img;
    }
    
    /** Whether the annotation type has its own glyph icon or not */
    public boolean isDefaultGlyph() {
        if (getProp(PROP_GLYPH_URL) == null)
            return true;
        else
            return false;
    }
    
    /** Getter for Highlight property
     * @return  highlight color */    
    public java.awt.Color getHighlight() {
        return (java.awt.Color)getProp(PROP_HIGHLIGHT_COLOR);
    }        

    /** Setter for the Highlight property
     * @param highlight highlight color */    
    public void setHighlight(java.awt.Color highlight) {
        col = null; // force the create new coloring
        putProp(PROP_HIGHLIGHT_COLOR, highlight);
        firePropertyChange(PROP_HIGHLIGHT_COLOR, null, null);
        processChange();
    }

    /** Getter for UseHighlightColor property
     * @return  whether the highlight color should be used or not */    
    public boolean isUseHighlightColor() {
        Boolean b = (Boolean)getProp(PROP_USE_HIGHLIGHT_COLOR);
        if (b == null)
            return true;
        return b.booleanValue();
    }

    /** Setter for the UseHighlightColor property
     * @param use use highlight color */    
    public void setUseHighlightColor(boolean use) {
        if (isUseHighlightColor() != use) {
            col = null; // force the create new coloring
            putProp(PROP_USE_HIGHLIGHT_COLOR, use ? Boolean.TRUE : Boolean.FALSE);
            firePropertyChange(PROP_USE_HIGHLIGHT_COLOR, null, null);
            processChange();
        }
    }
    
    /** Getter for Foreground property
     * @return  foreground color */    
    public java.awt.Color getForegroundColor() {
        return (java.awt.Color)getProp(PROP_FOREGROUND_COLOR);
    }

    /** Setter for the Foreground property
     * @param foregroundColor foreground color */    
    public void setForegroundColor(java.awt.Color foregroundColor) {
        col = null; // force the create new coloring
        putProp(PROP_FOREGROUND_COLOR, foregroundColor);
        firePropertyChange(PROP_FOREGROUND_COLOR, null, null);
        processChange();
    }

    /** Getter for InheritForegroundColor property
     * @return  whether the foreground color should be inherit or not */    
    public boolean isInheritForegroundColor() {
        Boolean b = (Boolean)getProp(PROP_INHERIT_FOREGROUND_COLOR);
        if (b == null)
            return true;
        return b.booleanValue();
    }

    /** Setter for the InheritfForegroundColor property
     * @param inherit inherit foreground color */    
    public void setInheritForegroundColor(boolean inherit) {
        if (isInheritForegroundColor() != inherit) {
            col = null; // force the create new coloring
            putProp(PROP_INHERIT_FOREGROUND_COLOR, inherit ? Boolean.TRUE : Boolean.FALSE);
            firePropertyChange(PROP_INHERIT_FOREGROUND_COLOR, null, null);
            processChange();
        }
    }
    
    /** Getter for WaveUnderline property
     * @return  waveunderline color */    
    public java.awt.Color getWaveUnderlineColor() {
        return (java.awt.Color)getProp(PROP_WAVEUNDERLINE_COLOR);
    }        

    /** Setter for the WaveUnderline property
     * @param waveunderline wave underline color */    
    public void setWaveUnderlineColor(java.awt.Color waveunderline) {
        col = null; // force the create new coloring
        putProp(PROP_WAVEUNDERLINE_COLOR, waveunderline);
        firePropertyChange(PROP_WAVEUNDERLINE_COLOR, null, null);
        processChange();
    }

    /** Getter for UseWaveUnderlineColor property
     * @return  whether the waveunderline color should be used or not */    
    public boolean isUseWaveUnderlineColor() {
        Boolean b = (Boolean)getProp(PROP_USE_WAVEUNDERLINE_COLOR);
        if (b == null)
            return true;
        return b.booleanValue();
    }

    /** Setter for the UseWaveUnderlineColor property
     * @param use use wave underline color */    
    public void setUseWaveUnderlineColor(boolean use) {
        if (isUseWaveUnderlineColor() != use) {
            col = null; // force the create new coloring
            putProp(PROP_USE_WAVEUNDERLINE_COLOR, use ? Boolean.TRUE : Boolean.FALSE);
            firePropertyChange(PROP_USE_WAVEUNDERLINE_COLOR, null, null);
            processChange();
        }
    }
    
    /** Process change of some setting. It means that 
     * listeners are notified and change is saved. */
    private void processChange() {
        // if type does not have this property it is just being loaded
        if (getProp(AnnotationType.PROP_FILE) == null)
            return;
        
// XXX: has this ever worked??
//        // force repaint of all documents
//        Settings.touchValue(null, null);
        
        AnnotationTypes.getTypes().saveType(this);
    }

    /** Gets all the colors composed as Coloring
     * @return  coloring containing all colors */    
    public Coloring getColoring() {
        if (col == null)
            col = new Coloring(null, Coloring.FONT_MODE_DEFAULT, isInheritForegroundColor() ? null : getForegroundColor(), isUseHighlightColor() ? getHighlight() : null, null, null, isUseWaveUnderlineColor() ? getWaveUnderlineColor() : null);
        return col;
    }
    
    /** Getter for Actions property
     * @return array of actions */    
    public javax.swing.Action[] getActions() {
        return (javax.swing.Action[])getProp(PROP_ACTIONS);
    }

    /** Setter for Actions property
     */
    public void setActions(javax.swing.Action[] actions) {
        putProp(PROP_ACTIONS, actions);
    }

    /** Getter for Combinations property
     * @return array of combinations */    
    public CombinationMember[] getCombinations() {
        return (CombinationMember[])getProp(PROP_COMBINATIONS);
    }

    /** Setter for Combinations property */
    public void setCombinations(CombinationMember[] combs) {
        putProp(PROP_COMBINATIONS, combs);
    }

    /** Getter for Name property
     * @return annotation type name */    
    public String getName() {
        return (String)getProp(PROP_NAME);
    }

    /** Setter for the Name property
     * @param name name of the annotation type */    
    public void setName(String name) {
        putProp(PROP_NAME, name);
    }
    
    /** Getter for Description property
     * @return localized description of the annotation type */    
    public String getDescription() {
        String desc = (String)getProp(PROP_DESCRIPTION);
        if (desc == null) {
            String localizer = (String)getProp(PROP_LOCALIZING_BUNDLE);
            String key = (String)getProp(PROP_DESCRIPTION_KEY);
            if (localizer != null && key != null) {
                try {
                    ResourceBundle bundle = ImplementationProvider.getDefault().getResourceBundle(localizer);                
                    desc = bundle.getString(key);
                } catch(java.util.MissingResourceException mre) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("Can't find '" + key + "' in " + localizer + " for AnnotationType '" + this.getName() + "'."); //NOI18N
                    }
                    desc = key;
                }
            }
            setDescription(desc); // cache it
        }
        if (desc == null) {
            desc = (String)getProp(PROP_NAME);
        }
        if (desc == null) {
            desc = "null"; //NOI18N
        }
        return desc;
    }

    /** Setter for the Description property
     * @param name localized description of the annotation type */    
    public void setDescription(String name) {
        putProp(PROP_DESCRIPTION, name);
    }
    
    /** Getter for TooltipText property
     * @return localized TooltipText of the annotation type */    
    public String getTooltipText() {
        String text = (String)getProp(PROP_TOOLTIP_TEXT);
        if (text == null) {
            String localizer = (String)getProp(PROP_LOCALIZING_BUNDLE);
            String key = (String)getProp(PROP_COMBINATION_TOOLTIP_TEXT_KEY);
            ResourceBundle bundle = ImplementationProvider.getDefault().getResourceBundle(localizer);
            text = bundle.getString(key);
            setTooltipText(text); // cache it
        }
        return text;
    }

    /** Setter for the TooltipText property
     * @param text localized TooltipText of the annotation type */    
    public void setTooltipText(String text) {
        putProp(PROP_TOOLTIP_TEXT, text);
    }
    
    /** Getter for CombinationOrder property
     * @return order of the annotation type */    
    public int getCombinationOrder() {
        if (getProp(PROP_COMBINATION_ORDER) == null)
            return 0;
        return ((Integer)getProp(PROP_COMBINATION_ORDER)).intValue();
    }

    /** Setter for the CombinationOrder property
     * @param order order of the annotation type combination */    
    public void setCombinationOrder(int order) {
        putProp(PROP_COMBINATION_ORDER, Integer.valueOf(order));
    }
    
    /** Setter for the CombinationOrder property
     * @param ord order of the annotation type combination */    
    public void setCombinationOrder(String ord) {
        int order;
        try {
            order = Integer.parseInt(ord);
        } catch (NumberFormatException ex) {
            Utilities.annotateLoggable(ex);
            return;
        }
        putProp(PROP_COMBINATION_ORDER, Integer.valueOf(order));
    }

    /** Getter for MinimumOptionals property
     * @return minimum number of the optional annotation types which
     * must be matched */    
    public int getMinimumOptionals() {
        if (getProp(PROP_COMBINATION_MINIMUM_OPTIONALS) == null)
            return 0;
        return ((Integer)getProp(PROP_COMBINATION_MINIMUM_OPTIONALS)).intValue();
    }

    public void setMinimumOptionals(int min) {
        putProp(PROP_COMBINATION_MINIMUM_OPTIONALS, Integer.valueOf(min));
    }
    
    public void setMinimumOptionals(String m) {
        int min;
        try {
            min = Integer.parseInt(m);
        } catch (NumberFormatException ex) {
            Utilities.annotateLoggable(ex);
            return;
        }
        putProp(PROP_COMBINATION_MINIMUM_OPTIONALS, Integer.valueOf(min));
    }
    
    /** Getter for Visible property
     * @return whether the annoation type is visible or not */    
    public boolean isVisible() {
        Boolean b = (Boolean)getProp(PROP_VISIBLE);
        if (b == null)
            return false;
        return b.booleanValue();
    }

    /** Setter for the Visible property
     * @param vis visibility of the annotation type */    
    public void setVisible(boolean vis) {
        putProp(PROP_VISIBLE, vis ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Setter for the Visible property
     * @param vis visibility of the annotation type */    
    public void setVisible(String vis) {
        putProp(PROP_VISIBLE, Boolean.valueOf(vis));
    }

    /** Getter for WholeLine property
     * @return whether this annotation type is whole line or not  */    
    public boolean isWholeLine() {
        Boolean b = (Boolean)getProp(PROP_WHOLE_LINE);
        if (b == null)
            return true;
        return b.booleanValue();
    }

    /** Setter for the WholeLine property
     * @param wl whether the annotation type is whole line or not */    
    public void setWholeLine(boolean wl) {
        putProp(PROP_WHOLE_LINE, wl ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Setter for the WholeLine property
     * @param wl whether the annotation type is whole line or not */    
    public void setWholeLine(String wl) {
        putProp(PROP_WHOLE_LINE, Boolean.valueOf(wl));
    }

    /** Getter for ContentType property
     * @return  list of content types separated by commas */    
    public String getContentType() {
        return (String)getProp(PROP_CONTENT_TYPE);
    }

    /** Setter for the ContentType property
     * @param ct list of content type separeted by commas */    
    public void setContentType(String ct) {
        putProp(PROP_CONTENT_TYPE, ct);
    }

    public boolean isUseCustomSidebarColor() {
        return ((Boolean)getProp(PROP_USE_CUSTOM_SIDEBAR_COLOR)).booleanValue();
    }

    public void setUseCustomSidebarColor(boolean value) {
        putProp(PROP_USE_CUSTOM_SIDEBAR_COLOR, Boolean.valueOf(value));
    }

    public Color getCustomSidebarColor() {
        return (Color) getProp(PROP_CUSTOM_SIDEBAR_COLOR);
    }

    public void setCustomSidebarColor(Color customSidebarColor) {
        putProp(PROP_CUSTOM_SIDEBAR_COLOR, customSidebarColor);
    }
    
    public Severity getSeverity() {
        return (Severity) getProp(PROP_SEVERITY);
    }

    public void setSeverity(Severity severity) {
        putProp(PROP_SEVERITY, severity);
    }
    
    public int getPriority() {
        return ((Integer) getProp(PROP_PRIORITY)).intValue();
    }

    public void setPriority(int priority) {
        putProp(PROP_PRIORITY, Integer.valueOf(priority));
    }

    public boolean isBrowseable() {
        return ((Boolean)getProp(PROP_BROWSEABLE)).booleanValue();
    }

    public void setBrowseable(boolean browseable) {
        putProp(PROP_BROWSEABLE, Boolean.valueOf(browseable));
    }

    /** Gets property for appropriate string value */
    public Object getProp(String prop){
        return properties.get(prop);
    }
    
    /** Puts property to Map */
    public void putProp(Object key, Object value){
        if (value == null) {
            properties.remove(key);
            return;
        }
        properties.put(key,value);
    }
    
    public @Override String toString() {
        return "AnnotationType: name='" + getName() + "', description='" + getDescription() + // NOI18N
            "', visible=" + isVisible() + ", wholeline=" + isWholeLine() + // NOI18N
            ", glyph=" + getGlyph() + ", highlight=" + getHighlight() + // NOI18N
            ", foreground=" + getForegroundColor() + // NOI18N
            "', inheritForeground=" + isInheritForegroundColor() + //NOI18N
            ", contenttype="+getContentType(); //NOI18N

    }

    /** Add listeners on changes of annotation type properties
     * @param l  change listener*/
    public final void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.addPropertyChangeListener (l);
    }
    
    /** Remove listeners on changes of annotation type properties
     * @param l  change listener*/
    public final void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.removePropertyChangeListener (l);
    }

    /** Fire property change to registered listeners. */
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }


    /** Hepler class describing annotation type and whether all
     * occurences of this type should be absorbed by combination or not.
     * The annonation type which want to combine some other types, must
     * define array of instances of this helper class. See 
     * AnnotationType.PROP_COMBINATIONS property.
     */
    public static final class CombinationMember {

        /** Name of the annotation type */
        private String type;
        
        /** Whether all occurences of this type should be absorbed or not */
        private boolean absorbAll;

        /** Whether this combination member is options or not */
        private boolean optional;
        
        /** Minimum count of this type which must be found on the line to make 
         * valid combination */
        private int minimumCount;

        public CombinationMember(String type, boolean absorbAll, boolean optional, int minimumCount) {
            this.type = type;
            this.absorbAll = absorbAll;
            this.optional = optional;
            this.minimumCount = minimumCount;
        }

        public CombinationMember(String type, boolean absorbAll, boolean optional, String minimumCount) {
            this.type = type;
            this.absorbAll = absorbAll;
            this.optional = optional;
            if (minimumCount != null && minimumCount.length() > 0) {
                try {
                    this.minimumCount = Integer.parseInt(minimumCount);
                } catch (NumberFormatException ex) {
                    Utilities.annotateLoggable(ex);
                    this.minimumCount = 0;
                }
            } else
                this.minimumCount = 0;
        }

        /** Gets name of the annotation type */
        public String getName() {
            return type;
        }

        /** Getter for AbsorbAll property  */
        public boolean isAbsorbAll() {
            return absorbAll;
        }
        
        /** Getter for Optional property  */
        public boolean isOptional() {
            return optional;
        }
        
        /** Getter for MinimumCount property  */
        public int getMinimumCount() {
            return minimumCount;
        }
    }
    
    public static final class Severity implements Comparable {
        
        /**Status OK.
         */
        private static final int STATUS_NONE_NUMBER = 0;
        
        /**Status OK.
         */
        private static final int STATUS_OK_NUMBER = 1;
        
        /**Status warning.
         */
        private static final int STATUS_WARNING_NUMBER = 2;
        
        /**Status error.
         */
        private static final int STATUS_ERROR_NUMBER = 3;
        
        /**Status OK.
         */
        public static final Severity STATUS_NONE = new Severity(STATUS_NONE_NUMBER);
        
        /**Status OK.
         */
        public static final Severity STATUS_OK = new Severity(STATUS_OK_NUMBER);
        
        /**Status warning.
         */
        public static final Severity STATUS_WARNING = new Severity(STATUS_WARNING_NUMBER);
        
        /**Status error.
         */
        public static final Severity STATUS_ERROR = new Severity(STATUS_ERROR_NUMBER);
        
        private static final Severity[] VALUES = new Severity[] {STATUS_NONE, STATUS_OK, STATUS_WARNING, STATUS_ERROR};
        
        private static final Color[] DEFAULT_STATUS_COLORS = new Color[] {Color.WHITE, Color.GREEN, Color.YELLOW, Color.RED};
        
        private int status;
        
        /**Creates a Status with a given status value.
         *
         * @param status status value to use
         * @see #STATUS_ERROR
         * @see #STATUS_WARNING
         * @see #STATUS_OK
         * @throws IllegalArgumentException if one of the provided statuses is something
         *                               else then {@link #STATUS_ERROR},
         *                                         {@link #STATUS_WARNING} and
         *                                         {@link #STATUS_OK}
         */
        private Severity(int status) throws IllegalArgumentException {
            if (status != STATUS_NONE_NUMBER && status != STATUS_ERROR_NUMBER && status != STATUS_WARNING_NUMBER && status != STATUS_OK_NUMBER)
                throw new IllegalArgumentException("Invalid status provided: " + status); // NOI18N
            this.status = status;
        }
        
        /**Returns the numerical status assigned to this {@link Status}.
         *
         * @return numerical status
         */
        private int getStatus() {
            return status;
        }
        
        /**{@inheritDoc}*/
        public int compareTo(Object o) {
            Severity remote = (Severity) o;
            
            if (status > remote.status) {
                return 1;
            }
            
            if (status < remote.status) {
                return -1;
            }
            
            return 0;
        }
        
        /**{@inheritDoc}*/
        public @Override boolean equals(Object o) {
            if (!(o instanceof Severity)) {
                return false;
            }
            
            Severity remote = (Severity) o;
            
            return    status == remote.status;
        }
        
        /**{@inheritDoc}*/
        public @Override int hashCode() {
            return 43 ^ status;
        }
        
        private static String[] STATUS_NAMES = new String[] {
            "none", "ok", "warning", "error" // NOI18N
        };
        
        /**Returns a {@link String} representation of the {@link Severity}.
         * The format of the {@link String} is not specified.
         * This method should only be used for debugging purposes.
         *
         * @return {@link String} representation of this object
         */
        public @Override String toString() {
            return "[Status: " + STATUS_NAMES[getStatus()] + "]"; // NOI18N
        }
        
        /**Return the more important status out of the two given statuses.
         * The statuses are ordered as follows:
         * {@link #STATUS_ERROR}&gt;{@link #STATUS_WARNING}&gt;{@link #STATUS_OK}.
         *
         * @param first one provided status
         * @param second another provided status
         * @return the more important status out of the two provided statuses
         * @throws IllegalArgumentException if one of the provided statuses is something
         *                               else then {@link #STATUS_ERROR},
         *                                         {@link #STATUS_WARNING} and
         *                                         {@link #STATUS_OK}
         */
        public static Severity getCompoundStatus(Severity first, Severity second) throws IllegalArgumentException {
            if (first != STATUS_ERROR && first != STATUS_WARNING && first != STATUS_OK)
                throw new IllegalArgumentException("Invalid status provided: " + first); // NOI18N
            
            if (second != STATUS_ERROR && second != STATUS_WARNING && second != STATUS_OK)
                throw new IllegalArgumentException("Invalid status provided: " + second); // NOI18N
            
            return VALUES[Math.max(first.getStatus(), second.getStatus())];
        }
        
        /**Returns default {@link Color} for a given {@link Severity}.
         *
         * @param s {@link Severity} for which default color should be found
         * @return default {@link Color} for a given {@link Severity}
         */
        public static Color getDefaultColor(Severity s) {
            return DEFAULT_STATUS_COLORS[s.getStatus()];
        }
        
        public static Severity valueOf(String severity) {
            Severity severityValue = Severity.STATUS_NONE;
            
            if (severity != null) {
                if ("ok".equals(severity)) { // NOI18N
                    severityValue = AnnotationType.Severity.STATUS_OK;
                } else {
                    if ("warning".equals(severity)) { // NOI18N
                        severityValue = AnnotationType.Severity.STATUS_WARNING;
                    } else {
                        if ("error".equals(severity)) { // NOI18N
                            severityValue = AnnotationType.Severity.STATUS_ERROR;
                        }
                    }
                }
            }
            
            return severityValue;
        }
        
        public String getName() {
            return STATUS_NAMES[status];
        }
    }
}
