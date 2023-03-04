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


package org.netbeans.core.windows.view;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.ui.MultiSplitPane;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Class which represents model of split element for GUI hierarchy.
 *
 * @author  Peter Zavadsky
 */
public class SplitView extends ViewElement {
    
    private int orientation;
    
    private ArrayList<Double> splitWeights;
    
    private ArrayList<ViewElement> children;
    
    private MultiSplitPane splitPane;
    
    private boolean isDirty = false;
    
    public SplitView(Controller controller, double resizeWeight, 
                        int orientation, List<Double> splitWeights, List<ViewElement> children) {
        super(controller, resizeWeight);
        
        this.orientation = orientation;
        this.splitWeights = new ArrayList<Double>( splitWeights );
        this.children = new ArrayList<ViewElement>( children );
    }

    public void setOrientation( int newOrientation ) {
        this.orientation = newOrientation;
    }
    
    public void setSplitWeights( List<Double> newSplitWeights ) {
        splitWeights.clear();
        splitWeights.addAll( newSplitWeights );
    }
    
    public int getOrientation() {
        return orientation;
    }
    
    public List<ViewElement> getChildren() {
        return new ArrayList<ViewElement>( children );
    }
    
    @Override
    public Component getComponent() {
        return getSplitPane();
    }
    
    public void remove( ViewElement view ) {
        int index = children.indexOf( view );
        if( index >= 0 ) {
            children.remove( index );
            splitWeights.remove( index );
            if( null != splitPane ) {
                splitPane.removeViewElementAt( index );
            }
            isDirty = true; //force invalidation of splitpane
        }
    }
    
    public void setChildren( List<ViewElement> newChildren ) {
        children.clear();
        children.addAll( newChildren );
        
        assert children.size() == splitWeights.size();
        
        isDirty = true; //force invalidation of splitpane
        
        if( null != splitPane ) {
            updateSplitPane();
        }
    }
    
    @Override
    public boolean updateAWTHierarchy(Dimension availableSpace) {
        boolean res = false;
        
        if( !availableSpace.equals( getSplitPane().getSize() ) || isDirty ) { 
            isDirty = false;
            getSplitPane().setSize( availableSpace );
            getSplitPane().invalidate();
            res = true;
        }
        for( Iterator i=children.iterator(); i.hasNext(); ) {
            ViewElement child = (ViewElement)i.next();
            res |= child.updateAWTHierarchy( child.getComponent().getSize() );
        }
        
        return res;
    }
    
    private MultiSplitPane getSplitPane() {
        if(splitPane == null) {
            splitPane = new MultiSplitPane();
            updateSplitPane();
            
            int dividerSize;
            //get default divider size from SplitPane's UI
            if (orientation == JSplitPane.VERTICAL_SPLIT) {
                dividerSize = UIManager.getInt("Nb.SplitPane.dividerSize.vertical"); //NOI18N
                if (dividerSize == 0) {
                    dividerSize = UIManager.getInt("SplitPane.dividerSize"); //NOI18N
                    if (dividerSize == 0) {
                        dividerSize = Constants.DIVIDER_SIZE_VERTICAL;
                    }
                }
            } else {
                dividerSize = UIManager.getInt("Nb.SplitPane.dividerSize.horizontal"); //NOI18N
                if (dividerSize == 0) {
                    dividerSize = UIManager.getInt("SplitPane.dividerSize"); //NOI18N
                    if (dividerSize == 0) {
                        dividerSize = Constants.DIVIDER_SIZE_HORIZONTAL;
                    }
                }
            }
            Integer override = Integer.getInteger("Nb.SplitPane.dividerSize"); //NOI18N
            if( null != override ) {
                dividerSize = override.intValue();
            }
            splitPane.setDividerSize(dividerSize);
            
            splitPane.setBorder(BorderFactory.createEmptyBorder());
            
            splitPane.addPropertyChangeListener("splitPositions", // NOI18N
                new PropertyChangeListener() {
                @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        ArrayList<Double> weights = new ArrayList<Double>( children.size() );
                        ArrayList<ViewElement> views = new ArrayList<ViewElement>( children.size() );
                        splitPane.calculateSplitWeights( views, weights );
                        ViewElement[] arrViews = new ViewElement[views.size()];
                        double[] arrWeights = new double[views.size()]; 
                        for( int i=0; i<views.size(); i++ ) {
                            arrViews[i] = views.get( i );
                            arrWeights[i] = weights.get( i ).doubleValue();
                        }
                        getController().userMovedSplit( SplitView.this, arrViews, arrWeights );
                    }
                });
        }
        
        return splitPane;
    }

    public int getDividerSize() {
        return getSplitPane().getDividerSize();
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append( super.toString() );
        buffer.append( "[" ); // NOI18N
        for( int i=0; i<children.size(); i++ ) {
            ViewElement child = (ViewElement)children.get( i );
            buffer.append( (i+1) );
            buffer.append( '=' );
            if( child instanceof SplitView ) {
                buffer.append( child.getClass() );
                buffer.append( '@' ); // NOI18N
                buffer.append( Integer.toHexString(child.hashCode()) );
            } else {
                buffer.append( child.toString() );
            }
            if( i < children.size()-1 )
                buffer.append( ", " ); // NOI18N
        }
        buffer.append( "]" ); // NOI18N
        
        return buffer.toString();
    }
    
    private void updateSplitPane() {
        ViewElement[] arrViews = new ViewElement[children.size()];
        double[] arrSplitWeights = new double[children.size()];
        for( int i=0; i<children.size(); i++ ) {
            ViewElement view = (ViewElement)children.get( i );
            
            arrViews[i] = view;
            arrSplitWeights[i] = ((Double)splitWeights.get(i)).doubleValue();
        }
        splitPane.setChildren( orientation, arrViews, arrSplitWeights );
    }

    private static void debugLog(String message) {
        Debug.log(SplitView.class, message);
    }
}

