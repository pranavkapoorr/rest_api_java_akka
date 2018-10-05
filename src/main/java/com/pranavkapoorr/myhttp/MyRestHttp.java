package com.pranavkapoorr.myhttp;

import java.util.concurrent.CompletionStage;
import akka.actor.ActorSystem;
import akka.http.javadsl.*;
import akka.http.javadsl.model.*;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.*;

public class MyRestHttp {
	final ActorSystem system;
	final Materializer materializer;
	final int bindingPort;
	
	private MyRestHttp(ActorSystem system,int bindingPort) {
		this.system = system;
		this.materializer = ActorMaterializer.create(system);
		this.bindingPort = bindingPort;
	}
	
	public static MyRestHttp getInstance(ActorSystem system,int bindingPort){
		return new MyRestHttp(system, bindingPort);
		
	}
	
	public void bind(){
		Source<IncomingConnection, CompletionStage<ServerBinding>> serverSource = Http.get(system).bind(ConnectHttp.toHost("0.0.0.0", bindingPort), materializer);
		System.out.println("server bound at " + bindingPort);
		
		@SuppressWarnings("serial")
		final Function<HttpRequest, HttpResponse> requestHandler =
			    new Function<HttpRequest, HttpResponse>() {
			      private final HttpResponse NOT_FOUND =
			        HttpResponse.create()
			          .withStatus(404)
			          .withEntity("Unknown resource!");


			      @Override
			      public HttpResponse apply(HttpRequest request) throws Exception {
			        Uri uri = request.getUri();
			        if (request.method() == HttpMethods.GET) {
			          if (uri.path().equals("/")) {
			            return HttpResponse.create().withEntity(ContentTypes.TEXT_HTML_UTF8,
			                  "<html><body>Hello world Get!</body></html>");
			          } else if (uri.path().equals("/hello")) {
			        	  String name = uri.query().get("name").orElse("Mister X");
			        	  return HttpResponse.create().withEntity("Hello " + name + "!");
			          } else if (uri.path().equals("/ping")) {
			        	  return HttpResponse.create().withEntity("PONG!");
			          } else {
			        	  return NOT_FOUND;
			          }
			        } else if(request.method() == HttpMethods.POST){
			        	if(uri.path().equals("/home")){
			        		return HttpResponse.create().withEntity(ContentTypes.TEXT_HTML_UTF8,"<html><body>Hello world Post!</body></html>");
			        	}else{
			        		return NOT_FOUND;
			        	}
			        }else{
			        	return NOT_FOUND;
			        }
			      }
			    };
		CompletionStage<ServerBinding> serverBindingFuture = serverSource
				.to(Sink.foreach(
						connection -> {
										System.out.println("Accepted new connection from " + connection.remoteAddress().getAddress());
										connection.handleWithSyncHandler(requestHandler, materializer);
									}
				  )
				)
				.run(materializer);
	}
	
}
