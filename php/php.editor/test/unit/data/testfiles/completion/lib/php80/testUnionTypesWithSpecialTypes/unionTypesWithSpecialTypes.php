<?php

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

namespace UnionTypes1;

class UnionParent
{
    protected object $parentField;
    public function parentMethod() {
    }
}

class UnionChild extends UnionParent
{
    use UnionTrait;
    private self|parent $union;

    public function childMethod(self|parent $param): self|parent|null {
        $param->testMethod();
        return new UnionChild();
    }

    public function childMethodParent(parent|null $param): parent|null {
        $param->parentMethod(); // parent
        return null;
    }

    public function testMethod() {
        $this->union->parentMethod();
        $this->childMethod($this)->parentMethod();
        $this->childMethodParent(null)->parentMethod();
        $this->traitMethod($this)->parentMethod();
    }
}

trait UnionTrait
{
    public function traitMethod(self|parent $param): self|parent|null {
        return null;
    }
}
