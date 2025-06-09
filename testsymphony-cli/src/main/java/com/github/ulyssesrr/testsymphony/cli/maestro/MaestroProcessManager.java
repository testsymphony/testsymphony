package com.github.ulyssesrr.testsymphony.cli.maestro;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MaestroProcessManager {

    private final ReentrantLock lock = new ReentrantLock();
    private Process process;

    /**
     * Starts the executable process if not already running.
     *
     * @param command the command to start the process (e.g. a script or binary path)
     * @throws IOException if the process fails to start
     * @throws IllegalStateException if the process is already running
     */
    public void startProcess() throws IOException {
        lock.lock();
        try {
            if (isProcessRunning()) {
                throw new IllegalStateException("Process is already running");
            }

            ProcessBuilder builder = new ProcessBuilder("maestro studio");
            builder.redirectErrorStream(true); // merge stderr with stdout if desired
            this.process = builder.start();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns true if the process is currently running.
     *
     * @return boolean indicating running status
     */
    public boolean isProcessRunning() {
        lock.lock();
        try {
            return process != null && process.isAlive();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stops the currently running process.
     */
    public void stopProcess() {
        lock.lock();
        try {
            if (isProcessRunning()) {
                process.destroy();
                process.waitFor(); // optionally wait for it to fully terminate
            }
            process = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while stopping process", e);
        } finally {
            lock.unlock();
        }
    }
}
