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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.properties;

import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Template for table editor setting registered in Lookup.
 *
 * @author  Petr Kuzel
 */
public abstract class TableViewSettings {
    
    /** Default key color. */
    public static final Color KEY_DEFAULT_COLOR = new Color(0, 0, 153);
    /** Default key background. */    
    public static final Color KEY_DEFAULT_BACKGROUND = Color.white;
    /** Default value color. */
    public static final Color VALUE_DEFAULT_COLOR = new Color(153, 0, 107);
    /** Default value background. */
    public static final Color VALUE_DEFAULT_BACKGROUND = Color.white;
    /** Default highlight color. */
    public static final Color HIGHLIGHT_DEFAULT_COLOR = Color.black;
    /** Default highlight background. */
    public static final Color HIGHLIGHT_DEFAULT_BACKGROUND = Color.yellow;
    /** Default shadow color. */
    public static final Color SHADOW_DEFAULT_COLOR = new Color(SystemColor.controlHighlight.getRGB());
    /** Default keystrokes for find next action. */
    public static final KeyStroke[] FIND_NEXT_DEFAULT_KEYSTROKES = new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)};
    /** Default keystrokes for find previous action. */
    public static final KeyStroke[] FIND_PREVIOUS_DEFAULT_KEYSTROKES = new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_F3, Event.SHIFT_MASK)};
    /** Default keystrokes for toggle highliht action. */
    public static final KeyStroke[] TOGGLE_HIGHLIGHT_DEFAULT_KEYSTROKES = new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.SHIFT_MASK | Event.ALT_MASK)};
    
    // the only instance
    private static DelegatingSettings delegatingSettings;

    // active registrations
    private static Lookup.Result<TableViewSettings> registrations;
    
    /** 
     * Reserved for subclasses 
     */
    protected TableViewSettings() {
    }
    
    /**
     * Query lookup for settings and watch them translating
     * registration changes to firing PCEs on returned
     * instance.
     */
    public synchronized static TableViewSettings getDefault() {
        
        if (delegatingSettings == null) {
            Lookup lookup = Lookup.getDefault();
            Lookup.Template<TableViewSettings> template = new Lookup.Template<TableViewSettings>(TableViewSettings.class);
            registrations = lookup.lookup(template);
            registrations.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent e) {
                    TableViewSettings peer = null;
                    if (registrations.allInstances().isEmpty()) {
                        peer = new HardcodedSettings();
                    } else {
                        peer = registrations.allInstances().iterator().next();
                    }
                    delegatingSettings.setPeer(peer);
                }
            });
            TableViewSettings peer = null;
            if (registrations.allInstances().isEmpty()) {
                peer = new HardcodedSettings();
            } else {
                peer = registrations.allInstances().iterator().next();
            }            
            delegatingSettings = new DelegatingSettings(peer);
        }
        
        return delegatingSettings;
    }
    
    // Settings accessors ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public abstract Color getKeyColor();
    public abstract Color getKeyBackground();
    public abstract Color getValueColor();
    public abstract Color getValueBackground();
    public abstract Color getHighlightColor();
    public abstract Color getHighlightBackground();
    public abstract Color getShadowColor();    
    public abstract Font getFont();

    public abstract KeyStroke[] getKeyStrokesFindNext();
    public abstract KeyStroke[] getKeyStrokesFindPrevious();
    public abstract KeyStroke[] getKeyStrokesToggleHighlight();

    public abstract void addPropertyChangeListener(PropertyChangeListener listener);
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returned by getDefault, allows to change impl as
     * reaction to registration changes.
     */
    private static class DelegatingSettings extends TableViewSettings implements PropertyChangeListener {
        
        private PropertyChangeSupport events = new PropertyChangeSupport(this);
        
        private TableViewSettings peer;
        
        DelegatingSettings(TableViewSettings peer) {
            this.peer = peer;
            peer.addPropertyChangeListener(this);
        }
        
        void setPeer(TableViewSettings peer) {
            this.peer.removePropertyChangeListener(this);
            this.peer = peer;
            this.peer.addPropertyChangeListener(this);            
            // everything has possibly changed
            events.firePropertyChange(null, null, null);            
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            events.addPropertyChangeListener(listener);
        }
        
        public Color getHighlightBackground() {
            return peer.getHighlightBackground();
        }
        
        public Color getHighlightColor() {
            return peer.getHighlightColor();
        }
        
        public Color getKeyBackground() {
            return peer.getKeyBackground();
        }
        
        public Color getKeyColor() {
            return peer.getKeyColor();
        }
        
        public Font getFont() {
            return peer.getFont();
        }
        
        public KeyStroke[] getKeyStrokesFindNext() {
            return peer.getKeyStrokesFindNext();
        }
        
        public KeyStroke[] getKeyStrokesFindPrevious() {
            return peer.getKeyStrokesFindPrevious();
        }
        
        public KeyStroke[] getKeyStrokesToggleHighlight() {
            return peer.getKeyStrokesToggleHighlight();
        }
        
        public Color getShadowColor() {
            return peer.getShadowColor();
        }
        
        public Color getValueBackground() {
            return peer.getValueBackground();
        }
        
        public Color getValueColor() {
            return peer.getValueColor();
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            events.removePropertyChangeListener(listener);
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            events.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
        
    }
    
    /**
     * If no setting is registered return following one.
     */
    private static class HardcodedSettings extends TableViewSettings {        
        public Color getKeyColor() {return KEY_DEFAULT_COLOR;}
        public Color getKeyBackground() {return KEY_DEFAULT_BACKGROUND;}
        public Color getValueColor() {return VALUE_DEFAULT_COLOR;}
        public Color getValueBackground() {return VALUE_DEFAULT_BACKGROUND;}
        public Color getHighlightColor() {return HIGHLIGHT_DEFAULT_COLOR;}
        public Color getHighlightBackground() {return HIGHLIGHT_DEFAULT_BACKGROUND;}
        public Color getShadowColor() { return SHADOW_DEFAULT_COLOR;}
        public Font getFont() { return UIManager.getFont("TextField.font"); }
        
        public KeyStroke[] getKeyStrokesFindNext() {return FIND_NEXT_DEFAULT_KEYSTROKES;}
        public KeyStroke[] getKeyStrokesFindPrevious() {return FIND_PREVIOUS_DEFAULT_KEYSTROKES;}
        public KeyStroke[] getKeyStrokesToggleHighlight() {return TOGGLE_HIGHLIGHT_DEFAULT_KEYSTROKES;}

        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        public void removePropertyChangeListener(PropertyChangeListener listener) {}        
    }
}
