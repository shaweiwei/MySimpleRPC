package com.myrpc;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.myrpc.centre.RPCServer;
import com.myrpc.centre.RPCServerCenter;
import com.myrpc.client.RPCClient;
import com.myrpc.service.HelloService;
import com.myrpc.service.HelloServiceImpl;

public class Test {

	RPCServer rpcserver = null;
	public static void main(String[] args) {
		int port = 5680;
		Test test = new Test();
		test.doing(port);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
//		test.stopRpcServer();
		
	}
	
	public void doing(final int port){
		
		new Thread(new Runnable() {
			public void run() {
				rpcserver = new RPCServerCenter(port);
				// 注册要调用的远程类HelloServiceImpl至rpc服务中心
				rpcserver.register(HelloService.class, HelloServiceImpl.class);
				rpcserver.start();// 启动rpc服务中心
			}
		}).start();
		
		
		// 模拟多次调用远程方法
		ExecutorService clientes = Executors.newCachedThreadPool();
		for (int i = 0; i < 10; i++) {
			clientes.execute(new Runnable() {
				public void run() {
					HelloService service = RPCClient.getRemoteProxyObj(HelloService.class, new InetSocketAddress("127.0.0.1", port));
					System.out.println(service.sayHi("jacke "+UUID.randomUUID().toString().substring(0, 6)));
				}
			});
		}
		
	}
	
	public void stopRpcServer(){
		rpcserver.stop();
	}
	
}
