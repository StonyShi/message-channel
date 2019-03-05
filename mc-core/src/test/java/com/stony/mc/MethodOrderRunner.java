package com.stony.mc;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午3:17
 * @since 2019/1/15
 */
public class MethodOrderRunner extends BlockJUnit4ClassRunner {


    ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(getChildren().size());
    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public MethodOrderRunner(Class<?> klass) throws InitializationError {
        super(klass);

        setScheduler(new RunnerScheduler() {
            @Override
            public void schedule(Runnable childStatement) {
                System.out.println();
                System.out.println("begin : " + childStatement);
                childStatement.run();

                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                childStatement.run();
                System.out.println();
                System.out.println("end : " + childStatement);
            }

            @Override
            public void finished() {
                System.out.println("-------finished----");
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("------ 关闭 ---");

            }
        });
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (isIgnored(method)) {
            notifier.fireTestIgnored(description);
        } else {
            runLeafAyncs(methodBlock(method), description, notifier);
        }
    }

    protected final void runLeafAyncs(Statement statement, Description description, RunNotifier notifier) {
        final EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        executorService.submit(() -> {
            try {
                eachNotifier.fireTestStarted();
                statement.evaluate();
            } catch (AssumptionViolatedException e) {
                eachNotifier.addFailedAssumption(e);
            } catch (Throwable e) {
                eachNotifier.addFailure(e);
            } finally {
                eachNotifier.fireTestFinished();
            }
        });

    }
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> list = getTestClass().getAnnotatedMethods(Test.class);
        return list
                .stream()
                .sorted(new MethodOrderComparator())
                .collect(Collectors.toList());
    }
    class MethodOrderComparator implements Comparator<FrameworkMethod> {
        @Override
        public int compare(FrameworkMethod a, FrameworkMethod b) {
            MethodOrder am = a.getMethod().getAnnotation(MethodOrder.class);
            MethodOrder bm = b.getMethod().getAnnotation(MethodOrder.class);
            if(am != null && bm != null) {
                return am.value() - bm.value();
            }
            return 0;
        }
    }
}
