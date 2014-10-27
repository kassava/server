package ru.vka.develop.session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Этот класс описания сессий.
 * Он представляет потоковую сессию между клиентом и сервером.
 * В этом классе поток обозначен словом "трек".
 * @author ultra
 *
 */
public class Session extends Thread {
	public static final String TAG = "session";
	
	private InetAddress destinationIP; // адрес клиента
	private InetAddress serverIP; // адрес сервера
	private DatagramSocket dtSocket; // сокет
	private long timeStamp; // временная метка
	private int type; // тип сессии (0 - прием, 1 - передача)
	private ArrayList<DatagramPacket> buffer; // буфер для принимаемых пакетов
	private ArrayList<Session> receiverList; // список получателей
	private long id;
	
	/**
	 * Создает сессию, в которую можно добавлять треки.
	 */
	public Session() {
		this(null, null, -1);
		try {
			serverIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			serverIP = null;
		}
	}
	/**
	 * Создает сессию, в которую можно добавлять треки.
	 * @param serverIP адрес сервера
	 * @param destinationIP адрес клиента
	 */
	public Session(InetAddress serverIP, InetAddress destinationIP, int type) {
		buffer = new ArrayList<DatagramPacket>();
		receiverList = new ArrayList<Session>();
		long uptime = System.currentTimeMillis();
		this.serverIP = serverIP;
		this.destinationIP = destinationIP;
		this.type = type;
		this.id = System.currentTimeMillis();
		timeStamp = (uptime/1000)<<32 & (((uptime-((uptime/1000)*1000))>>32)/1000); // NTP timestamp хз
		System.out.println(TAG + ": добавлена сессия: " + destinationIP + ", тип " + type + ", time: " 
				+ timeStamp);
	}
	
	/** 
	 * Адрес сервера.
	 * @param serverIP
	 */
	public void setServerIP(InetAddress serverIP) {
		this.serverIP = serverIP;
	}
	
	/**
	 * Адрес клиента.
	 * @param destinationIP
	 */
	public void setDestinationIP(InetAddress destinationIP) {
		this.destinationIP = destinationIP;
	}
	
	/**
	 * Тип сессии.
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * Сокет, используемый для отправки пакетов
	 * @param dts
	 */
	public void setDatagramSocket(DatagramSocket dts) {
		this.dtSocket = dts;
	}
	
	public InetAddress getServerIP() {
		return serverIP;
	}
	
	public InetAddress getDestinationIP() {
		return destinationIP;
	}
	
	public int getType() {
		return type;
	}
	
	public DatagramSocket getDatagramSocket() {
		return dtSocket;
	}
	
	public long getID() {
		return id;
	}
	
	public void run() {
		sendPacket(buffer.get(0));
	}
	
	/**
	 * Принимает пакет в сессию.
	 * @param packet Принятый пакет
	 */
	public void takePacket(DatagramPacket packet) {
		System.out.println("сессия: " + packet.getAddress() 
				+ ", " + new String(packet.getData()).trim());
//		if (buffer.size() < 50) {
//			buffer.add(packet);
//		} else {
//			buffer.remove(0);
//			buffer.add(packet);
//		}
		buffer.add(packet);
//		sendPacket(buffer.get(0)); 
		if (!this.isAlive()) {
			start();
		}
	}
	
	/**
	 * Отправка пакета подключившимся получателям.
	 * @param packet Пакет для отправки 
	 */
	private void sendPacket(DatagramPacket packet) {
		System.out.println("sendPacket: " + receiverList.size());
		for (Session s : receiverList) {
			packet.setAddress(s.getDestinationIP());
			System.out.println("sendPacket " + s.getDestinationIP());
			System.out.println("sendPacket " + new String(packet.getData()).trim());
			try {
				if (!dtSocket.isClosed()) {
					dtSocket.send(packet);
				}
			} catch (IOException e) {
				System.out.println("IOException");
				e.printStackTrace();
			}
		}
		buffer.remove(0);
	}
	
	/**
	 * Добавление получателя в список
	 * @param s
	 */
	public void addReceiver (Session s) {
		receiverList.add(s);
		System.out.println("receiverList: " + receiverList.size());
	}
	
	/**
	 * Удаление получателя из списка
	 * @param s
	 */
	public void delReceiver (Session s) {
		receiverList.remove(s);
		System.out.println("receiverList after del session: "
				+ receiverList.size());
	}
}
