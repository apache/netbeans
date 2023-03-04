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

function weather() {

  // Load the Ruby module
  Polyglot.eval("application/x-ruby", "eval(File.open(\"org/netbeans/modules/debugger/jpda/truffle/scripts/Weather.rb\").read)");

  let Weather = Polyglot.import('weather')
  Polyglot.export('tempInCity', function(name) {
    return Weather.temperature_in_city(name);
  });

  // Load the R module
  console.log("Preparing weather model... This may take a while.");
  Polyglot.eval("application/x-r", "source(\"org/netbeans/modules/debugger/jpda/truffle/scripts/Weather.r\")");

  // Import the function exported from the R module
  let createModel = Polyglot.import('createModel');
  let predictTemp = Polyglot.import('do_predict');
  let plotModel = Polyglot.import('plotModel');

  // Load the Python module
  Polyglot.eval("text/x-python", "import polyglot\n" +
        "@polyglot.export_value\n" +
        "def purchase(n):\n" +
        "    bill = 0\n" +
        "    bill += fruits(n, n // 2)\n" +
        "    return bill\n" +
        "\n" +
        "def fruits(a, b):\n" +
        "    prices = {'apple': 0.40, 'banana': 0.50}\n" +
        "    my_purchase = {\n" +
        "        'apple': a,\n" +
        "        'banana': b}\n" +
        "    grocery_bill = 0\n" +
        "    for f in my_purchase:\n" +
        "        grocery_bill += prices[f] * my_purchase[f]\n" +
        "    return grocery_bill\n");
  let Purchase = Polyglot.import('purchase')

  let cityService = new (Java.type('org.netbeans.modules.debugger.jpda.truffle.testapps.WeatherCityService'));
  var cities = cityService.getAll();

  let javaNullObj = cityService.getNull();

  // Create the linear regression model -> calls to R
  let updateModel = function(size) {
    function adjustIndex(i) {
      if (i >= 1) {
        return i - 1;
      } else {
        throw 'Wrong index: ' + i;
      }
    }
    function convertTemp(conversion, t) {
      switch(conversion) {
        case 'C2F': return t * 1.8 + 32;
        case 'F2C': return (t - 32) / 1.8;
        default: throw "Unsupported conversion: " + conversion;
      }
    }
    let getName = function(i) {
      return cities[adjustIndex(i)].getName();
    }
    let getLatitude = function(i) {
      return cities[adjustIndex(i)].getLatitude();
    }
    let getLongitude = function(i) {
      return cities[adjustIndex(i)].getLongitude();
    }
    let getTemperature = function(i) {
      let c = cities[adjustIndex(i)];
      return convertTemp('C2F', c.getTemperature());
    }
    return createModel(size, cities.length, getName, getLatitude, getLongitude, getTemperature);
  }

  let model = updateModel(5);

  let numCities = model.data.name.length;
  print("Have " + numCities + " cities:");
  for (let i = 0; i < numCities; i++) {
    let name = cities[i].getName();

    // Step into Ruby
    let temperature = Weather.temperature_in_city(name);

    // Step into Python
    let purchase = Purchase(i);
    print(" City " + name + ", " + cities[i].getCountry() + " has temperature " + temperature + "°C and bill $" + purchase);
  }

  print("Approximated temperatures by latitude are:");
  let tMean = 0;
  for (let lat = 90; lat >= -90; lat -= 10) {

    // Step into R
    let t = predictTemp(model, lat);
    print("Temperature at latitude " + lat + " is " + t + "°C.");
    tMean += t;
  }
  tMean /= 19;
  return tMean;
}

// Step into the weather test
weather();
