/**
 * 
 */
package com.louie.common.dubbo;

import org.apache.log4j.Logger;

import com.alibaba.dubbo.monitor.MonitorService;
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
	private  final Logger logger = Logger.getLogger(this.getClass());
	protected boolean isPropNotSet = true;
	
	protected final String BRAVE_NAME ;
	
	protected final String SEND_ADDRESS;
	
	protected final static String DUBBO_MONITOR_SERVICE = MonitorService.class.getName();
	
	public AbstracBaseDrpcInterceptor() {
			BRAVE_NAME  = System.getProperty(ZipkinConstants.BRAVE_NAME);
			SEND_ADDRESS = System.getProperty(ZipkinConstants.SEND_ADDRESS);
			if(StringUtils.isBlank(BRAVE_NAME) || StringUtils.isBlank(SEND_ADDRESS) ) {
				logger.warn("没有正确获取JVM属性配置，将不采集对应信息.如果需要，请使用java -D"
						.concat(ZipkinConstants.BRAVE_NAME).concat("=应用名称 -D")
						.concat(ZipkinConstants.SEND_ADDRESS).concat("=zipkin url"));
			}
			else {
				logger.info("Dubbo-Zipkin属性设置信息："
						.concat(ZipkinConstants.BRAVE_NAME).concat("=").concat(BRAVE_NAME).concat(" ")
						.concat(ZipkinConstants.SEND_ADDRESS).concat("=").concat(SEND_ADDRESS));
				isPropNotSet = false;
			}
	}

}
