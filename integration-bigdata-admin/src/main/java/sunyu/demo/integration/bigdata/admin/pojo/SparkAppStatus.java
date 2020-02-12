package sunyu.demo.integration.bigdata.admin.pojo;

public class SparkAppStatus {

    private String attemptNumber = "";//尝试次数

    private String id = "";//任务ID
    private String url;//hadoop页面
    private String name = "";//任务名称
    private String startTime = "";//任务开始时间

    private String containers = "";//申请容器
    private String cpu = "";//申请cpu
    private String memory = "";//申请内存

    private String dead = "";//死掉的
    private String failed = "";//出错的

    private String input = "";//数据量
    private String schedulingDelay = "";//延迟
    private String processingTime = "";//执行时间
    private String totalDelay = "";//总延迟

    private String windowPeriod = "";//执行周期

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(String attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainers() {
        return containers;
    }

    public void setContainers(String containers) {
        this.containers = containers;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getDead() {
        return dead;
    }

    public void setDead(String dead) {
        this.dead = dead;
    }

    public String getFailed() {
        return failed;
    }

    public void setFailed(String failed) {
        this.failed = failed;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getSchedulingDelay() {
        return schedulingDelay;
    }

    public void setSchedulingDelay(String schedulingDelay) {
        this.schedulingDelay = schedulingDelay;
    }

    public String getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    public String getTotalDelay() {
        return totalDelay;
    }

    public void setTotalDelay(String totalDelay) {
        this.totalDelay = totalDelay;
    }

    public String getWindowPeriod() {
        return windowPeriod;
    }

    public void setWindowPeriod(String windowPeriod) {
        this.windowPeriod = windowPeriod;
    }
}
