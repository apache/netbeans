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

package org.netbeans.core.windows.view.ui;

import java.awt.GraphicsEnvironment;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 * Some basic tests for MultiSplitPane class to verify its resizing behavior.
 *
 * @author Stanislav Aubrecht
 */
public class MultiSplitPaneTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiSplitPaneTest.class);
    }

    private static int DIVIDER_SIZE = 10;
    private MultiSplitPane split;
    private JWindow testWindow;

    public MultiSplitPaneTest(String testName) {
        super(testName);
    }

    protected boolean runInEQ () {
        return true;
    }

    protected void setUp() throws Exception {
        split = new MultiSplitPane();
        split.setDividerSize( DIVIDER_SIZE );
        
        testWindow = new JWindow();
        testWindow.setVisible( true );
        //testWindow.getContentPane().add( split );
    }

    protected void resizeSplit( int orientation, int splitSize, int nonResizingSize ) {
        if( orientation == JSplitPane.HORIZONTAL_SPLIT )
            split.setSize( splitSize, nonResizingSize );
        else
            split.setSize( nonResizingSize, splitSize );
        testWindow.getContentPane().add( split );
        split.validate();
    }
    
    protected void checkNonSplitSizes( int expectedSize, TestViewElement[] views ) {
        for( int i=0; i<views.length; i++ ) {
            assertEquals( "Invalid non-split size for view " + i, expectedSize, views[i].getNonSplitSize() );
        }
    }

    public void testProportionalResize2ChildrenHorizontal() {
        doTestProportionalResize2Children( JSplitPane.HORIZONTAL_SPLIT );
    }
    
    public void testProportionalResize2ChildrenVertical() {
        doTestProportionalResize2Children( JSplitPane.VERTICAL_SPLIT );
    }

    protected void doTestProportionalResize2Children( int orientation ) {
        TestViewElement[] views = new TestViewElement[2];
        double[] splitWeights = new double[2];
        
        views[0] = new TestViewElement( orientation, 0.0 );
        splitWeights[0] = 0.5;
        views[1] = new TestViewElement( orientation, 0.0 );
        splitWeights[1] = 0.5;

        split.setChildren( orientation, views, splitWeights );
        
        //initial resizing, children sizes must honour their initial split weights
        resizeSplit( orientation, 200+DIVIDER_SIZE, 200 );
        assertEquals( 100, views[0].getSizeInSplit() );
        assertEquals( 100, views[1].getSizeInSplit() );
        //check that children height matches the height of the split
        checkNonSplitSizes( 200, views );

        //children must shrink proportionally
        resizeSplit( orientation, 100+DIVIDER_SIZE, 300 );
        assertEquals( 50, views[0].getSizeInSplit() );
        assertEquals( 50, views[1].getSizeInSplit() );
        checkNonSplitSizes( 300, views );
        
        //minimum sizes must be honoured
        views[0].setMinSize( 10 );
        views[1].setMinSize( 10 );
        resizeSplit( orientation, 0, 100 );
        assertEquals( 10, views[0].getSizeInSplit() );
        assertEquals( 10, views[1].getSizeInSplit() );
        checkNonSplitSizes( 100, views );
    }

    public void testProportionalResize5ChildrenHorizontal() {
        doTestProportionalResize5Children( JSplitPane.HORIZONTAL_SPLIT );
    }
    
    public void testProportionalResize5ChildrenVertical() {
        doTestProportionalResize5Children( JSplitPane.VERTICAL_SPLIT );
    }

    protected void doTestProportionalResize5Children( int orientation ) {
        TestViewElement[] views = new TestViewElement[5];
        double[] splitWeights = new double[5];
        
        views[0] = new TestViewElement( orientation, 0.0 );
        splitWeights[0] = 0.1;
        views[1] = new TestViewElement( orientation, 0.0 );
        splitWeights[1] = 0.1;
        views[2] = new TestViewElement( orientation, 0.0 );
        splitWeights[2] = 0.1;
        views[3] = new TestViewElement( orientation, 0.0 );
        splitWeights[3] = 0.1;
        views[4] = new TestViewElement( orientation, 0.0 );
        splitWeights[4] = 0.6;

        split.setChildren( orientation, views, splitWeights );
        
        //initial resizing, children sizes must honour their initial split weights
        resizeSplit( orientation, 1000+4*DIVIDER_SIZE, 200 );
        assertEquals( 101, views[0].getSizeInSplit() );
        assertEquals( 101, views[1].getSizeInSplit() );
        assertEquals( 101, views[2].getSizeInSplit() );
        assertEquals( 101, views[3].getSizeInSplit() );
        assertEquals( 596, views[4].getSizeInSplit() );
        //check that children height matches the height of the split
        checkNonSplitSizes( 200, views );

        //children must shrink proportionally
        resizeSplit( orientation, 100+4*DIVIDER_SIZE, 300 );
        assertEquals( 15, views[0].getSizeInSplit() );
        assertEquals( 15, views[1].getSizeInSplit() );
        assertEquals( 15, views[2].getSizeInSplit() );
        assertEquals( 15, views[3].getSizeInSplit() );
        assertEquals( 40, views[4].getSizeInSplit() );
        checkNonSplitSizes( 300, views );
        
        //minimum sizes must be honoured
        views[0].setMinSize( 10 );
        views[1].setMinSize( 10 );
        views[2].setMinSize( 10 );
        views[3].setMinSize( 10 );
        views[4].setMinSize( 10 );
        resizeSplit( orientation, 0, 100 );
        assertEquals( 10, views[0].getSizeInSplit() );
        assertEquals( 10, views[1].getSizeInSplit() );
        assertEquals( 10, views[2].getSizeInSplit() );
        assertEquals( 10, views[3].getSizeInSplit() );
        assertEquals( 10, views[4].getSizeInSplit() );
        checkNonSplitSizes( 100, views );
    }


    public void testEqualResizeWeightsHorizontal() {
        doTestEqualResizeWeights( JSplitPane.HORIZONTAL_SPLIT );
    }
    
    public void testEqualResizeWeightsVertical() {
        doTestEqualResizeWeights( JSplitPane.VERTICAL_SPLIT );
    }

    protected void doTestEqualResizeWeights( int orientation ) {
        TestViewElement[] views = new TestViewElement[4];
        double[] splitWeights = new double[4];
        
        views[0] = new TestViewElement( orientation, 0.25 );
        splitWeights[0] = 1.0;
        views[1] = new TestViewElement( orientation, 0.25 );
        splitWeights[1] = 1.0;
        views[2] = new TestViewElement( orientation, 0.25 );
        splitWeights[2] = 1.0;
        views[3] = new TestViewElement( orientation, 0.25 );
        splitWeights[3] = 1.0;

        split.setChildren( orientation, views, splitWeights );
        
        //initial resizing, children sizes must honour their initial split weights
        resizeSplit( orientation, 1000+3*DIVIDER_SIZE, 200 );
        assertEquals( 250, views[0].getSizeInSplit() );
        assertEquals( 250, views[1].getSizeInSplit() );
        assertEquals( 250, views[2].getSizeInSplit() );
        assertEquals( 250, views[3].getSizeInSplit() );
        //check that children height matches the height of the split
        checkNonSplitSizes( 200, views );

        //children must shrink according to their resize weights
        resizeSplit( orientation, 100+3*DIVIDER_SIZE, 300 );
        assertEquals( 25, views[0].getSizeInSplit() );
        assertEquals( 25, views[1].getSizeInSplit() );
        assertEquals( 25, views[2].getSizeInSplit() );
        assertEquals( 25, views[3].getSizeInSplit() );
        checkNonSplitSizes( 300, views );
        
        //minimum sizes must be honoured
        views[0].setMinSize( 10 );
        views[1].setMinSize( 10 );
        views[2].setMinSize( 10 );
        views[3].setMinSize( 10 );
        resizeSplit( orientation, 0, 100 );
        assertEquals( 10, views[0].getSizeInSplit() );
        assertEquals( 10, views[1].getSizeInSplit() );
        assertEquals( 10, views[2].getSizeInSplit() );
        assertEquals( 10, views[3].getSizeInSplit() );
        checkNonSplitSizes( 100, views );
    }

    public void testDifferentResizeWeightsHorizontal() {
        doTestDifferentResizeWeights( JSplitPane.HORIZONTAL_SPLIT );
    }
    
    public void testDifferentResizeWeightsVertical() {
        doTestDifferentResizeWeights( JSplitPane.VERTICAL_SPLIT );
    }

    protected void doTestDifferentResizeWeights( int orientation ) {
        TestViewElement[] views = new TestViewElement[4];
        double[] splitWeights = new double[4];
        
        views[0] = new TestViewElement( orientation, 0.2 ); //normalized to .1
        splitWeights[0] = 1.0;
        views[1] = new TestViewElement( orientation, 0.2 ); //normalized to .1
        splitWeights[1] = 1.0;
        views[2] = new TestViewElement( orientation, 0.5 ); //normalized to .4
        splitWeights[2] = 1.0;
        views[3] = new TestViewElement( orientation, 0.5 ); //normalized to .4
        splitWeights[3] = 1.0;

        split.setChildren( orientation, views, splitWeights );
        
        //initial resizing, children sizes must honour their initial split weights
        resizeSplit( orientation, 100+3*DIVIDER_SIZE, 200 );
        assertEquals( 25, views[0].getSizeInSplit() );
        assertEquals( 25, views[1].getSizeInSplit() );
        assertEquals( 25, views[2].getSizeInSplit() );
        assertEquals( 25, views[3].getSizeInSplit() );
        //check that children height matches the height of the split
        checkNonSplitSizes( 200, views );

        //children must grow according to their resize weights
        resizeSplit( orientation, 1000+3*DIVIDER_SIZE, 300 );
        assertEquals( 115, views[0].getSizeInSplit() ); //+900*.1
        assertEquals( 115, views[1].getSizeInSplit() ); //+900*.1
        assertEquals( 385, views[2].getSizeInSplit() ); //+900*.4
        assertEquals( 385, views[3].getSizeInSplit() ); //+900*.4
        checkNonSplitSizes( 300, views );
        
        //children must shrink according to their resize weights
        resizeSplit( orientation, 100+3*DIVIDER_SIZE, 300 );
        assertEquals( 17, views[0].getSizeInSplit() ); //this is a bug, the correct value is 25
        assertEquals( 17, views[1].getSizeInSplit() ); //this is a bug, the correct value is 25
        assertEquals( 34, views[2].getSizeInSplit() ); //this is a bug, the correct value is 25
        assertEquals( 32, views[3].getSizeInSplit() ); //this is a bug, the correct value is 25
        checkNonSplitSizes( 300, views );

        //minimum sizes must be honoured
        views[0].setMinSize( 10 );
        views[1].setMinSize( 10 );
        views[2].setMinSize( 10 );
        views[3].setMinSize( 10 );
        resizeSplit( orientation, 0, 100 );
        assertEquals( 10, views[0].getSizeInSplit() );
        assertEquals( 10, views[1].getSizeInSplit() );
        assertEquals( 10, views[2].getSizeInSplit() );
        assertEquals( 10, views[3].getSizeInSplit() );
        checkNonSplitSizes( 100, views );
    }
}
