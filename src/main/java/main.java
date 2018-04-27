import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class main {
	private static int maxWait = 9999;

	@SuppressWarnings("null")
	public static void main(String args[]) {

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

		String hostInput = null;
		String node = null;
		int sections_needed = 0;
		String initial_direction = null;
		InterProcessMutex k1 = null;
		InterProcessMutex k2 = null;
		InterProcessMutex k3 = null;
		InterProcessMutex k4 = null;
		List<InterProcessMutex> list_of_locks = new ArrayList<InterProcessMutex>();

		if (args.length > 0) {
			hostInput = args[0];
		}

		System.out.println(hostInput);

		String zookeeperConnectionString = hostInput != null ? hostInput : "localhost:2181";
		CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);

		/*
		 * -------------------------------- Listen for connection changes
		 * --------------------------------
		 **/
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

		client.start();

		// Throw exception here maybe? If there is no argument[1] the node will be named
		// "null"
		if (args.length > 1) {
			node = args[1];
			initial_direction = args[2];
			sections_needed = Integer.parseInt(args[3]); // I hope you gave me an integer...

		}

		String lockPath = "/" + node;

		if (sections_needed == 4) {
			list_of_locks.add(k1 = new InterProcessMutex(client, lockPath + "/k1"));
			list_of_locks.add(k2 = new InterProcessMutex(client, lockPath + "/k2"));
			list_of_locks.add(k3 = new InterProcessMutex(client, lockPath + "/k3"));
			list_of_locks.add(k4 = new InterProcessMutex(client, lockPath + "/k4"));
		} 
		else if (sections_needed == 2 && initial_direction.equals("right")) {
			list_of_locks.add(k3 = new InterProcessMutex(client, lockPath + "/k3"));
			list_of_locks.add(k4 = new InterProcessMutex(client, lockPath + "/k4"));
		} 
		else if (sections_needed == 2 && initial_direction.equals("left")) {
			list_of_locks.add(k1 = new InterProcessMutex(client, lockPath + "/k1"));
			list_of_locks.add(k2 = new InterProcessMutex(client, lockPath + "/k2"));
		}else if (sections_needed == 1) {
			list_of_locks.add(k1 = new InterProcessMutex(client, lockPath + "/k1"));
		}
		
		try {
			if (list_of_locks.size()> 0 && locks_aquired(list_of_locks)) {
				System.out.println("lock_accepted");

				// waiting for input before releasing lock
				int i = System.in.read();
				System.out.println(i);
				for(InterProcessMutex lock : list_of_locks) {
					lock.release();
				}
				list_of_locks = null;
			}

		} catch (Exception e) {
			System.out.println("Could not acquire lock");
			System.out.println(e.getMessage());
		}
		//In case you move around the truck, the truck could be waiting for a continue signal
		//That's why we send one here. It won't effect the intersection management. 
		System.out.println("lock_accepted"); 
		System.exit(0);

	}

	public static boolean locks_aquired(List<InterProcessMutex> list_of_locks) {
		boolean all_locks_aquired = true;

		for (InterProcessMutex lock : list_of_locks) {
			try {
				if (!lock.acquire(maxWait, TimeUnit.SECONDS)) {
					all_locks_aquired = false;
				}
			} catch (Exception e) {
				System.out.println("Could not acquire lock");
				System.out.println(e.getMessage());
			}
		}
		return all_locks_aquired;

	}
}
