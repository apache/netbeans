<?php

namespace a
{
	use c\C;

	abstract class A
	{
		/**
		 *	@return C[]
		 */
		public function load()
		{
			return array();
		}
	}
}

namespace b
{
	use a\A;
	use d\D;

	class B extends A
	{
		/**
		 *	@return D[]
		 */
		public function load()
		{
			return parent::load();
		}
	}
}

namespace c
{
	class C
	{
		public function c()
		{
			echo "c";
		}
	}
}

namespace d
{
	use c\C;

	class D extends C
	{
		public function c()
		{
			echo "d";
		}

		public function d()
		{
			echo "d";
		}
	}

}

namespace
{
    use b\B;
	$b=new B;

	foreach ($b->load() as $cur_c)
	{
		$cur_c->
	}
}
?>