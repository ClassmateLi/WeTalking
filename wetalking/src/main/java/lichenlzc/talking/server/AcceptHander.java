package lichenlzc.talking.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHander implements ChannelHandler{
	
	private SelectionKey sk;
	private Selector sel;
	Connector connector;
	public AcceptHander(SelectionKey sk, Connector connector) {
		this.sk=sk;
		sel=sk.selector();
		this.connector=connector;
	}
	
	@Override
	public void handle() {
		ServerSocketChannel ssc=(ServerSocketChannel) sk.channel();
		try {
			SocketChannel soc=ssc.accept();
			soc.configureBlocking(false);
			if(soc!=null) {
				SelectionKey tpsk=soc.register(sel, 
						SelectionKey.OP_READ| SelectionKey.OP_WRITE);
				tpsk.attach(new ReadWriteHandler(tpsk,
								connector.getInQueueFor(soc),
								connector.getOutQueueFor(soc)));
				Log.writeDebugLog("register socket:"+soc);
			}
		} catch (IOException e) {
			e.printStackTrace();
			sk.cancel();
			Log.writeDebugLog("cancel "+sk.channel());
		}
	}
	
	
}
