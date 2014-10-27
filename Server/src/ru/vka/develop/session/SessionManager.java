package ru.vka.develop.session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


/**
 * Класс для управления сессиями
 * 
 * @author ultra
 * 
 */
public class SessionManager {
	private ArrayList<Session> showSessions = new ArrayList<Session>();
	private ArrayList<Session> viewSessions = new ArrayList<Session>();
	private DatagramPacket dtPacket;
	private DatagramSocket dtSocket;

	// Удалаяет публичный конструктор по умолчанию
	private SessionManager() {}

	// SessionManager применяет патерн "одиночка"
	private static volatile SessionManager instance = null;

	/**
	 * Возвращает ссылку на {@link SessionManager}.
	 * 
	 * @return ссылка на {@link SessionManager}
	 */
	public final static SessionManager getInstance() {
		if (instance == null) {
			synchronized (SessionManager.class) {
				if (instance == null) {
					SessionManager.instance = new SessionManager();
				}
			}
		}
		return instance;
	}

	/**
	 * Добавление пакета в менеджер
	 */
	public void addPacket(DatagramPacket packet) {
		dtPacket = packet;

		handle();
	}
	
	/**
	 * Назначение используемого сокета
	 * @param dts
	 */
	public void setDatagramSocket(DatagramSocket dts) {
		this.dtSocket = dts;
	}

	/**
	 * Определение судьбы пакета)
	 */
	private synchronized void handle() {
		InetAddress addr = dtPacket.getAddress();
		Session session;
		String packetData = new String(dtPacket.getData()).trim();
		
		System.out.println("packet: " + new String(dtPacket.getData()));
		
		if (packetData.equals("show")) {
			try {
				boolean flag = false;
				for (Session s : showSessions) {
					if (s.getDestinationIP().toString().equals(dtPacket.getAddress().toString())) {
						flag = true;
					}
				}
				if (!flag) {
					session = new Session(InetAddress.getLocalHost(), addr, 1);
					session.setDatagramSocket(dtSocket);
					showSessions.add(session);
				} else {
					System.out.println("Такая сессия уже зарегана");
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		if (packetData.equals("view")) {
			try {
				boolean flag = false;
				for (Session s : viewSessions) {
					if (s.getDestinationIP().toString().equals(dtPacket.getAddress().toString())) {
						flag = true;
					}
				}
				if (!flag) {
					session = new Session(InetAddress.getLocalHost(), addr, 0);
					session.setDatagramSocket(dtSocket);
					viewSessions.add(session);
				} else {
					System.out.println("Такая сессия уже зарегана");
				}
				byte[] buf = null;
				String str = "{\"translations\":[\"";
				
				// Отправка списка "показывающих" сессий
				for (Session s : showSessions) {
					str += s.getDestinationIP();
					str += "\",\"";
				}
				str += "end\"]}";
				buf = str.getBytes();
				
				System.out.println(str);
				
				DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, 
						addr, dtPacket.getPort());
				sendPacket.setData(buf);
				sendPacket.setLength(buf.length);
				buf = null;
				try {
					dtSocket.send(sendPacket);
				} catch (IOException e) {
					System.out.println("IOException");
					e.printStackTrace();
				} 
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			
			// ...
		}
		Session ss = null;
		if (packetData.indexOf(".") != -1) {
			for (Session s : showSessions) {
				System.out.println("showSessions: " + showSessions.size());
				System.out.println("s: " + s.getDestinationIP().toString());
				System.out.println("packet: " + packetData);
				if (s.getDestinationIP().toString().equals(packetData)) {
					ss = s;
					System.out.println("ss: " + ss.getDestinationIP());
				}
			}
			for (Session s : viewSessions) {
				if (s.getDestinationIP().equals(dtPacket.getAddress())) {
					System.out.println("showSessions: " + showSessions.size());
					System.out.println("s: " + s.getDestinationIP().toString());
					System.out.println("packet: " + packetData);
					ss.addReceiver(s);
				}
			}
		}
		
		if (packetData.indexOf("frame") != -1) {
			for (Session s : showSessions) {
				if (s.getDestinationIP().equals(addr)) {
					System.out.println("before take packet: " + new String(dtPacket.getData()).trim());
					s.takePacket(dtPacket);
				}
			}
		}
		
		if (packetData.indexOf("buy") != -1) {
			for (Session s : showSessions) {
				if (s.getDestinationIP().equals(addr)) {
					System.out.println("before take packet: " + new String(dtPacket.getData()).trim());
					s.takePacket(dtPacket);
					showSessions.remove(s);
					break;
				}
			}
			for (Session s : viewSessions) {
				if (s.getDestinationIP().equals(addr)) {
					viewSessions.remove(s);
					
					for (Session ses : showSessions) {
						ses.delReceiver(s);
					}
					break;
				}
			}
		}
	}
}
