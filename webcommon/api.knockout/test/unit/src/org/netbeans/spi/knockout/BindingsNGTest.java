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
package org.netbeans.spi.knockout;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.api.scripting.Scripting;
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
        ScriptEngineManager sem = Scripting.createManager();
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
        assertNumber(eng.eval("ko.value.currentTweets[0].from_user_id"), 0d, "Boolean values are set to true");
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

    private void assertNumber(Object real, double exp, String msg) {
        if (real instanceof Number) {
            assertEquals(((Number) real).doubleValue(), exp, 0.1, msg);
        } else {
            fail("Expecting number: " + real);
        }
    }

}