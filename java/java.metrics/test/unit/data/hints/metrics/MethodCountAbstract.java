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
package test;

import java.util.Enumeration;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.AbstractElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

/**
 *
 * @author sdedic
 */
class MethodCountAbstract extends AbstractElement {

    public MethodCountAbstract(AbstractDocument ad, Element parent, AttributeSet a) {
        ad.super(parent, a);
    }
    public void a1() {}
    public void a2() {}
    public void a3() {}
    public void a4() {}
    public void a5() {}
    public void a6() {}
    public void a7() {}
    public void a8() {}
    public void a9() {}
    public void a10() {}
    public void a11() {}
    public void a12() {}
    public void a13() {}
    
    @Override
    public int getStartOffset() {
        return 0;
    }

    @Override
    public int getEndOffset() {
        return 0;
    }

    @Override
    public Element getElement(int index) {
        return null;
    }

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public int getElementIndex(int offset) {
        return 0;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public Enumeration children() {
        return null;
    }
}
