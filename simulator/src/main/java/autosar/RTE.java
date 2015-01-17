package autosar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ecm.Ecm;

import messages.InstallAckMessage;
import messages.InstallMessage;
import messages.LoadMessage;
import messages.Message;
import messages.MessageType;
import messages.PluginMessage;
import messages.PublishMessage;
import messages.PublishPacket;
import messages.UninstallAckMessage;
import messages.UninstallMessage;

//import java.util.LinkedList;
//import java.util.List;
//import configs.CarModel;
//import port.SWCPPort;

public class RTE {
	private Ecm ecm;
	
	// ECU ID - SWC with PIRTE
	private HashMap<Integer, SWC> pirteSWCs = new HashMap<Integer, SWC>();
	
	// links between SWC
	private HashMap<Integer, Integer> links = new HashMap<Integer, Integer>();
	
	private HashMap<Integer, SWCPPort> pports = new HashMap<Integer, SWCPPort>();
	private HashMap<Integer, SWCRPort> rports = new HashMap<Integer, SWCRPort>();
	
//	private CarModel carModel;
//	private List<SWCRPort> rports = new LinkedList<SWCRPort>();
//	private List<SWCPPort> pports = new LinkedList<SWCPPort>();
	private static RTE instance;
	
	private RTE() {
	}

	public static synchronized RTE getInstance() {
		if(instance == null) {
			instance  = new RTE();
		}
		
		return instance;
	}
	
	public void addPPort(SWCPPort port) {
		pports.put(port.getId(), port);
	}
	
	public void addRPort(SWCRPort port) {
		rports.put(port.getId(), port);
	}
	
	public void addLink(int from, int to) {
		links.put(from, to);
	}
	
	public void addPirteSWC(int ecuId, SWC swc) {
		pirteSWCs.put(ecuId, swc);
	}
	
	public void addRteMessage(Message message) {
		int messageType = message.getMessageType();
		switch(messageType) {
		case MessageType.INSTALL:
			InstallMessage installMessage = (InstallMessage) message;
			int remoteEcuId = installMessage.getRemoteEcuId();
			SWC swc = pirteSWCs.get(remoteEcuId);
			swc.getPirte().addMessage(message);
			break;
		case MessageType.UNINSTALL:
			UninstallMessage uninstallMessage = (UninstallMessage) message;
			int remoteEcuId4Uninstall = uninstallMessage.getRemoteEcuId();
			SWC swc4Uninstall = pirteSWCs.get(remoteEcuId4Uninstall);
			swc4Uninstall.getPirte().addMessage(message);
			break;
		case MessageType.INSTALL_ACK:
			InstallAckMessage installAckMessage = (InstallAckMessage) message;
			ecm.process(installAckMessage);
			break;
		case MessageType.UNINSTALL_ACK:
			UninstallAckMessage uninstallAckMessage = (UninstallAckMessage) message;
			ecm.process(uninstallAckMessage);
			break;
		case MessageType.LOAD:
			LoadMessage loadMessage = (LoadMessage) message;
			int remoteEcuId4Load = loadMessage.getRemoteEcuId();
			SWC swc4Load = pirteSWCs.get(remoteEcuId4Load);
			swc4Load.getPirte().addMessage(message);
			break;
		case MessageType.PUBLISH:
			PublishMessage publishMessage = (PublishMessage) message;
			ecm.process(publishMessage);
			break;
		case MessageType.PLUGIN_MESSAGE:
			// TODO: temporary solution for car communication
			SWC swc4Subscribe = pirteSWCs.get(2);
			swc4Subscribe.getPirte().addMessage(message);
		default:
			System.out.println("Append other handlers.................");
		}
		
	}
	
	
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}
}
