package lichenlzc.talking.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import lichenlzc.talking.client.Message;
import lichenlzc.talking.client.Message.SocketMessageTransfer;

public class ReadWriteHandler  implements ChannelHandler{

	private SelectionKey sk;
	//in、out相对于外界来说
	private BlockingQueue<Message> inQueue;
	private BlockingQueue<Message> outQueue;
	SocketMessageTransfer transfer;
	//private boolean isClosed;
	
	
	public ReadWriteHandler(SelectionKey sk,BlockingQueue<Message> inQueue,
			BlockingQueue<Message> outQueue){
		this.sk=sk;
		this.inQueue=inQueue;
		this.outQueue=outQueue;
		transfer= 
				new Message.SocketMessageTransfer((SocketChannel) sk.channel());
	}
	
	@Override
	public void handle() throws InterruptedException {
		try {
			if(sk.isReadable()) {
				read();
			}
			if(sk.isWritable()) {
				write();
			}
		} catch (IOException e) {
			e.printStackTrace();
			sk.cancel();
			Log.writeDebugLog("cancel "+sk.channel());
		}
	}
	
	Message rmess;
	private void read() throws IOException, InterruptedException{
		for(int i=0;i<5;i++) {
			if(rmess!=null) {
				if(inQueue.offer(rmess,50,TimeUnit.MILLISECONDS)) {
					rmess=null;
				}else {
					break;
				}
			}
			if(rmess==null){
				//SocketMessageTransfer transfer=(SocketMessageTransfer) sk.attachment();
				Message rmess=transfer.readMessageFromSocket();
				if(rmess!=null) {
					if(inQueue.offer(rmess,50,TimeUnit.MILLISECONDS))
						rmess=null;
				}
			}
		}
	}
	
	Message wmess;
	private void write() throws InterruptedException, IOException {
		for(int i=0;i<5;i++) {
			if(wmess==null) {
				wmess=outQueue.poll(50,TimeUnit.MILLISECONDS);
			}
			if(wmess!=null) {
				//SocketMessageTransfer transfer=(SocketMessageTransfer) sk.attachment();
				if(transfer.writeMessageToSocket(wmess)) {
					wmess=null;
				}
			}else {
				break;
			}
		}
	}

}
