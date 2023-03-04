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

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.FoldingToolTip;
import org.netbeans.editor.FontMetricsCache;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.editor.view.spi.LockView;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * View over collapsed area of the fold.
 * <br>
 * The collapsed area spans one or more lines and it is presented as three dots.
 *
 * @author Martin Roskanin
 */
/* package */ @Deprecated class CollapsedView extends View {

    private static final int MARGIN_WIDTH = 4;
    
    private final Position startPos;
    
    private final Position endPos;
    
    private final String foldDescription;
    
    private volatile AttributeSet attribs;
    private Lookup.Result<? extends FontColorSettings> fcsLookupResult;
    private final LookupListener fcsTracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            attribs = null;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JTextComponent jtc = CollapsedView.this.getComponent();
                    if (jtc != null) {
                        CollapsedView.this.getBaseTextUI().damageRange(
                            jtc, CollapsedView.this.getStartOffset(), CollapsedView.this.getEndOffset());
                    }
                }
            });
        }
    };
    
    /** Creates a new instance of CollapsedView */
    public CollapsedView(Element elem, Position startPos, Position endPos, String foldDescription) {
        super(elem);
        
        this.startPos = startPos;
        this.endPos = endPos;
        this.foldDescription = foldDescription;
    }

    private Coloring getColoring() {
        if (attribs == null) {
            if (fcsLookupResult == null) {
                fcsLookupResult = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(getComponent()))
                        .lookupResult(FontColorSettings.class);
                fcsLookupResult.addLookupListener(WeakListeners.create(LookupListener.class, fcsTracker, fcsLookupResult));
            }
            
            FontColorSettings fcs = fcsLookupResult.allInstances().iterator().next();
            AttributeSet attr = fcs.getFontColors(FontColorNames.CODE_FOLDING_COLORING);
            if (attr == null) {
                attr = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            } else {
                attr = AttributesUtilities.createComposite(attr, fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
            }
            
            attribs = attr;
        }        
        return Coloring.fromAttributeSet(attribs);
    }
    
    private JTextComponent getComponent() {
        return (JTextComponent)getContainer();
    }
    
    private BaseTextUI getBaseTextUI(){
        JTextComponent comp = getComponent();
        return (comp!=null)?(BaseTextUI)comp.getUI():null;
    }
    
    private EditorUI getEditorUI(){
        BaseTextUI btui = getBaseTextUI();
        return (btui!=null) ? btui.getEditorUI() : null;
    }
    
    public @Override Document getDocument() {
        View parent = getParent();
        return (parent == null) ?  null : parent.getDocument();
    }
    
    public @Override int getStartOffset() {
        return startPos.getOffset();
    }
    
    public @Override int getEndOffset() {
        return endPos.getOffset();
    }
    
    public @Override float getAlignment(int axis) {
	return 0f;
    }
    
    public float getPreferredSpan(int axis){
        switch (axis) {
            case Y_AXIS:
                return getEditorUI().getLineHeight();
            case X_AXIS:
                return getCollapsedFoldStringWidth();
        }
        return 1f;
    }

    private int getCollapsedFoldStringWidth() {
        JTextComponent comp = getComponent();
        if (comp==null) return 0;
        FontMetrics fm = FontMetricsCache.getFontMetrics(getColoring().getFont(), comp);
        if (fm==null) return 0;
        return fm.stringWidth(foldDescription) + 2 * MARGIN_WIDTH;
    }
    
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        return new Rectangle(a.getBounds().x, a.getBounds().y, getCollapsedFoldStringWidth(), getEditorUI().getLineHeight());
    }
    
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        return getStartOffset();
    }
    
    public void paint(Graphics g, Shape allocation){
        Rectangle r = allocation.getBounds();
        Coloring coloring = getColoring();

        g.setColor(coloring.getBackColor());
        g.fillRect(r.x, r.y, r.width, r.height);
        
        g.setColor(coloring.getForeColor());
        g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        
        g.setFont(coloring.getFont());
        g.drawString(foldDescription, r.x + MARGIN_WIDTH, r.y + getEditorUI().getLineAscent() - 1);
    }
    
    public @Override int getNextVisualPositionFrom(
        int pos, Position.Bias b, Shape a, 
        int direction, Position.Bias[] biasRet
    ) throws BadLocationException {
	biasRet[0] = Position.Bias.Forward;
	switch (direction) {
	case NORTH:
	case SOUTH:
	{
	    JTextComponent target = (JTextComponent) getContainer();
	    Caret c = (target != null) ? target.getCaret() : null;
	    // YECK! Ideally, the x location from the magic caret position
	    // would be passed in.
	    Point mcp;
	    if (c != null) {
		mcp = c.getMagicCaretPosition();
	    }
	    else {
		mcp = null;
	    }
	    int x;
	    if (mcp == null) {
		Rectangle loc = target.modelToView(pos);
		x = (loc == null) ? 0 : loc.x;
	    }
	    else {
		x = mcp.x;
	    }
	    if (direction == NORTH) {
		pos = Utilities.getPositionAbove(target, pos, x);
	    }
	    else {
		pos = Utilities.getPositionBelow(target, pos, x);
	    }
	}
	    break;
	case WEST:
	    if(pos == -1) {
		pos = Math.max(0, getStartOffset());
	    }
	    else {
                if (b == Position.Bias.Backward){
                    pos = Math.max(0, getStartOffset());
                }else{
                    pos = Math.max(0, getStartOffset() - 1);
                }
	    }
	    break;
	case EAST:
	    if(pos == -1) {
		pos = getStartOffset();
	    }
	    else {
		pos = Math.min(getEndOffset(), getDocument().getLength());
                //JTextComponent target = (JTextComponent) getContainer();
                //if (target!=null && Utilities.getRowEnd(target, pos) == pos) pos = Math.min(pos+1, getDocument().getLength());
	    }
	    break;
	default:
	    throw new IllegalArgumentException("Bad direction: " + direction); // NOI18N
	}
	return pos;
    }    

    private View getExpandedView(){
        Element parentElem = getElement().getParentElement();
        int sei = parentElem.getElementIndex(getStartOffset());
        int so = parentElem.getElement(sei).getStartOffset();
        
        int eei = parentElem.getElementIndex(getEndOffset());
        int eo = parentElem.getElement(eei).getEndOffset();
        
        LockView fakeView = new LockView(
        new DrawEngineFakeDocView(parentElem, so, eo, false, true)
        );
        RootView rootView = new RootView();
        rootView.setView(fakeView);
        return fakeView;
    }
    
    public @Override String getToolTipText(float x, float y, Shape allocation){
        ToolTipSupport tts = getEditorUI().getToolTipSupport();
        JComponent toolTip = new FoldingToolTip(getExpandedView(), getEditorUI());
        tts.setToolTip(toolTip, PopupManager.ScrollBarBounds, PopupManager.Largest, -FoldingToolTip.BORDER_WIDTH, 0);
        return ""; //NOI18N
    }
    
    class RootView extends View {

        RootView() {
            super(null);
        }

        void setView(View v) {
            if (view != null) {
                // get rid of back reference so that the old
                // hierarchy can be garbage collected.
                view.setParent(null);
            }
            view = v;
            if (view != null) {
                view.setParent(this);
            }
        }

	/**
	 * Fetches the attributes to use when rendering.  At the root
	 * level there are no attributes.  If an attribute is resolved
	 * up the view hierarchy this is the end of the line.
	 */
        public @Override AttributeSet getAttributes() {
	    return null;
	}

        /**
         * Determines the preferred span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public float getPreferredSpan(int axis) {
            if (view != null) {
                return view.getPreferredSpan(axis);
            } else {
                return 10;
            }
        }

        /**
         * Determines the minimum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public @Override float getMinimumSpan(int axis) {
            if (view != null) {
                return view.getMinimumSpan(axis);
            } else {
                return 10;
            }
        }

        /**
         * Determines the maximum span for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the span the view would like to be rendered into.
         *         Typically the view is told to render into the span
         *         that is returned, although there is no guarantee.
         *         The parent may choose to resize or break the view.
         */
        public @Override float getMaximumSpan(int axis) {
	    return Integer.MAX_VALUE;
        }

        /**
         * Determines the desired alignment for this view along an axis.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the desired alignment, where 0.0 indicates the origin
         *     and 1.0 the full span away from the origin
         */
        public @Override float getAlignment(int axis) {
            if (view != null) {
                return view.getAlignment(axis);
            } else {
                return 0;
            }
        }

        /**
         * Renders the view.
         *
         * @param g the graphics context
         * @param allocation the region to render into
         */
        public void paint(Graphics g, Shape allocation) {
            if (view != null) {
                Rectangle alloc = (allocation instanceof Rectangle) ?
		          (Rectangle)allocation : allocation.getBounds();
		setSize(alloc.width, alloc.height);
                view.paint(g, allocation);
            }
        }
        
        /**
         * Sets the view parent.
         *
         * @param parent the parent view
         */
        public @Override void setParent(View parent) {
            throw new Error("Can't set parent on root view"); // NOI18N
        }

        /** 
         * Returns the number of views in this view.  Since
         * this view simply wraps the root of the view hierarchy
         * it has exactly one child.
         *
         * @return the number of views
         * @see #getView
         */
        public @Override int getViewCount() {
            return 1;
        }

        /** 
         * Gets the n-th view in this container.
         *
         * @param n the number of the view to get
         * @return the view
         */
        public @Override View getView(int n) {
            return view;
        }

	/**
	 * Returns the child view index representing the given position in
	 * the model.  This is implemented to return the index of the only
	 * child.
	 *
	 * @param pos the position >= 0
	 * @return  index of the view representing the given position, or 
	 *   -1 if no view represents that position
	 * @since 1.3
	 */
        public @Override int getViewIndex(int pos, Position.Bias b) {
	    return 0;
	}
    
        /**
         * Fetches the allocation for the given child view. 
         * This enables finding out where various views
         * are located, without assuming the views store
         * their location.  This returns the given allocation
         * since this view simply acts as a gateway between
         * the view hierarchy and the associated component.
         *
         * @param index the index of the child
         * @param a  the allocation to this view.
         * @return the allocation to the child
         */
        public @Override Shape getChildAllocation(int index, Shape a) {
            return a;
        }

        /**
         * Provides a mapping from the document model coordinate space
         * to the coordinate space of the view mapped to it.
         *
         * @param pos the position to convert
         * @param a the allocated region to render into
         * @return the bounding box of the given position
         */
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            if (view != null) {
                return view.modelToView(pos, a, b);
            } else {
                return null;
            }
        }

	/**
	 * Provides a mapping from the document model coordinate space
	 * to the coordinate space of the view mapped to it.
	 *
	 * @param p0 the position to convert >= 0
	 * @param b0 the bias toward the previous character or the
	 *  next character represented by p0, in case the 
	 *  position is a boundary of two views. 
	 * @param p1 the position to convert >= 0
	 * @param b1 the bias toward the previous character or the
	 *  next character represented by p1, in case the 
	 *  position is a boundary of two views. 
	 * @param a the allocated region to render into
	 * @return the bounding box of the given position is returned
	 * @exception BadLocationException  if the given position does
	 *   not represent a valid location in the associated document
	 * @exception IllegalArgumentException for an invalid bias argument
	 * @see View#viewToModel
	 */
	public @Override Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
	    if (view != null) {
		return view.modelToView(p0, b0, p1, b1, a);
	    } else {
                return null;
            }
	}

        /**
         * Provides a mapping from the view coordinate space to the logical
         * coordinate space of the model.
         *
         * @param x x coordinate of the view location to convert
         * @param y y coordinate of the view location to convert
         * @param a the allocated region to render into
         * @return the location within the model that best represents the
         *    given point in the view
         */
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            if (view != null) {
                int retValue = view.viewToModel(x, y, a, bias);
		return retValue;
            } else {
                return -1;
            }
        }

        /**
         * Provides a way to determine the next visually represented model 
         * location that one might place a caret.  Some views may not be visible,
         * they might not be in the same order found in the model, or they just
         * might not allow access to some of the locations in the model.
         *
         * @param pos the position to convert >= 0
         * @param a the allocated region to render into
         * @param direction the direction from the current position that can
         *  be thought of as the arrow keys typically found on a keyboard.
         *  This may be SwingConstants.WEST, SwingConstants.EAST, 
         *  SwingConstants.NORTH, or SwingConstants.SOUTH.  
         * @return the location within the model that best represents the next
         *  location visual position.
         * @exception BadLocationException
         * @exception IllegalArgumentException for an invalid direction
         */
        public @Override int getNextVisualPositionFrom(
            int pos, Position.Bias b, Shape a, 
            int direction, Position.Bias[] biasRet
        ) throws BadLocationException {
            if( view != null ) {
                int nextPos = view.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
		if(nextPos != -1) {
		    pos = nextPos;
		} else {
		    biasRet[0] = b;
		}
            } 
            return pos;
        }

        /**
         * Gives notification that something was inserted into the document
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public @Override void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.insertUpdate(e, a, f);
            }
        }
        
        /**
         * Gives notification that something was removed from the document
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public @Override void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.removeUpdate(e, a, f);
            }
        }

        /**
         * Gives notification from the document that attributes were changed
         * in a location that this view is responsible for.
         *
         * @param e the change information from the associated document
         * @param a the current allocation of the view
         * @param f the factory to use to rebuild if the view has children
         */
        public @Override void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            if (view != null) {
                view.changedUpdate(e, a, f);
            }
        }

        /**
         * Returns the document model underlying the view.
         *
         * @return the model
         */
        public @Override Document getDocument() {
            EditorUI editorUI = getEditorUI();
            return (editorUI == null) ? null : editorUI.getDocument();
        }
        
        /**
         * Returns the starting offset into the model for this view.
         *
         * @return the starting offset
         */
        public @Override int getStartOffset() {
            if (view != null) {
                return view.getStartOffset();
            } else {
                return getElement().getStartOffset();
            }
        }

        /**
         * Returns the ending offset into the model for this view.
         *
         * @return the ending offset
         */
        public @Override int getEndOffset() {
            if (view != null) {
                return view.getEndOffset();
            } else {
                return getElement().getEndOffset();
            }
        }

        /**
         * Gets the element that this view is mapped to.
         *
         * @return the view
         */
        public @Override Element getElement() {
            if (view != null) {
                return view.getElement();
            } else {
                return view.getDocument().getDefaultRootElement();
            }
        }

        /**
         * Breaks this view on the given axis at the given length.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @param len specifies where a break is desired in the span
         * @param the current allocation of the view
         * @return the fragment of the view that represents the given span
         *   if the view can be broken, otherwise null
         */
        public View breakView(int axis, float len, Shape a) {
            throw new Error("Can't break root view"); // NOI18N
        }

        /**
         * Determines the resizability of the view along the
         * given axis.  A value of 0 or less is not resizable.
         *
         * @param axis may be either X_AXIS or Y_AXIS
         * @return the weight
         */
        public @Override int getResizeWeight(int axis) {
            if (view != null) {
                return view.getResizeWeight(axis);
            } else {
                return 0;
            }
        }

        /**
         * Sets the view size.
         *
         * @param width the width
         * @param height the height
         */
        public @Override void setSize(float width, float height) {
            if (view != null) {
                view.setSize(width, height);
            }
        }

        /**
         * Fetches the container hosting the view.  This is useful for
         * things like scheduling a repaint, finding out the host 
         * components font, etc.  The default implementation
         * of this is to forward the query to the parent view.
         *
         * @return the container
         */
        public @Override Container getContainer() {
            EditorUI editorUI = getEditorUI();
            return (editorUI == null) ? null : editorUI.getComponent();
        }
        
        /**
         * Fetches the factory to be used for building the
         * various view fragments that make up the view that
         * represents the model.  This is what determines
         * how the model will be represented.  This is implemented
         * to fetch the factory provided by the associated
         * EditorKit unless that is null, in which case this
         * simply returns the BasicTextUI itself which allows
         * subclasses to implement a simple factory directly without
         * creating extra objects.  
         *
         * @return the factory
         */
        public @Override ViewFactory getViewFactory() {
            EditorUI editorUI = getEditorUI();
            if (editorUI != null) {
                BaseKit kit = Utilities.getKit(editorUI.getComponent());
                ViewFactory f = kit.getViewFactory();
                if (f != null) {
                    return f;
                }
            }
            return getBaseTextUI();
        }

        private View view;
    } // End of RootView class
    
}
