//package dataserver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataServer {
	public static void main(String [] args) {
		System.out.println("Server start ...");
		try {
			ServerSocket server_socket = new ServerSocket(6578);
			System.out.println("CCC");
			while(true) {
				
				new Thread ( new ClientWorker(server_socket.accept())).start();
			}
			
		}catch (IOException ex) {
			Logger.getLogger(DataServer.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("DDD");
		}
		
	}
}

class ClientWorker implements Runnable{
	private Socket target_socket;
	private DataInputStream din;
	private DataOutputStream dout;
	public ClientWorker(Socket recv_socket) {
		try {
			target_socket = recv_socket;
			din = new DataInputStream(target_socket.getInputStream());
			dout = new DataOutputStream(target_socket.getOutputStream());
			System.out.println("BBB");
		}catch(IOException ex) {
			System.out.println("cAAA");
			Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);

		}
	}
	
	
	@Override
	public void run() {
		RandomAccessFile  rw = null;
		long current_file_pointer = 0;
		boolean loop_break = false;
		while(true) {
			byte[] initialize = new byte[1];
			try {
				din.read(initialize, 0, initialize.length);

				if(initialize[0] == 2) {
					byte[] cmd_buff = new byte[3];
					din.read(cmd_buff, 0, cmd_buff.length);
					byte[] recv_data = ReadStream();
					System.out.println("DDD1");
					switch(Integer.parseInt(new String(cmd_buff))) {
					case 124:
						System.out.println("EEE1");
						rw = new RandomAccessFile("/Users/taipham/Downloads/" + new String(recv_data), "rw");
						dout.write(CreateDataPacket("125".getBytes("UTF8"), String.valueOf(current_file_pointer).getBytes("UTF8")));
						dout.flush();
						break;
					case 126:
						System.out.println("FFF1");
						rw.seek(current_file_pointer);
						rw.write(recv_data);
						
						current_file_pointer = rw.getFilePointer();

						System.out.println("Download percentage : " + ((float)current_file_pointer/rw.length())*100 + "%");
						dout.write(CreateDataPacket("125".getBytes("UTF8"), String.valueOf(current_file_pointer).getBytes("UTF8")));
						dout.flush();
						break;
					case 127:
						System.out.println("GGG1");
						if("Close".equals(new String(recv_data))) {
							loop_break = true;
						}
						break; // Get out of while loop
					}
				}
				if(loop_break == true) {
					target_socket.close();
					break;
				}
			}catch(IOException ex) {
				Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);

			}
	
		}
	}
	// Read and display data
	private byte[] ReadStream() {
		byte[] data_buff = null;
		try {
			int b = 0;
			String buffer_length = "";
			while((b = din.read()) != 4) {
				buffer_length += (char)b;
			}
			int data_length = Integer.parseInt(buffer_length);
			data_buff = new byte[data_length];
			int byte_read = 0;
			int byte_offset = 0;
			while(byte_offset < data_length) {
			
				byte_read = din.read(data_buff, byte_offset, data_length - byte_offset);
				byte_offset += byte_read;
			}
			
		}catch(IOException ex) {
			Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
		}
		return data_buff;
	}
	
	private byte[] CreateDataPacket(byte[] cmd, byte[] data) {
		byte[] packet = null;
		try {
			byte[] initialize = new byte[1];
			initialize[0] = 2;
			byte[] separator = new byte[1];
			separator[0] = 4;
			byte[] data_length = String.valueOf(data.length).getBytes("UTF8");
			packet = new byte[initialize.length + cmd.length + separator.length + data_length.length + data.length];
			
			// Copy data
			System.arraycopy(initialize, 0, packet, 0, initialize.length);
			System.arraycopy(cmd, 0, packet, initialize.length, cmd.length);
			System.arraycopy(data_length, 0, packet, initialize.length + cmd.length, data_length.length);
			System.arraycopy(separator, 0, packet, initialize.length + cmd.length + data_length.length, separator.length);
			System.arraycopy(data, 0, packet, initialize.length + cmd.length + data_length.length+separator.length, data.length);
			
		}catch(UnsupportedEncodingException ex) {
			Logger.getLogger(ClientWorker.class.getName()).log(Level.SEVERE, null, ex);
		}
		return packet;
	}

	
}
