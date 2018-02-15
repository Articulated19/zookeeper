import com.sun.mail.iap.ByteArray;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class main {
    public static void main(String args[]) {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        String zookeeperConnectionString = "localhost:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);

        client.start();

        String lockPath = "/_locknode_";

        int maxWait = 9999;

        InterProcessMutex lock = new InterProcessMutex(client, lockPath);
        try {
            if (lock.acquire(maxWait, TimeUnit.SECONDS)) {
                try {
                    System.out.println("Doing work");
                    sleep(5000);
                } finally {
                    lock.release();
                }
            }
        } catch (Exception e) {
            System.out.println("Could not acquire lock");
            System.out.println(e.getMessage());
        }

    }
}
