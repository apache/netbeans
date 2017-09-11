/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.openide.util.lookup;

import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import static org.junit.Assert.*;

/**
 * Test of a Lookup bug seen in NetBeans platforms 6.0-6.5M1.
 * @author rlee
 */
public class LookupBugTest implements LookupListener
{
    private static final int MAX_LOOPS = 1000;
    
    private AbstractLookup lookup;
    private InstanceContent content;
    private Lookup.Result<String> wordResult;
    private Lookup.Result<Integer> numberResult;
    private String word;
    private Integer number;
    private Logger LOG;
    
    private boolean fired;
    private int i;

    @Before
    public void setUp()
    {
        LOG = Logger.getLogger("test.LookupBugTest");
        content = new InstanceContent();
        lookup = new AbstractLookup(content);
        wordResult = lookup.lookupResult(java.lang.String.class);
        wordResult.addLookupListener(this);
        numberResult = lookup.lookupResult(java.lang.Integer.class);
        numberResult.addLookupListener(this);
        
        fired = false;
    }
    
    @Test
    public void lookupTest()
    {
        for(i = 0; i < MAX_LOOPS; i++ )
        {
            word = String.valueOf(i);
            number = new Integer(i);
            content.add(word);
            assertTrue( "word on loop " + i, checkLookupEventFired() );
            content.add(number);
            assertTrue( "number on loop " + i, checkLookupEventFired() );
            content.remove(word);
            assertTrue( "remove word on loop " + i, checkLookupEventFired() );
            content.remove(number);
            assertTrue( "remove number on loop " + i, checkLookupEventFired() );

            assertTrue("The lookup still needs to stay simple", AbstractLookup.isSimple(lookup));
        }
    }

    public void resultChanged(LookupEvent ev)
    {
        fired = true;
    }
    
    public boolean checkLookupEventFired()
    {
        LOG.fine("  round: " + i + " word = " + word + " number = " + number);
        if( fired )
        {
            fired = false;
            return true;
        }
        else return false;
    }
}
