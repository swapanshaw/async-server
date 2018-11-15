package io.netty.example.http.helloworld;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHelloWorldServerHandler extends SimpleChannelInboundHandler<HttpObject> {
  public static final String REGEX = "=";
  private static final byte[] CONTENT = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
  private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
  private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
  private static final AsciiString CONNECTION = AsciiString.cached("Connection");
  private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest req = (FullHttpRequest) msg;

      String content = req.content().toString(CharsetUtil.UTF_8);
      System.out.println(req.method());
      System.out.println(content);
      boolean keepAlive = HttpUtil.isKeepAlive(req);
      FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content.getBytes()));
      response.headers().set(CONTENT_TYPE, "text/plain");
      response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

      if (!keepAlive) {
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
      } else {
        response.headers().set(CONNECTION, KEEP_ALIVE);
        ctx.write(response);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}