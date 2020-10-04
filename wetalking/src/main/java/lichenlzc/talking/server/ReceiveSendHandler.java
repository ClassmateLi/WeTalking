package lichenlzc.talking.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReceiveSendHandler  implements ChannelHandler{

	SocketAddress sdd;
	@Override
	public void handle() {
//		ByteBuffer rbb=ByteBuffer.allocate(200);
//		ByteBuffer rwbb=ByteBuffer.wrap("Hellow, I am Server!\n".getBytes());
//		DatagramChannel dc=(DatagramChannel) sk.channel();
//		try {
//			if(sk.isReadable()) {
//				sdd=dc.receive(rbb);
//				for(byte b:rbb.array()) {
//					System.out.print(b+" ");
//				}
//				System.out.println();
//			}
//			if(sk.isWritable() && sdd!=null) {
//				dc.send(rwbb, sdd);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
