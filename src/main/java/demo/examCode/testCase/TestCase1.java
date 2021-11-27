package demo.examCode.testCase;

import demo.examCode.src.parent;
import demo.examCode.test;

public class TestCase1 implements test {

    @Override
    public void runTest() {
        parent p=new parent();
        p.sayHi();
    }
}
