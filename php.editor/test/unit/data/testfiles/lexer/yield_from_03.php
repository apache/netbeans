<?php

function gg() {
  yield 1;
  yield from new ArrayIterator([2, 3, 4]);
  yield 5;
}
