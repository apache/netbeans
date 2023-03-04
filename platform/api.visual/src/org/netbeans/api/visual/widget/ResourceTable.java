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
package org.netbeans.api.visual.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The ResourceTable is used to manage a widgets resource.  A ResourceTable 
 * can have a parent ResourceTable.  The values in the child resource table will
 * override the values in a parent resource table. 
 * 
 * @author treyspiva
 */
public class ResourceTable
{
    public static final String PARENT_RESOURCE_TABLE = "ParentResourceTable";
    
//    private PropertyChangeSupport propertySupport = null;
    private ArrayList < WeakReference <PropertyChangeListener> > listeners =
            new ArrayList < WeakReference <PropertyChangeListener> >();
    
    private HashMap < String, ArrayList < WeakReference <PropertyChangeListener> >> propertyListeners = 
            new HashMap < String, ArrayList < WeakReference <PropertyChangeListener> >>();
    
//    private HashMap < String, Paint > paintProperties 
//            = new HashMap<String, Paint>();
//    
//    private HashMap < String, Color > colorProperties 
//            = new HashMap<String, Color>();
//    
//    private HashMap < String, Font > fontProperties 
//            = new HashMap<String, Font>();
    
    private HashMap < String, Object > properties = 
            new HashMap < String, Object >();
    
    private ArrayList < WeakReference < ResourceTable > > childrenTables = 
            new ArrayList<WeakReference < ResourceTable >>();
    
    private ResourceTable parentTable = null;

    /**
     * Create a new resource table that has a parent.
     * 
     * @param parent the parent resource table.
     */
    public ResourceTable(ResourceTable parent)
    {
        this();
        
        this.parentTable = parent;
        if(this.parentTable != null)
        {
            this.parentTable.addChild(this);
        }
    }
    
    /**
     * Create a new resource table.
     */
    public ResourceTable()
    {
//        propertySupport = new PropertyChangeSupport(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data Management
    
    public void setParentTable(ResourceTable parent)
    {
        ResourceTable oldTable = this.parentTable;
        this.parentTable = parent;
        
        firePropertyChange(PARENT_RESOURCE_TABLE, oldTable, parent);
    }
    
    public ResourceTable getParentTable()
    {
        return parentTable;
    }
    
    /**
     * Add a child resource table.  The child resource table will be notified 
     * when items in the resource table are changes.
     * 
     * @param childTable the child table.
     */
    private void addChild(ResourceTable childTable)
    {
        childrenTables.add(new WeakReference < ResourceTable >(childTable));
    }
    
    /**
     * Remove a resource table from the children list.
     * 
     * @param childTable the child resource table.
     */
    private void removeChild(ResourceTable childTable)
    {
        childrenTables.remove(childTable);
    }
    
    /**
     * Removes the parent resource table.
     */
    public void removeParent()
    {
        parentTable.removeChild(this);
        parentTable = null;
    }
    
    /**
     * Clears the entire resource table.  Events will be sent to the listners.
     */
    public void clear()
    {
        for(Map.Entry<String, Object> entry : properties.entrySet())
        {
            firePropertyChange(entry.getKey(), entry.getValue(), null);
        }
        properties.clear();
//        for(Map.Entry<String, Color> entry : colorProperties.entrySet())
//        {
//            firePropertyChange(entry.getKey(), entry.getValue(), null);
//        }
//        colorProperties.clear();
//        
//        for(Map.Entry<String, Paint> entry : paintProperties.entrySet())
//        {
//            firePropertyChange(entry.getKey(), entry.getValue(), null);
//        }
//        paintProperties.clear();
//        
//        for(Map.Entry<String, Font> entry : fontProperties.entrySet())
//        {
//            firePropertyChange(entry.getKey(), entry.getValue(), null);
//        }
//        fontProperties.clear();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Resource Management methods
    
    public Object getProperty(String name)
    {
        Object retVal = properties.get(name);
        
        if((retVal == null) && (parentTable != null))
        {
            retVal = parentTable.getProperty(name);
        }
            
        return retVal;
    }
    
    /**
     * Add a resource property.  If a property with the same name already
     * exist, it will be overridden by the new property value.  If the new
     * value is null, the property will be removed from the table.
     * 
     * @param name the name of the property.
     * @param value the property value.
     */
    public void addProperty(String name, Object value)
    {
        Object oldValue = properties.remove(name);
        
        if(value != null)
        {
            properties.put(name, value);
        }
        
        firePropertyChange(name, oldValue, value);
    }
            
//    /**
//     * Retreives a color resource value.
//     * 
//     * @param name the resource name.
//     * @return The color property or null if the property does not exist.
//     */
//    public Color getColor(String name)
//    {
//        Color retVal = colorProperties.get(name);
//        
//        if((retVal == null) && (parentTable != null))
//        {
//            retVal = parentTable.getColor(name);
//        }
//        
//        return retVal;
//    }
//    
//    /**
//     * Add a color resource property.  If a property with the same name already
//     * exist, it will be overridden by the new property value.  If the new
//     * value is null, the property will be removed from the table.
//     * 
//     * @param name the name of the property.
//     * @param value the property value.
//     */
//    public void addColor(String name, Color value)
//    {
//        Color oldValue = colorProperties.remove(name);
//        
//        if(value != null)
//        {
//            colorProperties.put(name, value);
//        }
//        
//        firePropertyChange(name, oldValue, value);
//    }
//    
//    /**
//     * Retreives a color resource value.
//     * 
//     * @param name the resource name.
//     * @return The color property or null if the property does not exist.
//     */
//    public Font getFont(String name)
//    {
//        Font retVal = fontProperties.get(name);
//        
//        if((retVal == null) && (parentTable != null))
//        {
//            retVal = parentTable.getFont(name);
//        }
//        
//        return retVal;
//    }
//    
//    /**
//     * Add a color resource property.  If a property with the same name already
//     * exist, it will be overridden by the new property value.  If the new
//     * value is null, the property will be removed from the table.
//     * 
//     * @param name the name of the property.
//     * @param value the property value.
//     */
//    public void addFont(String name, Font value)
//    {
//        Font oldValue = fontProperties.remove(name);
//        
//        if(value != null)
//        {
//            fontProperties.put(name, value);
//        }
//        
//        firePropertyChange(name, oldValue, value);
//    }
//    
//    /**
//     * Retreives a paint resource value.
//     * 
//     * @param name the resource name.
//     * @return The paint property or null if the property does not exist.
//     */
//    public Paint getPaint(String name)
//    {
//        Paint retVal = paintProperties.get(name);
//        
//        if((retVal == null) && (parentTable != null))
//        {
//            retVal = parentTable.getPaint(name);
//        }
//        
//        return retVal;
//    }
//    
//    /**
//     * Add a paint resource property.  If a property with the same name already
//     * exist, it will be overridden by the new property value.  If the new
//     * value is null, the property will be removed from the table.
//     * 
//     * @param name the name of the property.
//     * @param value the property value.
//     */
//    public void addPaint(String name, Paint value)
//    {
//        Paint oldValue = paintProperties.remove(name);
//        
//        if(value != null)
//        {
//            paintProperties.put(name, value);
//        }
//        
//        firePropertyChange(name, oldValue, value);
//    }
//    
//    public Set<String> getLocalColorPropertyNames()
//    {
//        return (Set<String>)Collections.unmodifiableSet(colorProperties.keySet());
//    }
//    
//    public Set<String> getLocalPaintPropertyNames()
//    {
//        return (Set<String>)Collections.unmodifiableSet(paintProperties.keySet());
//    }
//    
//    public Set<String> getLocalFontPropertyNames()
//    {
//        return (Set<String>)Collections.unmodifiableSet(fontProperties.keySet());
//    }
    
    public Set<String> getLocalPropertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Listener Management
    
    /**
     * Add a PropertyChangeListener to the listener list. The listener is 
     * registered for all properties. The same listener object may be added 
     * more than once, and will be called as many times as it is added. If 
     * listener is null, no exception is thrown and no action is taken.
     * 
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        
        if (listener instanceof PropertyChangeListenerProxy)
        {
            PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
            addPropertyChangeListener(proxy.getPropertyName(),
                                      proxy.getListener());
        }
        else
        {
            listeners.add(new WeakReference<>(listener));
        }
    }
    
    /**
     * Add a PropertyChangeListener for a specific property. The listener will 
     * be invoked only when a call on firePropertyChange names that specific 
     * property. The same listener object may be added more than once. For each 
     * property, the listener will be invoked the number of times it was added 
     * for that property. If propertyName or listener is null, no exception is 
     * thrown and no action is taken.
     * 
     * @param propertyName The name of the property to listen on.
     * @param listener The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(String propertyName, 
                                          PropertyChangeListener listener)
    {
        ArrayList < WeakReference <PropertyChangeListener> > propListeners = propertyListeners.get(propertyName);
        if(propListeners == null)
        {
            propListeners = new ArrayList < WeakReference <PropertyChangeListener> >();
            propertyListeners.put(propertyName, propListeners);
        }
        
        propListeners.add(new WeakReference < PropertyChangeListener>(listener));
    }
    
    /**
     * Remove a PropertyChangeListener from the listener list. This removes a 
     * PropertyChangeListener that was registered for all properties. If 
     * listener was added more than once to the same event source, it will be 
     * notified one less time after being removed. If listener is null, or was 
     * never added, no exception is thrown and no action is taken.
     * 
     * @param listener The PropertyChangeListener to be rem ove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        
        for(int index = listeners.size() - 1; index >= 0; index--)
        {
            WeakReference<PropertyChangeListener> ref = listeners.get(index);
            PropertyChangeListener refListener = ref.get();
            if(refListener != null)
            {
                if(refListener.equals(listener) == true)
                {
                    listeners.remove(index);
                }
            }
            else
            {
                listeners.remove(index);
            }
        }
    }
    
    /**
     * Remove a PropertyChangeListener for a specific property. If listener was 
     * added more than once to the same event source for the specified property,
     * it will be notified one less time after being removed. If propertyName 
     * is null, no exception is thrown and no action is taken. If listener is 
     * null, or was never added for the specified property, no exception is 
     * thrown and no action is taken.
     * 
     * @param propertyName The name of the property that was listened on.
     * @param listener The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(String propertyName, 
                                          PropertyChangeListener listener)
    {
        ArrayList < WeakReference <PropertyChangeListener> > propListeners = propertyListeners.get(propertyName);
        if(propListeners != null)
        {
            for(int index = propListeners.size() - 1; index >= 0; index--)
            {
                WeakReference<PropertyChangeListener> ref = propListeners.get(index);
                PropertyChangeListener refListener = ref.get();
                if(refListener != null)
                {
                    if(refListener.equals(listener) == true)
                    {
                        propListeners.remove(index);
                    }
                }
                else
                {
                    propListeners.remove(index);
                }
            }
        }
    }
    
    /**
     * Fires a propety change to all registered listeners.  The event will also
     * be sent to all child resource tables as well.
     * 
     * @param name
     * @param oldValue
     * @param newValue
     */
    private void firePropertyChange(String name, Object oldValue, Object newValue)
    {
        
        PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
        firePropertyChange(name, event);
    }
    
    private void firePropertyChange(String name, PropertyChangeEvent event)
    {
        fireEventAndCleanList(listeners, event);

        ArrayList<WeakReference<PropertyChangeListener>> propListeners = propertyListeners.get(name);
        if (propListeners != null)
        {
            fireEventAndCleanList(propListeners, event);
        }


        for(int index = childrenTables.size() - 1; index >= 0; index--)
        {
            WeakReference < ResourceTable > childRef = childrenTables.get(index);
            if(childRef.get() != null)
            {
                childRef.get().notifyPropertyChanged(name, event);
            }
            else
            {
                childrenTables.remove(index);
            }
        }
    }
    
    private void fireParentChangedChange(ResourceTable oldTable, ResourceTable newTable)
    {
        PropertyChangeEvent event = new PropertyChangeEvent(this, PARENT_RESOURCE_TABLE, oldTable, newTable);
        fireEventAndCleanList(listeners, event);

        // Since the parent table changed, the listeners property value may have also changed.
        for(String key : propertyListeners.keySet())
        {   
            // If this table does not contain the key, then the parent specified
            // the value.  Therefore notify that the reource changed.
            if(properties.containsKey(key) == false)
            {
                Object oldResource = oldTable.getProperty(key);
                Object newResource = getProperty(key);
               
                if(oldResource.equals(newResource) == false)
                {
                    firePropertyChange(key, oldResource, newResource);
                }
            }
        }

        for(int index = childrenTables.size() - 1; index >= 0; index--)
        {
            WeakReference < ResourceTable > childRef = childrenTables.get(index);
            if(childRef.get() != null)
            {
                childRef.get().notifyPropertyChanged(PARENT_RESOURCE_TABLE, event);
            }
            else
            {
                childrenTables.remove(index);
            }
        }
    }
    
    private void fireEventAndCleanList(ArrayList <WeakReference<PropertyChangeListener>> refListeners,
                                       PropertyChangeEvent event)
    {
        for(int index = refListeners.size() - 1; index >= 0; index--)
        {
            WeakReference<PropertyChangeListener> ref = refListeners.get(index);
            PropertyChangeListener refListener = ref.get();
            if(refListener != null)
            {   
                refListener.propertyChange(event);
            }
            else
            {
                refListeners.remove(index);
            }
        }
    }
    
    /**
     * Notifies that resource table that a property has been changes.  This is
     * usally sent by a parent resource table.  If the resource is being 
     * overridden by the child resource table a property change event will not 
     * be sent to the child resource table, since the overriden value did not 
     * change.
     * 
     * @param name the name of the property.
     * @param event the event to send if the property is not overridden.
     */
    private void notifyPropertyChanged(String name, PropertyChangeEvent event)
    {
        if(PARENT_RESOURCE_TABLE.equals(name) == true)
        {
            // There is nothing for me to do.  If this table overrides the
            // property, Do not notify listeners of the change.  If this table
            // does not override a property, then the parent that changed will
            // notify via the property event.  Therefore nothing to do.
        }
        else if(properties.containsKey(name) == false)
        {
            firePropertyChange(name, event);
        }
    }
}
