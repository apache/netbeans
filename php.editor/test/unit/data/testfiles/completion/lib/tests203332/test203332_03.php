<?php
namespace Test_03;
  class Doctrine_Table
  {
    public function __construct ($component) {}
  }

  /**
   * @method getName getName() Returns table name
   */
  class BookTable extends Doctrine_Table
  {
    /**
     * @return BookTable
     */
    public static function getInstance ()
    {
      return Doctrine_Core::getTable('Book');
    }

    public function getTitle () {}
  }

  class FrontendBookTable extends BookTable
  {
    /**
     * @return FrontendBookTable
     */
    public static function getInstance ()
    {
      return Doctrine_Core::getTable('FrontendBook');
    }
  }

  FrontendBookTable::getInstance()->getName();
?>