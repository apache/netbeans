<?php
class Contacts extends BaseContacts
{
    public function getName() {
        return $this->getFirstName()." ".$this->getLast_Name();
    }

    public function getPhoneNumbers() {
        $query = Doctrine_Query::create()
                ->from("CellPhones c")
                ->where("c.contact_id = ?", $this->getId());
        $phones = Doctrine::getTable("CellPhones")->getPhoneNumbers($query);^
    }
}