<?php

namespace Maslosoft\NbReport\Interfaces;

interface ConstInterface
{

	const MyConstant = 'test';

}

namespace Maslosoft\NbReport;

use Maslosoft\NbReport\Interfaces\ConstInterface;

class BaseClass
{

	public function __construct($myParam = ConstInterface::MyConstant)
	{
		;
	}

	public function myMethod($myParam = ConstInterface::MyConstant)
	{

	}

}

class DerivedClass extends BaseClass {
}
