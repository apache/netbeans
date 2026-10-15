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

package org.netbeans.modules.editor.lib2.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.ListenerList;

/**
 * SPI class allowing to produce views.
 * <br>
 * There are two main factories: for folds and for highlights (the default one).
 * The factories have a priority and factory with highest priority
 * will always "win" in terms that its view will surely be created as desired.
 * Factories at lower levels will receive a limitOffset into createView()
 * being a start offset of the view produced by a higher level factory.
 * The factory may decide whether it will create view with limiting offset or not.
 * <br>
 * Factory generally operates in two modes:<ul>
 * <li>Regular mode when the factory produces views</li>
 * <li>Offset mode when the factory only returns bounds of the produced views
 *   but it does not create them. This helps the view hierarchy infrastructure
 *   to do estimates (e.g. how many lines a fold view will span etc.).</li>
 * </ul>
 *
 * @author Miloslav Metelka
 */

public abstract class EditorViewFactory {

    /**
     * Copy-on-write & unmodifiable collection, the instance can be returned to the clients.
     */
    private static volatile List<Factory> viewFactoryFactories = Collections.emptyList();

    public static synchronized void registerFactory(Factory factory) {
        List<Factory> copy = new ArrayList<Factory>(viewFactoryFactories);
        
        copy.add(factory);
        copy.sort(new Comparator<Factory>() {
            public int compare(Factory f0, Factory f1) {
                return f0.weight() - f1.weight();
            }
        });
        viewFactoryFactories = Collections.unmodifiableList(copy);
    }

    public static List<Factory> factories() {
        return viewFactoryFactories;
    }
    
    private DocumentView docView;
    
    private JTextComponent component;

    private ViewBuilder viewBuilder;

    private final ListenerList<EditorViewFactoryListener> listenerList = new ListenerList<EditorViewFactoryListener>();

    protected EditorViewFactory(View documentView) {
        assert (documentView instanceof DocumentView) : "documentView=" + documentView + // NOI18N
                " is not instance of " + DocumentView.class.getName(); // NOI18N
        // Remember component explicitly (it may be null-ed in DocView.setParent(null))
        this.docView = (DocumentView) documentView;
        this.component = docView.getTextComponent();
    }

    /**
     * Text component for which this view factory was constructed.
     *
     * @return text component or null if this view factory is released.
     */
    protected final JTextComponent textComponent() {
        return component;
    }
    
    /**
     * Document for which this view factory was constructed.
     * <b>Note</b>: Do not use <code>textComponent().getDocument()</code> since
     * it may differ from <code>document()</code> result at certain points
     * and it could lead to incorrect behavior.
     *
     * @return document for which the view hierarchy was constructed
     *   or null if this view factory is released.
     */
    protected final Document document() {
        return (docView != null) ? docView.getDocument() : null;
    }

    /**
     * Restart this view factory to start producing views.
     *
     * @param startOffset first offset from which the views will be produced.
     * @param endOffset offset where the view creation should end.
     *  Original views should match with the new ones at this offset (or earlier).
     *  However during the views creation it may be found out that this offset
     *  will be exceeded and if so then {@link #continueCreation(int, int)} gets called.
     * @param createViews If false then no physical views will be created
     *  {@link #createView(int, int, org.netbeans.modules.editor.lib2.view.EditorView)}
     *   will not be called and solely {@link #viewEndOffset(int, int)} will be called
     *   to give info about each potential view boundaries.
     */
    public abstract void restart(int startOffset, int endOffset, boolean createViews);

    /**
     * Notify next offset area where views will be created in case endCreationOffset
     * in {@link #restart(int, int)} was exceeded.
     * <br>
     * This method may be called multiple times if views building still does not match
     * original views boundaries.
     *
     * @param startOffset start offset (usually it's an original endCreationOffset).
     * @param endCreationOffset currently planned end of views creation offset.
     *  Unless the view hierarchy has custom bounds the endOffset typically points
     *  to end-offset of a line element.
     */
    public abstract void continueCreation(int startOffset, int endOffset);

    /**
     * Return starting offset of the next view to be produced by this view factory.
     * <br>
     * This method gets called after restarting of this view factory
     * (with a <code>startOffset</code> parameter passed to {@link #restart(int)})
     * and also after any of the registered view factories created a view
     * (with end offset of the created view).
     *
     * @param offset offset at which (or after which) a possible new view should be created.
     * @return start offset of the new view to be created or Integer.MAX_VALUE to indicate that
     *  no more views would be produced by this factory over the given offset.
     */
    public abstract int nextViewStartOffset(int offset);

    /**
     * Create a view at the given offset. The view factory must determine
     * the appropriate end offset of the produced view and set its length
     * returned by {@link EditorView#getLength()} appropriately.
     * <br>
     * This method is only called if the factory is in view-producing mode
     * (its {@link #viewEndOffset(startOffset, limitOffset)} is not called).
     *
     * @param startOffset start offset at which the view must start
     *  (it was previously returned from {@link #nextViewStartOffset(int)} by this factory
     *   and {@link EditorView#getStartOffset()} must return it).
     * @param limitOffset maximum end offset that the created view should have.
     *  It is lower than (or equal) to endOffset given in {@link #restart(int, int, boolean) }
     *  or {@link #continueCreation(int, int) }. It may be exceeded unless
     *  forcedLimit parameter is true.
     * @param forcedLimit whether view factory is obliged to respect limitOffset. It means
     *  that a view factory with higher weight will create view at limitOffset.
     * @param origView original view located at the given position (it may have a different
     *  physical offset due to just performed modification but it corresponds to the same text
     *  in the document). It may be null if there is no view to reuse.
     *  <br>
     *  The factory may not return the given instance but it may reuse an arbitrary information
     *  from it.
     *  <br>
     *  For example for text layout reuse the highlights view factory will first check if the view
     *  is non-null and matches views produced by it then it will check
     *  if the new view has same length as the original one and that the view attributes
     *  give the same font like original one. Then the original text layout may reused for the new view.
     * @param nextOrigViewOffset offset where an original view (possibly passed as origView) ends.
     *  Currently this is only used by highlights view factory for very long lines which
     *  (for performance reasons) are typically broken into multiple shorter views (text layouts).
     *  even though normally a single view (text layout) could be used. In case one of these shorter views
     *  gets modified all the subsequent shorter views could become recreated with their boundaries
     *  shifted up/down according to the modification. That is undesirable from the text layout reuse
     *  point of view. Therefore the factory may check when (nextOrigViewOffset - startOffset)
     *  is bigger than certain threshold and possibly end the view at nextOrigViewOffset.
     *  
     * @return EditorView instance or null if limitOffset is too limiting
     *  for the view that would be otherwise created.
     */
    public abstract EditorView createView(int startOffset, int limitOffset, boolean forcedLimit,
    EditorView origView, int nextOrigViewOffset);

    /**
     * Return to-be-created view's end offset.
     * <br>
     * This method is only called when createViews parameter
     * in {@link #restart(int, int, boolean)} is false. In such mode no physical views
     * are created and only view boundaries of potential views are being determined.
     *
     * @param startOffset start offset at which the view would start
     *  (it was previously returned from {@link #nextViewStartOffset(int)} by this factory).
     * @param limitOffset maximum end offset that the created view should have.
     *  It is lower than (or equal) to endOffset given in {@link #restart(int, int, boolean) }
     *  or {@link #continueCreation(int, int) }. It may be exceeded unless
     *  forcedLimit parameter is true.
     * @param forcedLimit whether view factory is obliged to respect limitOffset.
     * @return end offset of the view to be created or -1 if view's creation is refused by the factory.
     */
    public abstract int viewEndOffset(int startOffset, int limitOffset, boolean forcedLimit);

    /**
     * Finish this round of views creation.
     * <br>
     * {@link #restart(int) } may be called subsequently to init a new round
     * of views creation.
     */
    public abstract void finishCreation();

    public void addEditorViewFactoryListener(EditorViewFactoryListener listener) {
        listenerList.add(listener);
    }

    public void removeEditorViewFactoryListener(EditorViewFactoryListener listener) {
        listenerList.remove(listener);
    }

    protected void fireEvent(List<EditorViewFactoryChange> changes) {
        EditorViewFactoryEvent evt = new EditorViewFactoryEvent(this, changes);
        for (EditorViewFactoryListener listener : listenerList.getListeners()) {
            listener.viewFactoryChanged(evt);
        }
    }

    /**
     * Schedule repaint request on the view hierarchy.
     * <br>
     * Document must be read-locked prior calling this method.
     *
     * @param startOffset
     * @param endOffset 
     */
    public void offsetRepaint(int startOffset, int endOffset) {
        docView.offsetRepaint(startOffset, endOffset);
    }

    /**
     *  Signal that this view factory is no longer able to produce
     *  valid views due to some serious changes that it processes
     *  (for example highlights change for HighlightsViewFactory).
     *  <br>
     *  View creation may be stopped immediately by the caller and restarted to get
     *  the correct views. However if it would fail periodically the caller may decide
     *  to continue the creation to have at least some views. In both cases
     *  the view factory should be able to continue working normally.
     *  <br>
     *  This method can be called from any thread.
     */
    protected final void notifyStaleCreation() {
        ViewBuilder builder = viewBuilder;
        if (builder != null) {
            builder.notifyStaleCreation();
        }
    }
    
    void setViewBuilder(ViewBuilder viewBuilder) {
        this.viewBuilder = viewBuilder;
    }

    public final boolean isReleased() {
        return (docView == null);
    }

    /**
     * Notification that this factory is no longer being used so it should
     * release its resources - for example detach all listeners.
     * <br>
     * It's called upon document view receives setParent(null) which typically signals
     * that a new document view will be created for the particular editor pane.
     */
    protected void released() {
    }
    
    void releaseAll() {
        released(); // Is allowed to use docView and component info before subsequent clearing
        this.docView = null;
        this.component = null;
        // this.viewBuilder should be null-ed in try..finally manner
    }
    
    /**
     * Used by highlights view factory. If there would be a high demand for this method
     * it might become public although the view factories are used at times when
     * the document view is not in a "stable" state so the factories would have to be careful.
     * @return 
     */
    DocumentView documentView() {
        return docView;
    }
    
    @Override
    public String toString() {
        ViewBuilder vb = viewBuilder;
        return "viewBuilder:\n" + vb; // NOI18N
    }

    /**
     * Change that occurred in a view factory either due to insert/remove in a document
     * or due to some other cause.
     * For example when a fold gets collapsed the fold view factory fires an event with the change.
     */
    public static final class Change {

        private final int startOffset;

        private final int endOffset;

        Change(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public String toString() {
            return "<" + getStartOffset() + "," + getEndOffset() + ">";
        }

    }

    /**
     * Factory for producing editor view factories.
     */
    public static interface Factory {

        EditorViewFactory createEditorViewFactory(View documentView);

        /**
         * A factory with higher weight wins when wishing to create view
         * in the same offset area.
         *
         * @return weight &gt;0. A default factory for creating basic views has weight 0.
         */
        int weight();

    }

}
