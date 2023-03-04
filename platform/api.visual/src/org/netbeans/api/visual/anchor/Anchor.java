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
package org.netbeans.api.visual.anchor;

import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;
import java.util.EnumSet;
import java.util.Collections;
import java.util.ArrayList;

/**
 * This class represents an anchor for connections. An anchor is usually attached to widget and resolves a source or target
 * point of a connection where it is used. Single instance of an anchor could be used by multiple entries. An entry represents
 * the place where the anchor is used. An anchor can by attached to so-called proxy-anchor also.
 * The proxy-anchor uses a set of anchor and allows smooth switching of the active anchor.
 *
 * @author David Kaspar
 */
public abstract class Anchor implements Widget.Dependency {

    /**
     * The set of all orthogonal directions.
     */
    public static final EnumSet<Direction> DIRECTION_ANY = EnumSet.allOf (Direction.class);

    /**
     * The direction of the anchor. Used by orthogonal routing alogorithms to resolve which way the path can be directed.
     */
    public enum Direction {
        LEFT, TOP, RIGHT, BOTTOM
    }

    private boolean attachedToWidget;
    private Widget relatedWidget;
    private ArrayList<Entry> entries = new ArrayList<Entry> ();

    /**
     * Creates an anchor that is related to a widget.
     * @param relatedWidget the related widget; if null then the anchor is not related to any widget
     */
    protected Anchor (Widget relatedWidget) {
        this.relatedWidget = relatedWidget;
    }

    /**
     * Called by ConnectionWidget to register the usage of the anchor.
     * @param entry the anchor entry
     */
    public final void addEntry (Anchor.Entry entry) {
        if (entry == null)
            return;
        notifyEntryAdded (entry);
        entries.add (entry);
        if (! attachedToWidget  &&  entries.size () > 0) {
            attachedToWidget = true;
            if (relatedWidget != null)
                relatedWidget.addDependency (this);
            notifyUsed ();
        }
        revalidateDependency ();
    }

    /**
     * Called by ConnectionWidget to unregister the usage of the anchor.
     * @param entry the anchor entry
     */
    public final void removeEntry (Entry entry) {
        entries.remove (entry);
        notifyEntryRemoved (entry);
        if (attachedToWidget  &&  entries.size () <= 0) {
            attachedToWidget = false;
            if (relatedWidget != null)
                relatedWidget.removeDependency (this);
            notifyUnused ();
        }
        revalidateDependency ();
    }

    /**
     * Registers multiple entries at once.
     * @param entries a list of entries
     */
    public final void addEntries (List<Entry> entries) {
        for (Entry entry : entries)
            addEntry (entry);
    }

    /**
     * Unregisters multiple entries at once.
     * @param entries a list of entries
     */
    public final void removeEntries (List<Entry> entries) {
        for (Entry entry : entries)
            removeEntry (entry);
    }

    /**
     * Returns a list of registered entries
     * @return the list of entries
     */
    public final List<Entry> getEntries () {
        return Collections.unmodifiableList (entries);
    }

    /**
     * Notifies when an entry is registered
     * @param entry the registered entry
     */
    protected void notifyEntryAdded (Entry entry) {
    }

    /**
     * Notifies when an entry is unregistered
     * @param entry the unregistered entry
     */
    protected void notifyEntryRemoved (Entry entry) {
    }

    /**
     * Returns whether the anchor is used.
     * @return true if there is at least one entry registered
     */
    protected final boolean isUsed () {
        return attachedToWidget;
    }

    /**
     * Notifies when the anchor is going to be used.
     */
    protected void notifyUsed () {
    }

    /**
     * Notifies when the anchor is going to be not used.
     */
    protected void notifyUnused () {
    }

    /**
     * Notifies when the anchor is going to be revalidated.
     * @since 2.8
     */
    protected void notifyRevalidate () {
    }

    /**
     * This method is called by revalidation-change of related widget and notifies all entries about the anchor change.
     */
    public final void revalidateDependency () {
        notifyRevalidate ();
        for (Entry entry : entries)
            entry.revalidateEntry ();
    }

    /**
     * Returns a related widget.
     * @return the related widget; null if no related widget is assigned
     */
    public Widget getRelatedWidget () {
        return relatedWidget;
    }

    /**
     * Returns a scene location of a related widget.
     * @return the scene location; null if no related widget is assigned
     */
    public Point getRelatedSceneLocation () {
        if (relatedWidget != null) {
            Rectangle bounds = relatedWidget.getBounds ();
            if (bounds == null)
                throw new IllegalStateException ("Widget (" + relatedWidget + ") was not added into the scene");
            return GeomUtil.center (relatedWidget.convertLocalToScene (bounds));
        }
        assert false : "Anchor.getRelatedSceneLocation has to be overridden when a related widget is not used";
        return null;
    }

    /**
     * Returns a scene location of a related widget of an opposite anchor.
     * @param entry the entry to which the opposite anchor searched
     * @return the scene location
     */
    public Point getOppositeSceneLocation (Entry entry) {
        Anchor oppositeAnchor = entry.getOppositeAnchor ();
        return oppositeAnchor != null ? oppositeAnchor.getRelatedSceneLocation () : null;
    }

    public boolean allowsArbitraryConnectionPlacement() {
        return false ;
    }

    public List<Point> compute(List<Point> bestPoints) {
        return bestPoints ;
    }
    
    /**
     * Computes a result (position and direction) for a specific entry.
     * @param entry the entry
     * @return the calculated result
     */
    public abstract Result compute (Entry entry);

    /**
     * Represents calculated scene location and orthogonal direction(s) of an anchor.
     * Usually created within Anchor.compute method and used by Router.
     */
    public final class Result {

        private Point anchorSceneLocation;
        private EnumSet<Anchor.Direction> directions;

        /**
         * Creates a result with calculated scene location and single direction.
         * @param anchorSceneLocation the scene location of an anchor
         * @param direction the single direction of an anchor
         */
        public Result (Point anchorSceneLocation, Direction direction) {
            this (anchorSceneLocation, EnumSet.of (direction));
        }

        /**
         * Creates a result with calculated scene location and possible directions.
         * @param anchorSceneLocation the scene location of an anchor
         * @param directions the possible directions of an anchor
         */
        public Result (Point anchorSceneLocation, EnumSet<Direction> directions) {
            this.anchorSceneLocation = anchorSceneLocation;
            this.directions = directions;
        }

        /**
         * Returns a scene location of an anchor.
         * @return the scene location
         */
        public Point getAnchorSceneLocation () {
            return anchorSceneLocation;
        }

        /**
         * Returns possible directions of an anchor.
         * @return the directions
         */
        public EnumSet<Direction> getDirections () {
            return directions;
        }

    }

    /**
     * Represents a place where an anchor is used. Usually it is implemented by ConnectionWidget.getSourceAnchorEntry or
     * ConnectionWidget.getTargetAnchorEntry or ProxyAnchor.
     */
    public interface Entry {

        /**
         * Called for notifying that an anchor is changed and the entry has to me revalidated too.
         * Usually called by Anchor.revalidateDependency.
         */
        void revalidateEntry ();

        /**
         * Returns a connection widget where the entry is attached to.
         * @return the connection widget
         */
        ConnectionWidget getAttachedConnectionWidget ();

        /**
         * Returns whether an entry is attached to the source or the target of a connection widget.
         * @return true if attached to the source
         */
        boolean isAttachedToConnectionSource ();

        /**
         * Returns an anchor of a connection widget which relates to the entry.
         * @return the attached anchor
         */
        Anchor getAttachedAnchor ();

        /**
         * Returns an anchor of a connection widget which is opposite to the related anchor
         * @return the opposite anchor
         */
        Anchor getOppositeAnchor ();

    }

}
