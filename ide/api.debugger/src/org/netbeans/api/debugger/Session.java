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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.spi.debugger.ContextProvider;


/** Session visually represents one process or application. It should
 * be simple bean with properties like process ID, session name, etc.
 * All other functionality is delegated to current debugger engine.
 *
 * <table>
 * <caption>Description of Session</caption>
 * <tbody><tr>
 * <td colspan="2" style="background-color:#4D7A97"><b>Description </b></td>
 * </tr><tr><td>
 * <b>Functionality</b></td><td>
 *
 * <b>Properties:</b>
 *    Session has two standard read only properties - name ({@link #getName}) and
 *    location name ({@link #getLocationName}).
 *
 * <br><br>
 * <b>Management of languages and engines:</b>
 *    Debugger Core supports debugging in different languages. It means that 
 *    each session can be debugged using different languages and 
 *    {@link org.netbeans.api.debugger.DebuggerEngine}s. Session manages list 
 *    of supported languages ({@link #getSupportedLanguages}) and current 
 *    language ({@link #getCurrentLanguage}). Current language can be changed
 *    ({@link #setCurrentLanguage}).
 *    Each language corresponds to one 
 *    {@link org.netbeans.api.debugger.DebuggerEngine} 
 *    ({@link #getEngineForLanguage}). So, the current language
 *    defines current debugger engine ({@link #getCurrentEngine})
 *
 *    A support for a new debugger language can be added during a start of
 *    debugging only. See 
 *    {@link org.netbeans.api.debugger.DebuggerManager#startDebugging} and
 *    {@link org.netbeans.spi.debugger.DebuggerEngineProvider}
 *
 * <br><br>
 * <b>Support for additional services:</b>
 *    Session is final class. The standard method how to 
 *    extend its functionality is using lookup methods ({@link #lookup(String,Class)} and 
 *    {@link #lookupFirst(String,Class)}).
 *    There are two ways how to register some service provider for some
 *    type of Session:
 *    <ul>
 *      <li>Register 'live' instance of service provider during creation of 
 *        new instance of Session (see method
 *        {@link org.netbeans.spi.debugger.SessionProvider#getServices}).
 *      </li>
 *      <li>Register service provider in Manifest-inf/debugger/&lt;type ID&gt;
 *        folder. See Debugger SPI for more information about
 *        registration.</li>
 *    </ul>
 *
 * <br>
 * <b>Support for listening:</b>
 *    Session propagates all changes to
 *    {@link java.beans.PropertyChangeListener}.
 *
 * <br> 
 * </td></tr><tr><td>
 * <b>Clients / Providers</b></td><td>
 *
 * This class is final, so it does not have any external provider.
 * Debugger Core and UI modules are clients of this class.
 *
 * <br>
 * </td></tr><tr><td>
 * <b>Life-cycle</b></td><td>
 *
 * A new instance of Session class can be created and registered to
 * {@link org.netbeans.api.debugger.DebuggerManager} during the process
 * of starting of debugging (see
 * {@link org.netbeans.api.debugger.DebuggerManager#startDebugging}).
 *
 * Session is removed automatically from 
 * {@link org.netbeans.api.debugger.DebuggerManager} when the 
 * number of "supported languages" ({@link #getSupportedLanguages}) is zero.
 *
 * </td></tr><tr><td>
 * <b>Evolution</b></td><td>
 *
 * No method should be removed from this class, but some functionality can
 * be added in future.
 *
 * </td></tr></tbody></table>
 *
 * @author Jan Jancura
 */
public final class Session implements ContextProvider {
    
    /** Name of property for current language. */
    public static final String PROP_CURRENT_LANGUAGE = "currentLanguage";
    
    /** Name of property for the set of supported languages. */
    public static final String PROP_SUPPORTED_LANGUAGES = "supportedLanguages";

    
    // variables ...............................................................
    
    private final String        name;
    private final String        locationName;
    private DebuggerEngine      currentDebuggerEngine;
    private String              currentLanguage;
    private String[]            languages;
    private DebuggerEngine[]    engines;
    private final PropertyChangeSupport pcs;
    private Lookup              lookup;
    Lookup                      privateLookup;
    private final Map<DebuggerEngine, Lookup> enginesLookups = new HashMap<DebuggerEngine, Lookup>();

    
    // initialization ..........................................................
    
    Session (
        String name,
        String locationName,
        String id,
        Object[] services,
        Lookup diLookup
    ) {
        this.name = name;
        this.locationName = locationName;
        this.languages = new String [0];
        this.engines = new DebuggerEngine [0];
        pcs = new PropertyChangeSupport (this);
        
        // create lookup
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 0, services.length);
        s [s.length - 1] = this;
        privateLookup = new Lookup.Compound (
            new Lookup.Instance (s),
            new Lookup.MetaInf (id)
        );
        this.lookup = new Lookup.Compound (
            diLookup,
            privateLookup
        );
    }

    
    // public interface ........................................................

    /**
     * Returns display name of this session.
     *
     * @return display name of this session
     */
    public String getName () {
        return name;
    }
    
    /**
     * Returns identifier of type of this session. This id is used for 
     * identification of engine during registration of services in 
     * Meta-inf/debugger.
     *
     * @return identifier of type of this engine
     */
//    public String getTypeID () {
//        return id;
//    }
    
    /**
     * Returns name of location this session is running on.
     *
     * @return name of location this session is running on
     */
    public String getLocationName () {
        return locationName;
    }
    
    /**
     * Returns current debugger engine for this session.
     *
     * @return current debugger engine for this session
     */
    public DebuggerEngine getCurrentEngine () {
        return currentDebuggerEngine;
    }
    
    /**
     * Returns current language for this session.
     *
     * @return current language for this session
     */
    public String getCurrentLanguage () {
        return currentLanguage;
    }
    
    /**
     * Returns set of all languages supported by this session.
     *
     * @return set of all languages supported by this session
     * @see org.netbeans.spi.debugger.DebuggerEngineProvider
     */
    public String[] getSupportedLanguages () {
        return languages;
    }
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public <T> List<? extends T> lookup(String folder, Class<T> service) {
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return the service of given type
     */
    public <T> T lookupFirst(String folder, Class<T> service) {
        return lookup.lookupFirst (folder, service);
    }
    
    /**
     * Kills all registered engines / languages. This utility method calls
     * <pre>postAction (DebuggerEngine.ACTION_KILL)</pre> method on all
     * registered DebuggerEngines.
     */
    public void kill () {
        DebuggerEngine[] enginesToKill = engines;
        for (DebuggerEngine e : enginesToKill) {
            e.getActionsManager ().
                postAction (ActionsManager.ACTION_KILL);
        }
    }

    /**
     * Return DebuggerEngine registered for given language or null.
     *
     * @return DebuggerEngine registered for given language or null
     */
    public synchronized DebuggerEngine getEngineForLanguage (String language) {
        int i, k = languages.length;
        for (i = 0; i < k; i++)
            if (languages [i].equals (language))
                return engines [i];
        return null;
    }
    
    /**
     * Sets current language for this session. Language should be registered
     * for this session.
     *
     * @param language current language
     * @see org.netbeans.spi.debugger.DebuggerEngineProvider
     */
    public void setCurrentLanguage (String language) {
        Object oldL = null;
        int i, k;
        synchronized (this) {
            if (language.equals(currentLanguage)) {
                return ;
            }
            k = languages.length;
            for (i = 0; i < k; i++) {
                if (language.equals (languages [i])) {
                    oldL = currentLanguage;
                    currentLanguage = language;
                    currentDebuggerEngine = engines [i];
                    break;
                }
            }
        }
        if (i < k) { // was set
            pcs.firePropertyChange (
                PROP_CURRENT_LANGUAGE,
                oldL,
                language
            );
        }

    }

    
    // support methods .........................................................
    
    Lookup getLookup () {
        return lookup;
    }
    
    void addLanguage (
        String language,
        DebuggerEngine engine
    ) {
        Object oldL;
        String[] newLanguages;
        boolean fireCurrentLanguage = false;
        synchronized (this) {
            // is pair already added?
            int i, k = languages.length;
            for (i = 0; i < k; i++)
                if (language.equals (languages [i])) {
                    engines [i] = engine;
                    return;
                }

            // add pair
            newLanguages = new String [languages.length + 1];
            DebuggerEngine[] newEngines = new DebuggerEngine [engines.length + 1];
            System.arraycopy (languages, 0, newLanguages, 0, languages.length);
            System.arraycopy (engines, 0, newEngines, 0, engines.length);
            newLanguages [languages.length] = language;
            newEngines [engines.length] = engine;
            oldL = languages;
            languages = newLanguages;
            engines = newEngines;
            if (!enginesLookups.containsKey(engine)) {
                Lookup newCompoundLookup = new Lookup.Compound(lookup, engine.getPrivateLookup());
                lookup = newCompoundLookup;
                enginesLookups.put(engine, engine.getPrivateLookup());
            }
            if (currentLanguage == null) {
                currentLanguage = language;
                currentDebuggerEngine = engine;
                fireCurrentLanguage = true;
            }
        }
        DebuggerManager.getDebuggerManager ().addEngine (engine);
        pcs.firePropertyChange (
            PROP_SUPPORTED_LANGUAGES,
            oldL,
            newLanguages
        );
        if (fireCurrentLanguage) {
            pcs.firePropertyChange (
                PROP_CURRENT_LANGUAGE,
                null,
                language
            );
        }
    }
    
    void removeEngine (
        DebuggerEngine engine
    ) {
        String[] oldL;
        String[] newL;
        String oldCurrentL = null;
        String newCurrentL = null;
        synchronized (this) {
            if (engines.length == 0) return;
            int i, k = engines.length;
            // The engine can be in the array multiple times
            int t = 0; // It's there 't' times.
            for (i = 0; i < k; i++) {
                if (engine.equals (engines [i])) {
                    t++;
                }
            }
            if (t == 0) {
                // The engine is not there. Nothing to remove.
                return ;
            }
            newL = new String[k - t];
            DebuggerEngine[] newE = new DebuggerEngine[k - t];
            int j = 0;
            for (i = 0; i < k; i++) {
                if (!engine.equals (engines [i])) {
                    /*if (j == (k - t)) {
                        // The engine is not there. Nothing to remove.
                        return ;
                    }*/
                    newL[j] = languages [i];
                    newE[j] = engines [i];
                    j++;
                } else {
                    if (languages[i].equals(currentLanguage)) {
                        // The current language needs to be reset
                        oldCurrentL = currentLanguage;
                        if (i > 0) {
                            currentLanguage = languages[0];
                        } else if (i < (k - 1)) {
                            currentLanguage = languages[i + 1];
                        } else {
                            currentLanguage = null;
                        }
                        newCurrentL = currentLanguage;
                    }
                }
            }
            removeFromLookup(enginesLookups.remove(engine));
            oldL = languages;
            languages = newL;
            engines = newE;
        }
        DebuggerManager.getDebuggerManager ().removeEngine (engine);
        
        pcs.firePropertyChange (
            PROP_SUPPORTED_LANGUAGES,
            oldL,
            newL
        );
        if (oldCurrentL != newCurrentL) {
            pcs.firePropertyChange (
                PROP_CURRENT_LANGUAGE,
                oldCurrentL,
                newCurrentL
            );
        }
    }
    
    void removeLanguage (
        String language,
        DebuggerEngine engine
    ) {
        Object oldL;
        String[] newLanguages;
        synchronized (this) {
            int i, k = languages.length;
            for (i = 0; i < k; i++)
                if (language.equals (languages [i])) {
                    if (engines [i] != engine)
                        throw new IllegalArgumentException ();
                    break;
                }
            if (i >= k) return;

            newLanguages = new String [k - 1];
            DebuggerEngine[] newEngines = new DebuggerEngine [k - 1];
            if (i > 0) {
                System.arraycopy (languages, 0, newLanguages, 0, i);
                System.arraycopy (engines, 0, newEngines, 0, i);
            }
            System.arraycopy (languages, i + 1, newLanguages, i, k - i - 1);
            System.arraycopy (engines, i + 1, newEngines, i, k - i - 1);
            oldL = languages;
            languages = newLanguages;
            engines = newEngines;

            k = engines.length;
            for (i = 0; i < k; i++)
                if (engines [i] == engine) {
                    engine = null;
                    break;
                }
        }
        pcs.firePropertyChange (
            PROP_SUPPORTED_LANGUAGES,
            oldL,
            newLanguages
        );

        if (engine != null) {
            DebuggerManager.getDebuggerManager ().removeEngine (engine);
        }
    }

    private void removeFromLookup(Lookup engineLookup) {
        boolean [] wasRemovedPtr = new boolean[] { false };
        Lookup newLookup = removeFromLookup(lookup, engineLookup, wasRemovedPtr);
        if (wasRemovedPtr[0]) {
            lookup = newLookup;
        }
    }

    private Lookup removeFromLookup(Lookup lookup, Lookup engineLookup, boolean[] wasRemovedPtr) {
        if (engineLookup == null || !(lookup instanceof Lookup.Compound)) {
            return lookup;
        }
        Lookup.Compound cl = (Lookup.Compound) lookup;
        if (cl.l2 == engineLookup) {
            wasRemovedPtr[0] = true;
            return (Lookup) cl.l1;
        } else {
            if (!(cl.l1 instanceof Lookup) || !(cl.l2 instanceof Lookup)) {
                return lookup;
            }
            Lookup l1 = removeFromLookup((Lookup) cl.l1, engineLookup, wasRemovedPtr);
            Lookup l2 = removeFromLookup((Lookup) cl.l2, engineLookup, wasRemovedPtr);
            if (wasRemovedPtr[0]) {
                return new Lookup.Compound(l1, l2);
            } else {
                return lookup;
            }
        }
    }

    /**
     * Returns string representation of this session.
     *
     * @return string representation of this session
     */
    @Override
    public String toString () {
        return "" + getClass ().getName () + " " + getLocationName () + ":" +
            getName ();
    }
    
    
    // listener support ........................................................
    

    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
    /**
     * Adds a property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l the listener to add
     */
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }
    
    /**
     * Removes a property change listener.
     *
     * @param propertyName a name of property to stop listening on
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }


    // innerclasses ............................................................
    
//    private class Listener extends DebuggerEngineAdapter {
//        
//        public void actionPerformed (
//            DebuggerEngine engine, 
//            Object action, 
//            boolean success
//        ) {
//            if (action != DebuggerEngine.ACTION_KILL) return;
//            removeEngine (engine);
//        }
//        
//        public void actionStateChanged (
//            DebuggerEngine engine, 
//            Object action, 
//            boolean enabled
//        ) {
//        }
//        
//    }
}


