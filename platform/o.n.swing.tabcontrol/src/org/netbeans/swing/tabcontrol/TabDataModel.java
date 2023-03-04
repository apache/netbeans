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

package org.netbeans.swing.tabcontrol;

import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.util.Collections;
import java.util.List;

/**
 * A data model representing a set of tabs and their associated data. Allows for
 * atomic add/remove/modification operations any of which are guaranteed to fire
 * only one event on completion.  Note that for modification operations
 * (<code>setText()</code>, <code>setIcon</code>, <code>setIconsAndText</code>,
 * no event will be fired unless data is actually changed - calling these
 * methods with the same values that the tabs already have will not generate
 * events. The <code>isWidthChanged</code> method for generated events will
 * return <code>true</code> for events which can affect the area needed to
 * display a tab (such as text or icon width changes).
 * <p>
 * Note:  The standard UI implementations which use this model make no provisions
 * for thread-safety.  All changes fired from a TabDataModel should happen on the
 * AWT event thread. 
 *
 * @author Tim Boudreau
 */
public interface TabDataModel {
    /**
     * The number of tabs contained in the model.
     *
     * @return The number of tabs
     */
    public int size();

    /**
     * Retrieve data for a given tab
     *
     * @param index The index for which to retrieve tab data
     * @return Data describing the tab
     */
    public TabData getTab(int index);

    /**
     * Set the tab data for a given tab to the passed value
     *
     * @param index The index of the tab to be changed
     * @param data  The new tab data for this index
     */
    public void setTab(int index, TabData data);

    /**
     * Set the icon for a given tab.  Will trigger a list data event, and the
     * resulting event's widthChanged property will be set appropriately if the
     * displayed width has changed.
     *
     * @param index The index to set the icon for
     * @param i     The icon to use for the tab
     */
    public void setIcon(int index, Icon i);

    /**
     * Set the text for a given tab.  Triggers a list data event.
     *
     * @param index The index of the tab
     * @param txt   The replacement text
     */
    public void setText(int index, String txt);

    /**
     * Atomically set the icons for a set of indices.  Fires a single list data
     * event with the indexes of any tabs in which the data was actually
     * changed.  If the passed data perfectly match the existing data, no event
     * will be fired.
     *
     * @param indices The indices for which the corresponding icons should be
     *                changed
     * @param icons   The replacement icons.  This array must be the same length
     *                as the indices parameter
     */
    public void setIcon(int[] indices, Icon[] icons);

    /**
     * Atomically set the text for a number of tabs.  Fires a single list data
     * event with the indexes of any tabs in which the data was actually
     * changed.  If the passed data perfectly match the existing data, no event
     * will be fired.1
     *
     * @param indices The indices of the tabs to change
     * @param txt     The text values for the tabs
     */
    public void setText(int[] indices, String[] txt);

    /**
     * Atomically set the icons and text simultaneously for more than one tab.
     * Fires a single list data event with the indexes of any tabs in which the
     * data was actually changed.  If the passed data perfectly match the
     * existing data, no event will be fired.1
     *
     * @param indices The indices which should have their data changed
     * @param txt     The replacement text values corresponding to the passed
     *                indices
     * @param icons   The replacement icons corresponding to the passed indices
     */
    public void setIconsAndText(int[] indices, String[] txt, Icon[] icons);

    /**
     * Atomically add a set of tabs at the specified index
     *
     * @param start The insert point for new tabs
     * @param data  The tab data to insert
     */
    public void addTabs(int start, TabData[] data);

    /**
     * Remove the tab at the specified index
     *
     * @param index The tab index
     */
    public void removeTab(int index);

    /**
     * Add the specified tabs at the specified indices
     *
     * @param indices The indices at which tabs will be added
     * @param data    The tabs to add, in order corresponding to the indices
     *                parameter
     */
    public void addTabs(int[] indices, TabData[] data);

    /**
     * Replace the entire set of tabs represented by the model
     */
    public void setTabs(TabData[] data);

    /**
     * Remove the tabs at the specified indices
     *
     * @param indices The indices at which tabs should be removed
     */
    public void removeTabs(int[] indices);

    /**
     * Remove a range of tabs
     *
     * @param start the start index
     * @param end   the end index (exclusive)
     */
    public void removeTabs(int start, int end);

    /**
     * Add a single tab at the specified location
     */
    public void addTab(int index, TabData data);

    /**
     * Retrieve all the tab data contained in the model as a List
     *
     * @return a List of TabData objects
     */
    public java.util.List<TabData> getTabs();

    /**
     * Fetch the index of a tab matching the passed TabData object.  Note that
     * the tooltip property of the passed TabData object is not used to test
     * equality.
     *
     * See also <a href="@org-netbeans-core-windows@/org/netbeans/core/windows/ui/TabData.html#equals()">org.netbeans.core.windows.ui.TabData.equals()</a><BR>
     */
    public int indexOf(TabData td);

    /**
     * Add a data listener
     *
     * @param listener The listener
     */
    public void addComplexListDataListener(ComplexListDataListener listener);

    /**
     * Remove a data listener
     *
     * @param listener The listener
     */
    public void removeComplexListDataListener(ComplexListDataListener listener);
    
    
    //XXX remove this and handel the ComplexNNN events so nothing is repainted
    //if not displayed on screen!
    /**
     * The model will fire a change event whenever a modification occurs that
     * could require a repaint. <strong>This method is only here for the
     * prototype - eventually the UI delegate should listen for ComplexDataNN
     * events and optimize repaints based on the actual areas affected.
     */
    public void addChangeListener(ChangeListener listener);

    /**
     * The model will fire a change event whenever a modification occurs that
     * could require a repaint. <strong>This method is only here for the
     * prototype - eventually the UI delegate should listen for ComplexDataNN
     * events and optimize repaints based on the actual areas affected.
     */
    public void removeChangeListener(ChangeListener listener);
}
