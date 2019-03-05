package com.stony.mc;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>@RunWith(RetryRunner.class)</p>
 * Created by stony on 16/11/22.
 */
public class RetryRunner extends BlockJUnit4ClassRunner {
    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public RetryRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        if (method.getAnnotation(Repeat.class) != null && method.getAnnotation(Ignore.class) == null) {
            return describeRepeatTest(method);
        }
        return super.describeChild(method);
    }

    private Description describeRepeatTest(FrameworkMethod method) {
        int repeat = method.getAnnotation(Repeat.class).value();
        repeat = Math.max(1, repeat);
        Description description = Description.createSuiteDescription(
                testName(method) + " [" + repeat + " times]",
                method.getAnnotations());

        for (int i = 1; i <= repeat; i++) {
            Description d = Description.createSuiteDescription("[" + i + "] " + testName(method));
            d.addChild(Description.createTestDescription(getTestClass().getJavaClass(), testName(method)));
            description.addChild(d);
        }
        return description;
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        if (method.getAnnotation(Repeat.class) != null) {
            if (method.getAnnotation(Ignore.class) == null) {
                Description description = describeRepeatTest(method);
                runRepeatedly(methodBlock(method), description, notifier);
            }
            return;
        }
        super.runChild(method, notifier);
    }

    private void runRepeatedly(Statement statement, Description description,RunNotifier notifier) {
        for (Description desc : description.getChildren()) {
            runLeaf(statement, desc, notifier);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @interface Repeat {
        int value();
    }
}
