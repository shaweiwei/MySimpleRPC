package com.myrpc.centre;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务中心实现类
 * @author ko
 *
 */
public class RPCServerCenter implements RPCServer {
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static HashMap<String, Class> serviceRegistry = new HashMap<String, Class>();
	
	private static boolean isRunning;
	
	private static int port;
	
	

	public RPCServerCenter(int port) {
		this.port = port;
	}

	public synchronized void start() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(port));
			isRunning = true;
			System.out.println("RPC server "+port+" started... ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		try {
			while (true) {
				Socket accpetclient = serverSocket.accept();
				// 1.监听客户端的TCP连接，接到TCP连接后将其封装成task，由线程池执行
				System.out.println("监听客户端的TCP连接中...");
				if (accpetclient.isClosed()) {
//					stop();// 客户端断开连接，关闭服务线程池
//					break;
					System.out.println("客户端"+accpetclient.getLocalPort()+"断开连接...");
				}else{
					executorService.execute(new ServerTask(accpetclient.getInputStream(),accpetclient.getOutputStream()));
				}
				Thread.sleep(5000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	public synchronized void stop() {
		if (isRunning) {
			 isRunning = false;
			executorService.shutdown();
			System.out.println("RPC server stoped... ");
		}
	}

	public void register(Class serviceInterface, Class impl) {
		serviceRegistry.put(serviceInterface.getName(), impl);
		System.out.println(serviceInterface.getName()+" register to rpc server center...");
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getPort() {
		return port;
	}
	
	private class ServerTask implements Runnable{
		
//		private Socket client;
		private InputStream is = null;
		private OutputStream os = null;
		
		public ServerTask(InputStream is,OutputStream os){
//			this.client = client;
			this.is = is;
			this.os = os;
		}

		public void run() {
			ObjectInputStream input = null;
			ObjectOutputStream output = null;
			try {
				// 2.将客户端发送的码流反序列化成对象，反射调用服务实现者，获取执行结果
				input = new ObjectInputStream(is);
				
				String serviceName = input.readUTF();
				String methodName = input.readUTF();
				
				Class<?>[] parameterTypes = (Class<?>[])input.readObject();
				Object[] arguments = (Object[])input.readObject();
				Class serviceClass = serviceRegistry.get(serviceName);
				if (serviceClass == null) {
					throw new ClassNotFoundException(serviceName+" not found");
				}
				
				Method method = serviceClass.getMethod(methodName, parameterTypes);
				Object result = method.invoke(serviceClass.newInstance(), arguments);
				
				// 3.将执行结果反序列化，通过socket发送给客户端
				output = new ObjectOutputStream(os);
				output.writeObject(result);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} finally{
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
//				if (client != null) {
//					try {
//						client.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
			}
			
			
		}
		
	}
	

}
