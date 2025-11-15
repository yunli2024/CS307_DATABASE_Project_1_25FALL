public class StopWatch {

    private long startTime;
    private long endTime;
    private boolean running = false;

    // 开始计时
    public void start() {
        running = true;
        startTime = System.currentTimeMillis();
    }

    // 停止计时
    public void stop() {
        endTime = System.currentTimeMillis();
        running = false;
    }

    // 获取毫秒级耗时
    public long getElapsedMillis() {
        return (running ? System.currentTimeMillis() : endTime) - startTime;
    }

    // 获取秒级耗时（方便写报告）
    public double getElapsedSeconds() {
        return getElapsedMillis() / 1000.0;
    }

    // 打印结果（建议用于调试）
    public void print(String tag) {
        System.out.printf("[%s] Time elapsed: %.3f seconds%n",
                tag, getElapsedSeconds());
    }
}
