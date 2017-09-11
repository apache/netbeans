/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.api.progress.aggregate;

import junit.framework.TestCase;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.ProgressUIWorker;
import org.netbeans.modules.progress.spi.ProgressEvent;

/**
 *
 * @author mkleint
 */
public class AggregateProgressHandleTest extends TestCase {

    public AggregateProgressHandleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Controller.defaultInstance = new Controller(new ProgressUIWorker() {
            public void processProgressEvent(ProgressEvent event) { }
            public void processSelectedProgressEvent(ProgressEvent event) { }
        }) {
        };
    }

    public void testContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1, contrib2}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib2.getRemainingParentWorkUnits());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib2.getRemainingParentWorkUnits());
        // the +1 deal is there because of the rounding, the last one gest the remainder
        assertEquals(AggregateProgressHandle.WORKUNITS /3 + 1, contrib3.getRemainingParentWorkUnits());
    }
    
    public void testDynamicContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS, contrib1.getRemainingParentWorkUnits());
    
        handle.start();
        contrib1.start(100);
        contrib1.progress(50);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, handle.getCurrentProgress());
        
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        handle.addContributor(contrib2);
        assertEquals(AggregateProgressHandle.WORKUNITS /4, contrib2.getRemainingParentWorkUnits());
        contrib1.finish();
        assertEquals(AggregateProgressHandle.WORKUNITS /4 * 3, handle.getCurrentProgress());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib2.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib3.getRemainingParentWorkUnits());
        contrib3.start(100);
        contrib3.finish();
        assertEquals((AggregateProgressHandle.WORKUNITS /4 * 3) + (AggregateProgressHandle.WORKUNITS /8), 
                     handle.getCurrentProgress());
        
        
    }
    
}
