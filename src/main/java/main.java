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

        String hostInput = null;

        if (args.length > 0) {
            hostInput = args[0];
        }

        System.out.println(hostInput);

        String zookeeperConnectionString = hostInput != null ? hostInput : "localhost:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);

        client.start();

        String lockPath = "/_locknode_";

        int maxWait = 9999;

        InterProcessMutex lock = new InterProcessMutex(client, lockPath);
        try {
            if (lock.acquire(maxWait, TimeUnit.SECONDS)) {
                try {
                    System.out.println("lock_accepted");

                    // waiting for input before releasing lock
                    int i = System.in.read();
                    System.out.println(i);
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
