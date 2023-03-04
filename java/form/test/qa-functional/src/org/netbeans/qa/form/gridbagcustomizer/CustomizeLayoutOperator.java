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
package org.netbeans.qa.form.gridbagcustomizer;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Adam Senk adam.senk@oracle.com
 */
public class CustomizeLayoutOperator extends JDialogOperator {

    private JButtonOperator _btClose;
    private JButtonOperator _btYGridMinus;
    private JButtonOperator _btYGridPlus;
    private JButtonOperator _btXGridMinus;
    private JButtonOperator _btXGridPlus;
    private JButtonOperator _btHGridMinus;
    private JButtonOperator _btHGridPlus;
    private JButtonOperator _btVGridMinus;
    private JButtonOperator _btVGridPlus;
    private JButtonOperator _YPaddingPlus;
    private JButtonOperator _YPaddingMinus;
    private JButtonOperator _XPaddingPlus;
    private JButtonOperator _XPaddingMinus;
    private JButtonOperator _BothPaddingPlus;
    private JButtonOperator _BothPaddingMinus;
    private JButtonOperator _AllInsetsPlus;
    private JButtonOperator _AllInsetsMinus;
    private JButtonOperator _LeftInsetsPlus;
    private JButtonOperator _LeftInsetsMinus;
    private JButtonOperator _TopInsetsPlus;
    private JButtonOperator _TopInsetsMinus;
    private JButtonOperator _BottomInsetsPlus;
    private JButtonOperator _BottomInsetsMinus;
    private JButtonOperator _RightInsetsPlus;
    private JButtonOperator _RightInsetsMinus;
    private JButtonOperator _LeftAndRightInsetsMinus;
    private JButtonOperator _LeftAndRightInsetsPlus;
    private JButtonOperator _TopAndBottomInsetsPlus;
    private JButtonOperator _TopAndBottomInsetsMinus;
    private JButtonOperator _YWeightPlus;
    private JButtonOperator _YWeightMinus;
    private JButtonOperator _XWeightPlus;
    private JButtonOperator _XWeightMinus;
    private JButtonOperator _XWeightEqualize;
    private JButtonOperator _YWeightEqualize;
    private JButtonOperator _Redo;
    private JButtonOperator _Undo;
    private JButtonOperator _SetGapColumnWidthUp;
    private JButtonOperator _SetGapColumnWidthDown;
    private JButtonOperator _SetGapRowWidthUp;
    private JButtonOperator _SetGapRowWidthDown;
    private JButtonOperator _TestLayout;

    public CustomizeLayoutOperator() {
        super("Customize Layout");
    }
    //Methods for Position In Grid

    public JButtonOperator btYGridMinus() {
        if (_btYGridMinus == null) {
            _btYGridMinus = new JButtonOperator(this, 14 + reduction());  // NOI18N
        }
        return _btYGridMinus;
    }

    public JButtonOperator btXGridMinus() {
        if (_btXGridMinus == null) {
            _btXGridMinus = new JButtonOperator(this, 13+ reduction());  // NOI18N
        }
        return _btXGridMinus;
    }
    //OK

    public JButtonOperator btYGridPlus() {
        if (_btYGridPlus == null) {
            _btYGridPlus = new JButtonOperator(this, 15+ reduction());  // NOI18N
        }
        return _btYGridPlus;
    }
    //OK

    public JButtonOperator btXGridPlus() {
        if (_btXGridPlus == null) {
            _btXGridPlus = new JButtonOperator(this, 16+ reduction());  // NOI18N
        }
        return _btXGridPlus;
    }

    //Methods for Size In Grid -OK
    public JButtonOperator btHGridMinus() {
        if (_btHGridMinus == null) {
            _btHGridMinus = new JButtonOperator(this, 19+ reduction());  // NOI18N
        }
        return _btHGridMinus;
    }

    public JButtonOperator btVGridMinus() {
        if (_btVGridMinus == null) {
            _btVGridMinus = new JButtonOperator(this, 17+ reduction());  // NOI18N
        }
        return _btVGridMinus;
    }

    public JButtonOperator btHGridPlus() {
        if (_btHGridPlus == null) {
            _btHGridPlus = new JButtonOperator(this, 20+ reduction());  // NOI18N
        }
        return _btHGridPlus;
    }

    public JButtonOperator btVGridPlus() {
        if (_btVGridPlus == null) {
            _btVGridPlus = new JButtonOperator(this, 18+ reduction());  // NOI18N
        }
        return _btVGridPlus;
    }

    //Methods for Padding - ok
    public JButtonOperator btYPaddingPlus() {
        if (_YPaddingPlus == null) {
            _YPaddingPlus = new JButtonOperator(this, 21+ reduction());  // NOI18N
        }
        return _YPaddingPlus;
    }

    public JButtonOperator btYPaddingMinus() {
        if (_YPaddingMinus == null) {
            _YPaddingMinus = new JButtonOperator(this, 22+ reduction());  // NOI18N
        }
        return _YPaddingMinus;
    }

    public JButtonOperator btXPaddingPlus() {
        if (_XPaddingPlus == null) {
            _XPaddingPlus = new JButtonOperator(this, 24+ reduction());  // NOI18N
        }
        return _XPaddingPlus;
    }

    public JButtonOperator btXPaddingMinus() {
        if (_XPaddingMinus == null) {
            _XPaddingMinus = new JButtonOperator(this, 23+ reduction());  // NOI18N
        }
        return _XPaddingMinus;
    }

    public JButtonOperator btBothPaddingPlus() {
        if (_BothPaddingPlus == null) {
            _BothPaddingPlus = new JButtonOperator(this, 25+ reduction());  // NOI18N
        }
        return _BothPaddingPlus;
    }

    public JButtonOperator btBothPaddingMinus() {
        if (_BothPaddingMinus == null) {
            _BothPaddingMinus = new JButtonOperator(this, 26+ reduction());  // NOI18N
        }
        return _BothPaddingMinus;
    }

    //Methods for Insets OK
    public JButtonOperator btAllInsetsPlus() {
        if (_AllInsetsPlus == null) {
            _AllInsetsPlus = new JButtonOperator(this, 37+ reduction());  // NOI18N
        }
        return _AllInsetsPlus;
    }

    public JButtonOperator btAllInsetsMinus() {
        if (_AllInsetsMinus == null) {
            _AllInsetsMinus = new JButtonOperator(this, 38+ reduction());  // NOI18N
        }
        return _AllInsetsMinus;
    }
    
    public JButtonOperator btLeftInsetsPlus(){
        if (_LeftInsetsPlus==null) {
            _LeftInsetsPlus = new JButtonOperator(this,39+ reduction());  // NOI18N
        }
        return _LeftInsetsPlus;
    }
    public JButtonOperator btLeftInsetsMinus(){
        if (_LeftInsetsMinus==null) {
            _LeftInsetsMinus = new JButtonOperator(this,40+ reduction());  // NOI18N
        }
        return _LeftInsetsMinus;
    }
    
    public JButtonOperator btTopInsetsPlus(){
        if (_TopInsetsPlus==null) {
            _TopInsetsPlus = new JButtonOperator(this,41+ reduction());  // NOI18N
        }
        return _TopInsetsPlus;
    }
    
    public JButtonOperator btTopInsetsMinus(){
        if (_TopInsetsMinus==null) {
            _TopInsetsMinus = new JButtonOperator(this,42+ reduction());  // NOI18N
        }
        return _TopInsetsMinus;
    }
    
    public JButtonOperator btBottomInsetsPlus(){
        if (_BottomInsetsPlus==null) {
            _BottomInsetsPlus = new JButtonOperator(this,44+ reduction());  // NOI18N
        }
        return _BottomInsetsPlus;
    }
    
    public JButtonOperator btBottomInsetsMinus(){
        if (_BottomInsetsMinus==null) {
            _BottomInsetsMinus = new JButtonOperator(this,43+ reduction());  // NOI18N
        }
        return _BottomInsetsMinus;
    }
    
    public JButtonOperator btRightInsetsPlus(){
        if (_RightInsetsPlus==null) {
            _RightInsetsPlus = new JButtonOperator(this,46+ reduction());  // NOI18N
        }
        return _RightInsetsPlus;
    }
    
    public JButtonOperator btRightInsetsMinus(){
        if (_RightInsetsMinus==null) {
            _RightInsetsMinus = new JButtonOperator(this,45+ reduction());  // NOI18N
        }
        return _RightInsetsMinus;
    }
    
    public JButtonOperator btLeftAndRightInsetsMinus(){
        if (_LeftAndRightInsetsMinus==null) {
            _LeftAndRightInsetsMinus = new JButtonOperator(this,35+ reduction());  // NOI18N
        }
        return _LeftAndRightInsetsMinus;
    }
    
    public JButtonOperator btLeftAndRightInsetsPlus(){
        if (_LeftAndRightInsetsPlus==null) {
            _LeftAndRightInsetsPlus = new JButtonOperator(this,36+ reduction());  // NOI18N
        }
        return _LeftAndRightInsetsPlus;
    }
    
    public JButtonOperator btTopAndBottomInsetsPlus(){
        if (_TopAndBottomInsetsPlus==null) {
            _TopAndBottomInsetsPlus = new JButtonOperator(this,33+ reduction());  // NOI18N
        }
        return _TopAndBottomInsetsPlus;
    }
    
    public JButtonOperator btTopAndBottomInsetsMinus(){
        if (_TopAndBottomInsetsMinus==null) {
            _TopAndBottomInsetsMinus = new JButtonOperator(this,34+ reduction());  // NOI18N
        }
        return _TopAndBottomInsetsMinus;
    }
    
    
    //Methods for Weights
    public JButtonOperator btYWeightPlus(){
        if (_YWeightPlus==null) {
            _YWeightPlus = new JButtonOperator(this,27+ reduction());  // NOI18N
        }
        return _YWeightPlus;
    }
    
    public JButtonOperator btYWeightMinus(){
        if (_YWeightMinus==null) {
            _YWeightMinus = new JButtonOperator(this,28+ reduction());  // NOI18N
        }
        return _YWeightMinus;
    }
    
    public JButtonOperator btXWeightPlus(){
        if (_XWeightPlus==null) {
            _XWeightPlus = new JButtonOperator(this,30+ reduction());  // NOI18N
        }
        return _XWeightPlus;
    }
    
    public JButtonOperator btXWeightMinus(){
        if (_XWeightMinus==null) {
            _XWeightMinus = new JButtonOperator(this,29+ reduction());  // NOI18N
        }
        return _XWeightMinus;
    }
    
    public JButtonOperator btYWeightEqualize(){
        if (_YWeightEqualize==null) {
            _YWeightEqualize = new JButtonOperator(this,31+ reduction());  // NOI18N
        }
        return _YWeightEqualize;
    }
    
    public JButtonOperator btXWeightEqualize(){
        if (_XWeightEqualize==null) {
            _XWeightEqualize = new JButtonOperator(this,32+ reduction());  // NOI18N
        }
        return _XWeightEqualize;
    }
    
    //Top Toolbar buttons methods
    
    public JButtonOperator btRedo(){
        if (_Redo==null) {
            _Redo = new JButtonOperator(this,2);  // NOI18N
        }
        return _Redo;
    }
    
    public JButtonOperator btUndo(){
        if (_Undo==null) {
            _Undo = new JButtonOperator(this,3);  // NOI18N
        }
        return _Undo;
    }
    
    public JButtonOperator btSetGapColumnWidthUp(){
        if (_SetGapColumnWidthUp==null) {
            _SetGapColumnWidthUp = new JButtonOperator(this,4);  // NOI18N
        }
        return _SetGapColumnWidthUp;
    }
    
    public JButtonOperator btSetGapColumnWidthDown(){
        if (_SetGapColumnWidthDown==null) {
            _SetGapColumnWidthDown = new JButtonOperator(this,5);  // NOI18N
        }
        return _SetGapColumnWidthDown;
    }
    
    public JButtonOperator btSetGapRowWidthUp(){
        if (_SetGapRowWidthUp==null) {
            _SetGapRowWidthUp = new JButtonOperator(this,6);  // NOI18N
        }
        return _SetGapRowWidthUp;
    }
    
    public JButtonOperator btSetGapRowWidthDown(){
        if (_SetGapRowWidthDown==null) {
            _SetGapRowWidthDown = new JButtonOperator(this,7);  // NOI18N
        }
        return _SetGapRowWidthDown;
    }
    
    public JButtonOperator btestLayout(){
        if (_TestLayout==null) {
            _TestLayout = new JButtonOperator(this,8);  // NOI18N
            
        }
        return _TestLayout;
    }
    

    public JButtonOperator btClose() {
        if (_btClose == null) {
            _btClose = new JButtonOperator(this, "Close");  // NOI18N
        }
        return _btClose;
    }
    
    private int reduction(){
        int i=9;
        while(true){
            JButtonOperator jb=new JButtonOperator(this, i);
            i++;
            if(jb.getToolTipText()==null)
                continue;
            if(jb.getToolTipText().contains("Decrease Grid X"));
                return i-14;    
        }
    }
}
