package set.hyrts.demo.testCase;

import set.hyrts.demo.src.parent;
import set.hyrts.demo.test;

public class TestCase1 implements test {

    @Override
    public void runTest() {
        parent p=new parent();
        p.sayHi();
    }
}
