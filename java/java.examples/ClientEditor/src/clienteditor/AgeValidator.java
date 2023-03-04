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

package clienteditor;

import org.jdesktop.beansbinding.Validator;

/**
 * Validator that ensures that given value is between 1 and 199.
 * 
 * @author Jiri Vagner, Jan Stola
 */
public class AgeValidator extends Validator<Integer> {

    public Validator.Result validate(Integer arg) {        
        if ((arg < 1) || (arg > 199)) {
            return new Result(null, "Age range is 1-199");
        }
        
        return null;    
    }
}
