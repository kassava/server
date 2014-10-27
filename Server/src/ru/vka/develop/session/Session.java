package ru.vka.develop.session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * ���� ����� �������� ������.
 * �� ������������ ��������� ������ ����� �������� � ��������.
 * � ���� ������ ����� ��������� ������ "����".
 * @author ultra
 *
 */
public class Session extends Thread {
	public static final String TAG = "session";
	
	private InetAddress destinationIP; // ����� �������
	private InetAddress serverIP; // ����� �������
	private DatagramSocket dtSocket; // �����
	private long timeStamp; // ��������� �����
	private int type; // ��� ������ (0 - �����, 1 - ��������)
	private ArrayList<DatagramPacket> buffer; // ����� ��� ����������� �������
	private ArrayList<Session> receiverList; // ������ �����������
	private long id;
	
	/**
	 * ������� ������, � ������� ����� ��������� �����.
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
	 * ������� ������, � ������� ����� ��������� �����.
	 * @param serverIP ����� �������
	 * @param destinationIP ����� �������
	 */
	public Session(InetAddress serverIP, InetAddress destinationIP, int type) {
		buffer = new ArrayList<DatagramPacket>();
		receiverList = new ArrayList<Session>();
		long uptime = System.currentTimeMillis();
		this.serverIP = serverIP;
		this.destinationIP = destinationIP;
		this.type = type;
		this.id = System.currentTimeMillis();
		timeStamp = (uptime/1000)<<32 & (((uptime-((uptime/1000)*1000))>>32)/1000); // NTP timestamp ��
		System.out.println(TAG + ": ��������� ������: " + destinationIP + ", ��� " + type + ", time: " 
				+ timeStamp);
	}
	
	/** 
	 * ����� �������.
	 * @param serverIP
	 */
	public void setServerIP(InetAddress serverIP) {
		this.serverIP = serverIP;
	}
	
	/**
	 * ����� �������.
	 * @param destinationIP
	 */
	public void setDestinationIP(InetAddress destinationIP) {
		this.destinationIP = destinationIP;
	}
	
	/**
	 * ��� ������.
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * �����, ������������ ��� �������� �������
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
	 * ��������� ����� � ������.
	 * @param packet �������� �����
	 */
	public void takePacket(DatagramPacket packet) {
		System.out.println("������: " + packet.getAddress() 
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
	 * �������� ������ �������������� �����������.
	 * @param packet ����� ��� �������� 
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
	 * ���������� ���������� � ������
	 * @param s
	 */
	public void addReceiver (Session s) {
		receiverList.add(s);
		System.out.println("receiverList: " + receiverList.size());
	}
	
	/**
	 * �������� ���������� �� ������
	 * @param s
	 */
	public void delReceiver (Session s) {
		receiverList.remove(s);
		System.out.println("receiverList after del session: "
				+ receiverList.size());
	}
}
