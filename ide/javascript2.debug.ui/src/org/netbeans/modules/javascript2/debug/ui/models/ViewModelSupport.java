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
package org.netbeans.modules.javascript2.debug.ui.models;

import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent.TreeChanged;


/**
 * @author ads
 *
 */
public abstract class ViewModelSupport {

    protected static final Object[] EMPTY_CHILDREN = new Object[]{};

    private final CopyOnWriteArrayList<ModelListener> myListeners;

    protected ViewModelSupport() {
        myListeners = new CopyOnWriteArrayList<>();
    }

    public final void addModelListener(ModelListener l) {
        myListeners.add(l);
    }

    public final void removeModelListener(ModelListener l) {
        myListeners.remove(l);
    }

    protected final void refresh() {
        fireChangeEvent(new TreeChanged(this));
    }

    protected final void fireChangeEvent(ModelEvent modelEvent) {
        for ( ModelListener listener : myListeners ) {
            listener.modelChanged(modelEvent);
        }
    }

    protected final void fireChangeEvents(ModelEvent[] events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }

    protected final void fireChangeEvents(Collection<ModelEvent> events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }

    public static String toHTML(String text) {
        return toHTML(text, false, false, null);
    }

    public static String toHTML(String text, boolean bold, boolean italics,
                                Color color) {
        if (text == null) return null;
        if (text.length() > 6 && text.substring(0, 6).equalsIgnoreCase("<html>")) {
            return text; // Already HTML
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        if (bold) {
            sb.append("<b>");
        }
        if (italics) {
            sb.append("<i>");
        }
        Color fontColor = color;
        if (fontColor == null) {
            fontColor = UIManager.getColor("Table.foreground");
            if (fontColor == null) {
                fontColor = new JTable().getForeground();
            }
        }
        sb.append("<font color=\"");
        sb.append(String.format(
                "#%02x%02x%02x",
                fontColor.getRed(),
                fontColor.getGreen(),
                fontColor.getBlue()
        ));
        sb.append("\">");
        sb.append(text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
        sb.append("</font>");
        if (italics) {
            sb.append("</i>");
        }
        if (bold) {
            sb.append("</b>");
        }
        sb.append("</html>");
        return sb.toString ();
    }

}
