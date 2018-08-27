<?php

function g22() {
  yield 1;
  $g1result = yield from g11();
  yield 4;
  return $g1result;
}
