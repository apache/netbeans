<?php

function getCompany() {
return array(
"ic" => $this->getIc(),
"name" => $this->getName(),
"general_name" => $this->getGeneral_name(),
"category" => $this->getCategory(),
"password" => $this->getPassword(),
"description" => $this->getDescription(),
"street" => $this->getStreet(),
"streetNo" => $this->getStreetNo(),
"city" => $this->getCity(),
"zip" => $this->getZIP(),
"email" => $this->getEmail(),
"category" => $this->getCategory(),
"phone1" => $this->getPhone1(),
"phone1_type" => $this->getPhone1_type(),
"phone2" => $this->getPhone2(),
"phone2_type" => $this->getPhone2_type(),
"phone3" => $this->getPhone3(),
"phone3_type" => $this->getPhone3_type()
);
}

?>