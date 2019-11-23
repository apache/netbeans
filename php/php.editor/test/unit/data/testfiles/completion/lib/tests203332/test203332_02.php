<?php

  abstract class Doctrine_Query_Abstract
  {
    /**
     * @return Doctrine_Query
     */
    public function from ()
    {
      return $this;
    }

    protected function addPart ()
    {
      return $this;
    }

    /**
     * @return Doctrine_Query
     */
    public function addFrom ()
    {
      return $this->addPart();
    }
  }

  class Doctrine_Query extends Doctrine_Query_Abstract
  {

    /**
     * @return Doctrine_Query
     */
    public static function create ()
    {
      $class = __CLASS__;

      return new $class;
    }

  }

  class Doctrine_Table
  {

    public function createQuery ()
    {
      return Doctrine_Query::create()->from();
    }
  }

  class CultureTable extends Doctrine_Table
  {
    /**
     * @return CultureTable
     */
    public static function getInstance ()
    {
      return Doctrine_Core::getTable('Culture');
    }
  }


  $v = CultureTable::getInstance()->createQuery();
  $v->from();
  ?>