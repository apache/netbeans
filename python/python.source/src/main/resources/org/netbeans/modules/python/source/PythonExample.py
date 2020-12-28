"""Sample Module"""
import foo
CONSTANT = "VALUE"

class Foo(Bar):
    """Class doc"""

    CLASS_VARIABLE = True

    def __init__(self, args=''):
        # Comment
        print 1 + 2.0
        self.do()

    @decorator
    def do(self):
        # TODO: make it useful
        pass
