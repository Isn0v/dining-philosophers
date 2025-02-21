package org.nsu.syspro.parprog.examples;

import org.nsu.syspro.parprog.interfaces.Fork;
import org.nsu.syspro.parprog.interfaces.Philosopher;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultPhilosopher implements Philosopher {

    private static final AtomicLong idProvider = new AtomicLong(0);
    private static final ReentrantLock lock = new ReentrantLock();
    public final long id;
    private long successfulMeals;

    public DefaultPhilosopher() {
        this.id = idProvider.getAndAdd(1);
        this.successfulMeals = 0;
    }

    @Override
    public long meals() {
        return successfulMeals;
    }

    @Override
    public void countMeal() {
        successfulMeals++;
    }

    public void onHungry(Fork left, Fork right) {
        Fork minFork = left.id() < right.id() ? left : right;
        Fork maxFork = left.id() > right.id() ? left : right;
        eat(minFork, maxFork);
    }

    @Override
    public String toString() {
        return "DefaultPhilosopher{" +
                "id=" + id +
                '}';
    }
}
