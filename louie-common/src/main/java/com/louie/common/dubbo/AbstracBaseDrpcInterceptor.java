/**
 * 
 */
package com.louie.common.dubbo;

import com.alibaba.dubbo.rpc.Filter;
import com.louie.common.constant.ZipkinConstants;
import com.louie.utils.StringUtils;

/**
 * @author Cacti
 *  此处省去配置文件，避免给原有项目带来配置的困扰，使用-D的JVM属性来进行配置。
 *	2017年8月31日
 * 
 */
public abstract class AbstracBaseDrpcInterceptor implements Filter {
	protected boolean isPropNotSet = true;
	
	protected final String BRAVE_NAME ;
	
	protected final String SEND_ADDRESS;
	
	public AbstracBaseDrpcInterceptor() {
			BRAVE_NAME  = System.getProperty(ZipkinConstants.BRAVE_NAME);
			SEND_ADDRESS = System.getProperty(ZipkinConstants.SEND_ADDRESS);
			if(StringUtils.isNotBlank(BRAVE_NAME) || StringUtils.isNotBlank(SEND_ADDRESS) ) {
				isPropNotSet = false;
			}
	}

}
