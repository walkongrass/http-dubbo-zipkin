package com.louie.utils;

import java.util.Map;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;


/**
 * JSON工具类
 * @author 丁威 2015年9月25日 下午5:56:25
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public abstract class JsonUtils {
	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> jsonToMap(String jsonStr) {
		return (Map<String, Object>)json2Map(jsonStr);
	}
	
	public static String mapToJson(Map<String, Object> map) {
		return object2JSON(map);
	}
	
	public static final Map json2Map(String jsonStr) {
		if(StringUtils.isBlank(jsonStr)) return null;
		
		try {
			return JSON.parseObject(jsonStr, Map.class);
		} catch (Exception e) {
			logger.error("Json转换异常", e);
			return null;
		} 
	}
	
	
	public static final SortedMap json2SortedMap(String jsonStr) {
		if(StringUtils.isBlank(jsonStr)) return null;
		
		try {
			return JSON.parseObject(jsonStr, SortedMap.class);
		} catch (Exception e) {
			logger.error("Json转换异常", e);
			return null;
		} 
	}
	
	public static String object2JSON(Object obj) {
		if(obj == null){
			return "{}";
		}
		
		try {
			return JSON.toJSONString(obj);
		} catch (Exception e) {
			logger.error("转换异常", e);
			return "";
		}
		//return JSON.toJSONString(obj,SerializerFeature.WriteDateUseDateFormat);
	}
	
	public static String map2Json(Map map) {
		return object2JSON(map);
	}
	
	public static final <T> T json2Bean(String content, Class<T> valueType) {
		if (StringUtils.isBlank(content))
			return null;

		try {
			return JSON.parseObject(content,valueType);
		} catch (Exception e) {
			logger.error("Json转换异常", e);
			return null;
		}
	}
	
	public static final Map beanToMap(Object obj) {
		try {
			return json2Map(object2JSON(obj));
		} catch (Exception e) {
			logger.error("Json转换异常", e);
			return null;
		}
	}
}

