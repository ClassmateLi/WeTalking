package lichenlzc.talking.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

import lichenlzc.talking.server.Log;

public class Message {
	private static final char STRING_TYPE='S';
	
	private HeadMessage head;
	private byte[] body;
	
	public Message(char type, byte[] body){
		this.head=new HeadMessage();
		head.type=type;
		head.lengthContent=body.length;
		this.body=body;
	}
	
	private Message(){}
	
	public char getType() {
		return head.type;
	}
	
	public int getLength() {
		return head.lengthContent;
	}
	
	public byte[] getBody() {
		return body;
	}
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("{Type: ");
		sb.append(head.type);
		sb.append(",Length: ");
		sb.append(head.lengthContent);
		sb.append(",Content: ");
		sb.append(new String(body,Charset.forName("utf-8")));
		sb.append("}");
		return sb.toString();
	}
	
	//头部是定长的(),因此作为一个独立类实现
	static private class HeadMessage implements Serializable{
		
		static private final int LENGTH_HEAD=11; 
		private char type;
		private int lengthContent;
		
		public static HeadMessage getHeadFrom(byte[] bs) throws IOException{
			String str=new String(bs, Charset.forName("utf-8"));
			if(str.length()!=11 || !str.matches("\\w\\d{10}")) {
				throw new IOException("byte[] 格式不正确");
			}
			HeadMessage head=new HeadMessage();
			head.type=str.charAt(0);
			head.lengthContent=Integer.parseInt(str, 1, 11, 10);
			return head;
		}
		
		public byte[] toByteArray() throws IOException{
			StringBuilder sb=new StringBuilder();
			sb.append(this.type);
			sb.append(this.lengthContent);
			if(sb.length()>11) {
				throw new IOException("length 过大");
			}
			for(int i=11-sb.length();i>0;i--) {
				sb.insert(1, '0');
			}
			byte[] bs=sb.toString().getBytes(Charset.forName("utf-8"));
			return bs;
		}
	}
	
	//能够实现非阻塞Socket和Message的转化
	public static class SocketMessageTransfer{
		SocketChannel socket;
		public SocketMessageTransfer(SocketChannel socket){
			this.socket=socket;
		}
		
		private ByteBuffer headbuff=ByteBuffer.allocate(HeadMessage.LENGTH_HEAD);
		private ByteBuffer contentbuff;
		private Message rmess;
		public Message readMessageFromSocket() throws IOException{
			if(headbuff.hasRemaining()) {
				socket.read(headbuff);
				if(!headbuff.hasRemaining()) {
					rmess=new Message();
					rmess.head=HeadMessage.getHeadFrom(headbuff.array());
					contentbuff=ByteBuffer.allocate(rmess.head.lengthContent);
				}
			}
			if(!headbuff.hasRemaining() && contentbuff.hasRemaining()){
				socket.read(contentbuff);
			}
			if(!headbuff.hasRemaining() && !contentbuff.hasRemaining()){
				//读完了一个message
				rmess.body=contentbuff.array();
				contentbuff=null;
				headbuff.clear();
				Log.writeDebugLog("read a message: "+rmess);
				return rmess;
			}
			return null;
		} 
		
		private ByteBuffer wheadbuff=ByteBuffer.allocate(0);
		private ByteBuffer wcontentbuff=ByteBuffer.allocate(0);
		//如果mess!=null且接受了该mess的写则返回true
		public boolean writeMessageToSocket(Message mess) throws IOException {
			if(wheadbuff.hasRemaining()) {
				socket.write(wheadbuff);
			}
			if(!wheadbuff.hasRemaining()
					&& wcontentbuff.hasRemaining()) {
				socket.write(wcontentbuff);
			}
			if(mess!=null &&  !wcontentbuff.hasRemaining()){
				Log.writeDebugLog("start write a message: "+mess);
				wheadbuff=ByteBuffer.wrap(mess.head.toByteArray());
				wcontentbuff=ByteBuffer.wrap(mess.body);
				writeMessageToSocket(null);
				return true;
			}
			
			return false;
		}
		
	}
	
	static public void main(String[] args) throws IOException, InterruptedException {
		SocketChannel soc=SocketChannel.open();
		soc.configureBlocking(false);
		soc.connect(new InetSocketAddress("localhost",5998));
		while(!soc.finishConnect()) {
			;
		}
		Message.SocketMessageTransfer tran=new Message.SocketMessageTransfer(soc);
		Message mess=null;
		while(true) {
			while((mess=tran.readMessageFromSocket())!=null)
				System.out.println(mess);
		}
	}
	
}


