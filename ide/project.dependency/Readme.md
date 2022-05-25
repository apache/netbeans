# Project Dependencies

This module is highly experimental; provides an abstract API to query project dependncies
that shall be implemented by
- gradle
- maven
- ant/nbm support
At this moment, just Maven implements it and I need to validate the API design by adding
the other implementations before the API is going to be published officially. In the
meantime, implementation dependencies are required to access / implement the API.

The code will **eventually merge** into `project.api` module and this experimental one
will be deleted.

