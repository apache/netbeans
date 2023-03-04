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
/*
 * DefaultTabLayoutModel.java
 *
 * Created on April 2, 2004, 3:59 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.JComponent;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 * Default implementation of TabLayoutModel.  Simply provides a series of
 * rectangles for each tab starting at 0 and ending at the last element, with
 * the width set to the calculated width for the string plus a padding value
 * assigned in <code>setPadding</code>.
 * <p>
 * To implement TabLayoutModel, it is often useful to create an implementation which
 * wraps an instance of <code>DefaultTabLayoutModel</code>, and uses it to calculate
 * tab sizes.
 *
 * @author Tim Boudreau
 */
public final class DefaultTabLayoutModel extends BaseTabLayoutModel {
    
    /** Creates a new instance of DefaultTabLayoutModel */
    public DefaultTabLayoutModel(TabDataModel model, JComponent renderTarget) {
        super (model, renderTarget);
    }
    
}
