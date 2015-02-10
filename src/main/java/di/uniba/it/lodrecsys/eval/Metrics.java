package di.uniba.it.lodrecsys.eval;

/**
 * Created by simo on 09/02/15.
 */
public class Metrics {
    private String user;
    private String metric;

    public Metrics(String user, String metric) {
        this.user = user;
        this.metric = metric;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
