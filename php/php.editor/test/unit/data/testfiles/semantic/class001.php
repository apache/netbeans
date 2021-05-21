<?php
class user {
  public $first_name,$family_name,$address,$phone_num;
  function display()
  {
    echo "User information\n";
    echo "----------------\n\n";
    echo "First name:\t  ".$this->first_name."\n";
    echo "Family name:\t  ".$this->family_name."\n";
    echo "Address:\t  ".$this->address."\n";
    echo "Phone:\t\t  ".$this->phone_num."\n";
    echo "\n\n";
  }
  function initialize($first_name,$family_name,$address,$phone_num)
  {
    $this->first_name = $first_name;
    $this->family_name = $family_name;
    $this->address = $address;
    $this->phone_num = $phone_num;
  }
};
?>