package org.nsu.syspro.parprog;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nsu.syspro.parprog.base.DefaultFork;
import org.nsu.syspro.parprog.base.DiningTable;
import org.nsu.syspro.parprog.examples.DefaultPhilosopher;
import org.nsu.syspro.parprog.helpers.TestLevels;
import org.nsu.syspro.parprog.interfaces.Fork;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomSchedulingTest extends TestLevels {

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2)
    void testDeadlockFreedom(int N) {
        final CustomizedTable table = dine(new CustomizedTable(N), 1);
    }

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(5)
    void testSingleSlow(int N) {
        final DiningTable<DefaultPhilosopher, DefaultFork> table = dine(new SlowBasicTable(N), 2);

        assertTrue(table.maxMeals() > 1000, "Any Philosopher should have eaten at least 1000 times");
    }

    @EnabledIf("mediumEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2)
    void testWeakFairness(int N) {
        final DiningTable<DefaultPhilosopher, DefaultFork> table = dine(new DiningTable<>(N) {
            @Override
            public DefaultFork createFork() {
                return new AlmostDefaultFork();
            }

            @Override
            public DefaultPhilosopher createPhilosopher() {
                return new WeakUnfairPhilosopher();
            }
        }, 1);

        assertTrue(table.minMeals() > 0);
    }

    @EnabledIf("hardEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2)
    void testImpossibleFairness(int N) {
        final DiningTable<DefaultPhilosopher, DefaultFork> table = dine(new DiningTable<>(N) {
            @Override
            public DefaultFork createFork() {
                return new AlmostDefaultFork();
            }

            @Override
            public DefaultPhilosopher createPhilosopher() {
                return new StrongUnfairPhilosopher();
            }
        }, 1);
        final long minMeals = table.minMeals();
        final long maxMeals = table.maxMeals();
        assertFalse(maxMeals < 1.5 * minMeals); // some king of gini index for philosophers
    }

    static final class CustomizedPhilosopher extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            sleepMillis(this.id * 20);
            System.out.println(Thread.currentThread() + " " + this + ": onHungry");
            super.onHungry(left, right);
        }
    }

    static final class CustomizedFork extends DefaultFork {
        @Override
        public void acquire() {
            System.out.println(Thread.currentThread() + " trying to acquire " + this);
            super.acquire();
            System.out.println(Thread.currentThread() + " acquired " + this);
            sleepMillis(100);
        }
    }

    static final class AlmostDefaultFork extends DefaultFork {
        @Override
        public void acquire() {
            System.out.println(Thread.currentThread() + " trying to acquire " + this);
            super.acquire();
            System.out.println(Thread.currentThread() + " acquired " + this);
        }
    }

    static final class CustomizedTable extends DiningTable<CustomizedPhilosopher, CustomizedFork> {
        public CustomizedTable(int N) {
            super(N);
        }

        @Override
        public CustomizedFork createFork() {
            return new CustomizedFork();
        }

        @Override
        public CustomizedPhilosopher createPhilosopher() {
            return new CustomizedPhilosopher();
        }
    }


    static final class WeakUnfairPhilosopher extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            System.out.println(Thread.currentThread() + " " + this + ": onHungry");
            if (this.id % 2 == 0) {
                super.onHungry(left, right);
                sleepMillis(10);
            } else {
                super.onHungry(left, right);
                sleepMillis(100);
            }
        }
    }

    static final class StrongUnfairPhilosopher extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            System.out.println(Thread.currentThread() + " " + this + ": onHungry");
            if (this.id % 2 == 0) {
                super.onHungry(left, right);
                sleepMillis(50);
            } else {
                super.onHungry(left, right);
            }
        }
    }

    static final class SlowBasicTable extends DiningTable<DefaultPhilosopher, DefaultFork> {
        private int counter = 0;

        public SlowBasicTable(int N) {
            super(N);
        }

        @Override
        public DefaultFork createFork() {
            return new AlmostDefaultFork();
        }

        @Override
        public DefaultPhilosopher createPhilosopher() {
            counter++;
            if (counter == 1) {
                return new SlowPhilosopher();
            } else {
                return new DefaultPhilosopher();
            }

        }

        static final class SlowPhilosopher extends DefaultPhilosopher {
            @Override
            public void onHungry(Fork left, Fork right) {
                System.out.println(Thread.currentThread() + " " + this + ": onHungry");
                Fork minFork = left.id() < right.id() ? left : right;
                Fork maxFork = left.id() > right.id() ? left : right;

                minFork.acquire();
                maxFork.acquire();
                sleepMillis(1000);
                countMeal();
                minFork.release();
                maxFork.release();
            }
        }
    }
}
