#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

import polyglot
@polyglot.export_value
def purchase(n):
    bill = 0
    bill += fruits(n, n // 2)
    return bill

def fruits(a, b):
    prices = {'apple': 0.40, 'banana': 0.50}
    my_purchase = {
        'apple': a,
        'banana': b}
    grocery_bill = 0
    for f in my_purchase:
        grocery_bill += prices[f] * my_purchase[f]
    return grocery_bill

