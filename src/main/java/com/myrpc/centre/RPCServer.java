package com.myrpc.centre;

/**
 * 服务中心接口
 * @author ko
 *
 */
public interface RPCServer {
	
	public void start();
	
	public void stop();
	
	public void register(Class serviceInterface, Class impl);
	
	public boolean isRunning();
	
	public int getPort();

}
