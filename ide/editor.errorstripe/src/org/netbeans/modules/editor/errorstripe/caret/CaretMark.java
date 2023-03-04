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

package org.netbeans.modules.editor.errorstripe.caret;

import java.awt.Color;
import javax.swing.UIManager;
import org.netbeans.modules.editor.errorstripe.AnnotationView;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public class CaretMark implements Mark {

    private static final Color COLOR;
    
    static {
        Color c = UIManager.getColor( "nb.editor.errorstripe.caret.color" ); //NOI18N
        if( null == c )
            c = new Color( 110, 110, 110 );
        COLOR = c;
    }

    private int line;
    
    public CaretMark(int line) {
        this.line = line;
    }
    
    public String getShortDescription() {
        return NbBundle.getMessage(AnnotationView.class, "TP_Current_Line");
    }
    
    public int[] getAssignedLines() {
        return new int[] {line, line};
    }
    
    public Color getEnhancedColor() {
        return COLOR;
    }
    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    public Status getStatus() {
        return Status.STATUS_OK;
    }
    
    public int getType() {
        return TYPE_CARET;
    }
    
    public static Color getCaretMarkColor() {
        return COLOR;
    }
    
}
