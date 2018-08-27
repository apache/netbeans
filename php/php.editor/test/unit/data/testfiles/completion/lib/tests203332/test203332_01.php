<?php

  abstract class Book_Abstract
  {
    /**
     * @return Book
     */
    public function open ()
    {
      // do job
      return $this;
    }

    /**
     * @return Book
     */
    public function close ()
    {
      // do job
      return $this;
    }
  }

  class Book extends Book_Abstract {}

  $q = new Book();
  $q->open()->open();
  ?>