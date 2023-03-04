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
    public static synchronized TableViewSettings getDefault() {
        
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
