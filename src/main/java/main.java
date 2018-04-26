import com.sun.mail.iap.ByteArray;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class main {
	
    public static void main(String args[]) {

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        String hostInput = null;
        String node = null;
        int sections_needed = 0;
        InterProcessMutex k1;
    	InterProcessMutex k2;
    	InterProcessMutex k3;
    	InterProcessMutex k4;
    	int maxWait = 9999;
        
        if (args.length > 0) {
            hostInput = args[0];
        }

        String zookeeperConnectionString = hostInput != null ? hostInput : "localhost:2181";
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);

        client.start();

        /* --------------------------------
         * Listen for connection changes
         * -------------------------------- **/
        Listenable<ConnectionStateListener> csl = client.getConnectionStateListenable();
        csl.addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                System.out.println("Connection state: ");
                if (newState.isConnected()) {
                    System.out.println("zk_connection_successful");
                } else {
                    System.out.println("zk_connection_failed");
                }
            }
        });
        
        //Throw exception here maybe? If there is no argument[1] the node will be named "null"
        if (args.length > 1) {
            node = args[1];
            //sections_needed = Integer.parseInt(args[2]); //I hope you gave me an integer...
        }
        
        if (true) {
        	String lockPath = "/" + node;
        	k1 = new InterProcessMutex(client, lockPath+"/k1");
        	k2 = new InterProcessMutex(client, lockPath+"/k2");
        	k3 = new InterProcessMutex(client, lockPath+"/k3");
        	k4 = new InterProcessMutex(client, lockPath+"/k4");
        	
        	try {
                if (k1.acquire(maxWait, TimeUnit.SECONDS) &&
                		k2.acquire(maxWait, TimeUnit.SECONDS) &&
                		k3.acquire(maxWait, TimeUnit.SECONDS) &&
                		k4.acquire(maxWait, TimeUnit.SECONDS)) {
                    try {
                        System.out.println("lock_accepted");

                        // waiting for input before releasing lock
                        int i = System.in.read();
                        System.out.println(i);
                    } finally {
                        k1.release();
                        k2.release();
                        k3.release();
                        k4.release(); 
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not acquire lock");
                System.out.println(e.getMessage());
            }
        }
       

        

        
        
    }
}
