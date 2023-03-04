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

import spock.lang.Specification
import spock.lang.Unroll
/**
 *
 * @author Pet Pisl
 */
class SpockParam01 extends Specification {
    
    
    @Unroll
    void "should compute #nim to #square"() {
        when:
        def result = mathService.compute(nim)

        then:
        1 * mathService.compute(_) >> { Math.pow(nim, 2) }
        result == square
        
        
        
        where:
        nim || square
        2   || 4
        3   || 9
    }
    
}

