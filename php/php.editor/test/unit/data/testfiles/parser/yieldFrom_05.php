<?php

function g2() {
  yield 1;
  yield
          from g1();
  yield 5;
}
