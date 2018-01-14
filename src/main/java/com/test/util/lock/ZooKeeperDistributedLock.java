package com.test.util.lock;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ZooKeeperDistributedLock extends AbstractDistributedLock implements InitializingBean,DisposableBean{
	private static final String ROOT_PATH = "/lock";
	
	private ZkClient zkClient;

	private String zkServers;
	
	private int connectionTimeout;
	
	private long forever = 3600000;

	@Override
	protected String doAcquire(String lockKey, long millis) {
		this.createRoot();
		String lockKeyPath = this.lockKeyPath(lockKey);
		long endLine = 0;
		long now = System.currentTimeMillis();
		if(millis < 0){
			endLine = now + forever;
		}else if(millis == 0){
			endLine = now;
		}else{
			endLine = now + millis;
		}
		String lockValue = UUID.randomUUID().toString();
		boolean retry = true;
		while(now <= endLine && retry){
			try{
			    zkClient.createEphemeral(lockKeyPath, lockValue);
			    retry = false;
			}catch (Exception e) {
				e.printStackTrace();
			}
			if(!retry){
				return lockValue;
			}
			if(retry){
				//等待1s
				LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
				now = System.currentTimeMillis();
			}
			
		}
		
		return null;
	}

	@Override
	protected boolean doRelease(String lockKey, String lockValue) {
		String lockKeyPath = this.lockKeyPath(lockKey);
		Object readData = this.zkClient.readData(lockKeyPath, true);
		if(readData == null){//无此节点
			return true;
		}
		if(lockValue.equals(readData)){
			return this.zkClient.delete(lockKeyPath);
		}
		return false;
	}
	
	@Override
	public void destroy() throws Exception {
		this.zkClient.close();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.zkClient = new ZkClient(zkServers, connectionTimeout);
	}
	
	private void createRoot(){
		boolean exists = this.zkClient.exists(ROOT_PATH);
		if(!exists){
			this.zkClient.createPersistent(ROOT_PATH);
		}
	}
	
	private String lockKeyPath(String lockKey){
		return ROOT_PATH+"/"+lockKey;
	}

	public String getZkServers() {
		return zkServers;
	}

	public void setZkServers(String zkServers) {
		this.zkServers = zkServers;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public long getForever() {
		return forever;
	}

	public void setForever(long forever) {
		this.forever = forever;
	}

}
