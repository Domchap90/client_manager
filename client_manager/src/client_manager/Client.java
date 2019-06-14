package client_manager;

//import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

public class Client /* implements Serializable */ {
	public Client(String name) {
		this.name = name;
		this.clientSessionLog = new TreeMap<Date, String>();
	}

	// private static final long serialVersionUID = 1L;
	private String name;
	private TreeMap<Date, String> clientSessionLog;

	public TreeMap<Date, String> getClientSessionLog() {
		return clientSessionLog;
	}

	public void setClientSessionLog(Date appointmentDateTime, String packProgress) {
		clientSessionLog.put(appointmentDateTime, packProgress);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWeeklyFreq() {
		return weeklyFreq;
	}

	public void setWeeklyFreq(int weeklyFreq) {
		this.weeklyFreq = weeklyFreq;
	}

	private int weeklyFreq;

}
