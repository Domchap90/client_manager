package client_manager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NumOutOfRangeException extends Exception {
	private static final long serialVersionUID = 1L;

	public NumOutOfRangeException(String message) {
		super(message);
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}

public class ClientManagerRunner {
	static final Scanner scanMain = new Scanner(System.in);

	public static void main(String[] args) throws IOException, SQLException {
		// TODO Capitalize first letter of each name (client).
		// TODO Adjust queryDb and updateDB methods to account for different SQL scripts
		// -tidy up code.
		// TODO Find out whether the client's session is paid for or not. (Potential
		// option to send reminder)
		// TODO Eventually calculate total sessions per month -> thereby monthly
		// Revenue.
		int optionMenu = 0;
		String name = null;
		Map<String, Client> clientMap = new HashMap<String, Client>();// <clientName, Client object>
		String clientName = null;

		final Connection myCon = estConnection();


		do {
			System.out.println(
					"What would you like to do?\n" + "(Type 1) Enter an appointment for a new client.\n"
							+ "(Type 2)  Delete an appointment for a particular client.\n"
							+ "(Type 3)  View a client's session History.\n"
							+ "(Type 4)  View the current week's appointments.\n"
							+ "(Type 5)  View the current month's appointments.\n"
							+ "(Type 6)  View a particular month's appointment.\n"
							+ "(Type 7) View appointments between two dates.\n" + "(Type 8) Quit program.");

			optionMenu = numChecker(optionMenu, 1, 8,
					" is not an option. Please enter a valid number between 1 and 8.");

			scanMain.nextLine(); // consume new line character. Ready for next use of scanner.

			if (optionMenu == 1) {
				name = nameInputter();
				clientName = name;
				if (!clientMap.containsKey(clientName)) {
					clientMap.put(clientName, new Client(clientName));
				}
				String repeat;
				do {
					Timestamp sessionDateTime = dateInputter(
							"Please enter a date and time for " + name + "'s session.");
				String packProg = packProgInputter();
				Client clientObject = clientMap.get(name);
				clientObject.setClientSessionLog(sessionDateTime, packProg);
				System.out.println(name + " " + clientObject.getClientSessionLog());
				updateDB(name, sessionDateTime, packProg, myCon);

					System.out.println("Would you like to enter another appointment for the same client?");
					repeat = scanMain.nextLine();
				} while (repeat.equalsIgnoreCase("yes") || repeat.startsWith("y"));

			}

			if (optionMenu == 2) {
				ResultSet result = null;
				Statement myStmt = null;
				try {
					myStmt = myCon.createStatement();
					ResultSet r = myStmt.executeQuery("SELECT client_name FROM client_manager.client_appointments");
					while (r.next()) {
					System.out
								.println(r.getString("client_name"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				String nameToDel = null;
				do {
					System.out.println("\nWhose record would you like to delete?");
					nameToDel = nameInputter();
					String scriptName = "SELECT client_name FROM client_manager.client_appointments WHERE client_name = '"
							+ nameToDel + "'";
					result = myStmt.executeQuery(scriptName);
					try {
					result.next();
					} catch (SQLException ex) {
						result = null;
						System.out.println("Please enter a name that currently exists within the database.");
					}
				} while(result == null);
				String scriptDate = "SELECT appointment_date_time FROM client_manager.client_appointments WHERE client_name = '"
						+ nameToDel + "' ORDER BY appointment_date_time";
				ResultSet r = myStmt.executeQuery(scriptDate);
				while (r.next()) {
					System.out.println(r.getString("appointment_date_time"));
				}
				List<String> packDateList = queryDB(nameToDel, myCon);
				int noMatchCounter = 0;

				do {
					System.out.println();
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Timestamp datetoDelete = dateInputter(
							"Which session date would you like to delete for " + nameToDel + "?");
					String dateToDel = dateFormat.format(datetoDelete);
					String scriptDel = "DELETE FROM client_manager.client_appointments WHERE appointment_date_time = '"
					+ dateToDel + "'";

					for (int i = 0; i < packDateList.size(); i++) {
						List<String> entryArr = new ArrayList<String>(Arrays.asList((packDateList.get(i)).split(",\\W")));
						if (dateToDel.equals(entryArr.get(1))) {
							try {
								PreparedStatement p = myCon.prepareStatement(scriptDel);
								p.executeUpdate();
								System.out.println("Entry deleted.");
								} catch (SQLException e) {
									e.printStackTrace();
								}
						} else {
							noMatchCounter += 1;
						}
					} if (noMatchCounter == packDateList.size()) {
						System.out.println("There is no appointment logged on this date for "+ nameToDel+". Please enter another date.");
					}
					}while (noMatchCounter == packDateList.size());
			}

			if (optionMenu == 3) {
				System.out.println(
						"Which client's history would you like to view? (Please enter full name with space in between)");
				String nameEnquiry = scanMain.nextLine();
				List<String> viewHist = queryDB(nameEnquiry, myCon);
				for (int index = 0; index < viewHist.size(); index++) {
					String[] oneLine = new String[2];
					oneLine = viewHist.get(index).split(",");
					System.out.println(oneLine[0] + ": " + oneLine[1]);
				}
			}
			if (optionMenu == 4) {
				// TODO Find date of nearest Monday before current date
				// Find current day use switch case to subtract appropriate no. of days to find
				// Monday date.
				// TODO Submit Query to SQL listing ordered dates for the current week
				SimpleDateFormat day = new SimpleDateFormat("EEEE");
				Date now = new Date();
				Calendar cal = Calendar.getInstance();
				cal.setTime(now);
				int currentDate = cal.get(Calendar.DATE);
				cal.set(Calendar.HOUR_OF_DAY, 00);// 24 hour clock
				cal.set(Calendar.MINUTE, 01);
				cal.set(Calendar.SECOND, 00);
				String currentDayOfWeek = day.format(now);
				System.out.println(currentDayOfWeek);
				switch (currentDayOfWeek) {
				case "Monday":
					// date stays the same.
					break;
				case "Tuesday":
					cal.set(Calendar.DATE, currentDate-1);
					break;
				case "Wednesday":
					cal.set(Calendar.DATE, currentDate-2);
					break;
				case "Thursday":
					cal.set(Calendar.DATE, currentDate-3);
					break;
				case "Friday":
					cal.set(Calendar.DATE, currentDate-4);
					break;
				case "Saturday":
					cal.set(Calendar.DATE, currentDate-5);
					break;
				case "Sunday":
					cal.set(Calendar.DATE, currentDate-6);
					break;
				}
				Date weekBeg= cal.getTime();
				cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 6);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				Date weekEnd = cal.getTime();
				SimpleDateFormat dateOut = new SimpleDateFormat("MMMM dd yyyy");
				System.out.println("You have selected to view the current week's appointments starting from Monday "
						+ dateOut.format(weekBeg) + " to Sunday " + dateOut.format(weekEnd) + ":\n");
				betweenTwoDates(weekBeg, weekEnd, myCon);
			}

			if (optionMenu == 5) {
				int currMonth = Calendar.getInstance().get(Calendar.MONTH);
				int currYear = Calendar.getInstance().get(Calendar.YEAR);
				betweenTwoDates(getMonthFirstLastDates(currMonth, currYear).get(0),
						getMonthFirstLastDates(currMonth, currYear).get(1), myCon);
			}

			if (optionMenu == 6) {
				// Validate entries
				// take month and year as input. plug into betweenTwoDates via
				// getMonthFirstLastDates

				System.out
						.println(
								"Which Month would you like to view? \n(Please enter in number form 1(Jan) - 12(Dec))");
				int month = 0;
				month = numChecker(month, 1, 12, " is not an option. Please enter a valid month between 1 & 12.");

				Calendar now = Calendar.getInstance();
				int yearCheck = now.get(Calendar.YEAR); // use as upper limit for checking valid entry.
				System.out.println("Which Year would you like to view this month in? \n2000 - present ");
				int year = 0;
				year = numChecker(year, 2000, yearCheck,
						" is not an option. Please enter a valid year between 2000 & the current year.");
				int sessionCount = betweenTwoDates(getMonthFirstLastDates(month - 1, year).get(0),
						getMonthFirstLastDates(month - 1, year).get(1), myCon);

			}
			if (optionMenu == 7) {
				Timestamp firstDate = dateInputter("Chose the start date for your search:");
				Timestamp lastDate = dateInputter("Chose the end date for your search:");
				int sessionCount = betweenTwoDates(firstDate, lastDate, myCon);
			}

			if (optionMenu == 8) {
				System.out.println("You have selected to exit the client manager. Goodbye. :)");
			}

		} while (optionMenu < 8);

		scanMain.close();
		System.exit(0);
	}

	public static int numChecker(int numberToCheck, int lowerLim, int upperLim, String msg) {
		boolean moveOn = true;
		while (moveOn == true) {// number checker method. inputs: (int numberToCheck,int lowerlim, int
								// upperlim,String msg)
			try { // obtain valid number from user input.
				numberToCheck = scanMain.nextInt();
				validateNumberEntry(numberToCheck, lowerLim, upperLim);
				moveOn = false;
			} catch (InputMismatchException e) {
				System.out.println("Please enter a Number not a string.");
				scanMain.nextLine();
			} catch (NumOutOfRangeException f) {
				System.out.println(numberToCheck + msg);
			}
		}
		return numberToCheck;
	}

	public static Connection estConnection() {
		try {
			String driverName = "com.mysql.cj.jdbc.Driver";
			try {
				Class.forName(driverName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/client_manager", "root", "");
			return con;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> queryDB(String nameToQuery, Connection myCon) // equivalent of reading
																							// from a database
	{
			try {
				Statement myStmt = myCon.createStatement();
				//3. Execute SQL query
			String script = "SELECT appointment_packnum, appointment_date_time "
					+ "FROM client_appointments WHERE client_name = '" + nameToQuery
					+ "' ORDER BY appointment_date_time";
			ResultSet myRs = myStmt.executeQuery(script);
				//4. Process the result set.
			List<String> resultArr = new ArrayList<String>();
			while (myRs.next()) {
				resultArr.add(myRs.getString("appointment_packnum") + ", " + myRs.getString("appointment_date_time"));
			}
			return resultArr;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		return null;
	}

	public static void updateDB(String nameToWrite, Timestamp dateTimeToWrite, String packnumToWrite,
			Connection myCon) {
		try {
		PreparedStatement p = myCon.prepareStatement(
					"insert into client_appointments "
							+ "(client_name, appointment_packnum, appointment_date_time)" + " values('" + nameToWrite
							+ "','" + packnumToWrite + "', ? )");
			p.setTimestamp(1, dateTimeToWrite);
			// PreparedStatement pStmt = myCon.prepareStatement(script2);
			p.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void validateNumberEntry(int num, int lowerLim, int upperLim) throws NumOutOfRangeException {
		if (num > upperLim || num < lowerLim) {
			throw new NumOutOfRangeException("");
		}
	}

	static List<Date> getMonthFirstLastDates(int month, int year) {
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
	//Start month date
	cal.set(Calendar.MONTH, month);//change month to int type
	cal.set(Calendar.YEAR, year);
	cal.set(Calendar.DATE, 01);
	cal.set(Calendar.HOUR_OF_DAY, 00);
	cal.set(Calendar.MINUTE, 00);
	// End month date
	cal.set(Calendar.MONTH, month);
	cal.set(Calendar.YEAR, year);
	cal2.set(Calendar.HOUR_OF_DAY, 23);
	cal2.set(Calendar.MINUTE, 59);
		if (month == 0 | month == 2 | month == 4 | month == 6 | month == 7 | month == 9 | month == 11) {
		//days = 31
		cal2.set(Calendar.DATE, 31);
		} else if (month == 1 && isLeapYear(year) == true) {
		//days = 29
		cal2.set(Calendar.DATE, 29);
		} else if (month == 1 && isLeapYear(year) == false) {
		//days = 28
		cal2.set(Calendar.DATE, 28);
	} else {
		//days = 30
		cal2.set(Calendar.DATE, 30);
	}
	Date start = cal.getTime();
	Date end = cal2.getTime();
		List<Date> firstLast = List.of(start, end);
		return firstLast;
	}

	static int betweenTwoDates(Date startDate, Date endDate, Connection myCon) throws SQLException {
		Timestamp sd = new Timestamp(startDate.getTime());
		Timestamp ed = new Timestamp(endDate.getTime());
		int sessionCount = 0;
		Statement myStmt = myCon.createStatement();
		String script = "SELECT client_name, appointment_packnum, appointment_date_time "
				+ "FROM client_appointments WHERE appointment_date_time BETWEEN '" + sd + "' AND '" + ed
				+ "' ORDER BY appointment_date_time";
		ResultSet data = myStmt.executeQuery(script);
		List<String> dataDisplay = new ArrayList<String>();
		while (data.next()) {
			dataDisplay.add(data.getString("client_name") + ": " + data.getString("appointment_packnum") + " DATE "
					+ data.getString("appointment_date_time") + ".");
			sessionCount += 1;
		}
		System.out.println("There are " + sessionCount + " appointments booked in for this period:");

		for (int i = 0; i < dataDisplay.size(); i++) {
			System.out.println(dataDisplay.get(i));
		}
		System.out.println();
		return sessionCount;
	}

	// public static String nameToKey(String name) {
	// List<String> nameparts = new
	// ArrayList<String>(Arrays.asList(name.split("\\s")));
	// String Key = nameparts.get(0) + nameparts.get(1) + "Client";
	// return Key;
	// }

	public static Matcher matcherExpression(String input, String regexPattern) {
		String patternNameString = regexPattern;
		Pattern patternName = Pattern.compile(patternNameString);
		Matcher matcherName = patternName.matcher(input);
		return matcherName;
	}

	public static boolean isLeapYear(int year) {
		boolean isLeapYear = false;
		if (year % 4 == 0 && year % 100 != 0) {
			isLeapYear = true;
		}
		if (year % 100 == 0) {
			if (year % 400 == 0) {
				isLeapYear = true;
			} else {
				isLeapYear = false;
			}
		}
		return isLeapYear;
	}

	public static String nameInputter() throws IOException {
		String inputName;
		String input = "initiated";
		do {
			System.out.println("Please enter a valid full name:");
			input = scanMain.nextLine();

			inputName = input;
		} while (!matcherExpression(inputName, "[a-zA-Z]+(\\s)[a-zA-Z]+").matches());
		return inputName;
	}

	public static Timestamp dateInputter(String msg) {
		String inputDate;
		do {
			// String input2 = "initiated";
			System.out.println(msg + "\n(Using the format yyyy/mm/dd hh:mm)");
			inputDate = scanMain.nextLine();
		} while (!matcherExpression(inputDate,
				"(\\d\\d\\d\\d)[\\W](\\d\\d)[\\W](\\d\\d)(\\s)(\\d\\d)[\\W](\\d\\d)")
				.matches());

		// Ideally set some other condition to establish whether date is valid (method
		// from Date class). example not allowing 30/02/2019.
		List<String> dateTimeObjects = new ArrayList<String>(Arrays.asList(inputDate.split("\\W|\\s")));
		int dateYear = Integer.parseInt(dateTimeObjects.get(0));
		int dateMonth = Integer.parseInt(dateTimeObjects.get(1));
		int dateDay = Integer.parseInt(dateTimeObjects.get(2));
		int timeHour = Integer.parseInt(dateTimeObjects.get(3));
		int timeMinute = Integer.parseInt(dateTimeObjects.get(4));

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 000000);
		cal.set(dateYear, dateMonth - 1, dateDay, timeHour, timeMinute, 00);
		Date entryDate = cal.getTime();
		Timestamp ts = new Timestamp(entryDate.getTime());// SQL Timestamp ready to update DB
		return ts;
	}

	public static String packProgInputter() {
		String packProgInput;
		do {
			System.out.println(
					"Please enter the session and package number in the format: (session num)/(total sessions bought)");
			packProgInput = scanMain.nextLine();
		} while (!matcherExpression(packProgInput, "((\\d\\d)|\\d)(\\W)([3]|[8]|[1][0]|[1][2]|[2][0])").matches());

		List<String> packProgObjects = new ArrayList<String>(Arrays.asList(packProgInput.split("\\W")));
		System.out.println("packProg Registered!");
		String packNum = "Session " + packProgObjects.get(0) + " out of " + packProgObjects.get(1);
		return packNum;
	}
}
