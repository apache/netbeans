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

# Import the tempInCity function exported from the Ruby module
tempInCity <- import('tempInCity')

# The lattice library is needed for the visualization
library(lattice)

createModel <- function(size, length, getName, getLat, getLong, getTemp) {
  idx <- sample(1:length, size)
  data <- as.data.frame(list(
      name = sapply(idx, function(i) getName(i)),
      lat = sapply(idx, function(i) getLat(i)),
      long = sapply(idx, function(i) getLong(i)),
      temp = sapply(idx, function(i) getTemp(i))))
  list(data=data, model=lm(temp~lat, data=data))
}

do_predict <- function(model, lat) {
  predict(model$model, as.data.frame(list(lat = lat)))[[1]]
}

plotModel <- function(model) {
  svg()
  print(xyplot(temp ~ lat, data = model$data,
    panel = function(x, y) {
      panel.xyplot(x, y, cex=2, pch=19)
      panel.abline(model$model)
      labelsIdx <- seq(1, length(x), length.out = 10) # show only 10 labels, to make the graph more readable
      panel.text(x[labelsIdx] + 1, y[labelsIdx], model$data$name[labelsIdx], adj = c(0, 0.5))
  }));
  grDevices:::svg.off()
}

# Export the functions
export('createModel', createModel)
export('do_predict', do_predict)
export('plotModel', plotModel)

