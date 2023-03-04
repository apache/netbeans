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
 * CheckoutWizardOperator.java
 *
 * Created on 19/04/06 13:24
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.test.subversion.operators.actions.CheckoutAction;

/**
 * Class implementing all necessary methods for handling "CheckoutWizardOperator" NbDialog.
 *
 *
 * @author peter
 * @version 1.0
 */
public class CheckoutWizardOperator extends WizardOperator {

    /**
     * Creates new CheckoutWizardOperator that can handle it.
     */
    public CheckoutWizardOperator() {
        super("Checkout"); //NO I18N
    }

    /** Invokes new wizard and returns instance of CheckoutWizardOperator.
     * @return  instance of CheckoutWizardOperator
     */
    public static CheckoutWizardOperator invoke() {
        new CheckoutAction().perform();
        return new CheckoutWizardOperator();
    }
}

