package lichenlzc.talking.server;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface ChannelHandler {
	public void handle() throws InterruptedException;
}
