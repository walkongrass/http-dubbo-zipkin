package com.louie.common.dubbo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.ClientSpanThreadBinder;
import com.github.kristofa.brave.IdConversion;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.internal.Nullable;
import com.github.kristofa.brave.internal.Util;
import com.louie.utils.JsonUtils;
import com.twitter.zipkin.gen.Span;

import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.Sender;
import zipkin.reporter.okhttp3.OkHttpSender;

@Activate(group = Constants.CONSUMER)
public class DrpcClientInterceptor extends AbstracBaseDrpcInterceptor{
	
    private final ClientRequestInterceptor clientRequestInterceptor;
    private final ClientResponseInterceptor clientResponseInterceptor;
    private final ClientSpanThreadBinder clientSpanThreadBinder;
    
    public DrpcClientInterceptor() {
    	Sender sender = OkHttpSender.create(SEND_ADDRESS);
    	Reporter<zipkin.Span> reporter = AsyncReporter.builder(sender).build();
    	Brave brave = new Brave.Builder(BRAVE_NAME).reporter(reporter).build();
        this.clientRequestInterceptor = Util.checkNotNull(brave.clientRequestInterceptor(),null);
        this.clientResponseInterceptor = Util.checkNotNull(brave.clientResponseInterceptor(),null);
        this.clientSpanThreadBinder = Util.checkNotNull(brave.clientSpanThreadBinder(),null);
    }

   
	public Result invoke(Invoker<?> arg0, Invocation arg1) throws RpcException {
		if(isPropNotSet || DUBBO_MONITOR_SERVICE.equalsIgnoreCase(arg0.getInterface().getName())) {
			return arg0.invoke(arg1);
		}
		clientRequestInterceptor.handle(new GrpcClientRequestAdapter(arg1));
		Map<String,String> att = arg1.getAttachments();
		final Span currentClientSpan = clientSpanThreadBinder.getCurrentClientSpan();
		Result result ;
		try {
			result =  arg0.invoke(arg1);
            clientSpanThreadBinder.setCurrentSpan(currentClientSpan);
            clientResponseInterceptor.handle(new GrpcClientResponseAdapter(result));
        } finally {
            clientSpanThreadBinder.setCurrentSpan(null);
        }
		return result;
	}

    static final class GrpcClientRequestAdapter implements ClientRequestAdapter {
    	private Invocation invocation;
        public GrpcClientRequestAdapter(Invocation invocation) {
            this.invocation = invocation;
        }

       
        public String getSpanName() {
             return invocation.getInvoker().getUrl().getServiceInterface();
        }

       
        public void addSpanIdToRequest(@Nullable SpanId spanId) {
        	Map<String,String> at = this.invocation.getAttachments();
            if (spanId == null) {
                at.put("Sampled", "0");
            } else {
            	
                at.put("Sampled", "1");
                at.put("TraceId", spanId.traceIdString());
                at.put("SpanId", IdConversion.convertToString(spanId.spanId));
                
                if (spanId.nullableParentId() != null) {
                    at.put("ParentSpanId", IdConversion.convertToString(spanId.parentId));
                }
            }
        }

       
        public Collection<KeyValueAnnotation> requestAnnotations() {
        	Class params[] = invocation.getParameterTypes();
        	HashMap<String, Object> data = new HashMap<String, Object>();
        	int i = 0;
        	for(Class clazz : params) {
        		data.put(clazz.getName(), invocation.getArguments()[i]);
        		i ++;
        	}
        	KeyValueAnnotation an = KeyValueAnnotation.create("params", JsonUtils.map2Json(data));
            return Collections.singletonList(an);
        }
       
        public com.twitter.zipkin.gen.Endpoint serverAddress() {
            return null;
        }
    }

    static final class GrpcClientResponseAdapter implements ClientResponseAdapter {

        private final Result result;

        public GrpcClientResponseAdapter(Result result) {
            this.result = result;
        }

        
        public Collection<KeyValueAnnotation> responseAnnotations() {
        	return Collections.<KeyValueAnnotation>emptyList();
            /*
        	return !result.hasException()
                ? Collections.<KeyValueAnnotation>emptyList()
                : Collections.singletonList(KeyValueAnnotation.create("error", result.getException().getMessage()));
                */
        }
    }
}