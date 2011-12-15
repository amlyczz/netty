//The MIT License
//
//Copyright (c) 2009 Carl Bystršm
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package org.jboss.netty.example.http.websocketx.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Copied from https://github.com/cgbystrom/netty-tools
 * 
 * A factory for creating WebSocket clients. The entry point for creating and connecting a client. Can and should be
 * used to create multiple instances.
 */
public class WebSocketClientFactory {

    private final NioClientSocketChannelFactory socketChannelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

    /**
     * Create a new WebSocket client
     * 
     * @param url
     *            URL to connect to.
     * @param version
     *            Web Socket version to support
     * @param callback
     *            Callback interface to receive events
     * @param customHeaders
     *            Map of custom headers to add to the client request
     * @return A WebSocket client. Call {@link WebSocketClient#connect()} to connect.
     */
    public WebSocketClient newClient(final URI url, final WebSocketVersion version, final WebSocketCallback callback,
            final Map<String, String> customHeaders) {
        ClientBootstrap bootstrap = new ClientBootstrap(socketChannelFactory);

        String protocol = url.getScheme();
        if (!protocol.equals("ws") && !protocol.equals("wss")) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        final WebSocketClientHandler clientHandler = new WebSocketClientHandler(bootstrap, url, version, callback,
                customHeaders);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();

                // If you wish to support HyBi V00, you need to use
                // WebSocketHttpResponseDecoder instead for
                // HttpResponseDecoder.
                pipeline.addLast("decoder", new HttpResponseDecoder());

                pipeline.addLast("encoder", new HttpRequestEncoder());
                pipeline.addLast("ws-handler", clientHandler);
                return pipeline;
            }
        });

        return clientHandler;
    }
}
