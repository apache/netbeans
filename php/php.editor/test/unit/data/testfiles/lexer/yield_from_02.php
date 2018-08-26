<?php

function g() {
  yield 1;
  yield from [2, 3, 4];
  yield 5;
}
