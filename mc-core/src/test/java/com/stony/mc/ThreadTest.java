package com.stony.mc;

import com.stony.mc.concurrent.NamedThreadFactory;
import com.stony.mc.concurrent.TaskExecutorFactory;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午2:12
 * @since 2019/1/11
 */
public class ThreadTest {

    @Test
    public void test_13() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor executor = TaskExecutorFactory.getElasticExecutor("test", 10);
        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            print_log("task1 doing...");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "result1";
        }, executor);
        CompletableFuture<String> completableFuture2 = completableFuture1.thenCompose(result -> {
                    print_log("thenCompose doing...");
                    return CompletableFuture.supplyAsync(() -> {
                        print_log("task2 doing...");
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "result2";
                    }, executor);
                }
        );
        System.out.println(completableFuture2.get());
        executor.shutdown();
    }

    /**
     * 1. 异步获取汽车列表
     * 2. 异步更新汽车得分
     * 3. 异步合并结果
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_async() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor executor = TaskExecutorFactory.getElasticExecutor("test", 10);
        CompletableFuture<List<Car>> cars = CompletableFuture.supplyAsync(() -> {
            print_log("get car list doing...");
            return Arrays.asList(new Car("奔驰"), new Car("宝马"), new Car("奥迪"));
        }, executor).thenCompose(list -> {
            CompletableFuture<Car>[] updateds = list.stream().map(car -> CompletableFuture.supplyAsync(() -> {
                print_log("get car score doing...");
                return ThreadLocalRandom.current().nextInt(0, 10);
            }, executor).thenApply(score -> {
                print_log("update car score doing...");
                return car.score(score);
            })).toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(updateds).thenApplyAsync(aVoid -> {
                print_log("all join...");
                return Arrays.stream(updateds).map(CompletableFuture::join).collect(Collectors.toList());
            }, executor);
        }).whenComplete((vv, e) -> {
            if(e == null) {
                print_log("update completed car score: %s", vv);
            } else {
                print_log("error: %s", e.getMessage());
            }
        });
        System.out.println(cars.get());
        executor.shutdown();
    }

    static void print_log(String format, Object... args) {
        System.out.println(String.format("[%s] >>> %s", Thread.currentThread().getName(), String.format(format, args)));
    }
    class Car{
        String name;
        int score;
        public Car(String name) {
            this.name = name;
        }
        public Car score(int score) {
            this.score = score;
            return this;
        }

        @Override
        public String toString() {
            return "Car{" +
                    "name='" + name + '\'' +
                    ", score=" + score +
                    '}';
        }
    }


    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1,new NamedThreadFactory("timeout-schedule"));

    public static <T> CompletableFuture<T> failAfter(Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        scheduler.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + duration);
            return promise.completeExceptionally(ex);
        }, duration.toMillis(), TimeUnit.MILLISECONDS);
        return promise;
    }
    public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
        return future.applyToEither(failAfter(duration), Function.identity());
    }
    /**
     * 1. 异步获取汽车列表 超时异常
     * 2. 异步更新汽车得分
     * 3. 异步合并结果
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_timeout() throws ExecutionException, InterruptedException {
        int timeout = 5; //5秒超时
        ThreadPoolExecutor executor = TaskExecutorFactory.getElasticExecutor("test", 10);
        CompletableFuture<List<Car>> cars = within(CompletableFuture.supplyAsync(() -> {
            print_log("get car list doing...");
            try {
                TimeUnit.SECONDS.sleep(timeout+1);
            } catch (InterruptedException e) {}
            return Arrays.asList(new Car("奔驰"), new Car("宝马"), new Car("奥迪"));
        }, executor), Duration.ofSeconds(timeout))
        .thenCompose(list -> {
            CompletableFuture<Car>[] updateds = list.stream().map(car -> CompletableFuture.supplyAsync(() -> {
                print_log("get car score doing...");
                return ThreadLocalRandom.current().nextInt(0, 10);
            }, executor).thenApply(score -> {
                print_log("update car score doing...");
                return car.score(score);
            })).toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(updateds).thenApplyAsync(aVoid -> {
                print_log("all join...");
                return Arrays.stream(updateds).map(CompletableFuture::join).collect(Collectors.toList());
            }, executor);
        }).whenComplete((vv, e) -> {
            if (e == null) {
                print_log("update completed car score: %s", vv);
            } else {
                print_log("error: %s", e.getMessage());
            }
        });
        System.out.println(cars.get());
        executor.shutdown();
    }
}
