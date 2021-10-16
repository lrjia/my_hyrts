package set.hyrts.demo.testCase;

import set.hyrts.demo.src.son;
import set.hyrts.demo.test;

public class TestCase2 implements test {
    @Override
    public void runTest() {
        son s=new son();
        s.sayHi();
    }
}
