/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.spi.knockout;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class BindingsNGTest {
    private ScriptEngine eng;

    public BindingsNGTest() {
    }

    @BeforeMethod public void initEngine() {
        ScriptEngineManager sem = new ScriptEngineManager();
        eng = sem.getEngineByMimeType("text/javascript");
    }

    @Test public void generateTwitterModel() throws Exception {
        Bindings tweet = Bindings.create("Tweet").
            stringProperty("from_user", false).
            intProperty("from_user_id", false);

        Bindings tweeters = Bindings.create("Tweeters").
            stringProperty("name", false).
            stringProperty("userNames", true);

        Bindings twitterClient = Bindings.create("TwitterClient");
        twitterClient.
            stringProperty("activeTweetersName", false).
            stringProperty("activeTweeters", true).
            stringProperty("userNameToAdd", false).
            booleanProperty("loading", false).
            modelProperty("currentTweets", tweet, true).
            modelProperty("savedLists", tweeters, true);

        String txt = twitterClient.generate();

        assertValidJS(txt);

        assertNotNull(eng.eval("ko"));
        assertNotNull(eng.eval("ko.value"));
        assertEquals(eng.eval("ko.value.loading"), true, "Boolean values are set to true");
        assertEquals(eng.eval("ko.value.currentTweets[0].from_user_id"), 0d, "Boolean values are set to true");
    }

    @Test
    public void generateRecursiveModel() throws Exception {
        Bindings m1 = Bindings.create("Hello");
        Bindings m2 = Bindings.create("Multi");
        m1.modelProperty("multi", m2, false);
        m2.modelProperty("hello", m1, false);
        m2.intProperty("int", false);
        String txt = m2.generate();
        assertValidJS(txt);
        assertNotNull(eng.eval("ko"), txt);
        assertNotNull(eng.eval("Hello.multi"), txt);
        assertEquals(eng.eval("Hello.multi.hello === Hello"), Boolean.TRUE, txt);
    }
    
    public void generateModelWithFunctions() throws Exception {
        Bindings myModel = Bindings.create("MyModel")
                .function("myFunc1")
                .function("func2");

        String txt = myModel.generate();
        assertNotNull(eng.eval("ko"), txt);
        assertNotNull(eng.eval("Hello.multi"), txt);
        assertEquals(eng.eval("Hello.multi.hello === Hello"), Boolean.TRUE, txt);
        assertNotNull(eng.eval("ko"));
        assertNotNull(eng.eval("ko.value"));
        assertNotNull(eng.eval("ko.value.myFunc1"));
        assertNotNull(eng.eval("ko.value.func2"));
        assertEquals("function", eng.eval("typeof ko.value.myFunc1"));
        assertEquals("function", eng.eval("typeof ko.value.func2"));
    }

    @Test
    public void generateRecursiveModel2() throws Exception {
        Bindings one = Bindings.create("One");
        Bindings two = Bindings.create("Two");
        Bindings three = Bindings.create("Three");
        one.modelProperty("three", three, false);
        two.modelProperty("one", one, false);
        three.modelProperty("two",two , false);
        String txt = two.generate();
        assertValidJS(txt);
        assertNotNull(eng.eval("ko"), txt);
        assertNotNull(eng.eval("One.three"), txt);
        assertNotNull(eng.eval("One.three.two"), txt);
        assertEquals(eng.eval("One.three.two.one === One"), Boolean.TRUE, txt);
    }

    private void assertValidJS(String txt) {
        assertNotNull(txt, "We have some script");
        try {
            eng.eval("ko = {}; ko.applyBindings = function(val) { ko.value = val; }");
            eng.eval(txt);
        } catch (ScriptException ex) {
            throw new AssertionError(txt, ex);
        }
    }

}