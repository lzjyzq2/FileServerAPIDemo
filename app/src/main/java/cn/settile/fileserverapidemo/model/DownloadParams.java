package cn.settile.fileserverapidemo.model;

public class DownloadParams {
    private String[] path;
    private String name;

    public DownloadParams() {
    }

    public DownloadParams(String[] path, String name) {
        this.path = path;
        this.name = name;
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
