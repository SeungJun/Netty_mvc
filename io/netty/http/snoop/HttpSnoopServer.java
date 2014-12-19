package io.netty.http.snoop;

import spms.dao.SqlMemberDao;
import spms.util.DBConnectionPool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public final class HttpSnoopServer {
	static SqlMemberDao memberDao;
	static final boolean SSL = System.getProperty("ssl") != null;
	static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
	
	public static void main(String[] args) throws Exception {
		//DB연결 
		DBConnectionPool connPool = new DBConnectionPool(
				"com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"jdbc:sqlserver://127.0.0.1:1433;databaseName=testdb1",
				"sa", "admin123");
		
		//연결주입후 DAO호출 
		memberDao = new SqlMemberDao(connPool);
		
		// Configure SSL.
		final SslContext sslCtx;
		if (SSL){
			//Generates a temporary self-signed certificate for testing purposes
			//암호화 모듈을 생성하여 sslcontext 에 탑재 
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		} else {
			sslCtx = null;
		}

		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap sbs = new ServerBootstrap();
			sbs.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new HttpSnoopServerInitializer(sslCtx));

			Channel ch = sbs.bind(PORT).sync().channel();

			System.err.println("Open your web browser and navigate to " +
					(SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

			ch.closeFuture().sync();

		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
