package io.netty.http.snoop;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import spms.vo.Member;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object>{

	private HttpRequest request;
	/* Buffer that stores the response content */
	private final StringBuilder sbbuf = new StringBuilder();


	@Override
	public void channelReadComplete(ChannelHandlerContext ctx){
		ctx.flush();
	}

	//event handler method here. This method is called with the received message, whenever
	//new data is received from a client. In this example, the type of the received message
	//is ByteBuf 
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {


		sbbuf.setLength(0);
		//Method Get
		if (msg instanceof HttpRequest) {
			HttpRequest request = this.request = (HttpRequest)msg;

			//			System.out.println("Get method:"+request);
			if (HttpHeaders.is100ContinueExpected(request)){
				send100Continue(ctx);
			}
		}

		//Method POST
		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;

			//no use
			ByteBuf content = httpContent.content();
			if (content.isReadable()) {
				//				sbbuf.append("CONTENT: ");
				//post 방식 값을 출력 
				System.out.println("content.toString:"+content.toString(CharsetUtil.UTF_8));
				sbbuf.append(content.toString(CharsetUtil.UTF_8));
				//				System.out.println("content.toString :" +content.toString(CharsetUtil.UTF_8));
				//				sbbuf.append("\r\n");
				appendDecoderResult(sbbuf, request);
			}

			if (msg instanceof LastHttpContent){
				LastHttpContent trailer = (LastHttpContent) msg;
				if(!trailer.trailingHeaders().isEmpty()) {
					sbbuf.append("\r\n");
					for (String name: trailer.trailingHeaders().names()) {
						for (String value: trailer.trailingHeaders().getAll(name)){
							sbbuf.append("TRAILING HEADER: ");
							sbbuf.append(name).append(" = ").append(value).append("\r\n");
						}
					}
					sbbuf.append("\r\n");
				}

				if (!writeResponse(trailer, ctx)) {
					// If keep-alive is off, close the connection once the content is fully written.
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
	}


	protected void resultSet(){

		HashMap<String, String> map = new HashMap<String, String>();

		String uriPath = request.getUri();
		System.out.println("uri: "+uriPath);

		String bufPath = "seungjun.do?"+sbbuf.toString(); 
		System.out.println("sbbuf.toString() :" + bufPath);
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(bufPath);
		Map<String, List<String>> params = queryStringDecoder.parameters();
		if (!params.isEmpty()) {
			for(Entry<String, List<String>> p: params.entrySet()) {
				String key = p.getKey();
				List<String> vals = p.getValue();
				for (String val : vals) {
					System.out.println("PARAM: ["+key+"] = ["+val+"]\r\n");
					map.put(key, val);
				}
			}
		}
		Member member = new Member() ;
		String no = map.get("no");
		String name = map.get("name");
		String email = map.get("email");
		String password = map.get("password");
		String result =null; 
		//dao에 명령 
		try {
			switch(uriPath)
			{
			case "/list.do":
				sbbuf.append("{\"list\":[");
				int i = 0; 
				for(Member m: HttpSnoopServer.memberDao.selectList())
				{

					sbbuf.append("["+m.getNo()+",");
					sbbuf.append("\""+m.getName()+"\"],");
				}
				sbbuf.append("]}");
				String tmp = sbbuf.toString();
				tmp = tmp.replaceAll("],]", "]]");
				sbbuf.setLength(0);
				sbbuf.append(tmp);
				break;
			case "/update.do":

				Integer uno = Integer.parseInt(no); 
				member.setNo(uno);
				member.setName(name);
				member.setEmail(email); 

				HttpSnoopServer.memberDao.update(member);
				if(member==null){
					sbbuf.append(" ==>cannot find the user");
				}
				else{
					sbbuf.append("\r\n");
					sbbuf.append("{\"update\":{\"no\":").append(member.getNo()).append(",");
					sbbuf.append("\"name\":\"").append(member.getName()).append("\",");
					sbbuf.append("\"email\":\"").append(member.getEmail()).append("\"}}");
				}
				break; 

			case "/selectOne.do":
				String sno = map.get("no");
				Integer ino = Integer.parseInt(sno);
				System.out.println("Integer MNO:"+ino);
				sbbuf.append("\r\n");
				member = HttpSnoopServer.memberDao.selectOne(ino);
				if(member==null){
					sbbuf.append(" ==>cannot find the user");
				}
				else{
					sbbuf.append("\r\n");	
					sbbuf.append("{\"selectOne\":{\"no\":").append(member.getNo()).append(",");
					sbbuf.append("\"name\":\"").append(member.getName()).append("\",");
					sbbuf.append("\"email\":\"").append(member.getEmail()).append("\"}}");
					System.out.println(sbbuf.toString());
					sbbuf.append(" ==>success");
				}
				break; 

			case "/add.do":
				member.setName(name); 
				member.setPassword(password); 
				member.setEmail(email);
				HttpSnoopServer.memberDao.insert(member); 

				if(member==null){
					
					sbbuf.append(" ==>cannot find the user");
				}
				else{
					sbbuf.append("\r\n");
					sbbuf.append("{\"add\":{\"no\":").append(member.getNo()).append(",");
					sbbuf.append("\"name\":\"").append(member.getName()).append("\",");
					sbbuf.append("\"email\":\"").append(member.getEmail()).append("\"}}");
				}
			}
		} catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.getDecoderResult();
		if (result.isSuccess()) {
			return;
		}

		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		// Decide whether to close the connection or not.
		boolean keepAlive = HttpHeaders.isKeepAlive(request);
		resultSet(); 

		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(
				HTTP_1_1, currentObj.getDecoderResult().isSuccess()? OK : BAD_REQUEST,
						Unpooled.copiedBuffer(sbbuf.toString(), CharsetUtil.UTF_8));

		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive){
			// Add 'Content-Length' header only for a keep-alive connection.
			response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
			// Add keep alive header as per:
			// - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		// Encode the cookie.
		String cookieString = request.headers().get(COOKIE);
		if (cookieString != null){
			Set<Cookie> cookies = CookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()){
				// Reset the cookies if necessary.
				for (Cookie cookie: cookies){
					response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
				}
			}
		} else {
			// Browser sent no cookie.  Add some.
			response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"));
			response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"));
		}

		// Write the response.
		ctx.write(response);

		return keepAlive;
	}

	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
		ctx.write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
