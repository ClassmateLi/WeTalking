package lichenlzc.talking.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;

import lichenlzc.talking.client.Message;
import java.nio.charset.*;
/**
 * 主要实现连接两个用户，实现两个用户相互发送消息
 * 将一个用户发来的消息放到相应用户的输出队列中
 * @author liche
 *
 */
class Connector {

	BlockingQueue<Message> out=new ArrayBlockingQueue<Message>(20);
	BlockingQueue<Message> in=new ArrayBlockingQueue<Message>(20); 
	
	//相对于套接字，返回输出流
	public BlockingQueue<Message> getOutQueueFor(SocketChannel socket) {
		return out;
	}
	
	public BlockingQueue<Message> getInQueueFor(SocketChannel socket) {
		return in;
	}
	
	static Connector connector;
	static public Connector getConnector() {
		 if(connector==null)
			 connector=new Connector();
		return connector;
	}
	static public void main(String[] args) throws IOException, InterruptedException {
		Scanner sca=new Scanner(System.in);
		ExecutorService es=Executors.newCachedThreadPool();
		Controller con=new Controller(5998);
		con.startTCPService();
		es.execute(con.getRunnableTask());
		while(sca.hasNextLine()) {
			Message mess=new Message('S', 
					sca.nextLine().getBytes(Charset.forName("utf-8")));
			connector.out.put(mess);
			Log.writeDebugLog("put message into outQueue: "+mess);
		}
		
	}
}
