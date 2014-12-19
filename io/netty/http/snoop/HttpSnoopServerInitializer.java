package io.netty.http.snoop;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

public class HttpSnoopServerInitializer extends ChannelInitializer<SocketChannel>{
	private final SslContext sslCtx;

	public HttpSnoopServerInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline cp = ch.pipeline();
		if (sslCtx != null) {
			cp.addLast(sslCtx.newHandler(ch.alloc()));
		}
		cp.addLast(new HttpRequestDecoder());
		// Uncomment the following line if you don't want to handle HttpChunks.
		//p.addLast(new HttpObjectAggregator(1048576));
		cp.addLast(new HttpResponseEncoder());
		// Remove the following line if you don't want automatic content compression.
		//p.addLast(new HttpContentCompressor());
		cp.addLast(new HttpSnoopServerHandler());
	}

}
