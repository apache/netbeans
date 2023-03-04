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

package org.netbeans.modules.bugtracking.issuetable;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSetup;
import org.netbeans.modules.bugtracking.api.Query;

/**
 *
 * @author tomas
 */
public abstract class IssuetableTestFactory extends NbTestSetup {

    public IssuetableTestFactory(Test test) {
        super(test);
        registerMap (test);
    }

    public abstract Query createQuery();
    public abstract void setSaved(Query q);
    public abstract IssueTable getTable(Query q);
    public abstract int getColumnsCountBeforeSave();
    public abstract int getColumnsCountAfterSave();

    public static final  IssuetableTestFactory getInstance (Test test) {
        IssuetableTestFactory factory = getFromMap (test);
        return factory;
    }

    private static Map<Test, List<IssuetableTestFactory>> map = new HashMap<Test, List<IssuetableTestFactory>> ();

    private void registerMap (Test test) {
        if (test instanceof TestSuite) {
            Enumeration en = ((TestSuite)test).tests ();
            while (en.hasMoreElements()) {
                Test tst = (Test)en.nextElement();
                if (tst instanceof TestSuite)
                    registerMap (tst);
                else {
                    addToMap (tst);
                }
            }
        } else {
            addToMap (test);
        }
    }

    private void addToMap (Test test) {
        List<IssuetableTestFactory> s = map.get (test);
        if (s == null) {
            s = new LinkedList<IssuetableTestFactory>();
        }
        s.add(this);
        map.put(test ,s );
    }

    private static IssuetableTestFactory getFromMap (Test test) {
        LinkedList s = (LinkedList) map.get (test);
        IssuetableTestFactory  retVal;
        try {
            retVal = (IssuetableTestFactory) s.getLast();
        } catch (NoSuchElementException x ) {
            System.out.println("exc: "+ test + " : " );
            throw x;
        }
        return retVal;
    }
}
