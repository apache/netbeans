package org.netbeans.modules.bugtracking.issuetable;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */



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

    public final static  IssuetableTestFactory getInstance (Test test) {
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
