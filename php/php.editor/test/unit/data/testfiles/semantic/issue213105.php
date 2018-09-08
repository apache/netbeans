<?php
trait B
{
  use A { getName as protected; }
}
?>