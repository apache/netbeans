Tests created in this package are here just because there is some problem with Classpath when multiple tests are run from
the same file (some classpath information are lost after the first test and as a result other tests dependent on this
classpath information will fail). Because at the moment I have no clue where is the problem, I decided to created temporary
package for these few tests and create new file per test. I will fix the original problem later with better groovy base knowledge.

In each of those newly created tests there is a comment containing a reference to the original file where the test should be located.