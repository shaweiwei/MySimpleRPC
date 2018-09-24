package com.myrpc.service;

/**
 * 服务提供者实现类
 * @author ko
 *
 */
public class HelloServiceImpl implements HelloService {

	public String sayHi(String name) {
		// TODO Auto-generated method stub
		return "Hi "+name;
	}

}
