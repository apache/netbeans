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
