package lichenlzc.talking.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

public class Controller {

	private Selector selector;
	private int port;
	private boolean tcpIsStarted;
	private boolean udpIsStarted;
	Controller(int port) throws IOException{
		this.port=port;
		selector=Selector.open();
		this.tcpIsStarted=false;
		this.udpIsStarted=false;
	}
	
	public void startTCPService() throws IOException {
		if(!tcpIsStarted) {
			ServerSocketChannel ssc=ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.bind(new InetSocketAddress("localhost",port));
			SelectionKey tpsk=ssc.register(selector, SelectionKey.OP_ACCEPT);
			tpsk.attach(new AcceptHander(tpsk, Connector.getConnector()));
			tcpIsStarted=true;
			Log.writeDebugLog("register ServerSocket");
		}
	}
	
	public void startUDPService() throws IOException {
//		if(!udpIsStarted) {
//			DatagramChannel ssc=DatagramChannel.open();
//			ssc.configureBlocking(false);
//			ssc.bind(new InetSocketAddress("localhost",port));
//			ssc.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, new AcceptHander());
//			udpIsStarted=true;
//		}
	}
	
	public Runnable getRunnableTask() throws IOException, InterruptedException {
		if(tcpIsStarted || udpIsStarted) {
			return new Runnable() {
				@Override
				public void run() {
					try {
						Log.writeDebugLog("start Server");
						while(!Thread.interrupted()) {
							while(selector.select(50)==0) {
								Thread.sleep(50);
							}
							Set<SelectionKey> skset=selector.selectedKeys();
							for(SelectionKey sk:skset) {
								ChannelHandler ch=(ChannelHandler) sk.attachment();
								ch.handle();
							}
							skset.clear();
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}
		return null;
	}
}
