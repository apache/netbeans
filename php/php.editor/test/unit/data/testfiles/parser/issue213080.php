<?php
trait A
  {
    public function getName ()
    {
      return 'A';
    }
  }

  trait B
  {
    use A { getName as protected; }
  }
?>