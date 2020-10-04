package lichenlzc.talking.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lichenlzc.talking.client.Message.SocketMessageTransfer;
import lichenlzc.talking.server.Log;

public class IOClient {

	private SocketChannel socket;
	private Selector selector;
	private BlockingQueue<Message> inQueue;
	private BlockingQueue<Message> outQueue;
	private boolean isInitial=false;
	
	
	public IOClient(BlockingQueue<Message> inQueue, BlockingQueue<Message> outQueue) throws IOException{
		this.inQueue=inQueue;
		this.outQueue=outQueue;
		selector=Selector.open();
	}
	
	public void connectSocket(InetSocketAddress remote) throws IOException {
		socket=SocketChannel.open();
		socket.configureBlocking(false);
		socket.connect(remote);
		socket.register(selector, SelectionKey.OP_CONNECT);
		isInitial=true;
	}
	
	public Runnable getRunnableTask() {
		if(!isInitial)
			return null;
		return new Runnable() {

			@Override
			public void run() {
				try {
					Log.writeDebugLog("start client");
					while(!Thread.interrupted()) {
						while(selector.select(50)==0) {
							Thread.sleep(50);
						};
						Set<SelectionKey> set=selector.selectedKeys();
						for(SelectionKey key:set) {
							SocketChannel soc=(SocketChannel) key.channel();
							if(key.isConnectable()) {
								if(soc.finishConnect()) {
									key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
									key.attach(new Message.SocketMessageTransfer(soc));
								}
							}else {
								if(key.isReadable()) {
									IOClient.this.read(key);
								}
								if(key.isWritable()) {
									IOClient.this.send(key);
								}
							}
						}
						set.clear();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	//一次只能读最多5条消息
	Message rmess;
	private void read(SelectionKey sk) throws IOException, InterruptedException{
		for(int i=0;i<5;i++) {
			if(rmess!=null) {
				if(inQueue.offer(rmess,50,TimeUnit.MILLISECONDS)) {
					rmess=null;
				}else {
					break;
				}
			}
			if(rmess==null){
				SocketMessageTransfer transfer=(SocketMessageTransfer) sk.attachment();
				Message rmess=transfer.readMessageFromSocket();
				if(rmess!=null) {
					if(inQueue.offer(rmess,50,TimeUnit.MILLISECONDS))
						rmess=null;
				}
			}
		}
	}
	
	//一次只能写最多5条消息
	Message wmess;
	private void send(SelectionKey sk) throws InterruptedException, IOException {
		for(int i=0;i<5;i++) {
			if(wmess==null) {
				wmess=outQueue.poll(50,TimeUnit.MILLISECONDS);
			}
			if(wmess!=null) {
				SocketMessageTransfer transfer=(SocketMessageTransfer) sk.attachment();
				if(transfer.writeMessageToSocket(wmess)) {
					wmess=null;
				}
			}else {
				break;
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
//		BlockingQueue<Message> out=new ArrayBlockingQueue<Message>(20);
//		BlockingQueue<Message> in=new ArrayBlockingQueue<Message>(20);
//		IOClient ioc=new IOClient(in,out);
//		ioc.connectSocket(new InetSocketAddress("localhost",5998));
//		new Thread(ioc.getRunnableTask()).start();
//		while(true) {
//			Message mess=ioc.inQueue.take();
//			System.out.println(mess);
//		}
//		ConcurrentHashMap<Integer,Integer> map;
//		AtomicInteger ai;
//		HashMap<Integer,Integer> hash;
//		ArrayList<Integer> arr;
		System.out.println(Math.sqrt(4));
	
	}
}
