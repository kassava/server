package ru.vka.develop.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Calendar;

import ru.vka.develop.session.SessionManager;

/**
 * Поток для обработка пришедшего пакета
 * @author ultra
 *
 */
class OnePacket {
	private DatagramSocket dts;
	byte[] buf;
	private DatagramPacket recvPacket;
	
	public OnePacket(DatagramSocket s, DatagramPacket dp) {
		recvPacket = dp;
		dts = s;
		buf = new byte[1024];
		start();
	}
	
	public void start(){
		if (dts.isClosed()) {
			return;
		}
		String str = new String(recvPacket.getData()).trim();
		
		if (str.indexOf("Client") != -1) {
			buf = "Hello".getBytes();
			System.out.println("Client: " + recvPacket.getAddress() + ":"
					+ recvPacket.getPort());
				
			DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, 
					recvPacket.getAddress(), recvPacket.getPort());
			sendPacket.setData(buf);
			sendPacket.setLength(buf.length);
			try {
				if (!dts.isClosed()) {
					dts.send(sendPacket);
				}
			} catch (IOException e) {
				System.out.println("IOException");
				e.printStackTrace();
			}	
		}
		
		SessionManager sm = SessionManager.getInstance();
		sm.setDatagramSocket(dts);		
		sm.addPacket(recvPacket);
	}
}

/**
 * Поток сервера
 * @author ultra
 *
 */
class Server extends Thread {
	static final int port = 19655;
	public boolean interrupted = false; 
	public Server() {}
	
	public void run() {
		DatagramSocket dts = null;
		try {
			dts = new DatagramSocket(port);
			System.out.println("Server started");
			while(true) {
				if (interrupted) {
					System.out.println("interrupted");
					return;
				}
				byte[] buf = new byte[1024];
				DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
				dts.receive(recvPacket);
//				new OnePacket(dts, recvPacket);
				if (dts.isClosed()) {
					return;
				}
				String str = new String(recvPacket.getData()).trim();
				
				if (str.indexOf("Client") != -1) {
					buf = "Hello".getBytes();
					System.out.println("Client: " + recvPacket.getAddress() + ":"
							+ recvPacket.getPort());
						
					DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, 
							recvPacket.getAddress(), recvPacket.getPort());
					sendPacket.setData(buf);
					sendPacket.setLength(buf.length);
					try {
						if (!dts.isClosed()) {
							dts.send(sendPacket);
						}
					} catch (IOException e) {
						System.out.println("IOException");
						e.printStackTrace();
					}	
				}
				
				SessionManager sm = SessionManager.getInstance();
				sm.setDatagramSocket(dts);		
				sm.addPacket(recvPacket);
				
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
//		finally {
//			if (dts != null) {
//				System.out.println("Закрытие сокета");
//				dts.close();
//			}
//		}
	}
}

/**
 * Главное поток приложения
 * @author ultra
 *
 */
public class ServerApplication { 
	private static String version = "0.0.4";
	private static byte buffer[] = new byte[1024];
	private static Server server = new Server();
	
	public static void main(String[] args) throws IOException {		
		int pos = 0;
		while (true) {
			int command = System.in.read();
			switch (command) {
			case -1:
				System.out.println("Сервер завершил работу!");
				break;
			case '\r':
				break;
			case '\n':
				pos = 0;
				executeCommand(new String (buffer).trim());
				buffer = new byte[1024];
				break;
			default:
				buffer[pos++] = (byte) command;
			}
		}
	}
	
	/**
	 * Обработка команд
	 * @param string
	 */
	private static void executeCommand(String string) {
		if (string.equals("start")) {
			if (server.interrupted) {
//				server = new Server();
				server.start();
				server.interrupted = false;
			} else {
				server.start();
			}
		}
		if (string.equals("stop")) {
			if (server != null) {
				server.interrupted = true;
				System.out.println("Стоп сервер.");
			}
		}
		if (string.equals("time")) {
			Calendar calendar = Calendar.getInstance();
	           int hour = calendar.get(Calendar.HOUR_OF_DAY);
	           int minute = calendar.get(Calendar.MINUTE);
	           int second = calendar.get(Calendar.SECOND);
	           String s = String.format("%02d:%02d:%02d", hour, minute, second);  
			System.out.println(s);
		}
		if (string.equals("version")) {
			System.out.println(version);
		}
	}
}
